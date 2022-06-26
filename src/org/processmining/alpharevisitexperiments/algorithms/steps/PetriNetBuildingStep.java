package org.processmining.alpharevisitexperiments.algorithms.steps;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;

import java.util.Map;
import java.util.Set;

public abstract class PetriNetBuildingStep extends Step {
    protected PetriNetBuildingStep(String name) {
        super(name);
    }

    public abstract AcceptingPetriNet buildPetriNet(UIPluginContext context, LogProcessor logProcessor, Set<Pair<Set<String>, Set<String>>> candidates);

    public abstract Map<Pair<Set<String>, Set<String>>, Place> getCandidatePlaceMap();

    @Override
    public String getTypeName() {
        return "Constructing Petri net";
    }
}
