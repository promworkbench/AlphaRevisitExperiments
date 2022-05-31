package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.ReplayProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

public class AlphaOneDotOneWithReplay extends AlgorithmExperiment{


    final static ExperimentOption[] options = ReplayProcessor.STANDARD_REPLAY_OPTIONS;
    final AlphaOneDotOne alphaOneDotOne = new AlphaOneDotOne();

    public AlphaOneDotOneWithReplay() {
        super("Alpha 1.1 with replay",options);
    }


    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        AcceptingPetriNet net = alphaOneDotOne.execute(context,log);
        String[] frequentVariants = ReplayProcessor.getTopVariants(alphaOneDotOne.getLogProcessor().getVariants(), log.size(),this.getOptionValueByID(ReplayProcessor.FREQUENT_VARIANT_OPTION_ID));
        ReplayProcessor.replayAndRemovePlaces(net, frequentVariants, this.getOptionValueByID(ReplayProcessor.DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID));
        return net;
    }
}
