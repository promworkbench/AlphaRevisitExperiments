package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import com.google.common.collect.Sets;
import org.processmining.alpharevisitexperiments.algorithms.steps.CandidatePruningStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.options.ExperimentOptionCandidateChangeIndicator;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class CandidateTraceFittingFilter extends CandidatePruningStep {

    public final static String NAME = "Filter out low trace-fitness candidates";
    final ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, "min_fitting_traces", "Fitness threshold (t)", 0.8, 0.0, 1.0, "Locally replay event log traces on place candidates and filter out candidates based on how well they fit the traces, removing all candidates with fitness percentage below the specified value.", ExperimentOptionCandidateChangeIndicator.HIGH_DECREASES_CANDIDATES),
    };

    public CandidateTraceFittingFilter() {
        super(NAME);
        setOptions(options);
    }

    private static boolean getCandidateScore(LogProcessor logProcessor, Pair<Set<String>, Set<String>> candidate, double t) {
        int underfedTraces = 0;
        int overfedTraces = 0;
        int fittingTraces = 0;
        int fittingWithActs = 0;
        int numberOfTracesWithActs = 0;
        Set<String> allActsInCandidate = new HashSet<>(candidate.getFirst());
        allActsInCandidate.addAll(candidate.getSecond());
        HashMap<String, Integer> numOfTracesContainingAct = new HashMap<>();
        HashMap<String, Integer> numOfFittingTracesContainingAct = new HashMap<>();
        for (String a : allActsInCandidate) {
            numOfTracesContainingAct.put(a, 0);
            numOfFittingTracesContainingAct.put(a, 0);
        }

        for (String variant : logProcessor.getVariants().keySet()) {
            int weight = logProcessor.getVariants().getOrDefault(variant, 0);
            int tokenCount = 0;
            String[] trace = variant.split(",");
            for (String a : allActsInCandidate) {
//                    Does trace contain a?
                if (Arrays.asList(trace).contains(a)) {
                    int count = numOfTracesContainingAct.get(a);
                    numOfTracesContainingAct.put(a, count + weight);
                }
            }
            if (Sets.intersection(Arrays.stream(trace).collect(Collectors.toSet()), allActsInCandidate).size() > 0) {
                numberOfTracesWithActs += weight;
            }
            for (String activity : trace) {

//                if(candidate.getFirst().contains(activity) && candidate.getSecond().contains((activity))){
//                    if(tokenCount <= 0){
//                        tokenCount = -1;
//                        break;
//                    }
//                }
                if (candidate.getFirst().contains(activity)) {
                    tokenCount++;
                }

                if (candidate.getSecond().contains(activity)) {
                    tokenCount--;
                }

                if (tokenCount < 0) {
                    underfedTraces += weight;
                    break;
                }
            }
            if (tokenCount < 0) {
                underfedTraces += weight;
            } else if (tokenCount > 0) {
                overfedTraces += weight;
            } else {
                fittingTraces += weight;
                for (String a : allActsInCandidate) {
//                    Does trace contain a?
                    if (Arrays.asList(trace).contains(a)) {
                        int count = numOfFittingTracesContainingAct.get(a);
                        numOfFittingTracesContainingAct.put(a, count + weight);
                    }
                }
                if (Sets.intersection(Arrays.stream(trace).collect(Collectors.toSet()), allActsInCandidate).size() > 0) {
                    fittingWithActs += weight;
                }

            }
        }
        if (numberOfTracesWithActs <= 0) {
            return false;
        }
        Set<Double> fitnessPerContainedAct = new HashSet<Double>();
        for (String a : allActsInCandidate) {
            double containedIn = numOfTracesContainingAct.getOrDefault(a, 0);
            if (containedIn > 0) {
                double containedInFitting = numOfFittingTracesContainingAct.getOrDefault(a, 0);
                fitnessPerContainedAct.add(containedInFitting / containedIn);
            }
        }
        return ((double) fittingWithActs) / numberOfTracesWithActs >= t && Collections.min(fitnessPerContainedAct) >= t;
    }

    @Override
    public Set<Pair<Set<String>, Set<String>>> pruneCandidates(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {
        double t = getOptionValueByID("min_fitting_traces");
        return candidates.stream().filter(c -> getCandidateScore(logProcessor, c, t)).collect(Collectors.toSet());
    }
}
