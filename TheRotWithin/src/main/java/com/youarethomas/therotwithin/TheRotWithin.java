package com.youarethomas.therotwithin;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class TheRotWithin implements ModInitializer {

	public static final String MOD_ID = "therotwithin";
	public static final Random RANDOM = new Random();

	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		LOGGER.info("The Rot Within Initialised!");
	}
}
