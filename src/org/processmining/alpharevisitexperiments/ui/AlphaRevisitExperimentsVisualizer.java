package org.processmining.alpharevisitexperiments.ui;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.api.model.ViewType;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.alpharevisitexperiments.algorithms.AlgorithmExperiment;
import org.processmining.alpharevisitexperiments.algorithms.StepBasedAlgorithm;
import org.processmining.alpharevisitexperiments.algorithms.steps.impl.StandardAlphaPetriNetBuilding;
import org.processmining.alpharevisitexperiments.dialogs.OptionsUI;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.alpharevisitexperiments.util.Utils;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.shapes.Polygon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static org.processmining.alpharevisitexperiments.util.LogProcessor.END_ACTIVITY;
import static org.processmining.alpharevisitexperiments.util.LogProcessor.START_ACTIVITY;

public class AlphaRevisitExperimentsVisualizer extends JPanel {
    public static final int SIDE_PANEL_WIDTH = 550;

    AcceptingPetriNet net;

    XLog log;

    String logName;

    LogProcessor logProcessor;

    AlgorithmExperiment usedAlgo;

    List<ViewType> viewTypes;

    int selectedViewType = 0;

    View view;

    ProMResource<?> netRes;

    JPanel netVis;

    UIPluginContext context;

    ExperimentGUIRunner runner;

    JButton saveNetButton;

    JEditorPane debugText;

    JButton goButton;

    JProgressBar progressBar;

    boolean useColors = false;
    boolean useColorsScaled = false;

    public AlphaRevisitExperimentsVisualizer(UIPluginContext context, XLog log) {
        this.log = log;
        this.logName = XConceptExtension.instance().extractName(this.log);
        this.logProcessor = new LogProcessor(log);
        this.context = context;

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

        netVis = new JPanel();
        netVis.setLayout(new BoxLayout(netVis, BoxLayout.Y_AXIS));
        netVis.setPreferredSize(new Dimension(10000, 10000));
        netVis.setAlignmentX(CENTER_ALIGNMENT);

        saveNetButton = new JButton("Save accepting Petri net in ProM");
        saveNetButton.setAlignmentX(CENTER_ALIGNMENT);
        JComboBox viewSelector = SlickerFactory.instance().createComboBox(new String[]{});
        center.add(viewSelector);
        center.add(netVis);
        center.add(saveNetButton);
        add(center, BorderLayout.CENTER);
        debugText = new JEditorPane("text/html", "");
        debugText.setEditable(false);
        debugText.setFont(new Font("Dialog", Font.PLAIN, 16));
        debugText.setAutoscrolls(false);
        JPanel debugPanel = new JPanel();
        debugPanel.setLayout(new BorderLayout());
        JScrollPane debugScrollPanel = new JScrollPane(debugText);
        debugPanel.add(debugScrollPanel, BorderLayout.CENTER);

        debugPanel.setMinimumSize(new Dimension(SIDE_PANEL_WIDTH, 400));
        debugPanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, 400));
        sidePanel.add(debugPanel, BorderLayout.NORTH);

        OptionsUI optionsUI = new OptionsUI(true);

        JPanel visOptionsPanel = new JPanel();
        visOptionsPanel.setLayout(new BoxLayout(visOptionsPanel, BoxLayout.Y_AXIS));
        JCheckBox useColorsCheckbox = new JCheckBox("Color transitions to highlight DF relations that are not covered", false);
        visOptionsPanel.add(useColorsCheckbox);

        JCheckBox useColorsScaledCheckbox = new JCheckBox("Scale color opacity based on DF frequency", false);
        useColorsScaledCheckbox.setEnabled(this.useColors);
        useColorsScaledCheckbox.addActionListener(e -> {
            this.useColorsScaled = useColorsScaledCheckbox.isSelected();
        });
        visOptionsPanel.add(useColorsScaledCheckbox);

        useColorsCheckbox.addActionListener(e -> {
            this.useColors = useColorsCheckbox.isSelected();
            useColorsScaledCheckbox.setEnabled(useColorsCheckbox.isSelected());
        });
        debugPanel.add(visOptionsPanel, BorderLayout.SOUTH);
        sidePanel.add(optionsUI, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        goButton = new JButton("Mine with selected options");
        goButton.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, 50));
        goButton.add(progressBar);
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        bottomPanel.add(goButton, BorderLayout.CENTER);
        sidePanel.add(bottomPanel, BorderLayout.SOUTH);

        debugText.setText(getDebugText());
        debugText.setCaretPosition(0);

        Petrinet sampleNet = PetrinetFactory.newPetrinet("Sample Petri net");
        AcceptingPetriNet sampleANet = new AcceptingPetriNetImpl(sampleNet);
        final ProMResource<?> sampleANetRes = createProMResourceFromAPN(context, sampleANet, "Sample net");
        viewTypes = context.getGlobalContext().getViewManager().getViewTypes(sampleANetRes);
        sampleANetRes.destroy();

        viewSelector.removeAllItems();
        for (ViewType viewType : viewTypes) {
            viewSelector.addItem(viewType);
        }
        viewSelector.addActionListener(e -> {
            if (viewSelector.getSelectedIndex() > -1 && viewSelector.getSelectedIndex() != this.selectedViewType) {
                this.selectedViewType = viewSelector.getSelectedIndex();
                createNetVis();
            }
        });
        saveNetButton.setEnabled(false);
        saveNetButton.addActionListener(e -> {
            if (this.netRes != null && !this.netRes.isDestroyed()) {
                debugText.setText(getDebugText());
                debugText.setCaretPosition(0);
                createNetVis();
                this.netRes.setFavorite(true);
                //           Adding a dummy net and removing it again fixes a weird glitch, where a new (favorite) APN does only appear
                //            when switching views in the object menu
                Petrinet newSampleNet = PetrinetFactory.newPetrinet("Sample Petri net");
                AcceptingPetriNet newSampleANet = new AcceptingPetriNetImpl(newSampleNet);
                final ProMResource<?> newSampleANetRes = createProMResourceFromAPN(context, newSampleANet, "Sample net 2");
                newSampleANetRes.destroy();
            } else {
                JOptionPane.showMessageDialog(null, "The mined Petri net is not available. Please mine a new model.");
            }
        });

