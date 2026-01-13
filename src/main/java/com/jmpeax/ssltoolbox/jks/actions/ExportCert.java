package com.jmpeax.ssltoolbox.jks.actions;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.jmpeax.ssltoolbox.jks.JKSView;
import com.jmpeax.ssltoolbox.svc.CertificateHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ExportCert extends AnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportCert.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        var certificateHelper = ApplicationManager.getApplication().getService(CertificateHelper.class);
        var ksVirtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        var view = getViewComponent(e);
        if (view == null) {
            Messages.showErrorDialog("No JKS view found", "No JKS View");
            return;
        }
        var selectedAlias = view.getSelectedCertificate();
        if (selectedAlias == null) {
            Messages.showErrorDialog("No certificate selected", "No Certificate Selected");
            return;
        }
        var descriptor = new FileSaverDescriptor("Export Certificate", "Export certificate", "cer");

        var pwd = Messages.showPasswordDialog("Keystore password", "KeyStore Password");
        if (pwd != null && !pwd.isBlank()) {
            var f = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, view).save(selectedAlias + ".cer");
            var cert = certificateHelper.exportCertificateToByte(ksVirtualFile, selectedAlias, pwd.toCharArray());
            if (f != null) {
                ApplicationManager.getApplication().runWriteAction(() -> writeFile(f, cert));
                Messages.showInfoMessage("Certificate exported", "Certificate Exported");
            } else {
                Messages.showErrorDialog("No file selected", "No File Selected");
            }
        }
    }

    private void writeFile(VirtualFileWrapper f, ByteArrayOutputStream cert) {
        try {
            var file = f.getVirtualFile(true);
            if (file != null) {
                file.setBinaryContent(cert.toByteArray());
            }
        } catch (IOException e) {
            LOGGER.error("Error writing file", e);
            Messages.showErrorDialog("Error writing file", "Error Writing File");
        }
    }

    private JKSView getViewComponent(@NotNull AnActionEvent e) {
        // Retrieve your view component from context, e.g., using the data context from the event
        return e.getData(PlatformDataKeys.CONTEXT_COMPONENT) instanceof JKSView
                ? (JKSView) e.getData(PlatformDataKeys.CONTEXT_COMPONENT)
                : null;
    }
}
