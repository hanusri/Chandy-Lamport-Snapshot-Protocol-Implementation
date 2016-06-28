package utd.com;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class Node implements Serializable {
    private int nodeID;
    private String ipAddress;
    private int port;
    private Node parentNode;
    private transient SendController sendController;
    private ArrayList<Node> neighbours;
    private transient Boolean activeStatus;
    private transient int sentMessageCount;
    private int[] applicationClock;
    private transient Color logStatus;
    private transient LocalState localState;
    private transient ArrayList<ChannelState> channelStates;
    private transient GlobalState globalState;
    private transient HashMap<Integer, Boolean> logMap;
    private transient int snapshotCount = 1;

    //region Constructors

    public Node(int nodeID) {
        this.nodeID = nodeID;
        sendController = new SendController();
        activeStatus = nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE;
        sentMessageCount = 0;
        applicationClock = new int[NodeRunner.getTotalNodes()];
        logStatus = Color.BLUE;
        localState = new LocalState();
        channelStates = new ArrayList<>();
        globalState = new GlobalState();
        logMap = new HashMap<>();
    }

    public Node(int nodeID, String ipAddress, int port) {
        this.nodeID = nodeID;
        this.ipAddress = ipAddress;
        this.port = port;
        activeStatus = nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE;
        sendController = new SendController();
        sentMessageCount = 0;
        applicationClock = new int[NodeRunner.getTotalNodes()];
        logStatus = Color.BLUE;
        localState = new LocalState();
        channelStates = new ArrayList<>();
        globalState = new GlobalState();
        logMap = new HashMap<>();
    }
    //endregion

    //region Getters and Setters
    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public ArrayList<Node> getNeighbours() {
        return neighbours;
    }
    //endregion

    //region Private Methods
    private int getRandomMessageCount() {
        Random randomGenerator = new Random();
        int difference = NodeRunner.getMaxPerActive() - NodeRunner.getMinPerActive() + 1;
        int randomNumber = randomGenerator.nextInt(difference);
        return NodeRunner.getMinPerActive() + randomNumber;
    }

    private Node getRandomNeighbour() {
        return neighbours.get(new Random().nextInt(neighbours.size()));
    }
    //endregion

    //region Chandy Lamport Protocol Methods

    private boolean isAllMarkerMessageReceived() {
        // Check using logmap if all marker messages are received
        for (Integer nodeId : logMap.keySet()) {
            if (!logMap.get(nodeId))
                return false;
        }
        return true;
    }

    private void startChandyLamportProtocol() {
        // called everytime when chandy lamport protocol thread is triggered
        processMarkerMessage(null);
    }

    private void processMarkerMessage(MarkerMessage receivedMarker) {
        synchronized (this) {
            if (this.logStatus == Color.BLUE) {
                //Set the log status as Red
                this.logStatus = Color.RED;
                // Copy Application Vector to Local State
                if (localState == null)
                    localState = new LocalState();
                localState.setApplicationState(this.applicationClock);
                localState.setActiveStatus(this.activeStatus);
                localState.setNodeID(this.nodeID);

                // Send Marker Message to its neighbours
                for (Node neighbour : neighbours) {
                    Message markerMessage = new MarkerMessage(this);
                    send(neighbour, markerMessage);
                }
                // Mark in coming marker channel as true if received marker is not null
                if (receivedMarker != null) {
                    logMap.put(receivedMarker.getSourceNode().getNodeID(), true);
                    // check if this nodes expects marker message from only one node ( if this node has only one neighbour in the topology
                    if (isAllMarkerMessageReceived()) {
                        this.logStatus = Color.BLUE;
                        SnapshotMessage snapShotMessage = new SnapshotMessage(this.localState, new ArrayList<>(), this);
                        // send the snapshot to its parent
                        sendSnapShotToParent(snapShotMessage);
                        // reset all chandy lamport parameters for another snapshot to be taken if needed
                        resetOtherNodes();
                    }
                }
            } else {
                // set the logMap of node from where marker message received as true
                logMap.put(receivedMarker.getSourceNode().getNodeID(), true);
                // this.logStatus != Color.BLUE is checked to ensure logic works multi threading access at almost same time
                if (isAllMarkerMessageReceived() && this.logStatus != Color.BLUE) {
                    this.logStatus = Color.BLUE;
                    // if current node is not default node (node 0), then forward the snapshot to its parent. Else add it to global state of default node.
                    if (this.getNodeID() != ApplicationConstants.DEFAULTNODE_ACTIVE) {
                        SnapshotMessage snapShotMessage = new SnapshotMessage(this.localState, this.channelStates, this);
                        sendSnapShotToParent(snapShotMessage);
                        resetOtherNodes();
                    } else {
                        this.globalState.addLocalState(localState);
                        for (ChannelState localChannelState : this.channelStates)
                            this.globalState.addChannelState(localChannelState);
                        // print the output
                        printSnapshotOutput();
                    }
                }
            }
        }
    }

    private void printSnapshotOutput() {
        // if default node has received all local state do the following
        //  a. add the current global state to NodeRunner.GlobalState
        //  b. start taking next snapshot or exit based on application status
        if (this.globalState.getLocalStates().size() == NodeRunner.getTotalNodes()) {
            NodeRunner.addGlobalStates(this.globalState);
            reSnapOrExitApplication();
        }
    }

    private void reSnapOrExitApplication() {
        // if application is passive then send finish message
        if (isApplicationPassive() && this.globalState.getChannelStates().size() == 0) {
            sendFinishMessage();
        } else {
            reSnapProtocol();
        }
    }

    private void writeOutputFile() {
        OutputFileWriter writeSnapshotOutput = new OutputFileWriter(NodeRunner.getGlobalStates());
        writeSnapshotOutput.writeSnapShotOutput();
    }

    private void sendSnapShotToParent(SnapshotMessage snapshotMessage) {
        send(this.parentNode, snapshotMessage);
    }

    private void reSnapProtocol() {
        logStatus = Color.BLUE;
        localState = new LocalState();
        channelStates = new ArrayList<>();
        globalState = new GlobalState();
        initializeLogMap();
        System.out.println("Snapshot Number " + (++snapshotCount));
        new Thread(new ChandyLamportThread()).start();
    }

    private void resetOtherNodes() {
        this.localState = new LocalState();
        this.channelStates = new ArrayList<>();
        initializeLogMap();
    }

    private void sendFinishMessage() {
        // send finish message to all neighbours
        for (Node neighbour : this.neighbours) {
            if (neighbour.nodeID != ApplicationConstants.DEFAULTNODE_ACTIVE) {
                FinishMessage finishMessage = new FinishMessage(this);
                send(neighbour, finishMessage);
            }
        }
        // if current node is default node then write the snapshot to a file
        if (this.nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE) {
            writeOutputFile();
            if (ApplicationConstants.CHECK_SNAPSHOT_CONSISTENCY)
                checkSnapShotConsistency();
        }
        // close all socket and objectoutputstream
        sendController.haltController();
        System.exit(0);
    }

    private void checkSnapShotConsistency() {
        for (int snapshotIndex = 0; snapshotIndex < NodeRunner.getGlobalStates().size(); snapshotIndex++) {
            GlobalState globalState = NodeRunner.getGlobalStates().get(snapshotIndex);
            boolean isSnapshotConsistent = true;
            int ithProcess = 0;
            while (ithProcess < NodeRunner.getTotalNodes()) {
                int ithVectorValue = globalState.getLocalStateByNodeId(ithProcess).getApplicationState()[ithProcess];
                for (int i = 0; i < globalState.getLocalStates().size(); i++) {
                    if (i != ithProcess) {
                        int jthVectorValue = globalState.getLocalStates().get(i).getApplicationState()[ithProcess];
                        if (jthVectorValue > ithVectorValue) {
                            isSnapshotConsistent = false;
                            System.out.println("Global Snapshot number " + (snapshotIndex + 1) + " is not consistent");
                            System.out.println("Process in Node " + ithProcess + " is invalid");
                            break;
                        }
                    }
                }
                ithProcess++;
            }
            if (isSnapshotConsistent)
                System.out.println("Global Snapshot number " + (snapshotIndex + 1) + " is consistent");
        }
    }

    private boolean isApplicationPassive() {
        // is all nodes passive
        for (LocalState nodeLocalState : this.globalState.getLocalStates()) {
            if (nodeLocalState.isActiveStatus())
                return false;
        }

        return true;
    }

    public void initializeLogMap() {
        // set or reset logmap to false
        this.logMap = new HashMap<>();
        for (Node neighbourNode : this.neighbours) {
            this.logMap.put(neighbourNode.nodeID, false);
        }
    }
    //endregion

    //region Public Methods

    private void sendApplicationMessages() {
        Message sendMessage;
        StringBuilder message;
        Integer messagePerActive;
        try {
            // get random message count each time node becomes active
            messagePerActive = getRandomMessageCount();
            for (int i = 0; i < messagePerActive; i++) {
                synchronized (this.applicationClock) {
                    if (this.activeStatus && (sentMessageCount < NodeRunner.getMaxMessages())) {
                        this.applicationClock[this.getNodeID()]++;
                        sendMessage = new ApplicationMessage(this.applicationClock, this);
                        send(getRandomNeighbour(), sendMessage);
                        sentMessageCount++;
                    } else
                        break;
                }
                Thread.sleep(NodeRunner.getMinSendDelay());
            }
            System.out.println(this);
            // set node as passive after it sends all messages
            synchronized (this.activeStatus) {
                activeStatus = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private void send(Node destinationNode, Message sendMessage) {
        // since both chandy lamport protocol thread and message processing thread uses this resource, it must be synchronized
        synchronized (sendController) {
            sendController.send(destinationNode, sendMessage);
        }
    }

    public void setNodeNeighbour(ArrayList<Node> neighbours) {
        this.neighbours = neighbours;
    }

    public void initializeNode() {
        // Start Listening thread
        new Thread(new ListenerThread()).start();
        try {
            // Put the main thread in sleep for few seconds
            Thread.sleep(ApplicationConstants.INITIAL_THREAD_DELAY);
            sendController.initializeController(neighbours);
            if (nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE) {
                Thread.sleep(ApplicationConstants.MAP_PROTOCOL_INITIAL_DELAY);
                sendApplicationMessages();
                new Thread(new ChandyLamportThread()).start();
            }

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Node ID " + getNodeID() + " : [");
        if (applicationClock != null) {
            for (int i : applicationClock) {
                sb.append(i + " ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
//endregion

    //region Threads
    class ListenerThread implements Runnable {
        //private Socket socket;
        private ServerSocket serverSocket;
        private ObjectInputStream inputStream;

        public ListenerThread() {

        }

        @Override
        public void run() {
            try {
                System.out.print("Started Listening from " + nodeID);
                serverSocket = new ServerSocket(getPort());
                // creating new message procesisng thread for each socket getting openned
                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(new MessageProcessingThread(socket)).start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MessageProcessingThread implements Runnable {
        ObjectInputStream inputStream;
        Socket socket;

        public MessageProcessingThread(Socket socket) {
            this.socket = socket;
            System.out.println(socket.toString());
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Message incomingMessage = (Message) inputStream.readObject();
                    if (incomingMessage instanceof MarkerMessage) { // if incoming message is marker message
                        System.out.println("Received marker message");
                        MarkerMessage markerMessage = (MarkerMessage) incomingMessage;
                        processMarkerMessage(markerMessage);
                    } else if (incomingMessage instanceof ApplicationMessage) { // if incoming message is Application message
                        synchronized (Node.this.activeStatus) {
                            Node.this.activeStatus = (Node.this.sentMessageCount < NodeRunner.getMaxMessages());
                        }
                        ApplicationMessage applicationMessage = (ApplicationMessage) incomingMessage;
                        synchronized (Node.this.applicationClock) {
                            for (int i = 0; i < Node.this.applicationClock.length; i++) {
                                Node.this.applicationClock[i] = Math.max(Node.this.applicationClock[i],
                                        applicationMessage.messageClock[i]);
                            }
                            Node.this.applicationClock[Node.this.getNodeID()]++;
                        }
                        // Record Channel State
                        if (Node.this.logStatus == Color.RED && !Node.this.logMap.get(applicationMessage.getSourceNode().getNodeID())) {
                            synchronized (Node.this.channelStates) {
                                if (Node.this.channelStates == null)
                                    Node.this.channelStates = new ArrayList<>();

                                ChannelState newChannelState = new ChannelState(applicationMessage.getSourceNode(), Node.this, applicationMessage.messageClock);
                                Node.this.channelStates.add(newChannelState);
                            }
                        }
                        if (Node.this.activeStatus && Node.this.sentMessageCount < NodeRunner.getMaxMessages())
                            sendApplicationMessages();
                    } else if (incomingMessage instanceof SnapshotMessage) { // if incoming message is Snapshot message
                        SnapshotMessage snapshotMessage = (SnapshotMessage) incomingMessage;
                        if (Node.this.getNodeID() != ApplicationConstants.DEFAULTNODE_ACTIVE) {
                            sendSnapShotToParent(snapshotMessage);
                        } else {
                            synchronized (Node.this.globalState) {
                                Node.this.globalState.addLocalState(snapshotMessage.getLocalState());
                                for (ChannelState messageChannelState : snapshotMessage.getChannelStates()) {
                                    Node.this.globalState.addChannelState(messageChannelState);
                                }
                            }
                            printSnapshotOutput();
                        }
                    } else if (incomingMessage instanceof FinishMessage) {
                        sendFinishMessage();
                    }
                } catch (EOFException ex) {

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    class ChandyLamportThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(NodeRunner.getSnapShotDelay());
                startChandyLamportProtocol();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
//endregion

}

enum Color {
    BLUE,
    RED
}
