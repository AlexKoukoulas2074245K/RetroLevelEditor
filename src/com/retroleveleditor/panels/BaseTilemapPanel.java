package com.retroleveleditor.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class BaseTilemapPanel extends JPanel implements MouseWheelListener
{
    private static final int MIN_TILE_SIZE = 4;
    private static final int MAX_TILE_SIZE = 128;

    private final int tileCols;
    private final int tileRows;
    private int tileSize;

    public BaseTilemapPanel(final int tilemapCols, final int tilemapRows, final int tileSize)
    {
        super(new GridLayout(tilemapRows, tilemapCols));

        this.tileSize = tileSize;
        this.tileCols = tilemapCols;
        this.tileRows = tilemapRows;

        createTilemap();

        addMouseWheelListener(this);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, getSize().width, getSize().height);

        Component[] components = getComponents();
        for(Component component: components)
        {
            // Draw Tile outlines
            TilePanel tile = (TilePanel)component;
            g2.setColor(Color.black);
            g2.drawLine(tile.getX(), tile.getY() + tileSize - 1, tile.getX() + tileSize - 1, tile.getY() + tileSize - 1);
            g2.drawLine(tile.getX() + tileSize - 1 , tile.getY(), tile.getX() + tileSize - 1, tile.getY() + tileSize - 1);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (e.isControlDown())
        {
            this.tileSize -= (int)e.getPreciseWheelRotation();
            this.tileSize = Math.max(MIN_TILE_SIZE, this.tileSize);
            this.tileSize = Math.min(MAX_TILE_SIZE, this.tileSize);

            removeAll();

            createTilemap();

            getRootPane().revalidate();
            getRootPane().repaint();
        }
    }

    public int getTileSize()
    {
        return this.tileSize;
    }

    private void createTilemap()
    {
        for (int y = 0; y < this.tileRows; ++y)
        {
            for (int x = 0; x < this.tileCols; ++x)
            {
                add(new TilePanel(x, y, this.tileSize, this.tileRows));
            }
        }
    }
}
