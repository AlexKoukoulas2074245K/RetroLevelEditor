package com.retroleveleditor.commands;

import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.NpcAttributes;

public class SetTileNpcAttributesCommand implements ICommand
{
    private TilePanel tile;
    private NpcAttributes previousNpcAttributes;
    private NpcAttributes newNpcAttributes;

    public SetTileNpcAttributesCommand(TilePanel tile, NpcAttributes npcAttributes)
    {
        this.tile = tile;
        this.newNpcAttributes = npcAttributes;
        this.previousNpcAttributes = tile.getNpcAttributes();
    }

    @Override
    public void execute()
    {
        this.tile.setNpcAttributes(this.newNpcAttributes);

        if (this.tile.getNpcAttributes() != null)
        {
            this.tile.setToolTipText(this.tile.getNpcAttributes().toString());
        }
    }

    @Override
    public void undo()
    {
        this.tile.setNpcAttributes(this.previousNpcAttributes);

        if (this.tile.getNpcAttributes() != null)
        {
            this.tile.setToolTipText(this.tile.getNpcAttributes().toString());
        }
    }

    @Override
    public boolean isIdenticalTo(ICommand other)
    {
        if (other instanceof SetTileNpcAttributesCommand)
        {
            SetTileNpcAttributesCommand otherCommand = (SetTileNpcAttributesCommand)other;

            return this.tile == otherCommand.tile && this.newNpcAttributes == otherCommand.newNpcAttributes;
        }

        return false;
    }
}
