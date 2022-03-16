package com.youarethomas.therotwithin;

import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.therotwithin.runes.Test;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class TheRotWithin implements ModInitializer {

	public static final String MOD_ID = "therotwithin";
	public static final Random RANDOM = new Random();

	public static final Logger LOGGER = LogManager.getLogger();

	public static final Test TEST = new Test();

	@Override
	public void onInitialize() {
		RuneManager.register(new Identifier(MOD_ID, "test"), TEST);

		RuneManager.initializeRunes(new Identifier(MOD_ID, "runes"));

		LOGGER.info("The Rot Within Initialised!");
	}
}
