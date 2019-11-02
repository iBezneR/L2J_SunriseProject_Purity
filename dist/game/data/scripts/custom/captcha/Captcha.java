/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.captcha;

import custom.erengine.*;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.PledgeCrest;
import l2r.gameserver.network.serverpackets.TutorialCloseHtml;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;
import l2r.util.Rnd;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Erlandas
 */
public class Captcha
{
	private static ArrayList<String> NUMBERS;
	
	public Captcha()
	{
		NUMBERS = new ArrayList<>();
		for (int i = 0; i < 10; i++)
		{
			NUMBERS.add(i + "");
		}
		generateHTMLFile();
	}
	
	void startCaptcha(L2PcInstance player)
	{
		if (player.getCPlayer().getFailedCaptchas() >= ErConfig.CAPTCHA_FAIL)
		{
			if (player.getClient() == null)
			{
				return;
			}
			player.getClient().closeNow();
			return;
		}
		StringBuilder iCaptcha = new StringBuilder();
		StringBuilder newCaptcha = new StringBuilder();
		for (int i = 0; i < ErConfig.CAPTCHAS_LENGTH; i++)
		{
			iCaptcha.append("* ");
			newCaptcha.append(Rnd.get(0, 9));
		}
		player.getCPlayer().setCaptcha(newCaptcha.toString());
		player.getCPlayer().setInputedCaptcha(iCaptcha.toString());
		player.getCPlayer().setInputedNumbers();
		ArrayList<String> numbers = new ArrayList<>(NUMBERS);
		int[] xNumbers =
		{
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0
		};
		for (int i = 0; i < 10; i++)
		{
			int index = Rnd.get(0, numbers.size() - 1);
			xNumbers[i] = Integer.parseInt(numbers.get(index));
			numbers.remove(index);
		}
		player.getCPlayer().setHtmlNumbers(xNumbers);
		player.setIsParalyzed(true);
		player.startAbnormalEffect(AbnormalEffect.HOLD_1);
		player.setIsInvul(true);
		if (!ErConfig.CAPTCHA_SECOND_SECURITY_LEVEL)
		{
			showScreenMessage(player, "You have to input: " + player.getCPlayer().getCaptcha(), ErConfig.CAPTCHA_DELAY, ErSMPos.TOP_CENTER, false);
			player.sendMessage("You have to input: " + player.getCPlayer().getCaptcha());
		}
		else
		{
			int imgId = ErGlobalVariables.getInstance().getInt("LastImageId") < 10000 ? Rnd.get(1, 10) * 10000 : ErGlobalVariables.getInstance().getInt("LastImageId");
			player.getCPlayer().setImageId(imgId);
			try
			{
				player.sendPacket(new PledgeCrest(imgId, generateCaptcha(player)));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			ErGlobalVariables.getInstance().setData("LastImageId", ++imgId);
		}
		player.getCPlayer().startTimer();
		showCaptcha(player);
	}
	
	private void showCaptcha(L2PcInstance player)
	{
		String msg = HtmCache.getInstance().getHtm(player, "data/html/captcha/main.htm");
		msg = msg.replaceAll("%iCaptcha%", player.getCPlayer().getInputedCaptcha());
		int[] htmlNumbers = player.getCPlayer().getHtmlNumbers();
		for (int i = 0; i < 10; i++)
		{
			msg = msg.replaceAll("%" + i + "%", (htmlNumbers[i]) + "");
		}
		msg = msg.replaceAll("%rem%", (ErConfig.CAPTCHA_FAIL - player.getCPlayer().getFailedCaptchas()) + "");
		msg = msg.replaceAll("%captcha%", ErConfig.CAPTCHA_SECOND_SECURITY_LEVEL ? "<img src=\"Crest.crest_1_" + player.getCPlayer().getImageId() + "\" width=\"256\" height=\"64\"/>" : "");
		player.sendPacket(new TutorialShowHtml(msg));
	}
	
	public void onBypass(String command, L2PcInstance player)
	{
		if (command.startsWith("captcha_"))
		{
			if (player.getCPlayer().getInputedNumbers() >= ErConfig.CAPTCHAS_LENGTH)
			{
				showScreenMessage(player, "You have already imputed all captcha!", 5000, ErSMPos.BOTTOM_RIGHT, false);
				player.sendMessage("You have already imputed all captcha!");
				showCaptcha(player);
				return;
			}
			command = command.substring(8);
			String number = "" + player.getCPlayer().getHtmlNumbers()[Integer.parseInt(command)];
			player.getCPlayer().increaseInputedCaptcha(number);
			
			showCaptcha(player);
		}
		else if (command.equals("captchaClear"))
		{
			StringBuilder inputedCaptcha = new StringBuilder();
			for (int i = 0; i < ErConfig.CAPTCHAS_LENGTH; i++)
			{
				inputedCaptcha.append("* ");
			}
			player.getCPlayer().setInputedCaptcha(inputedCaptcha.toString());
			player.getCPlayer().setInputedNumbers();
			showCaptcha(player);
		}
		else if (command.equals("captchaRemove"))
		{
			StringBuilder inputedCaptcha = new StringBuilder();
			for (int i = 0; i < ErConfig.CAPTCHAS_LENGTH; i++)
			{
				inputedCaptcha.append("* ");
			}
			player.getCPlayer().decreaseInputedCaptcha(inputedCaptcha.toString());
			showCaptcha(player);
		}
		else if (command.equals("captchaConfirm"))
		{
			player.getCPlayer().stopTimer();
			if (player.getCPlayer().getInputedNumbers() >= ErConfig.CAPTCHAS_LENGTH)
			{
				checkCaptcha(player);
			}
			else
			{
				showCaptcha(player);
				showScreenMessage(player, "You haven't inputted all captcha!", 5000, ErSMPos.BOTTOM_RIGHT, false);
				player.sendMessage("You haven't inputted all captcha!");
			}
		}
	}
	
	void checkCaptcha(final L2PcInstance player)
	{
		if (player.getCPlayer().getInputedCaptcha().replaceAll(" ", "").equals(player.getCPlayer().getCaptcha()))
		{
			player.setIsParalyzed(false);
			player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
			ThreadPoolManager.getInstance().scheduleGeneral(() -> player.setIsInvul(false), 3000);
			showScreenMessage(player, "You have successfully got through the captcha!", 5000, ErSMPos.TOP_CENTER, false);
			player.sendMessage("You have successfully got through the captcha!");
			player.getCPlayer().setCaptcha("");
			player.getCPlayer().setFailedCaptchas(0);
			player.getCPlayer().setInputedCaptcha("");
			player.getCPlayer().setInputedNumbers();
			player.getCPlayer().clearKilledValues();
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
		}
		else
		{
			player.getCPlayer().setFailedCaptchas(player.getCPlayer().getFailedCaptchas() + 1);
			startCaptcha(player);
		}
	}
	
	private byte[] generateCaptcha(L2PcInstance player) throws IOException
	{
		File image = new File("data/images/captcha.png");
		final Color textColor = Color.decode("#6e6155");
		final Color circleColor = Color.decode("#2f2a20");
		final Color borderColor = Color.decode("#393838");
		final Font textFont = new Font("Times New Roman", Font.PLAIN, 34);
		final int charsToPrint = ErConfig.CAPTCHAS_LENGTH;
		final int width = 256;
		final int height = 64;
		final int circlesToDraw = 15;
		final int dotsToDraw = 50;
		final float horizMargin = 20.0f;
		final double rotationRange = 0.7;
		final BufferedImage bufferedImage = new BufferedImage(width, height, 1);
		final Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		g.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		g.setColor(borderColor);
		g.fillRect(0, 0, 256, 64);
		g.setColor(Color.decode("#181818"));
		g.fillRect(5, 5, 246, 54);
		g.setColor(circleColor);
		for (int i = 0; i < circlesToDraw; ++i)
		{
			final int circleRadius = (int) ((Math.random() * height) / 2.0);
			final int circleX = (int) ((Math.random() * width) - circleRadius);
			final int circleY = (int) ((Math.random() * height) - circleRadius);
			g.setStroke(new BasicStroke(Rnd.get(1, 5)));
			g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
		}
		for (int i = 0; i < dotsToDraw; ++i)
		{
			final int circleRadius = 1;
			final int circleX = (int) ((Math.random() * width) - circleRadius);
			final int circleY = (int) ((Math.random() * height) - circleRadius);
			g.setStroke(new BasicStroke(Rnd.get(1, 5)));
			g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
		}
		g.setColor(textColor);
		g.setFont(textFont);
		final FontMetrics fontMetrics = g.getFontMetrics();
		final int maxAdvance = fontMetrics.getMaxAdvance();
		final int fontHeight = fontMetrics.getHeight();
		final float spaceForLetters = (-horizMargin * 2.0f) + width;
		final float spacePerChar = spaceForLetters / (charsToPrint - 1.0f);
		int i = 0;
		for (char letter : player.getCPlayer().getCaptcha().toCharArray())
		{
			final int charWidth = fontMetrics.charWidth(letter);
			final int charDim = Math.max(maxAdvance, fontHeight);
			final int halfCharDim = charDim / 2;
			final BufferedImage charImage = new BufferedImage(charDim, charDim, 2);
			final Graphics2D charGraphics = charImage.createGraphics();
			charGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			charGraphics.translate(halfCharDim, halfCharDim);
			final double angle = (Math.random() - 0.5) * rotationRange;
			charGraphics.transform(AffineTransform.getRotateInstance(angle));
			charGraphics.translate(-halfCharDim, -halfCharDim);
			charGraphics.setColor(textColor);
			charGraphics.setFont(textFont);
			final int charX = (int) ((0.5 * charDim) - (0.5 * charWidth));
			charGraphics.drawString("" + letter, charX, ((charDim - fontMetrics.getAscent()) / 2) + fontMetrics.getAscent());
			final float x = (horizMargin + (spacePerChar * i)) - (charDim / 2.0f);
			final int y = (height - charDim) / 2;
			g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);
			charGraphics.dispose();
			i++;
		}
		g.dispose();
		ImageIO.write(bufferedImage, "png", image);
		return Objects.requireNonNull(DDSConverter.convertToDDS(image)).array();
	}
	
