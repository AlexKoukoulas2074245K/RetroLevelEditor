package com.retroleveleditor.util;

import java.awt.*;

public enum Colors
{
    PALLET_COLOR(new Color(184, 136, 248, 255), "PALLET"),
    VIRIDIAN_COLOR(new Color(147, 247, 31, 255), "VIRIDIAN"),
    PEWTER_COLOR(new Color(144, 144, 120, 255), "PEWTER"),
    CAVE_COLOR(new Color(184, 64, 0, 255), "CAVE"),
    CERULEAN_COLOR(new Color(40, 64, 248, 255), "CERULEAN"),
    VERMILION_COLOR(new Color(248, 152, 0, 255), "VERMILION"),
    LAVENDER_COLOR(new Color(200, 32, 248, 255), "LAVENDER");

    private final Color color;
    private final String name;
    private Colors(final Color color, final String colorName)
    {
        this.color = color;
        this.name = colorName;
    }

    public Color getColor() { return this.color; }
    public String getName() { return this.name; }
}
