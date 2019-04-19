package com.retroleveleditor.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class BaseTilemapPanel extends JPanel implements MouseWheelListener
{
    private static final int MIN_TILE_SIZE = 4;
    private static final int MAX_TILE_SIZE = 128;

    protected final int tileCols;
    protected final int tileRows;
    protected int tileSize;

    public BaseTilemapPanel(final int tilemapCols, final int tilemapRows, final int tileSize)
    {
        super(new GridLayout(tilemapRows, tilemapCols));

        this.tileSize = tileSize;
        this.tileCols = tilemapCols;
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
        g2.fillRect(0, 0, getSize().width, getSize().height);

        Component[] components = getComponents();
        for(Component component: components)
        {
            if (component instanceof TilePanel)
            {
                // Draw Tile outlines
                TilePanel tile = (TilePanel) component;
                g2.setColor(Color.black);
                g2.drawLine(tile.getX(), tile.getY() + tileSize - 1, tile.getX() + tileSize - 1, tile.getY() + tileSize - 1);
                g2.drawLine(tile.getX() + tileSize - 1, tile.getY(), tile.getX() + tileSize - 1, tile.getY() + tileSize - 1);

                if (tile.getTileImage() != null)
                {
                    g2.drawImage(tile.getTileImage().image, tile.getX() - 1, tile.getY() - 1, tileSize, tileSize, null);
                }
            }
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

            resetTilemap();
            JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(this);
            frame.pack();

            getRootPane().revalidate();
            getRootPane().repaint();
        }
        else
        {
            getParent().dispatchEvent(e);
        }
    }

    public int getTileSize()
    {
        return this.tileSize;
    }

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
}
