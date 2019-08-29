package com.retroleveleditor.panels;

import com.retroleveleditor.util.Colors;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class LevelEditorTilemapPanel extends BaseTilemapPanel
{
    private static Image DATA_IMAGE = null;

    private Colors levelColor;
    private String levelMusicName = null;

    static
    {
        try
        {
            DATA_IMAGE = ImageIO.read(ResourceTilemapPanel.class.getResourceAsStream("/data_icon.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public LevelEditorTilemapPanel(final int tilemapCols, final int tilemapRows, final int tileSize)
    {
        super(tilemapCols, tilemapRows, tileSize, false, false);
        levelColor = Colors.PALLET_COLOR;
        levelMusicName = null;
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
                    else if (tile.getTileTraits() == TilePanel.TileTraits.NO_ANIM_WARP)
                    {
                        g2.setColor(new Color(150, 0, 0, 150));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.PRESS_WARP)
                    {
                        g2.setColor(new Color(0, 0, 255, 150));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.ENCOUNTER)
                    {
                        g2.setColor(new Color(150, 150, 0, 150));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.JUMPING_LEDGE_BOT)
                    {
                        g2.setColor(new Color(100, 100, 100, 150));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.JUMPING_LEDGE_LEFT)
                    {
                        g2.setColor(new Color(150, 150, 150, 150));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.JUMPING_LEDGE_RIGHT)
                    {
                        g2.setColor(new Color(200, 200, 200, 150));
                    }
                    g2.fillRect(tile.getX() - 1, tile.getY() - 1, tile.getWidth(), tile.getHeight());
                    g2.setColor(new Color(255, 255, 255, 255));
                }

                if (tile.isMouseHoveringOverTile())
                {
                    g2.setColor(Color.BLACK);
                    g2.drawString(tile.getCoordsString(this.tileRows), tile.getX() + 1, tile.getY() + 10);
                }
                if (tile.getNpcAttributes() != null)
                {
                    g2.drawImage(DATA_IMAGE, tile.getX() + 1, tile.getY() + tile.getHeight()/3, tile.getWidth()/2, tile.getHeight()/2, null);
                }
            }
        }
    }

    public Colors getLevelColor() { return this.levelColor; }
    public void setLevelColor(final Colors levelColor) { this.levelColor = levelColor; }

    public String getLevelMusicName() { return this.levelMusicName; }
    public void setLevelMusicName(final String musicName) { this.levelMusicName = musicName; }

}
