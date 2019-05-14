package com.retroleveleditor.action_listeners;

import com.retroleveleditor.panels.BaseTilemapPanel;
import com.retroleveleditor.panels.MainPanel;
import com.retroleveleditor.panels.ResourceTilemapPanel;
import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.CharacterAtlasEntryDescriptor;
import com.retroleveleditor.util.CharacterMovementType;
import com.retroleveleditor.util.NpcInteractionParameters;
import com.retroleveleditor.util.Pair;

import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class SaveActionListener implements ActionListener
{
    public static String resourceDirectoryChooserOriginPath = ".";
    private static final String LEVEL_FILE_EXTENSION = ".json";
    private static final String OPTIMIZED_GROUND_LAYER_TEXTURE_NAME = "_groundLayer.png";
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
        Pair<Integer> exportedImageSize = exportOptimizedLevelGroundLayer(file);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file)))
        {
            BaseTilemapPanel levelTilemap = mainPanel.getLevelEditorTilemap();

            StringBuilder fileContentsBuilder = new StringBuilder();
            fileContentsBuilder.append("{\n");
            fileContentsBuilder.append("    \"level_header\":\n");
            fileContentsBuilder.append("    {\n");
            fileContentsBuilder.append("        \"name\": \"" + file.getName().split("\\.")[0] + "\",\n");
            fileContentsBuilder.append("        \"dimensions\": { \"cols\": " + levelTilemap.getTileCols() + ", \"rows\": " + levelTilemap.getTileRows() + "},\n");
            fileContentsBuilder.append("        \"color\": \"PALLET\"\n");
            fileContentsBuilder.append("    },\n");
            fileContentsBuilder.append("    \"level_ground_layer_editor\":\n");
            fileContentsBuilder.append("    [\n");

            Component[] components = levelTilemap.getComponents();
            for (Component component: components)
            {
                if (component instanceof TilePanel)
                {
                    TilePanel tile = (TilePanel)component;
                    if (tile.getDefaultTileImage() != null && tile.getDefaultTileImage().modelName.length() == 0)
                    {
                        fileContentsBuilder.append("        { \"editor_col\": " + tile.getCol() +
                                                           ", \"editor_row\": " + tile.getRow() +
                                                           ", \"game_position_x\": " + String.format("%.1f", (tile.getGameOverworldCol() * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                                           ", \"game_position_z\": " + String.format("%.1f", (tile.getGameOverworldRow(levelTilemap.getTileRows()) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                                           ", \"atlas_col\": " + tile.getDefaultTileImage().atlasCol +
                                                           ", \"atlas_row\": " + tile.getDefaultTileImage().atlasRow + " },\n");
                    }
                }
            }

            // Delete trailing comma on final entry
            if (fileContentsBuilder.charAt(fileContentsBuilder.length() - 2) == ',')
            {
                fileContentsBuilder.deleteCharAt(fileContentsBuilder.length() - 2);
            }
            fileContentsBuilder.append("    ],\n");


            fileContentsBuilder.append("    \"level_ground_layer_game\":\n");
            fileContentsBuilder.append("    [\n");
            fileContentsBuilder.append("        { \"texture_name\": \"" + (file.getName().split("\\.")[0] + OPTIMIZED_GROUND_LAYER_TEXTURE_NAME) + "\"" +
                                               ", \"game_position_x\": " + String.format("%.1f", (((exportedImageSize.x/16) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)/2.0f)) +
                                               ", \"game_position_z\": " + String.format("%.1f", (((exportedImageSize.y/16) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)/2.0f)) +
                                                   " }\n");
            fileContentsBuilder.append("    ],\n");

            // Save npcs
            fileContentsBuilder.append("    \"level_npc_list\":\n");
            fileContentsBuilder.append("    [\n");

            Map<String, NpcInteractionParameters> legacyInteractionData = extractLegacyInteractionData();
            Map<String, CharacterMovementType> characterMovementTypes = extractMovementTypes();

            for (Component component: components)
            {
                if (component instanceof TilePanel)
                {
                    TilePanel tile = (TilePanel)component;
                    if (tile.getCharTileImage() != null)
                    {
                        String npcDataKey = file.getName().split("\\.")[0] + "-" + tile.getGameOverworldCol() + "," + tile.getGameOverworldRow(levelTilemap.getTileRows());
                        String npcData = "";

                        if (legacyInteractionData.containsKey(npcDataKey))
                        {
                            NpcInteractionParameters legacyParams = legacyInteractionData.get(npcDataKey);
                            npcData = ", \"dialog\": \"" + legacyParams.dialog + "\"" +
                                      ", \"direction\": " + legacyParams.direction;
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(mainPanel, "Could extract interaction parameters for npc at: " + tile.getGameOverworldCol() + ", " + tile.getGameOverworldRow(levelTilemap.getTileRows()));
                        }

                        fileContentsBuilder.append("        { \"movement_type\": \"" + characterMovementTypes.get(tile.getCharTileImage().atlasCol + "," + tile.getCharTileImage().atlasRow).toString() + "\"" +
                                npcData +
                                ", \"editor_col\": " + tile.getCol() +
                                ", \"editor_row\": " + tile.getRow() +
                                ", \"game_col\": " + tile.getGameOverworldCol() +
                                ", \"game_row\": " + tile.getGameOverworldRow(levelTilemap.getTileRows()) +
                                ", \"game_position_x\": " + String.format("%.1f", (tile.getGameOverworldCol() * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                ", \"game_position_z\": " + String.format("%.1f", (tile.getGameOverworldRow(levelTilemap.getTileRows()) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                ", \"atlas_col\": " + tile.getCharTileImage().atlasCol +
                                ", \"atlas_row\": " + tile.getCharTileImage().atlasRow + " },\n");
                    }
                }
            }

            for (Map.Entry<String, NpcInteractionParameters> entry: legacyInteractionData.entrySet())
            {
                if (entry.getKey().startsWith(file.getName().split("\\.")[0]) && entry.getValue().direction == -1)
                {
                    String npcData = ", \"dialog\": \"" + entry.getValue().dialog + "\"" +
                            ", \"direction\": " + entry.getValue().direction;

                    fileContentsBuilder.append("        { \"movement_type\": \"STATIC\"" +
                            npcData +
                            ", \"editor_col\": " + 0 +
                            ", \"editor_row\": " + 0 +
                            ", \"game_col\": " + entry.getValue().coordsString.split(",")[0] +
                            ", \"game_row\": " + entry.getValue().coordsString.split(",")[1] +
                            ", \"game_position_x\": " + String.format("%.1f", (Integer.parseInt(entry.getValue().coordsString.split(",")[0]) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                            ", \"game_position_z\": " + String.format("%.1f", (Integer.parseInt(entry.getValue().coordsString.split(",")[1]) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                            ", \"atlas_col\": " + -1 +
                            ", \"atlas_row\": " + -1 + " },\n");
                }
            }

            // Delete trailing comma on final entry
            if (fileContentsBuilder.charAt(fileContentsBuilder.length() - 2) == ',')
            {
                fileContentsBuilder.deleteCharAt(fileContentsBuilder.length() - 2);
            }
            fileContentsBuilder.append("    ],\n");

            // Save models
            fileContentsBuilder.append("    \"level_model_list\":\n");
            fileContentsBuilder.append("    [\n");

            List<TilePanel> modelTiles = new ArrayList();
            for (Component component: components)
            {
                if (component instanceof TilePanel) {
                    TilePanel tile = (TilePanel) component;
                    if (tile.getDefaultTileImage() != null && tile.getDefaultTileImage().modelName.length() > 0)
                    {
                        modelTiles.add(tile);
                    }
                }
            }

            modelTiles.sort(new Comparator<TilePanel>()
            {
                @Override
                public int compare(TilePanel tileA, TilePanel tileB)
                {
                    return tileA.getDefaultTileImage().modelName.compareTo(tileB.getDefaultTileImage().modelName);
                }
            });

            for (TilePanel tile: modelTiles)
            {
                fileContentsBuilder.append("        " +
                        "{ \"model_name\": \"" + tile.getDefaultTileImage().modelName + "\"" +
                        ", \"editor_col\": " + tile.getCol() +
                        ", \"editor_row\": " + tile.getRow() +
                        ", \"game_col\": " + tile.getGameOverworldCol() +
                        ", \"game_row\": " + tile.getGameOverworldRow(levelTilemap.getTileRows()) +
                        ", \"game_position_x\": " + String.format("%.1f", (tile.getGameOverworldCol() * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                        ", \"game_position_z\": " + String.format("%.1f", (tile.getGameOverworldRow(levelTilemap.getTileRows()) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) + " },\n");
            }

            // Delete trailing comma on final entry
            if (fileContentsBuilder.charAt(fileContentsBuilder.length() - 2) == ',')
            {
                fileContentsBuilder.deleteCharAt(fileContentsBuilder.length() - 2);
            }
            fileContentsBuilder.append("    ],\n");

            // Save tile traits
            fileContentsBuilder.append("    \"level_tile_traits\":\n");
            fileContentsBuilder.append("    [\n");

            for (Component component: components)
            {
                if (component instanceof TilePanel)
                {
                    TilePanel tile = (TilePanel)component;
                    if (tile.getTileTraits() != TilePanel.TileTraits.NONE)
                    {
                        fileContentsBuilder.append("        " +
                                "{ \"tile_traits\": \"" + tile.getTileTraits().toString() + "\"" +
                                ", \"editor_col\": " + tile.getCol() +
                                ", \"editor_row\": " + tile.getRow() +
                                ", \"game_col\": " + tile.getGameOverworldCol() +
                                ", \"game_row\": " + tile.getGameOverworldRow(levelTilemap.getTileRows()) +
                                ", \"game_position_x\": " + String.format("%.1f", (tile.getGameOverworldCol() * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                ", \"game_position_z\": " + String.format("%.1f", (tile.getGameOverworldRow(levelTilemap.getTileRows()) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) + " },\n");
                    }
                }
            }

            // Delete trailing comma on final entry
            if (fileContentsBuilder.charAt(fileContentsBuilder.length() - 2) == ',')
            {
                fileContentsBuilder.deleteCharAt(fileContentsBuilder.length() - 2);
            }
            fileContentsBuilder.append("    ]\n");

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

    private Pair<Integer> exportOptimizedLevelGroundLayer(final File levelFilePath)
    {
        BaseTilemapPanel levelTilemap = mainPanel.getLevelEditorTilemap();

        int powerOfTwoCounter = 16;
        int targetWidth = powerOfTwoCounter;
        int targetHeight = powerOfTwoCounter;

        while (powerOfTwoCounter < 2048)
        {
            if (levelTilemap.getTileCols() * 16 <= powerOfTwoCounter && levelTilemap.getTileRows() * 16 <= powerOfTwoCounter)
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

        String exportedImagePath = mainPanel.getResourceRootDirectory() + MainPanel.TEXTURES_RELATIVE_DIRECTORY + levelFilePath.getName().split("\\.")[0] + OPTIMIZED_GROUND_LAYER_TEXTURE_NAME;
        File file = new File(exportedImagePath);
        try
        {
            ImageIO.write(groundLayerImage, "png", file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return new Pair<Integer>(targetWidth, targetHeight);
    }

    private Map<String, CharacterMovementType> extractMovementTypes()
    {
        Map<String, CharacterMovementType> result = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(mainPanel.getGameDataDirectoryPath() + "npcs_atlas_coords.dat")))
        {
            String line = null;
            while ((line = br.readLine()) != null)
            {
                String[] lineComponents = line.split(",");
                result.put((lineComponents[0] + "," + lineComponents[1]), CharacterMovementType.valueOf(lineComponents[2]));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;

    }

    private Map<String, NpcInteractionParameters> extractLegacyInteractionData()
    {
        Map<String, NpcInteractionParameters> result = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(mainPanel.getGameDataDirectoryPath() + "npcs_properties.dat")))
        {
            String line = null;
            while ((line = br.readLine()) != null)
            {
                if (line.startsWith("--"))
                {
                    break;
                }

                String[] dialogSplit = line.split("\\{");
                String[] lineComponents = line.split(" ");
                String resultKey = lineComponents[0] + "-" + lineComponents[1];
                result.put(resultKey, new NpcInteractionParameters(dialogSplit[1].substring(0, dialogSplit[1].length() - 1), lineComponents[1], Integer.parseInt(lineComponents[2])));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
