package com.retroleveleditor.action_listeners;

import com.retroleveleditor.panels.*;
import com.retroleveleditor.util.DisposeDialogHandler;
import com.retroleveleditor.util.NpcAttributes;
import com.retroleveleditor.util.Pair;
import com.retroleveleditor.util.PokemonInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class SaveActionListener implements ActionListener
{
    class ParentLocationWrapper
    {
        String parentLocation;
    }

    class WarpTargetLevelWrapper
    {
        String levelName;
        Process process;
    }

    class UnregisteredTileWithFlowData
    {
        final TilePanel tile;
        final int levelIndex;

        public UnregisteredTileWithFlowData(final TilePanel tile, final int levelIndex)
        {
            this.tile = tile;
            this.levelIndex = levelIndex;
        }
    }


    public static String resourceDirectoryChooserOriginPath = ".";
    private static final String LEVEL_FILE_EXTENSION = ".json";
    private static final String TOWN_MAP_LOCATIONS_FILE_NAME = "town_map_locations.json";
    private static final String UNDERGROUND_MODELS_FILE_NAME = "underground_model_names.json";
    private static final String WARP_CONNECTIONS_FILE_NAME = "warp_connections.json";
    private static final String OVERWORLD_FLOW_STATE_MAP_FILE_NAME = "overworld_flow_state_map.json";
    private static final String EXPOSED_FLOW_STATES_FILE_NAME = "exposed_named_flow_states.json";
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
    private final WarpTargetLevelWrapper selectedLevelData;

    public SaveActionListener(final MainPanel mainPanel, final boolean shouldAlwaysSaveToDifferentLocation)
    {
        this.mainPanel = mainPanel;
        this.shouldAlwaysSaveToDifferentLocation = shouldAlwaysSaveToDifferentLocation;
        this.undergroundModelNames = extractUndergroundModelNames();
        this.selectedLevelData = new WarpTargetLevelWrapper();
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
        String levelName = file.getName().split("\\.")[0];

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

        if (doesLevelHaveWarpTiles() && isThereAtLeastOneWarpNotRegistered(levelName))
        {
            int selOption = JOptionPane.showConfirmDialog (null, "Link unregistered warp connections?", "Link Warp Connections", JOptionPane.YES_NO_OPTION);
            if (selOption == JOptionPane.YES_OPTION)
            {
                startManualWarpLinking(levelName);
            }
        }

        if (isThereAtLeastOneTileWithFlowNotRgistered(levelName))
        {
            int selOption = JOptionPane.showConfirmDialog (null, "Specify flow states for unregistered tiles?", "Specify Flow States", JOptionPane.YES_NO_OPTION);
            if (selOption == JOptionPane.YES_OPTION)
            {
                startFlowStateSpecification(levelName);
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

        Collections.sort(parentLocations);

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
            /*
            bw.write("{\n");
            bw.write("\"indoor_locations_to_owner_locations\": "); bw.write(indoorLocationsToOwnerLocationsArray.toString());
            bw.write(",\n");
            bw.write("\"location_coords\": "); bw.write(locationCoordsArray.toString());
            bw.write("\n}");
            */

            bw.write("{\n");
            bw.write("\t\"indoor_locations_to_owner_locations\":\n");
            bw.write("\t[\n");

            StringBuilder indoorLocationsToOwnersSringBuilder = new StringBuilder();
            for (int i = 0; i < indoorLocationsToOwnerLocationsArray.length(); ++i)
            {
                JSONObject entry = indoorLocationsToOwnerLocationsArray.getJSONObject(i);

                indoorLocationsToOwnersSringBuilder.append("\t\t{\"indoor_location_name\": \"" + entry.getString("indoor_location_name") + "\", \"owner_location_name\": \"" + entry.getString("owner_location_name") + "\"},\n");
            }

            if (indoorLocationsToOwnersSringBuilder.charAt(indoorLocationsToOwnersSringBuilder.length() - 2) == ',')
            {
                indoorLocationsToOwnersSringBuilder.deleteCharAt(indoorLocationsToOwnersSringBuilder.length() - 2);
            }
            bw.write(indoorLocationsToOwnersSringBuilder.toString());

            bw.write("\t],\n");
            bw.write("\n");
            bw.write("\t\"location_coords\":\n");
            bw.write("\t[\n");

            StringBuilder locationCoordsStringBuilder = new StringBuilder();
            for (int i = 0; i < locationCoordsArray.length(); ++i)
            {
                JSONObject entry = locationCoordsArray.getJSONObject(i);

                locationCoordsStringBuilder.append("\t\t{\"location_name\": \"" + entry.getString("location_name") + "\", \"map_position_x\": " + entry.getInt("map_position_x") + ", \"map_position_y\": " + entry.getInt("map_position_y") + "},\n");
            }

            if (locationCoordsStringBuilder.charAt(locationCoordsStringBuilder.length() - 2) == ',')
            {
                locationCoordsStringBuilder.deleteCharAt(locationCoordsStringBuilder.length() - 2);
            }
            bw.write(locationCoordsStringBuilder.toString());

            bw.write("\t]\n");
            bw.write("}");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean doesLevelHaveWarpTiles()
    {
        Component[] components = mainPanel.getLevelEditorTilemap().getComponents();

        for (Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel)component;
                if (tile.getTileTraits() == TilePanel.TileTraits.PRESS_WARP ||
                    tile.getTileTraits() == TilePanel.TileTraits.NO_ANIM_WARP ||
                    tile.getTileTraits() == TilePanel.TileTraits.WARP)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isThereAtLeastOneWarpNotRegistered(final String currentLevelName)
    {
        return getUnregisteredWarpTiles(currentLevelName).size() > 0;
    }

    private List<TilePanel> getUnregisteredWarpTiles(final String currentLevelName)
    {
        List<TilePanel> unregisteredWarpTiles = new ArrayList<>();

        String fileContents = null;
        try
        {
            fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + WARP_CONNECTIONS_FILE_NAME).toPath()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject rootJsonObject  = new JSONObject(fileContents);
        JSONArray connectionsArray = rootJsonObject.getJSONArray("connections");

        Component[] components = mainPanel.getLevelEditorTilemap().getComponents();

        for (Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel)component;
                if (tile.getTileTraits() == TilePanel.TileTraits.PRESS_WARP ||
                        tile.getTileTraits() == TilePanel.TileTraits.NO_ANIM_WARP ||
                        tile.getTileTraits() == TilePanel.TileTraits.WARP)
                {

                    if (isWarpTileInFromEntryInConnectionArray(tile, currentLevelName, connectionsArray) == false)
                    {
                        unregisteredWarpTiles.add(tile);
                    }
                }
            }
        }

        return unregisteredWarpTiles;
    }

    private boolean isWarpTileInFromEntryInConnectionArray(final TilePanel tile, final String currentLevelName, final JSONArray connectionsArray)
    {
        for (int i = 0; i < connectionsArray.length(); ++i)
        {
            JSONObject connectionObject = connectionsArray.getJSONObject(i);
            JSONObject fromObject = connectionObject.getJSONObject("from");

            if (fromObject.getInt("level_col") == tile.getGameOverworldCol() &&
                fromObject.getInt("level_row") == tile.getGameOverworldRow(mainPanel.getLevelEditorTilemap().getTileRows()) &&
                fromObject.getString("level_name").equals(currentLevelName))
            {
                return true;
            }
        }

        return false;
    }

    private void startManualWarpLinking(final String currentLevelName)
    {
        List<TilePanel> unregisteredTiles = getUnregisteredWarpTiles(currentLevelName);

        for (TilePanel tilePanel: unregisteredTiles)
        {
            selectWarpLinkTarget(tilePanel, currentLevelName);
        }
    }

    private void selectWarpLinkTarget(final TilePanel tilePanel, final String currentLevelName)
    {
        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(mainPanel);
        JDialog jDialog = new JDialog(frame , "Set Warp Target for: " + tilePanel.getGameOverworldCol() + "," + tilePanel.getGameOverworldRow(mainPanel.getLevelEditorTilemap().getTileRows()), Dialog.ModalityType.APPLICATION_MODAL);
        jDialog.getRootPane().registerKeyboardAction(new DisposeDialogHandler(jDialog), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel referenceAreaPanel = new TileReferenceImagePanel(mainPanel, tilePanel);

        NumberFormatter dimensionsFormatter = new NumberFormatter(NumberFormat.getInstance());
        dimensionsFormatter.setValueClass(Integer.class);
        dimensionsFormatter.setMinimum(1);
        dimensionsFormatter.setCommitsOnValidEdit(false);

        LabelledInputPanel levelColsPanel = new LabelledInputPanel("col: ", dimensionsFormatter, 3, 0);
        LabelledInputPanel levelRowsPanel = new LabelledInputPanel("row: ", dimensionsFormatter, 3, 0);

        JPanel targetCoordsPanel = new JPanel();
        targetCoordsPanel.setLayout(new BoxLayout(targetCoordsPanel, BoxLayout.X_AXIS));
        targetCoordsPanel.add(levelColsPanel);
        targetCoordsPanel.add(levelRowsPanel);

        List<String> availableLevels = new ArrayList<>();
        File levelDirectory = new File(mainPanel.getGameLevelsDirectoryPath());
        for (File f: levelDirectory.listFiles())
        {
            if (f.getName().startsWith(".") == false)
            {
                availableLevels.add(f.getName());
            }
        }

        Collections.sort(availableLevels);

        JComboBox<String> availableLevelsComboBox = new JComboBox<String>(availableLevels.toArray(new String[0]));
        availableLevelsComboBox.setSelectedIndex(0);

        selectedLevelData.levelName = "";
        selectedLevelData.process   = null;

        availableLevelsComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg)
            {
                String previousLevelName = selectedLevelData.levelName;
                selectedLevelData.levelName = availableLevels.get(availableLevelsComboBox.getSelectedIndex());

                if (previousLevelName.equals(selectedLevelData.levelName) == false)
                {
                    try
                    {
                        if (selectedLevelData.process != null)
                        {
                            selectedLevelData.process.destroy();
                        }

                        selectedLevelData.process = Runtime.getRuntime().exec("java -jar " + mainPanel.getResourceRootDirectory() + "/../RetroLevelEditor.jar " + selectedLevelData.levelName.split("\\.")[0]);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        JLabel selectLevelLabel = new JLabel("Select Level");
        selectLevelLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel levelSelectionPanel = new JPanel(new BorderLayout());
        levelSelectionPanel.add(selectLevelLabel, BorderLayout.NORTH);
        levelSelectionPanel.add(availableLevelsComboBox, BorderLayout.SOUTH);


        JPanel targetSpecPanel = new JPanel(new BorderLayout());
        targetSpecPanel.add(levelSelectionPanel, BorderLayout.NORTH);
        targetSpecPanel.add(targetCoordsPanel, BorderLayout.SOUTH);
        targetSpecPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel topAreaPanel = new JPanel(new BorderLayout());
        topAreaPanel.add(referenceAreaPanel, BorderLayout.NORTH);
        topAreaPanel.add(targetSpecPanel, BorderLayout.SOUTH);

        JButton linkButton = new JButton("Link");
        linkButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String fileContents = null;
                try
                {
                    fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + WARP_CONNECTIONS_FILE_NAME).toPath()));
                }
                catch (IOException ee)
                {
                    ee.printStackTrace();
                }

                JSONObject rootJsonObject = new JSONObject(fileContents);

                JSONObject newFromEntry = new JSONObject();
                newFromEntry.put("level_name", currentLevelName);
                newFromEntry.put("level_col", tilePanel.getGameOverworldCol());
                newFromEntry.put("level_row", tilePanel.getGameOverworldRow(mainPanel.getLevelEditorTilemap().getTileRows()));

                JSONObject newToEntry = new JSONObject();
                newToEntry.put("level_name", selectedLevelData.levelName.split("\\.")[0]);
                newToEntry.put("level_col", Integer.parseInt(levelColsPanel.getTextField().getText()));
                newToEntry.put("level_row", Integer.parseInt(levelRowsPanel.getTextField().getText()));

                JSONObject newConnectionEntry = new JSONObject();
                newConnectionEntry.put("from", newFromEntry);
                newConnectionEntry.put("to", newToEntry);

                JSONArray connectionsArray = rootJsonObject.getJSONArray("connections");
                connectionsArray.put(newConnectionEntry);

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(mainPanel.getGameDataDirectoryPath() + WARP_CONNECTIONS_FILE_NAME))))
                {
                    bw.write("{\n");
                    bw.write("\t\"connections\":\n");
                    bw.write("\t[\n");

                    StringBuilder connectionsStringBuilder = new StringBuilder();
                    for (int i = 0; i < connectionsArray.length(); ++i)
                    {
                        JSONObject entry = connectionsArray.getJSONObject(i);
                        JSONObject fromObject = entry.getJSONObject("from");
                        JSONObject toObject   = entry.getJSONObject("to");
                        connectionsStringBuilder.append("\t\t{ \"from\": { \"level_name\": \"" + fromObject.getString("level_name") + "\", \"level_col\": " + fromObject.getInt("level_col") + ", \"level_row\": " + fromObject.getInt("level_row") + " }");
                        connectionsStringBuilder.append(", \"to\": { \"level_name\": \"" + toObject.getString("level_name") + "\", \"level_col\": " + toObject.getInt("level_col") + ", \"level_row\": " + toObject.getInt("level_row") + " } },\n");
                    }

                    if (connectionsStringBuilder.charAt(connectionsStringBuilder.length() - 2) == ',')
                    {
                        connectionsStringBuilder.deleteCharAt(connectionsStringBuilder.length() - 2);
                    }
                    bw.write(connectionsStringBuilder.toString());

                    bw.write("\t]\n");
                    bw.write("}");
                }
                catch (IOException ee)
                {
                    ee.printStackTrace();
                }

                jDialog.dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new DisposeDialogHandler(jDialog));

        JPanel actionButtonsPanel = new JPanel();
        actionButtonsPanel.add(linkButton);
        actionButtonsPanel.add(cancelButton);
        actionButtonsPanel.setBorder(new EmptyBorder(15, 0, 10, 0));

        JPanel setParentLocationPanel = new JPanel(new BorderLayout());
        setParentLocationPanel.add(topAreaPanel, BorderLayout.NORTH);
        setParentLocationPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        jDialog.setContentPane(setParentLocationPanel);
        jDialog.getRootPane().setDefaultButton(linkButton);
        jDialog.pack();
        jDialog.setResizable(false);
        jDialog.setLocationRelativeTo(frame);
        jDialog.setVisible(true);
        jDialog.getContentPane().setLayout(null);

    }

    boolean isThereAtLeastOneTileWithFlowNotRgistered(final String currentLevelName)
    {
        return getUnregisteredTilesWithFlows(currentLevelName).size() > 0;
    }

    private List<UnregisteredTileWithFlowData> getUnregisteredTilesWithFlows(final String currentLevelName)
    {
        List<UnregisteredTileWithFlowData> unregisteredTilesWithFlow = new ArrayList<>();

        String fileContents = null;
        try
        {
            fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + OVERWORLD_FLOW_STATE_MAP_FILE_NAME).toPath()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject rootJsonObject  = new JSONObject(fileContents);
        JSONArray npcFlowStatesArray = rootJsonObject.getJSONArray("npc_flow_states");
        JSONArray triggerFlowStatesArray = rootJsonObject.getJSONArray("trigger_flow_states");

        Component[] components = mainPanel.getLevelEditorTilemap().getComponents();
        int levelIndexCounter = 0;
        for (Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;
                if (tile.getNpcAttributes() != null)
                {
                    if (doesNpcHaveAnyFlowTriggeringInDialogs(tile.getNpcAttributes()) && isNpcTileRegistered(tile, levelIndexCounter, currentLevelName, npcFlowStatesArray) == false)
                    {
                        unregisteredTilesWithFlow.add(new UnregisteredTileWithFlowData(tile, levelIndexCounter));
                    }

                    levelIndexCounter++;
                }
                else if (tile.getTileTraits() == TilePanel.TileTraits.FLOW_TRIGGER)
                {
                    if (isTriggerTileRegistered(tile, currentLevelName, triggerFlowStatesArray) == false)
                    {
                        unregisteredTilesWithFlow.add(new UnregisteredTileWithFlowData(tile, -1));
                    }
                }
            }
        }

        return unregisteredTilesWithFlow;
    }

    boolean doesNpcHaveAnyFlowTriggeringInDialogs(final NpcAttributes npcAttributes)
    {
        for (final String sideDialog: npcAttributes.sideDialogs)
        {
            if (sideDialog.indexOf("+FLOW") != -1)
            {
                return true;
            }
        }

        if (npcAttributes.mainDialog.indexOf("+FLOW") != -1)
        {
            return true;
        }

        return false;
    }

    boolean isNpcTileRegistered(final TilePanel tile, final int levelIndex, final String levelName, JSONArray npcFlowStatesArray)
    {
        for (int i = 0; i < npcFlowStatesArray.length(); ++i)
        {
            JSONObject npcFlowStateEntryObject = npcFlowStatesArray.getJSONObject(i);

            if (npcFlowStateEntryObject.getString("level_name").equals(levelName) &&
                npcFlowStateEntryObject.getInt("npc_level_index") == levelIndex)
            {
                return true;
            }
        }

        return false;
    }

    boolean isTriggerTileRegistered(final TilePanel tile, final String levelName, JSONArray triggerFlowStatesArray)
    {
        for (int i = 0; i < triggerFlowStatesArray.length(); ++i)
        {
            JSONObject triggerFlowStateEntryObject = triggerFlowStatesArray.getJSONObject(i);
            if (triggerFlowStateEntryObject.getString("level_name").equals(levelName) &&
                triggerFlowStateEntryObject.getInt("level_col") == tile.getGameOverworldCol() &&
                triggerFlowStateEntryObject.getInt("level_row") == tile.getGameOverworldRow(mainPanel.getLevelEditorTilemap().getTileRows()))
            {
                return true;
            }
        }

        return false;
    }

    private void startFlowStateSpecification(final String currentLevelName)
    {
        List<UnregisteredTileWithFlowData> unregisteredTiles = getUnregisteredTilesWithFlows(currentLevelName);

        for (UnregisteredTileWithFlowData unregisteredTileWithFlowData: unregisteredTiles)
        {
            selectTargetFlowState(unregisteredTileWithFlowData.tile, unregisteredTileWithFlowData.levelIndex,currentLevelName);
        }
    }

    private void selectTargetFlowState(final TilePanel tilePanel, final int levelIndex, final String currentLevelName)
    {
        JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(mainPanel);
        JDialog jDialog = new JDialog(frame , "Select Flow State for: " + tilePanel.getGameOverworldCol() + "," + tilePanel.getGameOverworldRow(mainPanel.getLevelEditorTilemap().getTileRows()), Dialog.ModalityType.APPLICATION_MODAL);
        jDialog.getRootPane().registerKeyboardAction(new DisposeDialogHandler(jDialog), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel referenceAreaPanel = new TileReferenceImagePanel(mainPanel, tilePanel);

        List<String> availableFlowStates = getAvailableFlowStates();
        Collections.sort(availableFlowStates);

        JComboBox<String> availableFlowStatesComboBox = new JComboBox<String>(availableFlowStates.toArray(new String[0]));
        availableFlowStatesComboBox.setSelectedIndex(0);

        JPanel flowStatesComboPanel = new JPanel();
        flowStatesComboPanel.add(availableFlowStatesComboBox);

        JPanel topAreaPanel = new JPanel(new BorderLayout());
        topAreaPanel.add(referenceAreaPanel, BorderLayout.NORTH);
        topAreaPanel.add(flowStatesComboPanel, BorderLayout.SOUTH);

        JButton setFlowStateButton = new JButton("Set Flow State");

        setFlowStateButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String fileContents = null;
                try
                {
                    fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + OVERWORLD_FLOW_STATE_MAP_FILE_NAME).toPath()));
                }
                catch (IOException ee)
                {
                    ee.printStackTrace();
                }

                JSONObject rootJsonObject = new JSONObject(fileContents);
                JSONArray npcFlowStatesArray = rootJsonObject.getJSONArray("npc_flow_states");
                JSONArray triggerFlowStatesArray = rootJsonObject.getJSONArray("trigger_flow_states");

                boolean isTriggerTile = tilePanel.getTileTraits() == TilePanel.TileTraits.FLOW_TRIGGER;


                JSONObject newEntry = new JSONObject();
                newEntry.put("level_name", currentLevelName);
                newEntry.put("flow_state_name", availableFlowStates.get(availableFlowStatesComboBox.getSelectedIndex()));

                if (isTriggerTile)
                {
                    newEntry.put("level_col", tilePanel.getGameOverworldCol());
                    newEntry.put("level_row", tilePanel.getGameOverworldRow(mainPanel.getLevelEditorTilemap().getTileRows()));

                    triggerFlowStatesArray.put(newEntry);
                }
                else
                {
                    newEntry.put("npc_level_index", levelIndex);

                    npcFlowStatesArray.put(newEntry);
                }

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(mainPanel.getGameDataDirectoryPath() + OVERWORLD_FLOW_STATE_MAP_FILE_NAME))))
                {
                    bw.write("{\n");
                    bw.write("\t\"npc_flow_states\":\n");
                    bw.write("\t[\n");


                    StringBuilder npcFlowStateBuilder = new StringBuilder();
                    for (int i = 0; i < npcFlowStatesArray.length(); ++i)
                    {
                        JSONObject entry = npcFlowStatesArray.getJSONObject(i);

                        npcFlowStateBuilder.append("\t\t{\"level_name\": \"" + entry.getString("level_name") + "\", \"npc_level_index\": " + entry.getInt("npc_level_index") + ", \"flow_state_name\": \"" + entry.getString("flow_state_name") + "\"},\n");

                    }

                    if (npcFlowStateBuilder.charAt(npcFlowStateBuilder.length() - 2) == ',')
                    {
                        npcFlowStateBuilder.deleteCharAt(npcFlowStateBuilder.length() - 2);
                    }
                    bw.write(npcFlowStateBuilder.toString());

                    bw.write("\t],\n");
                    bw.write("\n");
                    bw.write("\t\"trigger_flow_states\":\n");
                    bw.write("\t[\n");

                    StringBuilder triggerFlowStateBuilder = new StringBuilder();
                    for (int i = 0; i < triggerFlowStatesArray.length(); ++i)
                    {
                        JSONObject entry = triggerFlowStatesArray.getJSONObject(i);

                        triggerFlowStateBuilder.append("\t\t{\"level_name\": \"" + entry.getString("level_name") + "\", \"level_col\": " + entry.getInt("level_col") + ", \"level_row\": " + entry.getInt("level_row") + ", \"flow_state_name\": \"" + entry.getString("flow_state_name") + "\"},\n");
                    }

                    if (triggerFlowStateBuilder.charAt(triggerFlowStateBuilder.length() - 2) == ',')
                    {
                        triggerFlowStateBuilder.deleteCharAt(triggerFlowStateBuilder.length() - 2);
                    }
                    bw.write(triggerFlowStateBuilder.toString());

                    bw.write("\t]\n");
                    bw.write("}");
                }
                catch (IOException ee)
                {
                    ee.printStackTrace();
                }

                jDialog.dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new DisposeDialogHandler(jDialog));

        JPanel actionButtonsPanel = new JPanel();
        actionButtonsPanel.add(setFlowStateButton);
        actionButtonsPanel.add(cancelButton);
        actionButtonsPanel.setBorder(new EmptyBorder(15, 0, 10, 0));

        JPanel setFlowStatePanel = new JPanel(new BorderLayout());
        setFlowStatePanel.add(topAreaPanel, BorderLayout.NORTH);
        setFlowStatePanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        jDialog.setContentPane(setFlowStatePanel);
        jDialog.getRootPane().setDefaultButton(setFlowStateButton);
        jDialog.pack();
        jDialog.setResizable(false);
        jDialog.setLocationRelativeTo(frame);
        jDialog.setVisible(true);
        jDialog.getContentPane().setLayout(null);
    }

    private List<String> getAvailableFlowStates()
    {
        List<String> availableFlowStates = new ArrayList<>();

        String fileContents = null;
        try
        {
            fileContents = new String(Files.readAllBytes(new File(mainPanel.getGameDataDirectoryPath() + EXPOSED_FLOW_STATES_FILE_NAME).toPath()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject rootJsonObject  = new JSONObject(fileContents);
        JSONArray availableFlowStatesJsonArray = rootJsonObject.getJSONArray("named_states");

        for (int i = 0; i < availableFlowStatesJsonArray.length(); ++i)
        {
            availableFlowStates.add(availableFlowStatesJsonArray.getString(i));
        }

        return availableFlowStates;
    }
}
