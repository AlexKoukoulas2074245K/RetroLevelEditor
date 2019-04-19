package com.retroleveleditor.panels;

import com.retroleveleditor.commands.ClearLevelEditorTileImagesCommand;
import com.retroleveleditor.commands.CommandManager;
import com.retroleveleditor.commands.SetLevelEditorTileImageCommand;
import com.retroleveleditor.util.TileImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TilePanel extends JPanel implements MouseListener
{
    public static TilePanel selectedResourceTile = null;
    private static boolean mouseLeftDown = false;
    private static boolean mouseRightDown = false;

    private final int tileCol;
    private final int tileRow;
    private boolean isMouseHoveringOverTile;
    private boolean isSelected;
    private boolean isResourceTile;

    private TileImage defaultTileImage;
    private TileImage charTileImage;

    public TilePanel(final int tileCol, final int tileRow, final int tileSize)
    {
        super();

        this.tileCol = tileCol;
        this.tileRow = tileRow;
        this.isResourceTile = false;
        this.isMouseHoveringOverTile = false;
        this.isSelected = false;
        this.defaultTileImage = null;
        this.charTileImage = null;

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

    public TileImage getDefaultTileImage()
    {
        return this.defaultTileImage;
    }

    public TileImage getCharTileImage()
    {
        return this.charTileImage;
    }

    public void setDefaultTileImage(final TileImage image)
    {
        this.defaultTileImage = image;
    }

    public void setCharTileImage(final TileImage image)
    {
        this.charTileImage = image;
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
            TilePanel.selectedResourceTile = this;
        }
        else if (isResourceTile == false)
        {
            ResourceTilemapPanel currentResourcePanel = (ResourceTilemapPanel)TilePanel.selectedResourceTile.getParent();

            // If the current resource tab is the characters one
            if (currentResourcePanel.getAtlasPath() == MainPanel.CHARACTERS_ATLAS_PATH)
            {
                CommandManager.executeCommand(new SetLevelEditorTileImageCommand(this, this.defaultTileImage, TilePanel.selectedResourceTile.getDefaultTileImage()));
            }
            else
            {
                CommandManager.executeCommand(new SetLevelEditorTileImageCommand(this, TilePanel.selectedResourceTile.getDefaultTileImage(), this.charTileImage));
            }

        }
    }

    private void OnMouseRightPressAndHold()
    {
        ((BaseTilemapPanel)getParent()).deselectAllTiles();

        if (isResourceTile == false)
        {
            CommandManager.executeCommand(new ClearLevelEditorTileImagesCommand(this));
        }
    }

}
