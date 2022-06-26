package org.processmining.alpharevisitexperiments.algorithms.steps;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

public abstract class PostProcessingPetriNetStep extends Step {
    protected PostProcessingPetriNetStep(String name) {
        super(name);
    }

    public abstract AcceptingPetriNet processPetriNet(UIPluginContext context, LogProcessor logProcessor, AcceptingPetriNet net);

    @Override
    public String getTypeName() {
        return "Post-processing Petri net";
    }
}
