package com.retroleveleditor.action_listeners;

import com.retroleveleditor.commands.CommandManager;
import com.retroleveleditor.commands.SetTileNpcAttributesCommand;
import com.retroleveleditor.main.MainFrame;
import com.retroleveleditor.panels.LabelledInputPanel;
import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.DisposeDialogHandler;
import com.retroleveleditor.util.NpcAttributes;
import com.retroleveleditor.util.PokemonInfo;
import com.retroleveleditor.util.SelectAllFocusListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class SetNpcAttributesActionListener implements ActionListener
{
    private static final EmptyBorder PANEL_BORDER = new EmptyBorder(10, 5, 10, 5);
    private static final int MAX_NUMBER_OF_SIDE_DIALOGS_ALLOWED = 4;

    private final String[] pokemonNames;

    private MainFrame mainFrame;
    private List<JPanel> sideDialogs;
    private List<JPanel> pokemonRosterEntryPanels;
    private JPanel trainerDataPanel;

    public SetNpcAttributesActionListener(final MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
        this.sideDialogs = new ArrayList<>();
        this.pokemonRosterEntryPanels = new ArrayList<>();
        this.trainerDataPanel = null;
        this.pokemonNames = extractPokemonNames();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JDialog jDialog = new JDialog(mainFrame, "Select Npc Attributes", Dialog.ModalityType.APPLICATION_MODAL);
        jDialog.getRootPane().registerKeyboardAction(new DisposeDialogHandler(jDialog), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        sideDialogs.clear();
        pokemonRosterEntryPanels.clear();
        trainerDataPanel = null;

        NumberFormatter dimensionsFormatter = new NumberFormatter(NumberFormat.getInstance());
        dimensionsFormatter.setValueClass(Integer.class);
        dimensionsFormatter.setMinimum(1);
        dimensionsFormatter.setMaximum(100);
        dimensionsFormatter.setCommitsOnValidEdit(false);

        // Movement Panel
        JLabel movementTypeLabel = new JLabel("Movement Type");
        String[] movementTypeStrings = new String[NpcAttributes.MovementType.values().length];
        for (int i = 0; i < movementTypeStrings.length; ++i)
        {
            movementTypeStrings[i] = NpcAttributes.MovementType.values()[i].toString();
        }

        JComboBox<String> movementTypesComboBox = new JComboBox<>(movementTypeStrings);

        JPanel movementTypePanel = new JPanel();
        movementTypePanel.setLayout(new BorderLayout());
        movementTypePanel.add(movementTypeLabel, BorderLayout.WEST);
        movementTypePanel.add(movementTypesComboBox, BorderLayout.EAST);
        movementTypePanel.setBorder(PANEL_BORDER);

        // Direction Panel
        String[] directionStrings = new String[5];
        directionStrings[0] = "NO_DIRECTION";
        directionStrings[1] = "NORTH";
        directionStrings[2] = "EAST";
        directionStrings[3] = "SOUTH";
        directionStrings[4] = "WEST";

        JComboBox<String> directionComboBox = new JComboBox<>(directionStrings);
        JLabel directionLabel = new JLabel("Direction");

        JPanel directionPanel = new JPanel();
        directionPanel.setLayout(new BorderLayout());
        directionPanel.add(directionLabel, BorderLayout.WEST);
        directionPanel.add(directionComboBox, BorderLayout.EAST);
        directionPanel.setBorder(PANEL_BORDER);

        // Main Dialog Panel
        JLabel mainDialogLabel = new JLabel("Main Dialog", SwingConstants.CENTER);
        JTextArea mainDialogTextArea = new JTextArea(3, 30);
        mainDialogTextArea.setLineWrap(true);
        JScrollPane mainDialogTextScrollPane = new JScrollPane(mainDialogTextArea);

        JPanel mainDialogPanel = new JPanel(new BorderLayout());
        mainDialogPanel.add(mainDialogLabel, BorderLayout.NORTH);
        mainDialogPanel.add(mainDialogTextScrollPane, BorderLayout.SOUTH);
        mainDialogPanel.setBorder(PANEL_BORDER);

        // Side Dialogs Panel
        JPanel sideDialogsPanel = new JPanel();
        sideDialogsPanel.setLayout(new BoxLayout(sideDialogsPanel, BoxLayout.Y_AXIS));

        JLabel sideDialogsLabel = new JLabel("Side Dialogs", SwingConstants.CENTER);
        JButton addSideDialogButton = new JButton("Add Side Dialog");
        addSideDialogButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (sideDialogs.size() < MAX_NUMBER_OF_SIDE_DIALOGS_ALLOWED)
                {
                    JLabel sideDialogLabel = new JLabel("Side Dialog " + (sideDialogs.size() + 1), SwingConstants.CENTER);
                    JTextArea sideDialogTextArea = new JTextArea(3, 30);
                    sideDialogTextArea.setLineWrap(true);
                    JScrollPane sideDialogTextScrollPane = new JScrollPane(sideDialogTextArea);

                    JPanel sideDialogPanel = new JPanel(new BorderLayout());
                    sideDialogPanel.add(sideDialogLabel, BorderLayout.NORTH);
                    sideDialogPanel.add(sideDialogTextScrollPane, BorderLayout.SOUTH);
                    sideDialogPanel.setBorder(PANEL_BORDER);

                    sideDialogsPanel.add(sideDialogPanel);
                    sideDialogs.add(sideDialogPanel);

                    jDialog.revalidate();
                    jDialog.repaint();

                }
            }
        });

        JButton removeSideDialogButton = new JButton("Remove Side Dialog");
        removeSideDialogButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (sideDialogs.size() > 0)
                {
                    sideDialogsPanel.remove(sideDialogs.get(sideDialogs.size() - 1));
                    sideDialogs.remove(sideDialogs.size() - 1);

                    jDialog.revalidate();
                    jDialog.repaint();

                }
            }
        });

        JPanel sideDialogsButtonsPanel = new JPanel(new BorderLayout());
        sideDialogsButtonsPanel.add(sideDialogsLabel, BorderLayout.NORTH);
        sideDialogsButtonsPanel.add(addSideDialogButton, BorderLayout.WEST);
        sideDialogsButtonsPanel.add(removeSideDialogButton, BorderLayout.EAST);

        sideDialogsPanel.add(sideDialogsButtonsPanel);
        sideDialogsPanel.setBorder(PANEL_BORDER);

        JCheckBox isTrainerCheckBox = new JCheckBox("isTrainer");
        isTrainerCheckBox.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel trainerDataMasterPanel = new JPanel(new BorderLayout());
        trainerDataMasterPanel.add(isTrainerCheckBox, BorderLayout.NORTH);
        trainerDataMasterPanel.setBorder(PANEL_BORDER);

        isTrainerCheckBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == 1)
                {
                    if (trainerDataPanel == null)
                    {
                        trainerDataPanel = new JPanel(new BorderLayout());
                        trainerDataPanel.add(new JLabel("Trainer Data", SwingConstants.CENTER), BorderLayout.NORTH);
                        trainerDataPanel.setBorder(new LineBorder(Color.GRAY, 1));


                        JPanel checkBoxAndPokemonRosterPanel = new JPanel(new BorderLayout());
                        JCheckBox isGymLeaderCheckbox = new JCheckBox("isGymLeader");
                        isGymLeaderCheckbox.setHorizontalAlignment(SwingConstants.CENTER);

                        JPanel pokemonRosterButtonsAndEntriesPanel = new JPanel(new BorderLayout());
                        JPanel pokemonRosterButtons = new JPanel(new BorderLayout());
                        JButton addPokemonButton = new JButton("Add Pokemon");
                        JButton removePokemonButton = new JButton("Remove Pokemon");


                        pokemonRosterButtons.add(addPokemonButton, BorderLayout.WEST);
                        pokemonRosterButtons.add(removePokemonButton, BorderLayout.EAST);
                        pokemonRosterButtons.setBorder(PANEL_BORDER);

                        pokemonRosterButtonsAndEntriesPanel.add(pokemonRosterButtons, BorderLayout.NORTH);
                        pokemonRosterButtonsAndEntriesPanel.setBorder(PANEL_BORDER);

                        JPanel allPokemonEntryPanels = new JPanel();
                        allPokemonEntryPanels.setLayout(new BoxLayout(allPokemonEntryPanels, BoxLayout.Y_AXIS));

                        pokemonRosterButtonsAndEntriesPanel.add(allPokemonEntryPanels, BorderLayout.SOUTH);

                        checkBoxAndPokemonRosterPanel.add(isGymLeaderCheckbox, BorderLayout.NORTH);
                        checkBoxAndPokemonRosterPanel.add(pokemonRosterButtonsAndEntriesPanel, BorderLayout.SOUTH);
                        checkBoxAndPokemonRosterPanel.setBorder(PANEL_BORDER);

                        trainerDataPanel.add(checkBoxAndPokemonRosterPanel, BorderLayout.SOUTH);

                        trainerDataMasterPanel.add(trainerDataPanel);

                        addPokemonButton.addActionListener(new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                if (pokemonRosterEntryPanels.size() < 6)
                                {
                                    JPanel pokemonEntryPanel = new JPanel();
                                    pokemonEntryPanel.setLayout(new BoxLayout(pokemonEntryPanel, BoxLayout.X_AXIS));

                                    JComboBox<String> pokemonNamesCheckbox = new JComboBox<>(pokemonNames);
                                    pokemonEntryPanel.add(pokemonNamesCheckbox);

                                    JFormattedTextField levelTextField = new JFormattedTextField(dimensionsFormatter);
                                    levelTextField.setValue(5);
                                    levelTextField.setColumns(3);
                                    levelTextField.addFocusListener(new SelectAllFocusListener(levelTextField));
                                    pokemonEntryPanel.add(levelTextField);

                                    allPokemonEntryPanels.add(pokemonEntryPanel);
                                    pokemonRosterEntryPanels.add(pokemonEntryPanel);

                                    jDialog.revalidate();
                                    jDialog.repaint();

                                }
                            }
                        });

                        removePokemonButton.addActionListener(new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                if (pokemonRosterEntryPanels.size() > 0)
                                {
                                    allPokemonEntryPanels.remove(pokemonRosterEntryPanels.get(pokemonRosterEntryPanels.size() - 1));
                                    pokemonRosterEntryPanels.remove(pokemonRosterEntryPanels.size() - 1);

                                    jDialog.revalidate();
                                    jDialog.repaint();

                                }
                            }
                        });
                    }
                }
                else
                {
                    if (trainerDataPanel != null)
                    {
                        pokemonRosterEntryPanels.clear();
                        trainerDataMasterPanel.remove(trainerDataPanel);
                        trainerDataPanel = null;
                    }
                }

                jDialog.revalidate();
                jDialog.repaint();

            }
        });



        JPanel npcAttributesPanel = new JPanel();
        npcAttributesPanel.setLayout(new BoxLayout(npcAttributesPanel, BoxLayout.Y_AXIS));
        npcAttributesPanel.add(movementTypePanel);
        npcAttributesPanel.add(directionPanel);
        npcAttributesPanel.add(mainDialogPanel);
        npcAttributesPanel.add(sideDialogsPanel);
        npcAttributesPanel.add(trainerDataMasterPanel);

        JButton createButton = new JButton("OK");
        createButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                NpcAttributes.MovementType npcMovementType = NpcAttributes.MovementType.valueOf((String)movementTypesComboBox.getSelectedItem());
                int npcDirection = directionComboBox.getSelectedIndex() - 1;
                String npcMainDialog = mainDialogTextArea.getText();
                npcMainDialog = npcMainDialog.replace("\t", "");

                List<String> npcSideDialogs = new ArrayList<>();
                for (JPanel sideDialogPanel: sideDialogs)
                {
                    for (Component component: sideDialogPanel.getComponents())
                    {
                        if (component instanceof JScrollPane)
                        {
                            JScrollPane sideDialogScrollPane = (JScrollPane)component;
                            JTextArea textArea = (JTextArea)sideDialogScrollPane.getViewport().getView();

                            if (textArea.getText().length() > 0)
                            {
                                npcSideDialogs.add(textArea.getText().replace("\t",""));
                            }
                        }
                    }
                }

                boolean npcIsTrainer = isTrainerCheckBox.isSelected();
                List<PokemonInfo> npcPokemonRosterInfo = new ArrayList<>();
                boolean npcIsGymLeader = false;

                if (npcIsTrainer)
                {
                    for (Component component: trainerDataPanel.getComponents())
                    {
                        if (component instanceof JPanel)
                        {
                            for (Component innerComponent : ((JPanel)component).getComponents())
                            {
                                if (innerComponent instanceof JCheckBox)
                                {
                                    npcIsGymLeader = ((JCheckBox)innerComponent).isSelected();
                                }
                            }
                        }
                    }

                    for (JPanel panel: pokemonRosterEntryPanels)
                    {
                        String pokemonName = "";
                        int pokemonLevel   = 1;

                        for (Component component : panel.getComponents())
                        {
                            if (component instanceof JComboBox)
                            {
                                 pokemonName = (String)(((JComboBox<String>)component).getSelectedItem());
                            }
                            else if (component instanceof JFormattedTextField)
                            {
                                pokemonLevel = (int)(((JFormattedTextField)component).getValue());
                            }
                        }

                        npcPokemonRosterInfo.add(new PokemonInfo(pokemonName, pokemonLevel));
                    }
                }

                Component[] components = mainFrame.getMainPanel().getLevelEditorTilemap().getComponents();
                for (Component component: components)
                {
                    if (component instanceof TilePanel)
                    {
                        TilePanel tile = (TilePanel) component;
                        if (tile.isMouseHoveringOverTile())
                        {
                            NpcAttributes npcAttributes = new NpcAttributes
                            (
                                npcMainDialog,
                                npcSideDialogs,
                                npcPokemonRosterInfo,
                                npcMovementType,
                                npcDirection,
                                npcIsTrainer,
                                npcIsGymLeader
                            );

                            CommandManager.executeCommand(new SetTileNpcAttributesCommand(tile, npcAttributes));

                            break;
                        }
                    }
                }

                jDialog.dispose();
                mainFrame.getRootPane().revalidate();
                mainFrame.getRootPane().repaint();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new DisposeDialogHandler(jDialog));

        JPanel actionButtonsPanel = new JPanel();
        actionButtonsPanel.add(createButton);
        actionButtonsPanel.add(cancelButton);

        JPanel npcAttributesButtonsPanel = new JPanel(new BorderLayout());
        npcAttributesButtonsPanel.add(actionButtonsPanel, BorderLayout.CENTER);

        JPanel npcAttributesMasterPanel = new JPanel(new BorderLayout());
        npcAttributesMasterPanel.add(npcAttributesPanel, BorderLayout.NORTH);
        npcAttributesMasterPanel.add(npcAttributesButtonsPanel, BorderLayout.SOUTH);

        JScrollPane masterNpcAttributesScrollablePanel = new JScrollPane(npcAttributesMasterPanel);
        masterNpcAttributesScrollablePanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        masterNpcAttributesScrollablePanel.setPreferredSize(new Dimension(400, 600));
        masterNpcAttributesScrollablePanel.setMaximumSize(new Dimension(400, 600));

        jDialog.setContentPane(masterNpcAttributesScrollablePanel);
        jDialog.getRootPane().setDefaultButton(createButton);
        jDialog.pack();
        jDialog.setResizable(true);
        jDialog.setLocationRelativeTo(mainFrame);
        jDialog.setVisible(true);
    }

    String[] extractPokemonNames()
    {
        String[] pokemonNames = new String[151];

        int pokemonNameIndex = 0;
        File pokemonTexturesDirectory = new File(mainFrame.getMainPanel().getGameTexturesDirectoryPath() + "pkmnfront/");
        for (File f: pokemonTexturesDirectory.listFiles())
        {
            if (f.getName().startsWith("\\.")) continue;
            pokemonNames[pokemonNameIndex++] = f.getName().replace(".png", "");
        }

        return pokemonNames;
    }

}
