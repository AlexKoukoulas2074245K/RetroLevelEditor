package com.retroleveleditor.panels;

import com.retroleveleditor.util.SelectAllFocusListener;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;

public class LabelledInputPanel extends JPanel
{
    private final JFormattedTextField textField;

    public LabelledInputPanel(final String inputPanelDescription, final NumberFormatter inputFormatter, final int numberOfDigitsAllowed, final Object defaultValue)
    {
        JLabel levelSpecInputLabel = new JLabel(inputPanelDescription);

        this.textField = new JFormattedTextField(inputFormatter);
        this.textField.setValue(defaultValue);
        this.textField.setColumns(numberOfDigitsAllowed);
        this.textField.addFocusListener(new SelectAllFocusListener(this.textField));

        add(levelSpecInputLabel);
        add(this.textField);
    }

    public LabelledInputPanel(final String inputPanelDescription, final DecimalFormat inputFormatter, final int numberOfDigitsAllowed, final Object defaultValue)
    {
        JLabel levelSpecInputLabel = new JLabel(inputPanelDescription);

        this.textField = new JFormattedTextField(inputFormatter);
        this.textField.setValue(defaultValue);
        this.textField.setColumns(numberOfDigitsAllowed);
        this.textField.addFocusListener(new SelectAllFocusListener(this.textField));

        add(levelSpecInputLabel);
        add(this.textField);
    }

    public JFormattedTextField getTextField()
    {
        return textField;
    }
}
