package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.LogRepairStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.options.ExperimentOptionCandidateChangeIndicator;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class DFSignificanceFilterLogRepair extends LogRepairStep {
    public final static String NAME = "Create Advising DFG";

    final ExperimentOption[] options = {
            new ExperimentOption<>(Integer.class, "significant_df_threshold", "Absolute DF threshold (n)", 1, 0, 100000, "Filter DFG based on the given absolute threshold (i.e., removing low-frequency edges in DFG).", ExperimentOptionCandidateChangeIndicator.DEPENDS),
            new ExperimentOption<>(Double.class, "significant_df_threshold_rel", "Relative DF threshold", 0.01, 0.0, 1.0, "Filter DFG based on the given relative threshold (i.e., removing low-frequency edges in DFG).", ExperimentOptionCandidateChangeIndicator.DEPENDS),
    };

    public DFSignificanceFilterLogRepair() {
        super(NAME);
        setOptions(options);
    }

    public DFSignificanceFilterLogRepair(int significant_df_threshold, double significant_df_threshold_rel) {
        super(NAME);
        options[0].setValue(significant_df_threshold);
        options[1].setValue(significant_df_threshold_rel);
        setOptions(options);
    }


    @Override
    public LogProcessor repairLog(UIPluginContext context, LogProcessor logProcessor) {
        final int SIGNIFICANT_DF_THRESHOLD = getOptionValueByID("significant_df_threshold");
        final double SIGNIFICANT_DF_THRESHOLD_REL = getOptionValueByID("significant_df_threshold_rel");
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
            return !((v >= SIGNIFICANT_DF_THRESHOLD_REL * outgoingEdgeSum / outgoingEdgeCount || v >= SIGNIFICANT_DF_THRESHOLD_REL * incomingEdgeSum / incomingEdgeCount) && dfg.getOrDefault(k, 0) >= SIGNIFICANT_DF_THRESHOLD);
        }).collect(Collectors.toSet());
        for (Pair<String, String> df : dfToDelete) {
            dfg.remove(df);
        }
        logProcessor.setDfg(dfg);
        return logProcessor;

    }
}
