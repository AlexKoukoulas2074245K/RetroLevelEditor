package com.retroleveleditor.panels;

import com.retroleveleditor.util.Pair;
import com.retroleveleditor.util.TileImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class ResourceTilemapPanel extends BaseTilemapPanel
{
    public static BufferedImage ENVIRONMENT_ATLAS_IMAGE = null;
    public static BufferedImage CHARACTER_ATLAS_IMAGE = null;

    // Statically load tile selection image
    public static Image SELECTION_IMAGE = null;
    private static Image FILLER_SELECTION_IMAGE = null;
    private static Image MODEL_SELECTION_IMAGE = null;

    static
    {
        try
        {
            SELECTION_IMAGE = ImageIO.read(ResourceTilemapPanel.class.getResourceAsStream("/selection.png"));
            FILLER_SELECTION_IMAGE = ImageIO.read(ResourceTilemapPanel.class.getResourceAsStream("/filler_selection.png"));
            MODEL_SELECTION_IMAGE = ImageIO.read(ResourceTilemapPanel.class.getResourceAsStream("/model_selection.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Structure of all atlases. Change as needed
    public static final float GAME_OVERWORLD_TILE_SIZE = 1.6f;
    private static final int ATLAS_COLS = 8;
    private static final int ATLAS_ROWS = 64;
    private static final int ATLAS_TILE_SIZE = 16;

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
        getTileAtCoords(0, 3).setTileTraits(TilePanel.TileTraits.NO_ANIM_WARP);
        getTileAtCoords(0, 4).setTileTraits(TilePanel.TileTraits.PRESS_WARP);
        getTileAtCoords(0, 5).setTileTraits(TilePanel.TileTraits.ENCOUNTER);
        getTileAtCoords(0, 6).setTileTraits(TilePanel.TileTraits.JUMPING_LEDGE_BOT);
        getTileAtCoords(0, 7).setTileTraits(TilePanel.TileTraits.JUMPING_LEDGE_LEFT);
        getTileAtCoords(0, 8).setTileTraits(TilePanel.TileTraits.JUMPING_LEDGE_RIGHT);
        getTileAtCoords(0, 9).setTileTraits(TilePanel.TileTraits.SEA_TILE_EDGE);
        getTileAtCoords(0, 10).setTileTraits(TilePanel.TileTraits.FLOW_TRIGGER);
        getTileAtCoords(0, 11).setTileTraits(TilePanel.TileTraits.CUTTABLE_TREE);
        getTileAtCoords(0, 12).setTileTraits(TilePanel.TileTraits.PUSHABLE_ROCK);
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

                if (tile.isFillerTile())
                {
                    g2.drawImage(FILLER_SELECTION_IMAGE, tile.getX() - 1, tile.getY() - 1, tileSize, tileSize, null);
                }
                else if (tile.isSelected())
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

    public TileImage getModelTileImage(final String modelName)
    {
        Component[] components = getComponents();
        for (Component component : components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel)component;
                if (tile.getDefaultTileImage() != null && tile.getDefaultTileImage().modelName.equals(modelName))
                {
                    return tile.getDefaultTileImage();
                }
            }
        }

        return null;
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

        List<Pair<File>> sortedModelToTextureFiles = new ArrayList<>();

        for (Map.Entry<File, File> entry: modelToTextureFiles.entrySet())
        {
            sortedModelToTextureFiles.add(new Pair(entry.getKey(), entry.getValue()));
        }

        sortedModelToTextureFiles.sort(new Comparator<Pair<File>>()
        {
            @Override
            public int compare(Pair<File> o1, Pair<File> o2)
            {
                return o1.x.compareTo(o2.x);
            }
        });

        for (Pair<File> pair: sortedModelToTextureFiles)
        {
            if (components[componentIndex] instanceof TilePanel)
            {
                TilePanel tilePanel = (TilePanel)components[componentIndex];

                try
                {
                    final String modelName = pair.x.getName().split("\\.")[0];
                    Pair<Integer> modelOverworldDims = this.modelNamesToOverworldDims.get(modelName);
                    tilePanel.setDefaultTileImage(new TileImage(
                            ImageIO.read(pair.y.getAbsoluteFile()),
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

            List<Pair<Integer>> southFacingCharacterCoords = extractSouthFacingCharacterCoords();

            BufferedImage atlasImage = ImageIO.read(new File(this.atlasPath));
            if (isCharacterAtlas) CHARACTER_ATLAS_IMAGE = atlasImage;
            else ENVIRONMENT_ATLAS_IMAGE = atlasImage;

            int colIndex = 0;
            int rowIndex = 0;
            for (int y = 0; y < ATLAS_ROWS; ++y)
            {
                for (int x = 0; x < ATLAS_COLS; ++x)
                {
                    TilePanel tile = getTileAtCoords(colIndex, rowIndex);
                    Image tileImage = atlasImage.getSubimage
                            (
                                    x * ATLAS_TILE_SIZE,
                                    y * ATLAS_TILE_SIZE,
                                    ATLAS_TILE_SIZE,
                                    ATLAS_TILE_SIZE
                            );

                    boolean skippedEntry = false;

                    if (isCharacterAtlas)
                    {
                        if (southFacingCharacterCoords.contains(new Pair<>(x, y)))
                        {
                            tile.setCharTileImage(new TileImage(tileImage, "", x, y));
                        }
                        else
                        {
                            skippedEntry = true;
                        }
                    }
                    else
                    {
                        tile.setDefaultTileImage(new TileImage(tileImage, "", x, y));
                    }

                    if (skippedEntry)
                    {
                        continue;
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

    private List<Pair<Integer>> extractSouthFacingCharacterCoords()
    {
        List<Pair<Integer>> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/npcs_atlas_coords.dat"))))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                String[] lineComponents = line.split(",");
                result.add(new Pair<>(Integer.parseInt(lineComponents[0]), Integer.parseInt(lineComponents[1])));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;

    }
}
