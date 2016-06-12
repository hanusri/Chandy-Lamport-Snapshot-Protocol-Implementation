package utd.com;

import java.io.Serializable;

/**
 * Created by Srikanth on 6/11/2016.
 */
public class ChannelState implements Serializable {
    private Node sourceNode;
    private Node receiveNode;

    private int[] channelClock;

    public ChannelState() {
        channelClock = new int[NodeRunner.getTotalNodes()];
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public int[] getChannelClock() {
        return channelClock;
    }

    public void setChannelClock(int[] channelClock) {
        System.arraycopy(channelClock, 0, this.channelClock, 0, channelClock.length);
    }

    public Node getReceiveNode() {
        return receiveNode;
    }

    public void setReceiveNode(Node receiveNode) {
        this.receiveNode = receiveNode;
    }
}
