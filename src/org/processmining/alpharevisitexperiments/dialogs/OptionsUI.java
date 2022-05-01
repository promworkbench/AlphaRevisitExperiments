package org.processmining.alpharevisitexperiments.dialogs;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import org.processmining.alpharevisitexperiments.algorithms.AlgorithmExperiment;
import org.processmining.alpharevisitexperiments.algorithms.AlphaOneDotOne;
import org.processmining.alpharevisitexperiments.algorithms.AlphaWithReplay;
import org.processmining.alpharevisitexperiments.algorithms.StandardAlpha;
import org.processmining.alpharevisitexperiments.options.ExperimentOption;
import org.processmining.framework.util.ui.widgets.ProMList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;

public class OptionsUI extends javax.swing.JPanel{

        private final AlgorithmExperiment[] algorithmExperiments = { new StandardAlpha(), new AlphaWithReplay(), new AlphaOneDotOne()};
        private HashMap<String,Object> optionValues = new HashMap<>();
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JScrollPane optionsScrollPane;
        private ProMList<AlgorithmExperiment> variantList;
        public OptionsUI() {
            initComponents();
        }

        @SuppressWarnings("unchecked")
        private void initComponents() {

            AbstractListModel listModel = new javax.swing.AbstractListModel<AlgorithmExperiment>() {
                AlgorithmExperiment[] experiments = algorithmExperiments;
                public int getSize() { return experiments.length; }
                public AlgorithmExperiment getElementAt(int i) { return experiments[i]; }
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

            jPanel1.setLayout(new GridLayout(0, 1, 0, 0));
            jLabel2.setText("Select further options");

            final Color bgColor = new Color(0xB0B0B0);
            jPanel1.setBackground(bgColor);
            optionsScrollPane.setBackground(bgColor);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel1)
                                            .addComponent(variantList, GroupLayout.PREFERRED_SIZE, 176, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                    .addComponent(jLabel2)
                                                    .addGap(105))
                                            .addGroup(layout.createSequentialGroup()
                                                    .addComponent(optionsScrollPane, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                                                    .addContainerGap())))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel1)
                                            .addComponent(jLabel2))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(optionsScrollPane, GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                                            .addComponent(variantList, GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
                                    .addContainerGap())
            );
            this.setLayout(layout);
        }

        private void variantListValueChanged(AlgorithmExperiment experiment) {
            System.out.println("Changed Value " + variantList.getSelectedValuesList());
            this.jPanel1.removeAll();
            jPanel1.setLayout(new GridLayout(0,1));
            final ExperimentOption[] options = experiment.getOptions();
            optionValues = new HashMap<>();
            for(ExperimentOption option : options) {
                JPanel panel = new JPanel();
                panel.setLayout(new GridBagLayout());
                JLabel jLabelName = new JLabel(option.getName(), SwingConstants.LEFT);
                jLabelName.setFont(new Font("Dialog", Font.BOLD, 16));
                GridBagConstraints c = new GridBagConstraints();
                c.weightx = 0.5;
                c.weighty = 0.5;
                c.gridx = 0;
                c.gridy = 0;
                c.fill = GridBagConstraints.HORIZONTAL;
                panel.add(jLabelName,c);
                if(option.getType() == Integer.class) {
                    ExperimentOption<Integer> integerOption = (ExperimentOption<Integer>) option;
                    NiceIntegerSlider jslider = SlickerFactory.instance().createNiceIntegerSlider("Select a value", integerOption.getMinValue(),
                    integerOption.getMaxValue(), integerOption.getStartValue(), NiceSlider.Orientation.HORIZONTAL);
                    new javax.swing.JSlider();
                    jslider.setValue(integerOption.getStartValue());
                    jslider.setToolTipText(integerOption.getName());
                    jslider.addChangeListener(e -> integerOption.setValue(jslider.getValue()));
                    c.weightx = 0.5;
                    c.gridx = 0;
                    c.gridy = 1;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    panel.add(jslider,c);
                    c.weightx = 0.5;
                    c.gridx = 0;
                    c.gridy = 3;
                    c.ipady = 20;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    panel.add(new JSeparator(),c);
                    panel.setOpaque(false);
                }else if(option.getType() == Boolean.class) {
                    ExperimentOption<Boolean> booleanOption = (ExperimentOption<Boolean>) option;
                    JCheckBox checkbox = SlickerFactory.instance().createCheckBox(booleanOption.getName(), booleanOption.getStartValue());
                    checkbox.addChangeListener(e -> booleanOption.setValue(checkbox.isSelected()));
                    c.weightx = 0.5;
                    c.gridx = 0;
                    c.gridy = 1;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    panel.add(checkbox,c);
                    c.weightx = 0.5;
                    c.gridx = 0;
                    c.gridy = 2;
                    c.ipady = 20;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    panel.add(new JSeparator(),c);
                    panel.setOpaque(false);
                }else {
                    System.err.println("Other options not yet implemented!");
                }
                jPanel1.add(panel);
            }
            System.out.println("Creating slider");
            optionsScrollPane.setViewportView(jPanel1);
            this.jPanel1.validate();
            this.jPanel1.repaint();
        }

        public String getSelected() {
            return variantList.getSelectedValuesList().get(0).name;
        }

        public HashMap<String,Object> getOptions(){
            return this.optionValues;
        }

        public AlgorithmExperiment getSelectedExperiment() {
            return  variantList.getSelectedValuesList().get(0);
        }
    }
