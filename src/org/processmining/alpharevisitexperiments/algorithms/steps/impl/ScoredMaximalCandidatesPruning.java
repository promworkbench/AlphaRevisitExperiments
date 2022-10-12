package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.alpharevisitexperiments.algorithms.steps.CandidatePruningStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ScoredMaximalCandidatesPruning extends CandidatePruningStep {

    public final static String NAME = "Fitness-aware Maximal Candidate Selection";
    final ExperimentOption[] options = {
    };

    public ScoredMaximalCandidatesPruning() {
        super(NAME);
        setOptions(options);
    }

    public static double calculateCandidateScore(LogProcessor logProcessor, Pair<Set<String>, Set<String>> candidate) {
        int score = 0;
        for (String a : candidate.getFirst()) {
            for (String b : candidate.getSecond()) {
//                score += logProcessor.getDfg().getOrDefault(new Pair<>(a,b),0);
                score += ((double) logProcessor.getDfg().getOrDefault(new Pair<>(a, b), 0) /
                        logProcessor.getActivityOccurrence(a));
                score -= ((double) logProcessor.getDfg().getOrDefault(new Pair<>(b, a), 0) /
                        logProcessor.getActivityOccurrence(b));
            }
        }

        return score;
    }

    private static double getCandidateScore(LogProcessor logProcessor, Pair<Set<String>, Set<String>> candidate) {
        int underfedTraces = 0;
        int overfedTraces = 0;
        int fittingTraces = 0;
        for (String variant : logProcessor.getVariants().keySet()) {
            int weight = logProcessor.getVariants().getOrDefault(variant, 0);
            int tokenCount = 0;
            String[] trace = variant.split(",");
            for (String activity : trace) {
                if (candidate.getFirst().contains(activity)) {
                    tokenCount++;
                }
                if (candidate.getSecond().contains(activity)) {
                    tokenCount--;
                }

                if (tokenCount < 0) {
                    underfedTraces++;
                    break;
                }
            }
            if (tokenCount < 0) {
                underfedTraces += weight;
            } else if (tokenCount > 0) {
                overfedTraces += weight;
            } else {
                fittingTraces += weight;
            }
        }
        return ((double) fittingTraces) / (logProcessor.getNumberOfCases());
    }

    @Override
    public Set<Pair<Set<String>, Set<String>>> pruneCandidates(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates) {

        Set<Pair<Set<String>, Set<String>>> sel = new HashSet<>(candidates);
        for (Pair<Set<String>, Set<String>> c : candidates) {
            Set<Pair<Set<String>, Set<String>>> containedBy = candidates.stream().filter(e -> e != c && e.getFirst().containsAll(c.getFirst()) && e.getSecond().containsAll(c.getSecond())).collect(Collectors.toSet());
            if (containedBy.size() > 0) {
                double candidateScore = getCandidateScore(logProcessor, c);
                boolean containedByWithBetterScoreExists = containedBy.stream().anyMatch(e -> getCandidateScore(logProcessor, e) >= candidateScore);
                if (containedByWithBetterScoreExists) {
                    sel.remove(c);
                } else {
                    sel.removeAll(containedBy);
                }
            }
        }
        return sel;
    }
}
