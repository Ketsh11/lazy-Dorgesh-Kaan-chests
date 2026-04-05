package com.ketsh11.dorgeshkaanchests;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DorgeshKaanChestPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DorgeshKaanChestPlugin.class);
		RuneLite.main(args);
	}
}
