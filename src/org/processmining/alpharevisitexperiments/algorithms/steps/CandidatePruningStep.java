package org.processmining.alpharevisitexperiments.algorithms.steps;

import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.Set;

public abstract class CandidatePruningStep extends Step {
    protected CandidatePruningStep(String name) {
        super(name);
    }

    public abstract Set<Pair<Set<String>, Set<String>>> pruneCandidates(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates);

    @Override
    public String getTypeName() {
        return "Pruning Candidates";
    }
}
