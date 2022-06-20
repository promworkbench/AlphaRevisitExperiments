package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.processmining.alpharevisitexperiments.util.LogProcessor.END_ACTIVITY;
import static org.processmining.alpharevisitexperiments.util.LogProcessor.START_ACTIVITY;

public class AlphaOneDotOne extends AlgorithmExperiment {
    final static ExperimentOption[] options = {
    };
    private Set<Pair<String, String>> dfRelation;
    private LogProcessor logProcessor;

    public AlphaOneDotOne() {
        super("Alpha 1.1", options);
    }

    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {

        context.getProgress().setIndeterminate(true);
        context.log("Preparing mining...");
        long lastTimeStart = System.nanoTime();
        Petrinet net = PetrinetFactory.newPetrinet("Petri net");
        Marking initialMarking = new Marking();
        Marking finalMarking = new Marking();

        logProcessor = new LogProcessor(log);
        HashSet<String> activities = logProcessor.getActivities();
        HashSet<String> activitiesHat = new HashSet<>(activities);
        activitiesHat.add(START_ACTIVITY);
        activitiesHat.add(END_ACTIVITY);
        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        dfRelation = dfg.keySet().stream().filter((e) -> dfg.get(e) > 0).collect(Collectors.toSet());

        Set<Pair<HashSet<String>, HashSet<String>>> initialCnd = new HashSet<>();
        for (Pair<String, String> el : dfRelation) {
            if (!dfRelation.contains(new Pair<>(el.getSecond(), el.getFirst()))
                    && !dfRelation.contains(new Pair<>(el.getFirst(), el.getFirst()))
                    && !dfRelation.contains(new Pair<>(el.getSecond(), el.getSecond()))
            ) {
                initialCnd.add(new Pair<HashSet<String>, HashSet<String>>(new HashSet<>(Collections.singleton(el.getFirst())), new HashSet<>(Collections.singleton(el.getSecond()))));
            }
        }

        System.out.println("Initial TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        lastTimeStart = System.nanoTime();

        context.log("Building initial candidate set...");
        ArrayList<Pair<HashSet<String>, HashSet<String>>> cnd = new ArrayList<>(initialCnd);
        HashSet<Pair<HashSet<String>, HashSet<String>>> cndSet = new HashSet<>();

        System.out.println("Cnd and cndset built TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        context.log("Recursively generating new candidates...");
        lastTimeStart = System.nanoTime();
        int newlyAdded = 0;
        int wereAlreadyAdded = 0;

        int notDFAll = 0;
        int allDF = 0;
        for (int i = 0; i < cnd.size(); i++) {
            for (int j = i + 1; j < cnd.size(); j++) {
                if (cnd.get(i) != cnd.get(j)) {
                    Set<String> a1 = cnd.get(i).getFirst(), a2 = cnd.get(i).getSecond(), b1 = cnd.get(j).getFirst(), b2 = cnd.get(j).getSecond();
//                    Size comparison to short-circuit combinations that do not event fit wrt. size
                    if ((a1.size() >= b1.size() && a1.containsAll(b1)) || (a2.size() >= b2.size() && a2.containsAll(b2))) {
                        if (isNoDFRelationBetween(a1, b1) && isNoDFRelationBetween(a2, b2)) {
                            HashSet<String> first = new HashSet<>(a1);
                            first.addAll(b1);
                            HashSet<String> second = new HashSet<>(a2);
                            second.addAll(b2);
                            if (areAllRelationsDFBetween(first, second)) {
                                allDF++;
                                Pair<HashSet<String>, HashSet<String>> newCandidate = new Pair<>(first, second);
                                if (!cndSet.contains(newCandidate)) {
                                    cndSet.add(newCandidate);
                                    cnd.add(newCandidate);
                                    newlyAdded++;
                                } else {
                                    wereAlreadyAdded++;
                                }
                            } else {
                                notDFAll++;
                            }
                        }

                    }
                }
            }
        }
        System.out.println("newlyAdded:" + newlyAdded + " wereAlreadyAdded:" + wereAlreadyAdded);
        System.out.println("notDFAll:" + notDFAll + " allDF:" + allDF);
        System.out.println("After cnd loop TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));

        context.log("Filtering out maximal candidates...");

        lastTimeStart = System.nanoTime();

        ArrayList<Pair<HashSet<String>, HashSet<String>>> sel = new ArrayList<>();
        for (Pair<HashSet<String>, HashSet<String>> c : cnd) {
            boolean isMaximal = cnd.stream().noneMatch(e -> e != c && e.getFirst().containsAll(c.getFirst()) && e.getSecond().containsAll(c.getSecond()));
            if (isMaximal) {
                sel.add(c);
            }
        }
        System.out.println("After isMaximal checks TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        context.log("Generating final Petri net...");

        lastTimeStart = System.nanoTime();

        for (Pair<HashSet<String>, HashSet<String>> e : sel) {
            Place p = net.addPlace(e.toString());
            if (e.getFirst().contains(START_ACTIVITY)) {
                initialMarking.add(p, 1);
            }
            if (e.getSecond().contains(END_ACTIVITY)) {
                finalMarking.add(p, 1);
            }
        }
        for (String activity : activities) {
            net.addTransition(activity);
        }

        for (Pair<HashSet<String>, HashSet<String>> e : sel) {
            for (String a : e.getFirst()) {
                if (activities.contains(a)) {
                    Transition from = Utils.getTransitionWithLabel(net, a);
                    Place to = Utils.getPlaceWithLabel(net, e.toString());
                    net.addArc(from, to);
                }
            }
            for (String a : e.getSecond()) {
                if (activities.contains(a)) {
                    Transition to = Utils.getTransitionWithLabel(net, a);
                    Place from = Utils.getPlaceWithLabel(net, e.toString());
                    net.addArc(from, to);
                }
            }
        }

        System.out.println("After Petri net built TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        System.out.println(net.getPlaces().size() + " #Places");
        System.out.println(net.getEdges().size() + " #Arcs");

        return AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking, finalMarking);
    }


    private boolean isNoDFRelationBetween(Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (dfRelation.contains(new Pair<>(a, b)) || dfRelation.contains(new Pair<>(b, a))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean areAllRelationsDFBetween(Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (!dfRelation.contains(new Pair<>(a, b)) || dfRelation.contains(new Pair<>(b, a))) {
                    return false;
                }
            }
        }
        return true;
    }


    public LogProcessor getLogProcessor() {
        return logProcessor;
    }


}

