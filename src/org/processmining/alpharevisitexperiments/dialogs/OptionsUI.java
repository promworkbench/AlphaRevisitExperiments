package org.processmining.alpharevisitexperiments.dialogs;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import org.processmining.alpharevisitexperiments.algorithms.*;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.framework.util.ui.widgets.ProMList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;


public class OptionsUI extends javax.swing.JPanel {

    private final AlgorithmExperiment[] algorithmExperiments = {new StandardAlpha(), new AlphaWithReplay(),
            new AlphaOneDotOne(), new AlphaOneDotOneWithReplay(),
//                new AlphaTwoDotZero(), new AlphaTwoDotZeroWithReplay(),
            new AlphaTwoDotOne(), new AlphaTwoDotOneWithReplay(),
            new AlphaThreeDotZero(), new AlphaThreeDotZeroWithReplay()};
    private HashMap<String, Object> optionValues = new HashMap<>();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane optionsScrollPane;
    private ProMList<AlgorithmExperiment> variantList;

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
            AlgorithmExperiment selected = variantList.getSelectedValuesList().get(0);
            variantListValueChanged(selected);
        });
        variantList.setSelectedIndex(0);
        optionsScrollPane.setViewportView(jPanel1);
        optionsScrollPane.setBorder(new EmptyBorder(3, 5, 3, 5));

        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));
        jLabel2.setText("Select further options");
        jLabel2.setAlignmentX(CENTER_ALIGNMENT);
        final Color bgColor = new Color(0xB0B0B0);
        setBackground(bgColor);
        jPanel1.setBackground(bgColor);
        optionsScrollPane.setBackground(bgColor);
        jLabel2.setBackground(bgColor);

        BorderLayout layout = new BorderLayout();
        JPanel variantPanel = new JPanel();
        variantPanel.setBackground(bgColor);
        variantPanel.setLayout(new BoxLayout(variantPanel, BoxLayout.Y_AXIS));
        variantPanel.add(jLabel1);
        variantPanel.add(variantList);
        variantList.setPreferredSize(new Dimension(200, 200));

        JPanel optionsPanel = new JPanel();
        optionsPanel.setBackground(bgColor);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.add(jLabel2, LEFT_ALIGNMENT);
        optionsPanel.add(optionsScrollPane);
        this.setLayout(layout);
        if (compactMode) {
            add(variantPanel, BorderLayout.NORTH);
            add(optionsPanel, BorderLayout.CENTER);
        } else {
            add(variantPanel, BorderLayout.LINE_START);
            add(optionsPanel, BorderLayout.CENTER);
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

    private void variantListValueChanged(AlgorithmExperiment experiment) {
        System.out.println("Changed Value " + variantList.getSelectedValuesList());
        this.jPanel1.removeAll();
        final ExperimentOption[] options = experiment.getOptions();
//            jPanel1.setLayout(new GridLayout(options.length,1,3,3));
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));
        jPanel1.setAlignmentY(TOP_ALIGNMENT);
        jPanel1.setAlignmentX(LEFT_ALIGNMENT);
        optionValues = new HashMap<>();
        for (ExperimentOption option : options) {
//            option.setValue(option.getStartValue());
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setAlignmentX(LEFT_ALIGNMENT);
            JTextArea jLabelName = createLabelWithTextWrap(option.getName());
            jLabelName.setAlignmentX(LEFT_ALIGNMENT);
            jLabelName.setFont(new Font("Dialog", Font.BOLD, 16));


            panel.add(new JSeparator());
            panel.add(jLabelName);
            panel.setOpaque(false);
            if (option.getType() == Integer.class) {
                ExperimentOption<Integer> integerOption = (ExperimentOption<Integer>) option;
                NiceIntegerSlider jslider = SlickerFactory.instance().createNiceIntegerSlider("Select", integerOption.getMinValue(),
                        integerOption.getMaxValue(), integerOption.getStartValue(), NiceSlider.Orientation.HORIZONTAL);
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
//                panel.setLayout(new BorderLayout());
                JPanel valuePanel = new JPanel();
                valuePanel.setOpaque(false);
                valuePanel.setAlignmentX(LEFT_ALIGNMENT);
                valuePanel.setLayout(new BorderLayout());
                ExperimentOption<Double> doubleOption = (ExperimentOption<Double>) option;

                JTextField doubleOptionExact = new JTextField(doubleOption.getValue().toString());
                doubleOptionExact.setOpaque(false);
                doubleOptionExact.setFont(new Font("Dialog", Font.BOLD, 14));
                doubleOptionExact.setAlignmentX(CENTER_ALIGNMENT);
                NiceDoubleSlider slider = SlickerFactory.instance().createNiceDoubleSlider("Select", doubleOption.getMinValue(), doubleOption.getMaxValue(), doubleOption.getStartValue(), NiceSlider.Orientation.HORIZONTAL);
                slider.setAlignmentX(LEFT_ALIGNMENT);
                slider.setValue(doubleOption.getValue());
//                slider.setMaximumSize(new Dimension(slider.getPreferredSize().width - 50, slider.getPreferredSize().height));
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

            panel.setMaximumSize(new Dimension(panel.getPreferredSize().width, panel.getPreferredSize().height + 40));
            jPanel1.add(panel);
        }
        optionsScrollPane.setViewportView(jPanel1);
        this.jPanel1.validate();
        this.jPanel1.repaint();
    }
}
