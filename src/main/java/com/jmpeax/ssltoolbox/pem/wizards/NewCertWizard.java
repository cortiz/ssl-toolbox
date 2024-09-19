package com.jmpeax.ssltoolbox.pem.wizards;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class NewCertWizard extends DialogWrapper {

    private JTextField commonNameField;
    private JTextField organizationField;
    private JTextField orgUnitField;
    private JTextField countryField;
    private ComboBox<String> keyAlgorithmField;
    private ComboBox<Integer> keySizeField;
    private JSpinner validFromField;
    private JSpinner validToField;

    public NewCertWizard(@Nullable Project project) {
        super(project);
        init();
        setTitle("New X509 Certificate Wizard");
    }
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 2));

        panel.add(new JLabel("Common Name (CN):"));
        commonNameField = new JTextField();
        panel.add(commonNameField);

        panel.add(new JLabel("Organization (O):"));
        organizationField = new JTextField();
        panel.add(organizationField);

        panel.add(new JLabel("Organizational Unit (OU):"));
        orgUnitField = new JTextField();
        panel.add(orgUnitField);

        panel.add(new JLabel("Country (C):"));
        countryField = new JTextField();
        panel.add(countryField);

        panel.add(new JLabel("Key Algorithm:"));
        keyAlgorithmField = new ComboBox<>(new String[]{"RSA", "DSA", "EC"});
        panel.add(keyAlgorithmField);

        panel.add(new JLabel("Key Size:"));
        keySizeField = new ComboBox<>(new Integer[]{1024, 2048, 4096});
        panel.add(keySizeField);

        panel.add(new JLabel("Valid From:"));
        SpinnerDateModel fromModel = new SpinnerDateModel();
        validFromField = new JSpinner(fromModel);
        panel.add(validFromField);

        panel.add(new JLabel("Valid To:"));
        SpinnerDateModel toModel = new SpinnerDateModel();
        validToField = new JSpinner(toModel);
        panel.add(validToField);

        return panel;
    }

    @Override
    protected void doOKAction() {
        String commonName = commonNameField.getText();
        String organization = organizationField.getText();
        String orgUnit = orgUnitField.getText();
        String country = countryField.getText();
        String keyAlgorithm = (String) keyAlgorithmField.getSelectedItem();
        int keySize = (int) keySizeField.getSelectedItem();
        Date validFrom = (Date) validFromField.getValue();
        Date validTo = (Date) validToField.getValue();

        // Add code to generate the X.509 certificate using the above values.

        super.doOKAction();
    }
}