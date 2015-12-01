package org.apache.jmeter.visualizers;


import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jmeter.util.JMeterUtils;
import java.io.Serializable;

public class StatGraphVisualizerProduct3 implements Serializable {
	private JComboBox valueFontNameList = new JComboBox(StatGraphProperties.getFontNameMap().keySet().toArray());
	private JComboBox valueFontSizeList = new JComboBox(StatGraphProperties.fontSize);
	private JComboBox valueFontStyleList = new JComboBox(StatGraphProperties.getFontStyleMap().keySet().toArray());

	public JComboBox getValueFontNameList() {
		return valueFontNameList;
	}

	public void setValueFontNameList(JComboBox valueFontNameList) {
		this.valueFontNameList = valueFontNameList;
	}

	public JComboBox getValueFontSizeList() {
		return valueFontSizeList;
	}

	public void setValueFontSizeList(JComboBox valueFontSizeList) {
		this.valueFontSizeList = valueFontSizeList;
	}

	public JComboBox getValueFontStyleList() {
		return valueFontStyleList;
	}

	public void setValueFontStyleList(JComboBox valueFontStyleList) {
		this.valueFontStyleList = valueFontStyleList;
	}

	public JPanel createGraphFontValuePane() {
		JPanel fontValueStylePane = new JPanel();
		fontValueStylePane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		fontValueStylePane.add(
				GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_value_font"), valueFontNameList));
		valueFontNameList.setSelectedIndex(0);
		fontValueStylePane
				.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_size"), valueFontSizeList));
		valueFontSizeList.setSelectedItem(StatGraphProperties.fontSize[2]);
		fontValueStylePane
				.add(GuiUtils.createLabelCombo(JMeterUtils.getResString("aggregate_graph_style"), valueFontStyleList));
		valueFontStyleList.setSelectedItem(JMeterUtils.getResString("fontstyle.normal"));
		return fontValueStylePane;
	}
}