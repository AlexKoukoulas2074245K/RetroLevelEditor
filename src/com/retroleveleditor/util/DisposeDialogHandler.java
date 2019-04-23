package com.retroleveleditor.util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DisposeDialogHandler implements ActionListener
{
    private final JDialog dialog;

    public DisposeDialogHandler(final JDialog dialog)
    {
        this.dialog = dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        dialog.dispose();
    }
}
