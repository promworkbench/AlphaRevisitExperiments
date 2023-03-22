package org.processmining.alpharevisitexperiments.util;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.*;

public class ReplayProcessor {

    public final static String FREQUENT_VARIANT_OPTION_ID = "replay_local_fitness_required";
    public final static ExperimentOption[] STANDARD_REPLAY_OPTIONS = {
            new ExperimentOption<>(Double.class, FREQUENT_VARIANT_OPTION_ID, "replay_local_fitness_required", 0.0, 0.0, 1.0),
    };

    public static String[] getTopVariants(HashMap<String, Integer> allVariantsWithCaseNumbers, int totalNumberOfCases,
                                          double goalMinCasePercentage) {
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

    public static Map<Place, Double> replayWithAllTraces(AcceptingPetriNet net, Map<String, Integer> allTracesWithFrequency) {
        HashMap<String, Transition> transitionMap = new HashMap<>();
        for (Transition t : net.getNet().getTransitions()) {
            transitionMap.put(t.getLabel().replaceAll(",", "_"), t);
        }

//        Set<Place> placesToRemove = new HashSet<>();
        Map<Place, Integer> numberOfFittingTracesPerPlace = new HashMap<>();
        Map<Place, Integer> numberOfRelevantTracesPerPlace = new HashMap<>();

        for (String variant : allTracesWithFrequency.keySet()) {
            Set<Place> violatingPlaces = new HashSet<>();
            Set<Place> relevantPlaces = new HashSet<>();
            HashMap<Place, Integer> tokenCount = new HashMap<>();
            String[] trace = variant.split(",");
// Place initial tokens
            for (Place p : net.getNet().getPlaces()) {
                if (net.getInitialMarking().contains(p)) {
                    tokenCount.put(p, 1);
                } else {
                    tokenCount.put(p, 0);
                }
            }


// Replay trace
            for (String activity : trace) {
// Check if incoming places have sufficient token
// Get every place with an arc to the corresponding (fired) transition
                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getNet()
                        .getInEdges(transitionMap.get(activity))) {
                    PetrinetNode incomingNode = e.getSource();
                    if (incomingNode instanceof Place) {
                        Place p = (Place) incomingNode;
                        relevantPlaces.add(p);
// Decrease the token count in that place by 1
                        int count = tokenCount.get(p);
                        if (count > 0) {
                            count -= 1;
                            tokenCount.put(p, count);
                        } else {
// If there is no token in the place, it should be removed
//                            System.out.println("No token in place" + p.getLabel() + " -> remove place");
                            violatingPlaces.add(p);
                        }
                    } else {
                        System.err.println("Error: Incoming PN node for transition is not a place?!");
                    }
                }
// Place tokens in outgoing places
// Get every place with an arc incoming from the fired transition
                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getNet()
                        .getOutEdges(transitionMap.get(activity))) {
                    PetrinetNode outgoingNode = e.getTarget();
                    if (outgoingNode instanceof Place) {
                        Place p = (Place) outgoingNode;
                        relevantPlaces.add(p);
// Increase the token count in that place by 1
                        int count = tokenCount.get(p);
                        count += 1;
                        tokenCount.put(p, count);
                    } else {
                        System.err.println("Error: Outgoing PN node for transition is not a place?!");
                    }
                }
            }

// Check final marking: Only places that are in the (selected?) final marking should have 1 token remaining, all others 0
//            Map<Marking,Integer> violationsPerFinalMarking = new HashMap<>();
            Map<Marking, Set<Place>> violatingPlacesPerFinalMarking = new HashMap<>();

            for (Place p : net.getNet().getPlaces()) {
// Keep a list of violating places per final marking (as there are multiple), then choose the best option
//                i.e., the one with the smallest number of violating places
                for (Marking m : net.getFinalMarkings()) {
                    if (m.contains(p)) {
                        if (tokenCount.get(p) != 1) {
                            // Violation!
                            Set<Place> violatingPlacesTmp = violatingPlacesPerFinalMarking.getOrDefault(m, new HashSet<>());
                            violatingPlacesTmp.add(p);
                            violatingPlacesPerFinalMarking.put(m, violatingPlacesTmp);
                        }
                    } else {
                        if (tokenCount.get(p) != 0) {
                            // Violation!
                            Set<Place> violatingPlacesTmp = violatingPlacesPerFinalMarking.getOrDefault(m, new HashSet<>());
                            violatingPlacesTmp.add(p);
                            violatingPlacesPerFinalMarking.put(m, violatingPlacesTmp);
                        }
                    }
                }
            }
            Marking bestFinalMarking = null;
            for (Marking m : net.getFinalMarkings()) {
                if (bestFinalMarking == null || (violatingPlacesPerFinalMarking.get(m).size() < violatingPlacesPerFinalMarking.get(bestFinalMarking).size())) {
                    // New best final marking found;
                    bestFinalMarking = m;
                }
            }
// Now add all violating places for selected best final marking to the violatingPlaces set
            violatingPlaces.addAll(violatingPlacesPerFinalMarking.getOrDefault(bestFinalMarking, new HashSet<>()));

//            Count Number of Relevant places
            for (Place p : relevantPlaces) {
                numberOfRelevantTracesPerPlace.put(p, numberOfRelevantTracesPerPlace.getOrDefault(p, 0) + allTracesWithFrequency.get(variant));
// First add all relevant traces as fitting, remove the violating ones later
                numberOfFittingTracesPerPlace.put(p, numberOfFittingTracesPerPlace.getOrDefault(p, 0) + allTracesWithFrequency.get(variant));
            }
//            Remove count for violating places
            for (Place p : violatingPlaces) {
                numberOfFittingTracesPerPlace.put(p, numberOfFittingTracesPerPlace.getOrDefault(p, 0) - allTracesWithFrequency.get(variant));
            }
        }
        Map<Place, Double> ret = new HashMap<>();
        for (Place p : net.getNet().getPlaces()) {
            double numberOfRelevantTraces = numberOfRelevantTracesPerPlace.getOrDefault(p, 0);
            if (numberOfRelevantTraces > 0) {
                double numberOfFittingTraces = numberOfFittingTracesPerPlace.getOrDefault(p, 0);
                ret.put(p, numberOfFittingTraces / numberOfRelevantTraces);
            } else {
                ret.put(p, 0.0);
            }
        }
        return ret;
    }

    public static void replayAndRemovePlaces(AcceptingPetriNet net, String[] frequentVariants, boolean doNotRemoveStartEndPlaces) {
        HashMap<String, Transition> transitionMap = new HashMap<>();
        for (Transition t : net.getNet().getTransitions()) {
            transitionMap.put(t.getLabel().replaceAll(",", "_"), t);
        }

        Set<Place> placesToRemove = new HashSet<>();
        for (String variant : frequentVariants) {
            System.out.println("- Replaying variant: " + variant);

            HashMap<Place, Integer> tokenCount = new HashMap<>();
            String[] trace = variant.split(",");
// Place initial tokens
            for (Place p : net.getNet().getPlaces()) {
                if (net.getInitialMarking().contains(p)) {
                    tokenCount.put(p, 1);
                } else {
                    tokenCount.put(p, 0);
                }
            }


// Replay trace
            for (String activity : trace) {
// Check if incoming places have sufficient token
// Get every place with an arc to the corresponding (fired) transition
                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getNet()
                        .getInEdges(transitionMap.get(activity))) {
                    PetrinetNode incomingNode = e.getSource();
                    if (incomingNode instanceof Place) {
                        Place p = (Place) incomingNode;
// Decrease the token count in that place by 1
                        int count = tokenCount.get(p);
                        if (count > 0) {
                            count -= 1;
                            tokenCount.put(p, count);
                        } else {
// If there is no token in the place, it should be removed
                            System.out.println("No token in place" + p.getLabel() + " -> remove place");
                            placesToRemove.add(p);
                        }
                    } else {
                        System.err.println("Error: Incoming PN node for transition is not a place?!");
                    }
                }
// Place tokens in outgoing places
// Get every place with an arc incoming from the fired transition
                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getNet()
                        .getOutEdges(transitionMap.get(activity))) {
                    PetrinetNode outgoingNode = e.getTarget();
                    if (outgoingNode instanceof Place) {
                        Place p = (Place) outgoingNode;
// Increase the token count in that place by 1
                        int count = tokenCount.get(p);
                        count += 1;
                        tokenCount.put(p, count);
                    } else {
                        System.err.println("Error: Outgoing PN node for transition is not a place?!");
                    }
                }
            }

