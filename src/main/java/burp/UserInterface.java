package burp;

import burp.api.montoya.MontoyaApi;
import javax.swing.*;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class UserInterface {
	public static final int textHeight = new JTextField().getPreferredSize().height;
	
	private static DefaultTableModel fuzzTableModel;
	private static JTable fuzzTable;
	private static JTextField keywordTextField;
	private static JTextField wordlistTextField;
	private static JTextField successTextField;
	private static JTextField delayTextField;

	public static void create(MontoyaApi api) {
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new GridBagLayout());
		gridPanel.setPreferredSize(new Dimension(800, 500));
		gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gridPanel.setBorder(BorderFactory.createTitledBorder("Settings"));

		Dimension btnDimension = new Dimension(100, 25);
		Dimension textFieldDimension = new Dimension(250, 25);
		GridBagConstraints c = new GridBagConstraints();

		// Row 1

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
		enabledBtn.setPreferredSize(btnDimension);

		c.gridx = 0;
		c.gridy = 0;
		c.insets.bottom = 10;
		gridPanel.add(enabledBtn, c);

		// Row 2

		JLabel delayLabel = new JLabel("Delay (ms): ");

		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 1;
		gridPanel.add(delayLabel, c);

		delayTextField = new JTextField();
		delayTextField.setPreferredSize(textFieldDimension);

		if (Config.instance().delay() != null) {
			delayTextField.setText(String.format("%d", Config.instance().delay()));
		}

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		gridPanel.add(delayTextField, c);

		// Row 3

		JSeparator sep = new JSeparator();
		sep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 4;
		gridPanel.add(sep, c);

		// Row 4

		JLabel keywordLabel = new JLabel("Fuzz Keyword: ");

		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 3;
		c.insets.left = 0;
		gridPanel.add(keywordLabel, c);

		keywordTextField = new JTextField("[FUZZ]");
		keywordTextField.setPreferredSize(textFieldDimension);

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		gridPanel.add(keywordTextField, c);

		// Row 5

		JLabel wordlistLabel = new JLabel("Wordlist: ");

		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 4;
		c.insets.left = 0;
		gridPanel.add(wordlistLabel, c);

		wordlistTextField = new JTextField("/path/to/wordlist");
		wordlistTextField.setPreferredSize(textFieldDimension);

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		gridPanel.add(wordlistTextField, c);

		// Row 6

		JLabel successLabel = new JLabel("Success Regex: ");

		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 5;
		c.insets.left = 0;
		gridPanel.add(successLabel, c);

		successTextField = new JTextField();
		successTextField.setPreferredSize(textFieldDimension);

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		gridPanel.add(successTextField, c);

		JButton addBtn = new JButton("Add");
		addBtn.setPreferredSize(btnDimension);
		addBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String keyword = keywordTextField.getText();
				if (keyword == "") {
					return;
				}

				String wordlist = wordlistTextField.getText();
				if (wordlist == "") {
					return;
				}

				String success = successTextField.getText();
				
				Object[] row = {keyword, wordlist, success};
				fuzzTableModel.addRow(row);

				keywordTextField.setText("");
				wordlistTextField.setText("");
				successTextField.setText("");
			}
		});

		c.gridx = 2;
		c.anchor = GridBagConstraints.EAST;
		gridPanel.add(addBtn, c);

		// Row 7

		fuzzTableModel = new DefaultTableModel(){
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		}; 
		fuzzTableModel.addColumn("Keyword");
		fuzzTableModel.addColumn("Wordlist");
		fuzzTableModel.addColumn("Regex");

		if (Config.instance().fuzzList().size() > 0) {
			Config.instance().fuzzList().stream().forEach(x -> fuzzTableModel.addRow(x.toRow()));
		}

		fuzzTable = new JTable(fuzzTableModel);

		JScrollPane scrollPane = new JScrollPane(fuzzTable);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(600, 200));

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 3;
		gridPanel.add(scrollPane, c);

		JButton modifyBtn = new JButton("Modify");
		modifyBtn.setPreferredSize(btnDimension);
		modifyBtn.setMinimumSize(btnDimension);
		modifyBtn.setMaximumSize(btnDimension);
		modifyBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = fuzzTable.getSelectedRow();
				if (selectedRow == -1) {
					return;
				}

				keywordTextField.setText((String)fuzzTableModel.getValueAt(selectedRow, 0));
				wordlistTextField.setText((String)fuzzTableModel.getValueAt(selectedRow, 1));
				successTextField.setText((String)fuzzTableModel.getValueAt(selectedRow, 2));

				fuzzTableModel.removeRow(selectedRow);
			}
		});

		JButton deleteBtn = new JButton("Delete");
		deleteBtn.setPreferredSize(btnDimension);
		deleteBtn.setMinimumSize(btnDimension);
		deleteBtn.setMaximumSize(btnDimension);
		deleteBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = fuzzTable.getSelectedRow();
				if (selectedRow == -1) {
					return;
				}

				fuzzTableModel.removeRow(selectedRow);
			}
		});

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
		buttonPane.add(Box.createVerticalGlue());
		buttonPane.add(modifyBtn);
		buttonPane.add(Box.createRigidArea(new Dimension(0, 10)));
		buttonPane.add(deleteBtn);

		c.gridx = 3;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		gridPanel.add(buttonPane, c);

		// Row 8

		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<Config.Fuzz> fuzzItems = new ArrayList<>();

				int numRows = fuzzTableModel.getRowCount();
				for (int i = 0; i < numRows; i++) {
					Config.Fuzz fuzzItem = Config.instance().new Fuzz();
					fuzzItem.setKeyword((String)fuzzTableModel.getValueAt(i, 0));
					fuzzItem.setWordlist((String)fuzzTableModel.getValueAt(i, 1));
					fuzzItem.setSuccess((String)fuzzTableModel.getValueAt(i, 2));

					fuzzItems.add(fuzzItem);
				}

				Config.instance().setFuzzList(fuzzItems);

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
		saveBtn.setPreferredSize(btnDimension);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 2;
		c.gridy = 7;
		gridPanel.add(saveBtn, c);

		JPanel mainPanel = new JPanel();
		mainPanel.add(gridPanel);

		api.userInterface().registerSuiteTab("WS Fuzzer", mainPanel);
	}
}
