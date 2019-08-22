package com.retroleveleditor.action_listeners;

import com.retroleveleditor.main.MainFrame;
import com.retroleveleditor.panels.LevelEditorTilemapPanel;
import com.retroleveleditor.panels.MainPanel;
import com.retroleveleditor.panels.ResourceTilemapPanel;
import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.Colors;
import com.retroleveleditor.util.NpcAttributes;
import com.retroleveleditor.util.PokemonInfo;
import com.retroleveleditor.util.TileImage;
import org.json.JSONArray;
import org.json.JSONObject;
import sun.rmi.server.LoaderHandler;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class OpenActionListener implements ActionListener
{
    public static String resourceDirectoryChooserOriginPath = ".";
    private final MainFrame mainFrame;

    public OpenActionListener(final MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser fc = new JFileChooser(resourceDirectoryChooserOriginPath);
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("JSON (*.json)", "json");
        fc.setFileFilter(fileFilter);

        int choice = fc.showOpenDialog(mainFrame);
        if (choice == JFileChooser.APPROVE_OPTION)
        {
            loadLevelFromFile(fc.getSelectedFile());
        }
    }

    private void loadLevelFromFile(final File file)
    {
        try
        {
            String fileContents = new String(Files.readAllBytes(file.toPath()));
            JSONObject rootJsonObject = new JSONObject(fileContents);


            JSONObject levelHeader = rootJsonObject.getJSONObject("level_header");
            JSONObject levelDimensions = levelHeader.getJSONObject("dimensions");

            mainFrame.resetContentPane(levelDimensions.getInt("cols"), levelDimensions.getInt("rows"), 48);

            MainPanel mainPanel = mainFrame.getMainPanel();

            for (Colors color: Colors.values())
            {
                if (color.getName().equals(levelHeader.getString("color")))
                {
                    ((LevelEditorTilemapPanel)mainPanel.getLevelEditorTilemap()).setLevelColor(color);
                    break;
                }
            }

            JSONArray groundLayerArray = rootJsonObject.getJSONArray("level_ground_layer_editor");
            for (int i = 0; i < groundLayerArray.length(); ++i)
            {
                JSONObject groundTileJsonObject = groundLayerArray.getJSONObject(i);
                TilePanel tile = mainPanel.getLevelEditorTilemap().getTileAtCoords(groundTileJsonObject.getInt("editor_col"), groundTileJsonObject.getInt("editor_row"));

                final int atlasCol = groundTileJsonObject.getInt("atlas_col");
                final int atlasRow = groundTileJsonObject.getInt("atlas_row");

                tile.setDefaultTileImage(new TileImage(ResourceTilemapPanel.ENVIRONMENT_ATLAS_IMAGE.getSubimage(atlasCol * 16, atlasRow * 16, 16, 16), "", atlasCol, atlasRow));
            }

            JSONArray npcArray = rootJsonObject.getJSONArray("level_npc_list");
            for (int i = 0; i < npcArray.length(); ++i)
            {
                JSONObject npcJsonObject = npcArray.getJSONObject(i);

                TilePanel tile = mainPanel.getLevelEditorTilemap().getTileAtCoords(npcJsonObject.getInt("editor_col"), npcJsonObject.getInt("editor_row"));

                final int atlasCol = npcJsonObject.getInt("atlas_col");
                final int atlasRow = npcJsonObject.getInt("atlas_row");

                tile.setCharTileImage(new TileImage(ResourceTilemapPanel.CHARACTER_ATLAS_IMAGE.getSubimage(atlasCol * 16, atlasRow * 16, 16, 16), "", atlasCol, atlasRow));
            }

            JSONArray modelsArray = rootJsonObject.getJSONArray("level_model_list");
            for (int i = 0; i < modelsArray.length(); ++i)
            {
                JSONObject modelJsonObject = modelsArray.getJSONObject(i);
                TilePanel tile = mainPanel.getLevelEditorTilemap().getTileAtCoords(modelJsonObject.getInt("editor_col"), modelJsonObject.getInt("editor_row"));

                final String modelName = modelJsonObject.getString("model_name");
                tile.setDefaultTileImage(mainPanel.getModelsPanel().getModelTileImage(modelName));
            }


            JSONArray tileTraitsArray = rootJsonObject.getJSONArray("level_tile_traits");
            for (int i = 0; i < tileTraitsArray.length(); ++i)
            {
                JSONObject tileTraitJsonObject = tileTraitsArray.getJSONObject(i);
                TilePanel tile = mainPanel.getLevelEditorTilemap().getTileAtCoords(tileTraitJsonObject.getInt("editor_col"), tileTraitJsonObject.getInt("editor_row"));
                tile.setTileTraits(TilePanel.TileTraits.valueOf(tileTraitJsonObject.getString("tile_traits")));
            }

            JSONArray npcAttributesArray = rootJsonObject.getJSONArray("level_npc_attributes");
            for (int i = 0; i < npcAttributesArray.length(); ++i)
            {
                JSONObject npcAttributesJsonObject = npcAttributesArray.getJSONObject(i);

                TilePanel tile = mainPanel.getLevelEditorTilemap().getTileAtCoords(npcAttributesJsonObject.getInt("editor_col"), npcAttributesJsonObject.getInt("editor_row"));

                NpcAttributes.MovemenType movemenType = NpcAttributes.MovemenType.valueOf(npcAttributesJsonObject.getString("movement_type"));
                int direction = npcAttributesJsonObject.getInt("direction");
                boolean isTrainer = npcAttributesJsonObject.getBoolean("is_trainer");
                boolean isGymLeader = npcAttributesJsonObject.getBoolean("is_gym_leader");
                String mainDialog = npcAttributesJsonObject.getString("dialog");

                List<String> sideDialogs = new ArrayList<>();
                JSONArray sideDialogArray = npcAttributesJsonObject.getJSONArray("side_dialogs");
                for (int j = 0; j < sideDialogArray.length(); ++j)
                {
                    sideDialogs.add(sideDialogArray.getString(j));
                }

                List<PokemonInfo> pokemonRoster = new ArrayList<PokemonInfo>();
                JSONArray pokemonRosterArray = npcAttributesJsonObject.getJSONArray("pokemon_roster");
                for (int j = 0; j < pokemonRosterArray.length(); ++j)
                {
                    JSONObject pokemonObject = pokemonRosterArray.getJSONObject(j);
                    pokemonRoster.add(new PokemonInfo(pokemonObject.getString("name"), pokemonObject.getInt("level")));
                }

                tile.setNpcAttributes(new NpcAttributes(mainDialog, sideDialogs, pokemonRoster, movemenType, direction, isTrainer, isGymLeader));
            }

            mainFrame.getRootPane().revalidate();
            mainFrame.getRootPane().repaint();

            mainPanel.setCurrentWorkingFile(file);
            OpenActionListener.resourceDirectoryChooserOriginPath = file.getAbsolutePath();
        }
        catch (org.json.JSONException e)
        {
            JOptionPane.showMessageDialog(mainFrame, "JSON parsing error: " + e.getLocalizedMessage(), "Malformed level json file", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}