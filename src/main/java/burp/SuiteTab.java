/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package burp;

import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.TextMessage;
import burp.api.montoya.websocket.WebSocket;

/**
 *
 * @author parallels
 */
public class SuiteTab extends javax.swing.JPanel {

    private static SuiteTab instance;

    public static SuiteTab get() {
        return instance;
    }

    public static void set(SuiteTab tab) {
        instance = tab;
    }

    private MontoyaApi api;

    /**
     * Creates new form SuiteTab
     */
    public SuiteTab(MontoyaApi api) {
        this.api = api;

        initComponents();

        fuzzTabbedPane.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent arg0) {
                if (fuzzTabbedPane.getTabCount() == 0) {
                    instructionsPanel.setVisible(false);
                }
            }
            @Override
            public void componentRemoved(ContainerEvent arg0) {
                if (fuzzTabbedPane.getTabCount() == 0) {
                    instructionsPanel.setVisible(true);
                }
            }
        });

        fuzzTabbedPane.enableClosableTabs();
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

        fuzzTabbedPane = new burp.ui.BTabbedPane();
        instructionsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new javax.swing.OverlayLayout(this));
        add(fuzzTabbedPane);

        instructionsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Cantarell", 1, 36)); // NOI18N
        jLabel1.setText(Extension.EXTENSION_NAME);
        instructionsPanel.add(jLabel1, new java.awt.GridBagConstraints());

        jLabel2.setFont(new java.awt.Font("Cantarell", 0, 18)); // NOI18N
        jLabel2.setText("To create a tab, send a WebSocket message in Repeater containing the words " + Extension.SEND_KEYWORD);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        instructionsPanel.add(jLabel2, gridBagConstraints);

        add(instructionsPanel);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private burp.ui.BTabbedPane fuzzTabbedPane;
    private javax.swing.JPanel instructionsPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    public void addFuzzTab(WebSocket socket, String url, TextMessage message) {
        FuzzTab tab = new FuzzTab(api, socket, url, message);
        fuzzTabbedPane.addTab(tab);
    }
}
