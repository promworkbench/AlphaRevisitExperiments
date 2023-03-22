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
            new ExperimentOption<>(Double.class, "significant_df_threshold_relative", "significant_df_threshold_relative", 2.0, 0.1, 100000.0),
    };

    public NamedTauLogRepair() {
        super(NAME);
        setOptions(options);
    }


    private static boolean isSignificantDFBetween(LogProcessor logProcessor, String a, String b, double df_threshold) {
        return logProcessor.getDfg().getOrDefault(new Pair<>(a, b), 0) >= df_threshold;
    }

    @Override
    public LogProcessor repairLog(UIPluginContext context, LogProcessor logProcessor) {
        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        final double SIGNIFICANT_DF_THRESHOLD = logProcessor.getAbsoluteValueFromRelativeDFThreshold(this.getOptionValueByID("significant_df_threshold_relative"));
        System.out.println("Using df_threshold of " + SIGNIFICANT_DF_THRESHOLD + " for SkipLogRepair");

        HashSet<String> newNamedTauActivities = new HashSet<>();
        for (String a : logProcessor.getActivities()) {
            if (dfg.getOrDefault(new Pair<>(a, a), 0) == 0) {
                Set<String> outgoingFromA = dfg.keySet().stream().filter(r -> r.getFirst().equals(a) && dfg.get(r) >= SIGNIFICANT_DF_THRESHOLD).map(r -> r.getSecond()).collect(Collectors.toSet());
                Set<String> canSkip = new HashSet<>();
                if (outgoingFromA.size() > 0) {

                    for (Pair<String, String> df : dfg.keySet()) {
                        if (df.getFirst().equals(a) && !df.getSecond().equals(a) && logProcessor.getActivities().contains(df.getSecond())) {
                            String b = df.getSecond();
                            if (!isSignificantDFBetween(logProcessor, b, b, SIGNIFICANT_DF_THRESHOLD) && !isSignificantDFBetween(logProcessor, b, a, SIGNIFICANT_DF_THRESHOLD)) {
                                Set<String> outgoingFromB = dfg.keySet().stream().filter(r -> r.getFirst().equals(b) && dfg.get(r) >= SIGNIFICANT_DF_THRESHOLD).map(r -> r.getSecond()).collect(Collectors.toSet());
                                if (outgoingFromA.containsAll(outgoingFromB)) {
                                    canSkip.add(b);
                                }


                            }

                        }
                    }
                }
// Update variants and DF
                if (canSkip.size() > 0) {
                    String newNamedTau = "skip_after_" + a;
                    assert !logProcessor.getActivities().contains(newNamedTau);
                    HashMap<String, Integer> variantsBefore = logProcessor.getVariants();
                    HashMap<String, Integer> variantsAfter = new HashMap<>();
                    for (String variant : variantsBefore.keySet()) {
                        String[] variantSplit = variant.split(",");
                        ArrayList<String> newVariantSplit = new ArrayList<>();
                        for (int i = 0; i < variantSplit.length; i++) {
                            newVariantSplit.add(variantSplit[i]);
                            if (variantSplit[i].equals(a) && i + 1 < variantSplit.length && !canSkip.contains(variantSplit[i + 1])) {
                                newVariantSplit.add(newNamedTau);
                            }
                        }
                        variantsAfter.put(String.join(",", newVariantSplit), variantsBefore.get(variant));
                    }

                    logProcessor.setVariants(variantsAfter);

                    int dfCountFromAToOther = 0;
                    Set<Pair<String, String>> dfstoDelete = new HashSet<>();
                    for (Pair<String, String> df : dfg.keySet()) {
                        if (df.getFirst().equals(a) && !canSkip.contains(df.getSecond())) {
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
                    newNamedTauActivities.add(newNamedTau);
                    logProcessor.getActivityOccurrences().put(newNamedTau, dfCountFromAToOther);
                }
            }
        }
        for (String newNamedTauAct : newNamedTauActivities) {
            System.out.println("added new named tau activity: " + newNamedTauAct);
//            Add new artificial activity to set of activities
//            Note, that we already updated the count before (see above)
            logProcessor.getActivities().add(newNamedTauAct);
        }
        return logProcessor;

    }
}