//      Execute selected plugin with given options and display the resulting net
        goButton.addActionListener(e -> {
            this.usedAlgo = optionsUI.getSelectedExperiment();
            context.log("Starting " + this.usedAlgo.name + "...");
            if (this.usedAlgo != null) {
                LogProcessor logProcessorToUse = new LogProcessor(log);
                System.out.println(this.usedAlgo.name + " was selected");
                runner = new ExperimentGUIRunner(this.usedAlgo, context, logProcessorToUse);
                progressBar.setVisible(true);
                goButton.setEnabled(false);
                context.getExecutor().execute(runner);
            }
        });
    }

    @Plugin(name = "Visualize Alpha Revisit Experiments", parameterLabels = {}, returnLabels = {"Visualization"}, returnTypes = {JComponent.class}, userAccessible = true, help = "Try out some experimental algorithms and their options.")
    @UITopiaVariant(affiliation = "RWTH Aachen University", author = "Aaron Küsters, Wil van der Aalst", email = "aaron.kuesters@rwth-aachen.de")
    @Visualizer
    public static JComponent visualize(UIPluginContext context, AlphaRevisitExperimentsVisualizer vis) {
        return vis;
    }

    private void createNetVis() {
        if (this.netRes != null && !this.netRes.isDestroyed()) {
            netVis.removeAll();
            netVis.revalidate();
            netVis.repaint();
            this.view = viewTypes.get(selectedViewType).createView(this.netRes);
            JComponent newNetVis = this.view.getViewComponent();
            netVis.add(newNetVis);
        } else {
            JOptionPane.showMessageDialog(null, "The mined Petri net is not available. Please mine a new model.");
        }
    }

    private ProMResource createProMResourceFromAPN(UIPluginContext context, AcceptingPetriNet net, String name) {
        context.getProvidedObjectManager().createProvidedObject(name, net, AcceptingPetriNet.class, context);
        final ProMResource<?> netRes = context.getGlobalContext().getResourceManager().getResourceForInstance(net);
        return netRes;
    }

    private String getDebugText() {
        StringBuilder newDebugText = new StringBuilder();
        if (net != null) {
            newDebugText.append("<h2>Petri net</h2><b>#Places: ").append(this.net.getNet().getPlaces().size()).append("</b><br/><b>#Arcs: ").append(this.net.getNet().getEdges().size()).append("</b><br/><b>#Transitions: ").append(this.net.getNet().getTransitions().size()).append("<br/>");
        }
        newDebugText.append("<h2>Log</h2><b>Activities:</b><br/>");
        String[] activities = logProcessor.getActivityOccurrences().keySet().toArray(new String[0]);
        Arrays.sort(activities, Comparator.comparingInt(a -> -this.logProcessor.getActivityOccurrence(a)));
        newDebugText.append("<table style=\"padding-left: 20px;\">\n" + "  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" + "    <th>Activity</th>\n" + "    <th>Count</th>\n" + "  </tr>\n");
        for (String act : activities) {
            int count = this.logProcessor.getActivityOccurrence(act);
            newDebugText.append("  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" + "    <td>").append(act).append("</td>\n").append("    <td>").append(count).append("</td>\n").append("  </tr>\n");
//            newDebugText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(act).append(": ").append(count).append("<br/>");
        }
        newDebugText.append("</table>\n");


        newDebugText.append("<br/><b>DF Relations:</b><br/>");
        newDebugText.append("<table style=\"padding-left: 20px;\">\n" + "  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" + "    <th>Relation</th>\n" + "    <th>Count</th>\n" + "  </tr>\n");

        Pair<String, String>[] keys = logProcessor.getDfg().keySet().toArray(new Pair[0]);
        Arrays.sort(keys, Comparator.comparingInt(e -> -this.logProcessor.getDfg().getOrDefault(e, 0)));
        for (Pair<String, String> key : keys) {
            int value = this.logProcessor.getDfg().getOrDefault(key, 0);
            newDebugText.append("  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" + "    <td>").append(key.getFirst()).append(" → ").append(key.getSecond()).append("</td>\n").append("    <td>").append(value).append("</td>\n").append("  </tr>\n");
//            newDebugText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(key.getFirst()).append(" → ").append(key.getSecond()).append(": ").append(value).append("<br/>");
        }
        newDebugText.append("</table>\n");
        if (usedAlgo != null) {
            newDebugText.append("<h2>").append("Candidates / Places Count").append("</h2>");
            newDebugText.append("<table style=\"padding-left: 20px;\">\n" + "  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" + "    <th>Set</th>\n" + "    <th>Size</th>\n" + "    <th>Change</th>\n" + "  </tr>\n");
            if (usedAlgo instanceof StepBasedAlgorithm) {
                StepBasedAlgorithm stepAlgo = (StepBasedAlgorithm) usedAlgo;
                stepAlgo.candidateCountAfterStep.keySet().stream().sorted().forEach(i -> newDebugText.append("  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n").append("    <td>").append(Utils.STANDARD_STEP_LABELS[i]).append("</td>\n").append("    <td>").append(stepAlgo.candidateCountAfterStep.get(i)).append("</td>\n").append("    <td>").append(i > 0 ? (stepAlgo.candidateCountAfterStep.get(i) - stepAlgo.candidateCountAfterStep.get(i - 1)) : "-").append("</td>\n").append("  </tr>\n"));
                newDebugText.append("</table>\n");


                Map<Pair<Set<String>, Set<String>>, Place> candidatePlaceMap = stepAlgo.buildingNetStep.getCandidatePlaceMap();
                Set<Pair<Set<String>, Set<String>>> includedCandidates = candidatePlaceMap.keySet().stream().filter(c -> this.net.getNet().getPlaces().contains(candidatePlaceMap.get(c))).collect(Collectors.toSet());
                List<Pair<String, String>> dfRelsNotCovered = Arrays.stream(keys).filter(p -> {
                    if (stepAlgo.buildingNetStep instanceof StandardAlphaPetriNetBuilding) {
                        if (p.getFirst().equals(START_ACTIVITY) || p.getSecond().equals(END_ACTIVITY)) {
                            return false;
                        }
                    }
                    if (logProcessor.getDfg().getOrDefault(new Pair<String, String>(p.getSecond(), p.getFirst()), 0) > 0) {
                        return false;
                    }
                    return includedCandidates.stream().noneMatch(c -> c.getFirst().contains(p.getFirst()) && c.getSecond().contains(p.getSecond()));
                }).collect(Collectors.toList());
                newDebugText.append("<br/><h2>DF Relations not covered by Places:</h2><br/>");
                if (dfRelsNotCovered.size() > 0) {
                    newDebugText.append("<table style=\"padding-left: 20px;\">\n" + "  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" + "    <th>Count</th>\n" + "    <th>Relation</th>\n" + "  </tr>\n");
                    int colorIndex = 0;
                    double scalingFactor = ((255 - 50) * 5.0) / this.logProcessor.getDfg().getOrDefault(keys[0], 1);
                    for (Pair<String, String> dfRel : dfRelsNotCovered) {
                        Color c = Utils.KELLYS_COLORS[colorIndex % Utils.KELLYS_COLORS.length];
                        int alpha = useColorsScaled ? Math.min((int) (scalingFactor * this.logProcessor.getDfg().getOrDefault(dfRel, 0)) + 50, 255) : 255;
                        if (useColors) {
                            Transition t1 = null;
                            Transition t2 = null;
                            if (dfRel.getFirst().equals(START_ACTIVITY)) {
                                if (!dfRel.getSecond().equals(END_ACTIVITY)) {
                                    t2 = Utils.getTransitionWithLabel(this.net.getNet(), dfRel.getSecond());
                                    c = Color.GREEN;
                                    c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
                                }
                            } else if (dfRel.getSecond().equals(END_ACTIVITY)) {
                                if (!dfRel.getFirst().equals(START_ACTIVITY)) {
                                    t1 = Utils.getTransitionWithLabel(this.net.getNet(), dfRel.getFirst());
                                    c = Color.RED;
                                    c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
                                }
                            } else {
                                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
                                colorIndex++;
                                t1 = Utils.getTransitionWithLabel(this.net.getNet(), dfRel.getFirst());
                                t2 = Utils.getTransitionWithLabel(this.net.getNet(), dfRel.getSecond());
                                Place imaginaryPlace = this.net.getNet().addPlace("Missing DF Relation ->");
                                imaginaryPlace.getAttributeMap().put(AttributeMap.SHAPE, new Polygon() {
                                    @Override
                                    protected Point2D[] getPoints(double x, double y, double h, double w) {
                                        Point2D[] points = new Point2D[3];
                                        points[0] = new Point2D.Double(x, y);
                                        points[1] = new Point2D.Double(x, y + h);
                                        points[2] = new Point2D.Double(x + w, y + (h / 2));
                                        return points;
                                    }
                                });
                                imaginaryPlace.getAttributeMap().put(AttributeMap.FILLCOLOR, c);
                                Arc a1 = this.net.getNet().addArc(t1, imaginaryPlace);
                                a1.getAttributeMap().put(AttributeMap.EDGECOLOR, c);
                                Arc a2 = this.net.getNet().addArc(imaginaryPlace, t2);
                                a2.getAttributeMap().put(AttributeMap.EDGECOLOR, c);
                            }
                            if (t1 != null) {
                                if (t1.getAttributeMap().get(AttributeMap.FILLCOLOR, null) == null) {
                                    t1.getAttributeMap().put(AttributeMap.FILLCOLOR, c);
                                } else if (t1.getAttributeMap().get(AttributeMap.GRADIENTCOLOR, null) == null) {
                                    t1.getAttributeMap().put(AttributeMap.GRADIENTCOLOR, c);
                                } else {
                                    t1.getAttributeMap().put(AttributeMap.DASHPATTERN, new float[]{0.5f, 5f});
                                    t1.getAttributeMap().put(AttributeMap.BORDERWIDTH, 2);
                                }
                            }

                            if (t2 != null) {
                                if (t2.getAttributeMap().get(AttributeMap.FILLCOLOR, null) == null) {
                                    t2.getAttributeMap().put(AttributeMap.FILLCOLOR, c);
                                } else if (t2.getAttributeMap().get(AttributeMap.GRADIENTCOLOR, null) == null) {
                                    t2.getAttributeMap().put(AttributeMap.GRADIENTCOLOR, c);
                                } else {
                                    t2.getAttributeMap().put(AttributeMap.DASHPATTERN, new float[]{0.5f, 5f});
                                    t2.getAttributeMap().put(AttributeMap.BORDERWIDTH, 2);
                                }
                            }


                        }

                        int value = this.logProcessor.getDfg().getOrDefault(dfRel, 0);
                        String rgb = "";
                        if (useColors) {
                            rgb = "rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")";
                        }

                        newDebugText.append("  <tr style=\"text-align: center; border-bottom: 1px solid black; \">\n" + "    <td style=\"background-color: " + rgb + ";\"" + "><b>").append(value).append("</b></td>\n").append("    <td>").append(dfRel.getFirst()).append(" → ").append(dfRel.getSecond()).append("</td>\n").append("  </tr>\n");
                    }
                    newDebugText.append("</table>\n");
                } else {
                    newDebugText.append("<p>All DF Relations are covered by Places.");
                }
            }
        }
        return newDebugText.toString();
    }


    private class ExperimentGUIRunner extends SwingWorker<AcceptingPetriNet, Void> {
        private final AlgorithmExperiment experiment;
        private final UIPluginContext context;
        private final LogProcessor logProcessor;

        public ExperimentGUIRunner(AlgorithmExperiment experiment, UIPluginContext context, LogProcessor logProcessor) {
            this.experiment = experiment;
            this.context = context;
            this.logProcessor = logProcessor;
        }

        @Override
        protected AcceptingPetriNet doInBackground() throws Exception {
            return experiment.execute(context, logProcessor);
        }

        @Override
        protected void done() {
            try {
                AcceptingPetriNet acceptingPetriNet = runner.get();
                if (netRes != null && !netRes.isFavorite()) {
                    netRes.destroy();
                }
                net = acceptingPetriNet;
                netRes = (ProMResource<?>) createProMResourceFromAPN(context, net, "Accepting Petri net of " + logName + " mined with " + usedAlgo.name);
                if (useColors) {
                    saveNetButton.setEnabled(false);
                    saveNetButton.setText("Mine a model without the coloring option to save it in ProM.");
                } else {
                    saveNetButton.setEnabled(true);
                    saveNetButton.setText("Save accepting Petri net in ProM");

                }
                debugText.setText(getDebugText());
                debugText.setCaretPosition(0);
                createNetVis();
                revalidate();
                repaint();
                goButton.setEnabled(true);
                progressBar.setVisible(false);
                context.getProgress().cancel();
                context.getTask().destroy();
            } catch (Exception exception) {
                goButton.setEnabled(true);
                progressBar.setVisible(false);
                exception.printStackTrace();
                System.err.println("AlphaRevisit Runner interrupted:" + exception);
                context.getTask().destroy();
//                    context.getFutureResult(0).cancel(true);
            }
        }
    }


}
