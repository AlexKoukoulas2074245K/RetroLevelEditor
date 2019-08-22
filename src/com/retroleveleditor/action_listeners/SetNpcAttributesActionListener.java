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
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class SetNpcAttributesActionListener implements ActionListener
{
    private MainFrame mainFrame;

    public SetNpcAttributesActionListener(final MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JDialog jDialog = new JDialog(mainFrame, "Select Npc Attributes", Dialog.ModalityType.APPLICATION_MODAL);
        jDialog.getRootPane().registerKeyboardAction(new DisposeDialogHandler(jDialog), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        NumberFormatter dimensionsFormatter = new NumberFormatter(NumberFormat.getInstance());
        dimensionsFormatter.setValueClass(Integer.class);
        dimensionsFormatter.setMinimum(1);
        dimensionsFormatter.setCommitsOnValidEdit(false);

        JLabel movementTypeLabel = new JLabel("Movement Type");
        String[] movementTypeStrings = new String[NpcAttributes.MovementType.values().length];
        for (int i = 0; i < movementTypeStrings.length; ++i)
        {
            movementTypeStrings[i] = NpcAttributes.MovementType.values()[i].toString();
        }

        JComboBox<String> movementTypesComboBox = new JComboBox<>(movementTypeStrings);

        JPanel movementTypePanel = new JPanel();
        movementTypePanel.setLayout(new BoxLayout(movementTypePanel, BoxLayout.X_AXIS));
        movementTypePanel.add(movementTypeLabel);
        movementTypePanel.add(movementTypesComboBox);


        String[] directionStrings = new String[5];
        directionStrings[0] = "NO_DIRECTION";
        directionStrings[1] = "NORTH";
        directionStrings[2] = "EAST";
        directionStrings[3] = "SOUTH";
        directionStrings[4] = "WEST";

        JComboBox<String> directionComboBox = new JComboBox<>(directionStrings);
        JLabel directionLabel = new JLabel("Direction");

        JPanel directionPanel = new JPanel();
        directionPanel.setLayout(new BoxLayout(directionPanel, BoxLayout.X_AXIS));
        directionPanel.add(directionLabel);
        directionPanel.add(directionComboBox);

        JPanel npcAttributesPanel = new JPanel();
        npcAttributesPanel.setLayout(new BoxLayout(npcAttributesPanel, BoxLayout.Y_AXIS));
        npcAttributesPanel.add(movementTypePanel);
        npcAttributesPanel.add(directionPanel);

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
        npcAttributesButtonsPanel.add(actionButtonsPanel, BorderLayout.EAST);

        JPanel npcAttributesMasterPanel = new JPanel(new BorderLayout());
        npcAttributesMasterPanel.add(npcAttributesPanel, BorderLayout.NORTH);
        npcAttributesMasterPanel.add(npcAttributesButtonsPanel, BorderLayout.SOUTH);

        jDialog.setContentPane(npcAttributesMasterPanel);
        jDialog.getRootPane().setDefaultButton(createButton);
        jDialog.pack();
        jDialog.setResizable(false);
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
