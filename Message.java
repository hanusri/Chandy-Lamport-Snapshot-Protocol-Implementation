package utd.com;

import java.io.Serializable;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class Message implements Serializable {
    private String message;
    private Node sourceNode;
    private String messageType;

    public Message()
    {

    }

    public Message(String message, Node sourceNode, String messageType) {
        this.message = message;
        this.sourceNode = sourceNode;
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
