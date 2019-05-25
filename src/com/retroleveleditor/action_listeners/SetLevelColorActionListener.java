package com.retroleveleditor.action_listeners;

import com.retroleveleditor.main.MainFrame;
import com.retroleveleditor.panels.LevelEditorTilemapPanel;
import com.retroleveleditor.util.Colors;
import com.retroleveleditor.util.DisposeDialogHandler;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class SetLevelColorActionListener implements ActionListener
{
    private MainFrame mainFrame;

    public SetLevelColorActionListener(final MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JDialog jDialog = new JDialog(mainFrame, "Set Level Color", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel colorSelectionPanel = new JPanel(new BorderLayout());
        colorSelectionPanel.setBorder(new EmptyBorder(25, 10, 15,10));
        String[] colorTypeNames = new String[Colors.values().length];
        for (int i = 0; i < Colors.values().length; ++i)
        {
            colorTypeNames[i] = Colors.values()[i].getName();
        }


        Colors currentlySelectedColor = ((LevelEditorTilemapPanel)mainFrame.getMainPanel().getLevelEditorTilemap()).getLevelColor();


        JPanel colorPreviewPanel = new JPanel();
        colorPreviewPanel.setPreferredSize(new Dimension(40, 20));
        colorPreviewPanel.setBackground(currentlySelectedColor.getColor());

        JComboBox<String> colorTypesComboBox = new JComboBox<String>(colorTypeNames);
        colorTypesComboBox.setSelectedIndex(Arrays.asList(Colors.values()).indexOf(currentlySelectedColor));
        colorTypesComboBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg)
            {
                colorPreviewPanel.setBackground(Colors.values()[colorTypesComboBox.getSelectedIndex()].getColor());
            }
        });

        colorSelectionPanel.add(colorTypesComboBox, BorderLayout.NORTH);
        colorSelectionPanel.add(colorPreviewPanel, BorderLayout.SOUTH);

        JButton setColorButton = new JButton("Set Color");
        setColorButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ((LevelEditorTilemapPanel)mainFrame.getMainPanel().getLevelEditorTilemap()).setLevelColor(Colors.values()[colorTypesComboBox.getSelectedIndex()]);
                jDialog.dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new DisposeDialogHandler(jDialog));

        JPanel actionButtonsPanel = new JPanel();
        actionButtonsPanel.add(setColorButton);
        actionButtonsPanel.add(cancelButton);
        actionButtonsPanel.setBorder(new EmptyBorder(15, 0, 10, 0));

        JPanel setLevelColorPanel = new JPanel(new BorderLayout());
        setLevelColorPanel.add(colorSelectionPanel, BorderLayout.NORTH);
        setLevelColorPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        jDialog.setContentPane(setLevelColorPanel);
        jDialog.getRootPane().setDefaultButton(setColorButton);
        jDialog.pack();
        jDialog.setResizable(false);
        jDialog.setLocationRelativeTo(mainFrame);
        jDialog.setVisible(true);
        jDialog.getContentPane().setLayout(null);
    }
}
