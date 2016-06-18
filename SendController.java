package utd.com;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class SendController {
    public static final int SEND_SUCCESS = 1;
    public static final int SEND_FAILURE = 0;
    private final int MAXRETRIES = 10;
    private Node destination;

    public SendController() {

    }

    public SendController(Node destination) {
        this.destination = destination;
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public void send(Message sendMessage) {
        try {
            Socket socket = new Socket(this.destination.getIpAddress(), this.destination.getPort());
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
            outStream.writeObject(sendMessage);
            if (ApplicationConstants.MESSAGE_FLOW_LOG) {
                LogWriter log = new LogWriter(sendMessage.getSourceNode().getNodeID());
                log.setSentTimeStamp(System.nanoTime());
                log.setReceivedTimeStamp(System.nanoTime());
                log.setSentNode(sendMessage.getSourceNode().getNodeID());
                log.setReceivedNode(destination.getNodeID());
                String message = "";

                if (sendMessage instanceof ApplicationMessage) {
                    message = ApplicationConstants.APPLICATION_MESSAGE_LOG;
                } else if (sendMessage instanceof FinishMessage) {
                    message = ApplicationConstants.FINISH_MESSAGE_LOG;
                } else if (sendMessage instanceof MarkerMessage) {
                    message = ApplicationConstants.MARKER_MESSAGE_LOG;
                } else if (sendMessage instanceof SnapshotMessage) {
                    message = ApplicationConstants.SNAPSHOT_MESSAGE_LOG;
                }

                log.setMessageType(message);
                log.write();
            }
            outStream.close();
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

