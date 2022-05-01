package org.processmining.alpharevisitexperiments.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.alpharevisitexperiments.util.Utils;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

import java.util.*;
import java.util.stream.Collectors;

public class AlphaOneDotOne extends AlgorithmExperiment {

    final static ExperimentOption[] options = {
    };

    public AlphaOneDotOne() {
        super("Alpha 1.1",options);
    }

    @Override
    public AcceptingPetriNet execute(UIPluginContext context, XLog log) {
        Petrinet net = PetrinetFactory.newPetrinet("Petri net");
        Marking initialMarking = new Marking();
        Marking finalMarking = new Marking();

        LogProcessor logProcessor = new LogProcessor(log);
        HashSet<String> activities = logProcessor.getActivities();

        HashMap<Pair<String, String>, Integer> dfg = logProcessor.getDfg();
        Set<Pair<String,String>> dfRelation = dfg.keySet().stream().filter((e) -> dfg.get(e) > 0).collect(Collectors.toSet());
        System.out.println(dfRelation);
        HashMap<String, Integer> variants = logProcessor.getVariants();
        Set<Pair<HashSet<String>, HashSet<String>>> initialCnd = new HashSet<>();
        for (Pair<String, String> el : dfRelation) {
            if(!dfRelation.contains(new Pair<String,String>(el.getSecond(),el.getFirst()))){
                initialCnd.add(new Pair<HashSet<String>,HashSet<String>>(new HashSet<>(Collections.singleton(el.getFirst())),new HashSet<>(Collections.singleton(el.getSecond()))));
            }
        }

        System.out.println("InitialCnd: " + initialCnd);

        ArrayList<Pair<HashSet<String>,HashSet<String>>> cnd = new ArrayList<>(initialCnd);
        for (int i = 0; i < cnd.size(); i++) {
            for (int j = i+1; j < cnd.size(); j++) {
                if(cnd.get(i) != cnd.get(j)){
                    Set<String> a1 = cnd.get(i).getFirst(), a2 = cnd.get(i).getSecond(), b1 = cnd.get(j).getFirst(), b2 = cnd.get(j).getSecond();
                    if(a1.containsAll(b1) || a2.containsAll(b2)){
                        if(isNoDFRelationBetween(dfRelation,a1,b1) && isNoDFRelationBetween(dfRelation,a2,b2)){
                            HashSet<String> first = new HashSet<>();
                            first.addAll(a1);
                            first.addAll(b1);
                            HashSet<String> second = new HashSet<>();
                            second.addAll(a2);
                            second.addAll(b2);
                            Pair<HashSet<String>,HashSet<String>> newCandidate = new Pair<>(first,second);
                            if(!cnd.contains(newCandidate)){
                                cnd.add(newCandidate);
                            }
                        }

                    }
                }
            }
        }
        ArrayList<Pair<HashSet<String>,HashSet<String>>> sel = new ArrayList<>();
        for (Pair<HashSet<String>, HashSet<String>> c : cnd) {
            boolean isMaximal = cnd.stream().noneMatch(e -> e != c && e.getFirst().containsAll(c.getFirst()) && e.getSecond().containsAll(c.getSecond()));
            if(isMaximal){
                sel.add(c);
            }
        }
        for (Pair<HashSet<String>, HashSet<String>> e : sel) {
            Place p = net.addPlace(e.toString());
            if(e.getFirst().contains("__START")){
                initialMarking.add(p,1);
            }else if(e.getSecond().contains("__END")){
                finalMarking.add(p,1);
            }
        }
        for (String activity : activities) {
            net.addTransition(activity);
        }

        for (Pair<HashSet<String>, HashSet<String>> e : sel) {
            for (String a : e.getFirst()) {
                if(activities.contains(a)){
                    Transition from = Utils.getTransitionWithLabel(net,a);
                    Place to = Utils.getPlaceWithLabel(net,e.toString());
                    net.addArc(from,to);
                }
            }
            for (String a : e.getSecond()) {
                if(activities.contains(a)){
                    Transition to = Utils.getTransitionWithLabel(net,a);
                    Place from = Utils.getPlaceWithLabel(net,e.toString());
                    net.addArc(from,to);
                }
            }
        }

        System.out.println("Pairs: " + cnd);
        return AcceptingPetriNetFactory.createAcceptingPetriNet(net,initialMarking,finalMarking);
    }


    private static boolean isNoDFRelationBetween(Set<Pair<String,String>> dfRelation, Collection<String> as, Collection<String> bs ){
        for(String a : as){
            for(String b : bs){
                // Condition a1 =/> a2 would not be satisfied, try next pair...
                if(dfRelation.contains(new Pair<>(a,b))){
                    return false;
                }
            }
        }
        return true;
    }

}

