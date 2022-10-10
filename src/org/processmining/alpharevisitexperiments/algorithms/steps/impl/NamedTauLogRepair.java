package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.LogRepairStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NamedTauLogRepair extends LogRepairStep {
    public final static String NAME = "Named Tau Log Repair";


    final ExperimentOption[] options = {
    };

    public NamedTauLogRepair() {
        super(NAME);
    }

    @Override
    public LogProcessor repairLog(UIPluginContext context, LogProcessor oldLogProcessor) {
        LogProcessor logProcessor = new LogProcessor(oldLogProcessor.getLog()); // TODO: Change this. Make a way to create a copy of a log processor based on processed data. This will cause problems as soon as multiple consecutive Repair steps are possible.
        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        for (String a : logProcessor.getActivities()) {
            for (String b : logProcessor.getActivities()) {
                if (dfg.getOrDefault(new Pair<>(a, b), 0) > 0
                        && dfg.getOrDefault(new Pair<>(b, a), 0) <= 0
                        && dfg.getOrDefault(new Pair<>(a, a), 0) <= 0
                        && dfg.getOrDefault(new Pair<>(b, b), 0) <= 0) {

                    Set<String> outgoingFromA = dfg.keySet().stream().filter(r -> r.getFirst().equals(a)).map(r -> r.getSecond()).collect(Collectors.toSet());
                    Set<String> outgoingFromB = dfg.keySet().stream().filter(r -> r.getFirst().equals(b)).map(r -> r.getSecond()).collect(Collectors.toSet());
                    if (outgoingFromA.containsAll(outgoingFromB)) {
//                        b seems to be optional?

// Update variants and DF
                        String newNamedTau = "skip_" + b;
                        assert !logProcessor.getActivities().contains(newNamedTau);
                        HashMap<String, Integer> variantsBefore = logProcessor.getVariants();
                        HashMap<String, Integer> variantsAfter = new HashMap<>();
                        for (String variant : variantsBefore.keySet()) {
                            String[] variantSplit = variant.split(",");
                            ArrayList<String> newVariantSplit = new ArrayList<>();
                            for (int i = 0; i < variantSplit.length; i++) {
                                newVariantSplit.add(variantSplit[i]);
                                if (variantSplit[i].equals(a) && i < variantSplit.length && !variantSplit[i + 1].equals(b)) {
                                    newVariantSplit.add(newNamedTau);
                                }
                            }
                            variantsAfter.put(String.join(",", newVariantSplit), variantsBefore.get(variant));
                        }

                        logProcessor.setVariants(variantsAfter);

                        int dfCountFromAToOther = 0;
                        Set<Pair<String, String>> dfstoDelete = new HashSet<>();
                        for (Pair<String, String> df : dfg.keySet()) {
                            if (df.getFirst().equals(a) && !df.getSecond().equals(b)) {
                                dfCountFromAToOther += dfg.get(df);
                                dfstoDelete.add(df);
                            }
                        }

                        for (Pair<String, String> df : dfstoDelete) {
                            dfg.put(new Pair<>(newNamedTau, df.getSecond()), dfg.get(df));
                            dfg.remove(df);
                        }
                        dfg.put(new Pair<>(a, newNamedTau), dfCountFromAToOther);

                        logProcessor.setDfg(dfg);

                        logProcessor.getActivities().add(newNamedTau);
                        logProcessor.getActivityOccurrences().put(newNamedTau, dfCountFromAToOther);

//                        initialCnd.add(new Pair<Set<String>, Set<String>>(new HashSet<>(Collections.singleton(a)), new HashSet<>(bWithTau)));
//                        for (String out : outgoingFromB) {
//                            initialCnd.add(new Pair<Set<String>, Set<String>>(new HashSet<>(bWithTau), new HashSet<>(Collections.singleton(out))));
//                        }
                    }
                }
            }
        }
        return logProcessor;

    }
}