	void showScreenMessage(L2PcInstance player, String text, int time, ErSMPos position, boolean small)
	{
		player.sendPacket(new ExShowScreenMessage(1, 0, position.ordinal(), 0, small ? 1 : 0, 0, 0, false, time, true, text));
	}
	
	private void generateHTMLFile()
	{
		if (ErGlobalVariables.getInstance().getBoolean("CaptchaHTMLInitialized"))
		{
			return;
		}
		
		String text = "";
		text += "<html><body scroll=no>\n";
		text += "\n";
		text += "<img src=\"L2UI.SquareBlank\" width=285 height=3/>\n";
		text += "<img src=\"L2UI.SquareGray\" width=285 height=2/>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=285 height=3/>\n";
		text += "\n";
		text += "<table width=285 bgcolor=2E2E2E>\n";
		text += "<tr>\n";
		text += "<td width=285 align=center><font color=393838 name=\"ScreenMessageLarge\">| </font><font name=\"ScreenMessageLarge\">%iCaptcha%</font><font color=393838 name=\"ScreenMessageLarge\">|</font></td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "\n";
		text += "<img src=\"L2UI.SquareBlank\" width=285 height=3>\n";
		text += "<img src=\"L2UI.SquareGray\" width=285 height=2>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=286 height=6>\n";
		text += "\n";
		text += "<table>\n";
		text += "<tr>\n";
		text += "<td width=45></td>\n";
		text += "<td>\n";
		text += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\">\n";
		text += "<tr>\n";
		text += "<td width=5></td>\n";
		text += "<td width=40 align=center>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "<button value=\"\" action=\"link captcha_0\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%0%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%0%\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "<button value=\"\" action=\"link captcha_1\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%1%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%1%\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "<button value=\"\" action=\"link captcha_2\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%2%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%2%\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "<button value=\"\" action=\"link captcha_9\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%9%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%9%\"/>\n";
		text += "</td>\n";
		text += "<td width=5></td>\n";
		text += "</tr>\n";
		text += "<tr>\n";
		text += "<td width=5></td>\n";
		text += "<td width=40 align=center>\n";
		text += "<button value=\"\" action=\"link captcha_3\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%3%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%3%\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<button value=\"\" action=\"link captcha_4\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%4%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%4%\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<button value=\"\" action=\"link captcha_5\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%5%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%5%\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<button value=\"\" action=\"link captchaRemove\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_KeyBack\" back=\"L2UI_CT1.CharacterPassword_DF_KeyBack\"/>\n";
		text += "</td>\n";
		text += "<td width=5></td>\n";
		text += "</tr>\n";
		text += "<tr>\n";
		text += "<td width=5></td>\n";
		text += "<td width=40 align=center>\n";
		text += "<button value=\"\" action=\"link captcha_6\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%6%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%6%\"/>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<button value=\"\" action=\"link captcha_7\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%7%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%7%\"/>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<button value=\"\" action=\"link captcha_8\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_Key%8%\" back=\"L2UI_CT1.CharacterPassword_DF_Key%8%\"/>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/>\n";
		text += "</td>\n";
		text += "<td width=40 align=center>\n";
		text += "<button value=\"\" action=\"link captchaClear\" width=\"37\" height=\"32\" fore=\"L2UI_CT1.CharacterPassword_DF_KeyC\" back=\"L2UI_CT1.CharacterPassword_DF_KeyC\"/>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/>\n";
		text += "</td>\n";
		text += "<td width=5></td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "\n";
		text += "<center>\n";
		text += "<button value=\"Confirm Captcha\" action=\"link captchaConfirm\" width=\"198\" height=\"28\" fore=\"L2UI_CT1.OlympiadWnd_DF_Apply\" back=\"L2UI_CT1.OlympiadWnd_DF_Apply_Down\"/>\n";
		text += "<font name=\"ScreenMessageSmall\">Remaining %rem% tries!</font>\n";
		text += "%captcha%\n";
		text += "</center>\n";
		text += "</body></html>\n";
		
		ErGlobalVariables.getInstance().setData("CaptchaHTMLInitialized", true);
		ErUtils.generateFile("data/html/captcha/", "main", ".htm", text);
	}
	
	public static Captcha getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final Captcha _instance = new Captcha();
	}
}
