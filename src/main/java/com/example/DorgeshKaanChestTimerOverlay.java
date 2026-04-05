package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class DorgeshKaanChestTimerOverlay extends Overlay
{
	private static final int PADDING_X = 10;
	private static final int PADDING_Y = 6;
	private static final int BORDER_RADIUS = 12;

	private final DorgeshKaanChestPlugin plugin;

	@Inject
	private DorgeshKaanChestTimerOverlay(DorgeshKaanChestPlugin plugin)
	{
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		String text = plugin.getHopIndicatorText();
		if (text == null)
		{
			return null;
		}

		Font oldFont = graphics.getFont();
		Font font = plugin.isSwitchLocked() ? FontManager.getRunescapeBoldFont() : FontManager.getRunescapeFont();
		graphics.setFont(font.deriveFont(plugin.isSwitchLocked() ? 36f : 34f));

		int textWidth = graphics.getFontMetrics().stringWidth(text);
		int textHeight = graphics.getFontMetrics().getHeight();
		int width = textWidth + (PADDING_X * 2);
		int height = textHeight + (PADDING_Y * 2);
		Dimension clip = graphics.getClipBounds() != null
			? new Dimension(graphics.getClipBounds().width, graphics.getClipBounds().height)
			: new Dimension(765, 503);
		int canvasWidth = clip.width;
		int canvasHeight = clip.height;
		int x = Math.max(8, (canvasWidth - width) / 2);
		int y = Math.max(8, (canvasHeight - height) / 2);

		Color background = plugin.isSwitchLocked() ? new Color(120, 12, 12, 220) : new Color(20, 120, 35, 220);
		Color border = plugin.isSwitchLocked() ? new Color(255, 70, 70) : new Color(130, 255, 130);

		graphics.setColor(background);
		graphics.fillRoundRect(x, y, width, height, BORDER_RADIUS, BORDER_RADIUS);
		graphics.setColor(border);
		graphics.drawRoundRect(x, y, width, height, BORDER_RADIUS, BORDER_RADIUS);

		int textX = x + PADDING_X;
		int textY = y + PADDING_Y + graphics.getFontMetrics().getAscent();
		OverlayUtil.renderTextLocation(graphics, new Point(textX, textY), text, Color.WHITE);
		graphics.setFont(oldFont);

		return null;
	}
}
