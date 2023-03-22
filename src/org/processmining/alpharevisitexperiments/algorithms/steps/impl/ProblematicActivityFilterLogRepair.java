package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.LogRepairStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.processmining.alpharevisitexperiments.util.LogProcessor.END_ACTIVITY;
import static org.processmining.alpharevisitexperiments.util.LogProcessor.START_ACTIVITY;

public class ProblematicActivityFilterLogRepair extends LogRepairStep {
    public final static String NAME = "Problematic Activity Filter Log Repair";

    final ExperimentOption[] options = {
    };

    public ProblematicActivityFilterLogRepair() {
        super(NAME);
        setOptions(options);
    }

    private static void increaseDFValueBy(Map<Pair<String, String>, Integer> dfg, String actA, String actB, int increaseBy) {
        Pair<String, String> p = new Pair<>(actA, actB);
        int prevValue = dfg.getOrDefault(p, 0);
        dfg.put(p, prevValue + increaseBy);
    }

    private static void addVariantToDFG(Map<Pair<String, String>, Integer> dfg, String variant, int count) {
        String[] acts = variant.split(",");
        String prev = START_ACTIVITY;
        for (int i = 0; i < acts.length; i++) {
            increaseDFValueBy(dfg, prev, acts[i], count);
            prev = acts[i];
        }
        increaseDFValueBy(dfg, prev, END_ACTIVITY, count);
    }

    @Override
    public LogProcessor repairLog(UIPluginContext context, LogProcessor logProcessor) {
        Set<String> problematicActivities = new HashSet<>();
        problematicActivities.add("W_Complete application");

        HashMap<String, Integer> oldVariants = logProcessor.getVariants();
        HashMap<String, Integer> newVariants = new HashMap<>();
        HashMap<Pair<String, String>, Integer> newDfg = new HashMap<>();
        for (String variant : oldVariants.keySet()) {
            String newVariant = variant;
            for (String problematicAct : problematicActivities) {
//              TODO: Fix: If one activity is a prefix of another this could cause problems!
                newVariant = newVariant.replaceAll(problematicAct + ",", "");
            }
            int count = oldVariants.getOrDefault(variant, 0);
            newVariants.put(newVariant, count);
            addVariantToDFG(newDfg, newVariant, count);

        }
        for (String problematicAct : problematicActivities) {
            logProcessor.getActivityOccurrences().remove(problematicAct);
            logProcessor.getActivities().remove(problematicAct);
            logProcessor.getLastInCaseActivities().remove(problematicAct);
            logProcessor.getFirstInCaseActivities().remove(problematicAct);
        }
        logProcessor.setVariants(newVariants);
        logProcessor.setDfg(newDfg);

        return logProcessor;

    }
}
