package com.retroleveleditor.commands;

public interface ICommand
{
    void execute();
    void undo();
    boolean isIdenticalTo(ICommand other);
}
