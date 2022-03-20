package com.youarethomas.arborealis.runes;

import com.google.gson.annotations.SerializedName;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** The Rune object created from a JSON rune file
 */
public abstract class AbstractRune {
    public String name;
    public String colour;
    public Identifier catalyst;
    public int lifeForce;
    public int[] shape;
    public RuneSettings settings;

    public int getIntColour() {
        return Integer.decode(colour);
    }

    public abstract void onRuneFound(World world, BlockPos pos, CarvedLogEntity be);

    public abstract void onRuneLost(World world, BlockPos pos, CarvedLogEntity be);

    public abstract void onServerTick(World world, BlockPos pos, CarvedLogEntity be);

    public abstract void onClientTick(World world, BlockPos pos, CarvedLogEntity be);

    public boolean showRadiusEffect() {
        return false;
    }

    public AbstractRune withSettings(RuneSettings settings) {
        this.name = settings.name;
        this.colour = settings.colour;
        this.catalyst = new Identifier(settings.catalyst);
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
