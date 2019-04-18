package com.retroleveleditor.panels;

import java.awt.*;

public class LevelEditorTilemapPanel extends BaseTilemapPanel
{
    public LevelEditorTilemapPanel(final int tilemapCols, final int tilemapRows, final int tileSize)
    {
        super(tilemapCols, tilemapRows, tileSize);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();


        Component[] components = getComponents();
        for(Component component: components)
        {
            TilePanel tile = (TilePanel)component;

            // Draw coords string if not resource cell
            if (tile.isMouseHoveringOverTile())
            {
                g2.setColor(Color.BLACK);
                g2.drawString(tile.getCoordsString(), tile.getX() + 1, tile.getY() + 10);
            }
        }
    }
}
