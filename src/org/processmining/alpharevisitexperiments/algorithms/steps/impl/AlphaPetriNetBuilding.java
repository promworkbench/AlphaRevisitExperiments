package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.alpharevisitexperiments.algorithms.steps.PetriNetBuildingStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.alpharevisitexperiments.util.Utils;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.processmining.alpharevisitexperiments.util.LogProcessor.END_ACTIVITY;
import static org.processmining.alpharevisitexperiments.util.LogProcessor.START_ACTIVITY;

public class AlphaPetriNetBuilding extends PetriNetBuildingStep {
    public final static String NAME = "Alpha 1.1+ Petri net creation";
    final ExperimentOption[] options = {
    };
    private Map<Pair<Set<String>, Set<String>>, Place> candidatePlaceMap;

    public AlphaPetriNetBuilding() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public AcceptingPetriNet buildPetriNet(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {
        Petrinet net = PetrinetFactory.newPetrinet("Petri net");
        Marking initialMarking = new Marking();
        Marking finalMarking = new Marking();

        candidatePlaceMap = new HashMap<>();

        for (Pair<Set<String>, Set<String>> e : candidates) {
            Place p = net.addPlace(e.toString());
            candidatePlaceMap.put(e, p);
            if (e.getFirst().contains(START_ACTIVITY)) {
                initialMarking.add(p, 1);
            } else if (e.getSecond().contains(END_ACTIVITY)) {
                finalMarking.add(p, 1);
            }
        }
        for (String activity : logProcessor.getActivities()) {
            Transition t = net.addTransition(activity);
            if (activity.startsWith("skip_")) {
                t.setInvisible(true);
            }
        }

        for (Pair<Set<String>, Set<String>> e : candidates) {
            for (String a : e.getFirst()) {
                if (logProcessor.getActivities().contains(a)) {
                    Transition from = Utils.getTransitionWithLabel(net, a);
                    Place to = Utils.getPlaceWithLabel(net, e.toString());
                    net.addArc(from, to);
                }
            }
            for (String a : e.getSecond()) {
                if (logProcessor.getActivities().contains(a)) {
                    Transition to = Utils.getTransitionWithLabel(net, a);
                    Place from = Utils.getPlaceWithLabel(net, e.toString());
                    net.addArc(from, to);
                }
            }
        }
        Set<Transition> transitionsToRemove = new HashSet<>();
        for (Transition t : net.getTransitions()) {
            if (t.isInvisible() && net.getInEdges(t).size() == 0 && net.getOutEdges(t).size() == 0) {
                transitionsToRemove.add(t);
            }
        }
        for (Transition t : transitionsToRemove) {
            net.removeTransition(t);
        }
        System.out.println("Done building Petri net");
        return AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking, finalMarking);
    }

    @Override
    public Map<Pair<Set<String>, Set<String>>, Place> getCandidatePlaceMap() {
        return candidatePlaceMap;
    }
}
