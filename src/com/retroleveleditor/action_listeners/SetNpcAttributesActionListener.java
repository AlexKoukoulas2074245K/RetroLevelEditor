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
        JDialog jDialog = new JDialog(mainFrame, "New Level Specification", Dialog.ModalityType.APPLICATION_MODAL);
        jDialog.getRootPane().registerKeyboardAction(new DisposeDialogHandler(jDialog), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        NumberFormatter dimensionsFormatter = new NumberFormatter(NumberFormat.getInstance());
        dimensionsFormatter.setValueClass(Integer.class);
        dimensionsFormatter.setMinimum(1);
        dimensionsFormatter.setCommitsOnValidEdit(false);

        LabelledInputPanel levelColsPanel = new LabelledInputPanel("Columns: ", dimensionsFormatter, 2, 32);
        LabelledInputPanel levelRowsPanel = new LabelledInputPanel("Rows: ", dimensionsFormatter, 2, 32);

        JPanel newLevelDimensionsPanel = new JPanel();
        newLevelDimensionsPanel.setLayout(new BoxLayout(newLevelDimensionsPanel, BoxLayout.X_AXIS));
        newLevelDimensionsPanel.add(levelColsPanel);
        newLevelDimensionsPanel.add(new JLabel("x"));
        newLevelDimensionsPanel.add(levelRowsPanel);

        LabelledInputPanel newLevelTileSizePanel = new LabelledInputPanel("Tile Size: ", dimensionsFormatter, 3, 48);

        JPanel newLevelSpecsPanel = new JPanel();
        newLevelSpecsPanel.setLayout(new BoxLayout(newLevelSpecsPanel, BoxLayout.Y_AXIS));
        newLevelSpecsPanel.add(newLevelDimensionsPanel);
        newLevelSpecsPanel.add(newLevelTileSizePanel);

        JButton createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                mainFrame.resetContentPane((int)levelColsPanel.getTextField().getValue(), (int)levelRowsPanel.getTextField().getValue(), (int)newLevelTileSizePanel.getTextField().getValue());
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

        JPanel newLevelActionButtonsPanel = new JPanel(new BorderLayout());
        newLevelActionButtonsPanel.add(actionButtonsPanel, BorderLayout.EAST);

        JPanel newLevelPanel = new JPanel(new BorderLayout());
        newLevelPanel.add(newLevelSpecsPanel, BorderLayout.NORTH);
        newLevelPanel.add(newLevelActionButtonsPanel, BorderLayout.SOUTH);

        jDialog.setContentPane(newLevelPanel);
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
