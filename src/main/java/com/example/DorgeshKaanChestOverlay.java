package com.example;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class DorgeshKaanChestOverlay extends Overlay
{
	private final DorgeshKaanChestPlugin plugin;

	@Inject
	private DorgeshKaanChestOverlay(DorgeshKaanChestPlugin plugin)
	{
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		DorgeshKaanChestConfig config = plugin.getConfig();
		graphics.setStroke(new BasicStroke(config.outlineWidth()));

		for (DorgeshKaanChestPlugin.ChestState chestState : plugin.getChestStates())
		{
			Color baseColor;
			switch (chestState.getRenderState())
			{
				case LOOTED:
					baseColor = config.lootedColor();
					break;
				case OPENING:
					baseColor = config.openingColor();
					break;
				case LOOTABLE:
				default:
					baseColor = config.lootableColor();
					break;
			}
			Color fillColor = withAlpha(baseColor, config.fillOpacity());

			Shape hull = chestState.getGameObject().getConvexHull();
			if (hull != null)
			{
				graphics.setColor(fillColor);
				graphics.fill(hull);
				graphics.setColor(baseColor);
				graphics.draw(hull);
				continue;
			}

			Polygon polygon = chestState.getGameObject().getCanvasTilePoly();
			if (polygon != null)
			{
				graphics.setColor(fillColor);
				graphics.fill(polygon);
				graphics.setColor(baseColor);
				graphics.draw(polygon);
			}
		}

		return null;
	}

	private Color withAlpha(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
