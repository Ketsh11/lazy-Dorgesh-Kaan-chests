package com.example;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Dorgesh-Kaan Chests"
)
public class DorgeshKaanChestPlugin extends Plugin
{
	private static final int LOOTABLE_CHEST_ID = 22681;
	private static final int LOOTED_CHEST_ID = 22682;
	private static final int OPENING_CHEST_ID = 22683;

	@Inject
	private Client client;

	@Inject
	private DorgeshKaanChestConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DorgeshKaanChestOverlay overlay;

	private final List<ChestState> chestStates = new ArrayList<>();

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		log.debug("Dorgesh-Kaan chests plugin started");
	}

	@Override
	protected void shutDown()
	{
		chestStates.clear();
		overlayManager.remove(overlay);
		log.debug("Dorgesh-Kaan chests plugin stopped");
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			chestStates.clear();
			return;
		}

		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();
		Tile[][] planeTiles = tiles[client.getPlane()];
		if (planeTiles == null)
		{
			chestStates.clear();
			return;
		}

		Player localPlayer = client.getLocalPlayer();
		WorldPoint playerLocation = localPlayer != null ? localPlayer.getWorldLocation() : null;
		List<ChestState> found = new ArrayList<>();

		for (Tile[] row : planeTiles)
		{
			if (row == null)
			{
				continue;
			}

			for (Tile tile : row)
			{
				if (tile == null)
				{
					continue;
				}

				for (GameObject object : tile.getGameObjects())
				{
					if (object == null)
					{
						continue;
					}

					int id = object.getId();
					if (id == LOOTABLE_CHEST_ID || id == LOOTED_CHEST_ID || id == OPENING_CHEST_ID)
					{
						ChestRenderState state = id == LOOTABLE_CHEST_ID
							? ChestRenderState.LOOTABLE
							: id == LOOTED_CHEST_ID
								? ChestRenderState.LOOTED
								: ChestRenderState.OPENING;
						found.add(new ChestState(object, state));
					}
				}
			}
		}

		if (playerLocation != null)
		{
			found.sort((a, b) ->
				Integer.compare(
					a.gameObject.getWorldLocation().distanceTo(playerLocation),
					b.gameObject.getWorldLocation().distanceTo(playerLocation)
				)
			);
		}

		chestStates.clear();
		for (ChestState state : found)
		{
			chestStates.add(state);
			if (chestStates.size() == 2)
			{
				break;
			}
		}
	}

	@Provides
	DorgeshKaanChestConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DorgeshKaanChestConfig.class);
	}

	List<ChestState> getChestStates()
	{
		return Collections.unmodifiableList(chestStates);
	}

	DorgeshKaanChestConfig getConfig()
	{
		return config;
	}

	static class ChestState
	{
		private final GameObject gameObject;
		private final ChestRenderState renderState;

		private ChestState(GameObject gameObject, ChestRenderState renderState)
		{
			this.gameObject = gameObject;
			this.renderState = renderState;
		}

		GameObject getGameObject()
		{
			return gameObject;
		}

		ChestRenderState getRenderState()
		{
			return renderState;
		}
	}

	enum ChestRenderState
	{
		LOOTABLE,
		LOOTED,
		OPENING
	}
}
