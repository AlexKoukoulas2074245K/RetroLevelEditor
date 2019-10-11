package com.retroleveleditor.commands;

import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.NpcAttributes;
import com.retroleveleditor.util.TileImage;

public class ClearLevelEditorTileCommand implements ICommand
{
    private TilePanel tile;
    private TileImage previousDefaultImage;
    private TileImage previousCharImage;
    private TilePanel.TileTraits previousTileTraits;
    private NpcAttributes npcAttributes;

    public ClearLevelEditorTileCommand(final TilePanel tile)
    {
        this.tile = tile;
        this.previousDefaultImage = tile.getDefaultTileImage();
        this.previousCharImage = tile.getCharTileImage();
        this.previousTileTraits = tile.getTileTraits();
        this.npcAttributes = tile.getNpcAttributes();
    }

    @Override
    public void execute()
    {
        this.tile.setDefaultTileImage(null);
        this.tile.setCharTileImage(null);
        this.tile.setTileTraits(TilePanel.TileTraits.NONE);
        this.tile.setNpcAttributes(null);
        this.tile.setToolTipText(null);
    }

    @Override
    public void undo()
    {
        this.tile.setDefaultTileImage(previousDefaultImage);
        this.tile.setCharTileImage(previousCharImage);
        this.tile.setTileTraits(previousTileTraits);
        this.tile.setNpcAttributes(npcAttributes);

        if (this.tile.getNpcAttributes() != null)
        {
            this.tile.setToolTipText(this.tile.getNpcAttributes().toString());
        }
    }

    @Override
    public boolean isIdenticalTo(ICommand other)
    {
        if (other instanceof ClearLevelEditorTileCommand)
        {
            ClearLevelEditorTileCommand otherCommand = (ClearLevelEditorTileCommand)other;

            return this.tile == otherCommand.tile;
        }

        return false;
    }
}
