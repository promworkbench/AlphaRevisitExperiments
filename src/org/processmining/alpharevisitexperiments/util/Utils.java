package org.processmining.alpharevisitexperiments.util;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {


    public static final String[] STANDARD_STEP_LABELS = {"L", "L", "L (DFG)", "Cnd₁(L)", "Cnd₂(L)", "Cnd₃(L)", "Sel(L)", "Places(L)", "Final_Places(L)"};

    // These colors are designed to be easily distinguishable
    // See also https://eleanormaclure.files.wordpress.com/2011/03/colour-coding.pdf and https://stackoverflow.com/questions/470690/how-to-automatically-generate-n-distinct-colors
    public static final Color[] KELLYS_COLORS = {
            Color.decode("0xFFB300"),    // Vivid Yellow
            Color.decode("0x803E75"),    // Strong Purple
            Color.decode("0xFF6800"),    // Vivid Orange
            Color.decode("0xA6BDD7"),    // Very Light Blue
            Color.decode("0xC10020"),    // Vivid Red
            Color.decode("0xCEA262"),    // Grayish Yellow
            Color.decode("0x817066"),    // Medium Gray
            //  May be hard to distinguish if color vision is impaired:
            Color.decode("0x007D34"),    // Vivid Green
            Color.decode("0xF6768E"),    // Strong Purplish Pink
            Color.decode("0x00538A"),    // Strong Blue
            Color.decode("0xFF7A5C"),    // Strong Yellowish Pink
            Color.decode("0x53377A"),    // Strong Violet
            Color.decode("0xFF8E00"),    // Vivid Orange Yellow
            Color.decode("0xB32851"),    // Strong Purplish Red
            Color.decode("0xF4C800"),    // Vivid Greenish Yellow
            Color.decode("0x7F180D"),    // Strong Reddish Brown
            Color.decode("0x93AA00"),    // Vivid Yellowish Green
            Color.decode("0x593315"),    // Deep Yellowish Brown
            Color.decode("0xF13A13"),    // Vivid Reddish Orange
            Color.decode("0x232C16"),    // Dark Olive Green
    };


    public static Place getPlaceWithLabel(Petrinet net, String label) {
        Place p = net.getPlaces().stream().filter((place -> label.equals(place.getLabel()))).findAny().get();
        return p;
    }

    public static Transition getTransitionWithLabel(Petrinet net, String label) {
        Optional<Transition> t = net.getTransitions().stream().filter((transition -> label.equals(transition.getLabel()))).findAny();
        if (t.isPresent()) {
            return t.get();
        } else {
            return null;
        }
    }

    public static boolean isNoDFRelationBetweenSymmetric(Set<Pair<String, String>> dfRelation, Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (dfRelation.contains(new Pair<>(a, b)) || dfRelation.contains(new Pair<>(b, a))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNoDFRelationBetweenAsymmetric(Set<Pair<String, String>> dfRelation, Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (dfRelation.contains(new Pair<>(a, b))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean areAllRelationsDFBetweenStrict(Set<Pair<String, String>> dfRelation, Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (!dfRelation.contains(new Pair<>(a, b)) || dfRelation.contains(new Pair<>(b, a))) {
                    return false;
                }
            }
        }
        return true;
    }


    public static boolean isFeasibleForA30(Set<Pair<String, String>> dfRelation, Collection<String> as, Collection<String> bs) {
        if (!areAllRelationsDFBetweenNonStrict(dfRelation, as, bs)) {
            return false;
        }
//            HashSet<String> asWithoutBs = new HashSet<>(as);
//            asWithoutBs.removeAll(bs);
//            HashSet<String> bsWithoutAs = new HashSet<>(bs);
//            bsWithoutAs.removeAll(as);

        Set<String> newAs = new HashSet<>(as);
        Set<String> newBs = new HashSet<>(bs);

//          Check if (3) is satisfiable
        for (String a1 : as) {
            for (String a2 : as) {
                if (!bs.contains(a2) && dfRelation.contains(new Pair<>(a1, a2))) {
                    newBs.add(a2);
                }
            }
        }
//          Check if (4) is satisfiable
        for (String a1 : bs) {
            if (!as.contains(a1)) {
                for (String a2 : bs) {
                    if (dfRelation.contains(new Pair<>(a1, a2))) {
                        newAs.add(a1);
                    }
                }
            }
        }
//
//        as.addAll(newAs);
//        bs.addAll(newBs);
        //        Check (1)
        if (areAllRelationsDFBetweenNonStrict(dfRelation, newAs, newBs)) {
            return true;
        } else {
            return false;
        }

    }


    public static boolean areAllRelationsDFBetweenNonStrict(Set<Pair<String, String>> dfRelation, Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (!dfRelation.contains(new Pair<>(a, b)) && a.indexOf("TAU(") == -1 && b.indexOf("TAU(") == -1) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean areNotAllRelationsDF(Set<Pair<String, String>> dfRelation, Collection<String> as, Collection<String> bs) {
        for (String a : as) {
            for (String b : bs) {
                if (!dfRelation.contains(new Pair<>(b, a))) {
                    return true;
                }
            }
        }
        return false;
    }


    public static String stringifyCandidates(Set<Pair<Set<String>, Set<String>>> cndSet) {
        return cndSet.stream().map(c -> stringifyCandidate(c)).sorted().collect(Collectors.joining("\n"));
    }

    public static String stringifyCandidate(Pair<Set<String>, Set<String>> cnd) {
        return "(" + sortedStringifySet(cnd.getFirst()) + ", \t" + sortedStringifySet(cnd.getSecond()) + ")";
    }

    public static String sortedStringifySet(Set<String> set) {
        return "{" + set.stream().sorted().collect(Collectors.joining(", ")) + "}";
    }

}
