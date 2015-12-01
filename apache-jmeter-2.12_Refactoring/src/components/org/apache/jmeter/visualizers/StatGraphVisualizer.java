/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.SaveGraphics;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RateRenderer;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import java.text.MessageFormat;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import java.text.DecimalFormat;
import java.text.Format;

/**
 * Aggregrate Table-Based Reporting Visualizer for JMeter. Props to the people
 * who've done the other visualizers ahead of me (Stefano Mazzocchi), who I
 * borrowed code from to start me off (and much code may still exist). Thank
 * you!
 *
 */
public class StatGraphVisualizer extends AbstractVisualizer implements Clearable, ActionListener {
    private StatGraphVisualizerProduct3 statGraphVisualizerProduct3 = new StatGraphVisualizerProduct3();

	private StatGraphVisualizerProduct2 statGraphVisualizerProduct2 = new StatGraphVisualizerProduct2();

	private StatGraphVisualizerProduct statGraphVisualizerProduct = new StatGraphVisualizerProduct();

	private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();
    
        private static final String pct1Label = JMeterUtils.getPropDefault("aggregate_rpt_pct1", "90");
        private static final String pct2Label = JMeterUtils.getPropDefault("aggregate_rpt_pct2", "95");
        private static final String pct3Label = JMeterUtils.getPropDefault("aggregate_rpt_pct3", "99");
       
        private static final Float pct1Value = new Float(Float.parseFloat(pct1Label)/100);
        private static final Float pct2Value =  new Float(Float.parseFloat(pct2Label)/100);
        private static final Float pct3Value =  new Float(Float.parseFloat(pct3Label)/100);

        static final String[] COLUMNS = { 
        		            "sampler_label",                  //$NON-NLS-1$
        		            "aggregate_report_count",         //$NON-NLS-1$
        		            "average",                        //$NON-NLS-1$
        		            "aggregate_report_median",        //$NON-NLS-1$
        		            "aggregate_report_xx_pct1_line",      //$NON-NLS-1$
        		            "aggregate_report_xx_pct2_line",      //$NON-NLS-1$
        		            "aggregate_report_xx_pct3_line",      //$NON-NLS-1$
        		            "aggregate_report_min",           //$NON-NLS-1$
        		            "aggregate_report_max",           //$NON-NLS-1$
        		            "aggregate_report_error%",        //$NON-NLS-1$
        		            "aggregate_report_rate",          //$NON-NLS-1$
        		            "aggregate_report_bandwidth" };   //$NON-NLS-1$
        
        // Column formats
            static final Format[] FORMATS =
                new Format[]{
                    null, // Label
                   null, // count
                    null, // Mean
                    null, // median
                    null, // 90%
                    null, // 95%
                    null, // 99%
                    null, // Min
                    null, // Max
                    new DecimalFormat("#0.00%"), // Error %age //$NON-NLS-1$
                    new DecimalFormat("#.0"),      // Throughput //$NON-NLS-1$
                    new DecimalFormat("#.0")    // pageSize   //$NON-NLS-1$
                };
            
        		    
        		    static final Object[][] COLUMNS_MSG_PARAMETERS = { null, //$NON-NLS-1$
        		            null,                             //$NON-NLS-1$
        		            null,                             //$NON-NLS-1$
        		            null,                             //$NON-NLS-1$
        		            new Object[]{pct1Label},                      //$NON-NLS-1$
        		            new Object[]{pct2Label},                      //$NON-NLS-1$
        		            new Object[]{pct3Label},                      //$NON-NLS-1$
        		            null,                             //$NON-NLS-1$
        		            null,                             //$NON-NLS-1$
        		            null,                             //$NON-NLS-1$
        		            null,                             //$NON-NLS-1$
        		            null };                           //$NON-NLS-1$
        		
