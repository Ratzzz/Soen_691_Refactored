package org.apache.jmeter.visualizers;


import javax.swing.JCheckBox;
import org.apache.jmeter.util.JMeterUtils;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import javax.swing.JPanel;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import java.awt.Dimension;
import java.io.Serializable;

public class StatGraphVisualizerProduct2 implements Serializable {
	private JCheckBox columnSelection = new JCheckBox(JMeterUtils.getResString("aggregate_graph_column_selection"),
			false);
	private JTextField columnMatchLabel = new JTextField();
	private JButton applyFilterBtn = new JButton(JMeterUtils.getResString("graph_apply_filter"));
	private JCheckBox caseChkBox = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_case"), false);
	private JCheckBox regexpChkBox = new JCheckBox(JMeterUtils.getResString("search_text_chkbox_regexp"), true);
	private Pattern pattern = null;
	private transient Matcher matcher = null;

	public JCheckBox getColumnSelection() {
		return columnSelection;
	}

	public void setColumnSelection(JCheckBox columnSelection) {
		this.columnSelection = columnSelection;
	}

	public JTextField getColumnMatchLabel() {
		return columnMatchLabel;
	}

	public void setColumnMatchLabel(JTextField columnMatchLabel) {
		this.columnMatchLabel = columnMatchLabel;
	}

	public JButton getApplyFilterBtn() {
		return applyFilterBtn;
	}

	public void setApplyFilterBtn(JButton applyFilterBtn) {
		this.applyFilterBtn = applyFilterBtn;
	}

	public JCheckBox getCaseChkBox() {
		return caseChkBox;
	}

	public void setCaseChkBox(JCheckBox caseChkBox) {
		this.caseChkBox = caseChkBox;
	}

	public JCheckBox getRegexpChkBox() {
		return regexpChkBox;
	}

	public void setRegexpChkBox(JCheckBox regexpChkBox) {
		this.regexpChkBox = regexpChkBox;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	/**
	* @param textToFind
	* @return  pattern ready to search
	*/
	public Pattern createPattern(String textToFind) {
		String textToFindQ = Pattern.quote(textToFind);
		if (regexpChkBox.isSelected()) {
			textToFindQ = textToFind;
		}
		Pattern pattern = null;
		try {
			if (caseChkBox.isSelected()) {
				pattern = Pattern.compile(textToFindQ);
			} else {
				pattern = Pattern.compile(textToFindQ, Pattern.CASE_INSENSITIVE);
			}
		} catch (PatternSyntaxException pse) {
			return null;
		}
		return pattern;
	}

	public JPanel createGraphSelectionSubPane(StatGraphVisualizer statGraphVisualizer) {
		Font font = new Font("SansSerif", Font.PLAIN, 10);
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
		searchPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		searchPanel.add(columnSelection);
		columnMatchLabel.setEnabled(false);
		applyFilterBtn.setEnabled(false);
		caseChkBox.setEnabled(false);
		regexpChkBox.setEnabled(false);
		columnSelection.addActionListener(statGraphVisualizer);
		searchPanel.add(columnMatchLabel);
		searchPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		applyFilterBtn.setFont(font);
		applyFilterBtn.addActionListener(statGraphVisualizer);
		searchPanel.add(applyFilterBtn);
		caseChkBox.setFont(font);
		searchPanel.add(caseChkBox);
		regexpChkBox.setFont(font);
		searchPanel.add(regexpChkBox);
		return searchPanel;
	}
}