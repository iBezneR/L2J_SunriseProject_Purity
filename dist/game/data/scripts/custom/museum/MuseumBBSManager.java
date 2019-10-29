package custom.museum;

/*
 * Copyright (C) 2004-2014 L2J Server
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

import custom.museum.MuseumManager.RefreshTime;
import l2r.gameserver.communitybbs.Managers.BaseBBSManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.text.NumberFormat;
import java.util.*;

public class MuseumBBSManager extends BaseBBSManager
{
	public static String MUSEUM_BBS_CMD = "_bbsmuseum";
	
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		String html = "<html><body><img src=L2UI.SquareBlank width=1 height=6/>";
		command = command.substring(command.length() > (MUSEUM_BBS_CMD.length()) ? MUSEUM_BBS_CMD.length() + 1 : MUSEUM_BBS_CMD.length());
		StringTokenizer st = new StringTokenizer(command, ";");
		String cmd = "main";
		int type = 0;
		int postType = 0;
		if (st.hasMoreTokens())
		{
			cmd = st.nextToken();
		}
		if (st.hasMoreTokens())
		{
			type = Integer.parseInt(st.nextToken());
		}
		if (st.hasMoreTokens())
		{
			postType = Integer.parseInt(st.nextToken());
		}
		if (cmd.startsWith("main"))
		{
			html += showTops(type, postType);
		}
		else if (cmd.startsWith("personal"))
		{
			html += showPlayerTops(activeChar, type);
		}
		html += "</body></html>";
		separateAndSend(html, activeChar);
	}
	
	public String showTops(int type, int postType)
	{
		MuseumCategory cat = MuseumManager.getInstance().getAllCategories().get((type * 256) + postType);
		String html = "";
		if (MuseumManager.WHOLE_COMMUNITY_BOARD || !MuseumManager.SEPARATED_TABS)
		{
			html += "<table cellspacing=-4><tr>";
			html += "<td width=12></td>";
			html += "<td align=center><img src=L2UI.SquareBlank width=1 height=7/><table background=\"L2UI_CT1.Tab_DF_Tab_Selected\" width=135 height=21><tr><td width=137 align=center><font color=e6dcbe>View Server Records</font></td></tr></table></td>";
			html += "<td><img src=L2UI.SquareBlank width=1 height=5/><button value=\"View My Records\" action=\"bypass " + MUSEUM_BBS_CMD + ";personal\" fore=\"L2UI_CT1.Tab_DF_Tab_Unselected\" back=\"L2UI_CT1.Tab_DF_Tab_Unselected_Over\" width=\"135\" height=\"23\"/></td>";
			html += "</tr></table>";
			html += "<img src=L2UI.SquareBlank width=1 height=2/>";
		}
		else
		{
			html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>";
		}
		html += "<center>";
		html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"><tr><td width=10></td>";
		// Categories Start
		html += "<td>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>";
		html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg\"><tr>";
		html += "<td width=\"6\"></td><td>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/>";
		html += "<table cellspacing=-6>";
		
		for (Map.Entry<Integer, String> entry : MuseumManager.getInstance().getAllCategoryNames().entrySet())
		{
			ArrayList<MuseumCategory> categories = MuseumManager.getInstance().getAllCategoriesByCategoryId(entry.getKey());
			if (categories == null)
			{
				continue;
			}
			if (entry.getKey() == type)
			{
				html += "<tr><td><table background=\"L2UI_CT1.Button_DF_Disable\" width=200 height=21><tr><td width=202 align=center><font color=e6dcbe>[-] " + entry.getValue() + "</font></td></tr></table></td></tr>";
				html += "<tr><td width=200 align=center><img src=L2UI.SquareBlank width=1 height=11/>";
				for (MuseumCategory category : categories)
				{
					html += "<table width=197 bgcolor=" + ((category.getTypeId() % 2) == 0 ? "111111" : "050505") + "><tr><td width=197 align=center><a action=\"bypass " + MUSEUM_BBS_CMD + ";main;" + type + ";" + category.getTypeId() + "\"><font name=\"CreditTextNormal\" color=" + (postType == category.getTypeId() ? "FFFFFF" : "D2B48C") + ">" + category.getTypeName() + "</font></a></td></tr></table>";
				}
				html += "<img src=L2UI.SquareBlank width=1 height=10/></td></tr>";
			}
			else
			{
				html += "<tr><td><button value=\"[+] " + entry.getValue() + "\" action=\"bypass " + MUSEUM_BBS_CMD + ";main;" + entry.getKey() + "\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\" width=\"200\" height=\"21\"/>";
				html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"1\"/></td></tr>";
			}
		}
		html += "</table>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"15\"/>";
		html += "</td><td width=\"6\"></td>";
		html += "</tr></table>";
		html += "</td>";
		// Categories End
		// Basic Start
		html += "<td>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>";
		html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg\"><tr>";
		html += "<td width=\"6\"></td><td>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/>";
		html += "<table cellspacing=-6><tr>";
		if (!cat.getRefreshTime().equals(RefreshTime.Total))
		{
			html += "<td><button value=\"" + cat.getRefreshTime().name() + " Rankings\" action=\"\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF\" width=\"242\" height=\"20\"/></td><td width=12></td>";
		}
		html += "<td><button value=\"Total Rankings\" action=\"\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF\" width=\"" + (!cat.getRefreshTime().equals(RefreshTime.Total) ? 242 : 487) + "\" height=\"20\"/></td>";
		html += "</tr></table>";
		html += "<img src=L2UI.SquareBlank width=1 height=5/>";
		html += "<table cellspacing=-3><tr>";
		// First half
		html += "<td>";
		html += "<table><tr><td><img src=\"L2UI.SquareGray\" width=230 height=1/>";
		HashMap<Integer, TopPlayer> players = cat.getRefreshTime().equals(RefreshTime.Total) ? cat.getAllTotalTops() : cat.getAllTops();
		for (int i = 0; i < 10; i++)
		{
			String name = "No information.";
			String value = "No information.";
			int cellSpacing = -1;
			if (players.size() > i)
			{
				TopPlayer player = players.get(i + 1);
				if (player != null)
				{
					name = player.getName();
					long count = player.getCount();
					value = convertToValue(count, cat.isTimer(), cat.getAdditionalText());
					cellSpacing = (count > 999 ? -3 : -2);
				}
			}
			String bgColor = i == 0 ? "ffe866" : (i == 1 ? "cccccc" : (i == 2 ? "daa671" : ((i % 2) == 1 ? "050505" : "111111")));
			String numberColor = i == 0 ? "ffca37 name=ScreenMessageLarge" : (i == 1 ? "949499 name=ScreenMessageMed" : (i == 2 ? "b37a4d name=ScreenMessageSmall" : "dededf name=CreditTextNormal"));
			String nameColor = i == 0 ? "eac842" : (i == 1 ? "dbdbdb" : (i == 2 ? "d29b65" : "e2e2e0"));
			String valueColor = i == 0 ? "eee79f" : "a78d6c";
			html += "<table width=230 bgcolor=" + bgColor + " height=37><tr>";
			html += "<td width=40 align=center><font color=" + numberColor + ">" + (i + 1) + "</font></td>";
			html += "<td width=190 align=left>";
			html += "<table cellspacing=" + (cellSpacing) + "><tr><td width=190><img src=L2UI.SquareBlank width=1 height=2/><font color=" + nameColor + " name=CreditTextNormal>" + name + "</font></td></tr><tr><td width=190><font color=" + valueColor + " name=CreditTextNormal>" + value + "</font></td></tr></table>";
			html += "<img src=\"L2UI.SquareBlank\" width=1 height=5/></td>";
			html += "";
			html += "</tr></table><img src=\"L2UI.SquareGray\" width=230 height=1/>";
		}
		html += "</td></tr></table>";
		html += "</td>";
		html += "<td width=12></td>";
		// Second half
		html += "<td>";
		html += "<table><tr><td><img src=\"L2UI.SquareGray\" width=249 height=1/>";
		for (int i = 10 - (cat.getRefreshTime().equals(RefreshTime.Total) ? 0 : 10); i < (20 - (cat.getRefreshTime().equals(RefreshTime.Total) ? 0 : 10)); i++)
		{
			String name = "No information.";
			String value = "No information.";
			int cellSpacing = -1;
			if (cat.getAllTotalTops().size() > i)
			{
				TopPlayer player = cat.getAllTotalTops().get(i + 1);
				if (player != null)
				{
					name = player.getName();
					value = convertToValue(player.getCount(), cat.isTimer(), cat.getAdditionalText());
					cellSpacing = (player.getCount() > 999 ? -3 : -2);
				}
			}
			
			String bgColor = i == 0 ? "ffe866" : (i == 1 ? "cccccc" : (i == 2 ? "daa671" : ((i % 2) == 1 ? "050505" : "111111")));
			String numberColor = i == 0 ? "ffca37 name=ScreenMessageLarge" : (i == 1 ? "949499 name=ScreenMessageMed" : (i == 2 ? "b37a4d name=ScreenMessageSmall" : "dededf name=CreditTextNormal"));
			String nameColor = i == 0 ? "eac842" : (i == 1 ? "dbdbdb" : (i == 2 ? "d29b65" : "e2e2e0"));
			String valueColor = i == 0 ? "eee79f" : "a78d6c";
			html += "<table width=230 bgcolor=" + bgColor + " height=37><tr>";
			html += "<td width=30 align=center><font color=" + numberColor + ">" + (i + 1) + "</font></td>";
			html += "<td width=190 align=left>";
			html += "<table cellspacing=" + (cellSpacing) + "><tr><td width=190><img src=L2UI.SquareBlank width=1 height=2/><font color=" + nameColor + " name=CreditTextNormal>" + name + "</font></td></tr><tr><td width=190><font color=" + valueColor + " name=CreditTextNormal>" + value + "</font></td></tr></table>";
			html += "<img src=\"L2UI.SquareBlank\" width=1 height=5/></td>";
			html += "";
			html += "</tr></table><img src=\"L2UI.SquareGray\" width=230 height=1/>";
		}
		html += "</td></tr></table>";
		html += "</td></tr></table>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"23\"/>";
		html += "</td><td width=\"6\"></td>";
		html += "</tr></table>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"10\"/>";
		html += "</td><td width=\"6\"></td>";
		// Basic End
		html += "</tr></table>";
		return html;
	}
	
	public String showPlayerTops(L2PcInstance player, int type)
	{
		String dailyType[] =
		{
			"Monthly",
			"Weekly",
			"Daily"
		};
		String html = "";
		if (MuseumManager.WHOLE_COMMUNITY_BOARD || !MuseumManager.SEPARATED_TABS)
		{
			html += "<table cellspacing=-4><tr>";
			html += "<td width=12></td>";
			html += "<td><img src=L2UI.SquareBlank width=1 height=5/><button value=\"View Server Records\" action=\"bypass " + MUSEUM_BBS_CMD + ";main\" fore=\"L2UI_CT1.Tab_DF_Tab_Unselected\" back=\"L2UI_CT1.Tab_DF_Tab_Unselected_Over\" width=\"135\" height=\"23\"/></td>";
			html += "<td align=center><img src=L2UI.SquareBlank width=1 height=7/><table background=\"L2UI_CT1.Tab_DF_Tab_Selected\" width=135 height=21><tr><td width=137 align=center><font color=e6dcbe>View My Records</font></td></tr></table></td>";
			html += "</tr></table>";
			html += "<img src=L2UI.SquareBlank width=1 height=2/>";
		}
		else
		{
			html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>";
		}
		html += "<center>";
		html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"><tr><td width=10></td>";
		
		// Categories Start
		html += "<td>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/>";
		html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg\"><tr>";
		html += "<td width=\"6\"></td><td>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"13\"/>";
		html += "<table cellspacing=-6><tr>";
		for (Map.Entry<Integer, String> entry : MuseumManager.getInstance().getAllCategoryNames().entrySet())
		{
			ArrayList<MuseumCategory> categories = MuseumManager.getInstance().getAllCategoriesByCategoryId(entry.getKey());
			if (categories == null)
			{
				continue;
			}
			if (entry.getKey() == type)
			{
				html += "<tr><td><table background=\"L2UI_CT1.Button_DF_Disable\" width=200 height=21><tr><td width=202 align=center><font color=e6dcbe>[-] " + entry.getValue() + "</font></td></tr></table>";
				html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/></td></tr>";
			}
			else
			{
				html += "<tr><td><button value=\"[+] " + entry.getValue() + "\" action=\"bypass " + MUSEUM_BBS_CMD + ";personal;" + entry.getKey() + "\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\" width=\"200\" height=\"21\"/>";
				html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"1\"/></td></tr>";
			}
		}
		html += "</table>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"15\"/>";
		html += "</td><td width=\"6\"></td>";
		html += "</tr></table>";
		html += "</td>";
		// Categories End
		// Basic Start
		html += "<td>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"7\"/>";
		html += "<table background=\"L2UI_CT1.Windows_DF_Drawer_Bg\"><tr>";
		html += "<td width=\"6\"></td><td>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"13\"/>";
		html += "<table cellspacing=-6><tr>";
		ArrayList<MuseumCategory> categories = MuseumManager.getInstance().getAllCategoriesByCategoryId(type);
		String typeHtml1[] =
		{
			"",
			"",
			"",
			""
		};
		String typeHtml2[] =
		{
			"",
			"",
			"",
			""
		};
		String typeHtml3[] =
		{
			"",
			"",
			"",
			""
		};
		int c[] =
		{
			0,
			0,
			0,
			0
		};
		for (MuseumCategory cat : categories)
		{
			int h = cat.getRefreshTime().ordinal();
			if (typeHtml1[h].equals(""))
			{
				
				typeHtml1[h] += "<table cellspacing=-5><tr>";
				typeHtml1[h] += "<td width=10></td>";
				typeHtml1[h] += "<td><button value=\"Item\" action=\"\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF\" width=\"" + (h > 0 ? 212 : 300) + "\" height=\"24\"/></td>";
				typeHtml1[h] += "<td width=10></td>";
				if (h > 0)
				{
					typeHtml1[h] += "<td><button value=\"" + dailyType[h - 1] + " Total\" action=\"\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF\" width=\"131\" height=\"24\"/></td>";
					typeHtml1[h] += "<td width=10></td>";
				}
				typeHtml1[h] += "<td><button value=\"Total\" action=\"\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF\" width=\"" + (h > 0 ? 131 : 176) + "\" height=\"24\"/></td>";
				typeHtml1[h] += "</tr></table>";
				typeHtml1[h] += "<table><tr>";
				typeHtml1[h] += "<td>";
				typeHtml1[h] += "<img src=L2UI.SquareGray width=475 height=1 />";
			}
			if (typeHtml3[h].equals(""))
			{
				typeHtml3[h] += "</td>";
				typeHtml3[h] += "</tr>";
				typeHtml3[h] += "</table>";
				if (h < 3)
				{
					typeHtml3[h] += "<br><br>";
				}
			}
			typeHtml2[h] += "<table bgcolor=" + ((c[h] % 2) == 0 ? "050505" : "111111") + "><tr><td width=" + (h > 0 ? 209 : 300) + " align=center><font name=\"CreditTextNormal\" color=\"D2B48C\">" + cat.getTypeName() + "</font></td>";
			MuseumPlayer mp = null;
			try
			{
				mp = (MuseumPlayer) player.getClass().getMethod("getMuseumPlayer").invoke(player);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			long[] d = mp == null ? null : mp.getData(cat.getType());
			if (d == null)
			{
				d = new long[]
				{
					0,
					0,
					0,
					0
				};
			}
			String value = "";
			value = convertToValue(d[h], cat.isTimer(), cat.getAdditionalText());
			String totalValue = "";
			if (h > 0)
			{
				totalValue = convertToValue(d[0], cat.isTimer(), cat.getAdditionalText());
			}
			if (h > 0)
			{
				typeHtml2[h] += "<td width=129 align=center><font name=\"CreditTextNormal\" color=\"D2B48C\">" + value + "</font></td>";
			}
			typeHtml2[h] += "<td width=" + (h > 0 ? 129 : 169) + " align=center><font name=\"CreditTextNormal\" color=\"D2B48C\">" + (h > 0 ? totalValue : value) + "</font></td>";
			typeHtml2[h] += "</tr></table>";
			typeHtml2[h] += "<img src=L2UI.SquareGray width=475 height=1 />";
			c[h]++;
			
		}
		for (int i = 0; i < 4; i++)
		{
			html += typeHtml1[i];
			html += typeHtml2[i];
			html += typeHtml3[i];
		}
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"23\"/>";
		html += "</td><td width=\"6\"></td>";
		html += "</tr></table>";
		html += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"13\"/>";
		html += "</td><td width=\"6\"></td>";
		// Basic End
		html += "</tr></table>";
		return html;
	}
	
	public String convertToValue(long count, boolean isTimer, String additionalText)
	{
		String value = "";
		if (!isTimer)
		{
			value = NumberFormat.getNumberInstance(Locale.US).format(count);
			value += " " + additionalText;
		}
		else
		{
			long days = count / 86400;
			long hours = (count % 86400) / 3600;
			long mins = (count % 3600) / 60;
			value = "";
			if (days > 0)
			{
				value += days + " day(s) ";
			}
			value += hours + " hour(s) ";
			if ((mins > 0) && (days < 1))
			{
				value += mins + " min(s) ";
			}
			if ((days < 1) && (hours < 1) && (mins < 1))
			{
				value = "0 min(s) " + count + " sec(s)";
			}
		}
		return value;
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	/**
	 * Gets the single instance of RegionBBSManager.
	 * @return single instance of RegionBBSManager
	 */
	public static MuseumBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MuseumBBSManager _instance = new MuseumBBSManager();
	}
}