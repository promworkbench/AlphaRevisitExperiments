package org.processmining.alpharevisitexperiments.ui;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
import org.processmining.alpharevisitexperiments.algorithms.AlgorithmExperiment;
import org.processmining.alpharevisitexperiments.dialogs.OptionsUI;
import org.processmining.alpharevisitexperiments.plugins.ExperimentRunner;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AlphaRevisitExperimentsVisualizer extends JPanel {
    public static final int SIDE_PANEL_WIDTH = 550;

    AcceptingPetriNet net;

    XLog log;

    LogProcessor logProcessor;

    AlgorithmExperiment usedAlgo;

    public AlphaRevisitExperimentsVisualizer(UIPluginContext context, XLog log) {
        this.log = log;
        logProcessor = new LogProcessor(log);

        setLayout(new BorderLayout());

        JPanel sidePanel = new JPanel();
        sidePanel.setOpaque(false);
        sidePanel.setLayout(new BorderLayout());
        sidePanel.setMaximumSize(new Dimension(SIDE_PANEL_WIDTH, 10000));
        sidePanel.setMinimumSize(new Dimension(SIDE_PANEL_WIDTH, 100));
        sidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, 10000));
        add(sidePanel, BorderLayout.LINE_END);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel netVis = new JPanel();
        netVis.setLayout(new BoxLayout(netVis, BoxLayout.Y_AXIS));
        netVis.setAlignmentX(CENTER_ALIGNMENT);

        JButton saveNetButton = new JButton("Save accepting Petri net in ProM");
        saveNetButton.setAlignmentX(CENTER_ALIGNMENT);

        center.add(netVis);
        center.add(saveNetButton);
        add(center, BorderLayout.CENTER);
        JEditorPane debugText = new JEditorPane("text/html", "");
        debugText.setEditable(false);
        debugText.setFont(new Font("Dialog", Font.PLAIN, 16));
        debugText.setAutoscrolls(false);
        JScrollPane debugPanel = new JScrollPane(debugText);
        debugPanel.setMinimumSize(new Dimension(SIDE_PANEL_WIDTH, 400));
        debugPanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, 400));
        sidePanel.add(debugPanel, BorderLayout.NORTH);

        OptionsUI optionsUI = new OptionsUI(true);
        sidePanel.add(optionsUI, BorderLayout.CENTER);

        JButton goButton = new JButton("Mine with selected options");
        goButton.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, 50));
        sidePanel.add(goButton, BorderLayout.SOUTH);

//      Execute initially selected algorithm
        this.usedAlgo = optionsUI.getSelectedExperiment();
        ExperimentRunner startRunner = new ExperimentRunner(this.usedAlgo, context, this.log);
        context.getGlobalContext().getMainPluginContext().getExecutor().execute(startRunner);
//        context.getExecutor().execute(startRunner);
        try {
            AcceptingPetriNet acceptingPetriNet = startRunner.get();
            this.net = acceptingPetriNet;
        } catch (Exception exception) {
            System.err.println("AlphaRevisit Runner interrupted:" + exception.toString());
            context.getFutureResult(0).cancel(true);
        }

        debugText.setText(getDebugText());
        debugText.setCaretPosition(0);

//      Add the initial accepting Petri net visualization
        netVis.add(VisualizeAcceptingPetriNetPlugin.visualize(context, net));
//      Button to save the current accepting Petri net to ProM
        saveNetButton.addActionListener(e -> {
            String logName = XConceptExtension.instance().extractName(this.log);
            context.getProvidedObjectManager().createProvidedObject("Accepting Petri net of " + logName + " mined with " + this.usedAlgo.name, net,
                    AcceptingPetriNet.class, context);
            context.getGlobalContext().getResourceManager().getResourceForInstance(net)
                    .setFavorite(true);
        });

//      Execute selected plugin with given options and display the resulting net
        goButton.addActionListener(e -> {
            this.usedAlgo = optionsUI.getSelectedExperiment();
            context.log("Starting " + this.usedAlgo.name + "...");
            context.getProgress().setIndeterminate(true);
            if (this.usedAlgo != null) {
                System.out.println(this.usedAlgo.name + " was selected");
                ExperimentRunner runner = new ExperimentRunner(this.usedAlgo, context, this.log);
                long startTime = System.nanoTime();
                context.getExecutor().execute(runner);
                try {
                    AcceptingPetriNet acceptingPetriNet = runner.get();
                    long duration = System.nanoTime() - startTime;
                    System.out.println("Algorithm finished; Duration: " + duration);
                    this.net = acceptingPetriNet;
                    netVis.removeAll();
                    netVis.revalidate();
                    netVis.repaint();
                    JComponent newNetVis = VisualizeAcceptingPetriNetPlugin.visualize(context, this.net);
                    netVis.add(newNetVis);
                    debugText.setText(getDebugText());
                    debugText.setCaretPosition(0);
                    revalidate();
                    repaint();
                } catch (Exception exception) {
                    System.err.println("AlphaRevisit Runner interrupted:" + exception.toString());
                    context.getFutureResult(0).cancel(true);
                }
            }
        });
    }


    @Plugin(name = "Visualize Alpha Revisit Experiments", parameterLabels = {},
            returnLabels = {"Visualization"},
            returnTypes = {JComponent.class},
            userAccessible = true, help = "Try out some experimental algorithms and their options.")
    @UITopiaVariant(affiliation = "RWTH Aachen University", author = "Aaron Küsters, Wil van der Aalst", email = "aaron.kuesters@rwth-aachen.de")
    @Visualizer
    public static JComponent visualize(UIPluginContext context, AlphaRevisitExperimentsVisualizer vis) {
        return vis;
    }


    private String getDebugText() {
        StringBuilder newDebugText = new StringBuilder("<h1>" + usedAlgo.name + "</h1><h2>Petri net</h2><b>#Places: " + this.net.getNet().getPlaces().size() +
                "</b><br/><b>#Arcs: " + this.net.getNet().getEdges().size() + "</b><br/>");
        newDebugText.append("<h2>Log</h2><b>DFG Relations:</b><br/>");
        for (Map.Entry<Pair<String, String>, Integer> entry : logProcessor.getDfg().entrySet()) {
            newDebugText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(entry.getKey().getFirst()).append(" → ").append(entry.getKey().getSecond()).append(": ").append(entry.getValue()).append("<br/>");
        }
        return newDebugText.toString();
    }
}
