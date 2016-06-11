package utd.com;

import java.io.Serializable;

/**
 * Created by Srikanth on 6/11/2016.
 */
public class LocalState implements Serializable {
    private int[] applicationState;

    LocalState() {
        applicationState = new int[NodeRunner.getTotalNodes()];
    }

    public int[] getApplicationState() {
        return applicationState;
    }

    public void setApplicationState(int[] applicationState) {
        System.arraycopy(applicationState, 0, this.applicationState, 0, applicationState.length);
    }
}
