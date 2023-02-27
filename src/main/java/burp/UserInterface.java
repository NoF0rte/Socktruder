package burp;

import burp.api.montoya.MontoyaApi;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class UserInterface {
	public static final int textHeight = new JTextField().getPreferredSize().height;

	public static void create(MontoyaApi api) {
		JPanel mainPanel = new JPanel();
		JPanel gridPanel = new JPanel();

		gridPanel.setLayout(new GridBagLayout());
		gridPanel.setPreferredSize(new Dimension(500, textHeight*8));
		gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gridPanel.setBorder(BorderFactory.createTitledBorder("Settings"));

		JTextField wordlistTextField = new JTextField();
		wordlistTextField.setPreferredSize(new Dimension(250, textHeight));

		if (Config.instance().wordlist() != null) {
			wordlistTextField.setText(Config.instance().wordlist());
		}

		JTextField fuzzKeywordTextField = new JTextField();
		fuzzKeywordTextField.setPreferredSize(new Dimension(250, textHeight));

		if (Config.instance().fuzzKeyword() != null) {
			fuzzKeywordTextField.setText(Config.instance().fuzzKeyword());
		}

		JTextField delayTextField = new JTextField();
		delayTextField.setPreferredSize(new Dimension(250, textHeight));

		if (Config.instance().delay() != null) {
			delayTextField.setText(String.format("%d", Config.instance().delay()));
		}

		JTextField successRegexTextField = new JTextField();
		successRegexTextField.setPreferredSize(new Dimension(250, textHeight));

		if (Config.instance().successRegex() != null) {
			successRegexTextField.setText(Config.instance().successRegex());
		}

		JLabel wordlistLabel = new JLabel("Wordlist: ");
		JLabel keywordLabel = new JLabel("Fuzz Keyword: ");
		JLabel delayLabel = new JLabel("Delay (ms): ");
		JLabel successLabel = new JLabel("Success Regex: ");

		JToggleButton enabledBtn = new JToggleButton();
		if (Config.instance().enabled()) {
			enabledBtn.setSelected(true);
			enabledBtn.setText("Enabled");
		} else {
			enabledBtn.setText("Disabled");
		}

		enabledBtn.addChangeListener(e -> {
			if (enabledBtn.isSelected()) {
				enabledBtn.setText("Enabled");
				Config.instance().setEnabled(true);
			} else {
				enabledBtn.setText("Disabled");
				Config.instance().setEnabled(false);
			}
		});

		Dimension buttonDimension = new Dimension(200, textHeight);
		enabledBtn.setPreferredSize(buttonDimension);
		enabledBtn.setMaximumSize(buttonDimension);
		enabledBtn.setMinimumSize(buttonDimension);

		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String wordlist = wordlistTextField.getText();
				if (wordlist != "") {
					Config.instance().setWordlist(wordlist);
				}

				String fuzzKeyword = fuzzKeywordTextField.getText();
				if (fuzzKeyword != "") {
					Config.instance().setFuzzKeyword(fuzzKeyword);
				}

				String successRegex = successRegexTextField.getText();
				if (successRegex != "") {
					Config.instance().setSuccessRegex(successRegex);
				}

				String delayText = delayTextField.getText();
				if (delayText != "") {
					try {
						int delay = Integer.parseInt(delayText);
						Config.instance().setDelay(delay);
					} catch (Exception ex) {
						api.logging().logToError(String.format("Error parsing delay: %s", ex.getMessage()));
					}
				}
			}
		});

		saveBtn.setPreferredSize(buttonDimension);
		saveBtn.setMaximumSize(buttonDimension);
		saveBtn.setMinimumSize(buttonDimension);

		JSeparator sep = new JSeparator();
		sep.setPreferredSize(buttonDimension);
		sep.setMaximumSize(buttonDimension);
		sep.setMinimumSize(buttonDimension);

		GridBagConstraints c = new GridBagConstraints();
		int tmpWidth = c.gridwidth;
		int tmpFill = c.fill;

		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		gridPanel.add(enabledBtn, c);

		c.gridy = 1;
		gridPanel.add(sep, c);

		c.gridwidth = tmpWidth;
		c.fill = tmpFill;
		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 2;
		gridPanel.add(wordlistLabel, c);
		c.gridy = 3;
		gridPanel.add(keywordLabel, c);
		c.gridy = 4;
		gridPanel.add(delayLabel, c);
		c.gridy = 5;
		gridPanel.add(successLabel, c);

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 2;
		gridPanel.add(wordlistTextField, c);
		c.gridy = 3;
		gridPanel.add(fuzzKeywordTextField, c);
		c.gridy = 4;
		gridPanel.add(delayTextField, c);
		c.gridy = 5;
		gridPanel.add(successRegexTextField, c);
		c.gridy = 6;
		gridPanel.add(saveBtn, c);

		mainPanel.add(gridPanel);

		api.userInterface().registerSuiteTab("WS Fuzzer", mainPanel);
	}
}
