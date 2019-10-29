/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.achievements;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.communitybbs.Managers.BaseBBSManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.Map.Entry;

/**
 * @author Erlandys
 */
public class AchievementBBSManager extends BaseBBSManager
{
	protected AchievementBBSManager()
	{
	}
	
	public static AchievementBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		
		if (command.startsWith("_bbsachcategory") || command.equalsIgnoreCase("_bbsach"))
		{
			if (command.length() > 16)
			{
				sendWindow(activeChar, Integer.parseInt(command.substring(16)));
			}
			else
			{
				sendWindow(activeChar, 0);
			}
		}
		else if (command.startsWith("_bbsachievement"))
		{
			if (command.length() > 16)
			{
				String split[] = command.substring(16).split(" ");
				if (split.length < 2)
				{
					sendWindow(activeChar, 0);
					return;
				}
				sendAchievementWindow(activeChar, split[1]);
				sendWindow(activeChar, Integer.parseInt(split[0]));
			}
		}
		else if (command.startsWith("_bbsachtaketitle"))
		{
			if (command.length() > 17)
			{
				String split[] = command.substring(17).split(" ");
				if (split.length < 2)
				{
					sendWindow(activeChar, 0);
					return;
				}
				if (activeChar.getAchievementPlayer().getAchievementTitleChanged() > System.currentTimeMillis())
				{
					sendWindow(activeChar, Integer.parseInt(split[0]));
					return;
				}
				Achievement a = AchievementsParser.getInstance().getAchievement(split[1]);
				if (a == null)
				{
					sendWindow(activeChar, Integer.parseInt(split[0]));
					return;
				}
				if (!a.giveTitle())
				{
					sendWindow(activeChar, Integer.parseInt(split[0]));
					return;
				}
				activeChar.setTitle(a.getTitle());
				activeChar.getAppearance().setTitleColor(a.getTitleColor());
				activeChar.broadcastTitleInfo();
				activeChar.getAchievementPlayer().setTitle(a);
				activeChar.sendMessage("You have successfully claimed [" + a.getTitle() + "]!");
				sendWindow(activeChar, Integer.parseInt(split[0]));
			}
		}
	}
	
	public void sendWindow(L2PcInstance activeChar, int category)
	{
		String path = "data/html/cboard/Achievements/Menu.htm";
		String text = HtmCache.getInstance().getHtm(activeChar, path);
		if (text == null)
		{
			System.out.println("Could not load [" + path + "] file!");
			return;
		}
		String categories = "";
		categories += "<table width=\"" + (AchievementsParser.getInstance().getCategories().size() * 74) + "\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\">";
		categories += "<tr>";
		for (Entry<Integer, AchievementCategory> entry : AchievementsParser.getInstance().getCategories().entrySet())
		{
			// categories += "<button value=\"" + entry.getValue().getName() + "\" action=\"bypass _bbsachcategory " + entry.getKey() + "\" width=\"74\" height=\"23\" fore=\"L2UI_CH3.petinterface_tab" + (category == entry.getKey() ? "1" : "2") + "\" back=\"L2UI_CH3.petinterface_tab" + (category ==
			// entry.getKey() ? "1" : "2") + "\" />";
			if (category == entry.getKey())
			{
				categories += "<td align=center><img src=L2UI.SquareBlank width=1 height=2/><table background=\"L2UI_CT1.Tab_DF_Tab_Selected\" width=74 height=21><tr><td width=76 align=center><font color=e6dcbe>" + entry.getValue().getName() + "</font></td></tr></table></td>";
			}
			else
			{
				categories += "<td><button value=\"" + entry.getValue().getName() + "\" action=\"bypass _bbsachcategory " + entry.getKey() + "\" fore=\"L2UI_CT1.Tab_DF_Tab_Unselected\" back=\"L2UI_CT1.Tab_DF_Tab_Unselected_Over\" width=\"74\" height=\"23\"/></td>";
			}
		}
		categories += "</tr>";
		categories += "</table>";
		if (text.contains("%%categories%%") && categories != null)
			text = text.replaceAll("%%categories%%", categories);
		String achievements = "";
		AchievementCategory ac = AchievementsParser.getInstance().getCategory(category);
		long nextTitleChange = activeChar.getAchievementPlayer().getAchievementTitleChanged();
		achievements += "<img src=\"L2UI.SquareGray\" width=\"496\" height=\"2\"/>";
		int i = 0;
		for (Entry<Integer, Achievement> entry : ac.getAchievements().entrySet())
		{
			boolean finished = activeChar.getAchievementPlayer().isAchievementFinished(entry.getValue().getType());
			int level = activeChar.getAchievementPlayer().getAchievementLevel(entry.getValue().getType());
			long count = activeChar.getAchievementPlayer().getAchievementCount(entry.getValue().getType());
			int rCount = entry.getValue().getRequiredValue(level + 1);
			Achievement a = entry.getValue();
			boolean isVisible = a.isVisible(activeChar);
			if (!isVisible && (count < 1))
			{
				continue;
			}
			AchievementLevel al = a.getLevel(level + 1);
			boolean isVisibleLevel = false;
			boolean isExtra = false;
			if (al != null)
			{
				isVisibleLevel = al.isVisible(activeChar);
				isExtra = al.isExtra();
			}
			achievements += "<table bgcolor=\"" + ((i % 2) == 0 ? "050505" : "111111") + "\" width=\"500\">";
			achievements += "<tr>";
			achievements += "<td width=\"10\"></td>";
			achievements += "<td width=\"40\">";
			achievements += "<table>";
			achievements += "<tr>";
			achievements += "<td width=\"40\">";
			achievements += "<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"" + a.getIcon() + "\">";
			achievements += "<tr>";
			achievements += "<td width=32 height=32 align=center valign=top>";
			achievements += "<button value=. action=\"bypass _bbsachievement " + category + " " + a.getType() + "\" width=34 height=34 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\"/>";
			achievements += "</td>";
			achievements += "</tr>";
			achievements += "</table>";
			achievements += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"3\"/>";
			achievements += "</td>";
			achievements += "</tr>";
			achievements += "<tr>";
			achievements += "<td width=\"40\">";
			
			achievements += getProgressBar(32, ((finished && isExtra && !isVisibleLevel && (count < 1)) || (al == null) ? rCount : (int) count), rCount, 6, ac.getProgressIconsSmall());
			
			achievements += "</td>";
			achievements += "</tr>";
			achievements += "</table>";
			achievements += "</td>";
			achievements += "<td width=\"360\">";
			achievements += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>";
			achievements += "<table>";
			achievements += "<tr>";
			achievements += "<td width=\"360\">";
			String locked = (!isVisibleLevel && ((count > 0) || !isExtra) && (al != null)) || (!isVisible && (count > 0)) ? " <font color=\"808080\" name=\"CreditTextNormal\">[locked]</font>" : "";
			String extra = "";
			if ((isExtra && ((count > 0) || isVisibleLevel)) || (level > a.getLevelsCount()))
			{
				extra += "<font color=\"ff6633\">+[" + ((level + (isExtra && ((count > 0) || isVisibleLevel) ? 1 : 0)) - a.getLevelsCount()) + "]</font>";
			}
			achievements += "<font color=\"FF7E00\" name=\"CreditTextNormal\"><a action=\"bypass _bbsachievement " + category + " " + a.getType() + "\">" + a.getName() + "</a></font> <font color=\"E52B50\" name=\"CreditTextNormal\">[" + Math.min(level, a.getLevelsCount()) + extra + " / " + a.getLevelsCount() + "]</font>" + locked;
			achievements += "</td>";
			achievements += "</tr>";
			achievements += "<tr>";
			achievements += "<td width=\"360\">";
			if ((isExtra && (isVisibleLevel || (count > 0))) || ((al != null) && (isVisibleLevel || !isExtra)))
			{
				String shortDesc = "";
				if ((al != null) && (al.getShortDesc() != null))
				{
					shortDesc = al.getShortDesc().replaceAll("%value%", a.getRequiredValue(level + 1) + "").replaceAll("%valueTime%", getValueTime(a.getRequiredValue(level + 1)));
				}
				else
				{
					shortDesc = a.getShortDesc().replaceAll("%value%", a.getRequiredValue(level + 1) + "").replaceAll("%valueTime%", getValueTime(a.getRequiredValue(level + 1)));
				}
				achievements += "<font color=\"D2B48C\" name=\"CreditTextNormal\">" + shortDesc + "</font>";
			}
			else
			{
				achievements += "<font color=\"D2B48C\" name=\"CreditTextNormal\">Achievement accomplished!</font>";
			}
			achievements += "</td>";
			achievements += "</tr>";
			achievements += "</table>";
			achievements += "</td>";
			achievements += "<td width=\"80\">";
			achievements += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"13\" />";
			if (a.giveTitle())
			{
				if (finished && (nextTitleChange < System.currentTimeMillis()))
				{
					achievements += "<button value=\"Get Title\" action=\"bypass _bbsachtaketitle " + category + " " + a.getType() + "\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\" width=\"75\" height=\"21\" />";
				}
				else
				{
					achievements += "<table background=\"L2UI_CT1.Button_DF_Disable\" width=75 height=21><tr><td width=77 align=center><font color=e6dcbe>Get Title</font></td></tr></table>";
					// achievements += "<button value=\"Get Title\" action=\"\" fore=\"L2UI_CH3.Btn1_normalDisable\" back=\"L2UI_CH3.Btn1_normalDisable\" width=\"75\" height=\"21\" />";
				}
			}
			achievements += "</td>";
			achievements += "<td width=\"10\"></td>";
			achievements += "</tr>";
			achievements += "<tr><td><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"10\" /></td></tr>";
			achievements += "</table>";
			if (i < (ac.getAchievements().size() - 1))
			{
				achievements += "<img src=\"L2UI.SquareGray\" width=\"496\" height=\"1\"/>";
			}
			i++;
		}
		achievements += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"10\"/>";
		text = text.replaceAll("%%achievements%%", achievements);
		String title = "<font color=\"D2B48C\" name=\"CreditTextNormal\">Currently you have no title taken!</font>";
		if (activeChar.getAchievementPlayer().hasTitle())
		{
			title = "<font color=\"D2B48C\" name=\"CreditTextNormal\">You have currently taken [</font><font color=\"" + activeChar.getAchievementPlayer().getTitleAchievement().getTitleColorHTML() + "\" name=\"CreditTextNormal\">" + activeChar.getAchievementPlayer().getTitleAchievement().getTitle() + "</font><font color=\"D2B48C\" name=\"CreditTextNormal\">] title!</font>";
		}
		text = text.replaceAll("%title%", title);
		String titleTimer = "";
		if (activeChar.getAchievementPlayer().getAchievementTitleChanged() > System.currentTimeMillis())
		{
			titleTimer = "<font color=\"FF7E00\" name=\"CreditTextNormal\">" + getValueTime((int) ((activeChar.getAchievementPlayer().getAchievementTitleChanged() - System.currentTimeMillis()) / 1000)) + "</font> <font color=\"D2B48C\" name=\"CreditTextNormal\">left until title change unlocks.</font>";
		}
		text = text.replaceAll("%titleTimer%", titleTimer);
		separateAndSend(text, activeChar);
	}
	
	public void sendAchievementWindow(L2PcInstance activeChar, String achievement)
	{
		Achievement a = AchievementsParser.getInstance().getAchievement(achievement);
		if (a == null)
		{
			return;
		}
		AchievementCategory ac = AchievementsParser.getInstance().getCategory(a.getCategoryId());
		int level = activeChar.getAchievementPlayer().getAchievementLevel(achievement);
		long currentCount = activeChar.getAchievementPlayer().getAchievementCount(achievement);
		int rCount = a.getRequiredValue(level + 1);
		boolean isFinished = activeChar.getAchievementPlayer().isAchievementFinished(achievement);
		boolean isVisible = a.isVisible(activeChar);
		AchievementLevel al = a.getLevel(level + 1);
		boolean isVisibleLevel = false;
		boolean isExtra = false;
		if (al != null)
		{
			isVisibleLevel = al.isVisible(activeChar);
			isExtra = al.isExtra();
		}
		String path = "data/html/cboard/Achievements/Achievement.htm";
		String text = HtmCache.getInstance().getHtm(activeChar, path);
		text = text.replaceAll("%%category%%", a.getCategory());
		String extra = "";
		if ((isExtra && ((currentCount > 0) || isVisibleLevel)) || (level > a.getLevelsCount()))
		{
			extra += "<font color=\"ff6633\" name=\"CreditTextNormal\">+[" + ((level + (isExtra && ((currentCount > 0) || isVisibleLevel) ? 1 : 0)) - a.getLevelsCount()) + "]</font>";
		}
		String locked = (!isVisibleLevel && ((currentCount > 0) || !isExtra) && (al != null)) || (!isVisible && (currentCount > 0)) ? " <font color=\"808080\" name=\"CreditTextNormal\">[locked]</font>" : "";
		
		text = text.replaceAll("%%name%%", "<font color=\"FF7E00\" name=\"CreditTextNormal\">" + a.getName() + "</font> <font color=\"E52B50\" name=\"CreditTextNormal\">[" + Math.min(level, a.getLevelsCount()) + extra + " / " + a.getLevelsCount() + "]</font>" + locked);
		text = text.replaceAll("%%icon%%", a.getIcon());
		String count = "";
		if (!isFinished || ((al != null) && (!isExtra || isVisibleLevel || (currentCount > 0))))
		{
			if (a.isWithTime())
			{
				count = "Current: " + getValueTime((int) currentCount) + "<br1>Required: " + getValueTime(rCount) + "";
			}
			else
			{
				count = "[" + currentCount + "/" + rCount + "]";
			}
		}
		else
		{
			count = "Accomplished";
		}
		
		text = text.replaceAll("%%count%%", count);
		
		if ((isExtra && (isVisibleLevel || (currentCount > 0))) || ((al != null) && (isVisibleLevel || !isExtra)))
		{
			String longDesc = "";
			if ((al != null) && (al.getLongDesc() != null))
			{
				longDesc = al.getLongDesc().replaceAll("'br'", "<br1>").replaceAll("%value%", a.getRequiredValue(level + 1) + "").replaceAll("%valueTime%", getValueTime(a.getRequiredValue(level + 1)));
			}
			else
			{
				longDesc = a.getLongDesc().replaceAll("'br'", "<br1>").replaceAll("%value%", a.getRequiredValue(level + 1) + "").replaceAll("%valueTime%", getValueTime(a.getRequiredValue(level + 1)));
			}
			text = text.replaceAll("%%longDesc%%", "" + longDesc + "");
		}
		else
		{
			text = text.replaceAll("%%longDesc%%", "Achievement accomplished!");
		}
		text = text.replaceAll("%%progress%%", getProgressBar(160, ((isFinished && isExtra && !isVisibleLevel && (currentCount < 1)) || (al == null) ? rCount : (int) currentCount), rCount, 12, ac.getProgressIcons()));
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(text);
		activeChar.sendPacket(html);
	}
	
	public static String getValueTime(int time)
	{
		String t = "";
		int h = time / 3600;
		int min = (time % 3600) / 60;
		int sec = time % 60;
		if (h < 10)
		{
			t = "0" + h + ":";
		}
		else
		{
			t = h + ":";
		}
		
		if (min < 10)
		{
			t += "0" + min + ":";
		}
		else
		{
			t += min + ":";
		}
		
		if (sec < 10)
		{
			t += "0" + sec;
		}
		else
		{
			t += sec;
		}
		
		return t;
	}
	
	public static String getProgressBar(int width, int step, int maxSteps, int height, String... progressIcons)
	{
		int percent = (int) ((step * 100.0) / maxSteps);
		int filledWidth = (int) ((percent * (width - 8)) / 100.0);
		
		String filledCenter = progressIcons[1];
		String unfilledCenter = progressIcons[4];
		
		String text = "<table cellspacing=0 cellpadding=0>";
		text += "<tr>";
		
		if (percent == 0)
		{
			text += "<td>";
			text += "<img src=\"" + progressIcons[3] + "\" width=\"4\" height=\"" + height + "\"/>";
			text += "</td>";
			text += "<td>";
			text += "<img src=\"" + unfilledCenter + "\" width=\"" + (width - 8) + "\" height=\"" + height + "\"/>";
			text += "</td>";
			text += "<td>";
			text += "<img src=\"" + progressIcons[5] + "\" width=\"4\" height=\"" + height + "\"/>";
			text += "</td>";
		}
		else if (percent == 100)
		{
			text += "<td>";
			text += "<img src=\"" + progressIcons[0] + "\" width=\"4\" height=\"" + height + "\"/>";
			text += "</td>";
			text += "<td>";
			text += "<img src=\"" + filledCenter + "\" width=\"" + (width - 8) + "\" height=\"" + height + "\"/>";
			text += "</td>";
			text += "<td>";
			text += "<img src=\"" + progressIcons[2] + "\" width=\"4\" height=\"" + height + "\"/>";
			text += "</td>";
		}
		else
		{
			text += "<td>";
			text += "<img src=\"" + progressIcons[0] + "\" width=\"4\" height=\"" + height + "\"/>";
			text += "</td>";
			text += "<td>";
			text += "<img src=\"" + filledCenter + "\" width=\"" + filledWidth + "\" height=\"" + height + "\"/>";
			text += "</td>";
			text += "<td>";
			text += "<img src=\"" + unfilledCenter + "\" width=\"" + ((width - 8) - filledWidth) + "\" height=\"" + height + "\"/>";
			text += "</td>";
			text += "<td>";
			text += "<img src=\"" + progressIcons[5] + "\" width=\"4\" height=\"" + height + "\"/>";
			text += "</td>";
		}
		
		text += "</tr>";
		text += "</table>";
		/*
		 * int percent = (int) ((step * 100.0) / maxSteps); int filledWidth = (int) ((percent * width) / 100.0); String text = "<table cellspacing=0 cellpadding=0>"; text += "<tr>"; text += "<td>"; text += "<img src=\""; text += progressIcons[filledWidth >= 2 ? 0 : 3]; text +=
		 * "\" width=\"4\" height=\"" + height + "\"/>"; text += "</td>"; if ((filledWidth >= (width - 8)) || (filledWidth < 4)) { text += "<td>"; text += "<img src=\""; text += progressIcons[filledWidth < 4 ? 4 : 1]; text += "\" width=\"" + (width - 8) + "\" height=\"" + height + "\"/>"; text +=
		 * "</td>"; } else { text += "<td>"; text += "<img src=\""; text += progressIcons[1]; text += "\" width=\"" + filledWidth + "\" height=\"" + height + "\"/>"; text += "</td>"; text += "<td>"; text += "<img src=\""; text += progressIcons[4]; text += "\" width=\"" + (width - 8 - filledWidth) +
		 * "\" height=\"" + height + "\"/>"; text += "</td>"; } text += "<td>"; text += "<img src=\""; text += progressIcons[filledWidth >= (width - 2) ? 2 : 5]; text += "\" width=\"4\" height=\"" + height + "\"/>"; text += "</td>"; text += "</tr>"; text += "</table>";
		 */
		
		return text;
	}
	
	private static class SingletonHolder
	{
		protected static final AchievementBBSManager _instance = new AchievementBBSManager();
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	
	}
}
