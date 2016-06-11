package utd.com;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class Node implements Serializable {
    private int nodeID;
    private String ipAddress;
    private int port;
    private transient HashMap<Integer, SendController> sendControllerMap;
    private ArrayList<Node> neighbours;
    private AtomicBoolean activeStatus;
    private transient int sentMessageCount;
    private int[] applicationClock;
    private transient Color logStatus;
    private LocalState localState;
    private ArrayList<ChannelState> channelStates;
    private GlobalState globalState;

    public Node(int nodeID) {
        this.nodeID = nodeID;
        activeStatus = new AtomicBoolean(nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE);
        sentMessageCount = 0;
        applicationClock = new int[NodeRunner.getTotalNodes()];
        logStatus = Color.BLUE;
        localState = new LocalState();
        channelStates = new ArrayList<>();
        globalState = new GlobalState();
    }

    public Node(int nodeID, String ipAddress, int port) {
        this.nodeID = nodeID;
        this.ipAddress = ipAddress;
        this.port = port;
        activeStatus = new AtomicBoolean(nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE);
        sentMessageCount = 0;
        applicationClock = new int[NodeRunner.getTotalNodes()];
        logStatus = Color.BLUE;
        localState = new LocalState();
        channelStates = new ArrayList<>();
        globalState = new GlobalState();
    }

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

    private int getRandomMessageCount() {
        Random randomGenerator = new Random();
        int difference = NodeRunner.getMaxPerActive() - NodeRunner.getMinPerActive() + 1;
        int randomNumber = randomGenerator.nextInt(difference);
        return NodeRunner.getMinPerActive() + randomNumber;
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

    private Node getRandomNeighbour() {
        return neighbours.get(new Random().nextInt(neighbours.size()));
    }

    public ArrayList<Node> getNeighbours() {
        return neighbours;
    }

    public void setNodeNeighbour(ArrayList<Node> neighbours) {
        this.neighbours = neighbours;
    }

    public void setUpSendControllerMap() {
        if (sendControllerMap == null)
            sendControllerMap = new HashMap<>();

        for (Integer key : NodeRunner.getNodeDictionary().keySet())
            sendControllerMap.put(key, new SendController(NodeRunner.getNodeDictionary().get(key)));
    }


    public void initializeNode() {
        // Start Listening thread
        new Thread(new ListenerThread(this)).start();
        try {
            // Put the main thread in sleep for few seconds
            Thread.sleep(ApplicationConstants.INITIAL_THREAD_DELAY);
            multicastMessages();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void multicastMessages() {
        if (this.activeStatus.get() && (sentMessageCount < NodeRunner.getMaxMessages())) {
            Message sendMessage;
            StringBuilder message;
            int messagePerActive;
            try {
                messagePerActive = getRandomMessageCount();
                System.out.println("Random Number Generated :" + messagePerActive);
                for (int i = 0; i < messagePerActive; i++) {
                    message = new StringBuilder("Actual Message from " + getNodeID());
                    synchronized (this.applicationClock) {
                        this.applicationClock[this.getNodeID()]++;
                    }
                    sendMessage = new ApplicationMessage(this.applicationClock, this);
                    send(getRandomNeighbour(), sendMessage);
                    sentMessageCount++;
                    Thread.sleep(NodeRunner.getMinSendDelay());
                }
                activeStatus.getAndSet(false);
                System.out.println(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void send(Node destinationNode, Message sendMessage) {
        sendControllerMap.get(destinationNode.getNodeID()).send(sendMessage);
    }

    class ListenerThread implements Runnable {
        private Socket socket;
        private ServerSocket serverSocket;
        private ObjectInputStream inputStream;
        private Node currentNode;

        public ListenerThread(Node currentNode) {
            this.currentNode = currentNode;
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(getPort());
                System.out.println("Listening from Node " + getNodeID());
                while (true) {
                    socket = serverSocket.accept();
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    Message receivedMsg = (Message) inputStream.readObject();

                    if (receivedMsg instanceof ApplicationMessage) {
                        currentNode.activeStatus.getAndSet(currentNode.sentMessageCount < NodeRunner.getMaxMessages());
                        ApplicationMessage receivedApplicationMessage = (ApplicationMessage) receivedMsg;
                        synchronized (currentNode.applicationClock) {
                            for (int i = 0; i < currentNode.applicationClock.length; i++) {
                                currentNode.applicationClock[i] = Math.max(currentNode.applicationClock[i],
                                        receivedApplicationMessage.getSourceNode().applicationClock[i]);
                            }
                            currentNode.applicationClock[currentNode.getNodeID()]++;
                        }
                        System.out.println(currentNode);
                        multicastMessages();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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
}

enum Color {
    BLUE,
    RED
}
