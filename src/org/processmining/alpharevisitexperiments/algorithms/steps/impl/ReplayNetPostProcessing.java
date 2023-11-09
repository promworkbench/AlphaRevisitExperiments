package org.processmining.alpharevisitexperiments.algorithms.steps.impl;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.algorithms.steps.PostProcessingPetriNetStep;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.options.ExperimentOptionCandidateChangeIndicator;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.alpharevisitexperiments.util.ReplayProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.processmining.alpharevisitexperiments.util.ReplayProcessor.FREQUENT_VARIANT_OPTION_ID;

public class ReplayNetPostProcessing extends PostProcessingPetriNetStep {

    public final static String NAME = "Replay & Remove problematic places";
    public final ExperimentOption[] options = {
            new ExperimentOption<>(Double.class, FREQUENT_VARIANT_OPTION_ID, "Local Place Replay Fitness Theshold (r)", 0.0, 0.0, 1.0, "Replays Petri net after construction and filters out places if their local fitness is below the specified threshold.", ExperimentOptionCandidateChangeIndicator.HIGH_DECREASES_CANDIDATES),
    };

    public ReplayNetPostProcessing() {
        super(NAME);
        setOptions(options);
    }

    public ReplayNetPostProcessing(double replay_local_fitness_required) {
        super(NAME);
        options[0].setValue(replay_local_fitness_required);
        setOptions(options);
    }

    @Override
    public AcceptingPetriNet processPetriNet(UIPluginContext context, LogProcessor logProcessor, AcceptingPetriNet net) {
        Map<Place, Double> replayRes = ReplayProcessor.replayWithAllTraces(net, logProcessor.getVariants());
        double replayLocalFitnessRequired = this.getOptionValueByID(FREQUENT_VARIANT_OPTION_ID);
        Set<Place> replayResProblematicPlaces = replayRes.keySet().stream().filter((k) -> replayRes.getOrDefault(k, 0.0) < replayLocalFitnessRequired).collect(Collectors.toSet());
        for (Place p : replayResProblematicPlaces) {
            for (Marking m : net.getFinalMarkings()) {
                m.remove(p);
            }
            net.getInitialMarking().remove(p);
            net.getNet().removePlace(p);
        }
        return net;
    }
}
