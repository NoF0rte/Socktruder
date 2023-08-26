/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package burp;

import java.util.ArrayList;
import java.util.List;

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

    private void updateHighlights(List<DocumentRange> ranges) {
        payloadEditor.clearMarkAllHighlights();

        if (ranges.size() != 0) {
            payloadEditor.markAll(ranges);
        }
    }

    private List<DocumentRange> getPayloadRanges() {
        char markerChar = MARKER.charAt(0);
        
        int start = -1;
        ArrayList<DocumentRange> ranges = new ArrayList<>();
        char[] chars = payloadEditor.getText().toCharArray();
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

        String text = payloadEditor.getText();
        for (int i = 0; i < ranges.size(); i++) {
            DocumentRange range = ranges.get(i);

            Position position = null;
            if (i > positions.size() - 1) {
                position = new Position();
                positions.add(position);
            } else {
                position = positions.get(i);
            }

            position.setStart(range.getStartOffset());
            position.setEnd(range.getEndOffset());
            position.setName(text.substring(range.getStartOffset() + 1, range.getEndOffset() - 1));
        }

        positionCount = ranges.size();
        updateHighlights(ranges);
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
        this.payloadEditor.setText(message.payload().replace(Config.SEND_KEYWORD, ""));
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
        payloadEditor = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        javax.swing.UIManager.put("RSyntaxTextAreaUI.actionMap", null);
        javax.swing.UIManager.put("RSyntaxTextAreaUI.inputMap", null);
        javax.swing.UIManager.put("RTextAreaUI.inputMap", null);
        javax.swing.UIManager.put("RTextAreaUI.actionMap", null);
        jPanel1 = new javax.swing.JPanel();
        addBtn = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10));
        clearBtn = new javax.swing.JButton();
        payloadsPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        payloadSetComboBox = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        payloadCountLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        pasteBtn = new javax.swing.JButton();
        loadBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        clearPayloadItemsBtn = new javax.swing.JButton();
        dedupeBtn = new javax.swing.JButton();
        addPayloadItemBtn = new javax.swing.JButton();
        payloadTextBox = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        payloadList = new javax.swing.JList<>();
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

        payloadEditor.setColumns(20);
        payloadEditor.setRows(5);
        payloadEditor.setCurrentLineHighlightColor(new java.awt.Color(240, 240, 240));
        payloadEditor.setMarkAllHighlightColor(new java.awt.Color(200, 241, 230));
        payloadEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                payloadEditorKeyTyped(evt);
            }
        });
        rTextScrollPane1.setViewportView(payloadEditor);

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

        addBtn.setText("Add §");
        addBtn.setMaximumSize(new java.awt.Dimension(80, 25));
        addBtn.setMinimumSize(new java.awt.Dimension(80, 25));
        addBtn.setPreferredSize(new java.awt.Dimension(80, 25));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });
        jPanel1.add(addBtn);
        jPanel1.add(filler1);

        clearBtn.setText(" Clear §");
        clearBtn.setMaximumSize(new java.awt.Dimension(80, 25));
        clearBtn.setMinimumSize(new java.awt.Dimension(80, 25));
        clearBtn.setPreferredSize(new java.awt.Dimension(80, 25));
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });
        jPanel1.add(clearBtn);

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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        payloadsPanel.add(payloadSetComboBox, gridBagConstraints);

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

        pasteBtn.setText("Paste");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(pasteBtn, gridBagConstraints);

        loadBtn.setText("Load ...");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(loadBtn, gridBagConstraints);

        removeBtn.setText("Remove");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(removeBtn, gridBagConstraints);

        clearPayloadItemsBtn.setText("Clear");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(clearPayloadItemsBtn, gridBagConstraints);

        dedupeBtn.setText("Deduplicate");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 30, 0);
        jPanel2.add(dedupeBtn, gridBagConstraints);

        addPayloadItemBtn.setText("Add");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(addPayloadItemBtn, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(payloadTextBox, gridBagConstraints);

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

        jTabbedPane1.addTab("Payloads", payloadsPanel);

        settingsPanel.setLayout(new java.awt.GridBagLayout());
        jTabbedPane1.addTab("Settings", settingsPanel);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        if (payloadEditor.getSelectedText() != null) {
            int start = payloadEditor.getSelectionStart();
            int end = payloadEditor.getSelectionEnd();

            payloadEditor.insert(MARKER, start);
            payloadEditor.insert(MARKER, end + 1);

            payloadEditor.setCaretPosition(end + 2);
        } else {
            payloadEditor.insert(MARKER, payloadEditor.getCaretPosition());
        }

        updatePositions();
        payloadEditor.grabFocus();
    }//GEN-LAST:event_addBtnActionPerformed

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        String before = payloadEditor.getText();
        String after = before.replace(MARKER, "");
        if (after != before) {
            payloadEditor.setText(after);
        }
        updatePositions();

        payloadEditor.grabFocus();
    }//GEN-LAST:event_clearBtnActionPerformed

    private void payloadEditorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_payloadEditorKeyTyped
        updatePositions();
    }//GEN-LAST:event_payloadEditorKeyTyped


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JButton addPayloadItemBtn;
    private javax.swing.JButton clearBtn;
    private javax.swing.JButton clearPayloadItemsBtn;
    private javax.swing.JButton dedupeBtn;
    private javax.swing.Box.Filler filler1;
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
    private javax.swing.JButton loadBtn;
    private javax.swing.JButton pasteBtn;
    private javax.swing.JLabel payloadCountLabel;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea payloadEditor;
    private javax.swing.JList<String> payloadList;
    private javax.swing.JComboBox<String> payloadSetComboBox;
    private javax.swing.JTextField payloadTextBox;
    private javax.swing.JPanel payloadsPanel;
    private javax.swing.JPanel positionsPanel;
    private org.fife.ui.rtextarea.RTextScrollPane rTextScrollPane1;
    private javax.swing.JButton removeBtn;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JTextField targetTextField;
    // End of variables declaration//GEN-END:variables
}
