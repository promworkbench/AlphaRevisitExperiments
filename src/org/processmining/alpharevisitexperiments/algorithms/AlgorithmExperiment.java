package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.Arrays;
import java.util.Optional;

public abstract class AlgorithmExperiment {
    public final String name;
    private ExperimentOption[] options;

    protected AlgorithmExperiment(String name, ExperimentOption[] options) {
        this.name = name;
        this.options = options;
    }

    public ExperimentOption[] getOptions() {
        return this.options;
    }

    public void setOptions(ExperimentOption[] options) {
        this.options = options;
    }

    public AcceptingPetriNet execute(UIPluginContext context, LogProcessor logProcessor) {
        throw new UnsupportedOperationException("Constructing Experiment with LogProcessor is not supported for this algorithm.");
    }

    public abstract AcceptingPetriNet execute(UIPluginContext context, XLog log);


    public <T extends Comparable<?>> T getOptionValueByID(String id) {
        Optional<ExperimentOption> optional = Arrays.stream(options).filter(o -> o.getID().equals(id)).findAny();
        if (optional.isPresent()) {
            ExperimentOption option = optional.get();
            System.out.println("Option " + id + ": " + option);
            T value = (T) option.getValue();
            return value;
        } else {
            System.err.println("Experiment Option not found: " + id);
            return null;
        }

    }

    @Override
    public String toString() {
        return name;
    }
}
