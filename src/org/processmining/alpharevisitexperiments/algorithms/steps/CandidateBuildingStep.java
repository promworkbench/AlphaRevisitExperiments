package org.processmining.alpharevisitexperiments.algorithms.steps;

import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.Set;

public abstract class CandidateBuildingStep extends Step {

    protected CandidateBuildingStep(String name) {
        super(name);
    }


    public abstract Set<Pair<Set<String>, Set<String>>> buildCandidates(UIPluginContext context, LogProcessor logProcessor);

    @Override
    public String getTypeName() {
        return "Constructing Candidates";
    }

}
