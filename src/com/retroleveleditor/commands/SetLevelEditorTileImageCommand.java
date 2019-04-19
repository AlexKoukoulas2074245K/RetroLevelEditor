package com.retroleveleditor.commands;

import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.TileImage;

public class SetLevelEditorTileImageCommand implements ICommand
{
    private TilePanel tile;
    private TileImage newDefaultImage;
    private TileImage newCharImage;
    private TileImage previousDefaultImage;
    private TileImage previousCharImage;

    public SetLevelEditorTileImageCommand(TilePanel tile, TileImage newDefaultImage, TileImage newCharImage)
    {
        this.tile = tile;
        this.newDefaultImage = newDefaultImage;
        this.newCharImage = newCharImage;
        this.previousDefaultImage = tile.getDefaultTileImage();
        this.previousCharImage = tile.getCharTileImage();
    }

    @Override
    public void execute()
    {
        this.tile.setDefaultTileImage(this.newDefaultImage);
        this.tile.setCharTileImage(this.newCharImage);
    }

    @Override
    public void undo()
    {
        this.tile.setDefaultTileImage(this.previousDefaultImage);
        this.tile.setCharTileImage(this.previousCharImage);
    }

    @Override
    public boolean isIdenticalTo(ICommand other)
    {
        if (other instanceof SetLevelEditorTileImageCommand)
        {
            SetLevelEditorTileImageCommand otherCommand = (SetLevelEditorTileImageCommand)other;

            return
                this.tile == otherCommand.tile &&
                this.newDefaultImage == otherCommand.newDefaultImage &&
                this.newCharImage == otherCommand.newCharImage;
        }

        return false;
    }
}
