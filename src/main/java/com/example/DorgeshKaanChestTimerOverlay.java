package com.example;

import java.awt.Color;
import java.awt.Dimension;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class DorgeshKaanChestTimerOverlay extends OverlayPanel
{
	private final DorgeshKaanChestPlugin plugin;

	@Inject
	private DorgeshKaanChestTimerOverlay(DorgeshKaanChestPlugin plugin)
	{
		this.plugin = plugin;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(java.awt.Graphics2D graphics)
	{
		String text = plugin.getHopIndicatorText();
		if (text == null)
		{
			return null;
		}

		panelComponent.getChildren().clear();
		panelComponent.setBackgroundColor(new Color(33, 33, 33, 180));
		panelComponent.getChildren().add(LineComponent.builder()
			.left(text)
			.right("")
			.leftColor(plugin.isSwitchLocked() ? Color.RED : Color.GREEN)
			.build());

		return super.render(graphics);
	}
}
