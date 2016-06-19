package utd.com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Srikanth on 6/2/2016.
 */
public class NodeRunner {
    private static int totalNodes;
    private static int maxMessages;
    private static HashMap<Integer, Node> nodeDictionary;
    private static int minPerActive;
    private static int maxPerActive;
    private static int minSendDelay;
    private static int snapShotDelay;
    private static String configFileName;
    private static ArrayList<GlobalState> globalStates;

    public NodeRunner() {
        nodeDictionary = new HashMap<>();
        globalStates = new ArrayList<>();
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

    public static String getConfigFileName() {
        return configFileName;
    }

    public static void setConfigFileName(String configFileName) {
        NodeRunner.configFileName = configFileName;
    }

    public static ArrayList<GlobalState> getGlobalStates() {
        return globalStates;
    }

    public static void addGlobalStates(GlobalState globalState) {
        if(globalStates == null)
            globalStates = new ArrayList<>();

        globalStates.add(globalState);
    }

    public static HashMap<Integer, Node> getNodeDictionary() {
        return nodeDictionary;
    }

    private static ArrayList<ArrayList<Integer>> readFile(String fileName) throws IOException {
        configFileName = fileName.substring(0, fileName.lastIndexOf('.'));
        File file = new File(fileName);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedFile = new BufferedReader(fileReader);
        String newLine = null;
        int fileSection = 1;
        int nodeCounter = 0;
        ArrayList<ArrayList<Integer>> neighbourList = new ArrayList<>();

        while ((newLine = bufferedFile.readLine()) != null) {
            // Comment line;Skip it
            if (newLine.startsWith(ApplicationConstants.COMMENT_INDICATOR))
                continue;
            // Blank line indicates end of section
            if (newLine.isEmpty()) {
                fileSection++;
                continue;
            }

            String[] lineList = newLine.split("\\s+");
            if (fileSection == 1) {
                totalNodes = Integer.parseInt(lineList[0]);
                minPerActive = Integer.parseInt(lineList[1]);
                maxPerActive = Integer.parseInt(lineList[2]);
                minSendDelay = Integer.parseInt(lineList[3]);
                snapShotDelay = Integer.parseInt(lineList[4]);
                maxMessages = Integer.parseInt(lineList[5]);
            }

            if (fileSection == 2) {
                if (nodeDictionary == null)
                    nodeDictionary = new HashMap<>();

                int nodeID = Integer.parseInt(lineList[0]);
                Node newNode = new Node(nodeID, lineList[1], Integer.parseInt(lineList[2]));
                nodeDictionary.put(nodeID, newNode);
            }

            if (fileSection == 3) {
                int i = 0;
                String newStringValue = lineList[i];
                ArrayList<Integer> neighbours = new ArrayList<>();
                while (!newStringValue.startsWith("#")) {
                    neighbours.add(Integer.parseInt(newStringValue));
                    newStringValue = lineList[++i];
                }
                neighbourList.add(neighbours);
            }
        }
        // build spanning tree for converge cast
        buildSpanningTree(neighbourList);

        return neighbourList;
    }

    private static void buildSpanningTree(ArrayList<ArrayList<Integer>> neighbourList) {
        if (nodeDictionary != null && nodeDictionary.size() != 0) {
            boolean[] visited = new boolean[totalNodes];
            Arrays.fill(visited, false);
            Queue<Integer> queue = new LinkedList<Integer>();
            queue.add(0);
            visited[0] = true;
            while (!queue.isEmpty()) {
                int nodeId = queue.remove();
                Node parent = nodeDictionary.get(nodeId);
                ArrayList<Integer> neighbours = neighbourList.get(nodeId);
                if (neighbours != null && neighbours.size() > 0) {
                    for (Integer neighbourId : neighbours) {
                        if (!visited[neighbourId]) {
                            Node neighbour = nodeDictionary.get(neighbourId);
                            neighbour.setParentNode(parent);
                            visited[neighbourId] = true;
                            queue.add(neighbourId);
                        }
                    }
                }
            }

        }
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

            for (Integer i : neighbours) {
                Node neighbour = nodeDictionary.get(i);
                neighbourNodes.add(neighbour);
            }
            currentNode.setNodeNeighbour(neighbourNodes);

            // set up current node; close socket is pending
            currentNode.setUpNeighbourMap();
            currentNode.initializeNode();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
