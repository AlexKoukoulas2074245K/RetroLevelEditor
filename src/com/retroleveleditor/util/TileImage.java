package com.retroleveleditor.util;

import java.awt.*;

public class TileImage
{
    public final Image image;
    public final String modelName;
    public final int atlasCol;
    public final int atlasRow;

    public TileImage(final Image image, final String modelName, final int atlasCol, final int atlasRow)
    {
        this.image     = image;
        this.modelName = modelName;
        this.atlasCol  = atlasCol;
        this.atlasRow  = atlasRow;
    }
}
