package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.contexts.uitopia.UIPluginContext;

import java.util.Arrays;

public abstract class AlgorithmExperiment {
    private ExperimentOption[] options;
    public final String name;

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

    public abstract AcceptingPetriNet execute(UIPluginContext context, XLog log);


    public <T extends Comparable<?>> T getOptionValueByID(String id) {
        ExperimentOption option = Arrays.stream(options).filter(o -> o.getID().equals(id)).findAny().get();
        System.out.println("Option " + id + ": " + option);
        T value = (T) option.getValue();
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
}
