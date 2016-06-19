package utd.com;

import jdk.nashorn.internal.objects.Global;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Srikanth on 6/18/2016.
 */
public class OutputFileWriter {
    private ArrayList<GlobalState> globalStates;

    public OutputFileWriter(ArrayList<GlobalState> globalStates) {
        this.globalStates = globalStates;
    }

    public ArrayList<GlobalState> getGlobalStates() {
        return globalStates;
    }

    public void setGlobalStates(ArrayList<GlobalState> globalStates) {
        this.globalStates = globalStates;
    }

    public void writeSnapShotOutput() {
        synchronized (this.globalStates) {
            try {
                for (GlobalState globalState : this.globalStates) {
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
                }
            } catch (IOException ex) {
                System.out.println("Error writing to file '" + "" + "'");
                // Or we could just do this: ex.printStackTrace();
            }
        }
    }

}
