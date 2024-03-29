/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package burp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

import org.fife.ui.rsyntaxtextarea.DocumentRange;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.opencsv.CSVWriter;

import burp.Config.ConfigChangeListener;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.Registration;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.WebSocketMessageEditor;
import burp.api.montoya.websocket.*;
import burp.swing.*;
import java.awt.HeadlessException;
import java.io.FileNotFoundException;

/**
 *
 * @author NoF0rte
 */
public class FuzzTab extends javax.swing.JPanel implements MessageHandler, CloseableComponent {

    public static final String MARKER = "§";

    private MontoyaApi api;
    private int positionCount = 0;
    private List<Position> allPositions = new ArrayList<>();
    private BListTableModel payloadsModel = new BListTableModel();
    private ToServerModel toServerModel = new ToServerModel();
    private ToClientModel toClientModel = new ToClientModel();
    private WebSocketMessageEditor toServerViewer;
    private WebSocketMessageEditor toClientViewer;
    private Runner runner = null;
    private FuzzSettings settings;
    private Registration messageRegistration;
    private TableRowHighlighter cellRenderer;
    private boolean updatingToServer = false;
    private boolean clearingTables = false;
    private String lastExportedLocation = "";
    private String lastLoadedPayloadLocation = "";
    private int totalRequests = 0;
    private ConfigChangeListener configChangeListener;

    private Position getSelectedPosition() {
        int index = positionsComboBox.getSelectedIndex();
        if (index == -1) {
            return null;
        }

        return allPositions.get(index);
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
            if (i > allPositions.size() - 1) {
                position = new Position();
                allPositions.add(position);
            } else {
                position = allPositions.get(i);
            }

            position.setIndex(i);
            position.setStart(range.getStartOffset());
            position.setEnd(range.getEndOffset());

            if (range.getStartOffset() != text.length() - 1) {
                position.setName(text.substring(range.getStartOffset() + 1, range.getEndOffset() - 1));
            } else {
                position.setName("");
            }
        }

        updateHighlights(ranges);

        positionCount = ranges.size();

        int selectedIndex = positionsComboBox.getSelectedIndex();

