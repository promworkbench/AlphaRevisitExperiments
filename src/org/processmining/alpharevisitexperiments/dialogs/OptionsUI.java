package org.processmining.alpharevisitexperiments.dialogs;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import org.processmining.alpharevisitexperiments.algorithms.AlgorithmExperiment;
import org.processmining.alpharevisitexperiments.algorithms.StepBasedAlgorithm;
import org.processmining.alpharevisitexperiments.algorithms.steps.*;
import org.processmining.alpharevisitexperiments.algorithms.steps.impl.*;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.alpharevisitexperiments.util.Utils;
import org.processmining.framework.util.ui.widgets.ProMList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;


public class OptionsUI extends javax.swing.JPanel {

    private final StepBasedAlgorithm[] algorithmExperiments = {
//            new StandardAlpha(), new AlphaWithReplay(),
//            new AlphaOneDotOne(), new AlphaOneDotOneWithReplay(),
//                new AlphaTwoDotZero(), new AlphaTwoDotZeroWithReplay(),
//            new AlphaTwoDotOne(), new AlphaTwoDotOneWithReplay(),
//            new AlphaThreeDotZero(), new AlphaThreeDotZeroWithReplay()
            new StepBasedAlgorithm()};

    private HashMap<String, Object> optionValues = new HashMap<>();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane optionsScrollPane;
    private ProMList<StepBasedAlgorithm> variantList;

    public OptionsUI() {
        this(false);
    }

    public OptionsUI(boolean compactMode) {
        initComponents(compactMode);
    }

    private static JTextArea createLabelWithTextWrap(String text) {
        JTextArea textArea = new JTextArea(1, 30);
        textArea.setText(text);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBackground(UIManager.getColor("Label.background"));
        textArea.setFont(UIManager.getFont("Label.font"));
        textArea.setBorder(UIManager.getBorder("Label.border"));
        return textArea;
    }

    @SuppressWarnings("unchecked")
    private void initComponents(boolean compactMode) {

        AbstractListModel listModel = new javax.swing.AbstractListModel<AlgorithmExperiment>() {
            final AlgorithmExperiment[] experiments = algorithmExperiments;

            public int getSize() {
                return experiments.length;
            }

            public AlgorithmExperiment getElementAt(int i) {
                return experiments[i];
            }
        };

        variantList = new ProMList<>("Select an experiment", listModel);
        variantList.getSelectedValuesList();
        variantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variantList.setPreferredSize(new Dimension(100, 100));
        jLabel1 = new javax.swing.JLabel();
        optionsScrollPane = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();


        variantList.addListSelectionListener(evt -> {
            StepBasedAlgorithm selected = variantList.getSelectedValuesList().get(0);
            variantListValueChanged(selected);
        });
        variantList.setSelectedIndex(0);
        optionsScrollPane.setViewportView(jPanel1);
//        optionsScrollPane.setBorder(new EmptyBorder(3, 5, 3, 5));

        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));
        jLabel2.setText("Select further options");
        jLabel2.setAlignmentX(CENTER_ALIGNMENT);

        BorderLayout layout = new BorderLayout();
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add(optionsScrollPane);
        this.setLayout(layout);
        if (compactMode) {
            setOpaque(false);
            jPanel1.setOpaque(false);
            optionsScrollPane.setOpaque(false);
            jLabel2.setOpaque(false);
            add(optionsPanel, BorderLayout.CENTER);
        } else {
            add(optionsPanel, BorderLayout.CENTER);

            final Color bgColor = new Color(0xB0B0B0);
            setBackground(bgColor);
            jPanel1.setBackground(bgColor);
            optionsScrollPane.setBackground(bgColor);
            jLabel2.setBackground(bgColor);
            optionsPanel.setBackground(bgColor);
        }
    }

    public String getSelected() {
        return variantList.getSelectedValuesList().get(0).name;
    }

    public HashMap<String, Object> getOptions() {
        return this.optionValues;
    }

    public AlgorithmExperiment getSelectedExperiment() {
        return variantList.getSelectedValuesList().get(0);
    }

    private void variantListValueChanged(StepBasedAlgorithm experiment) {
        System.out.println("Changed Value " + variantList.getSelectedValuesList());
        this.jPanel1.removeAll();
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));
        jPanel1.setAlignmentY(TOP_ALIGNMENT);
        jPanel1.setAlignmentX(LEFT_ALIGNMENT);
        jPanel1.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel presetPanel = new JPanel();
        presetPanel.setAlignmentX(LEFT_ALIGNMENT);
        JComboBox<String> presetChooser = new JComboBox<>();
        presetChooser.addItem("-");
        presetChooser.addItem("Alpha 1.0");
        presetChooser.addItem("Alpha 1.1");
        presetChooser.addItem("Alpha 2.0");
        presetChooser.addItem("Alpha 3.0");
        presetChooser.addItem("Alpha 3.0T");
        presetChooser.addItem("Alpha 3.0SM");
        presetChooser.addItem("Alpha 3.0EXP");

        presetChooser.setMaximumSize(presetChooser.getPreferredSize());
        JLabel presetChooserLabel = new JLabel("Apply a Preset:");
