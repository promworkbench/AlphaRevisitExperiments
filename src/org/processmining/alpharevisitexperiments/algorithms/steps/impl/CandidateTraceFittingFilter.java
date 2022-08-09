package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.CandidatePruningStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.Set;
import java.util.stream.Collectors;

public class CandidateTraceFittingFilter extends CandidatePruningStep {

    public final static String NAME = "Filter out low trace-fitness candidates";
    final ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, "min_fitting_traces", "Minimal fitting traces fraction", 0.0, 0.0, 1.0),
    };

    public CandidateTraceFittingFilter() {
        super(NAME);
        setOptions(options);
    }

    private static double getCandidateScore(LogProcessor logProcessor, Pair<Set<String>, Set<String>> candidate) {
        int underfedTraces = 0;
        int overfedTraces = 0;
        int fittingTraces = 0;
        for (String variant : logProcessor.getVariants().keySet()) {
            int weight = logProcessor.getVariants().getOrDefault(variant, 0);
            int tokenCount = 0;
            String[] trace = variant.split(",");
            for (String activity : trace) {
                if (candidate.getFirst().contains(activity)) {
                    tokenCount++;
                }

                if (candidate.getSecond().contains(activity)) {
                    tokenCount--;
                }

                if (tokenCount < 0) {
                    underfedTraces++;
                    break;
                }
            }
            if (tokenCount < 0) {
                underfedTraces += weight;
            } else if (tokenCount > 0) {
                overfedTraces += weight;
            } else {
                fittingTraces += weight;
            }
        }
        return ((double) fittingTraces) / (logProcessor.getLog().size());
    }

    @Override
    public Set<Pair<Set<String>, Set<String>>> pruneCandidates(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {
        double b = getOptionValueByID("min_fitting_traces");
        return candidates.stream().filter(c -> getCandidateScore(logProcessor, c) >= b).collect(Collectors.toSet());
    }
}
