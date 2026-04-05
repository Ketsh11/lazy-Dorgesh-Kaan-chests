package com.example;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("dorgeshkaanchests")
public interface DorgeshKaanChestConfig extends Config
{
	@ConfigItem(
		keyName = "lootableColor",
		name = "Lootable color",
		description = "Color for chests that can be pick-locked"
	)
	default Color lootableColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "lootedColor",
		name = "Looted color",
		description = "Color for chests that are currently looted / unavailable"
	)
	default Color lootedColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "openingColor",
		name = "Opening color",
		description = "Color for chests that are in the opening animation/state"
	)
	default Color openingColor()
	{
		return Color.YELLOW;
	}

	@Range(
		min = 1,
		max = 8
	)
	@ConfigItem(
		keyName = "outlineWidth",
		name = "Outline width",
		description = "Overlay outline width"
	)
	default int outlineWidth()
	{
		return 2;
	}

	@Range(
		min = 0,
		max = 255
	)
	@ConfigItem(
		keyName = "fillOpacity",
		name = "Fill opacity",
		description = "Opacity of chest fill color (0-255)"
	)
	default int fillOpacity()
	{
		return 65;
	}

	@ConfigItem(
		keyName = "hopHotkey",
		name = "Hop hotkey",
		description = "Press to hop when HOP NOW is active"
	)
	default Keybind hopHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "hopFilterSource",
		name = "Hop filter source",
		description = "Choose internal filtering or World Hopper plugin filters"
	)
	default HopFilterSource hopFilterSource()
	{
		return HopFilterSource.INTERNAL;
	}

}