//        JButton presetApplyButton = new JButton("Apply Preset");
//        presetChooser.addActionListener();
        presetChooser.addActionListener(e -> {
            String selectedPreset = presetChooser.getSelectedItem().toString();
            switch (selectedPreset) {
                case "Alpha 1.0":
                    experiment.logRepairSteps = new LogRepairStep[]{new IdentityLogRepair()};
                    experiment.buildingCandidatesStep = new StandardAlphaCandidateBuilding();
                    experiment.pruningCandidatesSteps = new CandidatePruningStep[]{new IdentityCandidatePruning(), new IdentityCandidatePruning(), new MaximalCandidatesPruning()};
                    experiment.buildingNetStep = new StandardAlphaPetriNetBuilding();
                    experiment.postProcessingPetriNetSteps = new PostProcessingPetriNetStep[]{new IdentityNetProcessing()};
                    variantListValueChanged(experiment);
                    return;
                case "Alpha 1.1":
                    experiment.logRepairSteps = new LogRepairStep[]{new IdentityLogRepair()};
                    experiment.buildingCandidatesStep = new AlphaOneDotOneCandidateBuilding();
                    experiment.pruningCandidatesSteps = new CandidatePruningStep[]{new IdentityCandidatePruning(), new IdentityCandidatePruning(), new MaximalCandidatesPruning()};
                    experiment.buildingNetStep = new AlphaPetriNetBuilding();
                    experiment.postProcessingPetriNetSteps = new PostProcessingPetriNetStep[]{new IdentityNetProcessing()};
                    variantListValueChanged(experiment);
                    return;
                case "Alpha 2.0":
                    experiment.logRepairSteps = new LogRepairStep[]{new IdentityLogRepair()};
                    experiment.buildingCandidatesStep = new AlphaThreeDotZeroCandidateBuilding();
                    experiment.pruningCandidatesSteps = new CandidatePruningStep[]{new IdentityCandidatePruning(), new IdentityCandidatePruning(), new MaximalCandidatesPruning()};
                    experiment.buildingNetStep = new AlphaPetriNetBuilding();
                    experiment.postProcessingPetriNetSteps = new PostProcessingPetriNetStep[]{new IdentityNetProcessing()};
                    variantListValueChanged(experiment);
                    return;
                case "Alpha 3.0":
                    experiment.logRepairSteps = new LogRepairStep[]{new IdentityLogRepair()};
                    experiment.buildingCandidatesStep = new AlphaThreeDotZeroCandidateBuilding();
                    experiment.pruningCandidatesSteps = new CandidatePruningStep[]{new BalanceBasedCandidatePruning(), new IdentityCandidatePruning(), new MaximalCandidatesPruning()};
                    experiment.buildingNetStep = new AlphaPetriNetBuilding();
                    experiment.postProcessingPetriNetSteps = new PostProcessingPetriNetStep[]{new IdentityNetProcessing()};
                    variantListValueChanged(experiment);
                    return;
                case "Alpha 3.0T":
                    experiment.logRepairSteps = new LogRepairStep[]{new IdentityLogRepair()};
                    experiment.buildingCandidatesStep = new AlphaThreeDotZeroCandidateBuilding();
                    experiment.pruningCandidatesSteps = new CandidatePruningStep[]{new BalanceBasedCandidatePruning(), new CandidateTraceFittingFilter(), new MaximalCandidatesPruning()};
                    experiment.buildingNetStep = new AlphaPetriNetBuilding();
                    experiment.postProcessingPetriNetSteps = new PostProcessingPetriNetStep[]{new IdentityNetProcessing()};
                    variantListValueChanged(experiment);
                    return;
                case "Alpha 3.0SM":
                    experiment.logRepairSteps = new LogRepairStep[]{new IdentityLogRepair()};
                    experiment.buildingCandidatesStep = new AlphaThreeDotZeroCandidateBuilding();
                    experiment.pruningCandidatesSteps = new CandidatePruningStep[]{new BalanceBasedCandidatePruning(), new CandidateTraceFittingFilter(), new ScoredMaximalCandidatesPruning()};
                    experiment.buildingNetStep = new AlphaPetriNetBuilding();
                    experiment.postProcessingPetriNetSteps = new PostProcessingPetriNetStep[]{new IdentityNetProcessing()};
                    variantListValueChanged(experiment);
                    return;
                case "Alpha 3.0EXP":
                    experiment.logRepairSteps = new LogRepairStep[]{new IdentityLogRepair()};
                    experiment.buildingCandidatesStep = new AlphaThreeDotZeroExperimentalCandidateBuilding();
                    experiment.pruningCandidatesSteps = new CandidatePruningStep[]{new BalanceBasedCandidatePruning(), new IdentityCandidatePruning(), new MaximalCandidatesPruning()};
                    experiment.buildingNetStep = new ExperimentalPetriNetBuilding();
                    experiment.postProcessingPetriNetSteps = new PostProcessingPetriNetStep[]{new IdentityNetProcessing()};
                    variantListValueChanged(experiment);
                    return;
            }
        });
        presetPanel.setOpaque(false);
        presetPanel.add(presetChooserLabel);
        presetPanel.add(presetChooser);
        presetPanel.setMaximumSize(new Dimension(100000, 100));
        jPanel1.add(presetPanel, LEFT_ALIGNMENT);
        Step[] steps = experiment.getAllSteps();
        for (int i = 0; i < steps.length; i++) {
            Step step = steps[i];
            final ExperimentOption[] options = step.getOptions();
            JLabel jLabelStepName = new JLabel((i + 1) + ". " + step.getTypeName() + ": → " + Utils.STANDARD_STEP_LABELS[i]);
            jLabelStepName.setAlignmentX(LEFT_ALIGNMENT);
            jLabelStepName.setFont(new Font("Dialog", Font.BOLD, 18));
            jPanel1.add(jLabelStepName);
            JComboBox<String> stepChooser = new JComboBox<>();
            stepChooser.setBackground(Color.WHITE);
            stepChooser.setFont(new Font("Dialog", Font.BOLD, 14));
            stepChooser.setToolTipText("Change step");
            if (step instanceof LogRepairStep) {
                stepChooser.addItem(IdentityLogRepair.NAME);
                stepChooser.addItem(NamedTauLogRepair.NAME);
                stepChooser.setSelectedItem(step.name);
                stepChooser.addActionListener(e -> {
                    System.out.println(e);
                    String selectedItem = stepChooser.getSelectedItem().toString();
                    for (int j = 0; j < experiment.logRepairSteps.length; j++) {
                        if (experiment.logRepairSteps[j] == step) {
                            if (selectedItem.equals(IdentityLogRepair.NAME)) {
                                experiment.logRepairSteps[j] = new IdentityLogRepair();
                            } else if (selectedItem.equals(NamedTauLogRepair.NAME)) {
                                experiment.logRepairSteps[j] = new NamedTauLogRepair();
                            }
                        }
                    }
                    variantListValueChanged(experiment);
                    return;
                });
            } else if (step instanceof CandidateBuildingStep) {
                stepChooser.addItem(StandardAlphaCandidateBuilding.NAME);
                stepChooser.addItem(AlphaOneDotOneCandidateBuilding.NAME);
                stepChooser.addItem(AlphaThreeDotZeroCandidateBuilding.NAME);
                stepChooser.addItem(AlphaThreeDotZeroExperimentalCandidateBuilding.NAME);
                stepChooser.setSelectedItem(step.name);
                stepChooser.addActionListener(e -> {
                    System.out.println(e);
                    String selectedItem = stepChooser.getSelectedItem().toString();
                    if (selectedItem.equals(AlphaThreeDotZeroCandidateBuilding.NAME)) {
                        experiment.buildingCandidatesStep = new AlphaThreeDotZeroCandidateBuilding();
                    } else if (selectedItem.equals(AlphaOneDotOneCandidateBuilding.NAME)) {
                        experiment.buildingCandidatesStep = new AlphaOneDotOneCandidateBuilding();
                    } else if (selectedItem.equals(StandardAlphaCandidateBuilding.NAME)) {
                        experiment.buildingCandidatesStep = new StandardAlphaCandidateBuilding();
                    } else if (selectedItem.equals(AlphaThreeDotZeroExperimentalCandidateBuilding.NAME)) {
                        experiment.buildingCandidatesStep = new AlphaThreeDotZeroExperimentalCandidateBuilding();
                    }
                    variantListValueChanged(experiment);
                    return;
                });
            } else if (step instanceof CandidatePruningStep) {
                stepChooser.addItem(IdentityCandidatePruning.NAME);
                stepChooser.addItem(BalanceBasedCandidatePruning.NAME);
                stepChooser.addItem(MaximalCandidatesPruning.NAME);
                stepChooser.addItem(CandidateTraceFittingFilter.NAME);
                stepChooser.addItem(ScoredMaximalCandidatesPruning.NAME);
                stepChooser.setSelectedItem(step.name);
                stepChooser.addActionListener(e -> {
                    System.out.println(e);
                    String selectedItem = stepChooser.getSelectedItem().toString();
                    for (int j = 0; j < experiment.pruningCandidatesSteps.length; j++) {
                        if (experiment.pruningCandidatesSteps[j] == step) {
                            if (selectedItem.equals(IdentityCandidatePruning.NAME)) {
                                experiment.pruningCandidatesSteps[j] = new IdentityCandidatePruning();
                            } else if (selectedItem.equals(BalanceBasedCandidatePruning.NAME)) {
                                experiment.pruningCandidatesSteps[j] = new BalanceBasedCandidatePruning();
                            } else if (selectedItem.equals(MaximalCandidatesPruning.NAME)) {
                                experiment.pruningCandidatesSteps[j] = new MaximalCandidatesPruning();
                            } else if (selectedItem.equals(CandidateTraceFittingFilter.NAME)) {
                                experiment.pruningCandidatesSteps[j] = new CandidateTraceFittingFilter();
                            } else if (selectedItem.equals(ScoredMaximalCandidatesPruning.NAME)) {
                                experiment.pruningCandidatesSteps[j] = new ScoredMaximalCandidatesPruning();
                            }
                        }
                    }
                    variantListValueChanged(experiment);
                    return;
                });

            } else if (step instanceof PetriNetBuildingStep) {
                stepChooser.addItem(StandardAlphaPetriNetBuilding.NAME);
                stepChooser.addItem(AlphaPetriNetBuilding.NAME);
                stepChooser.addItem(ExperimentalPetriNetBuilding.NAME);
                stepChooser.setSelectedItem(step.name);
                stepChooser.addActionListener(e -> {
                    System.out.println(e);
                    String selectedItem = stepChooser.getSelectedItem().toString();
                    if (selectedItem.equals(AlphaPetriNetBuilding.NAME)) {
                        experiment.buildingNetStep = new AlphaPetriNetBuilding();
                    } else if (selectedItem.equals(StandardAlphaPetriNetBuilding.NAME)) {
                        experiment.buildingNetStep = new StandardAlphaPetriNetBuilding();
                    } else if (selectedItem.equals(ExperimentalPetriNetBuilding.NAME)) {
                        experiment.buildingNetStep = new ExperimentalPetriNetBuilding();
                    }
                    variantListValueChanged(experiment);
                    return;
                });
            } else if (step instanceof PostProcessingPetriNetStep) {
                stepChooser.addItem(IdentityNetProcessing.NAME);
                stepChooser.addItem(ReplayNetPostProcessing.NAME);
                stepChooser.setSelectedItem(step.name);
                stepChooser.addActionListener(e -> {
                    String selectedItem = stepChooser.getSelectedItem().toString();
                    for (int j = 0; j < experiment.postProcessingPetriNetSteps.length; j++) {
                        if (experiment.postProcessingPetriNetSteps[j] == step) {
                            if (selectedItem.equals(IdentityNetProcessing.NAME)) {
                                experiment.postProcessingPetriNetSteps[j] = new IdentityNetProcessing();
                            } else if (selectedItem.equals(ReplayNetPostProcessing.NAME)) {
                                experiment.postProcessingPetriNetSteps[j] = new ReplayNetPostProcessing();
                            }
                        }
                    }
                    variantListValueChanged(experiment);
                    return;
                });

            }
            stepChooser.setMaximumSize(stepChooser.getPreferredSize());
            stepChooser.setAlignmentX(LEFT_ALIGNMENT);
            jPanel1.add(stepChooser, LEFT_ALIGNMENT);
            optionValues = new HashMap<>();
            for (ExperimentOption option : options) {
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setAlignmentX(LEFT_ALIGNMENT);
                JTextArea jLabelName = createLabelWithTextWrap(option.getName());
                jLabelName.setAlignmentX(LEFT_ALIGNMENT);
                jLabelName.setFont(new Font("Dialog", Font.PLAIN, 13));
                panel.add(jLabelName);
                panel.setOpaque(false);
                if (option.getType() == Integer.class) {
                    ExperimentOption<Integer> integerOption = (ExperimentOption<Integer>) option;
                    NiceIntegerSlider jslider = SlickerFactory.instance().createNiceIntegerSlider("Select", integerOption.getMinValue(), integerOption.getMaxValue(), integerOption.getStartValue(), NiceSlider.Orientation.HORIZONTAL);
                    jslider.setValue(integerOption.getValue());
                    jslider.setAlignmentX(LEFT_ALIGNMENT);
                    jslider.addChangeListener(e -> integerOption.setValue(jslider.getValue()));
                    panel.add(jslider);
                } else if (option.getType() == Boolean.class) {
                    ExperimentOption<Boolean> booleanOption = (ExperimentOption<Boolean>) option;
                    JCheckBox checkbox = SlickerFactory.instance().createCheckBox(booleanOption.getName(), booleanOption.getStartValue());
                    checkbox.setSelected(booleanOption.getValue());
                    checkbox.addChangeListener(e -> booleanOption.setValue(checkbox.isSelected()));
                    panel.add(checkbox);
                } else if (option.getType() == Double.class) {
                    JPanel valuePanel = new JPanel();
                    valuePanel.setOpaque(false);
                    valuePanel.setAlignmentX(LEFT_ALIGNMENT);
                    valuePanel.setLayout(new BorderLayout());
                    ExperimentOption<Double> doubleOption = (ExperimentOption<Double>) option;

                    JTextField doubleOptionExact = new JTextField(doubleOption.getValue().toString());
                    doubleOptionExact.setFont(new Font("Dialog", Font.BOLD, 14));
                    doubleOptionExact.setAlignmentX(CENTER_ALIGNMENT);
                    NiceDoubleSlider slider = SlickerFactory.instance().createNiceDoubleSlider("Select", doubleOption.getMinValue(), doubleOption.getMaxValue(), doubleOption.getStartValue(), NiceSlider.Orientation.HORIZONTAL);
                    slider.setAlignmentX(LEFT_ALIGNMENT);
                    slider.setValue(doubleOption.getValue());
                    slider.addChangeListener(e -> {
                        doubleOption.setValue(slider.getValue());
                        doubleOptionExact.setText(doubleOption.getValue().toString());
                    });
                    doubleOptionExact.addActionListener(e -> {
                        try {
                            Double newValue = Double.parseDouble(doubleOptionExact.getText());
                            doubleOption.setValue(newValue);
                            doubleOptionExact.setText(doubleOption.getValue().toString());
                            slider.setValue(doubleOption.getValue());
                        } catch (Exception exc) {
                            doubleOptionExact.setText(doubleOption.getValue().toString());
                        }

                    });
                    valuePanel.add(slider, BorderLayout.CENTER);
                    valuePanel.add(doubleOptionExact, BorderLayout.PAGE_START);
                    panel.add(valuePanel);

                } else {
                    System.err.println("Other options not yet implemented!");
                }

                panel.setMaximumSize(new Dimension(panel.getPreferredSize().width, panel.getPreferredSize().height + 0));
                jPanel1.add(panel);
            }
            JSeparator seperator = new JSeparator();
            seperator.setPreferredSize(new Dimension(1, 10));
            jPanel1.add(seperator);
        }
        optionsScrollPane.setViewportView(jPanel1);
        this.jPanel1.validate();
        this.jPanel1.repaint();
    }
}
