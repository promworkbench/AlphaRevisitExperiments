package org.processmining.alpharevisitexperiments.algorithms;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.framework.util.Pair;

public class AlphaWithReplay extends AlgorithmExperiment {
    final static ExperimentOption[] options = {
            new ExperimentOption<>(Integer.class,"replay_frequent_variants","% of frequent variants to perfectly replay",0,0,100),
            new ExperimentOption<>(Boolean.class,"try_connect","Try to connect disconnected transitions",false),
    };

    public AlphaWithReplay() {
        super("Alpha with replay",options);
    }

    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        Object[] objs = AlphaMinerPlugin.applyAlphaClassic(context, log, new XEventNameClassifier());
        Petrinet net = (Petrinet) objs[0];
        Marking initialMarking = (Marking) objs[1];
        Marking finalMarking = new Marking();
        for(Place p : net.getPlaces()){
            if(p.getLabel().equals("End")){
                finalMarking.add(p,1);
            }
        }
        LogProcessor logProcessor = new LogProcessor(log);

        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        HashMap<String, Integer> variants = logProcessor.getVariants();
        System.out.println(dfg);
        System.out.println(variants);
        String[] frequentVariants = getTopVariants(variants, log.size(),this.getOptionValueByID("replay_frequent_variants"));

        replayAndRemovePlaces(net, frequentVariants);

        return AcceptingPetriNetFactory.createAcceptingPetriNet(net,initialMarking,finalMarking);
    }



    private static String[] getTopVariants(HashMap<String, Integer> allVariantsWithCaseNumbers, int totalNumberOfCases,
                                           int goalMinCasePercentage) {
        String[] variantArray = allVariantsWithCaseNumbers.keySet().toArray(new String[0]);
        Arrays.sort(variantArray, Comparator.comparingInt(e -> -allVariantsWithCaseNumbers.getOrDefault(e, 0)));
        int numberOfCoveredCases = allVariantsWithCaseNumbers.getOrDefault(variantArray[0], 0);
        int lastVariantIndex = 0;
        while (numberOfCoveredCases * 100.0 / totalNumberOfCases < goalMinCasePercentage
                && (lastVariantIndex + 1) < allVariantsWithCaseNumbers.size()) {
            lastVariantIndex += 1;
            numberOfCoveredCases += allVariantsWithCaseNumbers.getOrDefault(variantArray[lastVariantIndex], 0);
        }
        System.out.println("!! Including the " + (lastVariantIndex + 1) + " most frequent variants covers around "
                + (numberOfCoveredCases * 100.0 / totalNumberOfCases) + "% of all cases.");
        return Arrays.copyOfRange(variantArray, 0, lastVariantIndex + 1);
    }

    private static void replayAndRemovePlaces(Petrinet net, String[] frequentVariants) {
        HashMap<String, Transition> transitionMap = new HashMap<>();
        for (Transition t : net.getTransitions()) {
            transitionMap.put(t.getLabel().replaceAll(",", "_"), t);
        }

        for (String variant : frequentVariants) {
            System.out.println("- Replaying variant: " + variant);
            HashMap<Place, Integer> tokenCount = new HashMap<>();
            String[] trace = variant.split(",");
            for (Place p : net.getPlaces()) {

                if (p.getLabel().equals("Start")) {
                    tokenCount.put(p, 1);
                } else {
                    tokenCount.put(p, 0);
                }
            }

            for (String activity : trace) {
                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net
                        .getInEdges(transitionMap.get(activity))) {
                    PetrinetNode incomingNode = e.getSource();
                    if (incomingNode instanceof Place) {
                        Place p = (Place) incomingNode;
                        int count = tokenCount.get(p);
                        if (count > 0) {
                            count -= 1;
                            tokenCount.put(p, count);
                        } else {
                            System.out.println("No token in place" + p.getLabel() + " -> remove place");
                            net.removePlace(p);
                        }
                    } else {
                        System.err.println("Error: Incoming PN node for transition is not a place?!");
                    }
                }

                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net
                        .getOutEdges(transitionMap.get(activity))) {
                    PetrinetNode outgoingNode = e.getTarget();
                    if (outgoingNode instanceof Place) {
                        Place p = (Place) outgoingNode;
                        int count = tokenCount.get(p);
                        count += 1;
                        tokenCount.put(p, count);
                    } else {
                        System.err.println("Error: Outgoing PN node for transition is not a place?!");
                    }
                }
            }
            Set<Place> placesToRemove = new HashSet<>();
            for (Place p : net.getPlaces()) {

                if (p.getLabel().equals("End")) {
                    if (tokenCount.get(p) != 1) {
                        System.out.println("No token in End place");
                    }
                } else {
                    if (tokenCount.get(p) != 0) {
                        System.out.println("Token left in place" + p.getLabel() + " -> remove place");
                        placesToRemove.add(p);
                    }
                }
            }
            for (Place p : placesToRemove) {
                net.removePlace(p);
            }
        }
    }

}
