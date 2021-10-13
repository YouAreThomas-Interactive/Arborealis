package com.youarethomas.arborealis.util;

import com.google.gson.annotations.SerializedName;

/** The Rune object created from a JSON rune file
 */
public class Rune {
    public String name;
    public String type;
    private String colour;
    public String[] catalysts;
    @SerializedName("life_force")
    public int lifeForce;
    public int[] shape;

    public int getColour() {
        return Integer.decode(colour);
    }
}
