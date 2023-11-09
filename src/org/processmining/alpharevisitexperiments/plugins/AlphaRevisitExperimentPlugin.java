package org.processmining.alpharevisitexperiments.plugins;

import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alpharevisitexperiments.algorithms.AlgorithmExperiment;
import org.processmining.alpharevisitexperiments.dialogs.OptionsUI;
import org.processmining.alpharevisitexperiments.ui.AlphaRevisitExperimentsVisualizer;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;

public class AlphaRevisitExperimentPlugin {
    @Plugin(name = "Alpha Revisit Experiments (Alpha+++)", parameterLabels = {},
            returnLabels = {"Alpha Revisit Interactive View"},
            returnTypes = {AlphaRevisitExperimentsVisualizer.class},
            userAccessible = true, help = "Experiment with interchangeable and modifiable algorithm steps based on the Alpha or Alpha+++ algorithm.")
    @UITopiaVariant(affiliation = "RWTH Aachen University", author = "Aaron Küsters, Wil van der Aalst", email = "kuesters@pads.rwth-aachen.de")
    public static AlphaRevisitExperimentsVisualizer runAlphaRevisitVisualPlugin(UIPluginContext context, XLog log) {
        return new AlphaRevisitExperimentsVisualizer(context, log);
    }


    private static AlgorithmExperiment getExperimentOption(UIPluginContext context) {
        OptionsUI dialog = new OptionsUI();
        TaskListener.InteractionResult result = context.showWizard("Choose an algorithm configuration", true, true, dialog);

        if (result != TaskListener.InteractionResult.FINISHED) {
            context.getFutureResult(0).cancel(true);
            return null;
        }

        return dialog.getSelectedExperiment();
    }

}
