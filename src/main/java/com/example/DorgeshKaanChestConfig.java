package com.ketsh11.dorgeshkaanchests;

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

	@Range(
		min = 4000,
		max = 8000
	)
	@ConfigItem(
		keyName = "hopCountdownMillis",
		name = "Hop timer (ms)",
		description = "Countdown duration after 'You attempt to pick the lock.'"
	)
	default int hopCountdownMillis()
	{
		return 5500;
	}

	@ConfigItem(
		keyName = "showStatistics",
		name = "Show statistics",
		description = "Display session stats under the center banner"
	)
	default boolean showStatistics()
	{
		return false;
	}

	@Range(
		min = 0,
		max = 700
	)
	@ConfigItem(
		keyName = "defaultWorld",
		name = "Default world",
		description = "Cycle restart world for hotkey hopping (0 = disabled)"
	)
	default int defaultWorld()
	{
		return 0;
	}

	@Range(
		min = 1,
		max = 50
	)
	@ConfigItem(
		keyName = "hopCycleLength",
		name = "Hop cycle length",
		description = "After this many hotkey hops, restart at default world"
	)
	default int hopCycleLength()
	{
		return 15;
	}

	@Range(
		min = 60,
		max = 900
	)
	@ConfigItem(
		keyName = "chestRespawnSeconds",
		name = "Chest respawn (s)",
		description = "Time before returning to default world is considered worthwhile"
	)
	default int chestRespawnSeconds()
	{
		return 300;
	}

	@ConfigItem(
		keyName = "afkSafeMode",
		name = "AFK-safe mode",
		description = "Only show strong alerts for HOP NOW / ALL CHESTS LOOTED, with flash + beep"
	)
	default boolean afkSafeMode()
	{
		return false;
	}

}
