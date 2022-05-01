package org.processmining.alpharevisitexperiments.plugins;

import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.algorithms.AlgorithmExperiment;
import org.processmining.alpharevisitexperiments.dialogs.OptionsUI;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;

public class AlphaRevisitExperimentPlugin{
    @Plugin(name = "Alpha Revisit Experiments", parameterLabels = {},
            returnLabels = {"Accepting Petrinet"},
            returnTypes = {AcceptingPetriNet.class},
            userAccessible = true, help = "Try out some experimental algorithms and their options.")
    @UITopiaVariant(affiliation = "RWTH Aachen University", author = "Aaron Küsters, Wil van der Aalst", email = "aaron.kuesters@rwth-aachen.de")
    public static AcceptingPetriNet helloWorld(UIPluginContext context, XLog log) {
        AlgorithmExperiment algo = getExperimentOption(context);
        if (algo != null) {
            System.out.println(algo.name + " was selected");
            AcceptingPetriNet acceptingPetriNet = algo.execute(context, log);
            return acceptingPetriNet;
        }else{
            System.out.println("No algo was selected");
            return null;
        }
    }

    private static AlgorithmExperiment getExperimentOption(UIPluginContext context) {
        OptionsUI dialog = new OptionsUI();
        TaskListener.InteractionResult result = context.showWizard("Choose an experiment", true, true, dialog);

        if (result != TaskListener.InteractionResult.FINISHED) {
            context.getFutureResult(0).cancel(false);
            return null;
        }

        return dialog.getSelectedExperiment();
    }

}
