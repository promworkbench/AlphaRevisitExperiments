package org.processmining.alpharevisitexperiments.util;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;

import java.util.HashMap;
import java.util.HashSet;

public class LogProcessor {

    public final static String START_ACTIVITY = "__START";
    public final static String END_ACTIVITY = "__END";
    private final HashMap<Pair<String, String>, Integer> dfg = new HashMap<>();
    private final HashMap<String, Integer> variants = new HashMap<>();

    private final HashMap<String, Integer> activityOccurrences = new HashMap<>();
    private final HashSet<String> activities = new HashSet<>();

    public LogProcessor(XLog log) {
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

                activities.add(activity);
                currentTrace[i + 1] = activity;
                Pair<String, String> edge;
                if (i == 0) {
                    edge = new Pair<>(START_ACTIVITY, activity);
                } else if (i == trace.size() - 1) {
                    edge = new Pair<>(previousActivity, activity);
                    int currTraceFrequency = variants.getOrDefault(String.join(",", currentTrace), 0);
                    variants.put(String.join(",", currentTrace), currTraceFrequency + 1);
                } else {
                    edge = new Pair<>(previousActivity, activity);
                }
                Integer currentValue = dfg.getOrDefault(edge, 0);
                dfg.put(edge, currentValue + 1);
                previousActivity = activity;
            }

            Pair<String, String> edge = new Pair<String, String>(previousActivity, END_ACTIVITY);
            Integer currentValue = dfg.getOrDefault(edge, 0);
            dfg.put(edge, currentValue + 1);
        }
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

    public HashSet<String> getActivities() {
        return activities;
    }

    public HashMap<String, Integer> getActivityOccurrences() {
        return activityOccurrences;
    }

    public Integer getActivityOccurrence(String activity) {
        return activityOccurrences.getOrDefault(activity, 0);
    }

}
