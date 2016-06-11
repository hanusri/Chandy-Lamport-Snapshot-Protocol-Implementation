package utd.com;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Srikanth on 6/11/2016.
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
}
