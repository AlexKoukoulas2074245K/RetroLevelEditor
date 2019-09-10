package com.retroleveleditor.panels;

import com.retroleveleditor.action_listeners.SetNpcAttributesActionListener;
import com.retroleveleditor.commands.ClearLevelEditorTileCommand;
import com.retroleveleditor.commands.CommandManager;
import com.retroleveleditor.commands.SetLevelEditorTileImageCommand;
import com.retroleveleditor.commands.SetLevelEditorTileTraitsCommand;
import com.retroleveleditor.main.MainFrame;
import com.retroleveleditor.util.NpcAttributes;
import com.retroleveleditor.util.TileImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class TilePanel extends JPanel implements MouseListener, MouseMotionListener
{
    public enum TileTraits
    {
        UNUSED,
        NONE,
        SOLID,
        WARP,
        NO_ANIM_WARP,
        PRESS_WARP,
        ENCOUNTER,
        JUMPING_LEDGE_BOT,
        JUMPING_LEDGE_LEFT,
        JUMPING_LEDGE_RIGHT,
        SEA_TILE_EDGE,
        FLOW_TRIGGER,
        CUTTABLE_TREE,
        PUSHABLE_ROCK
    }

    public static TilePanel selectedResourceTile = null;
    private static boolean mouseLeftDown = false;
    private static boolean mouseRightDown = false;

    private final int tileCol;
    private final int tileRow;
    private boolean isMouseHoveringOverTile;
    private boolean isSelected;
    private boolean isResourceTile;
    private boolean isFillerTile;

    private TileTraits tileTraits;
    private TileImage defaultTileImage;
    private TileImage charTileImage;
    private NpcAttributes npcAttributes;

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
        this.tileTraits = TileTraits.NONE;

        addMouseListener(this);
        addMouseMotionListener(this);
        setPreferredSize(new Dimension(tileSize, tileSize));
    }

    @Override
    public void paintComponent(Graphics g)
    {
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

    @Override
    public void mouseDragged(MouseEvent e)
    {
        getRootPane().revalidate();
        getRootPane().repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        getRootPane().revalidate();
        getRootPane().repaint();
    }

    public boolean isMouseHoveringOverTile()
    {
        return this.isMouseHoveringOverTile;
    }
    public boolean isSelected() { return this.isSelected; }
    public boolean isFillerTile() { return this.isFillerTile; }

    public int getCol()
    {
        return this.tileCol;
    }

    public int getRow()
    {
        return this.tileRow;
    }

    public int getGameOverworldCol() { return this.tileCol; }

    public int getGameOverworldRow(final int numberOfRows) { return numberOfRows - 1 - this.tileRow; }

    public String getCoordsString(final int numberOfRows)
    {
        return getGameOverworldCol() + "," + getGameOverworldRow(numberOfRows);
    }

    public boolean isResourceTile()
    {
        return this.isResourceTile;
    }

    public TileImage getDefaultTileImage()
    {
        return this.defaultTileImage;
    }

    public TileImage getCharTileImage()
    {
        return this.charTileImage;
    }

    public TileTraits getTileTraits() { return this.tileTraits; }

    public NpcAttributes getNpcAttributes() { return this.npcAttributes; }

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

    public void setIsFillerTile(final boolean isFillerTile) { this.isFillerTile = isFillerTile; }

    public void setIsResourceTile(final boolean isResourceTile)
    {
        this.isResourceTile = isResourceTile;
    }

    public void setTileTraits(final TileTraits tileTraits) { this.tileTraits = tileTraits; }

    public void setNpcAttributes(final NpcAttributes npcAttributes) { this.npcAttributes = npcAttributes; }

    private void OnMouseLeftPressAndHold()
    {
        ((BaseTilemapPanel)getParent()).deselectAllTiles();

        if (isResourceTile && isMouseHoveringOverTile && this.tileTraits != TileTraits.UNUSED)
        {
            isSelected = true;
            TilePanel.selectedResourceTile = this;
        }
        else if (isResourceTile == false)
        {
            ResourceTilemapPanel currentResourcePanel = (ResourceTilemapPanel)TilePanel.selectedResourceTile.getParent();

            // If the current resource tab is the characters one
            if (currentResourcePanel.isTraitsPanel())
            {
                CommandManager.executeCommand(new SetLevelEditorTileTraitsCommand(this, TilePanel.selectedResourceTile.getTileTraits()));
            }
            else if (currentResourcePanel.isModelsPanel())
            {
                CommandManager.executeCommand(new SetLevelEditorTileImageCommand(this, TilePanel.selectedResourceTile.getDefaultTileImage(), this.charTileImage));
            }
            else if (currentResourcePanel.getAtlasPath().endsWith(MainPanel.CHARACTERS_ATLAS_RELATIVE_PATH))
            {
                beginCharacterPlacementFlow();
            }
            else
            {
                CommandManager.executeCommand(new SetLevelEditorTileImageCommand(this, TilePanel.selectedResourceTile.getDefaultTileImage(), this.charTileImage));
            }

        }
    }

    private void OnMouseRightPressAndHold()
    {
        if (isResourceTile == false)
        {
            ((BaseTilemapPanel)getParent()).deselectAllTiles();
            CommandManager.executeCommand(new ClearLevelEditorTileCommand(this));
        }
    }

    private void beginCharacterPlacementFlow()
    {
        CommandManager.executeCommand(new SetLevelEditorTileImageCommand(this, this.defaultTileImage, TilePanel.selectedResourceTile.getCharTileImage()));
    }

}
