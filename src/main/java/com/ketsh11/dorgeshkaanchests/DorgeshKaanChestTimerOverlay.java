package com.ketsh11.dorgeshkaanchests;

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
		String statsText = plugin.getStatisticsText();
		boolean afkSafe = plugin.getConfig().afkSafeMode();
		boolean actionRequired = plugin.isActionRequiredIndicator();

		if (afkSafe && !actionRequired)
		{
			text = null;
			statsText = null;
		}

		if (text == null && statsText == null)
		{
			return null;
		}

		Font oldFont = graphics.getFont();
		Font mainFont = plugin.isSwitchLocked() ? FontManager.getRunescapeBoldFont() : FontManager.getRunescapeFont();
		graphics.setFont(mainFont.deriveFont(plugin.isSwitchLocked() ? 36f : 34f));

		int textWidth = text != null ? graphics.getFontMetrics().stringWidth(text) : 0;
		int textHeight = text != null ? graphics.getFontMetrics().getHeight() : 0;
		Dimension clip = graphics.getClipBounds() != null
			? new Dimension(graphics.getClipBounds().width, graphics.getClipBounds().height)
			: new Dimension(765, 503);
		int canvasWidth = clip.width;
		int canvasHeight = clip.height;
		int mainWidth = textWidth + (PADDING_X * 2);
		int mainHeight = textHeight + (PADDING_Y * 2);
		int mainX = Math.max(8, (canvasWidth - mainWidth) / 2);
		int mainY = Math.max(8, (canvasHeight - mainHeight) / 2);

		boolean flash = afkSafe && actionRequired && ((System.currentTimeMillis() / 250) % 2 == 0);
		Color background = plugin.isSwitchLocked()
			? (flash ? new Color(180, 20, 20, 235) : new Color(120, 12, 12, 220))
			: (flash ? new Color(35, 170, 55, 235) : new Color(20, 120, 35, 220));
		Color border = plugin.isSwitchLocked()
			? (flash ? new Color(255, 220, 220) : new Color(255, 70, 70))
			: (flash ? new Color(230, 255, 230) : new Color(130, 255, 130));

		if (text != null)
		{
			graphics.setColor(background);
			graphics.fillRoundRect(mainX, mainY, mainWidth, mainHeight, BORDER_RADIUS, BORDER_RADIUS);
			graphics.setColor(border);
			graphics.drawRoundRect(mainX, mainY, mainWidth, mainHeight, BORDER_RADIUS, BORDER_RADIUS);

			int textX = mainX + PADDING_X;
			int textY = mainY + PADDING_Y + graphics.getFontMetrics().getAscent();
			OverlayUtil.renderTextLocation(graphics, new Point(textX, textY), text, Color.WHITE);
		}

		if (statsText != null)
		{
			graphics.setFont(FontManager.getRunescapeSmallFont());
			int statsWidth = graphics.getFontMetrics().stringWidth(statsText) + (PADDING_X * 2);
			int statsHeight = graphics.getFontMetrics().getHeight() + (PADDING_Y * 2);
			int statsX = Math.max(8, (canvasWidth - statsWidth) / 2);
			int statsY = (text != null ? mainY + mainHeight + 8 : Math.max(8, (canvasHeight - statsHeight) / 2));

			graphics.setColor(new Color(15, 15, 15, 210));
			graphics.fillRoundRect(statsX, statsY, statsWidth, statsHeight, BORDER_RADIUS, BORDER_RADIUS);
			graphics.setColor(new Color(180, 180, 180));
			graphics.drawRoundRect(statsX, statsY, statsWidth, statsHeight, BORDER_RADIUS, BORDER_RADIUS);

			int statsTextX = statsX + PADDING_X;
			int statsTextY = statsY + PADDING_Y + graphics.getFontMetrics().getAscent();
			OverlayUtil.renderTextLocation(graphics, new Point(statsTextX, statsTextY), statsText, Color.WHITE);
		}
		graphics.setFont(oldFont);

		return null;
	}
}
