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

public class AlphaThreeDotZero extends AlgorithmExperiment {
    final static ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, "balance_value", "Balance threshold (b)", 1.0, 0.0, 1.0),
//            new ExperimentOption<>(Boolean.class, "balance_ignore_startend", "Always include potential start/end places for b-balanced candidates.", false),
    };
    private Set<Pair<String, String>> dfRelation;
    private LogProcessor logProcessor;

    public AlphaThreeDotZero() {
        super("Alpha 3.0", options);
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
        Set<Pair<HashSet<String>, HashSet<String>>> expandCandidates = new HashSet<>();
        for (Pair<String, String> el : dfRelation) {
            if (!dfRelation.contains(new Pair<String, String>(el.getSecond(), el.getFirst()))) {
                initialCnd.add(new Pair<HashSet<String>, HashSet<String>>(new HashSet<>(Collections.singleton(el.getFirst())), new HashSet<>(Collections.singleton(el.getSecond()))));
            } else {
                expandCandidates.add(new Pair<HashSet<String>, HashSet<String>>(new HashSet<>(Collections.singleton(el.getFirst())), new HashSet<>(Collections.singleton(el.getSecond()))));
            }
        }


        System.out.println("Initial TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        lastTimeStart = System.nanoTime();

        context.log("Building initial candidate set...");
        ArrayList<Pair<HashSet<String>, HashSet<String>>> cnd = new ArrayList<>(initialCnd);
        cnd.addAll(expandCandidates);
        Set<Pair<HashSet<String>, HashSet<String>>> cndSet = new HashSet<>(initialCnd);

        System.out.println("Cnd and cndset built TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        context.log("Recursively generating new candidates...");
        lastTimeStart = System.nanoTime();
        int newlyAdded = 0;
        int wereAlreadyAdded = 0;

        int notDFAll = 0;
        int allDF = 0;
        for (int i = 0; i < cnd.size(); i++) {
            for (int j = 0; j < cnd.size(); j++) {
                Set<String> a1 = cnd.get(i).getFirst(), a2 = cnd.get(i).getSecond(), b1 = cnd.get(j).getFirst(), b2 = cnd.get(j).getSecond();
                HashSet<String> first = new HashSet<>(a1);
                first.addAll(b1);
                HashSet<String> second = new HashSet<>(a2);
                second.addAll(b2);

                Set<String> all = new HashSet<>(first);
                all.addAll(second);
                if (areAllRelationsDFBetweenNonStrict(first, second)) { // checks 2

//                    a1 + b1, (a1 + b1 - a2 - b2)
//                    NO DF relation between
//                    a2 + b2 - a1 - b1, a2 + b2
                    HashSet<String> firstWithoutSecond = new HashSet<>(first);
                    firstWithoutSecond.removeAll(second);
                    HashSet<String> secondWithoutFirst = new HashSet<>(second);
                    secondWithoutFirst.removeAll(first);
                    if (checkConditionThree(firstWithoutSecond, secondWithoutFirst) && isNoDFRelationBetweenAsymmetric(first, firstWithoutSecond) && isNoDFRelationBetweenAsymmetric(secondWithoutFirst, second)) { // checks 3 + 4 + 5
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
//                        }
//                    }
//                    }
                }
            }
        }
        System.out.println("newlyAdded:" + newlyAdded + " wereAlreadyAdded:" + wereAlreadyAdded);
        System.out.println("notDFAll:" + notDFAll + " allDF:" + allDF);
        System.out.println("After cnd loop TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        double b = getOptionValueByID("balance_value");
//        boolean ignoreBalanceForStartEnd = getOptionValueByID("balance_ignore_startend");

        cndSet = cndSet.stream().filter(c -> {
//            if (ignoreBalanceForStartEnd && (c.getFirst().contains(START_ACTIVITY) || c.getSecond().contains(END_ACTIVITY))) {
//                return true;
//            }
            double candidateBalance = getBalance(c.getFirst(), c.getSecond());
            return candidateBalance <= b;
        }).collect(Collectors.toSet());

        context.log("Filtering out maximal candidates...");

        lastTimeStart = System.nanoTime();

        ArrayList<Pair<HashSet<String>, HashSet<String>>> sel = new ArrayList<>();
        for (Pair<HashSet<String>, HashSet<String>> c : cndSet) {
            Set<Pair<HashSet<String>, HashSet<String>>> containedBy = cndSet.stream().filter(e -> e != c && e.getFirst().containsAll(c.getFirst()) && e.getSecond().containsAll(c.getSecond())).collect(Collectors.toSet());
            if (containedBy.size() == 0) {
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
            } else if (e.getSecond().contains(END_ACTIVITY)) {
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

    private boolean isNoDFRelationBetweenAsymmetric(Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (dfRelation.contains(new Pair<>(a, b))) {
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


    private boolean areAllRelationsDFBetweenNonStrict(Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (!dfRelation.contains(new Pair<>(a, b))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkConditionThree(Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (!dfRelation.contains(new Pair<>(b, a))) {
                    return true;
                }
            }
        }
        return false;
    }

    public LogProcessor getLogProcessor() {
        return logProcessor;
    }

    private double getBalance(Collection<String> as, Collection<String> bs) {
        double countAs = getNumberOfOccurrences(as);
        double countBs = getNumberOfOccurrences(bs);
        return Math.abs(countAs - countBs) / (Math.max(countAs, countBs));
    }

    private int getNumberOfOccurrences(Collection<String> as) {
        int sum = 0;
        for (String act : as) {
            sum += logProcessor.getActivityOccurrence(act);
        }
        return sum;
    }
}

