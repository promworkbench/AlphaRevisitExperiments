package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.ReplayProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.Arrays;
import java.util.stream.Stream;

public class AlphaThreeDotZeroWithReplay extends AlgorithmExperiment {


    final static ExperimentOption[] options = Stream.concat(Arrays.stream(AlphaThreeDotZero.options), Arrays.stream(ReplayProcessor.STANDARD_REPLAY_OPTIONS))
            .toArray(ExperimentOption[]::new);
    final AlphaThreeDotZero alphaThreeDotZero = new AlphaThreeDotZero();

    public AlphaThreeDotZeroWithReplay() {
        super("Alpha 3.0 with replay", options);
    }


    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        alphaThreeDotZero.setOptions(options);
        AcceptingPetriNet net = alphaThreeDotZero.execute(context, log);
        String[] frequentVariants = ReplayProcessor.getTopVariants(alphaThreeDotZero.getLogProcessor().getVariants(), log.size(), this.getOptionValueByID(ReplayProcessor.FREQUENT_VARIANT_OPTION_ID));
        ReplayProcessor.replayAndRemovePlaces(net, frequentVariants, this.getOptionValueByID(ReplayProcessor.DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID));
        return net;
    }
}
