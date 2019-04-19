package com.retroleveleditor.action_listeners;

import com.retroleveleditor.commands.CommandManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UndoActionListener implements ActionListener
{
    private Frame mainFrame;

    public UndoActionListener(final Frame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        CommandManager.undoLastCommand();

        this.mainFrame.revalidate();
        this.mainFrame.repaint();
    }
}
