package com.jmpeax.ssltoolbox.pem.v2.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jmpeax.ssltoolbox.svc.CertificateHelper;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Set;

public class PemFileEditorProvider implements FileEditorProvider {
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        String fileExtension = file.getExtension();
        if(fileExtension == null) {
            return false;
        }
        return switch (Objects.requireNonNull(fileExtension).toLowerCase()) {
            case "pem", "cer", "der", "crt", "ca-bundle", "p7b", "p7c", "cert" -> true;
            default -> false;
        };
    }

    @Override
    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        try {
            var certificateHelper = ApplicationManager.getApplication().getService(CertificateHelper.class);
            var c = certificateHelper.getCertificate(file);
            Document document = EditorFactory.getInstance().createDocument(getCertificatePemText(c));
            var doc = TextEditorProvider.getInstance().getTextEditor(EditorFactory.getInstance().createEditor(document));
            return new PemEditor(doc, file,new PEMFileEditor(file));
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @NotNull
    public String getEditorTypeId() {
        return "pem-file-editor";
    }

    @Override
    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
    }

    private String getCertificatePemText(Set<X509Certificate> certificate) {
        StringWriter writer = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        try {
            for (X509Certificate x509Certificate : certificate) {
                pemWriter.writeObject(x509Certificate);
                pemWriter.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

}
