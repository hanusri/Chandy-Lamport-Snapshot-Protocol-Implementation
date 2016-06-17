package utd.com;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Srikanth on 6/16/2016.
 */
public class SnapshotMessage extends Message implements Serializable {
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
