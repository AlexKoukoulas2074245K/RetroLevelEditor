package com.retroleveleditor.commands;

import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.TileImage;

public class ClearLevelEditorTileImageCommand implements ICommand
{
    private TilePanel tile;
    private TileImage previousImage;

    public ClearLevelEditorTileImageCommand(final TilePanel tile)
    {
        this.tile = tile;
        this.previousImage = tile.getTileImage();
    }

    @Override
    public void execute()
    {
        this.tile.setTileImage(null);
    }

    @Override
    public void undo()
    {
        this.tile.setTileImage(previousImage);
    }

    @Override
    public boolean isIdenticalTo(ICommand other)
    {
        if (other instanceof ClearLevelEditorTileImageCommand)
        {
            ClearLevelEditorTileImageCommand otherCommand = (ClearLevelEditorTileImageCommand)other;

            return this.tile == otherCommand.tile;
        }

        return false;
    }
}
