package com.retroleveleditor.action_listeners;

import com.retroleveleditor.commands.CommandManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RedoActionListener implements ActionListener
{
    private final Frame mainFrame;

    public RedoActionListener(final Frame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        CommandManager.redoLastCommand();

        this.mainFrame.revalidate();
        this.mainFrame.repaint();
    }
}
