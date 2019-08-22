package com.retroleveleditor.action_listeners;

import com.retroleveleditor.commands.CommandManager;
import com.retroleveleditor.commands.SetTileNpcAttributesCommand;
import com.retroleveleditor.main.MainFrame;
import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.NpcAttributes;
import com.retroleveleditor.util.PokemonInfo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SetNpcAttributesActionListener implements ActionListener
{
    private MainFrame mainFrame;

    public SetNpcAttributesActionListener(final MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Component[] components = mainFrame.getMainPanel().getLevelEditorTilemap().getComponents();
        for (Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;
                if (tile.isMouseHoveringOverTile())
                {
                    NpcAttributes npcAttributes = new NpcAttributes
                    (
                            "", new ArrayList<String>(), new ArrayList<PokemonInfo>(), NpcAttributes.MovemenType.DYNAMIC, 0, false, false
                    );

                    CommandManager.executeCommand(new SetTileNpcAttributesCommand(tile, npcAttributes));
                }
            }
        }
    }
}
