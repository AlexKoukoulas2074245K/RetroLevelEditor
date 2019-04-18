package com.retroleveleditor.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TilePanel extends JPanel implements MouseListener
{
    private final int tileCol;
    private final int tileRow;
    private final int numberOfRows;
    private boolean isMouseHoveringOverTile;

    public TilePanel(final int tileCol, final int tileRow, final int tileSize, final int numberOfRows)
    {
        super();

        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.numberOfRows = numberOfRows;
        this.isMouseHoveringOverTile = false;

        addMouseListener(this);
        setPreferredSize(new Dimension(tileSize, tileSize));
    }

    @Override
    public void paintComponent(Graphics g)
    {
        return;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        getRootPane().revalidate();
        getRootPane().repaint();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        getRootPane().revalidate();
        getRootPane().repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {

    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        this.isMouseHoveringOverTile = true;
        getRootPane().revalidate();
        getRootPane().repaint();
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        this.isMouseHoveringOverTile = false;
        getRootPane().revalidate();
        getRootPane().repaint();
    }

    public boolean isMouseHoveringOverTile()
    {
        return this.isMouseHoveringOverTile;
    }

    public String getCoordsString()
    {
        return this.tileCol + "," + (this.numberOfRows - this.tileRow);
    }

}
