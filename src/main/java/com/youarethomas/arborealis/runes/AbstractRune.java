package com.youarethomas.arborealis.runes;

import com.google.gson.annotations.SerializedName;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** The Rune object created from a JSON rune file
 */
public abstract class AbstractRune {
    public String name;
    private String colour;
    public String catalyst;
    @SerializedName("life_force")
    public int lifeForce;
    public int[] shape;
    public RuneSettings settings;

    public int getColour() {
        return Integer.decode(colour);
    }

    public abstract void onRuneFound(World world, BlockPos pos, CarvedWoodEntity be);

    public abstract void onRuneLost(World world, BlockPos pos, CarvedWoodEntity be);

    public void onServerTick(World world, BlockPos pos, CarvedWoodEntity be) {}

    public void onClientTick(World world, BlockPos pos, CarvedWoodEntity be) {}

    public boolean showRadiusEffect() {
        return false;
    }

    public AbstractRune withSettings(RuneSettings settings) {
        this.name = settings.name;
        this.colour = settings.colour;
        this.catalyst = settings.catalyst;
        this.lifeForce = settings.lifeForce;
        this.shape = settings.shape;
        this.settings = settings;
        return this;
    }

    public class RuneSettings {
        public String id;
        String name;
        String colour;
        String catalyst;
        @SerializedName("life_force")
        int lifeForce;
        int[] shape;
    }
}
