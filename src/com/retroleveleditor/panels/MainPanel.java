package com.retroleveleditor.panels;

import com.retroleveleditor.main.MainFrame;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel
{
    public static final String ENVIRONMENTS_ATLAS_PATH = System.getProperty("os.name").indexOf("Win") >= 0
            ? "/Users/alex/Desktop/Code/ProjectRetro/res/atlases/environments.png"
            : "/Users/alex/Desktop/Code/ProjectRetro/res/atlases/environments.png";

    public static final String CHARACTERS_ATLAS_PATH = System.getProperty("os.name").indexOf("Win") >= 0
            ? "/Users/alex/Desktop/Code/ProjectRetro/res/atlases/characters.png"
            : "/Users/alex/Desktop/Code/ProjectRetro/res/atlases/characters.png";

    public static final String MODELS_TEXTURES_DIRECTORY = System.getProperty("os.name").indexOf("Win") >= 0
            ? "/Users/alex/Desktop/Code/ProjectRetro/res/textures/"
            : "/Users/alex/Desktop/Code/ProjectRetro/res/textures/";

    private static final int SCROLL_UNIT = 8;
    private static final int H_COMPONENT_GAP = 10;
    private static final int V_COMPONENT_GAP = 10;

    private static final int SIDE_BAR_PANELS_DEFAULT_WIDTH = 211;
    private static final int RESOURCES_PANEL_DEFAULT_HEIGHT = 350;
    private static final int LEVEL_EDITOR_DEFAULT_WIDTH = 768;
    private static final int LEVEL_EDITOR_DEFAULT_HEIGHT = 700;

    public MainPanel(final int levelEditorTileCols, final int levelEditorTileRows, final int tileSize)
    {
        super(new BorderLayout(H_COMPONENT_GAP, V_COMPONENT_GAP));

        JTabbedPane resourcesTabbedPane = new JTabbedPane();

        JScrollPane charactersScrollPane = new JScrollPane(new ResourceTilemapPanel(CHARACTERS_ATLAS_PATH,4, tileSize), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        charactersScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        charactersScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

        JScrollPane environmentsScrollPane = new JScrollPane(new ResourceTilemapPanel(ENVIRONMENTS_ATLAS_PATH,4, tileSize), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        environmentsScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        environmentsScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

        JScrollPane modelsScrollPane = new JScrollPane(new ModelsPanel(MODELS_TEXTURES_DIRECTORY), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        charactersScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        charactersScrollPane.setPreferredSize(new Dimension(SIDE_BAR_PANELS_DEFAULT_WIDTH, RESOURCES_PANEL_DEFAULT_HEIGHT));

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
}
