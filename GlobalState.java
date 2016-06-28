package utd.com;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Srikanth on 6/11/2016.
 * This class holds global state of single snapshot
 */
public class GlobalState implements Serializable {
    private ArrayList<LocalState> localStates;
    private ArrayList<ChannelState> channelStates;

    public GlobalState() {
        localStates = new ArrayList<>();
        channelStates = new ArrayList<>();
    }

    public ArrayList<LocalState> getLocalStates() {
        return localStates;
    }

    public void setLocalStates(ArrayList<LocalState> localStates) {
        this.localStates = localStates;
    }

    public ArrayList<ChannelState> getChannelStates() {
        return channelStates;
    }

    public void setChannelStates(ArrayList<ChannelState> channelStates) {
        this.channelStates = channelStates;
    }

    public void addChannelState(ChannelState channelState) {
        if (channelStates == null)
            channelStates = new ArrayList<>();

        channelStates.add(channelState);
    }

    public void addLocalState(LocalState localState) {
        if (localStates == null)
            localStates = new ArrayList<>();

        localStates.add(localState);
    }

    public LocalState getLocalStateByNodeId(int nodeId) {
        for (LocalState localState : this.localStates) {
            if (localState.getNodeID() == nodeId)
                return localState;
        }
        return null;
    }
}
