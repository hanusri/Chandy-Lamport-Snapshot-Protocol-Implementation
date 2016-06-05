package utd.com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class NodeRunner {
    private static int totalNodes;
    private static int maxMessages;
    private static HashMap<Integer,Node> nodeDictionary;
    private static int minPerActive;
    private static int maxPerActive;
    private static int minSendDelay;
    private static int snapShotDelay;


    public NodeRunner()
    {
        nodeDictionary = new HashMap<>();
    }

    public static int getTotalNodes() {
        return totalNodes;
    }

    public static int getMaxMessages() {
        return maxMessages;
    }

    public static int getMinPerActive() {
        return minPerActive;
    }

    public static int getMaxPerActive() {
        return maxPerActive;
    }

    public static int getMinSendDelay() {
        return minSendDelay;
    }

    public static int getSnapShotDelay() {
        return snapShotDelay;
    }

    public static HashMap<Integer, Node> getNodeDictionary() {
        return nodeDictionary;
    }

    private static ArrayList<ArrayList<Integer>> readFile(String fileName) throws IOException
    {
        File file = new File(fileName);
        System.out.println(file.getAbsoluteFile());
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedFile = new BufferedReader(fileReader);
        String newLine = null;
        int fileSection = 1;
        int nodeCounter = 0;
        ArrayList<ArrayList<Integer>> neighbourList = new ArrayList<>();

        while((newLine = bufferedFile.readLine()) != null)
        {
            // Comment line;Skip it
            if(newLine.startsWith(ApplicationConstants.COMMENT_INDICATOR))
                continue;
            // Blank line indicates end of section
            if(newLine.isEmpty())
            {
                fileSection++;
                continue;
            }

            String[] lineList = newLine.split("\\s+");
            if(fileSection == 1)
            {
                totalNodes = Integer.parseInt(lineList[0]);
                minPerActive = Integer.parseInt(lineList[1]);
                maxPerActive  = Integer.parseInt(lineList[2]);
                minSendDelay = Integer.parseInt(lineList[3]);
                snapShotDelay = Integer.parseInt(lineList[4]);
                maxMessages = Integer.parseInt(lineList[5]);
            }

            if(fileSection == 2)
            {
                if(nodeDictionary == null)
                    nodeDictionary = new HashMap<>();

                int nodeID = Integer.parseInt(lineList[0]);
                Node newNode = new Node(nodeID,lineList[1],Integer.parseInt(lineList[2]));
                nodeDictionary.put(nodeID,newNode);
            }

            if(fileSection == 3)
            {
                int i = 0;
                String newStringValue = lineList[i];
                ArrayList<Integer> neighbours = new ArrayList<>();
                while(!newStringValue.startsWith("#"))
                {
                    neighbours.add(Integer.parseInt(newStringValue));
                    newStringValue = lineList[++i];
                }
                neighbourList.add(neighbours);
            }
        }

        return neighbourList;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.print("Invalid Argument passed");
            System.exit(0);
        }
        try {
            ArrayList<ArrayList<Integer>> neighboursList = readFile(args[1]);

            // Create Current Node
            int currentNodeId = Integer.parseInt(args[0]);
            Node currentNode = nodeDictionary.get(currentNodeId);

            // Load its neighbours
            ArrayList<Node> neighbourNodes = new ArrayList<>();
            ArrayList<Integer> neighbours = neighboursList.get(currentNodeId);
            for(Integer i : neighbours)
            {
                Node neighbour = nodeDictionary.get(i);
                neighbourNodes.add(neighbour);
            }
            currentNode.setNodeNeighbour(neighbourNodes);

            // set up current node; close socket is pending
            currentNode.setUpSendControllerMap();
            currentNode.initializeNode();

        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
