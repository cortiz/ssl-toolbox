package com.jmpeax.ssltoolbox.pem;



import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.security.cert.CertificateException;


public class PEMEditor2 extends TextEditorWithPreview {

    private final VirtualFile file;

    public PEMEditor2(@NotNull TextEditor textEditor, @NotNull VirtualFile file) throws CertificateException, IOException {
       super(textEditor, new PEMFileEditor(file),"Certificate Viewer",Layout.SHOW_PREVIEW);
       this.file = file;
       addDocumentListener(file);
        addFocusListenerToEditor(textEditor);
    }

    @Override
    public VirtualFile getFile() {
        return file;
    }
    /**
     * Add a DocumentListener to the document associated with the current file.
     */
    private void addDocumentListener(@NotNull VirtualFile file) {
        // Use FileDocumentManager to retrieve the Document associated with this VirtualFile
        Document document = FileDocumentManager.getInstance().getDocument(file);

        if (document != null) {
            // Add a DocumentListener to the document
            document.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent event) {
                    // Fetch the updated content
                    String updatedText = document.getText();

                    // Example Action: Log or Process the Updated Content
                    processDocumentChange(updatedText);
                }
            });
        }
    }

    /**
     * Process the action to perform after a document change.
     * Replace this logic with your own needs (e.g., updating a UI component or validating content).
     */
    private void processDocumentChange(@NotNull String updatedText) {
        System.out.println("Document was modified. Updated content:\n" + updatedText);

        // Additional logic: For example, validation or preview updates
        if (updatedText.contains("ERROR")) {
            System.out.println("Validation Warning: The document contains 'ERROR'.");
        }
    }

    /**
     * Add a FocusListener to perform an action when the editor loses focus.
     */
    private void addFocusListenerToEditor(@NotNull TextEditor textEditor) {
        Editor editor = textEditor.getEditor();
        if (editor != null) {
            editor.getContentComponent().addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    // Optional: Log or perform an action when editor gains focus
                    System.out.println("Editor focus gained.");
                }

                @Override
                public void focusLost(FocusEvent e) {
                    // Action to perform when the editor loses focus
                    onEditorFocusLost();
                }
            });
        }
    }

    /**
     * Action to execute when the editor loses focus.
     */
    private void onEditorFocusLost() {
        System.out.println("Editor lost focus.");

        // Example: Validate or save the file content when focus is lost
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
            String content = document.getText();
            System.out.println("Content at focus lost:\n" + content);

            // Add custom validation logic here (if required)
            if (!content.contains("-----BEGIN CERTIFICATE-----")) {
                System.err.println("Validation Warning: Missing certificate header.");
            }

        }
    }

}
