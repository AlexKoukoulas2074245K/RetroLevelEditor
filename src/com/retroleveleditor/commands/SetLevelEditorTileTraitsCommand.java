package com.retroleveleditor.commands;

import com.retroleveleditor.panels.TilePanel;

public class SetLevelEditorTileTraitsCommand implements ICommand
{
    private final TilePanel tile;
    private final TilePanel.TileTraits oldTileTraits;
    private final TilePanel.TileTraits newTileTraits;

    public SetLevelEditorTileTraitsCommand(final TilePanel tilePanel, final TilePanel.TileTraits newTileTraits)
    {
        this.tile = tilePanel;
        this.oldTileTraits = tilePanel.getTileTraits();
        this.newTileTraits = newTileTraits;
    }

    @Override
    public void execute()
    {
        this.tile.setTileTraits(this.newTileTraits);
    }

    @Override
    public void undo()
    {
        this.tile.setTileTraits(this.oldTileTraits);
    }

    @Override
    public boolean isIdenticalTo(ICommand other)
    {
        if (other instanceof SetLevelEditorTileTraitsCommand)
        {
            SetLevelEditorTileTraitsCommand otherCommand = (SetLevelEditorTileTraitsCommand)other;

            return this.tile == otherCommand.tile && this.newTileTraits == otherCommand.newTileTraits;
        }

        return false;
    }
}
