package com.retroleveleditor.util;

import com.retroleveleditor.main.Main;
import com.retroleveleditor.panels.MainPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class SelectResourceDirectoryHandler implements ActionListener
{
    public static String resourceDirectoryChooserOriginPath = ".";

    private final MainPanel mainPanel;
    public SelectResourceDirectoryHandler(final MainPanel mainPanel)
    {
        this.mainPanel = mainPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        selectResourceDirectory();
    }

    public void selectResourceDirectory()
    {
        try {
            EventQueue.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    showDirectorySelectionDialog();
                }
            });
        }
        catch (InterruptedException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private void showDirectorySelectionDialog()
    {
        while (true)
        {
            JFileChooser fc = new JFileChooser(resourceDirectoryChooserOriginPath);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            String selectedResourceRootAbsolutePath = "";
            int choice = fc.showOpenDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION)
            {
                selectedResourceRootAbsolutePath = fc.getSelectedFile().getAbsolutePath().replace('\\', '/');
                resourceDirectoryChooserOriginPath = selectedResourceRootAbsolutePath;
                if (MainPanel.isValidResourceRootPath(selectedResourceRootAbsolutePath))
                {
                    mainPanel.setResourceRootDirectory(selectedResourceRootAbsolutePath);
                    break;
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "Chosen file was not a resource directory.\nPlease select the project's root resource directory", "Bad editor config file", JOptionPane.ERROR_MESSAGE);
                }
            }
            else if (choice == JFileChooser.CANCEL_OPTION)
            {
                System.exit(0);
            }
        }
    }

}
