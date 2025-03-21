package com.jmpeax.ssltoolbox.jks;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.JBUI;
import com.jmpeax.ssltoolbox.pem.PemView;
import com.jmpeax.ssltoolbox.svc.CertificateHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JKSView extends JPanel {

    private JBList<String> list;
    private PemView pemView;
    private final VirtualFile file;
    private final JPanel listPanel;
    private final JPanel pemViewPanel;
    private JBPasswordField passwordField;
    private final DefaultListModel<String> listModel ;
    private Map<String, X509Certificate> certs;

    public JKSView(@NotNull VirtualFile file) {
        super(new GridBagLayout());
        this.file = file;
        this.listModel = new DefaultListModel<>();
        this.certs = Map.of();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.BOTH;

        // Add unlock button at the top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        add(createUnlockButton(), gbc);

        // Add certificate list panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.45;
        gbc.weighty = 1.0;
        listPanel = createListPanel();
        add(listPanel, gbc);

        // Add PEM view panel
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.55;
        pemViewPanel = createPemViewPanel();
        add(pemViewPanel, gbc);
    }

    private void loadPemView(X509Certificate certificate) {
        if (pemView != null) {
            pemViewPanel.remove(pemView);
        }
        pemView = new PemView(certificate,true);
        pemViewPanel.removeAll();
        pemViewPanel.add(pemView, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(labelPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPemViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUnlockButton() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        this.passwordField = new JBPasswordField();
        panel.setBorder(JBUI.Borders.customLineBottom(JBUI.CurrentTheme.Toolbar.SEPARATOR_COLOR));
        JButton unlockButton = getButton(passwordField, panel);
        JBLabel passwordLabel = new JBLabel("Enter password: ");
        passwordField.requestFocusInWindow();

        // Set focus traversal keys for the password field to move to the unlock button
        Set<AWTKeyStroke> forwardKeys = new HashSet<>(passwordField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        passwordField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
        unlockButton.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());

        // Add password label (20%)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        panel.add(passwordLabel, gbc);

        // Add password field (60%)
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        panel.add(passwordField, gbc);

        // Add unlock button (20%)
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        panel.add(unlockButton, gbc);

        return panel;
    }

    private @NotNull JButton getButton(JPasswordField passwordField, JPanel panel) {
        JButton unlockButton = new JButton("Unlock Certificate Store");
        unlockButton.addActionListener(e -> {
            char[] password = passwordField.getPassword();
            if (password != null) {
                try {
                    var certificateHelper = ApplicationManager.getApplication().getService(CertificateHelper.class);
                    this.certs = certificateHelper.getKeyStoreCerts(file.getInputStream(), password);
                    addCertificate(certs);
                    panel.removeAll();
                    panel.add(buildToolBar());
                    revalidate();
                    repaint();
                } catch (IOException ex) {
                    throw new RuntimeException("Error reading JKS file", ex);
                }
            }
        });
        return unlockButton;
    }

    private JPanel buildToolBar() {
        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("JKS-Actions");
        ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("JKS-Actions-Toolbar", actionGroup, true);
        actionToolBar.setTargetComponent(this);
        DataContext dataContext = dataId -> this.file;
        actionToolBar.getComponent().putClientProperty(DataContext.class, dataContext);


        JPanel panel = new JPanel(new GridLayout(1,1));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(actionToolBar.getComponent(), gbc);
        return panel;

    }

    private void addCertificate(Map<String, X509Certificate> certs) {
        certs.keySet().forEach(listModel::addElement);
        list = new JBList<>(listModel);
        list.setCellRenderer(new IconListRenderer());
        // Add a selection listener to handle selection changes
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                var selectedIndex = list.getSelectedValue();
                if (selectedIndex != null && !selectedIndex.isEmpty()) {
                    loadPemView(certs.get(selectedIndex));
                }
            }
        });
        listPanel.removeAll();
        listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
    }

    public @Nullable JComponent getUnlockText() {
        return this.passwordField;
    }

    private static class IconListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(AllIcons.FileTypes.Any_type);
            return label;
        }
    }

    public void addCertificate(String alias, X509Certificate certificate) {
        listModel.addElement(alias);
        certs.put(alias, certificate);
    }

    public String getSelectedCertificate() {
        return list.getSelectedValue();
    }

    public void removeCertificate(String alias) {
        listModel.remove(listModel.indexOf(alias));
        certs.remove(alias);
    }
}