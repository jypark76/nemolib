package edu.uwb.nemolib_examples.network_motif_detector;

import edu.uwb.nemolib.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NetworkMotifDetector {

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        long startTally = startTime;

        if (args.length < 3) {
            System.err.println("usage: NetworkMotifDetector path_to_data " +
                    "motif_size, random_graph_count");
            System.exit(1);
        }

        String filename = args[0];
        System.out.println("filename = " + args[0]);
        int motifSize = Integer.parseInt(args[1]);
        int randGraphCount = Integer.parseInt(args[2]);

        if (motifSize < 3) {
            System.err.println("Motif getSize must be 3 or larger");
            System.exit(-1);
        }

        // parse input graph
        System.out.println("Parsing target graph...");
        Graph targetGraph = null;
        try {
            targetGraph = GraphParser.parse(filename);
        } catch (IOException e) {
            System.err.println("Could not process " + filename);
            System.err.println(e);
            System.exit(-1);
        }

        System.out.println("File Input Time \t\t\t" + ((System.currentTimeMillis() - startTally) / 1000.0));
        System.out.println("time from the beginning: \t" + ((System.currentTimeMillis() - startTime) / 1000.0) + "\n\n");
        startTally = System.currentTimeMillis();

        // Hard-code probs for now. This vector will take about ~10% sample
        List<Double> probs = new LinkedList<>();
        for (int i = 0; i < motifSize - 2; i++) {
            probs.add(1.0);
        }
        probs.add(0.5);
        probs.add(0.5);

        System.out.println("Probing time \t\t\t\t" + ((System.currentTimeMillis() - startTally) / 1000.0));
        System.out.println("time from the beginning: \t" + ((System.currentTimeMillis() - startTime) / 1000.0) + "\n\n");
        startTally = System.currentTimeMillis();

        SubgraphEnumerationResult subgraphCount = new SubgraphCount();
        SubgraphEnumerator targetGraphESU = new ESU();

        System.out.println("Emumeration time \t\t\t" + ((System.currentTimeMillis() - startTally) / 1000.0));
        System.out.println("time from the beginning: \t" + ((System.currentTimeMillis() - startTime) / 1000.0) + "\n\n");
        startTally = System.currentTimeMillis();

        TargetGraphAnalyzer targetGraphAnalyzer =
                new TargetGraphAnalyzer(targetGraphESU, subgraphCount);
        Map<String, Double> targetLabelToRelativeFrequency =
                targetGraphAnalyzer.analyze(targetGraph, motifSize);

        System.out.println("(map emerator init) time \t" + ((System.currentTimeMillis() - startTally) / 1000.0));
        System.out.println("time from the beginning: \t" + ((System.currentTimeMillis() - startTime) / 1000.0) + "\n\n");
        startTally = System.currentTimeMillis();

        System.out.println("---------------------------------------------------------" + "\n\n\n");

        System.out.println(targetGraph);
        System.out.println("randGraphCount: " + randGraphCount + "\n\n");
        System.out.println("motifSize: " + motifSize + "\n\n");
        System.out.print("probs: ");
        for (double i : probs) {
            System.out.print(i + " ");
        }
        System.out.print("\n\n");


        SubgraphEnumerator randESU = new RandESU(probs);

        RandomGraphAnalyzer randomGraphAnalyzer =
                new RandomGraphAnalyzer(randESU, randGraphCount);

        System.out.println("Random Graph analyzer time \t" + ((System.currentTimeMillis() - startTally) / 1000.0));
        System.out.println("time from the beginning: \t" + ((System.currentTimeMillis() - startTime) / 1000.0) + "\n\n");
        startTally = System.currentTimeMillis();


        // this part consume the most time
        Map<String, List<Double>> randomLabelToRelativeFrequencies =
                randomGraphAnalyzer.analyze(targetGraph, motifSize);

        System.out.println("---------------------------------------------------------" + "\n\n\n");

        System.out.println("random Label Relative time \t" + ((System.currentTimeMillis() - startTally) / 1000.0));
        System.out.println("time from the beginning: \t" + ((System.currentTimeMillis() - startTime) / 1000.0) + "\n\n");
        startTally = System.currentTimeMillis();

        RelativeFrequencyAnalyzer relativeFrequencyAnalyzer =
                new RelativeFrequencyAnalyzer(randomLabelToRelativeFrequencies,
                        targetLabelToRelativeFrequency);

        System.out.println("relative frequence time \t" + ((System.currentTimeMillis() - startTally) / 1000.0));
        System.out.println("time from the beginning: \t" + ((System.currentTimeMillis() - startTime) / 1000.0) + "\n\n");
        startTally = System.currentTimeMillis();

        System.out.println(relativeFrequencyAnalyzer);
        System.out.println("Compete");

        System.out.println("Result output time \t\t\t" + ((System.currentTimeMillis() - startTally) / 1000.0));
        System.out.println("time from the beginning: \t" + ((System.currentTimeMillis() - startTime) / 1000.0) + "\n\n");
    }
}

