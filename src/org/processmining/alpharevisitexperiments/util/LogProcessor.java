package org.processmining.alpharevisitexperiments.util;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;

import java.util.HashMap;
import java.util.HashSet;

public class LogProcessor {

    public final static String START_ACTIVITY = "__START";
    public final static String END_ACTIVITY = "__END";
    private HashMap<Pair<String, String>, Integer> dfg = new HashMap<>();
    private HashMap<String, Integer> variants = new HashMap<>();
    private HashMap<String, Integer> activityOccurrences = new HashMap<>();
    private HashSet<String> activities = new HashSet<>();

    private final HashSet<String> firstInCaseActivities = new HashSet<>();
    private final HashSet<String> lastInCaseActivities = new HashSet<>();

    private final XLog log;

    private int numberOfCases = 0;


    private double meanDFInitial = 0;

    public LogProcessor(XLog log) {
        this.log = log;
        this.numberOfCases = log.size();
        for (XTrace trace : log) {
            String previousActivity = null;
            String[] currentTrace = new String[trace.size() + 2];
            currentTrace[0] = START_ACTIVITY;
            currentTrace[trace.size() + 1] = END_ACTIVITY;
            increaseActivityOccurenceByOne(START_ACTIVITY);
            increaseActivityOccurenceByOne(END_ACTIVITY);
            for (int i = 0; i < trace.size(); i++) {
                String activity = trace.get(i).getAttributes().get("concept:name").toString().replaceAll(",", "_");
                increaseActivityOccurenceByOne(activity);
                if (i == 0) {
                    firstInCaseActivities.add(activity);
                } else if (i == trace.size() - 1) {
                    lastInCaseActivities.add(activity);
                }
                activities.add(activity);
                currentTrace[i + 1] = activity;
                Pair<String, String> edge;
                if (i == 0) {
                    edge = new Pair<>(START_ACTIVITY, activity);
                    Integer currentValue = dfg.getOrDefault(edge, 0);
                    dfg.put(edge, currentValue + 1);
                }
                if (i == trace.size() - 1) {
                    if (i > 0) {
                        edge = new Pair<>(previousActivity, activity);
                        Integer currentValue = dfg.getOrDefault(edge, 0);
                        dfg.put(edge, currentValue + 1);
                    }
                    int currTraceFrequency = variants.getOrDefault(String.join(",", currentTrace), 0);
                    variants.put(String.join(",", currentTrace), currTraceFrequency + 1);
                } else if (i > 0) {
                    edge = new Pair<>(previousActivity, activity);
                    Integer currentValue = dfg.getOrDefault(edge, 0);
                    dfg.put(edge, currentValue + 1);
                }
                previousActivity = activity;
            }

            Pair<String, String> edge = new Pair<String, String>(previousActivity, END_ACTIVITY);
            Integer currentValue = dfg.getOrDefault(edge, 0);
            dfg.put(edge, currentValue + 1);
        }
        int n = dfg.size();
        double sum = dfg.values().stream().mapToInt(Integer::intValue).sum();
        this.meanDFInitial = sum / n;

    }

    private void increaseActivityOccurenceByOne(String activity) {
        int count = activityOccurrences.getOrDefault(activity, 0);
        count += 1;
        activityOccurrences.put(activity, count);
    }

    public HashMap<Pair<String, String>, Integer> getDfg() {
        return dfg;
    }

    public HashMap<String, Integer> getVariants() {
        return variants;
    }

    public void setVariants(HashMap<String, Integer> newVariants) {
        this.variants = newVariants;
    }

    public void setActivities(HashSet<String> newActivities) {
        this.activities = newActivities;
    }

    public void setDfg(HashMap<Pair<String, String>, Integer> newDfg) {
        this.dfg = newDfg;
    }


    public int getNumberOfCases() {
        return numberOfCases;
    }

    public void setNumberOfCases(int numberOfCases) {
        this.numberOfCases = numberOfCases;
    }

    public void setActivityOccurrences(HashMap<String, Integer> newActivityOccurrences) {
        this.activityOccurrences = newActivityOccurrences;
    }


    public HashSet<String> getActivities() {
        return activities;
    }

    public HashMap<String, Integer> getActivityOccurrences() {
        return activityOccurrences;
    }

    public Integer getActivityOccurrence(String activity) {
        return activityOccurrences.getOrDefault(activity, 0);
    }

    public XLog getLog() {
        return log;
    }

    public HashSet<String> getFirstInCaseActivities() {
        return firstInCaseActivities;
    }

    public HashSet<String> getLastInCaseActivities() {
        return lastInCaseActivities;
    }

    public double getAbsoluteValueFromRelativeDFThreshold(double relativeValue) {
        return relativeValue * this.meanDFInitial;
    }
}
