package com.jmpeax.ssltoolbox.pem;

import com.intellij.diff.util.FileEditorBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.jmpeax.ssltoolbox.svc.CertificateHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class PEMFileEditor extends FileEditorBase {

    private final VirtualFile file;
    private final JBTabbedPane panel;

    public PEMFileEditor(@NotNull VirtualFile file)
            throws CertificateException, IOException {
        this.file = file;
        this.panel = new JBTabbedPane();
        var certificateHelper = ApplicationManager.getApplication().getService(CertificateHelper.class);
        var c = certificateHelper.getCertificate(file);
        if (c.isEmpty()){
            this.panel.add(new JLabel("Error loading " + file.getName()));
        }else {
            c.forEach(x509Certificate -> buildTabbedPane(certificateHelper, x509Certificate));
        }
    }

    private void buildTabbedPane(CertificateHelper certificateHelper,X509Certificate x509Certificate) {
        var name = certificateHelper.getCommonName(x509Certificate);
        if (certificateHelper.isValid(x509Certificate)) {
            this.panel.addTab(name, new PemView(x509Certificate));
        } else {
            this.panel.addTab(name, AllIcons.Ide.FatalError, new PemView(x509Certificate));
        }
    }


    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Certificate Viewer";
    }

    @Override
    public VirtualFile getFile() {
        return file;
    }
}
