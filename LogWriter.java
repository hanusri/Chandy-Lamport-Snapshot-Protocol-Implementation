package utd.com;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by ram on 6/17/16.
 */
public class LogWriter {

    Long time;
    Long sentTimeStamp;
    Long receivedTimeStamp;
    int sentNode;
    int receivedNode;
    String messageType;
    FileWriter fileWritter;
    BufferedWriter bufferWritter;

    public LogWriter(int node) throws IOException{

        this.time = System.nanoTime();
        File outputFile = new File("log"+ node +".txt" );
        fileWritter = new FileWriter(outputFile.getName(),true);
        bufferWritter = new BufferedWriter(fileWritter);

        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
    }

    public LogWriter() {
        this.time = System.nanoTime();
    }

    public void setSentTimeStamp(long timeStamp) {

        this.sentTimeStamp = timeStamp;
    }

    public void setReceivedTimeStamp(long timeStamp) {
        this.receivedTimeStamp = timeStamp;
    }

    public void setSentNode(int node) {
        this.sentNode = node;
    }

    public void setReceivedNode(int node) {
        this.receivedNode = node;
    }

    public void setMessageType(String message) {
        this.messageType = message;
    }

    public void write() throws IOException {
        StringBuilder output = new StringBuilder(time + "," + sentTimeStamp + "," + receivedTimeStamp + "," + sentNode + "," + receivedNode + "," + messageType + "\n");
        //System.out.println(output);

        bufferWritter.write(output.toString());
        bufferWritter.flush();
        bufferWritter.close();
        fileWritter.close();
    }
}
