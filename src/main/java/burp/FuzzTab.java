/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package burp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

import org.fife.ui.rsyntaxtextarea.DocumentRange;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.Direction;
import burp.api.montoya.websocket.TextMessage;
import burp.api.montoya.websocket.WebSocket;

/**
 *
 * @author kali
 */
public class FuzzTab extends javax.swing.JPanel {

    private final String MARKER = "§";

    private MontoyaApi api;
    private WebSocket socket;
    private Direction direction;
    private List<Position> positions = new ArrayList<>();
    private int positionCount = 0;
    private DefaultListModel<String> payloadsModel = new DefaultListModel<>();

    private Position getSelectedPosition() {
        int index = positionsComboBox.getSelectedIndex();
        if (index == -1) {
            return null;
        }

        return positions.get(index);
    }

    private void updateHighlights(List<DocumentRange> ranges) {
        messageEditor.clearMarkAllHighlights();

        if (ranges.size() != 0) {
            messageEditor.markAll(ranges);
        }
    }

    private List<DocumentRange> getPayloadRanges() {
        char markerChar = MARKER.charAt(0);
        
        int start = -1;
        ArrayList<DocumentRange> ranges = new ArrayList<>();
        char[] chars = messageEditor.getText().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c != markerChar) {
                continue;
            }

