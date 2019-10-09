package com.retroleveleditor.action_listeners;

import com.retroleveleditor.panels.*;
import com.retroleveleditor.util.Colors;
import com.retroleveleditor.util.DisposeDialogHandler;
import com.retroleveleditor.util.Pair;
import com.retroleveleditor.util.PokemonInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SaveActionListener implements ActionListener
{
    class ParentLocationWrapper
    {
        String parentLocation;
    }

    public static String resourceDirectoryChooserOriginPath = ".";
    private static final String LEVEL_FILE_EXTENSION = ".json";
    private static final String TOWN_MAP_LOCATIONS_FILE_NAME = "town_map_locations.json";
    private static final String UNDERGROUND_MODELS_FILE_NAME = "underground_model_names.json";
    private static final String OPTIMIZED_GROUND_LAYER_TEXTURE_NAME = "_groundLayer.png";
    private static Image TRANSPARENT_TILE_IMAGE = null;
    static
    {
        try
        {
            TRANSPARENT_TILE_IMAGE = ImageIO.read(ResourceTilemapPanel.class.getResourceAsStream("/transparent_tile.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private final MainPanel mainPanel;
    private final boolean shouldAlwaysSaveToDifferentLocation;
    private final List<String> undergroundModelNames;

    public SaveActionListener(final MainPanel mainPanel, final boolean shouldAlwaysSaveToDifferentLocation)
    {
        this.mainPanel = mainPanel;
        this.shouldAlwaysSaveToDifferentLocation = shouldAlwaysSaveToDifferentLocation;
        this.undergroundModelNames = extractUndergroundModelNames();
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
            JFileChooser fc = new JFileChooser(mainPanel.getGameLevelsDirectoryPath());
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
        if (((LevelEditorTilemapPanel)mainPanel.getLevelEditorTilemap()).getLevelMusicName() == null)
        {
            int selOption = JOptionPane.showConfirmDialog (null, "Select music for level?", "Music Selection", JOptionPane.YES_NO_OPTION);
            if (selOption == JOptionPane.YES_OPTION)
            {
                JFileChooser fc = new JFileChooser(mainPanel.getGameMusicDirectoryPath());
                FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("OGG (*.ogg)", "ogg");
                fc.setFileFilter(fileFilter);

                int choice = fc.showSaveDialog(mainPanel);
                if (choice == JFileChooser.APPROVE_OPTION)
                {
                    String musicName = fc.getSelectedFile().getName().split("\\.")[0];
                    ((LevelEditorTilemapPanel)mainPanel.getLevelEditorTilemap()).setLevelMusicName(musicName);
                }
            }
        }

        if (file.getName().startsWith("in_"))
        {
            if (doesLocationExistInTownMapLocationsFile(file) == false)
            {
                int selOption = JOptionPane.showConfirmDialog (null, "Select owner level location?", "Owner Level Selection", JOptionPane.YES_NO_OPTION);
                if (selOption == JOptionPane.YES_OPTION)
                {
                    selectOwnerLevelLocation(file);
                }
            }
        }

        Pair<Integer> exportedImageSize = exportOptimizedLevelGroundLayer(file);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file)))
        {
            TilePanel fillerTile = null;
            Component[] resourceEnvComponents = mainPanel.getEnvironmentsPanel().getComponents();
            for (Component component: resourceEnvComponents)
            {
                if (component instanceof TilePanel)
                {
                    if (((TilePanel)component).isFillerTile())
                    {
                        fillerTile = ((TilePanel)component);
                        break;
                    }
                }
            }

            BaseTilemapPanel levelTilemap = mainPanel.getLevelEditorTilemap();

            StringBuilder fileContentsBuilder = new StringBuilder();
            fileContentsBuilder.append("{\n");
            fileContentsBuilder.append("    \"level_header\":\n");
            fileContentsBuilder.append("    {\n");
            fileContentsBuilder.append("        \"name\": \"" + file.getName().split("\\.")[0] + "\",\n");
            fileContentsBuilder.append("        \"dimensions\": { \"cols\": " + levelTilemap.getTileCols() + ", \"rows\": " + levelTilemap.getTileRows() + "},\n");
            fileContentsBuilder.append("        \"color\": \"" + ((LevelEditorTilemapPanel)levelTilemap).getLevelColor().getName() + "\"");
            if (((LevelEditorTilemapPanel)levelTilemap).getLevelMusicName() == null)
            {
                fileContentsBuilder.append("\n");
            }
            else
            {
                fileContentsBuilder.append(",\n");
                fileContentsBuilder.append("        \"music\": \"" + ((LevelEditorTilemapPanel)levelTilemap).getLevelMusicName() + "\"\n");
            }
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
                    else if (tile.getDefaultTileImage() != null && tile.getDefaultTileImage().modelName.length() > 0)
                    {
                        fileContentsBuilder.append("        { \"editor_col\": " + tile.getCol() +
                                                           ", \"editor_row\": " + tile.getRow() +
                                                           ", \"game_position_x\": " + String.format("%.1f", (tile.getGameOverworldCol() * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                                           ", \"game_position_z\": " + String.format("%.1f", (tile.getGameOverworldRow(levelTilemap.getTileRows()) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                                           ", \"atlas_col\": " + fillerTile.getDefaultTileImage().atlasCol +
                                                           ", \"atlas_row\": " + fillerTile.getDefaultTileImage().atlasRow + " },\n");
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


            // Save npc attributes
            fileContentsBuilder.append("    \"level_npc_attributes\":\n");
            fileContentsBuilder.append("    [\n");

            for (Component component: components)
            {
                if (component instanceof TilePanel)
                {
                    TilePanel tile = (TilePanel) component;

                    if (tile.getNpcAttributes() != null)
                    {
                        StringBuilder sideDialogStringBuilder = new StringBuilder();
                        sideDialogStringBuilder.append('[');
                        for (String dialog: tile.getNpcAttributes().sideDialogs)
                        {
                            sideDialogStringBuilder.append("\"");
                            sideDialogStringBuilder.append(dialog);
                            sideDialogStringBuilder.append("\"");
                            sideDialogStringBuilder.append(',');
                        }
                        // Delete trailing comma on final entry
                        sideDialogStringBuilder.append(']');
                        if (sideDialogStringBuilder.length() > 2)
                        {
                            if (sideDialogStringBuilder.charAt(sideDialogStringBuilder.length() - 2) == ',')
                            {
                                sideDialogStringBuilder.deleteCharAt(sideDialogStringBuilder.length() - 2);
                            }
                        }

                        StringBuilder pokemonRosterStringBuilder = new StringBuilder();
                        pokemonRosterStringBuilder.append('[');
                        for (PokemonInfo pokemonInfo: tile.getNpcAttributes().pokemonRoster)
                        {
                            pokemonRosterStringBuilder.append("{ \"name\": \"" + pokemonInfo.pokemonName + "\", \"level\": " + pokemonInfo.pokemonLevel + " },");
                        }

                        pokemonRosterStringBuilder.append(']');
                        if (pokemonRosterStringBuilder.length() > 2)
                        {
                            if (pokemonRosterStringBuilder.charAt(pokemonRosterStringBuilder.length() - 2) == ',')
                            {
                                pokemonRosterStringBuilder.deleteCharAt(pokemonRosterStringBuilder.length() - 2);
                            }
                        }

                        fileContentsBuilder.append("         {" +
                                " \"movement_type\": \"" + tile.getNpcAttributes().movementType.toString() + "\"" +
                                ", \"direction\": " + tile.getNpcAttributes().direction +
                                ", \"is_trainer\": " + (tile.getNpcAttributes().isTrainer ? "true" : "false") +
                                ", \"is_gym_leader\": " + (tile.getNpcAttributes().isGymLeader ? "true" : "false") +
                                ", \"trainer_name\": \"" + tile.getNpcAttributes().trainerName + "\"" +
                                ", \"dialog\": \"" + tile.getNpcAttributes().mainDialog + "\"" +
                                ", \"side_dialogs\": " + sideDialogStringBuilder.toString() +
                                ", \"pokemon_roster\": " + pokemonRosterStringBuilder.toString() +
                                ", \"editor_col\": " + tile.getCol() +
                                ", \"editor_row\": " + tile.getRow() +
                                ", \"game_col\": " + tile.getGameOverworldCol() +
                                ", \"game_row\": " + tile.getGameOverworldRow(levelTilemap.getTileRows()) +
                                ", \"game_position_x\": " + String.format("%.1f", (tile.getGameOverworldCol() * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                ", \"game_position_z\": " + String.format("%.1f", (tile.getGameOverworldRow(levelTilemap.getTileRows()) * ResourceTilemapPanel.GAME_OVERWORLD_TILE_SIZE)) +
                                " },\n");
                    }
                }
            }

            // Delete trailing comma on final entry
            if (fileContentsBuilder.charAt(fileContentsBuilder.length() - 2) == ',')
            {
                fileContentsBuilder.deleteCharAt(fileContentsBuilder.length() - 2);
            }
            fileContentsBuilder.append("    ],\n");

            // Save npcs
            fileContentsBuilder.append("    \"level_npc_sprites\":\n");
            fileContentsBuilder.append("    [\n");

            for (Component component: components)
            {
                if (component instanceof TilePanel)
                {
                    TilePanel tile = (TilePanel)component;
                    if (tile.getCharTileImage() != null)
                    {
                        fileContentsBuilder.append("        { " +
                                " \"editor_col\": " + tile.getCol() +
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

        while (powerOfTwoCounter < 4096)
        {
            if (levelTilemap.getTileCols() * 16 <= powerOfTwoCounter && targetWidth == 16)
            {
                targetWidth = powerOfTwoCounter;
            }
            if (levelTilemap.getTileRows() * 16 <= powerOfTwoCounter && targetHeight == 16)
            {
                targetHeight = powerOfTwoCounter;
            }

            if (targetWidth != 16 && targetHeight != 16)
            {
                break;
            }

            powerOfTwoCounter *= 2;
        }

        Image fillerTileImage = null;
        Component[] resourceTileComponents = mainPanel.getEnvironmentsPanel().getComponents();
        for (Component component: resourceTileComponents)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;

                if (tile.isFillerTile())
                {
                    fillerTileImage = tile.getDefaultTileImage().image;
                }
            }
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
                    else if (respectiveTile.getDefaultTileImage() != null && isUndergroundModel(respectiveTile.getDefaultTileImage().modelName))
                    {
                        gfx.drawImage(TRANSPARENT_TILE_IMAGE, renderTargetColIndex * 16, renderTargetRowIndex * 16, 16, 16, null);
                    }
                    else
                    {
                        gfx.drawImage(fillerTileImage, renderTargetColIndex * 16, renderTargetRowIndex * 16, 16, 16, null);
                    }
                }
                else
                {
                    gfx.drawImage(fillerTileImage, renderTargetColIndex * 16, renderTargetRowIndex * 16, 16, 16, null);
                }

                if (++renderTargetColIndex >= targetWidth/16)
                {
                    renderTargetColIndex = 0;
                    renderTargetRowIndex++;
                }
            }
        }


        String exportedImagePath = mainPanel.getResourceRootDirectory() + MainPanel.TEXTURES_RELATIVE_DIRECTORY + levelFilePath.getName().split("\\.")[0] + OPTIMIZED_GROUND_LAYER_TEXTURE_NAME;
        File exportedImageFile = new File(exportedImagePath);

        if (exportedImageFile.exists())
        {
            int overwriteOption = JOptionPane.showConfirmDialog (null, "Overwrite ground layer texture?", "Overwrite Ground Layer", JOptionPane.YES_NO_OPTION);
            if (overwriteOption == JOptionPane.YES_OPTION)
            {
                try
                {
                    ImageIO.write(groundLayerImage, "png", new File(exportedImagePath));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            try
            {
                ImageIO.write(groundLayerImage, "png", new File(exportedImagePath));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return new Pair<Integer>(targetWidth, targetHeight);
    }

    List<String> extractUndergroundModelNames()
    {
        List<String> undergroundModelNames = new ArrayList<>();

        String fileContents = null;
        try
        {
            fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + UNDERGROUND_MODELS_FILE_NAME).toPath()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject rootJsonObject = new JSONObject(fileContents);
        JSONArray undergroundModelsArray = rootJsonObject.getJSONArray("underground_model_names");
        for (int i = 0; i < undergroundModelsArray.length(); ++i)
        {
            undergroundModelNames.add(undergroundModelsArray.getString(i));
        }

        return undergroundModelNames;
    }

    boolean isUndergroundModel(final String modelName)
    {
        for (String undergroundModelName: undergroundModelNames)
        {
            if (modelName.startsWith(undergroundModelName))
            {
                return true;
            }
        }

        return false;
    }

    boolean doesLocationExistInTownMapLocationsFile(final File file)
    {
        String levelName = file.getName().split("\\.")[0];
        String fileContents = null;
        try
        {
            fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + TOWN_MAP_LOCATIONS_FILE_NAME).toPath()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject rootJsonObject = new JSONObject(fileContents);

        JSONArray indoorLocationsToOwnerLocationsArray = rootJsonObject.getJSONArray("indoor_locations_to_owner_locations");
        for (int i = 0; i < indoorLocationsToOwnerLocationsArray.length(); ++i)
        {
            JSONObject indoorLocationToOwnerLocationJsonObject = indoorLocationsToOwnerLocationsArray.getJSONObject(i);
            if (indoorLocationToOwnerLocationJsonObject.getString("indoor_location_name").equals(levelName))
            {
                return true;
            }
        }

        return false;
    }

    void selectOwnerLevelLocation(final File file)
    {
        // Gather parent locations available
        String levelName = file.getName().split("\\.")[0];
        String fileContents = null;
        try
        {
            fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + TOWN_MAP_LOCATIONS_FILE_NAME).toPath()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject rootJsonObject      = new JSONObject(fileContents);
        JSONArray parentLocationsArray = rootJsonObject.getJSONArray("location_coords");
        List<String> parentLocations   = new ArrayList<>();

        for (int i = 0; i < parentLocationsArray.length(); ++i)
        {
            JSONObject parentLocationJsonObject = parentLocationsArray.getJSONObject(i);
            parentLocations.add(parentLocationJsonObject.getString("location_name"));
        }

        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(mainPanel);
        JDialog jDialog = new JDialog(frame , "Set Level Owner Location", Dialog.ModalityType.APPLICATION_MODAL);

        JComboBox<String> parentLocationsComboBox = new JComboBox<String>(parentLocations.toArray(new String[0]));
        parentLocationsComboBox.setSelectedIndex(0);

        ParentLocationWrapper parentLocationWrapper = new ParentLocationWrapper();

        JPanel parentLocationPanel = new JPanel();
        parentLocationPanel.add(parentLocationsComboBox);

        parentLocationsComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg)
            {
                parentLocationWrapper.parentLocation = parentLocations.get(parentLocationsComboBox.getSelectedIndex());
            }
        });

        JButton setColorButton = new JButton("Select");
        setColorButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addIndoorLocationToOwnerLocation(levelName, parentLocationWrapper.parentLocation);
                jDialog.dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new DisposeDialogHandler(jDialog));

        JPanel actionButtonsPanel = new JPanel();
        actionButtonsPanel.add(setColorButton);
        actionButtonsPanel.add(cancelButton);
        actionButtonsPanel.setBorder(new EmptyBorder(15, 0, 10, 0));

        JPanel setParentLocationPanel = new JPanel(new BorderLayout());
        setParentLocationPanel.add(parentLocationPanel, BorderLayout.NORTH);
        setParentLocationPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        jDialog.setContentPane(setParentLocationPanel);
        jDialog.getRootPane().setDefaultButton(setColorButton);
        jDialog.pack();
        jDialog.setResizable(false);
        jDialog.setLocationRelativeTo(frame);
        jDialog.setVisible(true);
        jDialog.getContentPane().setLayout(null);
    }

    private void addIndoorLocationToOwnerLocation(final String indoorLocationName, final String ownerLocationName)
    {
        String fileContents = null;
        try
        {
            fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + TOWN_MAP_LOCATIONS_FILE_NAME).toPath()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject rootJsonObject = new JSONObject(fileContents);

        JSONArray indoorLocationsToOwnerLocationsArray = rootJsonObject.getJSONArray("indoor_locations_to_owner_locations");
        JSONObject newEntry = new JSONObject();
        newEntry.put("indoor_location_name", indoorLocationName);
        newEntry.put("owner_location_name", ownerLocationName);
        indoorLocationsToOwnerLocationsArray.put(newEntry);


        JSONArray locationCoordsArray = rootJsonObject.getJSONArray("location_coords");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(mainPanel.getGameDataDirectoryPath() + TOWN_MAP_LOCATIONS_FILE_NAME))))
        {
            bw.write("{\n");
            bw.write("\"indoor_locations_to_owner_locations\": "); bw.write(indoorLocationsToOwnerLocationsArray.toString());
            bw.write(",\n");
            bw.write("\"location_coords\": "); bw.write(locationCoordsArray.toString());
            bw.write("\n}");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
