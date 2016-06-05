package utd.com;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class SendController {
    public static final int SEND_SUCCESS = 1;
    public static final int SEND_FAILURE = 0;
    private final int MAXRETRIES = 10;
    private String ipAddressToSend;
    private int portToSend;

    public SendController()
    {

    }

    public SendController(String ipAddressToSend, int portToSend) {
        this.ipAddressToSend = ipAddressToSend;
        this.portToSend = portToSend;
    }

    public String getIpAddressToSend() {
        return ipAddressToSend;
    }

    public void setIpAddressToSend(String ipAddressToSend) {
        this.ipAddressToSend = ipAddressToSend;
    }

    public int getPortToSend() {
        return portToSend;
    }

    public void setPortToSend(int portToSend) {
        this.portToSend = portToSend;
    }

    public void send(Message sendMessage) {
        Socket socket = null;
        ObjectOutputStream outStream = null;
        try {
            socket = new Socket(ipAddressToSend, portToSend);
            outStream = new ObjectOutputStream(socket.getOutputStream());
            outStream.writeObject(sendMessage);
            outStream.flush();
            socket.close();
            Thread.sleep(1000);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
