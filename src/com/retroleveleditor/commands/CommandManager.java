package com.retroleveleditor.commands;

import java.util.LinkedList;
import java.util.List;

public class CommandManager
{
    private static List<ICommand> executedCommands = new LinkedList<>();
    private static List<ICommand> undoneCommands   = new LinkedList<>();

    public static void executeCommand(final ICommand command)
    {
        command.execute();
        executedCommands.add(command);
    }

    public static void undoLastCommand()
    {
        if (executedCommands.size() > 0)
        {
            ICommand command = executedCommands.remove(executedCommands.size() - 1);
            command.undo();
            undoneCommands.add(command);
        }
    }

    public static void redoLastCommand()
    {
        if (undoneCommands.size() > 0)
        {
            ICommand command = undoneCommands.remove(undoneCommands.size() - 1);
            executeCommand(command);
        }
    }
}
