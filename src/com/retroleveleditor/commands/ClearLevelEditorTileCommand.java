package com.retroleveleditor.commands;

import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.TileImage;

public class ClearLevelEditorTileCommand implements ICommand
{
    private TilePanel tile;
    private TileImage previousDefaultImage;
    private TileImage previousCharImage;
    private TilePanel.TileTraits previousTileTraits;

    public ClearLevelEditorTileCommand(final TilePanel tile)
    {
        this.tile = tile;
        this.previousDefaultImage = tile.getDefaultTileImage();
        this.previousCharImage = tile.getCharTileImage();
        this.previousTileTraits = tile.getTileTraits();
    }

    @Override
    public void execute()
    {
        this.tile.setDefaultTileImage(null);
        this.tile.setCharTileImage(null);
        this.tile.setTileTraits(TilePanel.TileTraits.NONE);
    }

    @Override
    public void undo()
    {
        this.tile.setDefaultTileImage(previousDefaultImage);
        this.tile.setCharTileImage(previousCharImage);
        this.tile.setTileTraits(previousTileTraits);
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
