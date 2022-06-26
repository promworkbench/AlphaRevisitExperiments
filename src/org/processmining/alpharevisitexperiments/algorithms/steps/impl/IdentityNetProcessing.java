package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.algorithms.steps.PostProcessingPetriNetStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

public class IdentityNetProcessing extends PostProcessingPetriNetStep {
    public final static String NAME = "Skip (No Replay - Keep Petri net as is)";
    final ExperimentOption[] options = {
    };

    public IdentityNetProcessing() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public AcceptingPetriNet processPetriNet(UIPluginContext context, LogProcessor logProcessor, AcceptingPetriNet net) {
        return net;
    }
}
