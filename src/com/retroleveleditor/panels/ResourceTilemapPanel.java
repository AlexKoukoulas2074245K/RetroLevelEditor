package com.retroleveleditor.panels;

public class ResourceTilemapPanel extends BaseTilemapPanel
{
    // Structure of all atlases. Change as needed
    static final int ATLAS_COLS = 8;
    static final int ATLAS_ROWS = 64;

    public ResourceTilemapPanel(final String atlasPath, final int resourcePanelCols, final int tileSize)
    {
        super(resourcePanelCols, ((ATLAS_COLS * ATLAS_ROWS)/resourcePanelCols) + 1, tileSize);
    }
}
