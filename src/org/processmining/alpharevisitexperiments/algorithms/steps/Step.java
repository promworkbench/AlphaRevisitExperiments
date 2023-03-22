package org.processmining.alpharevisitexperiments.algorithms.steps;

import org.processmining.alpharevisitexperiments.options.ExperimentOption;

import java.util.Arrays;

public abstract class Step {

    public final String name;

    private ExperimentOption[] options = {
    };

    protected Step(String name) {
        this.name = name;
    }

    public ExperimentOption[] getOptions() {
        return this.options;
    }

    public void setOptions(ExperimentOption[] options) {
        this.options = options;
    }


    public <T extends Comparable<?>> T getOptionValueByID(String id) {
        ExperimentOption option = Arrays.stream(options).filter(o -> o.getID().equals(id)).findAny().get();
        T value = (T) option.getValue();
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract String getTypeName();
}
