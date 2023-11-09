package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.CandidatePruningStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.options.ExperimentOptionCandidateChangeIndicator;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class BalanceBasedCandidatePruning extends CandidatePruningStep {

    public final static String NAME = "Prune Based on Balance";
    final ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, "balance_value", "Balance Threshold (b)", 0.2, 0.0, 1.0, "The balance threshold filters place candidates based on their (relative) frequency in the event log. A low balance value is a necessary (but not sufficient) condition for a fit place.", ExperimentOptionCandidateChangeIndicator.HIGH_INCREASES_CANDIDATES),
    };
    private LogProcessor logProcessor;

    public BalanceBasedCandidatePruning() {
        super(NAME);
        setOptions(options);
    }

    public BalanceBasedCandidatePruning(double balance_value) {
        super(NAME);
        options[0].setValue(balance_value);
        setOptions(options);
    }


    @Override
    public Set<Pair<Set<String>, Set<String>>> pruneCandidates(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {
        this.logProcessor = logProcessor;
        double b = getOptionValueByID("balance_value");
        return candidates.stream().filter(c -> {
            double candidateBalance = getBalance(c.getFirst(), c.getSecond());
            return candidateBalance <= b;
        }).collect(Collectors.toSet());
    }

    private double getBalance(Collection<String> as, Collection<String> bs) {
        double countAs = getNumberOfOccurrences(as);
        double countBs = getNumberOfOccurrences(bs);
        return Math.abs(countAs - countBs) / (Math.max(countAs, countBs));
    }

    private int getNumberOfOccurrences(Collection<String> as) {
        int sum = 0;
        for (String act : as) {
            sum += logProcessor.getActivityOccurrence(act);
        }
        return sum;
    }
}
