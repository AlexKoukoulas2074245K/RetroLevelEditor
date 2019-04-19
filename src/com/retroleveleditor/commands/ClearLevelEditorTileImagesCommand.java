package com.retroleveleditor.commands;

import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.TileImage;

public class ClearLevelEditorTileImagesCommand implements ICommand
{
    private TilePanel tile;
    private TileImage previousDefaultImage;
    private TileImage previousCharImage;

    public ClearLevelEditorTileImagesCommand(final TilePanel tile)
    {
        this.tile = tile;
        this.previousDefaultImage = tile.getDefaultTileImage();
        this.previousCharImage = tile.getCharTileImage();
    }

    @Override
    public void execute()
    {
        this.tile.setDefaultTileImage(null);
        this.tile.setCharTileImage(null);
    }

    @Override
    public void undo()
    {
        this.tile.setDefaultTileImage(previousDefaultImage);
        this.tile.setCharTileImage(previousCharImage);
    }

    @Override
    public boolean isIdenticalTo(ICommand other)
    {
        if (other instanceof ClearLevelEditorTileImagesCommand)
        {
            ClearLevelEditorTileImagesCommand otherCommand = (ClearLevelEditorTileImagesCommand)other;

            return this.tile == otherCommand.tile;
        }

        return false;
    }
}
