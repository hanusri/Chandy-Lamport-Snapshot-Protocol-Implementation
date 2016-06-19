package utd.com;

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
    private transient HashMap<Integer, SendController> sendControllerMap;
    private ArrayList<Node> neighbours;
    private Boolean activeStatus;
    private transient int sentMessageCount;
    private int[] applicationClock;
    private transient Color logStatus;
    private transient LocalState localState;
    private transient ArrayList<ChannelState> channelStates;
    private transient GlobalState globalState;
    private transient HashMap<Integer, Boolean> logMap;


    //region Constructors
    public Node(int nodeID) {
        this.nodeID = nodeID;
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
        for (Integer nodeId : logMap.keySet()) {
            if (!logMap.get(nodeId))
                return false;
        }
        return true;
    }

    private void startChandyLamportProtocol() {
        processMarkerMessage(null);
    }

    private void processMarkerMessage(MarkerMessage receivedMarker) {
        if (this.logStatus == Color.BLUE) {
            synchronized (this.logStatus) {
                //Set the log status as Red
                this.logStatus = Color.RED;
            }
            synchronized (this.localState) {
                // Copy Application Vector to Local State
                if (localState == null)
                    localState = new LocalState();
                localState.setApplicationState(this.applicationClock);
                localState.setActiveStatus(this.activeStatus);
                localState.setNodeID(this.nodeID);
            }
            // Send Marker Message to its neighbours
            for (Node neighbour : neighbours) {
                Message markerMessage = new MarkerMessage(this);
                send(neighbour, markerMessage);
            }
            // Mark in coming marker channel as true if received marker is not null
            if (receivedMarker != null) {
                synchronized (this.logMap) {
                    logMap.put(receivedMarker.getSourceNode().getNodeID(), true);
                }
                if (isAllMarkerMessageReceived()) {
                    synchronized (this.logStatus) {
                        this.logStatus = Color.BLUE;
                    }
                    SnapshotMessage snapShotMessage = new SnapshotMessage(this.localState, new ArrayList<>(), this);
                    sendSnapShotToParent(snapShotMessage);
                }
            }
        } else {
            synchronized (this.logMap) {
                logMap.put(receivedMarker.getSourceNode().getNodeID(), true);
            }
            if (isAllMarkerMessageReceived() && this.logStatus != Color.BLUE) {
                synchronized (this.logMap) {
                    this.logStatus = Color.BLUE;
                }
                if (this.getNodeID() != ApplicationConstants.DEFAULTNODE_ACTIVE) {
                    SnapshotMessage snapShotMessage = new SnapshotMessage(this.localState, this.channelStates, this);
                    sendSnapShotToParent(snapShotMessage);
                } else {
                    synchronized (this.globalState) {
                        this.globalState.addLocalState(localState);
                        for (ChannelState localChannelState : this.channelStates)
                            this.globalState.addChannelState(localChannelState);
                    }
                    // print the output
                    printSnapshotOutput();
                }
            }
        }
    }

    private void printSnapshotOutput() {
        if (this.globalState.getLocalStates().size() == NodeRunner.getTotalNodes()) {
            NodeRunner.addGlobalStates(this.globalState);
            reSnapOrExitApplication();
        }
    }

    private void reSnapOrExitApplication() {
        if (isApplicationPassive()) {
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
        resetOtherNodes();
    }

    private void reSnapProtocol() {
        synchronized (this) {
            logStatus = Color.BLUE;
            localState = new LocalState();
            channelStates = new ArrayList<>();
            globalState = new GlobalState();
            initializeLogMap();
            new Thread(new ChandyLamportThread()).start();
        }
    }

    private void resetOtherNodes() {
        synchronized (this) {
            this.localState = new LocalState();
            this.channelStates = new ArrayList<>();
            initializeLogMap();
        }
    }

    private void sendFinishMessage() {
        for (Node neighbour : this.neighbours) {
            if (neighbour.nodeID != ApplicationConstants.DEFAULTNODE_ACTIVE) {
                FinishMessage finishMessage = new FinishMessage(this);
                send(neighbour, finishMessage);
            }
        }
        if (this.nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE)
            writeOutputFile();

        System.exit(0);
    }

    private boolean isApplicationPassive() {
        for (LocalState nodeLocalState : this.globalState.getLocalStates()) {
            if (nodeLocalState.isActiveStatus())
                return false;
        }

        return true;
    }

    private void initializeLogMap() {
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
            synchronized (this.activeStatus) {
                activeStatus = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private void send(Node destinationNode, Message sendMessage) {
        synchronized (this.sendControllerMap) {
            sendControllerMap.get(destinationNode.getNodeID()).send(sendMessage);
        }
    }

    public void setNodeNeighbour(ArrayList<Node> neighbours) {
        this.neighbours = neighbours;
    }

    public void setUpNeighbourMap() {
        if (sendControllerMap == null)
            sendControllerMap = new HashMap<>();

        for (Node neighbourNode : this.neighbours) {
            sendControllerMap.put(neighbourNode.nodeID, new SendController(neighbourNode));
        }

        initializeLogMap();
    }

    public void initializeNode() {
        // Start Listening thread
        new Thread(new ListenerThread()).start();
        try {
            if (nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE) {
                // Put the main thread in sleep for few seconds
                Thread.sleep(ApplicationConstants.INITIAL_THREAD_DELAY);
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
        private Socket socket;
        private ServerSocket serverSocket;
        private ObjectInputStream inputStream;


        public ListenerThread() {

        }

        @Override
        public void run() {
            try {
                System.out.print("Started Listening from " + nodeID);
                serverSocket = new ServerSocket(getPort());
                while (true) {
                    socket = serverSocket.accept();
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    Message incomingMessage = (Message) inputStream.readObject();
                    new Thread(new MessageProcessingThread(incomingMessage)).start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MessageProcessingThread implements Runnable {
        private Message incomingMessage;

        public MessageProcessingThread(Message incomingMessage) {
            this.incomingMessage = incomingMessage;
        }

        @Override
        public void run() {
            try {
                // if incoming message is marker message
                if (incomingMessage instanceof MarkerMessage) {
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
            } catch (Exception ex) {
                ex.printStackTrace();
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
