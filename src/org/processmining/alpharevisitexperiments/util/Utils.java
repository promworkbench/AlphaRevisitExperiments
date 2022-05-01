package org.processmining.alpharevisitexperiments.util;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class Utils {


    public static Place getPlaceWithLabel(Petrinet net, String label){
        Place p = net.getPlaces().stream().filter((place -> label.equals(place.getLabel()))).findAny().get();
        return p;
    }

    public static Transition getTransitionWithLabel(Petrinet net, String label){
        Transition t = net.getTransitions().stream().filter((transition -> label.equals(transition.getLabel()))).findAny().get();
        return t;
    }
}
