package com.jmpeax.ssltoolbox.pem.wizards;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class NewCertWizardAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null) {
            return;
        }
        // Create and show the wizard
        NewCertWizard wizard = new NewCertWizard(e.getProject());
        wizard.show();
    }
}
