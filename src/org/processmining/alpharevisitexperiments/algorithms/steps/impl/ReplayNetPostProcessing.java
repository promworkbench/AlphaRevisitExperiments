package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.algorithms.steps.PostProcessingPetriNetStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.alpharevisitexperiments.util.ReplayProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

import static org.processmining.alpharevisitexperiments.util.ReplayProcessor.DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID;
import static org.processmining.alpharevisitexperiments.util.ReplayProcessor.FREQUENT_VARIANT_OPTION_ID;

public class ReplayNetPostProcessing extends PostProcessingPetriNetStep {

    public final static String NAME = "Replay & Remove problematic places";
    public final ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, FREQUENT_VARIANT_OPTION_ID, "Cover this percentage of all traces with the frequent variants used for replay and place removal", 0.0, 0.0, 100.0),
            new ExperimentOption<>(Boolean.class, DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID, "Do not remove places that part of a start or end marking.", false),
    };

    public ReplayNetPostProcessing() {
        super(NAME);
        setOptions(options);
    }

    @Override
    public AcceptingPetriNet processPetriNet(UIPluginContext context, LogProcessor logProcessor, AcceptingPetriNet net) {
        String[] frequentVariants = ReplayProcessor.getTopVariants(logProcessor.getVariants(), logProcessor.getNumberOfCases(), this.getOptionValueByID(FREQUENT_VARIANT_OPTION_ID));
        ReplayProcessor.replayAndRemovePlaces(net, frequentVariants, this.getOptionValueByID(DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID));
        return net;
    }
}
