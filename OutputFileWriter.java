package utd.com;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Srikanth on 6/18/2016.
 */
public class OutputFileWriter {
    private GlobalState globalState;

    public OutputFileWriter(GlobalState globalState) {
        this.globalState = globalState;
    }

    public GlobalState getGlobalState() {
        return globalState;
    }

    public void setGlobalState(GlobalState globalState) {
        this.globalState = globalState;
    }

    public void writeSnapShotOutput() {
        synchronized (this) {
            try {
                for (LocalState localState : globalState.getLocalStates()) {
                    String fileName = NodeRunner.getConfigFileName() + "-" + localState.getNodeID() + ApplicationConstants.OUTPUTFILE_TYPE;
                    File file = new File(fileName);
                    FileWriter fileWriter;
                    boolean fileExists = false;
                    if (file.exists()) {
                        fileWriter = new FileWriter(file, true);
                        fileExists = true;
                    } else {
                        fileWriter = new FileWriter(file);
                    }
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    if (fileExists)
                        bufferedWriter.write("\n");

                    for (Integer vectorValue : localState.getApplicationState())
                        bufferedWriter.write(vectorValue + " ");

                    // Always close files.
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    fileWriter.close();
                }
            } catch (IOException ex) {
                System.out.println("Error writing to file '" + "" + "'");
                // Or we could just do this: ex.printStackTrace();
            }
        }
    }

}
