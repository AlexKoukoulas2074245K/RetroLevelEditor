package com.retroleveleditor.panels;

import com.retroleveleditor.util.SelectResourceDirectoryHandler;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainPanel extends JPanel
{
    public static final String ENVIRONMENTS_ATLAS_RELATIVE_PATH = "/atlases/environments.png";
    public static final String CHARACTERS_ATLAS_RELATIVE_PATH = "/atlases/characters.png";
    public static final String TEXTURES_RELATIVE_DIRECTORY = "/textures/";
    public static final String MODELS_RELATIVE_DIRECTORY = "/models/";
    public static final String GAME_DATA_RELATIVE_DIRECTORY = "/gamedata/";

    private static final int SCROLL_UNIT = 8;
    private static final int H_COMPONENT_GAP = 10;
    private static final int V_COMPONENT_GAP = 10;
    private static final String CONFIG_FILE_PATH = "config.rle";

    private static final int SIDE_BAR_PANELS_DEFAULT_WIDTH = 211;
    private static final int RESOURCES_PANEL_DEFAULT_HEIGHT = 640;
    private static final int LEVEL_EDITOR_DEFAULT_WIDTH = 768;
    private static final int LEVEL_EDITOR_DEFAULT_HEIGHT = 700;

    private LevelEditorTilemapPanel levelEditorPanel;
    private ResourceTilemapPanel modelsPanel;

    private String resourceRootDirectory;
    private File currentWorkingFile;

    public static boolean isValidResourceRootPath(final String rootResourcePath)
    {
        return new File(rootResourcePath + MainPanel.ENVIRONMENTS_ATLAS_RELATIVE_PATH).exists() &&
               new File(rootResourcePath + MainPanel.CHARACTERS_ATLAS_RELATIVE_PATH).exists() &&
               new File(rootResourcePath + MainPanel.TEXTURES_RELATIVE_DIRECTORY).exists() &&
               new File(rootResourcePath + MainPanel.GAME_DATA_RELATIVE_DIRECTORY).exists();
    }

    public MainPanel(final int levelEditorTileCols, final int levelEditorTileRows, final int tileSize)
    {
        super(new BorderLayout(H_COMPONENT_GAP, V_COMPONENT_GAP));

        checkForEditorConfig();

        JTabbedPane resourcesTabbedPane = new JTabbedPane();

        JScrollPane tileTraitsScrollPane = new JScrollPane(new ResourceTilemapPanel
        (
                tileSize
        ), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tileTraitsScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        tileTraitsScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

        this.modelsPanel = new ResourceTilemapPanel
        (
                resourceRootDirectory + MODELS_RELATIVE_DIRECTORY,
                resourceRootDirectory + GAME_DATA_RELATIVE_DIRECTORY,
                extractModelsToTextureFiles(),
                tileSize
        );
        JScrollPane modelsScrollPane = new JScrollPane(modelsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        modelsScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        modelsScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

        JScrollPane charactersScrollPane = new JScrollPane(new ResourceTilemapPanel
        (
                resourceRootDirectory + CHARACTERS_ATLAS_RELATIVE_PATH,
                resourceRootDirectory + GAME_DATA_RELATIVE_DIRECTORY,
                4,
                tileSize
        ), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        charactersScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        charactersScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

        JScrollPane environmentsScrollPane = new JScrollPane(new ResourceTilemapPanel
        (
                resourceRootDirectory + ENVIRONMENTS_ATLAS_RELATIVE_PATH,
                resourceRootDirectory + GAME_DATA_RELATIVE_DIRECTORY,
                4,
                tileSize
        ), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        environmentsScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        environmentsScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

        resourcesTabbedPane.addTab("Environments", environmentsScrollPane);
        resourcesTabbedPane.addTab("Characters", charactersScrollPane);
        resourcesTabbedPane.addTab("Models", modelsScrollPane);
        resourcesTabbedPane.addTab("TileTraits", tileTraitsScrollPane);

        // Handle selected tiles when resource tabs are changed
        resourcesTabbedPane.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                TilePanel currentlySelectedTile = TilePanel.selectedResourceTile;
                JViewport scrollPaneViewport = ((JScrollPane)resourcesTabbedPane.getSelectedComponent()).getViewport();
                ResourceTilemapPanel resourceTilemapPanel = (ResourceTilemapPanel)scrollPaneViewport.getView();
                resourceTilemapPanel.deselectAllTiles();

                if (resourceTilemapPanel.isModelsPanel || resourceTilemapPanel.isTraitsPanel)
                {
                    TilePanel.selectedResourceTile = resourceTilemapPanel.getTileAtCoords(0, 0);

                }
                else
                {
                    TilePanel.selectedResourceTile = resourceTilemapPanel.getTileAtCoords(currentlySelectedTile.getCol(), currentlySelectedTile.getRow());
                }

                TilePanel.selectedResourceTile.setIsSelected(true);
            }
        });

        JPanel resourceAndComponentPanel = new JPanel(new BorderLayout(10, 10));
        resourceAndComponentPanel.add(resourcesTabbedPane, BorderLayout.NORTH);

        levelEditorPanel = new LevelEditorTilemapPanel(levelEditorTileCols, levelEditorTileRows, tileSize);
        JScrollPane levelEditorScrollPane = new JScrollPane(levelEditorPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        levelEditorScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        levelEditorScrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_UNIT);
        levelEditorScrollPane.setPreferredSize(new Dimension(LEVEL_EDITOR_DEFAULT_WIDTH, LEVEL_EDITOR_DEFAULT_HEIGHT));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, levelEditorScrollPane, resourceAndComponentPanel);
        splitPane.setResizeWeight(0.84);

        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setPreferredSize(new Dimension(LEVEL_EDITOR_DEFAULT_WIDTH + SIDE_BAR_PANELS_DEFAULT_WIDTH, LEVEL_EDITOR_DEFAULT_HEIGHT));
        add(splitPane, BorderLayout.CENTER);
    }

    public File getCurrentWorkingFile()
    {
        return this.currentWorkingFile;
    }

    public BaseTilemapPanel getLevelEditorTilemap() { return this.levelEditorPanel; }

    public ResourceTilemapPanel getModelsPanel() { return this.modelsPanel; }

    public void setCurrentWorkingFile(final File newWorkingFile)
    {
        this.currentWorkingFile = newWorkingFile;
    }

    public void setResourceRootDirectory(final String resourceRootDirectory)
    {
        this.resourceRootDirectory = resourceRootDirectory;

        File configFile = new File(CONFIG_FILE_PATH);
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(configFile)))
        {
            bw.write(resourceRootDirectory);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private Map<File, File> extractModelsToTextureFiles()
    {
        Map<File, File> result = new HashMap<>();

        File[] allModelFiles = new File(resourceRootDirectory + MODELS_RELATIVE_DIRECTORY).listFiles();
        File[] allTextureFiles = new File(resourceRootDirectory + TEXTURES_RELATIVE_DIRECTORY).listFiles();

        for (File f: allModelFiles)
        {
            if (f.getName().startsWith("."))
            {
                continue;
            }

            String modelFileName = f.getName().split("\\.")[0];
            for (File tf: allTextureFiles)
            {
                if (tf.getName().startsWith(modelFileName))
                {
                    result.put(f, tf);
                }
            }
        }

        return result;
    }

    private void checkForEditorConfig()
    {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists())
        {
            try(BufferedReader br = new BufferedReader(new FileReader(configFile)))
            {
                String selectedResourceRootAbsolutePath = br.readLine();
                SelectResourceDirectoryHandler.resourceDirectoryChooserOriginPath = selectedResourceRootAbsolutePath;
                if (MainPanel.isValidResourceRootPath(selectedResourceRootAbsolutePath))
                {
                    resourceRootDirectory = selectedResourceRootAbsolutePath;
                }
                else
                {
                    showRootResourceDirectoryChooser();
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            showRootResourceDirectoryChooser();
        }
    }

    private void showRootResourceDirectoryChooser()
    {
        JOptionPane.showMessageDialog(null, "Config file not found, or invalid. Please select the project's root resource directory", "Bad editor config file", JOptionPane.INFORMATION_MESSAGE);
        new SelectResourceDirectoryHandler(this).selectResourceDirectory();
    }

}
