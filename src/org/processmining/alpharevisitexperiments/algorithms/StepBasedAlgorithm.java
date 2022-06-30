package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.algorithms.steps.*;
import org.processmining.alpharevisitexperiments.algorithms.steps.impl.*;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class StepBasedAlgorithm extends AlgorithmExperiment {
    final static ExperimentOption[] NO_OPTIONS = {
    };
    public CandidateBuildingStep buildingCandidatesStep = new AlphaThreeDotZeroCandidateBuilding();
    public CandidatePruningStep[] pruningCandidatesSteps = new CandidatePruningStep[]{new BalanceBasedCandidatePruning(), new IdentityCandidatePruning(), new MaximalCandidatesPruning()};
    public PetriNetBuildingStep buildingNetStep = new AlphaPetriNetBuilding();
    public PostProcessingPetriNetStep[] postProcessingPetriNetSteps = new PostProcessingPetriNetStep[]{new IdentityNetProcessing()};

    public HashMap<Integer, Integer> candidateCountAfterStep = new HashMap<>();


    public StepBasedAlgorithm() {
        super("Alpha-Based Algorithm with Modifiable Steps", NO_OPTIONS);
    }

    @Override
    public AcceptingPetriNet execute(UIPluginContext context, LogProcessor logProcessor) {
        int stepNum = 0;
        Set<Pair<Set<String>, Set<String>>> candidates = buildingCandidatesStep.buildCandidates(context, logProcessor);
        candidateCountAfterStep.put(stepNum++, candidates.size());
        for (CandidatePruningStep pruningStep : pruningCandidatesSteps) {
            candidates = pruningStep.pruneCandidates(context, logProcessor, candidates);
            candidateCountAfterStep.put(stepNum++, candidates.size());
        }
        AcceptingPetriNet net = buildingNetStep.buildPetriNet(context, logProcessor, candidates);

        candidateCountAfterStep.put(stepNum++, net.getNet().getPlaces().size());

        for (PostProcessingPetriNetStep postProcessingPetriNetStep : postProcessingPetriNetSteps) {
            net = postProcessingPetriNetStep.processPetriNet(context, logProcessor, net);
            candidateCountAfterStep.put(stepNum++, net.getNet().getPlaces().size());
        }

        return net;
    }

    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        LogProcessor logProcessor = new LogProcessor(log);
        return this.execute(context, logProcessor);
    }

    public Step[] getAllSteps() {
        ArrayList<Step> allSteps = new ArrayList<>();
        allSteps.add(this.buildingCandidatesStep);
        for (Step step : this.pruningCandidatesSteps) {
            allSteps.add(step);
        }
        allSteps.add(this.buildingNetStep);
        for (Step step : this.postProcessingPetriNetSteps) {
            allSteps.add(step);
        }
        return allSteps.toArray(new Step[]{});
    }
}
