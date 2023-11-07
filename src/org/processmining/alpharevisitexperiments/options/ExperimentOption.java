package org.processmining.alpharevisitexperiments.options;

public class ExperimentOption<T extends Comparable<T>> {
    private final String name;
    private final String id;
    private final T startValue;
    private final Class<T> type;
    private T currentValue;
    private T maxValue;
    private T minValue;

    private final String hintText;

    private final ExperimentOptionCandidateChangeIndicator changeIndicator;

    public ExperimentOption(Class<T> type, String id, String name, T startValue, String hintText, ExperimentOptionCandidateChangeIndicator changeIndicator) {
        this.id = id;
        this.name = name;
        this.startValue = startValue;
        this.setValue(startValue);
        this.type = type;
        this.hintText = hintText;
        this.changeIndicator = changeIndicator;
    }

    public ExperimentOption(Class<T> type, String id, String name, T startValue, T minValue, T maxValue, String hintText, ExperimentOptionCandidateChangeIndicator changeIndicator) {
        this(type, id, name, startValue, hintText, changeIndicator);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public T getStartValue() {
        return startValue;
    }

    public T getValue() {
        return currentValue;
    }

    public void setValue(T newVal) {
        if (minValue != null && newVal.compareTo(minValue) < 0) {
            return;
        } else if (maxValue != null && newVal.compareTo(maxValue) > 0) {
            return;
        } else {
            currentValue = newVal;
        }
    }

    public Class<T> getType() {
        return this.type;
    }

    public T getMinValue() {
        return minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public String getHintText() {
        return hintText;
    }

    public ExperimentOptionCandidateChangeIndicator getChangeIndicator() {
        return changeIndicator;
    }
}
