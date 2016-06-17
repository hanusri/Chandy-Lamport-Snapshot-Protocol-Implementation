package utd.com;

import java.io.Serializable;

/**
 * Created by Srikanth on 6/16/2016.
 */
public class FinishMessage extends Message implements Serializable {
    public FinishMessage() {
        super();
    }

    public FinishMessage(Node sourceNode) {
        super("", sourceNode);
    }
}
