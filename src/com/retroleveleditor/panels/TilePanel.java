package com.retroleveleditor.panels;

import com.retroleveleditor.commands.ClearLevelEditorTileImageCommand;
import com.retroleveleditor.commands.CommandManager;
import com.retroleveleditor.commands.SetLevelEditorTileImageCommand;
import com.retroleveleditor.util.TileImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class TilePanel extends JPanel implements MouseListener
{
    public static TileImage selectedResourceImage = null;
    private static boolean mouseLeftDown = false;
    private static boolean mouseRightDown = false;

    private final int tileCol;
    private final int tileRow;
    private boolean isMouseHoveringOverTile;
    private boolean isSelected;
    private boolean isResourceTile;

    private TileImage tileImage;

    public TilePanel(final int tileCol, final int tileRow, final int tileSize)
    {
        super();

        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.isResourceTile = false;
        this.isMouseHoveringOverTile = false;
        this.isSelected = false;
        this.tileImage = null;

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
        if (e.getButton() == MouseEvent.BUTTON1)
        {
            mouseLeftDown = true;
            OnMouseLeftPressAndHold();
        }
        else if (e.getButton() == MouseEvent.BUTTON3)
        {
            mouseRightDown = true;
            OnMouseRightPressAndHold();
        }

        getRootPane().revalidate();
        getRootPane().repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1)
        {
            mouseLeftDown = false;
        }
        else if (e.getButton() == MouseEvent.BUTTON3)
        {
            mouseRightDown = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        this.isMouseHoveringOverTile = true;

        if (mouseLeftDown)
        {
            OnMouseLeftPressAndHold();
        }
        else if (mouseRightDown)
        {
            OnMouseRightPressAndHold();
        }

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
    public boolean isSelected() { return this.isSelected; }

    public int getCol()
    {
        return this.tileCol;
    }

    public int getRow()
    {
        return this.tileRow;
    }

    public boolean isResourceTile()
    {
        return this.isResourceTile;
    }

    public String getCoordsString(final int numberOfRows)
    {
        return this.tileCol + "," + (numberOfRows - this.tileRow);
    }

    public TileImage getTileImage()
    {
        return this.tileImage;
    }

    public void setTileImage(final TileImage image)
    {
        this.tileImage = image;
    }

    public void setIsSelected(final boolean isSelected)
    {
        this.isSelected = isSelected;
    }

    public void setIsResourceTile(final boolean isResourceTile)
    {
        this.isResourceTile = isResourceTile;
    }

    private void OnMouseLeftPressAndHold()
    {
        ((BaseTilemapPanel)getParent()).deselectAllTiles();

        if (isResourceTile && isMouseHoveringOverTile)
        {

            isSelected = true;
            TilePanel.selectedResourceImage = this.tileImage;
        }
        else if (isResourceTile == false)
        {
            CommandManager.executeCommand(new SetLevelEditorTileImageCommand(this, TilePanel.selectedResourceImage));
        }
    }

    private void OnMouseRightPressAndHold()
    {
        ((BaseTilemapPanel)getParent()).deselectAllTiles();

        if (isResourceTile == false)
        {
            CommandManager.executeCommand(new ClearLevelEditorTileImageCommand(this));
        }
    }

}
