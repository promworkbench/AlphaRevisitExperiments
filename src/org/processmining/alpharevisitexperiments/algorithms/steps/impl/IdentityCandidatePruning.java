package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.CandidatePruningStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.Set;

public class IdentityCandidatePruning extends CandidatePruningStep {

    public final static String NAME = "Skip (Don't remove candidates in this step)";
    final ExperimentOption[] options = {
    };

    public IdentityCandidatePruning() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public Set<Pair<Set<String>, Set<String>>> pruneCandidates(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {
        return candidates;
    }
}
