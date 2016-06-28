package utd.com;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class SendController {
    private HashMap<Integer, Socket> socketMap;
    private HashMap<Integer, ObjectOutputStream> outputMap;

    public SendController() {
        socketMap = new HashMap<>();
        outputMap = new HashMap<>();
    }

    public HashMap<Integer, ObjectOutputStream> getOutputMap() {
        return outputMap;
    }

    public void initializeController(ArrayList<Node> neighbourNodes) {
        if (neighbourNodes != null) {
            try {
                for (Node neighbour : neighbourNodes) {
                    Socket clientSocket = new Socket(neighbour.getIpAddress(), neighbour.getPort());
                    socketMap.put(neighbour.getNodeID(), clientSocket);
                    outputMap.put(neighbour.getNodeID(), new ObjectOutputStream(clientSocket.getOutputStream()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void haltController() {
        try {
            for (Integer nodeId : outputMap.keySet()) {
                outputMap.get(nodeId).close();
            }
            for (Integer nodeId : socketMap.keySet()) {
                socketMap.get(nodeId).close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void send(Node destinationNode, Message sendMessage) {
        try {
            if (outputMap.size() == 0) {
                initializeController(sendMessage.getSourceNode().getNeighbours());
            }
            ObjectOutputStream output = outputMap.get(destinationNode.getNodeID());
            output.writeObject(sendMessage);
            output.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

