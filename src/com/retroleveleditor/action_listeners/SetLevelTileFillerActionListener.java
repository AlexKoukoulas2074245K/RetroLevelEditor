package com.retroleveleditor.action_listeners;

import com.retroleveleditor.main.MainFrame;
import com.retroleveleditor.panels.BaseTilemapPanel;
import com.retroleveleditor.panels.TilePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetLevelTileFillerActionListener implements ActionListener
{
    private MainFrame mainFrame;

    public SetLevelTileFillerActionListener(final MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        TilePanel selectedResourceTile = TilePanel.selectedResourceTile;
        if (selectedResourceTile.getParent() != mainFrame.getMainPanel().getEnvironmentsPanel())
        {
            JOptionPane.showMessageDialog(null, "Selected tile is NOT an environment tile", "Illegal selection", JOptionPane.ERROR_MESSAGE);
        }

        Component[] components = mainFrame.getMainPanel().getEnvironmentsPanel().getComponents();
        for (Component component : components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;

                if (tile.isFillerTile())
                {
                    tile.setIsFillerTile(false);
                }
            }
        }

        selectedResourceTile.setIsFillerTile(true);

        mainFrame.getRootPane().invalidate();
        mainFrame.getRootPane().repaint();
    }
}
