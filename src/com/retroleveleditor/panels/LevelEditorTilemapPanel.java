package com.retroleveleditor.panels;

import java.awt.*;

public class LevelEditorTilemapPanel extends BaseTilemapPanel
{
    public LevelEditorTilemapPanel(final int tilemapCols, final int tilemapRows, final int tileSize)
    {
        super(tilemapCols, tilemapRows, tileSize, false, false);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        Component[] components = getComponents();
        for(Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel)component;

                if (tile.getTileTraits() != TilePanel.TileTraits.NONE)
                {
                    if (tile.getTileTraits() == TilePanel.TileTraits.SOLID)
                    {
                        g2.setColor(new Color(0, 0, 0, 150));
                    }

                    else if (tile.getTileTraits() == TilePanel.TileTraits.WARP)
                    {
                        g2.setColor(new Color(150, 0, 150, 150));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.ENCOUNTER)
                    {
                        g2.setColor(new Color(150, 150, 0, 150));
                    }
                    g2.fillRect(tile.getX() - 1, tile.getY() - 1, tile.getWidth(), tile.getHeight());
                    g2.setColor(new Color(255, 255, 255, 255));
                }

                if (tile.isMouseHoveringOverTile())
                {
                    g2.setColor(Color.BLACK);
                    g2.drawString(tile.getCoordsString(this.tileRows), tile.getX() + 1, tile.getY() + 10);
                }
            }
        }
    }
}
