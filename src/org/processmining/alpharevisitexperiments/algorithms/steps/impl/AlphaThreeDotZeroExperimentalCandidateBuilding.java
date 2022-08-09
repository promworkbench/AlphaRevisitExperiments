package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.CandidateBuildingStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.alpharevisitexperiments.util.Utils;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.processmining.alpharevisitexperiments.util.LogProcessor.END_ACTIVITY;
import static org.processmining.alpharevisitexperiments.util.LogProcessor.START_ACTIVITY;

public class AlphaThreeDotZeroExperimentalCandidateBuilding extends CandidateBuildingStep {
    public final static String NAME = "Alpha 2.0/3.0 EXPERIMENTAL Candidate Building";
    final ExperimentOption[] options = {
            new ExperimentOption<>(Boolean.class, "tau_candidates_skip", "Enable tau candidate skip.", true),
            new ExperimentOption<>(Boolean.class, "weak_third_condition", "Weak third condition (exists...)", true),
    };
    private Set<Pair<String, String>> dfRelation;

    public AlphaThreeDotZeroExperimentalCandidateBuilding() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public Set<Pair<Set<String>, Set<String>>> buildCandidates(UIPluginContext context, LogProcessor logProcessor) {

        context.getProgress().setIndeterminate(true);
        context.log("Building Candidates");
        long lastTimeStart = System.nanoTime();
        HashSet<String> activities = logProcessor.getActivities();
        HashSet<String> activitiesHat = new HashSet<>(activities);
        activitiesHat.add(START_ACTIVITY);
        activitiesHat.add(END_ACTIVITY);
        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        dfRelation = dfg.keySet().stream().filter((e) -> dfg.get(e) > 0).collect(Collectors.toSet());

        Set<Pair<Set<String>, Set<String>>> initialCnd = new HashSet<>();
        Set<Pair<Set<String>, Set<String>>> expandCandidates = new HashSet<>();
        for (String a : activitiesHat) {
            for (String b : activitiesHat) {
                if (dfRelation.contains(new Pair<>(a, b))
                        && !dfRelation.contains(new Pair<>(b, a))
                        && !dfRelation.contains(new Pair<>(a, a))
                        && !dfRelation.contains(new Pair<>(b, b))) {
                    initialCnd.add(new Pair<Set<String>, Set<String>>(new HashSet<>(Collections.singleton(a)), new HashSet<>(Collections.singleton(b))));

//                    Tau-optional check
                    if (getOptionValueByID("tau_candidates_skip")) {

                        if (!b.equals(START_ACTIVITY) && !b.equals(END_ACTIVITY)) {
                            Set<String> outgoingFromA = dfRelation.stream().filter(r -> r.getFirst().equals(a)).map(r -> r.getSecond()).collect(Collectors.toSet());
                            Set<String> outgoingFromB = dfRelation.stream().filter(r -> r.getFirst().equals(b)).map(r -> r.getSecond()).collect(Collectors.toSet());
                            if (outgoingFromA.containsAll(outgoingFromB)) {
//                        b seems to be optional?
                                String tau = "TAU(" + b + "_" + a + ")";
                                Set<String> bWithTau = new HashSet<>();
                                bWithTau.add(b);
                                bWithTau.add(tau);

                                initialCnd.add(new Pair<Set<String>, Set<String>>(new HashSet<>(Collections.singleton(a)), new HashSet<>(bWithTau)));
                                for (String out : outgoingFromB) {
                                    initialCnd.add(new Pair<Set<String>, Set<String>>(new HashSet<>(bWithTau), new HashSet<>(Collections.singleton(out))));
                                }
                            }
                        }
                    }
                } else {
                    expandCandidates.add(new Pair<Set<String>, Set<String>>(new HashSet<>(Collections.singleton(a)), new HashSet<>(Collections.singleton(b))));

                }
            }
        }
//        for (Pair<String, String> el : dfRelation) {
//            if (!dfRelation.contains(new Pair<String, String>(el.getSecond(), el.getFirst()))) {
//                initialCnd.add(new Pair<Set<String>, Set<String>>(new HashSet<>(Collections.singleton(el.getFirst())), new HashSet<>(Collections.singleton(el.getSecond()))));
//            } else {
//                expandCandidates.add(new Pair<Set<String>, Set<String>>(new HashSet<>(Collections.singleton(el.getFirst())), new HashSet<>(Collections.singleton(el.getSecond()))));
//            }
//        }


        System.out.println("Initial TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        lastTimeStart = System.nanoTime();

        context.log("Building initial candidate set...");
        ArrayList<Pair<Set<String>, Set<String>>> cnd = new ArrayList<>(initialCnd);
        cnd.addAll(expandCandidates);
        Set<Pair<Set<String>, Set<String>>> cndSet = new HashSet<>(initialCnd);

        System.out.println("Cnd and cndset built TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        context.log("Recursively generating new candidates...");
        lastTimeStart = System.nanoTime();

        boolean weakThirdCondition = getOptionValueByID("weak_third_condition");
        for (int i = 0; i < cnd.size(); i++) {
            for (int j = 0; j < cnd.size(); j++) {
                Set<String> a1 = cnd.get(i).getFirst(), a2 = cnd.get(i).getSecond(), b1 = cnd.get(j).getFirst(), b2 = cnd.get(j).getSecond();
                HashSet<String> first = new HashSet<>(a1);
                first.addAll(b1);
                HashSet<String> second = new HashSet<>(a2);
                second.addAll(b2);

                Set<String> all = new HashSet<>(first);
                all.addAll(second);
                if (Utils.areAllRelationsDFBetweenNonStrict(dfRelation, first, second)) { // checks 2

//                    a1 + b1, (a1 + b1 - a2 - b2)
//                    NO DF relation between
//                    a2 + b2 - a1 - b1, a2 + b2
                    HashSet<String> firstWithoutSecond = new HashSet<>(first);
                    firstWithoutSecond.removeAll(second);
                    HashSet<String> secondWithoutFirst = new HashSet<>(second);
                    secondWithoutFirst.removeAll(first);
                    if (

                            ((Utils.areNotAllRelationsDF(dfRelation, firstWithoutSecond, secondWithoutFirst) || (weakThirdCondition && (firstWithoutSecond.isEmpty() || secondWithoutFirst.isEmpty()))) && Utils.isNoDFRelationBetweenAsymmetric(dfRelation, first, firstWithoutSecond) && Utils.isNoDFRelationBetweenAsymmetric(dfRelation, secondWithoutFirst, second))
                    ) { // checks 3 + 4 + 5
                        Pair<Set<String>, Set<String>> newCandidate = new Pair<>(first, second);
                        if (!cndSet.contains(newCandidate)) {
                            cndSet.add(newCandidate);
                            cnd.add(newCandidate);
                        }
                    }
                }
            }
        }

        return cndSet;
    }
}
