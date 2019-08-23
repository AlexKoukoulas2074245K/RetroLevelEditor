package com.retroleveleditor.action_listeners;

import com.retroleveleditor.commands.CommandManager;
import com.retroleveleditor.commands.SetTileNpcAttributesCommand;
import com.retroleveleditor.main.MainFrame;
import com.retroleveleditor.panels.LabelledInputPanel;
import com.retroleveleditor.panels.TilePanel;
import com.retroleveleditor.util.DisposeDialogHandler;
import com.retroleveleditor.util.NpcAttributes;
import com.retroleveleditor.util.PokemonInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class SetNpcAttributesActionListener implements ActionListener
{
    private static final EmptyBorder PANEL_BORDER = new EmptyBorder(10, 5, 10, 5);
    private static final int MAX_NUMBER_OF_SIDE_DIALOGS_ALLOWED = 4;

    private MainFrame mainFrame;
    private List<JPanel> sideDialogs;
    private JPanel trainerDataPanel;

    public SetNpcAttributesActionListener(final MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
        this.sideDialogs = new ArrayList<>();
        this.trainerDataPanel = null;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JDialog jDialog = new JDialog(mainFrame, "Select Npc Attributes", Dialog.ModalityType.APPLICATION_MODAL);
        jDialog.getRootPane().registerKeyboardAction(new DisposeDialogHandler(jDialog), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        sideDialogs.clear();
        trainerDataPanel = null;

        NumberFormatter dimensionsFormatter = new NumberFormatter(NumberFormat.getInstance());
        dimensionsFormatter.setValueClass(Integer.class);
        dimensionsFormatter.setMinimum(1);
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
        JTextArea mainDialogTextArea = new JTextArea(5, 30);
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
                    JTextArea sideDialogTextArea = new JTextArea(5, 30);
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
                    jDialog.pack();
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
                    jDialog.pack();
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
                        trainerDataPanel.add(new JLabel("SDADASDJADJSA"));

                        trainerDataMasterPanel.add(trainerDataPanel);
                    }
                }
                else
                {
                    if (trainerDataPanel != null)
                    {
                        trainerDataMasterPanel.remove(trainerDataPanel);
                        trainerDataPanel = null;
                    }
                }

                jDialog.revalidate();
                jDialog.repaint();
                jDialog.pack();
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

        jDialog.setContentPane(npcAttributesMasterPanel);
        jDialog.getRootPane().setDefaultButton(createButton);
        jDialog.pack();
        jDialog.setResizable(true);
        jDialog.setLocationRelativeTo(mainFrame);
        jDialog.setVisible(true);

        Component[] components = mainFrame.getMainPanel().getLevelEditorTilemap().getComponents();
        for (Component component: components)
        {
            if (component instanceof TilePanel)
            {
                TilePanel tile = (TilePanel) component;
                if (tile.isMouseHoveringOverTile())
                {
                    /*
                    List<String> sideDialogs = new ArrayList<>();
                    sideDialogs.add("adasdakalksd");
                    sideDialogs.add("gbxbxcvxcvzxc");
                    sideDialogs.add("afzxkjlkjlke");

                    List<PokemonInfo> pokemonInfo = new ArrayList<>();
                    pokemonInfo.add(new PokemonInfo("CHARIZARD", 5));
                    pokemonInfo.add(new PokemonInfo("MEWTWO", 100));

                    NpcAttributes npcAttributes = new NpcAttributes
                    (
                            "Main Dialog", sideDialogs, pokemonInfo, NpcAttributes.MovemenType.DYNAMIC, 0, false, false
                    );

                    CommandManager.executeCommand(new SetTileNpcAttributesCommand(tile, npcAttributes));
                                    */
                }
            }
        }
    }
}
