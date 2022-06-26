package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.CandidatePruningStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaximalCandidatesPruning extends CandidatePruningStep {

    public final static String NAME = "Select Maximal Candidates Only)";
    final ExperimentOption[] options = {
    };

    public MaximalCandidatesPruning() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public Set<Pair<Set<String>, Set<String>>> pruneCandidates(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {
        Set<Pair<Set<String>, Set<String>>> sel = new HashSet<>();
        for (Pair<Set<String>, Set<String>> c : candidates) {
            Set<Pair<Set<String>, Set<String>>> containedBy = candidates.stream().filter(e -> e != c && e.getFirst().containsAll(c.getFirst()) && e.getSecond().containsAll(c.getSecond())).collect(Collectors.toSet());
            if (containedBy.size() == 0) {
                sel.add(c);
            }
        }
        return sel;
    }
}
