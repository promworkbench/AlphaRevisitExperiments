package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.LogRepairStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.options.ExperimentOptionCandidateChangeIndicator;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.processmining.alpharevisitexperiments.util.ReplayProcessor.getTopVariants;

public class InfrequentVariantEliminationLogRepair extends LogRepairStep {
    public final static String NAME = "Infrequent Variant Elimination Log Repair";


    final ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, "top_perc_to_keep", "Keep most frequent variants accounting to this % of traces", 100.0, 0.0, 100.0, "Keep the most frequent trace variants accounting for the specified percentage of traces. The other, low-frequent, case variants are removed.", ExperimentOptionCandidateChangeIndicator.DEPENDS),

    };

    public InfrequentVariantEliminationLogRepair() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public LogProcessor repairLog(UIPluginContext context, LogProcessor logProcessor) {
        double topPerc = getOptionValueByID("top_perc_to_keep");
        Set<String> freqVars = Arrays.stream(getTopVariants(logProcessor.getVariants(), logProcessor.getNumberOfCases(), topPerc)).collect(Collectors.toSet());
        Set<String> variantsToRemove = new HashSet<>();
        for (String variant : logProcessor.getVariants().keySet()) {
            if (!freqVars.contains(variant)) {
                variantsToRemove.add(variant);
            }
        }

        for (String variant : variantsToRemove) {
            String actBefore = null;
            for (String a : variant.split(",")) {
                int before = logProcessor.getActivityOccurrences().get(a);
                int newCount = before - logProcessor.getVariants().get(variant);
                if (newCount > 0) {
                    logProcessor.getActivityOccurrences().put(a, newCount);
                } else {
                    logProcessor.getActivityOccurrences().put(a, 0);
//                    TODO: maybe also delete activity from activities?
                }
                if (actBefore != null) {
                    Pair<String, String> dfPair = new Pair<>(actBefore, a);
                    int dfBefore = logProcessor.getDfg().getOrDefault(dfPair, 0);
                    int dfNew = dfBefore - logProcessor.getVariants().get(variant);
                    if (dfNew > 0) {
                        logProcessor.getDfg().put(dfPair, dfNew);
                    } else {
                        logProcessor.getDfg().remove(dfPair);
                    }
                }
            }
            int casesBefore = logProcessor.getNumberOfCases();
            logProcessor.setNumberOfCases(casesBefore - logProcessor.getVariants().get(variant));
            logProcessor.getVariants().remove(variant);
        }


        return logProcessor;
    }
}
