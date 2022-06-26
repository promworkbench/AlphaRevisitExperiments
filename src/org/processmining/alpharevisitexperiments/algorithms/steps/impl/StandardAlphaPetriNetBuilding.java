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
import java.util.Map;
import java.util.Set;

public class StandardAlphaPetriNetBuilding extends PetriNetBuildingStep {
    public final static String NAME = "Standard Alpha 1.0 Petri net creation";
    final ExperimentOption[] options = {
    };
    private Map<Pair<Set<String>, Set<String>>, Place> candidatePlaceMap;

    public StandardAlphaPetriNetBuilding() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public AcceptingPetriNet buildPetriNet(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {
        Petrinet net = PetrinetFactory.newPetrinet("Petri net");
        Marking initialMarking = new Marking();
        Marking finalMarking = new Marking();

        candidatePlaceMap = new HashMap<>();

        Place startPlace = net.addPlace("__Start");
        Place endPlace = net.addPlace("__End");
        initialMarking.add(startPlace);
        finalMarking.add(endPlace);

        for (Pair<Set<String>, Set<String>> e : candidates) {
            Place p = net.addPlace(e.toString());
            candidatePlaceMap.put(e, p);
        }
        for (String activity : logProcessor.getActivities()) {
            Transition t = net.addTransition(activity);
            if (logProcessor.getFirstInCaseActivities().contains(activity)) {
                net.addArc(startPlace, t);
            }
            if (logProcessor.getLastInCaseActivities().contains(activity)) {
                net.addArc(t, endPlace);
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
        return AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking, finalMarking);
    }

    @Override
    public Map<Pair<Set<String>, Set<String>>, Place> getCandidatePlaceMap() {
        return candidatePlaceMap;
    }
}
