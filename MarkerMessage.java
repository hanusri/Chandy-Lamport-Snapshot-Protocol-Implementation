package utd.com;

import java.io.Serializable;

/**
 * Created by Srikanth on 6/16/2016.
 * Marker message used as part of Chandy Lamport Protocol
 */
public class MarkerMessage extends Message implements Serializable {
    public MarkerMessage() {
        super();
    }

    public MarkerMessage(Node sourceNode) {
        super("", sourceNode);
    }
}
