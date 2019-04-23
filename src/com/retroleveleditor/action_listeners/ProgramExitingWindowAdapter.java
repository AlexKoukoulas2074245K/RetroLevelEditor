package com.retroleveleditor.action_listeners;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgramExitingWindowAdapter extends WindowAdapter
{
    @Override
    public void windowClosing(final WindowEvent __)
    {
        int selOption = JOptionPane.showConfirmDialog (null, "The program is exiting, any unsaved progress will be lost.\nExit Editor?", "Exiting Option", JOptionPane.YES_NO_OPTION);
        if (selOption == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
        else if (selOption == JOptionPane.NO_OPTION)
        {
            return;
        }
    }
}
