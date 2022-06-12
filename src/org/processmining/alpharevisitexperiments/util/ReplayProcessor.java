package org.processmining.alpharevisitexperiments.util;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.*;

public class ReplayProcessor {

    public final static String FREQUENT_VARIANT_OPTION_ID = "replay_frequent_variants";
    public final static String DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID = "do_not_remove_startend_places";
    public final static ExperimentOption[] STANDARD_REPLAY_OPTIONS = {
            new ExperimentOption<>(Integer.class, FREQUENT_VARIANT_OPTION_ID, "top% of frequent variants to use for replay and place removal", 0, 0, 100),
            new ExperimentOption<>(Boolean.class, DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID, "Do not remove places that are in the start or end marking.", false),
//            new ExperimentOption<>(Boolean.class,"try_connect","Try to connect disconnected transitions",false),
    };

    public static String[] getTopVariants(HashMap<String, Integer> allVariantsWithCaseNumbers, int totalNumberOfCases,
                                          int goalMinCasePercentage) {
        String[] variantArray = allVariantsWithCaseNumbers.keySet().toArray(new String[0]);
        Arrays.sort(variantArray, Comparator.comparingInt(e -> -allVariantsWithCaseNumbers.getOrDefault(e, 0)));
        int numberOfCoveredCases = 0;
        int lastVariantIndex = -1;
        while (numberOfCoveredCases * 100.0 / totalNumberOfCases < goalMinCasePercentage
                && (lastVariantIndex + 1) < allVariantsWithCaseNumbers.size()) {
            lastVariantIndex += 1;
            numberOfCoveredCases += allVariantsWithCaseNumbers.getOrDefault(variantArray[lastVariantIndex], 0);
        }
        System.out.println("!! Including the " + (lastVariantIndex + 1) + " most frequent variants covers around "
                + (numberOfCoveredCases * 100.0 / totalNumberOfCases) + "% of all cases.");
        return Arrays.copyOfRange(variantArray, 0, lastVariantIndex + 1);
    }

    public static void replayAndRemovePlaces(AcceptingPetriNet net, String[] frequentVariants) {
        replayAndRemovePlaces(net, frequentVariants, false);
    }

    public static void replayAndRemovePlaces(AcceptingPetriNet net, String[] frequentVariants, boolean doNotRemoveStartEndPlaces) {
        HashMap<String, Transition> transitionMap = new HashMap<>();
        for (Transition t : net.getNet().getTransitions()) {
            transitionMap.put(t.getLabel().replaceAll(",", "_"), t);
        }

        for (String variant : frequentVariants) {
            System.out.println("- Replaying variant: " + variant);
            HashMap<Place, Integer> tokenCount = new HashMap<>();
            String[] trace = variant.split(",");
            for (Place p : net.getNet().getPlaces()) {
                if (net.getInitialMarking().contains(p)) {
                    tokenCount.put(p, 1);
                } else {
                    tokenCount.put(p, 0);
                }
            }

            Set<Place> placesToRemove = new HashSet<>();
            for (String activity : trace) {
                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getNet()
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
                            placesToRemove.add(p);
                        }
                    } else {
                        System.err.println("Error: Incoming PN node for transition is not a place?!");
                    }
                }

                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getNet()
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
            for (Place p : net.getNet().getPlaces()) {
                if (net.getFinalMarkings().contains(p)) {
                    if (tokenCount.get(p) != 1) {
                        System.out.println("No token in End place: " + p.getLabel());
                        placesToRemove.add(p);
                    }
                } else {
                    if (tokenCount.get(p) != 0) {
                        System.out.println("Token left in place " + p.getLabel() + " -> remove place");
                        placesToRemove.add(p);
                    }
                }
            }

            if (doNotRemoveStartEndPlaces) {
                for (Marking m : net.getFinalMarkings()) {
                    for (Place p : m) {
                        placesToRemove.remove(p);
                    }
                }
                for (Place p : net.getInitialMarking()) {
                    placesToRemove.remove(p);
                }
            }

            for (Place p : placesToRemove) {
                for (Marking m : net.getFinalMarkings()) {
                    m.remove(p);
                }
                net.getInitialMarking().remove(p);
                net.getNet().removePlace(p);
            }
        }
    }
}
