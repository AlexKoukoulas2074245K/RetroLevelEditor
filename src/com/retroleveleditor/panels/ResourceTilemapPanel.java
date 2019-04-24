package com.retroleveleditor.panels;

import com.retroleveleditor.util.CharacterAtlasEntryDescriptor;
import com.retroleveleditor.util.Pair;
import com.retroleveleditor.util.TileImage;

import javafx.scene.text.Font;
import sun.plugin2.os.windows.OVERLAPPED;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResourceTilemapPanel extends BaseTilemapPanel
{
    // Structure of all atlases. Change as needed
    public static final float GAME_OVERWORLD_TILE_SIZE = 1.6f;
    private static final int ATLAS_COLS = 8;
    private static final int ATLAS_ROWS = 64;
    private static final int ATLAS_TILE_SIZE = 16;


    // Statically load tile selection image
    private static Image SELECTION_IMAGE = null;
    private static Image MODEL_SELECTION_IMAGE = null;

    static
    {
        try
        {
            SELECTION_IMAGE = ImageIO.read(ResourceTilemapPanel.class.getResourceAsStream("/selection.png"));
            MODEL_SELECTION_IMAGE = ImageIO.read(ResourceTilemapPanel.class.getResourceAsStream("/model_selection.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private Map<String, Pair<Integer>> modelNamesToOverworldDims;
    private String atlasPath;
    private String gameDataPath;

    public ResourceTilemapPanel(final String atlasPath, final String gameDataPath, final int resourcePanelCols, final int tileSize)
    {
        super(resourcePanelCols, ((ATLAS_COLS * ATLAS_ROWS) / resourcePanelCols) + 1, tileSize, false ,false);
        this.atlasPath = atlasPath;
        this.gameDataPath = gameDataPath;

        markTilesAsResourceTiles();
        extractAtlasImages();
        getTileAtCoords(0, 0).setIsSelected(true);
        TilePanel.selectedResourceTile = getTileAtCoords(0, 0);
    }

    public ResourceTilemapPanel(final String modelsDirectory, final String gameDataPath, final Map<File, File> modelToTextureFiles, final int tileSize)
    {
        super(1, modelToTextureFiles.size(), tileSize, true, false);
        this.gameDataPath = gameDataPath;

        markTilesAsResourceTiles();
        extractModelImagesAndNames(modelToTextureFiles);
        getTileAtCoords(0, 0).setIsSelected(true);
        TilePanel.selectedResourceTile = getTileAtCoords(0, 0);
    }

    public ResourceTilemapPanel(final int tileSize)
    {
        super(1, TilePanel.TileTraits.values().length, tileSize, false, true);
        markTilesAsResourceTiles();
        getTileAtCoords(0, 0).setIsSelected(true);
        TilePanel.selectedResourceTile = getTileAtCoords(0, 0);

        getTileAtCoords(0, 1).setTileTraits(TilePanel.TileTraits.SOLID);
        getTileAtCoords(0, 2).setTileTraits(TilePanel.TileTraits.WARP);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        Component[] components = getComponents();
        for (Component component : components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;

                if (tile.isResourceTile() && tile.isSelected())
                {
                    if (this.isModelsPanel || this.isTraitsPanel)
                    {
                        g2.drawImage(MODEL_SELECTION_IMAGE, tile.getX() - 1, tile.getY() - 1, tile.getWidth(), tile.getHeight(), null);
                    }
                    else
                    {
                        g2.drawImage(SELECTION_IMAGE, tile.getX() - 1, tile.getY() - 1, tileSize, tileSize, null);
                    }
                }
            }
        }
    }

    public String getAtlasPath()
    {
        return this.atlasPath;
    }

    private void markTilesAsResourceTiles()
    {
        Component[] components = getComponents();
        for (Component component : components)
        {
            if (component instanceof TilePanel)
            {
                ((TilePanel) component).setIsResourceTile(true);
            }
        }
    }

    private void extractModelImagesAndNames(final Map<File, File> modelToTextureFiles)
    {
        CalculateModelOverworldTileDimensions(modelToTextureFiles);
        Component[] components = getComponents();
        int componentIndex = 0;

        for (Map.Entry<File, File> entry: modelToTextureFiles.entrySet())
        {
            if (components[componentIndex] instanceof TilePanel)
            {
                TilePanel tilePanel = (TilePanel)components[componentIndex];

                try
                {
                    final String modelName = entry.getKey().getName().split("\\.")[0];
                    Pair<Integer> modelOverworldDims = this.modelNamesToOverworldDims.get(modelName);
                    tilePanel.setDefaultTileImage(new TileImage(
                            ImageIO.read(entry.getValue().getAbsoluteFile()),
                            modelName + " (" + modelOverworldDims.x + "," + modelOverworldDims.y + ")",
                            modelOverworldDims.x, modelOverworldDims.y
                            ));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            componentIndex++;
        }
    }

    private void extractAtlasImages()
    {
        try
        {
            boolean isCharacterAtlas = this.atlasPath.endsWith(MainPanel.CHARACTERS_ATLAS_RELATIVE_PATH);
            List<CharacterAtlasEntryDescriptor> characterEntries = null;

            if (isCharacterAtlas)
            {
                characterEntries = extractCharacterEntriesInAtlas();
            }

            BufferedImage atlasImage = ImageIO.read(new File(this.atlasPath));
            int colIndex = 0;
            int rowIndex = 0;
            for (int y = 0; y < ATLAS_ROWS; ++y)
            {
                for (int x = 0; x < ATLAS_COLS; ++x)
                {

                    if (isCharacterAtlas)
                    {
                        if (characterEntries.size() == 0)
                        {
                            continue;
                        }

                        CharacterAtlasEntryDescriptor nextEntry = characterEntries.get(0);

                        if (x != nextEntry.atlasCol || y != nextEntry.atlasRow)
                        {
                            continue;
                        }
                        else
                        {
                            characterEntries.remove(0);
                        }
                    }

                    TilePanel tile = getTileAtCoords(colIndex, rowIndex);
                    Image tileImage = atlasImage.getSubimage
                            (
                                    x * ATLAS_TILE_SIZE,
                                    y * ATLAS_TILE_SIZE,
                                    ATLAS_TILE_SIZE,
                                    ATLAS_TILE_SIZE
                            );

                    if (isCharacterAtlas)
                    {
                        tile.setCharTileImage(new TileImage(tileImage, "", x, y));
                    }
                    else
                    {
                        tile.setDefaultTileImage(new TileImage(tileImage, "", x, y));
                    }


                    if (++colIndex >= this.tileCols)
                    {
                        colIndex = 0;
                        rowIndex++;
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private List<CharacterAtlasEntryDescriptor> extractCharacterEntriesInAtlas()
    {
        List<CharacterAtlasEntryDescriptor> characterEntries = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(new File(this.gameDataPath + "npcs_atlas_coords.dat"))))
        {
            String line = null;
            while ((line = br.readLine()) != null)
            {
                String[] lineComponents = line.split(",");
                characterEntries.add(new CharacterAtlasEntryDescriptor(Integer.parseInt(lineComponents[0]), Integer.parseInt(lineComponents[1])));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return characterEntries;
    }

    private void CalculateModelOverworldTileDimensions(final Map<File, File> modelToTextureFiles)
    {
        this.modelNamesToOverworldDims = new HashMap<>();

        for (Map.Entry<File, File> entry: modelToTextureFiles.entrySet())
        {
            try(BufferedReader br = new BufferedReader(new FileReader(entry.getKey())))
            {
                String line = "";
                float minX = +100.0f;
                float maxX = -100.0f;
                float minZ = +100.0f;
                float maxZ = -100.0f;

                while ((line = br.readLine()) != null)
                {
                    if (line.startsWith("v "))
                    {
                        String[] vertexPositionComps = line.split(" ");
                        float vx = Float.parseFloat(vertexPositionComps[1]);
                        float vz = Float.parseFloat(vertexPositionComps[3]);

                        if (vx < minX) minX = vx;
                        if (vx > maxX) maxX = vx;
                        if (vz < minZ) minZ = vz;
                        if (vz > maxZ) maxZ = vz;
                    }
                }

                float xDim = Math.round((maxX - minX)/GAME_OVERWORLD_TILE_SIZE)*GAME_OVERWORLD_TILE_SIZE;
                float zDim = Math.round((maxZ - minZ)/GAME_OVERWORLD_TILE_SIZE)*GAME_OVERWORLD_TILE_SIZE;

                int colDim = Math.max(Math.round(xDim/GAME_OVERWORLD_TILE_SIZE), 1);
                int rowDim = Math.max(Math.round(zDim/GAME_OVERWORLD_TILE_SIZE), 1);

                this.modelNamesToOverworldDims.put(entry.getKey().getName().split("\\.")[0], new Pair<Integer>(colDim, rowDim));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
