package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

public class StandardAlpha extends AlgorithmExperiment{
    final static ExperimentOption[] options = {
    };

    public StandardAlpha() {
        super("Standard alpha",options);
    }

    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        Object[] objs = AlphaMinerPlugin.applyAlphaClassic(context, log, new XEventNameClassifier());
        Petrinet net = (Petrinet) objs[0];
        Marking initialMarking = (Marking) objs[1];
        Marking finalMarking = new Marking();
        return AcceptingPetriNetFactory.createAcceptingPetriNet(net,initialMarking,finalMarking);
    }

}