            if (start == -1) {
                start = i;
            } else {
                ranges.add(new DocumentRange(start, i + 1));
                start = -1;
            }
        }
        if (start != -1) {
            ranges.add(new DocumentRange(start, chars.length));
        }

        return ranges;
    }

    private void updatePositions() {
        List<DocumentRange> ranges = getPayloadRanges();

        String text = messageEditor.getText();
        for (int i = 0; i < ranges.size(); i++) {
            DocumentRange range = ranges.get(i);

            Position position = null;
            if (i > positions.size() - 1) {
                position = new Position();
                positions.add(position);
            } else {
                position = positions.get(i);
            }

            position.setIndex(i);
            position.setStart(range.getStartOffset());
            position.setEnd(range.getEndOffset());
            position.setName(text.substring(range.getStartOffset() + 1, range.getEndOffset() - 1));
        }

        updateHighlights(ranges);

        positionCount = ranges.size();

        int selectedIndex = positionsComboBox.getSelectedIndex();

        positionsComboBox.removeAllItems();
        for (int i = 0; i < positionCount; i++) {
            positionsComboBox.addItem(positions.get(i));
        }

        if (positionCount == 0) {
            selectedIndex = -1;
        } else if (selectedIndex > positionCount - 1) {
            selectedIndex = positionCount - 1;
        } else if (selectedIndex == -1) {
            selectedIndex = 0;
        }

        positionsComboBox.setSelectedIndex(selectedIndex);
        selectedPositionChanged();
    }

    private void addToPayloadList(String item) {
        Position position = getSelectedPosition();
        if (position == null) {
            return;
        }

        position.getPayloads().add(item);
        payloadsModel.addElement(item);

        updateCountLabel();
    }

    private void removePayloadAt(int i) {
        Position position = getSelectedPosition();
        if (position == null) {
            return;
        }

        position.getPayloads().remove(i);
        payloadsModel.removeElementAt(i);

        updateCountLabel();
    }

    private void clearPayloads() {
        Position position = getSelectedPosition();
        if (position == null) {
            return;
        }

        position.getPayloads().clear();
        payloadsModel.clear();

        updateCountLabel();
    }

    private void selectedPositionChanged() {
        payloadsModel.clear();

        Position position = getSelectedPosition();
        if (position != null) {
            List<String> payloads = position.getPayloads();

            payloadsModel.addAll(payloads);
        }

        updateCountLabel();
    }

    private void updateCountLabel() {
        payloadCountLabel.setText(Integer.toString(payloadsModel.size()));
    }

    /**
     * Creates new form FuzzTab
     */
    public FuzzTab(MontoyaApi api, WebSocket socket, String url, TextMessage message) {
        this.api = api;
        this.socket = socket;
        this.direction = message.direction();

        initComponents();

        targetTextField.setText(url);
        messageEditor.setText(message.payload().replace(Config.SEND_KEYWORD, ""));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane1 = new javax.swing.JTabbedPane();
        positionsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        targetTextField = new javax.swing.JTextField();
        rTextScrollPane1 = new org.fife.ui.rtextarea.RTextScrollPane();
        javax.swing.text.JTextComponent.removeKeymap("RTextAreaKeymap");
        messageEditor = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        javax.swing.UIManager.put("RSyntaxTextAreaUI.actionMap", null);
        javax.swing.UIManager.put("RSyntaxTextAreaUI.inputMap", null);
        javax.swing.UIManager.put("RTextAreaUI.inputMap", null);
        javax.swing.UIManager.put("RTextAreaUI.actionMap", null);
        jPanel1 = new javax.swing.JPanel();
        addMarkerBtn = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        clearMarkersBtn = new javax.swing.JButton();
        payloadsPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        positionsComboBox = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        payloadCountLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        pastePayloadsBtn = new javax.swing.JButton();
        loadPayloadsBtn = new javax.swing.JButton();
        removePayloadBtn = new javax.swing.JButton();
        clearPayloadsBtn = new javax.swing.JButton();
        dedupePayloadsBtn = new javax.swing.JButton();
        addPayloadBtn = new javax.swing.JButton();
        payloadTextBox = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        payloadList = new javax.swing.JList<>();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        settingsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 20, 5));

        positionsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel1.setText("Payload positions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 0);
        positionsPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel2.setText("Configure the positions where the payloads will be inserted");
        jLabel2.setMaximumSize(new java.awt.Dimension(363, 20));
        jLabel2.setMinimumSize(new java.awt.Dimension(363, 20));
        jLabel2.setPreferredSize(new java.awt.Dimension(363, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        positionsPanel.add(jLabel2, gridBagConstraints);

        jLabel3.setText("URL:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        positionsPanel.add(jLabel3, gridBagConstraints);

        targetTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionsPanel.add(targetTextField, gridBagConstraints);

        messageEditor.setColumns(20);
        messageEditor.setRows(5);
        messageEditor.setCurrentLineHighlightColor(new java.awt.Color(240, 240, 240));
        messageEditor.setMarkAllHighlightColor(new java.awt.Color(200, 241, 230));
        messageEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                messageEditorKeyTyped(evt);
            }
        });
        rTextScrollPane1.setViewportView(messageEditor);

        rTextScrollPane1.setLineNumbersEnabled(true);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        positionsPanel.add(rTextScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        addMarkerBtn.setText("Add §");
        addMarkerBtn.setMaximumSize(new java.awt.Dimension(80, 25));
        addMarkerBtn.setMinimumSize(new java.awt.Dimension(80, 25));
        addMarkerBtn.setPreferredSize(new java.awt.Dimension(80, 25));
        addMarkerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMarkerBtnActionPerformed(evt);
            }
        });
        jPanel1.add(addMarkerBtn);
        jPanel1.add(filler1);

        clearMarkersBtn.setText(" Clear §");
        clearMarkersBtn.setMaximumSize(new java.awt.Dimension(80, 25));
        clearMarkersBtn.setMinimumSize(new java.awt.Dimension(80, 25));
        clearMarkersBtn.setPreferredSize(new java.awt.Dimension(80, 25));
        clearMarkersBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearMarkersBtnActionPerformed(evt);
            }
        });
        jPanel1.add(clearMarkersBtn);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionsPanel.add(jPanel1, gridBagConstraints);

        jTabbedPane1.addTab("Positions", positionsPanel);

        payloadsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel4.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel4.setText("Payload sets");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 0);
        payloadsPanel.add(jLabel4, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel5.setText("You can define one or more payload sets.");
        jLabel5.setMaximumSize(new java.awt.Dimension(363, 20));
        jLabel5.setMinimumSize(new java.awt.Dimension(363, 20));
        jLabel5.setPreferredSize(new java.awt.Dimension(363, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        payloadsPanel.add(jLabel5, gridBagConstraints);

        jLabel6.setText("Payload set: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        payloadsPanel.add(jLabel6, gridBagConstraints);

        positionsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionsComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        payloadsPanel.add(positionsComboBox, gridBagConstraints);

        jLabel7.setText("Payload count:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        payloadsPanel.add(jLabel7, gridBagConstraints);

        payloadCountLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        payloadsPanel.add(payloadCountLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        payloadsPanel.add(jSeparator1, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel8.setText("Payload settings");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 0);
        payloadsPanel.add(jLabel8, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel9.setText("Configure a simple list of strings that are used as payloads");
        jLabel9.setMaximumSize(new java.awt.Dimension(363, 20));
        jLabel9.setMinimumSize(new java.awt.Dimension(363, 20));
        jLabel9.setPreferredSize(new java.awt.Dimension(363, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        payloadsPanel.add(jLabel9, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        pastePayloadsBtn.setText("Paste");
        pastePayloadsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pastePayloadsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(pastePayloadsBtn, gridBagConstraints);

        loadPayloadsBtn.setText("Load ...");
        loadPayloadsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadPayloadsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(loadPayloadsBtn, gridBagConstraints);

        removePayloadBtn.setText("Remove");
        removePayloadBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePayloadBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(removePayloadBtn, gridBagConstraints);

        clearPayloadsBtn.setText("Clear");
        clearPayloadsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearPayloadsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(clearPayloadsBtn, gridBagConstraints);

        dedupePayloadsBtn.setText("Deduplicate");
        dedupePayloadsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dedupePayloadsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 30, 0);
        jPanel2.add(dedupePayloadsBtn, gridBagConstraints);

        addPayloadBtn.setText("Add");
        addPayloadBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPayloadBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(addPayloadBtn, gridBagConstraints);

        payloadTextBox.setMaximumSize(new java.awt.Dimension(260, 25));
        payloadTextBox.setMinimumSize(new java.awt.Dimension(260, 25));
        payloadTextBox.setPreferredSize(new java.awt.Dimension(260, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(payloadTextBox, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(258, 130));

        payloadList.setModel(payloadsModel);
        payloadList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        payloadList.setFixedCellWidth(258);
        jScrollPane1.setViewportView(payloadList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        payloadsPanel.add(jPanel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        payloadsPanel.add(filler2, gridBagConstraints);

        jTabbedPane1.addTab("Payloads", payloadsPanel);

        settingsPanel.setLayout(new java.awt.GridBagLayout());
        jTabbedPane1.addTab("Settings", settingsPanel);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void addMarkerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMarkerBtnActionPerformed
        if (messageEditor.getSelectedText() != null) {
            int start = messageEditor.getSelectionStart();
            int end = messageEditor.getSelectionEnd();

            messageEditor.insert(MARKER, start);
            messageEditor.insert(MARKER, end + 1);

            messageEditor.setCaretPosition(end + 2);
        } else {
            messageEditor.insert(MARKER, messageEditor.getCaretPosition());
        }

        updatePositions();
        messageEditor.grabFocus();
    }//GEN-LAST:event_addMarkerBtnActionPerformed

    private void clearMarkersBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMarkersBtnActionPerformed
        String before = messageEditor.getText();
        String after = before.replace(MARKER, "");
        if (after != before) {
            messageEditor.setText(after);
        }
        updatePositions();

        messageEditor.grabFocus();
    }//GEN-LAST:event_clearMarkersBtnActionPerformed

    private void messageEditorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messageEditorKeyTyped
        updatePositions();
    }//GEN-LAST:event_messageEditorKeyTyped

    private void loadPayloadsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadPayloadsBtnActionPerformed
        Position position = getSelectedPosition();
        if (position == null) {
            return;
        }
        int selectedIndex = positionsComboBox.getSelectedIndex();
        if (selectedIndex == -1) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setDragEnabled(false);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileHidingEnabled(true);

        int choice = fileChooser.showOpenDialog(api.userInterface().swingUtils().suiteFrame());
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();

                while (line != null) {
                    // Add line to list and model
                    addToPayloadList(line);
                    line = reader.readLine();
                }

                reader.close();
            } catch (IOException e) {
                api.logging().logToError(e);
            }
        }
    }//GEN-LAST:event_loadPayloadsBtnActionPerformed

    private void clearPayloadsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearPayloadsBtnActionPerformed
        clearPayloads();
    }//GEN-LAST:event_clearPayloadsBtnActionPerformed

    private void removePayloadBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePayloadBtnActionPerformed
        int selected = payloadList.getSelectedIndex();
        if (selected == -1) {
            return;
        }

        removePayloadAt(selected);
    }//GEN-LAST:event_removePayloadBtnActionPerformed

    private void addPayloadBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPayloadBtnActionPerformed
        if (positionsComboBox.getSelectedIndex() == -1) {
            return;
        }

        String payload = payloadTextBox.getText().trim();
        if (payload.isBlank() || payload.isEmpty()) {
            return;
        }

        addToPayloadList(payload);
        payloadTextBox.setText("");
    }//GEN-LAST:event_addPayloadBtnActionPerformed

    private void positionsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionsComboBoxActionPerformed
        selectedPositionChanged();
    }//GEN-LAST:event_positionsComboBoxActionPerformed

    private void pastePayloadsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pastePayloadsBtnActionPerformed
        if (positionsComboBox.getSelectedIndex() == -1) {
            return;
        }

        try {
            String clipboard = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            clipboard.lines().forEach(item -> addToPayloadList(item));
        } catch (Exception e) {
            api.logging().logToError(e);
        }
    }//GEN-LAST:event_pastePayloadsBtnActionPerformed

    private void dedupePayloadsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dedupePayloadsBtnActionPerformed
        Position position = getSelectedPosition();
        if (position == null) {
            return;
        }

        List<String> payloads = position.getPayloads();
        HashSet<String> set = new HashSet<>(payloads);

        payloads.clear();
        payloadsModel.clear();

        set.stream().forEach(item -> {
            payloads.add(item);
            payloadsModel.addElement(item);
        });

        updateCountLabel();
    }//GEN-LAST:event_dedupePayloadsBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMarkerBtn;
    private javax.swing.JButton addPayloadBtn;
    private javax.swing.JButton clearMarkersBtn;
    private javax.swing.JButton clearPayloadsBtn;
    private javax.swing.JButton dedupePayloadsBtn;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton loadPayloadsBtn;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea messageEditor;
    private javax.swing.JButton pastePayloadsBtn;
    private javax.swing.JLabel payloadCountLabel;
    private javax.swing.JList<String> payloadList;
    private javax.swing.JTextField payloadTextBox;
    private javax.swing.JPanel payloadsPanel;
    private javax.swing.JComboBox<Position> positionsComboBox;
    private javax.swing.JPanel positionsPanel;
    private org.fife.ui.rtextarea.RTextScrollPane rTextScrollPane1;
    private javax.swing.JButton removePayloadBtn;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JTextField targetTextField;
    // End of variables declaration//GEN-END:variables
}
