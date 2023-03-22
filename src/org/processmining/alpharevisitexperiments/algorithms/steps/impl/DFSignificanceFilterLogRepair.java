package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.LogRepairStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class DFSignificanceFilterLogRepair extends LogRepairStep {
    public final static String NAME = "Filter DF using Threshold Log Repair";

    final ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, "significant_df_threshold", "significant_df_threshold", 0.0, 0.0, 100000.0),
    };

    public DFSignificanceFilterLogRepair() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public LogProcessor repairLog(UIPluginContext context, LogProcessor logProcessor) {
        final double SIGNIFICANT_DF_THRESHOLD = getOptionValueByID("significant_df_threshold");
        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        Set<Pair<String, String>> dfToDelete = dfg.keySet().stream().filter(k -> {
//            double bc. of fractional calculation done later
            double outgoingEdgeSum = 0;
            double outgoingEdgeCount = 0;
            double incomingEdgeSum = 0;
            double incomingEdgeCount = 0;
            for (Pair<String, String> edge : dfg.keySet()) {
                if (edge.getFirst().equals(k.getFirst())) {
                    outgoingEdgeSum += dfg.getOrDefault(edge, 0);
                    outgoingEdgeCount += 1;
                }
                if (edge.getSecond().equals(k.getSecond())) {
                    incomingEdgeSum += dfg.getOrDefault(edge, 0);
                    incomingEdgeCount += 1;
                }
            }
            int v = dfg.get(k);
            return !((v >= 0.01 * outgoingEdgeSum / outgoingEdgeCount || v >= 0.01 * incomingEdgeSum / incomingEdgeCount) && dfg.getOrDefault(k, 0) > SIGNIFICANT_DF_THRESHOLD);
        }).collect(Collectors.toSet());
        for (Pair<String, String> df : dfToDelete) {
            dfg.remove(df);
        }
        logProcessor.setDfg(dfg);
        return logProcessor;

    }
}
