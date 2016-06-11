package utd.com;

import java.io.Serializable;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class Message implements Serializable {
    private String message;
    private Node sourceNode;


    public Message() {

    }

    public Message(String message, Node sourceNode) {
        this.message = message;
        this.sourceNode = sourceNode;

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
}

class ApplicationMessage extends Message implements Serializable {
    int[] messageVector;

    public ApplicationMessage() {
        super();
        this.messageVector = new int[NodeRunner.getTotalNodes()];
    }

    public ApplicationMessage(int[] messageVector, Node sourceNode)
    {
        this.messageVector = new int[NodeRunner.getTotalNodes()];
        System.arraycopy(messageVector,0,this.messageVector,0,messageVector.length);
        this.setSourceNode(sourceNode);
    }
}
