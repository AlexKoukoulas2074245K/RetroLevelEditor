package com.retroleveleditor.util;

import java.awt.*;

public class TileImage
{
    public final Image image;
    public final int atlasCol;
    public final int atlasRow;

    public TileImage(final Image image, final int atlasCol, final int atlasRow)
    {
        this.image = image;
        this.atlasCol = atlasCol;
        this.atlasRow = atlasRow;
    }
}
