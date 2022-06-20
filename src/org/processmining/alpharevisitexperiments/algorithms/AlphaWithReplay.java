package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.alpharevisitexperiments.util.ReplayProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.HashMap;

public class AlphaWithReplay extends AlgorithmExperiment {
    final static ExperimentOption[] options = ReplayProcessor.STANDARD_REPLAY_OPTIONS;

    public AlphaWithReplay() {
        super("Alpha with replay", options);
    }

    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        Object[] objs = AlphaMinerPlugin.applyAlphaClassic(context, log, new XEventNameClassifier());
        Petrinet net = (Petrinet) objs[0];
        Marking initialMarking = (Marking) objs[1];
        Marking finalMarking = new Marking();
        for (Place p : net.getPlaces()) {
            if (p.getLabel().equals("End")) {
                finalMarking.add(p, 1);
            }
        }

        AcceptingPetriNet acceptingNet = AcceptingPetriNetFactory.createAcceptingPetriNet(net, initialMarking, finalMarking);

        LogProcessor logProcessor = new LogProcessor(log);
        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        String[] frequentVariants = ReplayProcessor.getTopVariants(logProcessor.getVariants(), log.size(), this.getOptionValueByID(ReplayProcessor.FREQUENT_VARIANT_OPTION_ID));
        ReplayProcessor.replayAndRemovePlaces(acceptingNet, frequentVariants, this.getOptionValueByID(ReplayProcessor.DO_NOT_REMOVE_STARTEND_PLACES_OPTION_ID));

        return acceptingNet;
    }


}
