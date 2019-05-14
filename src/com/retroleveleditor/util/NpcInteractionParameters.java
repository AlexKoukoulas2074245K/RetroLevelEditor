package com.retroleveleditor.util;

public class NpcInteractionParameters
{
    public final String dialog;
    public final String coordsString;
    public final int direction;

    public NpcInteractionParameters(final String dialog, final String coordsString, final int direction)
    {
        this.dialog = dialog;
        this.coordsString = coordsString;
        this.direction = direction;
    }
}
