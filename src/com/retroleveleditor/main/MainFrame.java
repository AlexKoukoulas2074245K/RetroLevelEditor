package com.retroleveleditor.main;

import com.retroleveleditor.action_listeners.RedoActionListener;
import com.retroleveleditor.action_listeners.UndoActionListener;
import com.retroleveleditor.panels.MainPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame
{
    public static final int MENU_MODIFIER_KEY = System.getProperty("os.name").indexOf("Win") >= 0 ? ActionEvent.CTRL_MASK : ActionEvent.META_MASK;

    public MainFrame()
    {
        super("Retro Level Editor");

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { e.printStackTrace(); }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        resetContentPane();
        createMenus();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(true);
        //addWindowListener(new ProgramExitingWindowAdapter());
    }

    public void resetContentPane()
    {
        setContentPane(new MainPanel(32, 32, 48));
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createComponentsMenu());
        menuBar.add(createGameMenu());
        menuBar.add(createOptionsMenu());
        setJMenuBar(menuBar);
    }

    private JMenu createFileMenu()
    {
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New..");
        JMenuItem loadMenuItem = new JMenuItem("Open..");
        JMenuItem saveMenuItem = new JMenuItem("Save As..");
        JMenuItem changeResourceDirectoryMenuItem = new JMenuItem("Change Root Res Directory");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_MODIFIER_KEY));
        //newMenuItem.addActionListener(new NewMenuItemActionHandler(this));

        loadMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MENU_MODIFIER_KEY));
        //loadMenuItem.addActionListener(new OpenMenuItemActionHandler(this));

        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_MODIFIER_KEY));
        //saveMenuItem.addActionListener(new SaveAsMenuItemActionHandler(this));

        changeResourceDirectoryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_MODIFIER_KEY));
        //changeResourceDirectoryMenuItem.addActionListener(new SelectResourceDirectoryHandler(this));

        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, MENU_MODIFIER_KEY));
        //exitMenuItem.addActionListener(new ExitMenuItemActionHandler(this));

        fileMenu.add(newMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(changeResourceDirectoryMenuItem);
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createEditMenu()
    {
        JMenu editMenu = new JMenu("Edit");

        JMenuItem undoMenuItem = new JMenuItem("Undo");
        JMenuItem redoMenuItem = new JMenuItem("Redo");
        JMenuItem cutMenuItem = new JMenuItem("Cut");
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        JMenuItem pasteMenuItem = new JMenuItem("Paste");

        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MENU_MODIFIER_KEY));
        undoMenuItem.addActionListener(new UndoActionListener(this));

        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, MENU_MODIFIER_KEY | InputEvent.SHIFT_MASK));
        redoMenuItem.addActionListener(new RedoActionListener(this));

        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, MENU_MODIFIER_KEY));
        //cutMenuItem.addActionListener(new CutMenuItemActionHandler(this));

        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, MENU_MODIFIER_KEY));
        //copyMenuItem.addActionListener(new CopyMenuItemActionHandler());

        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, MENU_MODIFIER_KEY));
        //pasteMenuItem.addActionListener(new PasteMenuItemActionHandler(this));

        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);

        return editMenu;
    }

    private JMenu createComponentsMenu()
    {
        JMenu componentsMenu = new JMenu("Components");

        JMenuItem addPhysicsComponentMenuItem  = new JMenuItem("Add Physics Component");
        addPhysicsComponentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, MENU_MODIFIER_KEY));
        //addPhysicsComponentMenuItem.addActionListener(new AddPhysicsComponentMenuItemActionHandler(this));

        JMenuItem addAIComponentMenuItem = new JMenuItem("Add AI Component..");
        addAIComponentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MENU_MODIFIER_KEY));
        //addAIComponentMenuItem.addActionListener(new AddAIComponentMenuItemActionHandler(this));

        JMenuItem addHealthComponentMenuItem = new JMenuItem("Add Health Component");
        addHealthComponentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, MENU_MODIFIER_KEY));
        //addHealthComponentMenuItem.addActionListener(new AddHealthComponentMenuItemActionHandler(this));

        JMenuItem addDamageComponenetMenuItem = new JMenuItem("Add Damage Component");
        addDamageComponenetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, MENU_MODIFIER_KEY));
        //addDamageComponenetMenuItem.addActionListener(new AddDamageComponentMenuItemActionHandler(this));

        componentsMenu.add(addPhysicsComponentMenuItem);
        componentsMenu.add(addAIComponentMenuItem);
        componentsMenu.add(addHealthComponentMenuItem);
        componentsMenu.add(addDamageComponenetMenuItem);

        return componentsMenu;
    }

    private JMenu createGameMenu()
    {
        JMenu gameMenu = new JMenu("Game");

        JMenuItem setTileEntityname = new JMenuItem("Set Tile Entity Name");
        setTileEntityname.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MENU_MODIFIER_KEY));
        //setCellEntityNameMenuItem.addActionListener(new SetCellEntityNameMenuItemActionHandler(this));

        gameMenu.add(setTileEntityname);
        return gameMenu;
    }

    private JMenu createOptionsMenu()
    {
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem toggleHitBoxDisplayMenuItem = new JMenuItem("Toggle Hitbox Display");
        toggleHitBoxDisplayMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, MENU_MODIFIER_KEY));
        //toggleHitBoxDisplayMenuItem.addActionListener(new ToggleHitBoxDisplayMenuItemActionHandler(this));

        optionsMenu.add(toggleHitBoxDisplayMenuItem);
        return optionsMenu;
    }
}
