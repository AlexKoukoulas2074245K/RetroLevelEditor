package com.retroleveleditor.util;

import java.util.List;

public class NpcAttributes
{
    public enum MovementType
    {
        STATIC, STATIONARY, DYNAMIC
    }

    public final String mainDialog;
    public final String trainerName;
    public final List<String> sideDialogs;
    public final List<PokemonInfo> pokemonRoster;
    public final MovementType movementType;
    public final int direction;
    public final boolean isTrainer;
    public final boolean isGymLeader;

    public NpcAttributes
    (
        final String mainDialog,
        final String trainerName,
        final List<String> sideDialogs,
        final List<PokemonInfo> pokemonRoster,
        final MovementType movementType,
        final int direction,
        final boolean isTrainer,
        final boolean isGymLeader
    )
    {
        this.mainDialog    = mainDialog;
        this.trainerName   = trainerName;
        this.sideDialogs   = sideDialogs;
        this.pokemonRoster = pokemonRoster;
        this.movementType  = movementType;
        this.direction     = direction;
        this.isTrainer     = isTrainer;
        this.isGymLeader   = isGymLeader;
    }

    @Override
    public String toString()
    {
        StringBuilder pokemonStringBuilder = new StringBuilder();
        int charCounter = 0;

        pokemonStringBuilder.append("[");
        boolean commaToggle = false;
        for (PokemonInfo pokemon: pokemonRoster)
        {
            if (commaToggle == false)
            {
                commaToggle = true;
            }
            else
            {
                pokemonStringBuilder.append(", ");
            }

            pokemonStringBuilder.append("L" + pokemon.pokemonLevel + " " + pokemon.pokemonName);
        }
        pokemonStringBuilder.append("]");

        StringBuilder mainDialogStringBuilder = new StringBuilder();
        charCounter = 0;
        for (int i = 0; i < mainDialog.length(); ++i)
        {
            mainDialogStringBuilder.append(mainDialog.charAt(i));

            if (++charCounter == 100)
            {
                charCounter = 0;
                mainDialogStringBuilder.append("<br>");
            }
        }

        StringBuilder sideDialogsStringBuilder = new StringBuilder();
        sideDialogsStringBuilder.append(sideDialogs.toString());
        charCounter = 0;
        for (int i = 0; i < sideDialogsStringBuilder.length(); ++i)
        {
            if (++charCounter == 100)
            {
                charCounter = 0;
                sideDialogsStringBuilder.insert(i, "<br>");
            }
        }

        return "<html>trainerName: " + trainerName + "<br>" +
               "movementType: " + movementType.toString() + "<br>" +
               "direction: " + direction + "<br>" +
               "isTrainer: " + (isTrainer ? "true<br>" : "false<br>") +
               "isGymLeader: " + (isGymLeader ? "true<br>" : "false<br>") +
                "pokemon: " + pokemonStringBuilder.toString() + "<br>" +
                "mainDialog: " + mainDialogStringBuilder.toString() + "<br>" +
               "sideDialogs: " + sideDialogsStringBuilder.toString() + "<br>" +
               "</html>";

    }
}
