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
    private transient HashMap<Integer, SendController> sendControllerMap;
    private ArrayList<Node> neighbours;
    private boolean activeStatus;
    private int sentMessageCount;

    public Node(int nodeID) {
        this.nodeID = nodeID;
        activeStatus = nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE;
        sentMessageCount = 0;
    }

    public Node(int nodeID, String ipAddress, int port) {
        this.nodeID = nodeID;
        this.ipAddress = ipAddress;
        this.port = port;
        activeStatus = nodeID == ApplicationConstants.DEFAULTNODE_ACTIVE;
        sentMessageCount = 0;
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

        /*for (Integer key : NodeRunner.getNodeDictionary().keySet())
            sendControllerMap.put(key, new SendController(NodeRunner.getNodeDictionary().get(key)));*/

        for (Integer key : NodeRunner.getNodeDictionary().keySet()) {
            Node neighbourNode = NodeRunner.getNodeDictionary().get(key);
            sendControllerMap.put(key, new SendController(neighbourNode.getIpAddress(), neighbourNode.getPort()));
        }
    }

    public void initializeNode() {
        // Start Listening thread
        new Thread(new ListenerThread(this)).start();
        try {
            // Put the main thread in sleep for few seconds
            Thread.sleep(30000);
            multicastMessages();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void multicastMessages() {
        if (this.activeStatus && (sentMessageCount < NodeRunner.getMaxMessages())) {
            try {
                int messagePerActive = getRandomMessageCount();
                Message sendMessage;
                StringBuilder message;
                for (int i = 0; i < messagePerActive; i++) {
                    message = new StringBuilder("Actual Message from " + getNodeID());
                    sendMessage = new Message(message.toString(), this, ApplicationConstants.MESSAGE_ACTUAL);
                    send(getRandomNeighbour(), sendMessage);
                    sentMessageCount++;
                    Thread.sleep(NodeRunner.getMinSendDelay());
                }
                activeStatus = false;
                System.out.println("Current Node " + getNodeID() + " active status is " + activeStatus);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void send(Node destinationNode, Message sendMessage) {
        SendController sendController = sendControllerMap.get(destinationNode.getNodeID());
        sendController.send(sendMessage);
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
                    switch (receivedMsg.getMessageType()) {
                        case ApplicationConstants.MESSAGE_ACTUAL: {
                            currentNode.activeStatus = currentNode.sentMessageCount < NodeRunner.getMaxMessages();
                            System.out.println("Current Node " + getNodeID() + " active status is " + currentNode.activeStatus);
                            System.out.println("Received following message from " + receivedMsg.getSourceNode().getNodeID());
                            System.out.println(receivedMsg.getMessage());
                            // sending acknowledgement message back
                            Message acknowledgementMessage = new Message("Message Acknowledged", currentNode,
                                    ApplicationConstants.MESSAGE_ACKNOWLEDGEMENT);
                            send(receivedMsg.getSourceNode(), acknowledgementMessage);
                            // Receiving node starts sending messages
                            multicastMessages();
                            break;
                        }
                        case ApplicationConstants.MESSAGE_ACKNOWLEDGEMENT: {
                            System.out.println("Received Acknowledgement from " + receivedMsg.getSourceNode().getNodeID());
                            break;
                        }
                        case ApplicationConstants.Message_FINAL:{
                            System.exit(0);
                            break;
                        }
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
