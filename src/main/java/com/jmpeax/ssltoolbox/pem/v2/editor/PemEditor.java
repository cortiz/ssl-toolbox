package com.jmpeax.ssltoolbox.pem.v2.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.cert.CertificateException;

public class PemEditor extends TextEditorWithPreview {

    private final @NotNull VirtualFile file;

    public PemEditor(@NotNull TextEditor textEditor, @NotNull VirtualFile file, PEMFileEditor editor) throws CertificateException, IOException {
        super(textEditor, editor,"Certificate Viewer",Layout.SHOW_PREVIEW);
        this.file = file;
        Document document = textEditor.getEditor().getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                try {
                    reload(document.getCharsSequence());
                } catch (CertificateException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        },this);

    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }

    private void reload(@NotNull CharSequence newFragment) throws CertificateException, IOException {
     if (super.myPreview instanceof PEMFileEditor) {
         var editor = (PEMFileEditor) super.myPreview;
         editor.reload(newFragment);
     }
    }

}
