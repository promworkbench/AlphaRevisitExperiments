package org.processmining.alpharevisitexperiments.ui;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.api.model.ViewType;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.alpharevisitexperiments.algorithms.AlgorithmExperiment;
import org.processmining.alpharevisitexperiments.dialogs.OptionsUI;
import org.processmining.alpharevisitexperiments.plugins.ExperimentRunner;
import org.processmining.alpharevisitexperiments.util.LogProcessor;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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

        JButton saveNetButton = new JButton("Save accepting Petri net in ProM");
        saveNetButton.setAlignmentX(CENTER_ALIGNMENT);
        JComboBox viewSelector = SlickerFactory.instance().createComboBox(new String[]{});
        center.add(viewSelector);
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

        debugText.setText(getDebugText());
        debugText.setCaretPosition(0);

        Petrinet sampleNet = PetrinetFactory.newPetrinet("Sample Petri net");
        AcceptingPetriNet sampleANet = new AcceptingPetriNetImpl(sampleNet);
        final ProMResource<?> sampleANetRes = createProMResourceFromAPN(context, sampleANet, "Sample net");
        viewTypes = context.getGlobalContext().getViewManager().getViewTypes(sampleANetRes);
        sampleANetRes.setFavorite(true);

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
            this.netRes.setFavorite(true);
//           Adding a dummy net and removing it again fixes a weird glitch, where a new (favorite) APN does only appear
//            when switching views in the object menu
            Petrinet newSampleNet = PetrinetFactory.newPetrinet("Sample Petri net");
            AcceptingPetriNet newSampleANet = new AcceptingPetriNetImpl(sampleNet);
            final ProMResource<?> newSampleANetRes = createProMResourceFromAPN(context, sampleANet, "Sample net 2");
            newSampleANetRes.setFavorite(true);
            newSampleANetRes.destroy();
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
                    if (netRes != null && !netRes.isFavorite()) {
                        this.netRes.destroy();
                    }
                    this.net = acceptingPetriNet;
                    final ProMResource<?> newNetRes = createProMResourceFromAPN(context, this.net, "Accepting Petri net of " + logName + " mined with " + this.usedAlgo.name);
                    this.netRes = newNetRes;
                    saveNetButton.setEnabled(true);
                    createNetVis();
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


    private PluginExecutionResult getVisualizationResult(UIPluginContext contextx, ProMCanceller proMCanceller, Pair<Integer, PluginParameterBinding> binding, AcceptingPetriNet net) {
        PluginParameterBinding parameterBinding = binding.getSecond();
        List<Class<?>> parameterTypes = parameterBinding.getPlugin().getParameterTypes(parameterBinding.getMethodIndex());
        return parameterTypes.size() == 2 && parameterTypes.get(1) == ProMCanceller.class ? parameterBinding.invoke(contextx, new Object[]{net, proMCanceller}) : parameterBinding.invoke(contextx, new Object[]{net});
    }

    private void createNetVis() {
        netVis.removeAll();
        netVis.revalidate();
        netVis.repaint();
        this.view = viewTypes.get(selectedViewType).createView(this.netRes);
        JComponent newNetVis = this.view.getViewComponent();
        netVis.add(newNetVis);
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

    private ProMResource createProMResourceFromAPN(UIPluginContext context, AcceptingPetriNet net, String name) {
        context.getProvidedObjectManager().createProvidedObject(name, net,
                AcceptingPetriNet.class, context);
        final ProMResource<?> netRes = context.getGlobalContext().getResourceManager().getResourceForInstance(net);
        return netRes;
    }

    private String getDebugText() {
        StringBuilder newDebugText = new StringBuilder();
        if (usedAlgo != null) {
            newDebugText.append("<h1>" + usedAlgo.name + "</h1>");
        }
        if (net != null) {
            newDebugText.append("<h2>Petri net</h2><b>#Places: " + this.net.getNet().getPlaces().size() +
                    "</b><br/><b>#Arcs: " + this.net.getNet().getEdges().size() + "</b><br/>");
        }
        newDebugText.append("<h2>Log</h2><b>Activities:</b><br/>");
        String[] activities = logProcessor.getActivityOccurrences().keySet().toArray(new String[0]);
        Arrays.sort(activities, Comparator.comparingInt(a -> -this.logProcessor.getActivityOccurrence(a)));
        newDebugText.append("<table style=\"padding-left: 20px;\">\n" +
                "  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" +
                "    <th>Activity</th>\n" +
                "    <th>Count</th>\n" +
                "  </tr>\n");
        for (String act : activities) {
            int count = this.logProcessor.getActivityOccurrence(act);
            newDebugText.append("  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" +
                    "    <td>" + act + "</td>\n" +
                    "    <td>" + count + "</td>\n" +
                    "  </tr>\n");
//            newDebugText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(act).append(": ").append(count).append("<br/>");
        }
        newDebugText.append("</table>\n");


        newDebugText.append("<br/><b>DF Relations:</b><br/>");
        newDebugText.append("<table style=\"padding-left: 20px;\">\n" +
                "  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" +
                "    <th>Relation</th>\n" +
                "    <th>Count</th>\n" +
                "  </tr>\n");

        Pair<String, String>[] keys = logProcessor.getDfg().keySet().toArray(new Pair[0]);
        Arrays.sort(keys, Comparator.comparingInt(e -> -this.logProcessor.getDfg().getOrDefault(e, 0)));
        for (Pair<String, String> key : keys) {
            int value = this.logProcessor.getDfg().getOrDefault(key, 0);
            newDebugText.append("  <tr style=\"text-align: center; border-bottom: 1px solid black;\">\n" +
                    "    <td>" + key.getFirst() + " → " + key.getSecond() + "</td>\n" +
                    "    <td>" + value + "</td>\n" +
                    "  </tr>\n");
//            newDebugText.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;").append(key.getFirst()).append(" → ").append(key.getSecond()).append(": ").append(value).append("<br/>");
        }
        newDebugText.append("</table>\n");
        return newDebugText.toString();
    }
}
