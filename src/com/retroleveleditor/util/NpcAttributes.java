package com.retroleveleditor.util;

import java.util.List;

public class NpcAttributes
{
    public enum MovemenType
    {
        STATIC, STATIONARY, DYNAMIC
    }

    public final String mainDialog;
    public final List<String> sideDialogs;
    public final List<PokemonInfo> pokemonRoster;
    public final MovemenType movementType;
    public final int direction;
    public final boolean isTrainer;
    public final boolean isGymLeader;

    public NpcAttributes
    (
        final String mainDialog,
        final List<String> sideDialogs,
        final List<PokemonInfo> pokemonRoster,
        final MovemenType movementType,
        final int direction,
        final boolean isTrainer,
        final boolean isGymLeader
    )
    {
        this.mainDialog    = mainDialog;
        this.sideDialogs   = sideDialogs;
        this.pokemonRoster = pokemonRoster;
        this.movementType  = movementType;
        this.direction     = direction;
        this.isTrainer     = isTrainer;
        this.isGymLeader   = isGymLeader;
    }
}
