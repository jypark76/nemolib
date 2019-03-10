package edu.uwb.nemolib;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * RandESU is a static class used for executing the RandESU on a portion of a
 * network graph.
 */
public class RandESU implements SubgraphEnumerator {
    List<Double> probs;

    // prevent instantiation via default constructor
    private RandESU() {
        throw new AssertionError();
    }

    public RandESU(List<Double> probs) {
        this.probs = probs;
    }

    /**
     * Enumerates all subgraphSize Subgraphs in the input Graph using the
     * RAND-ESU algorithm.
     *
     * @param graph        the graph on which to execute RAND-ESU
     * @param subgraphSize the getSize of the target Subgraphs
     */
    public void enumerate(Graph graph,
                          int subgraphSize,
                          SubgraphEnumerationResult subgraphs) {

        long startTime = System.nanoTime();

        // maintain list of nodes selected so far
        List<Integer> selectedVertices = new ArrayList<>();

        if (probs.get(0) == 1.0) // select all nodes
        {
            for (int i = 0; i < graph.getSize(); ++i) {
                selectedVertices.add(i);
            }
        } else { // determine how many nodes should be sampled initially
            int numVerticesToSelect =
                    (int) (Math.round(probs.get(0) * graph.getSize()));

            Random rand = new Random();
            // populate list with appropriate number of nodes
            for (int numVerticesSelected = 0; numVerticesSelected <
                    numVerticesToSelect; ++numVerticesSelected) {

                int nodeSelected = rand.nextInt(graph.getSize());
                // ensure no duplicates
                while (selectedVertices.contains(nodeSelected)) {
                    nodeSelected = rand.nextInt(graph.getSize());
                }
                selectedVertices.add(nodeSelected);
            }
        }

        long endTime = System.nanoTime();

        long duration1 = endTime - startTime;

        startTime = System.nanoTime();
        int i = 0;
        for (int vertex : selectedVertices) {
            ++i;
            enumerate(graph, subgraphs, subgraphSize, probs, vertex);
        }
        endTime = System.nanoTime();

        long duration2 = endTime - startTime;

        System.out.println("***** RANDESU (enumerate 1) *****");
        System.out.println("\t" + "Graph Size: " + graph.getSize());
        System.out.println("\t" + "First prob: " + probs.get(0));
        System.out.println();
        System.out.println("\t" + "duration1: \t\t" + duration1);
        System.out.println("\t" + "duration2: \t\t" + duration2);
        System.out.println("\t" + "Loop count: \t\t" + i);


        System.exit(1);
    }

    /**
     * Enumerates all subgraphSize Subgraphs for the specified vertex's branch
     * of an ESU execution tree using the RAND-ESU algorithm. Allows for more
     * control over execution order compared to the enumerate method that does
     * not include a vertex parameter.
     *
     * @param graph        the graph on which to execute RAND-ESU
     * @param subgraphs
     * @param subgraphSize
     * @param probs
     * @param vertex
     */
    public static void enumerate(Graph graph,
                                 SubgraphEnumerationResult subgraphs,
                                 int subgraphSize, List<Double> probs, int vertex) {

        long startTime = 0;
        long endTime = 0;
        long begin = System.nanoTime();
        long end = 0;
        long duration1 = 0;
        long duration2 = 0;
        long duration3 = 0;
        long duration4 = 0;
        long duration5 = 0;
        long duration6 = 0;

        startTime = System.nanoTime();
        Subgraph subgraph = new Subgraph(subgraphSize);
        endTime = System.nanoTime();
        duration1 = endTime - startTime;

        startTime = System.nanoTime();
        AdjacencyList adjacencyList = new AdjacencyList();
        endTime = System.nanoTime();
        duration2 = endTime - startTime;

        startTime = System.nanoTime();
        CompactHashSet.Iter iter =
                graph.getAdjacencyList(vertex).iterator();
        endTime = System.nanoTime();
        duration3 = endTime - startTime;

        startTime = System.nanoTime();
        while (iter.hasNext()) {
            int next = iter.next();
            if (next > vertex) {
                adjacencyList.add(next);
            }
        }
        endTime = System.nanoTime();
        duration4 = endTime - startTime;

        startTime = System.nanoTime();
        subgraph.add(vertex, graph.getAdjacencyList(vertex));
        endTime = System.nanoTime();
        duration5 = endTime - startTime;

        startTime = System.nanoTime();
        // randomly decide whether to extend
        if (shouldExtend(probs.get(1))) {
            extend(graph, subgraph, adjacencyList, probs, subgraphs);
        }
        endTime = System.nanoTime();
        duration6 = endTime - startTime;
        end = System.nanoTime();

        System.out.println("Enumeration part!!!");
        System.out.println("\t\t" + "duration1: \t\t" + duration1);
        System.out.println("\t\t" + "duration2: \t\t" + duration2);
        System.out.println("\t\t" + "duration3: \t\t" + duration3);
        System.out.println("\t\t" + "duration4: \t\t" + duration4);
        System.out.println("\t\t" + "duration5: \t\t" + duration5);
        System.out.println("\t\t" + "duration6: \t\t" + duration6);
        System.out.println("\t\t" + "total: \t\t\t" + (end-begin));
//        System.exit(1);
    }

