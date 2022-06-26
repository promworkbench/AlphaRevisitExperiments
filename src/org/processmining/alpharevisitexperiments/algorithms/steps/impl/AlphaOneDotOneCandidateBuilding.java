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

public class AlphaOneDotOneCandidateBuilding extends CandidateBuildingStep {
    public final static String NAME = "Alpha 1.1 Candidate Building";
    final ExperimentOption[] options = {
    };
    private Set<Pair<String, String>> dfRelation;

    public AlphaOneDotOneCandidateBuilding() {
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
        for (Pair<String, String> el : dfRelation) {
            if (!dfRelation.contains(new Pair<>(el.getSecond(), el.getFirst()))
                    && !dfRelation.contains(new Pair<>(el.getFirst(), el.getFirst()))
                    && !dfRelation.contains(new Pair<>(el.getSecond(), el.getSecond()))
            ) {
                initialCnd.add(new Pair<Set<String>, Set<String>>(new HashSet<>(Collections.singleton(el.getFirst())), new HashSet<>(Collections.singleton(el.getSecond()))));
            }
        }

        System.out.println("Initial TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        lastTimeStart = System.nanoTime();

        context.log("Building initial candidate set...");
        ArrayList<Pair<Set<String>, Set<String>>> cnd = new ArrayList<>(initialCnd);
        Set<Pair<Set<String>, Set<String>>> cndSet = new HashSet<>(initialCnd);

        System.out.println("Cnd and cndset built TIME" + TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeStart, TimeUnit.NANOSECONDS));
        context.log("Recursively generating new candidates...");
        lastTimeStart = System.nanoTime();
        for (int i = 0; i < cnd.size(); i++) {
            for (int j = i + 1; j < cnd.size(); j++) {
                if (cnd.get(i) != cnd.get(j)) {
                    Set<String> a1 = cnd.get(i).getFirst(), a2 = cnd.get(i).getSecond(), b1 = cnd.get(j).getFirst(), b2 = cnd.get(j).getSecond();
//                    Size comparison to short-circuit combinations that do not event fit wrt. size
                    if ((a1.size() >= b1.size() && a1.containsAll(b1)) || (a2.size() >= b2.size() && a2.containsAll(b2))) {
                        if (Utils.isNoDFRelationBetweenSymmetric(dfRelation, a1, b1) && Utils.isNoDFRelationBetweenSymmetric(dfRelation, a2, b2)) {
                            HashSet<String> first = new HashSet<>(a1);
                            first.addAll(b1);
                            HashSet<String> second = new HashSet<>(a2);
                            second.addAll(b2);
                            if (Utils.areAllRelationsDFBetweenStrict(dfRelation, first, second)) {
                                Pair<Set<String>, Set<String>> newCandidate = new Pair<>(first, second);
                                if (!cndSet.contains(newCandidate)) {
                                    cndSet.add(newCandidate);
                                    cnd.add(newCandidate);
                                }
                            }
                        }

                    }
                }
            }
        }
        return cndSet;
    }
}