        		    private final String[] GRAPH_COLUMNS = {"average",//$NON-NLS-1$
        		            "aggregate_report_median",        //$NON-NLS-1$
        		            "aggregate_report_xx_pct1_line",      //$NON-NLS-1$
        		            "aggregate_report_xx_pct2_line",      //$NON-NLS-1$
        		            "aggregate_report_xx_pct3_line",      //$NON-NLS-1$
        		            "aggregate_report_min",           //$NON-NLS-1$
        		            "aggregate_report_max"};   

    private final String TOTAL_ROW_LABEL =
        JMeterUtils.getResString("aggregate_report_total_label");       //$NON-NLS-1$

    private Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 10); // $NON-NLS-1$

    private JTable myJTable;

    private JScrollPane myScrollPane;

    private transient ObjectTableModel model;

    /**
     * Lock used to protect tableRows update + model update
     */
    private final transient Object lock = new Object();
    
    private final Map<String, SamplingStatCalculator> tableRows =
        new ConcurrentHashMap<String, SamplingStatCalculator>();

    private AxisGraph graphPanel = null;

    private JPanel settingsPane = null;

    private JSplitPane spane = null;

    //NOT USED protected double[][] data = null;

    private JTabbedPane tabbedGraph = new JTabbedPane(SwingConstants.TOP);

    private JButton displayButton =
        new JButton(JMeterUtils.getResString("aggregate_graph_display"));                //$NON-NLS-1$

    private JButton saveGraph =
        new JButton(JMeterUtils.getResString("aggregate_graph_save"));                    //$NON-NLS-1$

    private JButton saveTable =
        new JButton(JMeterUtils.getResString("aggregate_graph_save_table"));            //$NON-NLS-1$

    private JButton chooseForeColor =
        new JButton(JMeterUtils.getResString("aggregate_graph_choose_foreground_color"));            //$NON-NLS-1$

    private JButton syncWithName =
        new JButton(JMeterUtils.getResString("aggregate_graph_sync_with_name"));            //$NON-NLS-1$

    private JCheckBox saveHeaders = // should header be saved with the data?
        new JCheckBox(JMeterUtils.getResString("aggregate_graph_save_table_header"));    //$NON-NLS-1$

    private JLabeledTextField graphTitle =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_user_title"));    //$NON-NLS-1$

    private JLabeledTextField maxLengthXAxisLabel =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_max_length_xaxis_label"), 8);//$NON-NLS-1$

    private JLabeledTextField maxValueYAxisLabel =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_yaxis_max_value"), 8);//$NON-NLS-1$

    /**
     * checkbox for use dynamic graph size
     */
    private JCheckBox dynamicGraphSize = new JCheckBox(JMeterUtils.getResString("aggregate_graph_dynamic_size")); // $NON-NLS-1$

    private JLabeledTextField graphWidth =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_width"), 6);        //$NON-NLS-1$
    private JLabeledTextField graphHeight =
        new JLabeledTextField(JMeterUtils.getResString("aggregate_graph_height"), 6);        //$NON-NLS-1$

    private String yAxisLabel = JMeterUtils.getResString("aggregate_graph_response_time");//$NON-NLS-1$

    private String yAxisTitle = JMeterUtils.getResString("aggregate_graph_ms");        //$NON-NLS-1$

    private boolean saveGraphToFile = false;

    private int defaultWidth = 400;

    private int defaultHeight = 300;

    private JComboBox columnsList = new JComboBox(GRAPH_COLUMNS);

    private List<BarGraph> eltList = new ArrayList<BarGraph>();

    private JComboBox titleFontNameList = new JComboBox(StatGraphProperties.getFontNameMap().keySet().toArray());

    private JComboBox titleFontSizeList = new JComboBox(StatGraphProperties.fontSize);

    private JComboBox titleFontStyleList = new JComboBox(StatGraphProperties.getFontStyleMap().keySet().toArray());

    private JCheckBox drawOutlinesBar = new JCheckBox(JMeterUtils.getResString("aggregate_graph_draw_outlines"), true); // Default checked // $NON-NLS-1$

    private JCheckBox numberShowGrouping = new JCheckBox(JMeterUtils.getResString("aggregate_graph_number_grouping"), true); // Default checked // $NON-NLS-1$
    
    private JCheckBox valueLabelsVertical = new JCheckBox(JMeterUtils.getResString("aggregate_graph_value_labels_vertical"), true); // Default checked // $NON-NLS-1$

    private Color colorBarGraph = Color.YELLOW;

    private Color colorForeGraph = Color.BLACK;
    
    private int nbColToGraph = 1;

    public StatGraphVisualizer() {
        super();
                model = createObjectTableModel();
                eltList.add(new BarGraph(JMeterUtils.getResString("average"), true, new Color(202, 0, 0)));
                eltList.add(new BarGraph(JMeterUtils.getResString("aggregate_report_median"), false, new Color(49, 49, 181)));
                eltList.add(new BarGraph(MessageFormat.format(JMeterUtils.getResString("aggregate_report_xx_pct1_line"),new Object[]{pct1Label}), false, new Color(42, 121, 42)));
                eltList.add(new BarGraph(MessageFormat.format(JMeterUtils.getResString("aggregate_report_xx_pct2_line"),new Object[]{pct2Label}), false, new Color(242, 226, 8)));
                eltList.add(new BarGraph(MessageFormat.format(JMeterUtils.getResString("aggregate_report_xx_pct3_line"),new Object[]{pct3Label}), false, new Color(202, 10 , 232)));
                eltList.add(new BarGraph(JMeterUtils.getResString("aggregate_report_min"), false, Color.LIGHT_GRAY));
                eltList.add(new BarGraph(JMeterUtils.getResString("aggregate_report_max"), false, Color.DARK_GRAY));
                clearData();
                init();
            }
    
        /**
         * Creates that Table model 
         * @return ObjectTableModel
         */
        static ObjectTableModel createObjectTableModel() {
            return new ObjectTableModel(COLUMNS,
                SamplingStatCalculator.class,
                new Functor[] {
                new Functor("getLabel"),                    //$NON-NLS-1$
                new Functor("getCount"),                    //$NON-NLS-1$
                new Functor("getMeanAsNumber"),                //$NON-NLS-1$
                new Functor("getMedian"),                    //$NON-NLS-1$
                new Functor("getPercentPoint",                //$NON-NLS-1$
                 new Object[] { pct1Value }),
                 new Functor("getPercentPoint",                //$NON-NLS-1$
                 new Object[] { pct2Value }),
                 new Functor("getPercentPoint",                //$NON-NLS-1$
                 new Object[] { pct3Value }),
                new Functor("getMin"),                        //$NON-NLS-1$
                new Functor("getMax"),                         //$NON-NLS-1$
                new Functor("getErrorPercentage"),            //$NON-NLS-1$
                new Functor("getRate"),                        //$NON-NLS-1$
                new Functor("getKBPerSecond") },            //$NON-NLS-1$
                new Functor[] { null, null, null, null, null, null, null, null, null, null, null, null },
                                new Class[] { String.class, Long.class, Long.class, Long.class, Long.class, 
                                            Long.class, Long.class, Long.class, Long.class, String.class, 
                                            String.class, String.class });
    }

    // Column renderers
        static final TableCellRenderer[] RENDERERS =
        new TableCellRenderer[]{
            null, // Label
            null, // count
            null, // Mean
            null, // median
            null, // 90%
            null, // 95%
            null, // 99%
            null, // Min
            null, // Max
            new NumberRenderer("#0.00%"), // Error %age //$NON-NLS-1$
            new RateRenderer("#.0"),      // Throughput //$NON-NLS-1$
            new NumberRenderer("#.0"),    // pageSize
        };

    public static boolean testFunctors(){
        StatGraphVisualizer instance = new StatGraphVisualizer();
        return instance.model.checkFunctors(null,instance.getClass());
    }

    @Override
    public String getLabelResource() {
        return "aggregate_graph_title";                        //$NON-NLS-1$
    }

    @Override
    public void add(final SampleResult res) {
        final String sampleLabel = res.getSampleLabel();
        // Sampler selection
        if (statGraphVisualizerProduct2.getColumnSelection().isSelected() && statGraphVisualizerProduct2.getPattern() != null) {
            statGraphVisualizerProduct2.setMatcher(statGraphVisualizerProduct2.getPattern().matcher(sampleLabel));
        }
        if ((statGraphVisualizerProduct2.getMatcher() == null) || (statGraphVisualizerProduct2.getMatcher().find())) {
            JMeterUtils.runSafe(new Runnable() {
                @Override
                public void run() {
                    SamplingStatCalculator row = null;
                    synchronized (lock) {
                        row = tableRows.get(sampleLabel);
                        if (row == null) {
                            row = new SamplingStatCalculator(sampleLabel);
                            tableRows.put(row.getLabel(), row);
                            model.insertRow(row, model.getRowCount() - 1);
                        }
                    }
                    row.addSample(res);
                    tableRows.get(TOTAL_ROW_LABEL).addSample(res);
                    model.fireTableDataChanged();                    
                }
            });
        }
    }

    /**
     * Clears this visualizer and its model, and forces a repaint of the table.
     */
    @Override
    public void clearData() {
        synchronized (lock) {
            model.clearData();
            tableRows.clear();
            tableRows.put(TOTAL_ROW_LABEL, new SamplingStatCalculator(TOTAL_ROW_LABEL));
            model.addRow(tableRows.get(TOTAL_ROW_LABEL));
        }
    }

    /**
     * Main visualizer setup.
     */
    private void init() {
        this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);
        Border margin2 = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(makeTitlePanel());

        myJTable = new JTable(model);
        myJTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer(COLUMNS_MSG_PARAMETERS));
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        RendererUtils.applyRenderers(myJTable, RENDERERS);
        myScrollPane = new JScrollPane(myJTable);

        settingsPane = new VerticalPanel();
        settingsPane.setBorder(margin2);

        graphPanel = new AxisGraph();
        graphPanel.setPreferredSize(new Dimension(defaultWidth, defaultHeight));

        settingsPane.add(createGraphActionsPane());
        settingsPane.add(createGraphColumnPane());
        settingsPane.add(createGraphTitlePane());
        settingsPane.add(createGraphDimensionPane());
        JPanel axisPane = new JPanel(new BorderLayout());
        axisPane.add(createGraphXAxisPane(), BorderLayout.WEST);
        axisPane.add(createGraphYAxisPane(), BorderLayout.CENTER);
        settingsPane.add(axisPane);
        settingsPane.add(statGraphVisualizerProduct.createLegendPane());

        tabbedGraph.addTab(JMeterUtils.getResString("aggregate_graph_tab_settings"), settingsPane); //$NON-NLS-1$
        tabbedGraph.addTab(JMeterUtils.getResString("aggregate_graph_tab_graph"), graphPanel); //$NON-NLS-1$

        // If clic on the Graph tab, make the graph (without apply interval or filter)
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane srcTab = (JTabbedPane) changeEvent.getSource();
                int index = srcTab.getSelectedIndex();
                if (srcTab.getTitleAt(index).equals(JMeterUtils.getResString("aggregate_graph_tab_graph"))) { //$NON-NLS-1$
                    actionMakeGraph();
                }
            }
        };
        tabbedGraph.addChangeListener(changeListener);

        spane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        spane.setLeftComponent(myScrollPane);
        spane.setRightComponent(tabbedGraph);
        spane.setResizeWeight(.2);
        spane.setBorder(null); // see bug jdk 4131528
        spane.setContinuousLayout(true);

        this.add(mainPanel, BorderLayout.NORTH);
        this.add(spane, BorderLayout.CENTER);
    }

    public void makeGraph() {
        nbColToGraph = getNbColumns();
        Dimension size = graphPanel.getSize();
        String lstr = maxLengthXAxisLabel.getText();
        // canvas size
        int width = (int) size.getWidth();
        int height = (int) size.getHeight();
        if (!dynamicGraphSize.isSelected()) {
            String wstr = graphWidth.getText();
            String hstr = graphHeight.getText();
            if (wstr.length() != 0) {
                width = Integer.parseInt(wstr);
            }
            if (hstr.length() != 0) {
                height = Integer.parseInt(hstr);
            }
        }

        if (lstr.length() == 0) {
            lstr = "20";//$NON-NLS-1$
        }
        int maxLength = Integer.parseInt(lstr);
        String yAxisStr = maxValueYAxisLabel.getText();
        int maxYAxisScale = yAxisStr.length() == 0 ? 0 : Integer.parseInt(yAxisStr);

        graphPanel.setData(this.getData());
        graphPanel.setTitle(graphTitle.getText());
        graphPanel.setMaxLength(maxLength);
        graphPanel.setMaxYAxisScale(maxYAxisScale);
        graphPanel.setXAxisLabels(model.getAxisLabels());
        graphPanel.setXAxisTitle(JMeterUtils.getResString((String) columnsList.getSelectedItem()));;
        graphPanel.setYAxisLabels(this.yAxisLabel);
        graphPanel.setYAxisTitle(this.yAxisTitle);
        graphPanel.setLegendLabels(getLegendLabels());
        graphPanel.setColor(getBackColors());
        graphPanel.setForeColor(colorForeGraph);
        graphPanel.setOutlinesBarFlag(drawOutlinesBar.isSelected());
        graphPanel.setShowGrouping(numberShowGrouping.isSelected());
        graphPanel.setValueOrientation(valueLabelsVertical.isSelected());
        graphPanel.setLegendPlacement(StatGraphProperties.getPlacementNameMap()
                .get(statGraphVisualizerProduct.getLegendPlacementList().getSelectedItem()).intValue());

        graphPanel.setTitleFont(new Font(StatGraphProperties.getFontNameMap().get(titleFontNameList.getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(titleFontStyleList.getSelectedItem()).intValue(),
                Integer.parseInt((String) titleFontSizeList.getSelectedItem())));
        graphPanel.setLegendFont(new Font(StatGraphProperties.getFontNameMap().get(statGraphVisualizerProduct.getFontNameList().getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(statGraphVisualizerProduct.getFontStyleList().getSelectedItem()).intValue(),
                Integer.parseInt((String) statGraphVisualizerProduct.getFontSizeList().getSelectedItem())));
        graphPanel.setValueFont(new Font(StatGraphProperties.getFontNameMap().get(statGraphVisualizerProduct3.getValueFontNameList().getSelectedItem()),
                StatGraphProperties.getFontStyleMap().get(statGraphVisualizerProduct3.getValueFontStyleList().getSelectedItem()).intValue(),
                Integer.parseInt((String) statGraphVisualizerProduct3.getValueFontSizeList().getSelectedItem())));

        graphPanel.setHeight(height);
        graphPanel.setWidth(width);
        spane.repaint();
    }

    public double[][] getData() {
        if (model.getRowCount() > 1) {
            int count = model.getRowCount() -1;
            
            int size = nbColToGraph;
            double[][] data = new double[size][count];
            int s = 0;
            int cpt = 0;
            for (BarGraph bar : eltList) {
                if (bar.getChkBox().isSelected()) {
                    int col = model.findColumn((String) columnsList.getItemAt(cpt));
                    for (int idx=0; idx < count; idx++) {
                        data[s][idx] = ((Number)model.getValueAt(idx,col)).doubleValue();
                    }
                    s++;
                }
                cpt++;
            }
            return data;
        }
        // API expects null, not empty array
        return null;
    }

    private String[] getLegendLabels() {
        String[] legends = new String[nbColToGraph];
        int i = 0;
        for (BarGraph bar : eltList) {
            if (bar.getChkBox().isSelected()) {
                legends[i] = bar.getLabel();
                i++;
            }
        }
        return legends;
    }

    private Color[] getBackColors() {
        Color[] backColors = new Color[nbColToGraph];
        int i = 0;
        for (BarGraph bar : eltList) {
            if (bar.getChkBox().isSelected()) {
                backColors[i] = bar.getBackColor();
                i++;
            }
        }
        return backColors;
    }

    private int getNbColumns() {
        int i = 0;
        for (BarGraph bar : eltList) {
            if (bar.getChkBox().isSelected()) {
                i++;
            }
        }
        return i;
    }
    
    /**
     * We use this method to get the data, since we are using
     * ObjectTableModel, so the calling getDataVector doesn't
     * work as expected.
     * @return the data from the model
     */
    public static List<List<Object>> getAllTableData(ObjectTableModel model, Format[] formats) {
        List<List<Object>> data = new ArrayList<List<Object>>();
        if (model.getRowCount() > 0) {
            for (int rw=0; rw < model.getRowCount(); rw++) {
                int cols = model.getColumnCount();
                List<Object> column = new ArrayList<Object>();
                data.add(column);
                for (int idx=0; idx < cols; idx++) {
                    Object val = model.getValueAt(rw,idx);
                    if(formats[idx] != null) {
                    	                        column.add(formats[idx].format(val));
                    	                    } else {
                    	                        column.add(val);
                    	                    }
                }
            }
        }
        return data;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        boolean forceReloadData = false;
        final Object eventSource = event.getSource();
        if (eventSource == displayButton) {
            actionMakeGraph();
        } else if (eventSource == saveGraph) {
            saveGraphToFile = true;
            try {
                ActionRouter.getInstance().getAction(
                        ActionNames.SAVE_GRAPHICS,SaveGraphics.class.getName()).doAction(
                                new ActionEvent(this,event.getID(),ActionNames.SAVE_GRAPHICS));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        } else if (eventSource == saveTable) {
            JFileChooser chooser = FileDialoger.promptToSaveFile("statistics.csv");    //$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(chooser.getSelectedFile()); // TODO Charset ?
                CSVSaveService.saveCSVStats(getAllTableData(model, FORMATS),writer,saveHeaders.isSelected() ? COLUMNS : null);
            } catch (FileNotFoundException e) {
                JMeterUtils.reportErrorToUser(e.getMessage(), "Error saving data");
            } catch (IOException e) {
                JMeterUtils.reportErrorToUser(e.getMessage(), "Error saving data");
            } finally {
                JOrphanUtils.closeQuietly(writer);
            }
        } else if (eventSource == chooseForeColor) {
            Color color = JColorChooser.showDialog(
                    null,
                    JMeterUtils.getResString("aggregate_graph_choose_color"), //$NON-NLS-1$
                    colorBarGraph);
            if (color != null) {
                colorForeGraph = color;
            }
        } else if (eventSource == syncWithName) {
            graphTitle.setText(namePanel.getName());
        } else if (eventSource == dynamicGraphSize) {
            // if use dynamic graph size is checked, we disable the dimension fields
            if (dynamicGraphSize.isSelected()) {
                graphWidth.setEnabled(false);
                graphHeight.setEnabled(false);
            } else {
                graphWidth.setEnabled(true);
                graphHeight.setEnabled(true);
            }
        } else if (eventSource == statGraphVisualizerProduct2.getColumnSelection()) {
            if (statGraphVisualizerProduct2.getColumnSelection().isSelected()) {
                statGraphVisualizerProduct2.getColumnMatchLabel().setEnabled(true);
                statGraphVisualizerProduct2.getApplyFilterBtn().setEnabled(true);
                statGraphVisualizerProduct2.getCaseChkBox().setEnabled(true);
                statGraphVisualizerProduct2.getRegexpChkBox().setEnabled(true);
            } else {
                statGraphVisualizerProduct2.getColumnMatchLabel().setEnabled(false);
                statGraphVisualizerProduct2.getApplyFilterBtn().setEnabled(false);
                statGraphVisualizerProduct2.getCaseChkBox().setEnabled(false);
                statGraphVisualizerProduct2.getRegexpChkBox().setEnabled(false);
                // Force reload data
                forceReloadData = true;
            }
        }
        // Not 'else if' because forceReloadData 
        if (eventSource == statGraphVisualizerProduct2.getApplyFilterBtn() || forceReloadData) {
            if (statGraphVisualizerProduct2.getColumnSelection().isSelected() && statGraphVisualizerProduct2.getColumnMatchLabel().getText() != null
                    && statGraphVisualizerProduct2.getColumnMatchLabel().getText().length() > 0) {
                statGraphVisualizerProduct2.setPattern(statGraphVisualizerProduct2
						.createPattern(statGraphVisualizerProduct2.getColumnMatchLabel().getText()));
            } else if (forceReloadData) {
                statGraphVisualizerProduct2.setPattern(null);
                statGraphVisualizerProduct2.setMatcher(null);
            }
            if (getFile() != null && getFile().length() > 0) {
                clearData();
                FilePanel filePanel = (FilePanel) getFilePanel();
                filePanel.actionPerformed(event);
            }
        } else if (eventSource instanceof JButton) {
            // Changing color for column
            JButton btn = ((JButton) eventSource);
            if (btn.getName() != null) {
                try {
                    BarGraph bar = eltList.get(Integer.parseInt(btn.getName()));
                    Color color = JColorChooser.showDialog(null, bar.getLabel(), bar.getBackColor());
                    if (color != null) {
                        bar.setBackColor(color);
                        btn.setBackground(bar.getBackColor());
                    }
                } catch (NumberFormatException nfe) { } // nothing to do
            }
        }
    }

    private void actionMakeGraph() {
        if (model.getRowCount() > 1) {
            makeGraph();
            tabbedGraph.setSelectedIndex(1);
        } else {
            JOptionPane.showMessageDialog(null, JMeterUtils
                    .getResString("aggregate_graph_no_values_to_graph"), // $NON-NLS-1$
                    JMeterUtils.getResString("aggregate_graph_no_values_to_graph"), // $NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    @Override
    public JComponent getPrintableComponent() {
        if (saveGraphToFile == true) {
            saveGraphToFile = false;
            graphPanel.setBounds(graphPanel.getLocation().x,graphPanel.getLocation().y,
                    graphPanel.width,graphPanel.height);
            return graphPanel;
        }
        return this;
    }

    private JPanel createGraphActionsPane() {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel displayPane = new JPanel();
        displayPane.add(displayButton);
        displayButton.addActionListener(this);
        buttonPanel.add(displayPane, BorderLayout.WEST);

        JPanel savePane = new JPanel();
        savePane.add(saveGraph);
        savePane.add(saveTable);
        savePane.add(saveHeaders);
        saveGraph.addActionListener(this);
        saveTable.addActionListener(this);
        syncWithName.addActionListener(this);
        buttonPanel.add(savePane, BorderLayout.EAST);

        return buttonPanel;
    }

    private JPanel createGraphColumnPane() {
        JPanel colPanel = new JPanel();
        colPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JLabel label = new JLabel(JMeterUtils.getResString("aggregate_graph_columns_to_display")); //$NON-NLS-1$
        colPanel.add(label);
        for (BarGraph bar : eltList) {
            colPanel.add(bar.getChkBox());
            colPanel.add(createColorBarButton(bar, eltList.indexOf(bar)));
        }
        colPanel.add(Box.createRigidArea(new Dimension(5,0)));
        chooseForeColor.setFont(FONT_SMALL);
        colPanel.add(chooseForeColor);
        chooseForeColor.addActionListener(this);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        optionsPanel.add(statGraphVisualizerProduct3.createGraphFontValuePane());
        optionsPanel.add(drawOutlinesBar);
        optionsPanel.add(numberShowGrouping);
        optionsPanel.add(valueLabelsVertical);
        
        JPanel barPane = new JPanel(new BorderLayout());
        barPane.add(colPanel, BorderLayout.NORTH);
        barPane.add(Box.createRigidArea(new Dimension(0,3)), BorderLayout.CENTER);
        barPane.add(optionsPanel, BorderLayout.SOUTH);

        JPanel columnPane = new JPanel(new BorderLayout());
        columnPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_column_settings"))); // $NON-NLS-1$
        columnPane.add(barPane, BorderLayout.NORTH);
        columnPane.add(Box.createRigidArea(new Dimension(0,3)), BorderLayout.CENTER);
        columnPane.add(statGraphVisualizerProduct2.createGraphSelectionSubPane(this), BorderLayout.SOUTH);
        
        return columnPane;
    }

    private JButton createColorBarButton(BarGraph barGraph, int index) {
        // Button
        JButton colorBtn = new JButton();
        colorBtn.setName(String.valueOf(index));
        colorBtn.setFont(FONT_SMALL);
        colorBtn.addActionListener(this);
        colorBtn.setBackground(barGraph.getBackColor());
        return colorBtn;
    }

    private JPanel createGraphTitlePane() {
        JPanel titleNamePane = new JPanel(new BorderLayout());
        syncWithName.setFont(new Font("SansSerif", Font.PLAIN, 10));
        titleNamePane.add(graphTitle, BorderLayout.CENTER);
        titleNamePane.add(syncWithName, BorderLayout.EAST);
        
        JPanel titleStylePane = new JPanel();
        titleStylePane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_font"), //$NON-NLS-1$
                titleFontNameList));
        titleFontNameList.setSelectedIndex(0); // default: sans serif
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), //$NON-NLS-1$
                titleFontSizeList));
        titleFontSizeList.setSelectedItem(StatGraphProperties.fontSize[6]); // default: 16
        titleStylePane.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), //$NON-NLS-1$
                titleFontStyleList));
        titleFontStyleList.setSelectedItem(JMeterUtils.getResString("fontstyle.bold"));  // $NON-NLS-1$ // default: bold

        JPanel titlePane = new JPanel(new BorderLayout());
        titlePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_title_group"))); // $NON-NLS-1$
        titlePane.add(titleNamePane, BorderLayout.NORTH);
        titlePane.add(titleStylePane, BorderLayout.SOUTH);
        return titlePane;
    }

    private JPanel createGraphDimensionPane() {
        JPanel dimensionPane = new JPanel();
        dimensionPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dimensionPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_dimension"))); // $NON-NLS-1$

        dimensionPane.add(dynamicGraphSize);
        dynamicGraphSize.setSelected(true); // default option
        graphWidth.setEnabled(false);
        graphHeight.setEnabled(false);
        dynamicGraphSize.addActionListener(this);
        dimensionPane.add(Box.createRigidArea(new Dimension(10,0)));
        dimensionPane.add(graphWidth);
        dimensionPane.add(Box.createRigidArea(new Dimension(5,0)));
        dimensionPane.add(graphHeight);
        return dimensionPane;
    }

    /**
     * Create pane for X Axis options
     * @return X Axis pane
     */
    private JPanel createGraphXAxisPane() {
        JPanel xAxisPane = new JPanel();
        xAxisPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        xAxisPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_xaxis_group"))); // $NON-NLS-1$
        xAxisPane.add(maxLengthXAxisLabel);
        return xAxisPane;
    }

    /**
     * Create pane for Y Axis options
     * @return Y Axis pane
     */
    private JPanel createGraphYAxisPane() {
        JPanel yAxisPane = new JPanel();
        yAxisPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        yAxisPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("aggregate_graph_yaxis_group"))); // $NON-NLS-1$
        yAxisPane.add(maxValueYAxisLabel);
        return yAxisPane;
    }
}
