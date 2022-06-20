package org.processmining.alpharevisitexperiments.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.algorithms.AlgorithmExperiment;
import org.processmining.contexts.uitopia.UIPluginContext;

import javax.swing.*;

public class ExperimentRunner extends SwingWorker<AcceptingPetriNet, Void> {
    private final AlgorithmExperiment experiment;
    private final UIPluginContext context;
    private final XLog log;

    public ExperimentRunner(AlgorithmExperiment experiment, UIPluginContext context, XLog log) {
        this.experiment = experiment;
        this.context = context;
        this.log = log;
    }

    @Override
    protected AcceptingPetriNet doInBackground() throws Exception {
        return experiment.execute(context, log);
    }
}
