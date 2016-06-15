package utd.com;

import jdk.nashorn.internal.runtime.FunctionInitializer;

import java.io.Serializable;
import java.util.ArrayList;

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
    int[] messageClock;

    public ApplicationMessage() {
        super();
        this.messageClock = new int[NodeRunner.getTotalNodes()];
    }

    public ApplicationMessage(int[] messageClock, Node sourceNode) {
        this.messageClock = new int[NodeRunner.getTotalNodes()];
        System.arraycopy(messageClock, 0, this.messageClock, 0, messageClock.length);
        this.setSourceNode(sourceNode);
    }
}

class MarkerMessage extends Message implements Serializable {
    public MarkerMessage() {
        super();
    }

    public MarkerMessage(Node sourceNode) {
        super("", sourceNode);
    }
}

class FinishMessage extends Message implements Serializable {
    public FinishMessage() {
        super();
    }

    public FinishMessage(Node sourceNode) {
        super("", sourceNode);
    }
}

class SnapshotMessage extends Message implements Serializable {
    private LocalState localState;
    private ArrayList<ChannelState> channelStates;

    public SnapshotMessage() {
        super();
        localState = new LocalState();
        channelStates = new ArrayList<>();
    }

    public SnapshotMessage(LocalState applicationState, ArrayList<ChannelState> channelStates, Node sourceNode) {
        this.localState = applicationState;
        this.channelStates = channelStates;
        this.setSourceNode(sourceNode);
    }

    public LocalState getLocalState() {
        return localState;
    }

    public void setLocalState(LocalState localState) {
        this.localState = localState;
    }

    public ArrayList<ChannelState> getChannelStates() {
        return channelStates;
    }

    public void setChannelStates(ArrayList<ChannelState> channelStates) {
        this.channelStates = channelStates;
    }
}

