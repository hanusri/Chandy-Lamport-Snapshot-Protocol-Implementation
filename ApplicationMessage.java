package utd.com;

import java.io.Serializable;

/**
 * Created by Srikanth on 6/16/2016.
 */
public class ApplicationMessage extends Message implements Serializable {
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