        positionsComboBox.removeAllItems();
        for (int i = 0; i < positionCount; i++) {
            positionsComboBox.addItem(allPositions.get(i));
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

    private void addPayload(String item) {
        Position position = getSelectedPosition();
        if (position == null) {
            return;
        }

        position.getPayloads().add(item);
        payloadsModel.addRow(item);

        updateCountLabels();
    }

    private void removePayloadAt(int i) {
        Position position = getSelectedPosition();
        if (position == null) {
            return;
        }

        position.getPayloads().remove(i);
        payloadsModel.removeRow(i);

        updateCountLabels();
    }

    private void clearPayloads() {
        Position position = getSelectedPosition();
        if (position == null) {
            return;
        }

        position.getPayloads().clear();
        payloadsModel.clear();

        updateCountLabels();
    }

    private void selectedPositionChanged() {
        payloadsModel.clear();

        Position position = getSelectedPosition();
        if (position != null) {
            List<String> payloads = position.getPayloads();

            for (String payload : payloads) {
                payloadsModel.addRow(payload);
            }
        }

        updateCountLabels();
    }

    private void updateCountLabels() {
        payloadCountLabel.setText(Integer.toString(payloadsModel.getRowCount()));

        totalRequests = 0;
        for (int i = 0; i < positionCount; i++) {
            totalRequests += allPositions.get(i).getPayloads().size();
        }
        requestsCountLabel.setText(Integer.toString(totalRequests));
    }

    private void updateToClientHighlight(boolean scrollIntoView) {
        int serverSelectedRow = toServerTable.getSelectedRow();
        if (serverSelectedRow == -1) {
            return;
        }
        
        int serverRowNum = toServerTable.getRowSorter().convertRowIndexToModel(serverSelectedRow);

        // Find the row in the client table who is the closest time after the selected server row
        ToServerModel.Row serverRow = toServerModel.getRow(serverRowNum);
        ArrayList<ToClientModel.Row> allRows = toClientModel.getRows();
        int rowToHighlight = -1;
        ZoneOffset zone = ZoneOffset.of("Z");
        long minDiff = -1;
        for (int i = 0; i < allRows.size(); i++) {
            long diff = allRows.get(i).time.toInstant(zone).toEpochMilli() - serverRow.time.toInstant(zone).toEpochMilli();
            if (diff > 0 && (diff < minDiff || minDiff < 0)) {
                minDiff = diff;
                rowToHighlight = i;
            }
        }
        
        RowSorter<?> sorter = toClientTable.getRowSorter();
        cellRenderer.setHighlightRow(sorter.convertRowIndexToView(rowToHighlight));
        toClientTable.repaint();

        if (scrollIntoView && rowToHighlight != -1) {
            toClientTable.scrollRectToVisible(toClientTable.getCellRect(sorter.convertRowIndexToView(rowToHighlight), 1, true));
        }
    }

    private void startAttack() {
        if (!validateAttackSettings()) {
            return;
        }

        startPauseAttackBtn.setText("Pause");
        stopAttackBtn.setEnabled(true);
        messageEditor.setEnabled(false);
        addMarkerBtn.setEnabled(false);
        clearMarkersBtn.setEnabled(false);
        pastePayloadsBtn.setEnabled(false);
        loadPayloadsBtn.setEnabled(false);
        removePayloadBtn.setEnabled(false);
        clearPayloadsBtn.setEnabled(false);
        dedupePayloadsBtn.setEnabled(false);
        addPayloadBtn.setEnabled(false);
        delayTextField.setEnabled(false);
        exportTablesBtn.setEnabled(false);

        attackProgressBar.setValue(0);
        attackProgressBar.setMaximum(totalRequests);
        attackProgressLabel.setText(String.format("%d of %d", 0, totalRequests));

        runner = new Runner(settings);
        Thread thread = new Thread(runner);

        messageRegistration = settings.getSocket().registerMessageHandler(this);
        runner.setMessageListener(e -> {
            updatingToServer = true;
            toServerModel.addRow(e.getMessage(), e.getPosition(), e.getPayload());
            updatingToServer = false;

            int currentProgress = attackProgressBar.getValue() + 1;
            attackProgressBar.setValue(currentProgress);
            attackProgressLabel.setText(String.format("%d of %d", currentProgress, totalRequests));
        });
        runner.setDoneListener(() -> {
            messageRegistration.deregister();
            messageRegistration = null;

            runner = null;

            startPauseAttackBtn.setText("Start attack");

            stopAttackBtn.setEnabled(false);
            messageEditor.setEnabled(true);
            addMarkerBtn.setEnabled(true);
            clearMarkersBtn.setEnabled(true);
            pastePayloadsBtn.setEnabled(true);
            loadPayloadsBtn.setEnabled(true);
            removePayloadBtn.setEnabled(true);
            clearPayloadsBtn.setEnabled(true);
            dedupePayloadsBtn.setEnabled(true);
            addPayloadBtn.setEnabled(true);
            delayTextField.setEnabled(true);
            exportTablesBtn.setEnabled(true);
        });

        thread.start();
    }

    private void resumeAttack() {
        try {
            settings.setDelay(Integer.parseInt(delayTextField.getText()));
        } catch (NumberFormatException e) {
            Dialog.showError("Error parsing delay: " + e.getMessage());
            return;
        }

        startPauseAttackBtn.setText("Pause");
        delayTextField.setEnabled(false);
        exportTablesBtn.setEnabled(false);

        Thread thread = new Thread(runner);
        thread.start();
    }

    private void pauseAttack() {
        runner.pause();
        startPauseAttackBtn.setText("Resume");
        delayTextField.setEnabled(true);
        exportTablesBtn.setEnabled(true);
    }

    private boolean validateAttackSettings() {
        if (positionCount == 0) {
            Dialog.showError("Payload positions must be defined!");
            return false;
        }

        Position[] positions = new Position[positionCount];
        for (int i = 0; i < positionCount; i++) {
            Position position = allPositions.get(i);
            if (position.getPayloads().size() == 0) {
                Dialog.showError(String.format("No payloads defined for position %s ", position.toString()));
                return false;
            }

            positions[i] = position;
        }

        settings.setPositions(positions);
        settings.setMessage(messageEditor.getText());
        try {
            settings.setDelay(Integer.parseInt(delayTextField.getText()));
        } catch (NumberFormatException e) {
            Dialog.showError("Error parsing delay: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void populateCustomLists() {
        customListsComboBox.removeAllItems();

        customListsComboBox.addItem(new ListItem<File>("Add from list ...", null));
        for (File file : Config.get().getCustomLists()) {
            customListsComboBox.addItem(new ListItem<File>(file.getName(), file));
        }
    }

    public boolean close() {
        if (runner != null) {
            int result = JOptionPane.showConfirmDialog(null, "Close tab?", Extension.EXTENSION_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return false;
            }
            
            runner.stop();
        }

        Config.get().removeChangeListener(configChangeListener);

        return true;
    }

    public void updateSocket(WebSocket socket) {
        settings.setSocket(socket);
        if (messageRegistration != null) {
            if (messageRegistration.isRegistered()) {
                messageRegistration.deregister();
            }

            messageRegistration = socket.registerMessageHandler(this);
        }
    }

    public boolean isAttacking() {
        if (runner == null) {
            return false;
        }

        return runner.isRunning();
    }

    @Override
    public TextMessageAction handleTextMessage(TextMessage textMessage) {
        if (textMessage.direction() == Direction.SERVER_TO_CLIENT) {
            toClientModel.addRow(textMessage.payload());
        }
        return TextMessageAction.continueWith(textMessage);
    }

    @Override
    public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
        return BinaryMessageAction.continueWith(binaryMessage);
    }

    /**
     * Creates new form FuzzTab
     */
    public FuzzTab(MontoyaApi api, WebSocket socket, String url, TextMessage message) {
        this.api = api;
        this.settings = new FuzzSettings(api, socket);
        
        toServerViewer = api.userInterface().createWebSocketMessageEditor(EditorOptions.READ_ONLY);
        toClientViewer = api.userInterface().createWebSocketMessageEditor(EditorOptions.READ_ONLY);

        initComponents();

        // Doing this here instead of initComponents/NetBeans so that the designer shows the component
        jSplitPane3.setPreferredSize(new Dimension(0, 0));
        jSplitPane3.setDividerLocation(-1);

        targetTextField.setText(url);
        messageEditor.setText(Extension.SEND_REGEX.matcher(message.payload()).replaceAll(""));
        messageEditor.setCodeFoldingEnabled(true);

        payloadsModel.addColumn("TEMP");
        payloadsTable.setTableHeader(null);

        cellRenderer = new TableRowHighlighter();

        // Set the toServerViewer content every time a new row is selected
        toServerTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || updatingToServer || clearingTables) {
                return;
            }

            int serverRowNum = toServerTable.getRowSorter().convertRowIndexToModel(toServerTable.getSelectedRow());

            ToServerModel.Row serverRow = toServerModel.getRow(serverRowNum);
            toServerViewer.setContents(ByteArray.byteArray(serverRow.message));

            updateToClientHighlight(true);
        });
        toServerTable.getColumn("Message").setPreferredWidth(150);
        toServerTable.getColumn("Time").setPreferredWidth(150);

        toClientTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || clearingTables) {
                return;
            }

            int row = toClientTable.getRowSorter().convertRowIndexToModel(toClientTable.getSelectedRow());
            String msg = toClientModel.getRow(row).message;
            toClientViewer.setContents(ByteArray.byteArray(msg));
        });
        toClientTable.getColumn("Message").setPreferredWidth(150);
        toClientTable.getColumn("Time").setPreferredWidth(150);
        toClientTable.setDefaultRenderer(Object.class, cellRenderer);
        toClientTable.setDefaultRenderer(Integer.class, cellRenderer);
        toClientTable.getRowSorter().addRowSorterListener(e -> {
            SwingUtilities.invokeLater(() -> {
                updateToClientHighlight(false); // Whenever the table is sorted, we need to update which row is highlighted
            });
        });

        syntaxHighlightingComboBox.setModel(new DefaultComboBoxModel<>(new String[]{
            SyntaxConstants.SYNTAX_STYLE_NONE,
            SyntaxConstants.SYNTAX_STYLE_JSON,
            SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS,
            SyntaxConstants.SYNTAX_STYLE_XML,
            SyntaxConstants.SYNTAX_STYLE_CSV,
            SyntaxConstants.SYNTAX_STYLE_HTML,
            SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT,
            SyntaxConstants.SYNTAX_STYLE_SQL,
            SyntaxConstants.SYNTAX_STYLE_YAML
        }));
        syntaxHighlightingComboBox.setSelectedIndex(0);

        // Ensure the start attack panel is in the same place for every tab
        jTabbedPane1.addChangeListener(e -> {
            attackPanel.getParent().remove(attackPanel);

            java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();

            constraints.anchor = java.awt.GridBagConstraints.LINE_END;
            // constraints.insets = new java.awt.Insets(0, 0, 0, 5);
            constraints.gridy = 0;
            constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;

            int selected = jTabbedPane1.getSelectedIndex();
            if (selected == 0) { // Positions
                constraints.gridx = 2;
                positionsPanel.add(attackPanel, constraints);
            } else if (selected == 1) { // Payloads
                constraints.gridx = 2;
                payloadsPanel.add(attackPanel, constraints);
            } else if (selected == 2) { // Settings
                constraints.gridx = 1;
                settingsPanel.add(attackPanel, constraints);
            }
        });

        configChangeListener = new ConfigChangeListener() {
            @Override
            public void configChanged(String option) {
                if (option.equals(Config.CUSTOM_LISTS_DIR_PROPERTY)) {
                    populateCustomLists();
                }
            }
        };
        Config.get().addChangeListener(configChangeListener);

        populateCustomLists();
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel10 = new javax.swing.JPanel();
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
        attackPanel = new javax.swing.JPanel();
        stopAttackBtn = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        startPauseAttackBtn = new javax.swing.JButton();
        syntaxHighlightingComboBox = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
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
        customListsComboBox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        payloadsTable = new javax.swing.JTable();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jLabel16 = new javax.swing.JLabel();
        requestsCountLabel = new javax.swing.JLabel();
        settingsPanel = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        delayTextField = new javax.swing.JTextField();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jLabel13 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jPanel8 = new javax.swing.JPanel();
        clearTablesBtn = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        exportTablesBtn = new javax.swing.JButton();
        jSplitPane3 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        toServerTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        toServerViewContainer = new javax.swing.JPanel();
        jSplitPane4 = new javax.swing.JSplitPane();
        jPanel5 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        toClientTable = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        toClientViewerContainer = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        attackProgressLabel = new javax.swing.JLabel();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        attackProgressBar = new javax.swing.JProgressBar();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setResizeWeight(0.4);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jSplitPane1.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel10.setLayout(new java.awt.GridBagLayout());

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 10, 5));
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(0, 0));

        positionsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel1.setText("Payload positions");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        positionsPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel2.setText("Configure the positions where the payloads will be inserted.");
        jLabel2.setMaximumSize(new java.awt.Dimension(370, 20));
        jLabel2.setMinimumSize(new java.awt.Dimension(370, 20));
        jLabel2.setPreferredSize(new java.awt.Dimension(370, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        positionsPanel.add(jLabel2, gridBagConstraints);

        jLabel3.setText("URL:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        positionsPanel.add(jLabel3, gridBagConstraints);

        targetTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
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
        gridBagConstraints.gridwidth = 3;
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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        positionsPanel.add(jPanel1, gridBagConstraints);

        attackPanel.setLayout(new javax.swing.BoxLayout(attackPanel, javax.swing.BoxLayout.LINE_AXIS));

        stopAttackBtn.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        stopAttackBtn.setText("Stop");
        stopAttackBtn.setEnabled(false);
        stopAttackBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAttackBtnActionPerformed(evt);
            }
        });
        attackPanel.add(stopAttackBtn);
        attackPanel.add(filler4);

        startPauseAttackBtn.setBackground(new java.awt.Color(255, 102, 51));
        startPauseAttackBtn.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
        startPauseAttackBtn.setForeground(new java.awt.Color(255, 255, 255));
        startPauseAttackBtn.setText("Start attack");
        startPauseAttackBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startPauseAttackBtnActionPerformed(evt);
            }
        });
        attackPanel.add(startPauseAttackBtn);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        positionsPanel.add(attackPanel, gridBagConstraints);

        syntaxHighlightingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syntaxHighlightingComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        positionsPanel.add(syntaxHighlightingComboBox, gridBagConstraints);

        jLabel17.setText("Syntax Highlighting:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        positionsPanel.add(jLabel17, gridBagConstraints);

        jTabbedPane1.addTab("Positions", positionsPanel);

        payloadsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel4.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel4.setText("Payload sets");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        payloadsPanel.add(jLabel4, gridBagConstraints);

        jLabel5.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel5.setText("You can define one or more payload sets.");
        jLabel5.setMaximumSize(new java.awt.Dimension(363, 20));
        jLabel5.setMinimumSize(new java.awt.Dimension(363, 20));
        jLabel5.setPreferredSize(new java.awt.Dimension(363, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        payloadsPanel.add(jLabel5, gridBagConstraints);

        jLabel6.setText("Payload set: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        payloadsPanel.add(jLabel6, gridBagConstraints);

        positionsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionsComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        payloadsPanel.add(positionsComboBox, gridBagConstraints);

        jLabel7.setText("Payload count:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        payloadsPanel.add(jLabel7, gridBagConstraints);

        payloadCountLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        payloadsPanel.add(payloadCountLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        payloadsPanel.add(jSeparator1, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel8.setText("Payload settings");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        payloadsPanel.add(jLabel8, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("Cantarell", 0, 14)); // NOI18N
        jLabel9.setText("Configure a simple list of strings that are used as payloads.");
        jLabel9.setMaximumSize(new java.awt.Dimension(363, 20));
        jLabel9.setMinimumSize(new java.awt.Dimension(363, 20));
        jLabel9.setPreferredSize(new java.awt.Dimension(363, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
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

        payloadTextBox.setMaximumSize(new java.awt.Dimension(300, 25));
        payloadTextBox.setMinimumSize(new java.awt.Dimension(300, 25));
        payloadTextBox.setPreferredSize(new java.awt.Dimension(300, 25));
        payloadTextBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                payloadTextBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel2.add(payloadTextBox, gridBagConstraints);

        customListsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customListsComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(customListsComboBox, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(300, 80));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 80));

        payloadsTable.setModel(payloadsModel);
        payloadsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        payloadsTable.setShowGrid(false);
        jScrollPane1.setViewportView(payloadsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        payloadsPanel.add(filler2, gridBagConstraints);

        jLabel16.setText("Request count:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 0);
        payloadsPanel.add(jLabel16, gridBagConstraints);

        requestsCountLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 15, 0);
        payloadsPanel.add(requestsCountLabel, gridBagConstraints);

        jTabbedPane1.addTab("Payloads", payloadsPanel);

        settingsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel12.setText("Delay (ms):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        settingsPanel.add(jLabel12, gridBagConstraints);

        delayTextField.setText(Integer.toString(settings.getDelay()));
        delayTextField.setMaximumSize(new java.awt.Dimension(500, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        settingsPanel.add(delayTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        settingsPanel.add(filler3, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel13.setText("Timing");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        settingsPanel.add(jLabel13, gridBagConstraints);

        jTabbedPane1.addTab("Settings", settingsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        jPanel10.add(jTabbedPane1, gridBagConstraints);

        jSplitPane1.setLeftComponent(jPanel10);

        jPanel7.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.8;
        jPanel7.add(filler6, gridBagConstraints);

        jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.LINE_AXIS));

        clearTablesBtn.setText("Clear");
        clearTablesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearTablesBtnActionPerformed(evt);
            }
        });
        jPanel8.add(clearTablesBtn);
        jPanel8.add(filler5);

        exportTablesBtn.setText("Export");
        exportTablesBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportTablesBtnActionPerformed(evt);
            }
        });
        jPanel8.add(exportTablesBtn);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        jPanel7.add(jPanel8, gridBagConstraints);

        jSplitPane3.setDividerLocation(400);
        jSplitPane3.setResizeWeight(0.5);

        jSplitPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 20, 0));
        jSplitPane2.setDividerLocation(250);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setMinimumSize(new java.awt.Dimension(0, 0));
        jSplitPane2.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel10.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel10.setText("To Server");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        jPanel3.add(jLabel10, gridBagConstraints);

        toServerTable.setAutoCreateRowSorter(true);
        toServerTable.setModel(toServerModel);
        toServerTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        toServerTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        toServerTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(toServerTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel3.add(jScrollPane2, gridBagConstraints);

        jSplitPane2.setTopComponent(jPanel3);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jLabel11.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel11.setText("Content");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        jPanel4.add(jLabel11, gridBagConstraints);

        toServerViewContainer.setLayout(new java.awt.BorderLayout());

        toServerViewContainer.add(toServerViewer.uiComponent(), java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel4.add(toServerViewContainer, gridBagConstraints);

        jSplitPane2.setRightComponent(jPanel4);

        jSplitPane3.setLeftComponent(jSplitPane2);

        jSplitPane4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 20, 10));
        jSplitPane4.setDividerLocation(250);
        jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane4.setMinimumSize(new java.awt.Dimension(0, 0));
        jSplitPane4.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel5.setLayout(new java.awt.GridBagLayout());

        jLabel14.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel14.setText("To Client");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        jPanel5.add(jLabel14, gridBagConstraints);

        toClientTable.setAutoCreateRowSorter(true);
        toClientTable.setModel(toClientModel);
        toClientTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        toClientTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        toClientTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(toClientTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel5.add(jScrollPane3, gridBagConstraints);

        jSplitPane4.setTopComponent(jPanel5);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jLabel15.setFont(new java.awt.Font("Cantarell", 1, 16)); // NOI18N
        jLabel15.setText("Content");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        jPanel6.add(jLabel15, gridBagConstraints);

        toClientViewerContainer.setLayout(new java.awt.BorderLayout());

        toClientViewerContainer.add(toClientViewer.uiComponent(), java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel6.add(toClientViewerContainer, gridBagConstraints);

        jSplitPane4.setRightComponent(jPanel6);

        jSplitPane3.setRightComponent(jSplitPane4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.9;
        jPanel7.add(jSplitPane3, gridBagConstraints);

        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));

        attackProgressLabel.setText("0 of 0");
        jPanel9.add(attackProgressLabel);
        jPanel9.add(filler7);
        jPanel9.add(attackProgressBar);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 10);
        jPanel7.add(jPanel9, gridBagConstraints);

        jSplitPane1.setRightComponent(jPanel7);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
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
        String selectedText = messageEditor.getSelectedText();
        if (!selectedText.isEmpty()) {
            String after = selectedText.replace(MARKER, "");
            if (after != selectedText) {
                messageEditor.replaceRange(after, messageEditor.getSelectionStart(), messageEditor.getSelectionEnd());
            }
        } else {
            String before = messageEditor.getText();
            String after = before.replace(MARKER, "");
            if (after != before) {
                messageEditor.setText(after);
            }
        }
        
        updatePositions();

        messageEditor.grabFocus();
    }//GEN-LAST:event_clearMarkersBtnActionPerformed

    private void messageEditorKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messageEditorKeyTyped
        // Invoke later to ensure the newly typed character is in the text of the message editor
        SwingUtilities.invokeLater(() -> {
            updatePositions();
        });
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
        
        if (lastLoadedPayloadLocation != "") {
            fileChooser.setCurrentDirectory(new File(lastLoadedPayloadLocation));
        }

        int choice = fileChooser.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            lastLoadedPayloadLocation = fileChooser.getCurrentDirectory().toString();
            File file = fileChooser.getSelectedFile();

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();

                while (line != null) {
                    // Add line to list and model
                    addPayload(line);
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
        int selected = payloadsTable.getSelectedRow();
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

        addPayload(payload);
        payloadTextBox.setText("");
        payloadTextBox.grabFocus();
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
            clipboard.lines().forEach(item -> addPayload(item));
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
        LinkedHashSet<String> set = new LinkedHashSet<>(payloads);

        payloads.clear();
        payloadsModel.clear();

        set.stream().forEach(item -> {
            payloads.add(item);
            payloadsModel.addRow(item);
        });

        updateCountLabels();
    }//GEN-LAST:event_dedupePayloadsBtnActionPerformed

    private void startPauseAttackBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startPauseAttackBtnActionPerformed
        if (runner != null) {
            if (runner.isPaused()) {
                resumeAttack();
            } else if (runner.isRunning()) {
                pauseAttack();
            }
            return;
        }

        startAttack();
    }//GEN-LAST:event_startPauseAttackBtnActionPerformed

    private void stopAttackBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopAttackBtnActionPerformed
        if (runner == null) {
            return;
        }

        runner.stop();
    }//GEN-LAST:event_stopAttackBtnActionPerformed

    private void clearTablesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearTablesBtnActionPerformed
        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear to results tables?", Extension.EXTENSION_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        clearingTables = true;
        toClientModel.clear();
        toServerModel.clear();
        clearingTables = false;

        cellRenderer.setHighlightRow(-1);
        
        toServerViewer.setContents(ByteArray.byteArray(""));
        toClientViewer.setContents(ByteArray.byteArray(""));
    }//GEN-LAST:event_clearTablesBtnActionPerformed

    private void exportTablesBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportTablesBtnActionPerformed
        if (toServerTable.getRowCount() == 0 && toClientTable.getRowCount() == 0) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select the file to save");
        fileChooser.setCurrentDirectory(new File(lastExportedLocation != "" ? lastExportedLocation : System.getProperty("user.home")));
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setDragEnabled(false);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return true;
            }
            @Override
            public String getDescription() {
                return "CSV file (*.csv)";
            }
        });

        int result = fileChooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        lastExportedLocation = fileChooser.getCurrentDirectory().toString();
        String selectedFile = fileChooser.getSelectedFile().toString();
        if (!selectedFile.toLowerCase().endsWith(".csv")) {
            selectedFile = selectedFile + ".csv";
        }

        ArrayList<ExportRow> rows = new ArrayList<>();
        toServerModel.getRows().forEach(row -> {
            rows.add(new ExportRow(row));
        });
        toClientModel.getRows().forEach(row -> {
            rows.add(new ExportRow(row));
        });

        rows.sort((a, b) -> {
            return a.time.compareTo(b.time);
        });

        try {
            FileWriter outputFile = new FileWriter(selectedFile);
            CSVWriter writer = new CSVWriter(outputFile);

            writer.writeNext(new String[]{"Direction", "Position", "Message", "Payload", "Length", "Time"});
            writer.writeAll(rows.stream().map(row -> {
                return row.asArray();
            }).toList());

            writer.close();

            JOptionPane.showMessageDialog(null, String.format("Tables export to \"%s\"", selectedFile));
        } catch (HeadlessException | IOException e) {
            Dialog.showError("Error exporting tables: " + e.getMessage());
        }
    }//GEN-LAST:event_exportTablesBtnActionPerformed

    private void payloadTextBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_payloadTextBoxActionPerformed
        addPayloadBtnActionPerformed(evt);
    }//GEN-LAST:event_payloadTextBoxActionPerformed

    private void syntaxHighlightingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syntaxHighlightingComboBoxActionPerformed
        messageEditor.setSyntaxEditingStyle((String)syntaxHighlightingComboBox.getSelectedItem());
    }//GEN-LAST:event_syntaxHighlightingComboBoxActionPerformed

    private void customListsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customListsComboBoxActionPerformed
        if (customListsComboBox.getSelectedIndex() == -1) {
            return;
        }
        
        if (positionsComboBox.getSelectedIndex() == -1) {
            customListsComboBox.setSelectedIndex(0);
            return;
        }

        ListItem<File> item =  customListsComboBox.getItemAt(customListsComboBox.getSelectedIndex());
        if (item != null && item.getValue() != null) {
            try {
                FileReader reader = new FileReader(item.getValue());
                BufferedReader buffered = new BufferedReader(reader);

                buffered.lines().forEach(line -> {
                    addPayload(line);
                });
            } catch (FileNotFoundException e) {
                Dialog.showError("Error reading file: " + e.getMessage());
            }
        }

        customListsComboBox.setSelectedIndex(0);
    }//GEN-LAST:event_customListsComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMarkerBtn;
    private javax.swing.JButton addPayloadBtn;
    private javax.swing.JPanel attackPanel;
    private javax.swing.JProgressBar attackProgressBar;
    private javax.swing.JLabel attackProgressLabel;
    private javax.swing.JButton clearMarkersBtn;
    private javax.swing.JButton clearPayloadsBtn;
    private javax.swing.JButton clearTablesBtn;
    private javax.swing.JComboBox<ListItem<File>> customListsComboBox;
    private javax.swing.JButton dedupePayloadsBtn;
    private javax.swing.JTextField delayTextField;
    private javax.swing.JButton exportTablesBtn;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton loadPayloadsBtn;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea messageEditor;
    private javax.swing.JButton pastePayloadsBtn;
    private javax.swing.JLabel payloadCountLabel;
    private javax.swing.JTextField payloadTextBox;
    private javax.swing.JPanel payloadsPanel;
    private javax.swing.JTable payloadsTable;
    private javax.swing.JComboBox<Position> positionsComboBox;
    private javax.swing.JPanel positionsPanel;
    private org.fife.ui.rtextarea.RTextScrollPane rTextScrollPane1;
    private javax.swing.JButton removePayloadBtn;
    private javax.swing.JLabel requestsCountLabel;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JButton startPauseAttackBtn;
    private javax.swing.JButton stopAttackBtn;
    private javax.swing.JComboBox<String> syntaxHighlightingComboBox;
    private javax.swing.JTextField targetTextField;
    private javax.swing.JTable toClientTable;
    private javax.swing.JPanel toClientViewerContainer;
    private javax.swing.JTable toServerTable;
    private javax.swing.JPanel toServerViewContainer;
    // End of variables declaration//GEN-END:variables

    private class ExportRow {
        private final String position;
        private final String direction;
        private final String message;
        private final String payload;
        private final int length;
        private final LocalDateTime time;

        public ExportRow(ToClientModel.Row row) {
            this.position = "";
            this.direction = "To Client";
            this.message = row.message;
            this.payload = "";
            this.length = row.length;
            this.time = row.time;
        }
        public ExportRow(ToServerModel.Row row) {
            this.position = Integer.toString(row.position);
            this.direction = "To Server";
            this.message = row.message;
            this.payload = row.payload;
            this.length = row.length;
            this.time = row.time;
        }

        public String[] asArray(){
            return new String[]{
                direction,
                position,
                message,
                payload,
                Integer.toString(length),
                time.toString()
            };
        }
    }
}
