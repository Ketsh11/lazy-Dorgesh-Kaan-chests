package com.example;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.World;
import net.runelite.api.WorldType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Dorgesh-Kaan Chests"
)
public class DorgeshKaanChestPlugin extends Plugin
{
	private static final int LOOTABLE_CHEST_ID = 22681;
	private static final int LOOTED_CHEST_ID = 22682;
	private static final int OPENING_CHEST_ID = 22683;
	private static final int ACTIVE_RANGE_TILES = 12;
	private static final long HOP_COUNTDOWN_MILLIS = 5500L;
	private static final long ALL_LOOTED_NOTICE_MILLIS = 3500L;
	private static final String PICK_LOCK_ATTEMPT_MESSAGE = "You attempt to pick the lock.";
	private static final EnumSet<WorldType> DISALLOWED_WORLD_TYPES = EnumSet.of(
		WorldType.PVP,
		WorldType.HIGH_RISK,
		WorldType.DEADMAN,
		WorldType.BETA_WORLD,
		WorldType.TOURNAMENT_WORLD,
		WorldType.SEASONAL,
		WorldType.PVP_ARENA,
		WorldType.QUEST_SPEEDRUNNING
	);

	@Inject
	private Client client;

	@Inject
	private DorgeshKaanChestConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private DorgeshKaanChestOverlay overlay;

	@Inject
	private DorgeshKaanChestTimerOverlay timerOverlay;

	private final List<ChestState> chestStates = new ArrayList<>();
	private Instant hopReadyAt;
	private Instant allLootedNoticeUntil;
	private boolean inChestArea;
	private boolean awaitingPostHopChestCheck;

