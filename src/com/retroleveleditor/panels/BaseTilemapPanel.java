package com.retroleveleditor.panels;

import com.retroleveleditor.util.Pair;
import com.retroleveleditor.util.TileImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;

public class BaseTilemapPanel extends JPanel implements MouseWheelListener
{
    private static final int MIN_TILE_SIZE = 4;
    private static final int MAX_TILE_SIZE = 128;

    protected final int tileCols;
    protected final int tileRows;
    protected final boolean isModelsPanel;
    protected final boolean isTraitsPanel;
    protected int tileSize;

    public BaseTilemapPanel(final int tilemapCols, final int tilemapRows, final int tileSize, final boolean isModelsPanel, final boolean isTraitsPanel)
    {
        super(new GridLayout(tilemapRows, tilemapCols));

        this.tileSize = tileSize;
        this.tileCols = Math.max(1, tilemapCols);
        this.isModelsPanel = isModelsPanel;
        this.isTraitsPanel = isTraitsPanel;
        this.tileRows = tilemapRows;

        resetTilemap();

        addMouseWheelListener(this);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, getWidth(), getHeight());

        Component[] components = getComponents();
        Map<Component, TileImage> componentsWithTrailingModelImages = new HashMap<>();
        for (Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;
                if (!tile.isResourceTile() && tile.getDefaultTileImage() != null)
                {
                    final String modelName = tile.getDefaultTileImage().modelName;
                    if (modelName.length() > 0)
                    {
                        final int modelXTileDims = tile.getDefaultTileImage().atlasCol;
                        final int modelYTileDims = tile.getDefaultTileImage().atlasRow;

                        for (int row = tile.getRow(); row < tile.getRow() + modelYTileDims; ++row)
                        {
                            for (int col = tile.getCol(); col < tile.getCol() + modelXTileDims; ++col)
                            {
                                if (row == tile.getRow() && col == tile.getCol()) continue;
                                componentsWithTrailingModelImages.put(getTileAtCoords(col, row), tile.getDefaultTileImage());
                            }
                        }
                    }
                }
            }
        }

        for (Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;
                g2.setColor(Color.black);

                // Draw models resource tab
                if (this.isModelsPanel)
                {
                    g2.drawLine(tile.getX(), tile.getY() + tile.getHeight() - 1, tile.getX() + tile.getWidth() - 1, tile.getY() + tile.getHeight() - 1);
                    g2.drawLine(tile.getX() + tile.getWidth() - 1, tile.getY(), tile.getX() + tile.getWidth() - 1, tile.getY() + tile.getHeight() - 1);

                    if (tile.getDefaultTileImage() != null)
                    {
                        g2.drawImage(tile.getDefaultTileImage().image, tile.getX() + tile.getWidth() - tile.getHeight(), tile.getY(), tile.getHeight(), tile.getHeight(), null);

                        Font defaultFont = g2.getFont();
                        adjustFontForStringToFitInSpace(g2, tile.getDefaultTileImage().modelName, tile.getWidth() - tile.getHeight());
                        g2.drawString(tile.getDefaultTileImage().modelName, tile.getX() + 5, tile.getY() + tile.getHeight()/2);
                        g2.setFont(defaultFont);
                    }
                }
                else if (this.isTraitsPanel)
                {
                    g2.drawLine(tile.getX(), tile.getY() + tile.getHeight() - 1, tile.getX() + tile.getWidth() - 1, tile.getY() + tile.getHeight() - 1);
                    g2.drawLine(tile.getX() + tile.getWidth() - 1, tile.getY(), tile.getX() + tile.getWidth() - 1, tile.getY() + tile.getHeight() - 1);

                    Font defaultFont = g2.getFont();
                    adjustFontForStringToFitInSpace(g2, tile.getTileTraits().toString(), tile.getWidth() - tile.getHeight());
                    g2.drawString(tile.getTileTraits().toString(), tile.getX() + 5, tile.getY() + tile.getHeight()/2);
                    g2.setFont(defaultFont);

                    if (tile.getTileTraits() == TilePanel.TileTraits.SOLID)
                    {
                        g2.setColor(new Color(0, 0, 0, 255));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.WARP)
                    {
                        g2.setColor(new Color(150, 0, 150, 255));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.NO_ANIM_WARP)
                    {
                        g2.setColor(new Color(150, 0, 0, 255));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.PRESS_WARP)
                    {
                        g2.setColor(new Color(0, 0, 255, 255));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.ENCOUNTER)
                    {
                        g2.setColor(new Color(150, 150, 0, 255));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.JUMPING_LEDGE_BOT)
                    {
                        g2.setColor(new Color(100, 100, 100, 255));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.JUMPING_LEDGE_LEFT)
                    {
                        g2.setColor(new Color(150, 150, 150, 255));
                    }
                    else if (tile.getTileTraits() == TilePanel.TileTraits.JUMPING_LEDGE_RIGHT)
                    {
                        g2.setColor(new Color(200, 200, 200, 255));
                    }

                    if (tile.getTileTraits() != TilePanel.TileTraits.NONE && tile.getTileTraits() != TilePanel.TileTraits.UNUSED)
                    {
                        g2.fillRect(tile.getX() + tile.getWidth() - tile.getHeight(), tile.getY(), tile.getHeight(), tile.getHeight());
                    }

                    g2.setColor(new Color(255, 255, 255, 255));
                }
                // Draw every other tilemap
                else
                {
                    g2.drawLine(tile.getX(), tile.getY() + tileSize - 1, tile.getX() + tileSize - 1, tile.getY() + tileSize - 1);
                    g2.drawLine(tile.getX() + tileSize - 1, tile.getY(), tile.getX() + tileSize - 1, tile.getY() + tileSize - 1);

                    if (tile.getDefaultTileImage() != null)
                    {
                        g2.drawImage(tile.getDefaultTileImage().image, tile.getX() - 1, tile.getY() - 1, tileSize, tileSize, null);
                    }

                    if (tile.getCharTileImage() != null)
                    {
                        g2.drawImage(tile.getCharTileImage().image, tile.getX() - 1, tile.getY() - 1, tileSize, tileSize, null);
                    }
                    
                    if (componentsWithTrailingModelImages.containsKey(component))
                    {
                        g2.drawImage(componentsWithTrailingModelImages.get(component).image, tile.getX() - 1, tile.getY() - 1, tileSize, tileSize, null);
                        g2.setColor(new Color(255, 255, 255, 150));
                        g2.fillRect(tile.getX() - 1, tile.getY() - 1, tileSize, tileSize);
                        g2.setColor(new Color(255, 255, 255, 255));
                    }
                }
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        getRootPane().revalidate();
        getRootPane().repaint();

        if (e.isControlDown())
        {
            this.tileSize -= (int)e.getPreciseWheelRotation();
            this.tileSize = Math.max(MIN_TILE_SIZE, this.tileSize);
            this.tileSize = Math.min(MAX_TILE_SIZE, this.tileSize);

            resetTilemap();

            getRootPane().revalidate();
            getRootPane().repaint();
        }
        else
        {
            getParent().dispatchEvent(e);
        }
    }

    public int getTileCols() { return this.tileCols; }

    public int getTileRows() { return this.tileRows; }

    public int getTileSize()
    {
        return this.tileSize;
    }

    public boolean isModelsPanel() { return this.isModelsPanel; }

    public boolean isTraitsPanel() { return this.isTraitsPanel; }

    public TilePanel getTileAtCoords(final int col, final int row)
    {
        Component[] components = getComponents();
        for (Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tilePanel = (TilePanel) component;

                if (tilePanel.getCol() == col && tilePanel.getRow() == row)
                {
                    return tilePanel;
                }
            }
        }

        return null;
    }

    private void resetTilemap()
    {
        for (int y = 0; y < this.tileRows; ++y)
        {
            for (int x = 0; x < this.tileCols; ++x)
            {
                TilePanel tile = getTileAtCoords(x, y);
                if (tile == null)
                {
                    add(new TilePanel(x, y, this.tileSize));
                }
                else
                {
                    tile.setSize(new Dimension(this.tileSize, this.tileSize));
                    tile.setPreferredSize(new Dimension(this.tileSize, this.tileSize));
                    tile.setMinimumSize(new Dimension(this.tileSize, this.tileSize));
                    tile.setMaximumSize(new Dimension(this.tileSize, this.tileSize));
                }
            }
        }
    }

    public void deselectAllTiles()
    {
        Component[] components = getComponents();
        for (Component component : components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;
                tile.setIsSelected(false);
            }
        }
    }

    private void adjustFontForStringToFitInSpace(final Graphics2D gfx, final String string, final int spaceToFitIn)
    {
        final Font previousFont = gfx.getFont();
        int targetFontSize = previousFont.getSize();
        int renderedStringWidth = gfx.getFontMetrics().stringWidth(string);

        while (renderedStringWidth >= spaceToFitIn * 9/10)
        {
            targetFontSize--;
            gfx.setFont(previousFont.deriveFont(Font.PLAIN, targetFontSize));
            renderedStringWidth = gfx.getFontMetrics(gfx.getFont()).stringWidth(string);
        }
    }

}
