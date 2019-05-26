package com.retroleveleditor.main;

import com.retroleveleditor.action_listeners.*;
import com.retroleveleditor.panels.MainPanel;
import com.retroleveleditor.util.SelectResourceDirectoryHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.security.Key;

public class MainFrame extends JFrame
{
    public static final int MENU_MODIFIER_KEY = System.getProperty("os.name").indexOf("Win") >= 0 ? ActionEvent.CTRL_MASK : ActionEvent.META_MASK;

    private MainPanel mainPanel;

    public MainFrame()
    {
        super("Retro Level Editor");

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        resetContentPane(32, 32, 48);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(true);
        addWindowListener(new ProgramExitingWindowAdapter());
    }

    public void resetContentPane(final int cols, final int rows, final int tileSize)
    {
        mainPanel = new MainPanel(cols, rows, tileSize);
        setContentPane(mainPanel);
        createMenus();
    }

    public MainPanel getMainPanel()
    {
        return mainPanel;
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createOptionsMenu());
        setJMenuBar(menuBar);
    }

    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New..");
        JMenuItem openMenuItem = new JMenuItem("Open..");
        JMenuItem saveMenuItem = new JMenuItem("Save..");
        JMenuItem saveAsMenuItem = new JMenuItem("Save As..");
        JMenuItem changeResourceDirectoryMenuItem = new JMenuItem("Change Root Res Directory");
        JMenuItem quitMenuItem = new JMenuItem("Quit");

        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_MODIFIER_KEY));
        newMenuItem.addActionListener(new NewCanvasActionListener(this));

        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MENU_MODIFIER_KEY));
        openMenuItem.addActionListener(new OpenActionListener(this));

        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_MODIFIER_KEY));
        saveMenuItem.addActionListener(new SaveActionListener(mainPanel, false));

        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_MODIFIER_KEY|ActionEvent.SHIFT_MASK));
        saveAsMenuItem.addActionListener(new SaveActionListener(mainPanel, true));

        changeResourceDirectoryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_MODIFIER_KEY));
        changeResourceDirectoryMenuItem.addActionListener(new SelectResourceDirectoryHandler(mainPanel));

        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, MENU_MODIFIER_KEY));
        quitMenuItem.addActionListener(new QuitActionHandler(this));

        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(changeResourceDirectoryMenuItem);
        fileMenu.add(quitMenuItem);

        return fileMenu;
    }

    private JMenu createEditMenu()
    {
        JMenu editMenu = new JMenu("Edit");

        JMenuItem undoMenuItem = new JMenuItem("Undo");
        JMenuItem redoMenuItem = new JMenuItem("Redo");


        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MENU_MODIFIER_KEY));
        undoMenuItem.addActionListener(new UndoActionListener(this));

        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MENU_MODIFIER_KEY | ActionEvent.SHIFT_MASK));
        redoMenuItem.addActionListener(new RedoActionListener(this));

        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);

        return editMenu;
    }

    private JMenu createOptionsMenu()
    {
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem setLevelColorMenuItem      = new JMenuItem("Set Level Color");
        JMenuItem setLevelTileFillerMenuItem = new JMenuItem("Set Level Tile Filler");

        setLevelColorMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.SHIFT_MASK));
        setLevelColorMenuItem.addActionListener(new SetLevelColorActionListener(this));

        setLevelTileFillerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.SHIFT_MASK));
        setLevelTileFillerMenuItem.addActionListener(new SetLevelTileFillerActionListener(this));

        optionsMenu.add(setLevelColorMenuItem);
        optionsMenu.add(setLevelTileFillerMenuItem);

        return optionsMenu;
    }
}
