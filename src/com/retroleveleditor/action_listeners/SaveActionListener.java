package com.retroleveleditor.action_listeners;

import com.retroleveleditor.panels.BaseTilemapPanel;
import com.retroleveleditor.panels.MainPanel;
import com.retroleveleditor.panels.TilePanel;
import com.sun.xml.internal.rngom.parse.host.Base;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveActionListener implements ActionListener
{
    private static final String LEVEL_FILE_EXTENSION = ".json";
    public static String resourceDirectoryChooserOriginPath = ".";

    private final MainPanel mainPanel;
    private final boolean shouldAlwaysSaveToDifferentLocation;

    public SaveActionListener(final MainPanel mainPanel, final boolean shouldAlwaysSaveToDifferentLocation)
    {
        this.mainPanel = mainPanel;
        this.shouldAlwaysSaveToDifferentLocation = shouldAlwaysSaveToDifferentLocation;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (shouldAlwaysSaveToDifferentLocation == false && mainPanel.getCurrentWorkingFile() != null)
        {
            saveLevelToFile(mainPanel.getCurrentWorkingFile());
        }
        else
        {
            JFileChooser fc = new JFileChooser(resourceDirectoryChooserOriginPath);
            FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("JSON (*.json)", "json");
            fc.setFileFilter(fileFilter);

            int choice = fc.showSaveDialog(mainPanel);
            if (choice == JFileChooser.APPROVE_OPTION)
            {
                File selFile = fc.getSelectedFile();
                File adjustedFile = selFile;

                if (!selFile.getName().endsWith(LEVEL_FILE_EXTENSION))
                {
                    adjustedFile = new File(selFile.getAbsolutePath() + LEVEL_FILE_EXTENSION);
                }

                if (adjustedFile.exists())
                {
                    int selOption = JOptionPane.showConfirmDialog (null, "Overwrite existing file?", "Save Option", JOptionPane.YES_NO_OPTION);
                    if (selOption == JOptionPane.YES_OPTION)
                    {
                        saveLevelToFile(adjustedFile);
                        JOptionPane.showMessageDialog(mainPanel, "Successfully saved level at: " + adjustedFile.getAbsolutePath(), "Save Level", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else
                {
                    saveLevelToFile(adjustedFile);
                    JOptionPane.showMessageDialog(mainPanel, "Successfully saved level at: " + adjustedFile.getAbsolutePath(), "Save Level", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

    }

    private void saveLevelToFile(final File file)
    {
        exportOptimizedLevelGroundLayer(file);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file)))
        {
            BaseTilemapPanel levelTilemap = mainPanel.getLevelEditorTilemap();

            StringBuilder fileContentsBuilder = new StringBuilder();
            fileContentsBuilder.append("{\n");

            fileContentsBuilder.append("    {\n");
            fileContentsBuilder.append("        \"dimensions\" : { \"cols\": " + levelTilemap.getTileCols() + ", \"rows\": " + levelTilemap.getTileRows() + "},\n");
            fileContentsBuilder.append("    }\n");

            fileContentsBuilder.append("}\n");
            bw.write(fileContentsBuilder.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        mainPanel.setCurrentWorkingFile(file);
        SaveActionListener.resourceDirectoryChooserOriginPath = file.getAbsolutePath();
    }

    private void exportOptimizedLevelGroundLayer(final File levelFilePath)
    {
        BaseTilemapPanel levelTilemap = mainPanel.getLevelEditorTilemap();

        int powerOfTwoCounter = 16;
        int targetWidth = powerOfTwoCounter;
        int targetHeight = powerOfTwoCounter;

        while (powerOfTwoCounter < 2048)
        {
            if (levelTilemap.getTileCols() * 16 < powerOfTwoCounter && levelTilemap.getTileRows() * 16 < powerOfTwoCounter)
            {
                targetWidth = powerOfTwoCounter;
                targetHeight = powerOfTwoCounter;
                break;
            }
            powerOfTwoCounter *= 2;
        }

        BufferedImage emptyTileImage = null;
        try
        {
            emptyTileImage = ImageIO.read(getClass().getResourceAsStream("/empty_tile.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        BufferedImage groundLayerImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gfx = groundLayerImage.createGraphics();

        final int colDifference = targetWidth/16 - levelTilemap.getTileCols();
        final int rowDifference = targetHeight/16 - levelTilemap.getTileRows();

        final int colStart = -colDifference/2;
        final int rowStart = -rowDifference/2;
        final int colEnd = levelTilemap.getTileCols() + ((colDifference % 2 == 0) ? colDifference/2 : colDifference/2 + 1);
        final int rowEnd = levelTilemap.getTileRows() + ((rowDifference % 2 == 0) ? rowDifference/2 : rowDifference/2 + 1);

        int renderTargetColIndex = 0;
        int renderTargetRowIndex = 0;

        for (int y = rowStart; y <= rowEnd; ++y)
        {
            for (int x = colStart; x < colEnd; ++x)
            {
                if (y < levelTilemap.getTileRows() && x < levelTilemap.getTileCols() && y >= 0 && x >= 0)
                {
                    TilePanel respectiveTile = levelTilemap.getTileAtCoords(x, y);
                    if (respectiveTile.getDefaultTileImage() != null && respectiveTile.getDefaultTileImage().modelName.length() == 0)
                    {
                        gfx.drawImage(respectiveTile.getDefaultTileImage().image, renderTargetColIndex * 16, renderTargetRowIndex * 16, 16, 16, null);
                    }
                    else
                    {
                        gfx.drawImage(emptyTileImage, renderTargetColIndex * 16, renderTargetRowIndex * 16, 16, 16, null);
                    }
                }
                else
                {
                    gfx.drawImage(emptyTileImage, renderTargetColIndex * 16, renderTargetRowIndex * 16, 16, 16, null);
                }

                if (++renderTargetColIndex >= targetWidth/16)
                {
                    renderTargetColIndex = 0;
                    renderTargetRowIndex++;
                }
            }
        }

        File file = new File(levelFilePath.getAbsolutePath().substring(0, levelFilePath.getAbsolutePath().length() - 5) + "_groundLayer.png");
        try
        {
            ImageIO.write(groundLayerImage, "png", file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
