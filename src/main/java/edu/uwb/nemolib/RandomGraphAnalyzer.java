package edu.uwb.nemolib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * RandomGraphAnalyzer is a facade class that generates and enumerates using
 * RAND-ESU a set of random network graphs based on the degree sequence vector
 * of a specified graph. The output of the analyze() method can be used by a
 * RelativeFrequencyAnalyzer object to determine whether a target graph contains any
 * network motifs.
 */
public final class RandomGraphAnalyzer {

    private SubgraphEnumerator enumerator;
    private int randomGraphCount;

    public RandomGraphAnalyzer(SubgraphEnumerator enumerator,
                               int randomGraphCount) {
        this.enumerator = enumerator;
        this.randomGraphCount = randomGraphCount;

    }

    /**
     * Generate and enumerate a set of random graphs.
     *
     * @param targetGraph  the network graph from which to derive a degree
     *                     sequence vector for generating random graphs
     * @param subgraphSize the getSize of subgraph to enumerate
     * @return mapping of labels to relative frequencies as found in the
     * random graph pool
     */
    public Map<String, List<Double>> analyze(Graph targetGraph, int subgraphSize) {

        // create the return map and fill it with the labels we found in the
        // target graph, as those are the only labels about which we care
        Map<String, List<Double>> labelToRelativeFrequencies = new HashMap<>();

        long startTime = System.currentTimeMillis();
        double duration1 = 0;
        double duration2 = 0;
        double duration3 = 0;
        double duration4 = 0;
        double duration5 = 0;
        double duration6 = 0;
        double duration7 = 0;

        for (int i = 0; i < randomGraphCount; i++) {
            //display status for every 100th graph
            if (i % 100 == 99)
                System.out.println("Analyzing random graph " + (i + 1) + "...");

            startTime = System.currentTimeMillis();
            Graph randomGraph = RandomGraphGenerator.generate(targetGraph);
            duration1 += (System.currentTimeMillis() - startTime) / 1000.0;

            // enumerate random graphs
            startTime = System.currentTimeMillis();
            SubgraphCount subgraphCount = new SubgraphCount();
            duration2 += (System.currentTimeMillis() - startTime) / 1000.0;

            startTime = System.currentTimeMillis();
            enumerator.enumerate(randomGraph, subgraphSize, subgraphCount);
            duration3 += (System.currentTimeMillis() - startTime) / 1000.0;

            startTime = System.currentTimeMillis();
            subgraphCount.label();
            duration4 += (System.currentTimeMillis() - startTime) / 1000.0;

            startTime = System.currentTimeMillis();
            Map<String, Double> curLabelRelFreqMap =
                    subgraphCount.getRelativeFrequencies();
            duration5 += (System.currentTimeMillis() - startTime) / 1000.0;

            startTime = System.currentTimeMillis();
            // populate labelToRelativeFrequencies with result
            for (Map.Entry<String, Double> curLabelRelFreqPair :
                    curLabelRelFreqMap.entrySet()) {
                String curLabel = curLabelRelFreqPair.getKey();
                Double curFreq = curLabelRelFreqPair.getValue();

                if (!labelToRelativeFrequencies.containsKey(curLabel)) {
                    labelToRelativeFrequencies.put(curLabel, new LinkedList<>());
                }
                labelToRelativeFrequencies.get(curLabel).add(curFreq);
            }
            duration6 += (System.currentTimeMillis() - startTime) / 1000.0;
        }

        startTime = System.currentTimeMillis();
        // fill in with zeros any List that is less than subgraph count to
        // ensure non-detection is accounted for.
        for (List<Double> freqs :
                labelToRelativeFrequencies.values()) {
            while (freqs.size() < randomGraphCount) {
                freqs.add(0.0);
            }
        }
        duration7 += (System.currentTimeMillis() - startTime) / 1000.0;

        System.out.println();
        System.out.println("**************************************");
        System.out.println("RandomGraphAnalyzer Statistics");
        System.out.println("duration1: " + duration1);
        System.out.println("duration2: " + duration2);
        System.out.println("duration3: " + duration3);
        System.out.println("duration4: " + duration4);
        System.out.println("duration5: " + duration5);
        System.out.println("duration6: " + duration6);
        System.out.println("duration7: " + duration7);
        System.out.println("**************************************");
        System.out.println();

        return labelToRelativeFrequencies;
    }
}