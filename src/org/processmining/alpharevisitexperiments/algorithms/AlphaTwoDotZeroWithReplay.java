package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.ReplayProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

public class AlphaTwoDotZeroWithReplay extends AlgorithmExperiment{


    final static ExperimentOption[] options = ReplayProcessor.STANDARD_REPLAY_OPTIONS;
    final AlphaTwoDotZero alphaTwoDotZero = new AlphaTwoDotZero();

    public AlphaTwoDotZeroWithReplay() {
        super("Alpha 2.0 legacy with replay", options);
    }


    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        AcceptingPetriNet net = alphaTwoDotZero.execute(context,log);
        String[] frequentVariants = ReplayProcessor.getTopVariants(alphaTwoDotZero.getLogProcessor().getVariants(), log.size(),this.getOptionValueByID(ReplayProcessor.FREQUENT_VARIANT_OPTION_ID));
        ReplayProcessor.replayAndRemovePlaces(net, frequentVariants,this.getOptionValueByID(ReplayProcessor.DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID));
        return net;
    }
}
