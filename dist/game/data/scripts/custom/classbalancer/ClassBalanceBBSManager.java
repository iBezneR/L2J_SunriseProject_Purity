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
package custom.classbalancer;

import l2r.gameserver.communitybbs.Managers.BaseBBSManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ClassBalanceBBSManager extends BaseBBSManager
{
	public static String CLASS_BALANCE_BBS_CMD = "_bbsbalancer";
	
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		if (!activeChar.isGM())
		{
			return;
		}
		ClassBalanceManager.getInstance().loadSecondProffessions();
		if (command.equals("admin_classbalancer"))
		{
			command = CLASS_BALANCE_BBS_CMD + ";main";
		}
		String html = "<html><body><br><br>";
		command = command.substring(command.length() > (CLASS_BALANCE_BBS_CMD.length()) ? CLASS_BALANCE_BBS_CMD.length() + 1 : CLASS_BALANCE_BBS_CMD.length());
		if (command.startsWith("main"))
		{
			int classId = -1;
			int targetClassId = -1;
			boolean forOlympiad = false;
			if (command.length() > 4)
			{
				StringTokenizer st = new StringTokenizer(command.substring(5), ";");
				if (st.hasMoreTokens())
				{
					forOlympiad = st.nextToken().equals("1");
				}
				if (st.hasMoreTokens())
				{
					classId = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens())
				{
					targetClassId = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens())
				{
					classId = Integer.parseInt(st.nextToken());
				}
			}
			html += showMainPage(classId, targetClassId, forOlympiad);
		}
		else if (command.startsWith("show"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(5), ";");
			boolean forOlympiad = st.nextToken().equals("1");
			int page = Integer.parseInt(st.nextToken());
			int classId = Integer.parseInt(st.nextToken());
			int targetClassId = Integer.parseInt(st.nextToken());
			html += showPage(page, classId, targetClassId, forOlympiad);
		}
		else if (command.startsWith("increase"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(9), ";");
			boolean forOlympiad = st.nextToken().equals("1");
			int page = Integer.parseInt(st.nextToken());
			int classId = Integer.parseInt(st.nextToken());
			int targetClassId = Integer.parseInt(st.nextToken());
			int cId = Integer.parseInt(st.nextToken());
			int tcId = Integer.parseInt(st.nextToken());
			int key = Integer.parseInt(st.nextToken());
			int type = Integer.parseInt(st.nextToken());
			double value = Double.parseDouble(st.nextToken());
			ClassBalanceManager.getInstance().updateBalance(key, cId, tcId, type, value, forOlympiad);
			html += showPage(page, classId, targetClassId, forOlympiad);
		}
		else if (command.startsWith("delete"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(7), ";");
			boolean forOlympiad = st.nextToken().equals("1");
			int page = Integer.parseInt(st.nextToken());
			int classId = Integer.parseInt(st.nextToken());
			int targetClassId = Integer.parseInt(st.nextToken());
			int cId = Integer.parseInt(st.nextToken());
			int tcId = Integer.parseInt(st.nextToken());
			int key = Integer.parseInt(st.nextToken());
			ClassBalanceManager.getInstance().removeBalance(key, cId, tcId, forOlympiad);
			html += showPage(page, classId, targetClassId, forOlympiad);
		}
		else if (command.startsWith("addpage"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(8), ";");
			boolean forOlympiad = st.nextToken().equals("1");
			int classId = Integer.parseInt(st.nextToken());
			int targetClassId = Integer.parseInt(st.nextToken());
			int race = 0, tRace = 0;
			if (st.hasMoreTokens())
			{
				race = Integer.parseInt(st.nextToken());
			}
			if (st.hasMoreTokens())
			{
				tRace = Integer.parseInt(st.nextToken());
			}
			html += showAddPage(classId, targetClassId, race, tRace, forOlympiad);
		}
		else if (command.startsWith("add"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(4), ";");
			boolean forOlympiad = st.nextToken().equals("1");
			String className = st.nextToken();
			if (className.startsWith(" "))
			{
				className = className.substring(1);
			}
			if (className.endsWith(" "))
			{
				className = className.substring(0, className.length() - 1);
			}
			className = className.replaceAll(" ", "");
			String targetClassName = st.nextToken();
			if (targetClassName.startsWith(" "))
			{
				targetClassName = targetClassName.substring(1);
			}
			if (targetClassName.endsWith(" "))
			{
				targetClassName = targetClassName.substring(0, targetClassName.length() - 1);
			}
			targetClassName = targetClassName.replaceAll(" ", "");
			int classId = -1;
			int targetClassId = -1;
			if (!className.equals(""))
			{
				for (ClassId cId : ClassId.values())
				{
					if (cId.name().equalsIgnoreCase(className))
					{
						classId = cId.getId();
					}
				}
			}
			if (!targetClassName.equals(""))
			{
				for (ClassId cId : ClassId.values())
				{
					if (cId.name().equalsIgnoreCase(targetClassName))
					{
						targetClassId = cId.getId();
					}
				}
			}
			double values[] =
			{
				1,
				1,
				1,
				1,
				1,
				1,
				1,
				classId,
				targetClassId
			};
			int key = ((classId * 256) * (targetClassId == -1 ? -1 : 1)) + (targetClassId == -1 ? 0 : targetClassId);
			ClassBalanceManager.getInstance().updateBalance(key, classId, targetClassId, values, forOlympiad);
			html += showPage(1, classId, targetClassId, forOlympiad);
		}
		html += "</body></html>";
		separateAndSend(html, activeChar);
	}
	
	public String showMainPage(int classId, int targetClassId, boolean forOlympiad)
	{
		String html = "<center>";
		html += "<font name=\"ScreenMessageSmall\">Used classes in balancer</font><br>";
		html += "<table width=220><tr><td width=20 align=center><img width=1 height=3 src=\"L2UI.SquareBlank\"/><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";main;" + (forOlympiad ? 0 : 1) + ";" + classId + ";" + targetClassId + "\" width=\"14\" height=\"14\" back=\"L2UI.CheckBox" + (forOlympiad ? "" : "_checked") + "\" fore=\"L2UI.CheckBox" + (forOlympiad ? "_checked" : "") + "\"/></td><td width=200 align=left><font color=BABABA name=\"CreditTextNormal\">Show for Olympiad</font></td></tr></table>";
		html += "<table width=600 cellspacing=0 cellpadding=0><tr>";
		int i = 0;
		for (ClassId cl : ClassId.values())
		{
			if (cl.level() < 3)
			{
				continue;
			}
			if ((i % 12) == 0)
			{
				html += "<td width=200>";
			}
			html += "<table width=200 align=center bgcolor=" + ((i % 2) == 0 ? "050505" : "111111") + ">";
			String name = cl.name();
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
			int cId = classId;
			int tcId = targetClassId;
			if ((cId == -1) && (tcId == -1))
			{
				cId = cl.getId();
			}
			else if ((cId == -1) && (tcId != -1))
			{
				if (tcId == cl.getId())
				{
					tcId = -1;
				}
				else
				{
					cId = cl.getId();
				}
			}
			else if ((cId != -1) && (tcId == -1))
			{
				if (cId == cl.getId())
				{
					cId = -1;
				}
				else
				{
					tcId = cl.getId();
				}
			}
			else
			{
				if (tcId == cl.getId())
				{
					tcId = -1;
				}
				else if (cId == cl.getId())
				{
					cId = -1;
				}
				else
				{
					cId = tcId;
					tcId = cl.getId();
				}
			}
			html += "<tr><td width=20 align=center><img width=1 height=3 src=\"L2UI.SquareBlank\"/><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";main;" + (forOlympiad ? 1 : 0) + ";" + cId + ";" + tcId + "\" width=\"14\" height=\"14\" back=\"L2UI.CheckBox" + ((cl.getId() == classId) || (cl.getId() == targetClassId) ? "" : "_checked") + "\" fore=\"L2UI.CheckBox" + ((cl.getId() == classId) || (cl.getId() == targetClassId) ? "_checked" : "") + "\"/></td><td width=200><font color=BABABA name=\"CreditTextNormal\">" + name + "</font></td></tr>";
			html += "</table>";
			if ((i % 12) == 11)
			{
				html += "</td>";
			}
			i++;
		}
		if (!html.endsWith("</td>"))
		{
			html += "</td>";
		}
		html += "</tr></table><br>";
		int count = 0;
		if ((classId != -1) && (targetClassId != -1))
		{
			if (ClassBalanceManager.getInstance().getAllBalances(forOlympiad).containsKey((classId * 256) + targetClassId))
			{
				count++;
			}
			if (ClassBalanceManager.getInstance().getAllBalances(forOlympiad).containsKey((targetClassId * 256) + classId))
			{
				count++;
			}
		}
		else if ((classId == -1) && (targetClassId != -1))
		{
			if (ClassBalanceManager.getInstance().getAllBalancesForIngame(forOlympiad).containsKey(targetClassId))
			{
				count += ClassBalanceManager.getInstance().getAllBalancesForIngame(forOlympiad).get(targetClassId).size();
			}
		}
		else if ((classId != -1) && (targetClassId == -1))
		{
			if (ClassBalanceManager.getInstance().getAllBalancesForIngame(forOlympiad).containsKey(classId))
			{
				count += ClassBalanceManager.getInstance().getAllBalancesForIngame(forOlympiad).get(classId).size();
			}
		}
		html += "<table bgcolor=000000><tr>";
		html += "<td width=220><font color=CBCBCB name=\"CreditTextNormal\">Available " + count + " balances:</font></td>";
		html += "<td><img src=\"L2UI.SquareBlank\" width=1 height=5/><button value=\"Show\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";show;" + (forOlympiad ? 1 : 0) + ";1;" + classId + ";" + targetClassId + "\" width=\"74\" height=\"22\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" ></td>";
		html += "<td width=200></td>";
		html += "<td><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;0;" + classId + ";" + targetClassId + "\" width=\"32\" height=\"32\" back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue_Down\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Blue\" ></td>";
		html += "</tr></table>";
		html += "</center>";
		return html;
	}
	
	public String showPage(int page, int classId, int targetClassId, boolean forOlympiad)
	{
		String html = "<center>";
		HashMap<Integer, double[]> _used = new HashMap<>();
		
		if ((classId != -1) && (targetClassId != -1))
		{
			if (ClassBalanceManager.getInstance().getAllBalances(forOlympiad).containsKey((classId * 256) + targetClassId))
			{
				_used.put((classId * 256) + targetClassId, ClassBalanceManager.getInstance().getBalance((classId * 256) + targetClassId, forOlympiad));
			}
			if (ClassBalanceManager.getInstance().getAllBalances(forOlympiad).containsKey((targetClassId * 256) + classId)) // TODO: OLY
			{
				_used.put((targetClassId * 256) + classId, ClassBalanceManager.getInstance().getBalance((targetClassId * 256) + classId, forOlympiad));
			}
		}
		else
		{
			if (classId != -1)
			{
				ArrayList<Integer> data = ClassBalanceManager.getInstance().getBalanceForIngame(classId, forOlympiad);
				if ((data != null) && (data.size() > 0))
				{
					for (int cl : data)
					{
						double[] d = ClassBalanceManager.getInstance().getBalance(cl, forOlympiad);
						if (d == null)
						{
							continue;
						}
						_used.put(cl, ClassBalanceManager.getInstance().getBalance(cl, forOlympiad));
					}
				}
			}
			if (targetClassId != -1)
			{
				ArrayList<Integer> data = ClassBalanceManager.getInstance().getBalanceForIngame(targetClassId, forOlympiad);
				if ((data != null) && (data.size() > 0))
				{
					for (int cl : data)
					{
						double[] d = ClassBalanceManager.getInstance().getBalance(cl, forOlympiad);
						if (d == null)
						{
							continue;
						}
						_used.put(cl, ClassBalanceManager.getInstance().getBalance(cl, forOlympiad));
					}
				}
			}
		}
		html += "<font name=\"ScreenMessageSmall\">Class balancer</font><br>";
		html += "<table width=600 align=center bgcolor=050505>";
		html += "<tr>";
		html += "<td width=30 align=center></td>";
		html += "<td width=250><img width=1 height=5 src=\"L2UI.SquareBlank\"/><font color=2E8424 name=\"CreditTextNormal\">Class</font><font color=888888 name=\"CreditTextNormal\"> -></font> <font color=ED792C name=\"CreditTextNormal\">Target Class</font></td>";
		html += "<td width=70 align=center><font color=888888 name=\"CreditTextNormal\">N</td>";
		html += "<td width=70 align=center>C</td>";
		html += "<td width=70 align=center>M</td>";
		html += "<td width=70 align=center>MC</td>";
		html += "<td width=70 align=center>B</td>";
		html += "<td width=70 align=center>PS</td>";
		html += "<td width=70 align=center>PSC</font></td>";
		html += "</tr>";
		html += "</table>";
		html += "<img src=\"L2UI.SquareBlank\" width=1 height=1/>";
		html += "<img src=\"L2UI.SquareGray\" width=604 height=2/>";
		html += "<img src=\"L2UI.SquareBlank\" width=1 height=1/>";
		int i = 0;
		int f = 0;
		int objectsInPage = 3;
		for (Map.Entry<Integer, double[]> entry : _used.entrySet())
		{
			if ((i < ((page - 1) * objectsInPage)) || (i >= (page * objectsInPage)))
			{
				i++;
				continue;
			}
			String className = ClassId.getClassId((int) entry.getValue()[7]).name();
			className = className.substring(0, 1).toUpperCase() + className.substring(1);
			String targetClassName = "All";
			if ((int) entry.getValue()[8] > -1)
			{
				targetClassName = ClassId.getClassId((int) entry.getValue()[8]).name();
				targetClassName = targetClassName.substring(0, 1).toUpperCase() + targetClassName.substring(1);
			}
			html += "<table width=600 align=center bgcolor=" + ((f % 2) == 0 ? "111111" : "050505") + ">";
			html += "</tr><tr><td><img width=1 height=3 src=\"L2UI.SquareBlank\"/></td></tr>";
			html += "<tr>";
			html += "<td width=30 align=center><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";delete;" + (forOlympiad ? 1 : 0) + ";" + page + ";" + classId + ";" + targetClassId + ";" + (int) entry.getValue()[7] + ";" + (int) entry.getValue()[8] + ";" + entry.getKey() + "\" back=\"L2UI_CT1.Button_DF_Delete_Down\" width=14 height=14 fore=\"L2UI_CT1.Button_DF_Delete\" ></td>";
			html += "<td width=250>";
			html += "<table><tr><td align=center width=200><font color=2E8424 name=\"CreditTextNormal\">" + className + "</font></td></tr><tr><td width=100 align=center><font color=ED792C name=\"CreditTextNormal\">" + targetClassName + "</font></td></tr></table>";
			html += "</td>";
			html += "<td><font color=\"ABABAB\" name=\"CreditTextNormal\">";
			html += "<table align=center width=380>";
			String h1 = "<tr>";
			String h2 = "<tr>";
			String h3 = "<tr>";
			for (int h = 0; h < 7; h++)
			{
				int val = (int) ((entry.getValue()[h] - 1) * 100);
				h1 += "<td width=35 align=center><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";increase;" + (forOlympiad ? 1 : 0) + ";" + page + ";" + classId + ";" + targetClassId + ";" + (int) entry.getValue()[7] + ";" + (int) entry.getValue()[8] + ";" + entry.getKey() + ";" + h + ";" + (entry.getValue()[h] + 0.1) + "\" back=\"L2UI_CH3.upbutton_down\" width=14 height=14 fore=\"L2UI_CH3.UpButton\" ></td>";
				h2 += "<td width=70 align=center>" + (val >= 0 ? "+" + val : val) + "%</td>";
				h3 += "<td width=35 align=center><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";increase;" + (forOlympiad ? 1 : 0) + ";" + page + ";" + classId + ";" + targetClassId + ";" + (int) entry.getValue()[7] + ";" + (int) entry.getValue()[8] + ";" + entry.getKey() + ";" + h + ";" + (entry.getValue()[h] - 0.1) + "\" back=\"L2UI_CH3.downbutton_down\" width=14 height=14 fore=\"L2UI_ch3.DownButton\" ></td>";
			}
			html += h1 + "</tr>" + h2 + "</tr>" + h3 + "</tr>";
			html += "</table>";
			html += "</font></td>";
			html += "</tr><tr><td><img width=1 height=5 src=\"L2UI.SquareBlank\"/></td></tr>";
			html += "</table>";
			html += "<img src=\"L2UI.SquareGray\" width=602 height=1/>";
			i++;
			f++;
		}
		if ((i == 0) || (f == 0))
		{
			html += "<table width=604 align=center bgcolor=000000>";
			html += "<tr><td align=center width=701><font color=CF1616 name=\"CreditTextNormal\">No balances found!</font></td></tr>";
			html += "</table>";
			html += "<img src=\"L2UI.SquareGray\" width=602 height=1/>";
		}
		
		html += "<table bgcolor=000000><tr>";
		if (page > 1)
		{
			html += "<td><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";show;" + (forOlympiad ? 1 : 0) + ";" + (page - 1) + ";" + classId + ";" + targetClassId + "\" width=\"14\" height=\"14\" back=\"L2UI_CT1.Button_DF_Left_Down\" fore=\"L2UI_CT1.Button_DF_Left\" ></td>";
		}
		else
		{
			html += "<td width=14></td>";
		}
		html += "<td width=20 align=center><font color=CBCBCB name=\"CreditTextNormal\">" + page + "</font></td>";
		if ((page * objectsInPage) < _used.size())
		{
			html += "<td><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";show;" + (forOlympiad ? 1 : 0) + ";" + (page + 1) + ";" + classId + ";" + targetClassId + "\" width=\"14\" height=\"14\" back=\"L2UI_CT1.Button_DF_Right_Down\" fore=\"L2UI_CT1.Button_DF_Right\" ></td>";
		}
		else
		{
			html += "<td width=14></td>";
		}
		html += "</tr></table>";
		html += "<button value=\"Back\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";main;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + "\" width=\"135\" height=\"21\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" >";
		html += "</center>";
		return html;
	}
	
	public String showAddPage(int classId, int targetClassId, int race, int tRace, boolean forOlympiad)
	{
		String classes = "";
		for (ClassId cl : ClassId.values())
		{
			if ((cl.level() == 3) && (cl.getRace().ordinal() == race))
			{
				String className = cl.name();
				className = className.substring(0, 1).toUpperCase() + className.substring(1);
				classes += className + ";";
			}
		}
		String tClasses = "";
		for (ClassId cl : ClassId.values())
		{
			if ((cl.level() == 3) && (cl.getRace().ordinal() == tRace))
			{
				String className = cl.name();
				className = className.substring(0, 1).toUpperCase() + className.substring(1);
				tClasses += className + ";";
			}
		}
		String content = "<br><font color=BBBBBB>";
		content += "<table width=\"600\">";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=20 height=10></td></tr>";
		content += "<tr><td></td><td align=\"center\"><font name=\"CreditTextNormal\" color=\"FF7E00\">Class Id</td><td align=\"center\">Target Class Id</font></td><td width=\"20\"></td></tr>";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=20 height=10></td></tr>";
		content += "<tr><td><table>";
		content += "<tr><td width=35></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";0;" + tRace + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 0 ? "_checked" : "") + "\">";
		content += "</td><td<font name=\"CreditTextNormal\" color=\"D2B48C\">Human</td></tr>";
		content += "<tr><td width=35></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";1;" + tRace + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 1 ? "_checked" : "") + "\">";
		content += "</td><td>Elf</td></tr>";
		content += "<tr><td width=35></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";2;" + tRace + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 2 ? "_checked" : "") + "\">";
		content += "</td><td>Dark Elf</td></tr>";
		content += "<tr><td width=35></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";3;" + tRace + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 3 ? "_checked" : "") + "\">";
		content += "</td><td>Orc</td></tr>";
		content += "<tr><td width=35></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";4;" + tRace + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 4 ? "_checked" : "") + "\">";
		content += "</td><td>Dwarf</td></tr>";
		content += "<tr><td width=35></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";5;" + tRace + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 5 ? "_checked" : "") + "\">";
		content += "</td><td>Kamael</td></tr>";
		content += "</table></td>";
		content += "<td align=\"center\"><combobox var=\"classId\" list=\"" + classes + "\" width=110></td>";
		content += "<td align=\"center\"><combobox var=\"tClassId\" list=\"All;" + tClasses + "\" width=110></td>";
		content += "<td><table>";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";" + race + ";0" + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (tRace == 0 ? "_checked" : "") + "\">";
		content += "</td><td>Human</td></tr>";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";" + race + ";1" + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (tRace == 1 ? "_checked" : "") + "\">";
		content += "</td><td>Elf</td></tr>";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";" + race + ";2" + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (tRace == 2 ? "_checked" : "") + "\">";
		content += "</td><td>Dark Elf</td></tr>";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";" + race + ";3" + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (tRace == 3 ? "_checked" : "") + "\">";
		content += "</td><td>Orc</td></tr>";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";" + race + ";4" + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (tRace == 4 ? "_checked" : "") + "\">";
		content += "</td><td>Dwarf</font></td></tr>";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + targetClassId + ";" + race + ";5" + "\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (tRace == 5 ? "_checked" : "") + "\">";
		content += "</td><td>Kamael</font></td></tr>";
		content += "</table></td>";
		content += "</tr>";
		content += "</table>";
		content += "<center><br>";
		content += "<table width=170><tr><td width=20 align=center><img width=1 height=3 src=\"L2UI.SquareBlank\"/><button value=\"\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 0 : 1) + ";" + classId + ";" + targetClassId + ";" + race + ";" + tRace + "\" width=\"14\" height=\"14\" back=\"L2UI.CheckBox" + (forOlympiad ? "" : "_checked") + "\" fore=\"L2UI.CheckBox" + (forOlympiad ? "_checked" : "") + "\"/></td><td width=150 align=left><font name=ScreenMessageSmall color=BABABA>For Olympiad</font></td></tr></table>";
		content += "<br>";
		content += "<button value=\"Add\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";add;" + (forOlympiad ? 1 : 0) + "; $classId ; $tClassId \" width=75 height=24 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
		content += "</center><br>";
		content += "<table width=600><tr><td width=600 align=right>";
		content += "<button value=\"Back\" action=\"bypass " + CLASS_BALANCE_BBS_CMD + ";main;0;" + classId + ";" + targetClassId + "\" width=75 height=24 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
		content += "</td></tr></table><br>";
		content += "<center>";
		content += "<font color=\"E52B50\" name=\"CreditTextNormal\">N -> Normal, C -> Critical, M -> Magic, MC -> Magic Critical,</br1>B -> Blow, PS -> Physical Skill, PSC -> Physical Skill Critical.</font>";
		content += "</center></font>";
		return content;
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
	
	/**
	 * Gets the single instance of RegionBBSManager.
	 * @return single instance of RegionBBSManager
	 */
	public static ClassBalanceBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClassBalanceBBSManager _instance = new ClassBalanceBBSManager();
	}
}