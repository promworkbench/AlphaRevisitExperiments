package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.LogRepairStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static org.processmining.alpharevisitexperiments.util.LogProcessor.END_ACTIVITY;
import static org.processmining.alpharevisitexperiments.util.LogProcessor.START_ACTIVITY;

public class NamedTauLoopLogRepair extends LogRepairStep {
    public final static String NAME = "Named Tau Loop Log Repair";


    final ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, "significant_df_threshold_relative", "significant_df_threshold_relative", 2.0, 0.1, 100000.0),
    };

    public NamedTauLoopLogRepair() {
        super(NAME);
        setOptions(options);
    }


    private Set<String> getDFActs(String a, LogProcessor logProcessor, double df_threshold) {
        return logProcessor.getDfg().keySet().stream().filter(e -> e.getFirst().equals(a) && logProcessor.getDfg().getOrDefault(e, 0) > df_threshold).map(e -> e.getSecond()).collect(Collectors.toSet());
    }


    private Set<List<String>> getReachableBF(String activity, LogProcessor logProcessor, double df_threshold) {
        Set<List<String>> reachable = new HashSet<>();
        Set<List<String>> finishedReachable = new HashSet<>();
        Set<String> directlyReachable = getDFActs(activity, logProcessor, df_threshold);
        for (String a : directlyReachable) {
            if (!a.equals(activity)) {
                List<String> l = new ArrayList<>();
                l.add(activity);
                l.add(a);
                reachable.add(l);
            }
        }
        boolean expanded = true;
        while (expanded) {
            expanded = false;
            Set<List<String>> additions = new HashSet<>();
            for (List<String> visitedActs : reachable) {
                String lastVisitedAct = visitedActs.get(visitedActs.size() - 1);
                Set<String> dfAs = getDFActs(lastVisitedAct, logProcessor, df_threshold);
                if (dfAs.size() == 0) {
                    finishedReachable.add(visitedActs);
                } else {
                    for (String a : dfAs) {
                        List<String> newPath = new ArrayList<>(visitedActs);
                        if (newPath.contains(a)) {
//                                Loop found
                            newPath.add(a);
                            finishedReachable.add(newPath);
                        } else {
                            newPath.add(a);
                            additions.add(newPath);
                            expanded = true;
                        }
                    }
                }
            }
            reachable = additions;
            reachable.removeAll(finishedReachable);
        }
//        Add also those without loops
        finishedReachable.addAll(reachable);
        return finishedReachable;
    }

    @Override
    public LogProcessor repairLog(UIPluginContext context, LogProcessor logProcessor) {

        final double SIGNIFICANT_DF_THRESHOLD = logProcessor.getAbsoluteValueFromRelativeDFThreshold(this.getOptionValueByID("significant_df_threshold_relative"));

        System.out.println("Using Loop Log Repair with df_threshold of " + SIGNIFICANT_DF_THRESHOLD);
        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        HashSet<Pair<String, String>> insertNamedTausBetween = new HashSet<>();

        Set<List<String>> bf_approach_reachable = getReachableBF(START_ACTIVITY, logProcessor, SIGNIFICANT_DF_THRESHOLD);
        Set<List<String>> loop_candidates = bf_approach_reachable.stream().filter(e -> !e.get(e.size() - 1).equals(END_ACTIVITY)).collect(Collectors.toSet());

        for (List<String> path : loop_candidates) {
            if (path.size() >= 2) {
                String act = path.get(path.size() - 1);
                String actBefore = path.get(path.size() - 2);
                insertNamedTausBetween.add(new Pair<>(actBefore, act));
            }

        }

        for (Pair<String, String> newNamedTau : insertNamedTausBetween) {
            String newActName = "skip_loop_" + newNamedTau.getFirst() + "_" + newNamedTau.getSecond();
            assert !logProcessor.getActivities().contains(newActName);
            System.out.println("Adding new activity " + newActName);
            HashMap<String, Integer> variantsBefore = logProcessor.getVariants();
            HashMap<String, Integer> variantsAfter = new HashMap<>();
            for (String variant : variantsBefore.keySet()) {
                String newVariant = variant.replaceAll(newNamedTau.getFirst() + "," + newNamedTau.getSecond(), newNamedTau.getFirst() + "," + newActName + "," + newNamedTau.getSecond());
                variantsAfter.put(newVariant, variantsBefore.get(variant));

            }
            logProcessor.setVariants(variantsAfter);

            int dfCount = dfg.getOrDefault(newNamedTau, 0);


            logProcessor.getActivities().add(newActName);
            logProcessor.getActivityOccurrences().put(newActName, dfCount);

            dfg.remove(newNamedTau);

            dfg.put(new Pair<>(newNamedTau.getFirst(), newActName), dfCount);
            dfg.put(new Pair<>(newActName, newNamedTau.getSecond()), dfCount);


        }

        return logProcessor;

    }
}
