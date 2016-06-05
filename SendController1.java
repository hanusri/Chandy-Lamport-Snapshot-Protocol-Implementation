package utd.com;

import java.io.IOException;
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
    private Socket socket;
    private ObjectOutputStream outStream;

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
        //ObjectOutputStream outStream = null;
        try {
            if (socket == null) {
                socket = new Socket(this.destination.getIpAddress(), this.destination.getPort());
                outStream = new ObjectOutputStream(socket.getOutputStream());
            }
            outStream.writeObject(sendMessage);
            outStream.flush();
            Thread.sleep(2000);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

