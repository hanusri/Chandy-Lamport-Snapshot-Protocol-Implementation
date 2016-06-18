package utd.com;

import java.io.Serializable;

/**
 * Created by Srikanth on 6/11/2016.
 */
public class LocalState implements Serializable {
    private int[] applicationState;
    private boolean activeStatus;
    private int nodeID;

    LocalState() {
        applicationState = new int[NodeRunner.getTotalNodes()];
        activeStatus = false;
    }

    public int[] getApplicationState() {
        return applicationState;
    }

    public void setApplicationState(int[] applicationState) {
        System.arraycopy(applicationState, 0, this.applicationState, 0, applicationState.length);
    }

    public boolean isActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }
}
