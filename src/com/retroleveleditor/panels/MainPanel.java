package com.retroleveleditor.panels;

import com.retroleveleditor.main.MainFrame;
import com.retroleveleditor.util.SelectResourceDirectoryHandler;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class MainPanel extends JPanel
{
    public static final String ENVIRONMENTS_ATLAS_RELATIVE_PATH = "/atlases/environments.png";
    public static final String CHARACTERS_ATLAS_RELATIVE_PATH = "/atlases/characters.png";
    public static final String TEXTURES_RELATIVE_DIRECTORY = "/textures/";
    public static final String MODELS_RELATIVE_DIRECTORY = "/models/";

    private static final int SCROLL_UNIT = 8;
    private static final int H_COMPONENT_GAP = 10;
    private static final int V_COMPONENT_GAP = 10;
    private static final String CONFIG_FILE_PATH = "config.rle";

    private static final int SIDE_BAR_PANELS_DEFAULT_WIDTH = 211;
    private static final int RESOURCES_PANEL_DEFAULT_HEIGHT = 350;
    private static final int LEVEL_EDITOR_DEFAULT_WIDTH = 768;
    private static final int LEVEL_EDITOR_DEFAULT_HEIGHT = 700;

    private String resourceRootDirectory;

    public static boolean isValidResourceRootPath(final String rootResourcePath)
    {
        return new File(rootResourcePath + MainPanel.ENVIRONMENTS_ATLAS_RELATIVE_PATH).exists() &&
               new File(rootResourcePath + MainPanel.CHARACTERS_ATLAS_RELATIVE_PATH).exists() &&
               new File(rootResourcePath + MainPanel.TEXTURES_RELATIVE_DIRECTORY).exists();
    }

    public MainPanel(final int levelEditorTileCols, final int levelEditorTileRows, final int tileSize)
    {
        super(new BorderLayout(H_COMPONENT_GAP, V_COMPONENT_GAP));

        checkForEditorConfig();

        JTabbedPane resourcesTabbedPane = new JTabbedPane();

        JScrollPane modelsScrollPane = new JScrollPane(new ModelsPanel(resourceRootDirectory + MODELS_RELATIVE_DIRECTORY), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        modelsScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        modelsScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

        JScrollPane charactersScrollPane = new JScrollPane(new ResourceTilemapPanel(resourceRootDirectory + CHARACTERS_ATLAS_RELATIVE_PATH,4, tileSize), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        charactersScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        charactersScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

        JScrollPane environmentsScrollPane = new JScrollPane(new ResourceTilemapPanel(resourceRootDirectory + ENVIRONMENTS_ATLAS_RELATIVE_PATH,4, tileSize), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        environmentsScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        environmentsScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));


        resourcesTabbedPane.addTab("Environments", environmentsScrollPane);
        resourcesTabbedPane.addTab("Characters", charactersScrollPane);
        resourcesTabbedPane.addTab("Models", modelsScrollPane);

        JPanel resourceAndComponentPanel = new JPanel(new BorderLayout(10, 10));
        resourceAndComponentPanel.add(resourcesTabbedPane, BorderLayout.NORTH);

        LevelEditorTilemapPanel levelEditorPanel = new LevelEditorTilemapPanel(levelEditorTileCols, levelEditorTileRows, tileSize);
        JScrollPane levelEditorScrollPane = new JScrollPane(levelEditorPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        levelEditorScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        levelEditorScrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_UNIT);
        levelEditorScrollPane.setPreferredSize(new Dimension(LEVEL_EDITOR_DEFAULT_WIDTH, LEVEL_EDITOR_DEFAULT_HEIGHT));

        add(resourceAndComponentPanel, BorderLayout.EAST);
        add(levelEditorScrollPane, BorderLayout.CENTER);
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

    private void checkForEditorConfig()
    {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists())
        {
            try(BufferedReader br = new BufferedReader(new FileReader(configFile)))
            {
                String selectedResourceRootAbsolutePath = br.readLine();
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
