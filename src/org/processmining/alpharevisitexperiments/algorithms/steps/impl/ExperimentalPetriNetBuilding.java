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
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.*;

import static org.processmining.alpharevisitexperiments.util.LogProcessor.END_ACTIVITY;
import static org.processmining.alpharevisitexperiments.util.LogProcessor.START_ACTIVITY;

public class ExperimentalPetriNetBuilding extends PetriNetBuildingStep {
    public final static String NAME = "Alpha 1.1+ EXPERIMENTAL Petri net creation";
    final ExperimentOption[] options = {
            new ExperimentOption<>(Boolean.class, "tau_candidates_skip", "Enable tau (single) candidate skip.", true),
            new ExperimentOption<>(Boolean.class, "loop_detection", "Enable loop detection.", true),
            new ExperimentOption<>(Boolean.class, "skip_detection", "Enable (larger) skip detection.", true),

    };
    private Map<Pair<Set<String>, Set<String>>, Place> candidatePlaceMap;

    public ExperimentalPetriNetBuilding() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public AcceptingPetriNet buildPetriNet(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {
        Petrinet net = PetrinetFactory.newPetrinet("Petri net");
        Marking initialMarking = new Marking();
        Marking finalMarking = new Marking();

        candidatePlaceMap = new HashMap<>();

        Set<Set<String>> activitiesWithLoopTau = new HashSet<>();

        Set<Pair<Pair<Set<String>, Set<String>>, Pair<Set<String>, Set<String>>>> skipCandidates = new HashSet<>();

        HashMap<String, Set<Pair<Set<String>, Set<String>>>> candidatesWithTau = new HashMap<>();

        for (Pair<Set<String>, Set<String>> e : candidates) {
            if (getOptionValueByID("tau_candidates_skip")) {
                e.getFirst().stream().filter(p -> p.indexOf("TAU(") == 0).forEach(p -> {
                    Set<Pair<Set<String>, Set<String>>> candidatesForFoundTau = candidatesWithTau.getOrDefault(p, new HashSet<>());
                    candidatesForFoundTau.add(e);
                    candidatesWithTau.put(p, candidatesForFoundTau);
                });
                e.getSecond().stream().filter(p -> p.indexOf("TAU(") == 0).forEach(p -> {
                    Set<Pair<Set<String>, Set<String>>> candidatesForFoundTau = candidatesWithTau.getOrDefault(p, new HashSet<>());
                    candidatesForFoundTau.add(e);
                    candidatesWithTau.put(p, candidatesForFoundTau);
                });
            }
            Place p = net.addPlace(e.toString());
            candidatePlaceMap.put(e, p);
            if (e.getFirst().contains(START_ACTIVITY)) {
                initialMarking.add(p, 1);
            } else if (e.getSecond().contains(END_ACTIVITY)) {
                finalMarking.add(p, 1);
            }
        }
        for (String activity : logProcessor.getActivities()) {
            net.addTransition(activity);
        }

        for (Pair<Set<String>, Set<String>> e : candidates) {
            if (getOptionValueByID("skip_detection")) {
                if (e.getFirst().size() > 1) {
                    candidates.stream().filter(p -> {
                        if (p.getSecond().size() <= 1 || p.equals(e)) {
                            return false;
                        }
                        boolean firstOverlap = p.getFirst().stream().anyMatch(fi -> e.getFirst().contains(fi));
                        boolean secondOverlap = p.getSecond().stream().anyMatch(fi -> e.getSecond().contains(fi));
                        return firstOverlap && secondOverlap;
                    }).forEach(p -> {
                        Set<String> firstIntersection = new HashSet<>();
                        for (String a : p.getFirst()) {
                            if (e.getFirst().contains(a)) {
                                firstIntersection.add(a);
                            }
                        }
                        Set<String> secondIntersection = new HashSet<>();
                        for (String a : p.getSecond()) {
                            if (e.getSecond().contains(a)) {
                                secondIntersection.add(a);
                            }
                        }
                        boolean isSkipCandidate = true;
                        for (String s : firstIntersection) {
                            if (secondIntersection.contains(s)) {
                                isSkipCandidate = false;
                            }
                        }
                        if (isSkipCandidate) {
                            skipCandidates.add(new Pair<>(e, p));
                        }
//                    skippablePartsEndpoints.add(new Pair<>(firstIntersection,secondIntersection));
                    });
                }
            }
            Optional<Pair<Set<String>, Set<String>>> beforeCnd = null;
            Optional<Pair<Set<String>, Set<String>>> afterCnd = null;
            if (e.getSecond().containsAll(e.getFirst())) {
                beforeCnd = candidates.stream().filter(p -> (p.getSecond().equals(e.getFirst()) && p.getFirst().containsAll(p.getSecond()))).findAny();
            } else if (e.getFirst().containsAll(e.getSecond())) {
                afterCnd = candidates.stream().filter(p -> (p.getFirst().equals(e.getSecond()) && p.getSecond().containsAll(p.getFirst()))).findAny();
            }
            if (beforeCnd != null && beforeCnd.isPresent()) {
//                Loop
//                Only add incoming arcs
                for (String a : e.getFirst()) {
                    if (logProcessor.getActivities().contains(a)) {
                        Transition from = Utils.getTransitionWithLabel(net, a);
                        Place to = Utils.getPlaceWithLabel(net, e.toString());
                        net.addArc(from, to);
                    }
                }
//                Tau transition to go "back" to other place
//                Transition t = net.addTransition("tau");
//                t.setInvisible(true);
                Place otherPlace = Utils.getPlaceWithLabel(net, beforeCnd.get().toString());
                Place thisPlace = Utils.getPlaceWithLabel(net, e.toString());
//                net.addArc(thisPlace,t);
//                net.addArc(t,otherPlace);
                if (getOptionValueByID("loop_detection")) {
                    if (!thisPlace.equals(otherPlace)) {
                        activitiesWithLoopTau.add(e.getFirst());
                    }
                }
//                activitiesWithLoopTau.add(new Pair<>(e.getFirst(),t));
                for (String a : e.getSecond()) {
                    if (logProcessor.getActivities().contains(a) && !e.getFirst().contains(a)) {
                        Transition to = Utils.getTransitionWithLabel(net, a);
                        Place from = Utils.getPlaceWithLabel(net, e.toString());
                        net.addArc(from, to);
                    }
                }
            } else {
                for (String a : e.getFirst()) {
                    if (logProcessor.getActivities().contains(a) && (afterCnd == null || !afterCnd.isPresent() || !e.getSecond().contains(a))) {
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
        }
        Set<Pair<Set<Place>, Set<Place>>> connectWithTau = new HashSet<>();
        for (Set<String> loopAct : activitiesWithLoopTau) {
            for (String activity : loopAct) {
//                Transition tau = net.addTransition("tau");
//                tau.setInvisible(true);
                Transition transition = Utils.getTransitionWithLabel(net, activity);
                Set<Place> inPlaces = new HashSet<>();
                Set<Place> outPlaces = new HashSet<>();
                for (PetrinetEdge e : net.getInEdges(transition)) {
                    Place p = (Place) e.getSource();
                    outPlaces.add(p);
//                    if(net.getArc(tau,p) == null){
//                      PetrinetEdge edge = net.addArc(tau,p);
//                      System.out.println(edge);
//                    }
                }
                for (PetrinetEdge e : net.getOutEdges(transition)) {
                    Place p = (Place) e.getTarget();
                    inPlaces.add(p);
//                    if(net.getArc(p,tau) == null){
//                        PetrinetEdge edge = net.addArc(p,tau);
//                        System.out.println(edge);
//                    }
                }
                connectWithTau.add(new Pair<>(inPlaces, outPlaces));
            }
        }
        System.out.println("#connectWithTau" + connectWithTau.size());
        for (Pair<Set<Place>, Set<Place>> places : connectWithTau) {
            Transition tau = net.addTransition("tau: tau skip candidate");
            tau.setInvisible(true);
            for (Place p : places.getFirst()) {
                net.addArc(p, tau);
            }
            for (Place p : places.getSecond()) {
                net.addArc(tau, p);
            }
        }

        for (Pair<Pair<Set<String>, Set<String>>, Pair<Set<String>, Set<String>>> skipEndpoints : skipCandidates) {
            Pair<Set<String>, Set<String>> start = skipEndpoints.getSecond();
            Pair<Set<String>, Set<String>> end = skipEndpoints.getFirst();
            Set<String> firstIntersection = new HashSet<>();
            for (String a : start.getFirst()) {
                if (end.getFirst().contains(a)) {
                    firstIntersection.add(a);
                }
            }
            Set<String> secondIntersection = new HashSet<>();
            for (String a : end.getSecond()) {
                if (start.getSecond().contains(a)) {
                    secondIntersection.add(a);
                }
            }

            for (String s : firstIntersection) {
                Transition t = Utils.getTransitionWithLabel(net, s);
                for (PetrinetEdge e : net.getOutEdges(t)) {
                    Place target = (Place) e.getTarget();
                    if (target.getLabel().equals(end.toString())) {
                        net.removeEdge(e);
                    }
                }
            }


            for (String s : secondIntersection) {
                Transition t = Utils.getTransitionWithLabel(net, s);
                for (PetrinetEdge e : net.getInEdges(t)) {
                    Place source = (Place) e.getSource();
                    if (source.getLabel().equals(start.toString())) {
                        net.removeEdge(e);
                    }
                }
            }
            if (!firstIntersection.equals(secondIntersection)) {
//                break;
                Transition tau = net.addTransition("skip tau: " + start + "; " + end);
                tau.setInvisible(true);
                net.addArc(Utils.getPlaceWithLabel(net, start.toString()), tau);
                net.addArc(tau, Utils.getPlaceWithLabel(net, end.toString()));
            }
        }

        for (String tauName : candidatesWithTau.keySet()) {
            Transition tau = net.addTransition(tauName);
            tau.setInvisible(true);
            for (Pair<Set<String>, Set<String>> cnd : candidatesWithTau.get(tauName)) {
                if (cnd.getFirst().contains(tauName)) {
//                    First
                    net.addArc(tau, Utils.getPlaceWithLabel(net, cnd.toString()));
                } else {
//                    Second

                    net.addArc(Utils.getPlaceWithLabel(net, cnd.toString()), tau);
                }
            }
        }
        System.out.println("Done building Petri net");
        return AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking, finalMarking);
    }

    @Override
    public Map<Pair<Set<String>, Set<String>>, Place> getCandidatePlaceMap() {
        return candidatePlaceMap;
    }
}
