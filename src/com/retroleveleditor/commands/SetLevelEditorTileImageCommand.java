package com.retroleveleditor.commands;

import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.TileImage;

public class SetLevelEditorTileImageCommand implements ICommand
{
    private TilePanel tile;
    private TileImage newImage;
    private TileImage previousImage;

    public SetLevelEditorTileImageCommand(TilePanel tile, TileImage tileImage)
    {
        this.tile = tile;
        this.newImage = tileImage;
        this.previousImage = tile.getTileImage();
    }

    @Override
    public void execute()
    {
        this.tile.setTileImage(newImage);
    }

    @Override
    public void undo()
    {
        this.tile.setTileImage(previousImage);
    }

    @Override
    public boolean isIdenticalTo(ICommand other)
    {
        if (other instanceof SetLevelEditorTileImageCommand)
        {
            SetLevelEditorTileImageCommand otherCommand = (SetLevelEditorTileImageCommand)other;

            return this.tile == otherCommand.tile && this.newImage == otherCommand.newImage;
        }

        return false;
    }
}