    // extend the subgraphs recursively
    private static void extend(Graph graph,
                               Subgraph subgraph,
                               AdjacencyList extension,
                               List<Double> probs,
                               SubgraphEnumerationResult subgraphs) {

        int v = subgraph.root();
        CompactHashSet.Iter wIter = extension.iterator();

        // optimize by not creating next extension if subgraph is
        // 1 node away from completion
        if (subgraph.size() == subgraph.order() - 1) {
            while (wIter.hasNext()) {
                int w = wIter.next();
                // check the last value in prob list
                if (shouldExtend(probs.get(probs.size() - 1))) {
                    // construct a union of w and the existing subgraph
                    Subgraph subgraphUnion = subgraph.copy();
                    subgraphUnion.add(w, graph.getAdjacencyList(w));
                    synchronized (subgraphs) {
                        subgraphs.addSubgraph(subgraphUnion);
                    }
                }
            }
        }

        // otherwise create the extention
        while (wIter.hasNext()) {
            int w = wIter.next();
            wIter.remove();

            // next extension contains at least the current extension
            AdjacencyList nextExtension = extension.copy();

            // examine each node 'u' from the set of nodes adjacent to 'w'
            // and addSubgraph it to the next extension if it is exclusive
            CompactHashSet.Iter uIter = graph.getAdjacencyList(w).iterator();
            while (uIter.hasNext()) {
                int u = uIter.next();
                if (u > v) {
                    if (isExclusive(graph, u, subgraph)) {
                        nextExtension.add(u);
                    }
                }
            }

            // construct a union of w and the existing subgraph
            Subgraph subgraphUnion = subgraph.copy();
            subgraphUnion.add(w, graph.getAdjacencyList(w));

            // randomly choose whether or not to extend to the next level
            // based on the probability vector provided.
            if (shouldExtend(probs.get(subgraphUnion.size() - 1))) {
                extend(graph, subgraphUnion, nextExtension, probs, subgraphs);
            }
        }
    }

    // determines whether or not to extend based on a given probability, given
    // as an integer.
    // precondition: 0.0 <= prob <= 1.0
    private static boolean shouldExtend(double prob) throws
            IllegalArgumentException {

        if (prob == 1.0) {
            return true;
        }

        if (prob == 0.0) {
            return false;
        }

        if (prob > 1.0 || prob < 0.0) {
            throw new IllegalArgumentException("RAND-ESU probability outside" +
                    " acceptable range (0.0 to 1.0)");
        }

        Random rand = new Random();
        int randomNum = rand.nextInt(100) + 1;
        return randomNum <= prob * 100.0;
    }

    // returns true if the node index is exclusive to the given subgraph
    // (that is, is not already in the subgraph, and is not adjacent to any of
    //  the nodes in the subgraph)
    private static boolean isExclusive(Graph graph,
                                       int node,
                                       Subgraph subgraph) {
        for (int i = 0; i < subgraph.size(); i++) {
            int subgraphNode = subgraph.get(i);
            if (subgraphNode == node) {
                return false;
            }
        }
        for (int i = 0; i < subgraph.size(); i++) {
            int subgraphNode = subgraph.get(i);
            if (graph.getAdjacencyList(subgraphNode).contains(node)) {
                return false;
            }
        }
        return true;
    }
}