// Check final marking: Only places that are in the (selected?) final marking should have 1 token remaining, all others 0
//            Map<Marking,Integer> violationsPerFinalMarking = new HashMap<>();
            Map<Marking, Set<Place>> violatingPlacesPerFinalMarking = new HashMap<>();

            for (Place p : net.getNet().getPlaces()) {
// Keep a list of violating places per final marking (as there are multiple), then choose the best option
//                i.e., the one with the smallest number of violating places
                for (Marking m : net.getFinalMarkings()) {
                    if (m.contains(p)) {
                        if (tokenCount.get(p) != 1) {
                            // Violation!
                            Set<Place> violatingPlaces = violatingPlacesPerFinalMarking.getOrDefault(m, new HashSet<>());
                            violatingPlaces.add(p);
                            violatingPlacesPerFinalMarking.put(m, violatingPlaces);
                        }
                    } else {
                        if (tokenCount.get(p) != 0) {
                            // Violation!
                            Set<Place> violatingPlaces = violatingPlacesPerFinalMarking.getOrDefault(m, new HashSet<>());
                            violatingPlaces.add(p);
                            violatingPlacesPerFinalMarking.put(m, violatingPlaces);
                        }
                    }
                }
            }
            Marking bestFinalMarking = null;
            for (Marking m : net.getFinalMarkings()) {
                if (bestFinalMarking == null || (violatingPlacesPerFinalMarking.get(m).size() < violatingPlacesPerFinalMarking.get(bestFinalMarking).size())) {
                    // New best final marking found;
                    bestFinalMarking = m;
                }
            }
// Now add all violating places for selected best final marking to the placesToRemove set
            placesToRemove.addAll(violatingPlacesPerFinalMarking.getOrDefault(bestFinalMarking, new HashSet<>()));

        }
        // Actually remove places that had missing/remaining tokens
// Respecting the doNotRemoveStartEndPlaces option
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
