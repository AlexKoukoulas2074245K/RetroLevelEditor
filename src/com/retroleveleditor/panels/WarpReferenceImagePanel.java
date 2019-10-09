package com.retroleveleditor.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class WarpReferenceImagePanel extends JPanel
{
    private static final int REFERENCE_IMAGE_COLS      = 9;
    private static final int REFERENCE_IMAGE_ROWS      = 9;
    private static final int REFERENCE_IMAGE_TILE_SIZE = 48;

    private final MainPanel mainPanel;
    private final TilePanel tilePanel;
    private BufferedImage generatedReferenceImage;

    public WarpReferenceImagePanel(final MainPanel mainPanel, final TilePanel originTile)
    {
        this.mainPanel = mainPanel;
        this.tilePanel = originTile;

        int mainPanelWidth  = mainPanel.getLevelEditorTilemap().getWidth();
        int mainPanelHeight = mainPanel.getLevelEditorTilemap().getHeight();
        this.generatedReferenceImage = new BufferedImage(mainPanelWidth, mainPanelHeight, BufferedImage.TYPE_INT_ARGB);

        setPreferredSize(new Dimension(REFERENCE_IMAGE_COLS * REFERENCE_IMAGE_TILE_SIZE, REFERENCE_IMAGE_ROWS * REFERENCE_IMAGE_TILE_SIZE));
        Graphics2D gfx = generatedReferenceImage.createGraphics();

        mainPanel.getLevelEditorTilemap().paint(generatedReferenceImage.getGraphics());

        int subImageX = Math.max(0, tilePanel.getX() - REFERENCE_IMAGE_COLS/2 * tilePanel.getWidth());
        int subImageY = Math.max(0, tilePanel.getY() - REFERENCE_IMAGE_ROWS/2 * tilePanel.getHeight());
        int subImageWidth = tilePanel.getWidth() * REFERENCE_IMAGE_COLS + 10;
        int subImageHeight = tilePanel.getHeight() * REFERENCE_IMAGE_ROWS;

        if (subImageX + subImageWidth > generatedReferenceImage.getWidth())
        {
            subImageWidth = generatedReferenceImage.getWidth() - subImageX;
        }
        if (subImageY + subImageHeight > generatedReferenceImage.getHeight())
        {
            subImageHeight = generatedReferenceImage.getHeight() - subImageY;
        }

        generatedReferenceImage = generatedReferenceImage.getSubimage(subImageX, subImageY, subImageWidth, subImageHeight);

        setBorder(new EmptyBorder(0, 10, 0, 0));
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setColor(new Color(24, 24, 24, 255));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(generatedReferenceImage, 0, 0, null);
        g.drawImage(ResourceTilemapPanel.SELECTION_IMAGE, REFERENCE_IMAGE_COLS/2 * tilePanel.getWidth(), REFERENCE_IMAGE_ROWS/2 * tilePanel.getHeight(), tilePanel.getWidth(), tilePanel.getHeight(), null);
    }
}
