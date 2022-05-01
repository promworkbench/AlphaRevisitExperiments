package org.processmining.alpharevisitexperiments.options;

public class ExperimentOption<T extends Comparable<T>> {
    private final String name;
    private final String id;
    private final T startValue;
    private T currentValue;

    private T maxValue;
    private T minValue;
    private Class<T> type;
    public ExperimentOption(Class<T> type, String id, String name, T startValue) {
        this.id = id;
        this.name = name;
        this.startValue = startValue;
        this.setValue(startValue);
        this.type = type;
    }

    public ExperimentOption(Class<T> type, String id, String name, T startValue, T minValue, T maxValue) {
        this(type,id, name, startValue);
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

    public Class<T> getType(){
        return this.type;
    }

    public void setValue(T newVal) {
        if(minValue != null && newVal.compareTo(minValue) < 0) {
            return;
        }else if(maxValue != null && newVal.compareTo(maxValue) > 0) {
            return;
        }else {
            currentValue = newVal;
        }
    }

    public T getMinValue() {
        return minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

}
