package com.retroleveleditor.action_listeners;

import com.retroleveleditor.panels.MainPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveActionListener implements ActionListener
{
    private static final String LEVEL_FILE_EXTENSION = ".json";
    public static String resourceDirectoryChooserOriginPath = ".";

    private final MainPanel mainPanel;
    private final boolean shouldAlwaysSaveToDifferentLocation;

    public SaveActionListener(final MainPanel mainPanel, final boolean shouldAlwaysSaveToDifferentLocation)
    {
        this.mainPanel = mainPanel;
        this.shouldAlwaysSaveToDifferentLocation = shouldAlwaysSaveToDifferentLocation;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (shouldAlwaysSaveToDifferentLocation == false && mainPanel.getCurrentWorkingFile() != null)
        {
            saveLevelToFile(mainPanel.getCurrentWorkingFile());
        }
        else
        {
            JFileChooser fc = new JFileChooser(resourceDirectoryChooserOriginPath);
            FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("JSON (*.json)", "json");
            fc.setFileFilter(fileFilter);

            int choice = fc.showSaveDialog(mainPanel);
            if (choice == JFileChooser.APPROVE_OPTION)
            {
                File selFile = fc.getSelectedFile();
                File adjustedFile = selFile;

                if (!selFile.getName().endsWith(LEVEL_FILE_EXTENSION))
                {
                    adjustedFile = new File(selFile.getAbsolutePath() + LEVEL_FILE_EXTENSION);
                }

                if (adjustedFile.exists())
                {
                    int selOption = JOptionPane.showConfirmDialog (null, "Overwrite existing file?", "Save Option", JOptionPane.YES_NO_OPTION);
                    if (selOption == JOptionPane.YES_OPTION)
                    {
                        saveLevelToFile(adjustedFile);
                        JOptionPane.showMessageDialog(mainPanel, "Successfully saved level at: " + adjustedFile.getAbsolutePath(), "Save Level", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else
                {
                    saveLevelToFile(adjustedFile);
                    JOptionPane.showMessageDialog(mainPanel, "Successfully saved level at: " + adjustedFile.getAbsolutePath(), "Save Level", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

    }

    private void saveLevelToFile(final File file)
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file)))
        {
            StringBuilder fileContentsBuilder = new StringBuilder();
            fileContentsBuilder.append("{\n");


            fileContentsBuilder.append("}\n");
            bw.write(fileContentsBuilder.toString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        mainPanel.setCurrentWorkingFile(file);
        SaveActionListener.resourceDirectoryChooserOriginPath = file.getAbsolutePath();
    }
}
