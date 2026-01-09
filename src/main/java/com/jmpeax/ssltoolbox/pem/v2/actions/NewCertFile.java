package com.jmpeax.ssltoolbox.pem.v2.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.jmpeax.ssltoolbox.pem.PEMFileType;
import org.jetbrains.annotations.NotNull;

public class NewCertFile extends AnAction {

    private static final String EMPTY_PEM_TEMPLATE = """
            -----BEGIN CERTIFICATE-----
            
            -----END CERTIFICATE-----
            """;

    /**
     * Action to create a new PEM file.
     *
     * @param e The event that triggered this action.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Ensure we have the project context
        var project = e.getProject();
        if (project == null) {
            return;
        }
        VirtualFile newFile = new LightVirtualFile("new.pem", new PEMFileType(), EMPTY_PEM_TEMPLATE);
        FileEditorManager.getInstance(project).openFile(newFile, true);

    }

    /**
     * Update the visibility and enablement of this action.
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        // This action is available only if a project is open
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