	private final HotkeyListener hopHotkeyListener = new HotkeyListener(() -> config.hopHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			clientThread.invoke(DorgeshKaanChestPlugin.this::tryHotkeyHop);
		}
	};

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(timerOverlay);
		keyManager.registerKeyListener(hopHotkeyListener);
		log.debug("Dorgesh-Kaan chests plugin started");
	}

	@Override
	protected void shutDown()
	{
		chestStates.clear();
		hopReadyAt = null;
		allLootedNoticeUntil = null;
		inChestArea = false;
		awaitingPostHopChestCheck = false;
		keyManager.unregisterKeyListener(hopHotkeyListener);
		overlayManager.remove(overlay);
		overlayManager.remove(timerOverlay);
		log.debug("Dorgesh-Kaan chests plugin stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.HOPPING)
		{
			awaitingPostHopChestCheck = true;
		}
		else if (event.getGameState() == GameState.LOGGED_IN)
		{
			// Also evaluate chest state after fresh login, not only world hop.
			awaitingPostHopChestCheck = true;
		}

		if (event.getGameState() != GameState.LOGGED_IN)
		{
			hopReadyAt = null;
			allLootedNoticeUntil = null;
			inChestArea = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			chestStates.clear();
			hopReadyAt = null;
			inChestArea = false;
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

		if (playerLocation == null || found.isEmpty())
		{
			inChestArea = false;
			hopReadyAt = null;
			return;
		}

		int nearestChestDistance = found.stream()
			.mapToInt(state -> state.getGameObject().getWorldLocation().distanceTo(playerLocation))
			.min()
			.orElse(Integer.MAX_VALUE);

		inChestArea = nearestChestDistance <= ACTIVE_RANGE_TILES;
		if (!inChestArea)
		{
			hopReadyAt = null;
		}

		if (awaitingPostHopChestCheck && inChestArea && chestStates.size() >= 2)
		{
			boolean lootableChestNearby = chestStates.stream()
				.anyMatch(state -> state.getRenderState() == ChestRenderState.LOOTABLE);

			if (!lootableChestNearby)
			{
				allLootedNoticeUntil = Instant.now().plusMillis(ALL_LOOTED_NOTICE_MILLIS);
			}

			awaitingPostHopChestCheck = false;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());
		if (message == null)
		{
			return;
		}

		if (inChestArea && PICK_LOCK_ATTEMPT_MESSAGE.equals(message))
		{
			hopReadyAt = Instant.now().plusMillis(HOP_COUNTDOWN_MILLIS);
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

	boolean isSwitchLocked()
	{
		return getSecondsUntilHopReady() > 0;
	}

	Integer getSwitchLockSecondsRemaining()
	{
		int seconds = getSecondsUntilHopReady();
		return seconds > 0 ? seconds : 0;
	}

	String getHopIndicatorText()
	{
		if (!inChestArea)
		{
			return null;
		}

		if (allLootedNoticeUntil != null && Instant.now().isBefore(allLootedNoticeUntil))
		{
			return "ALL CHESTS LOOTED";
		}

		boolean lootableChestNearby = chestStates.stream()
			.anyMatch(state -> state.getRenderState() == ChestRenderState.LOOTABLE);
		if (lootableChestNearby)
		{
			return null;
		}

		if (hopReadyAt == null)
		{
			return null;
		}

		int seconds = getSecondsUntilHopReady();
		if (seconds > 0)
		{
			return "HOP IN " + seconds + "s";
		}

		return "HOP NOW";
	}

	private int getSecondsUntilHopReady()
	{
		if (hopReadyAt == null)
		{
			return 0;
		}
		long millis = Duration.between(Instant.now(), hopReadyAt).toMillis();
		return (int) Math.max(0, (millis + 999) / 1000);
	}

	private void tryHotkeyHop()
	{
		if (!isHopNowActive())
		{
			return;
		}

		World target = findNextHopWorld();
		if (target == null)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No suitable next world found.", null);
			return;
		}

		client.hopToWorld(target);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Hopping to world " + target.getId() + ".", null);
	}

	private boolean isHopNowActive()
	{
		if (!inChestArea || hopReadyAt == null)
		{
			return false;
		}

		if (getSecondsUntilHopReady() > 0)
		{
			return false;
		}

		boolean lootableChestNearby = chestStates.stream()
			.anyMatch(state -> state.getRenderState() == ChestRenderState.LOOTABLE);
		return !lootableChestNearby;
	}

	private World findNextHopWorld()
	{
		World[] worlds = client.getWorldList();
		if (worlds == null || worlds.length == 0)
		{
			return null;
		}

		boolean onMembersWorld = client.getWorldType().contains(WorldType.MEMBERS);
		List<World> candidates = Arrays.stream(worlds)
			.filter(world -> isWorldAllowed(world, onMembersWorld))
			.sorted(Comparator.comparingInt(World::getId))
			.collect(Collectors.toList());

		if (candidates.isEmpty())
		{
			return null;
		}

		int currentWorldId = client.getWorld();
		for (World world : candidates)
		{
			if (world.getId() > currentWorldId)
			{
				return world;
			}
		}

		World fallback = candidates.get(0);
		if (fallback.getId() == currentWorldId && candidates.size() > 1)
		{
			return candidates.get(1);
		}
		return fallback;
	}

	private boolean isWorldAllowed(World world, boolean onMembersWorld)
	{
		if (config.hopFilterSource() == HopFilterSource.WORLD_HOPPER)
		{
			return isWorldAllowedByWorldHopper(world);
		}
		return isWorldAllowedInternal(world, onMembersWorld);
	}

	private boolean isWorldAllowedInternal(World world, boolean onMembersWorld)
	{
		if (world == null || world.getId() <= 0)
		{
			return false;
		}

		EnumSet<WorldType> types = world.getTypes();
		if (types == null)
		{
			return false;
		}

		boolean worldIsMembers = types.contains(WorldType.MEMBERS);
		if (worldIsMembers != onMembersWorld)
		{
			return false;
		}

		for (WorldType disallowed : DISALLOWED_WORLD_TYPES)
		{
			if (types.contains(disallowed))
			{
				return false;
			}
		}

		return true;
	}

	private boolean isWorldAllowedByWorldHopper(World world)
	{
		if (world == null || world.getId() <= 0)
		{
			return false;
		}

		EnumSet<WorldType> types = world.getTypes();
		if (types == null)
		{
			return false;
		}

		String subscriptionFilter = configManager.getConfiguration("worldhopper", "subscriptionFilter");
		boolean worldIsMembers = types.contains(WorldType.MEMBERS);
		if ("FREE".equals(subscriptionFilter) && worldIsMembers)
		{
			return false;
		}
		if ("MEMBERS".equals(subscriptionFilter) && !worldIsMembers)
		{
			return false;
		}

		Set<String> selectedWorldTypeFilters = parseEnumSet(configManager.getConfiguration("worldhopper", "worldTypeFilter"));
		if (!selectedWorldTypeFilters.isEmpty() && !matchesWorldTypeFilters(selectedWorldTypeFilters, types))
		{
			return false;
		}

		Set<String> selectedRegions = parseEnumSet(configManager.getConfiguration("worldhopper", "regionFilter"));
		if (!selectedRegions.isEmpty() && !matchesRegionFilters(selectedRegions, world.getLocation()))
		{
			return false;
		}

		return true;
	}

	private Set<String> parseEnumSet(String raw)
	{
		if (raw == null || raw.length() < 2 || "[]".equals(raw))
		{
			return Collections.emptySet();
		}

		String trimmed = raw.trim();
		if (!trimmed.startsWith("[") || !trimmed.endsWith("]"))
		{
			return Collections.emptySet();
		}

		String inner = trimmed.substring(1, trimmed.length() - 1).trim();
		if (inner.isEmpty())
		{
			return Collections.emptySet();
		}

		Set<String> values = new HashSet<>();
		for (String token : inner.split(","))
		{
			String value = token.trim();
			if (!value.isEmpty())
			{
				values.add(value);
			}
		}
		return values;
	}

	private boolean matchesWorldTypeFilters(Set<String> selectedFilters, Set<WorldType> types)
	{
		for (String filter : selectedFilters)
		{
			switch (filter)
			{
				case "NORMAL":
					if (isNormalWorld(types))
					{
						return true;
					}
					break;
				case "DEADMAN":
					if (types.contains(WorldType.DEADMAN))
					{
						return true;
					}
					break;
				case "SEASONAL":
					if (types.contains(WorldType.SEASONAL))
					{
						return true;
					}
					break;
				case "QUEST_SPEEDRUNNING":
					if (types.contains(WorldType.QUEST_SPEEDRUNNING))
					{
						return true;
					}
					break;
				case "FRESH_START_WORLD":
					if (types.contains(WorldType.FRESH_START_WORLD))
					{
						return true;
					}
					break;
				case "PVP":
					if (types.contains(WorldType.PVP))
					{
						return true;
					}
					break;
				case "SKILL_TOTAL":
					if (types.contains(WorldType.SKILL_TOTAL))
					{
						return true;
					}
					break;
				case "HIGH_RISK":
					if (types.contains(WorldType.HIGH_RISK))
					{
						return true;
					}
					break;
				case "BOUNTY_HUNTER":
					if (types.contains(WorldType.BOUNTY))
					{
						return true;
					}
					break;
				default:
					break;
			}
		}

		return false;
	}

	private boolean isNormalWorld(Set<WorldType> types)
	{
		return !types.contains(WorldType.DEADMAN)
			&& !types.contains(WorldType.SEASONAL)
			&& !types.contains(WorldType.QUEST_SPEEDRUNNING)
			&& !types.contains(WorldType.FRESH_START_WORLD)
			&& !types.contains(WorldType.PVP)
			&& !types.contains(WorldType.SKILL_TOTAL)
			&& !types.contains(WorldType.HIGH_RISK)
			&& !types.contains(WorldType.BOUNTY);
	}

	private boolean matchesRegionFilters(Set<String> selectedRegions, int location)
	{
		for (String region : selectedRegions)
		{
			switch (region)
			{
				case "UNITED_STATES":
					// US East + US West
					if (location == 0 || location == 9)
					{
						return true;
					}
					break;
				case "UNITED_KINGDOM":
					if (location == 1)
					{
						return true;
					}
					break;
				case "AUSTRALIA":
					if (location == 3)
					{
						return true;
					}
					break;
				case "GERMANY":
					if (location == 7)
					{
						return true;
					}
					break;
				case "BRAZIL":
					if (location == 8)
					{
						return true;
					}
					break;
				default:
					break;
			}
		}

		return false;
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
