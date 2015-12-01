package org.apache.jmeter.visualizers;


import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import java.io.Serializable;

public class StatGraphVisualizerProduct implements Serializable {
	private JComboBox fontNameList = new JComboBox(StatGraphProperties.getFontNameMap().keySet().toArray());
	private JComboBox fontSizeList = new JComboBox(StatGraphProperties.fontSize);
	private JComboBox fontStyleList = new JComboBox(StatGraphProperties.getFontStyleMap().keySet().toArray());
	private JComboBox legendPlacementList = new JComboBox(StatGraphProperties.getPlacementNameMap().keySet().toArray());

	public JComboBox getFontNameList() {
		return fontNameList;
	}

	public void setFontNameList(JComboBox fontNameList) {
		this.fontNameList = fontNameList;
	}

	public JComboBox getFontSizeList() {
		return fontSizeList;
	}

	public void setFontSizeList(JComboBox fontSizeList) {
		this.fontSizeList = fontSizeList;
	}

	public JComboBox getFontStyleList() {
		return fontStyleList;
	}

	public void setFontStyleList(JComboBox fontStyleList) {
		this.fontStyleList = fontStyleList;
	}

	public JComboBox getLegendPlacementList() {
		return legendPlacementList;
	}

	public void setLegendPlacementList(JComboBox legendPlacementList) {
		this.legendPlacementList = legendPlacementList;
	}

	/**
	* Create pane for legend settings
	* @return  Legend pane
	*/
	public JPanel createLegendPane() {
		JPanel legendPanel = new JPanel();
		legendPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		legendPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				JMeterUtils.getResString("aggregate_graph_legend")));
		legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_legend_placement"),
				legendPlacementList));
		legendPlacementList.setSelectedItem(JMeterUtils.getResString("aggregate_graph_legend.placement.bottom"));
		legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_font"), fontNameList));
		fontNameList.setSelectedIndex(0);
		legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), fontSizeList));
		fontSizeList.setSelectedItem(StatGraphProperties.fontSize[2]);
		legendPanel.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), fontStyleList));
		fontStyleList.setSelectedItem(JMeterUtils.getResString("fontstyle.normal"));
		return legendPanel;
	}
}