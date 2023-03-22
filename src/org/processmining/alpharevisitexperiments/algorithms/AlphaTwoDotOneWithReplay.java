package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.ReplayProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

public class AlphaTwoDotOneWithReplay extends AlgorithmExperiment {


    final static ExperimentOption[] options = ReplayProcessor.STANDARD_REPLAY_OPTIONS;
    final AlphaTwoDotOne alphaTwoDotOne = new AlphaTwoDotOne();

    public AlphaTwoDotOneWithReplay() {
        super("Alpha 2.0 with replay", options);
    }


    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        AcceptingPetriNet net = alphaTwoDotOne.execute(context, log);
        String[] frequentVariants = ReplayProcessor.getTopVariants(alphaTwoDotOne.getLogProcessor().getVariants(), log.size(), this.getOptionValueByID(ReplayProcessor.FREQUENT_VARIANT_OPTION_ID));
        ReplayProcessor.replayAndRemovePlaces(net, frequentVariants, false);
        return net;
    }
}
