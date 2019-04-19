package com.retroleveleditor.panels;

import com.retroleveleditor.util.CharacterAtlasEntryDescriptor;
import com.retroleveleditor.util.TileImage;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ResourceTilemapPanel extends BaseTilemapPanel
{
    // Structure of all atlases. Change as needed
    private static final int ATLAS_COLS = 8;
    private static final int ATLAS_ROWS = 64;
    private static final int ATLAS_TILE_SIZE = 16;

    // Statically load tile selection image
    private static Image SELECTION_IMAGE = null;

    static
    {
        try
        {
            SELECTION_IMAGE = ImageIO.read(ResourceTilemapPanel.class.getResourceAsStream("/selection.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public ResourceTilemapPanel(final String atlasPath, final int resourcePanelCols, final int tileSize)
    {
        super(resourcePanelCols, ((ATLAS_COLS * ATLAS_ROWS) / resourcePanelCols) + 1, tileSize);
        markTilesAsResourceTiles();
        extractAtlasImages(atlasPath);
        getTileAtCoords(0, 0).setIsSelected(true);
        TilePanel.selectedResourceImage = getTileAtCoords(0, 0).getTileImage();
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
                    g2.drawImage(SELECTION_IMAGE, tile.getX() - 1, tile.getY() - 1, tileSize, tileSize, null);
                }
            }
        }
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

    private void extractAtlasImages(final String atlasPath)
    {
        try
        {
            boolean isCharacterAtlas = atlasPath.equals(MainPanel.CHARACTERS_ATLAS_PATH);
            List<CharacterAtlasEntryDescriptor> characterEntries = null;

            if (isCharacterAtlas)
            {
                characterEntries = extractCharacterEntriesInAtlas();
            }

            BufferedImage atlasImage = ImageIO.read(new File(atlasPath));
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

                    tile.setTileImage(new TileImage(tileImage, x, y));

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

        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/npcs_atlas_coords.dat"))))
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
}
