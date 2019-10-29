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
package custom.skillsbalancer;

import l2r.gameserver.communitybbs.Managers.BaseBBSManager;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.skills.L2Skill;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SkillsBalanceBBSManager extends BaseBBSManager
{
	public static String SKILLS_BALANCE_BBS_CMD = "_bbsskillsbalancer";
	
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		if (!activeChar.isGM())
		{
			return;
		}
		if (command.equals("admin_skillsbalancer"))
		{
			command = SKILLS_BALANCE_BBS_CMD + ";main";
		}
		String html = "<html><body><br><br>";
		command = command.substring(command.length() > (SKILLS_BALANCE_BBS_CMD.length()) ? SKILLS_BALANCE_BBS_CMD.length() + 1 : SKILLS_BALANCE_BBS_CMD.length());
		StringTokenizer st = new StringTokenizer(command, ";");
		String cmd = "main";
		if (st.hasMoreTokens())
		{
			cmd = st.nextToken();
		}
		boolean forOlympiad = false;
		if (st.hasMoreTokens())
		{
			forOlympiad = st.nextToken().equals("1");
		}
		int classId = -1;
		if (st.hasMoreTokens() && !cmd.equalsIgnoreCase("add"))
		{
			classId = Integer.parseInt(st.nextToken());
		}
		if (!cmd.startsWith("add"))
		{
			html += showHeading(forOlympiad, classId);
		}
		if (cmd.equalsIgnoreCase("main"))
		{
			html += showMain(forOlympiad, classId);
		}
		else if (cmd.equalsIgnoreCase("search"))
		{
			html += showSearchResults(st, activeChar, forOlympiad, classId);
		}
		else if (cmd.equalsIgnoreCase("delete"))
		{
			int key = Integer.parseInt(st.nextToken());
			int skillId = Integer.parseInt(st.nextToken());
			SkillsBalanceManager.getInstance().removeBalance(key, skillId, classId, forOlympiad);
			html += showSearchResults(st, activeChar, forOlympiad, classId);
		}
		else if (cmd.equalsIgnoreCase("increase"))
		{
			int key = Integer.parseInt(st.nextToken());
			int skillId = Integer.parseInt(st.nextToken());
			int type = Integer.parseInt(st.nextToken());
			double value = Double.parseDouble(st.nextToken());
			SkillsBalanceManager.getInstance().updateBalance(key, skillId, classId, type, value, forOlympiad);
			html += showSearchResults(st, activeChar, forOlympiad, classId);
		}
		else if (cmd.equalsIgnoreCase("addpage"))
		{
			int race = Integer.parseInt(st.nextToken());
			html += showAddPage(classId, race, forOlympiad);
		}
		else if (cmd.equalsIgnoreCase("add"))
		{
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
			String skill = st.nextToken().replaceAll(" ", "");
			boolean isNumber = true;
			int skillId = -1;
			try
			{
				skillId = Integer.parseInt(skill);
			}
			catch (NumberFormatException e)
			{
				isNumber = false;
			}
			int race = Integer.parseInt(st.nextToken());
			if (!isNumber)
			{
				activeChar.sendMessage(("Implemented skill id is not number [" + skill + "]!"));
				html += showAddPage(classId, race, forOlympiad);
			}
			else
			{
				if (SkillData.getInstance().getInfo(skillId, 1) == null)
				{
					activeChar.sendMessage(("Skill you are trying to add with id, does not exist [" + skill + "]!"));
					html += showAddPage(classId, race, forOlympiad);
				}
				else
				{
					double values[] =
					{
						1,
						1,
						skillId,
						classId
					};
					int key = ((skillId * (classId < 0 ? -1 : 1)) + (classId * 65536));
					SkillsBalanceManager.getInstance().updateBalance(key, skillId, classId, values, forOlympiad);
					StringTokenizer st1 = new StringTokenizer("" + skillId);
					html += showHeading(forOlympiad, classId);
					html += showSearchResults(st1, activeChar, forOlympiad, classId);
				}
			}
		}
		html += "</body></html>";
		separateAndSend(html, activeChar);
	}
	
	public String showSearchResults(StringTokenizer st, L2PcInstance activeChar, boolean forOlympiad, int classId)
	{
		String html = "";
		String skill = "";
		if (st.hasMoreTokens())
		{
			skill = st.nextToken();
		}
		skill = skill.replace(" ", "");
		int page = 1;
		if (st.hasMoreTokens())
		{
			page = Integer.parseInt(st.nextToken());
		}
		boolean isId = true;
		int skillId = -1;
		try
		{
			skillId = Integer.parseInt(skill);
		}
		catch (NumberFormatException e)
		{
			isId = false;
		}
		
		if (!isId && (skill.length() < 4))
		{
			activeChar.sendMessage(("You can not imput less than 4 characters for name search!"));
			html += showMain(forOlympiad, classId);
		}
		else if (!isId && (skill.length() > 3))
		{
			ArrayList<Integer> skills = SkillsBalanceManager.getInstance().getSkillsByName(forOlympiad, skill, classId);
			if (skills.size() < 1)
			{
				String cl = "";
				if (classId >= 0)
				{
					String name = ClassId.getClassId(classId).name();
					name = name.substring(0, 1).toUpperCase() + name.substring(1);
					cl = " to target " + name;
				}
				activeChar.sendMessage(("No used skills were found using " + skill + cl + "!"));
				html += showMain(forOlympiad, classId);
			}
			else
			{
				html += showSkills(forOlympiad, classId, skill, skills, page);
			}
		}
		else
		{
			ArrayList<Integer> skills = SkillsBalanceManager.getInstance().getUsedSkillsById(forOlympiad, skillId, classId);
			if ((skills == null) || (skills.size() < 1))
			{
				String cl = "";
				if (classId >= 0)
				{
					String name = ClassId.getClassId(classId).name();
					name = name.substring(0, 1).toUpperCase() + name.substring(1);
					cl = " to target " + name;
				}
				activeChar.sendMessage(("No used skills were found using ID[" + skillId + "]" + cl + "!"));
				html += showMain(forOlympiad, classId);
			}
			else
			{
				html += showSkills(forOlympiad, classId, String.valueOf(skillId), skills, page);
			}
		}
		return html;
	}
	
	public String showHeading(boolean forOlympiad, int classId)
	{
		String html = "<center>";
		html += "<font name=\"ScreenMessageSmall\">Skills balancer</font><br>";
		html += "<table width=500><tr>";
		html += "<td width=20 align=center><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";main;" + (forOlympiad ? 0 : 1) + ";" + classId + "\" width=\"14\" height=\"14\" back=\"L2UI.CheckBox" + (forOlympiad ? "" : "_checked") + "\" fore=\"L2UI.CheckBox" + (forOlympiad ? "_checked" : "") + "\"/></td>";
		html += "<td width=200 align=left><font color=BABABA name=\"CreditTextNormal\">Show for Olympiad</font><img width=1 height=10 src=\"L2UI.SquareBlank\"/></td>";
		html += "<td width=140 align=left><edit var=\"skill\" width=140 height=15></td>";
		html += "<td width=140 align=left><button value=\"Search\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";search;" + (forOlympiad ? 1 : 0) + ";" + classId + "; $skill \" width=\"74\" height=\"22\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>";
		html += "</tr></table><br>";
		return html;
	}
	
	public String showMain(boolean forOlympiad, int classId)
	{
		String html = "";
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
			html += "<tr><td width=20 align=center><img width=1 height=3 src=\"L2UI.SquareBlank\"/><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";main;" + (forOlympiad ? 1 : 0) + ";" + (cl.getId() == classId ? -1 : cl.getId()) + "\" width=\"14\" height=\"14\" back=\"L2UI.CheckBox" + (cl.getId() == classId ? "" : "_checked") + "\" fore=\"L2UI.CheckBox" + (cl.getId() == classId ? "_checked" : "") + "\"/></td><td width=200><font name=CreditTextNormal color=BABABA>" + name + "</font></td></tr>";
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
		html += "<table bgcolor=000000><tr>";
		html += "<td width=400></td>";
		html += "<td width=200></td>";
		html += "<td><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";addpage;0;" + classId + ";0\" width=\"32\" height=\"32\" back=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red_Down\" fore=\"L2UI_CT1.MiniMap_DF_PlusBtn_Red\" ></td>";
		html += "</tr></table>";
		return html;
		
	}
	
	public String showSkills(boolean forOlympiad, int classId, String search, ArrayList<Integer> skills, int page)
	{
		String html = "<center>";
		html += "<table width=602 align=center bgcolor=000000>";
		html += "<tr>";
		html += "<td width=30 align=center></td>";
		html += "<td width=400><img width=1 height=5 src=\"L2UI.SquareBlank\"/><font color=2E8424 name=\"CreditTextNormal\">Skill</font><font color=888888> -></font> <font color=ED792C name=\"CreditTextNormal\">Target Class</font></td>";
		html += "<td width=85 align=center><font color=888888 name=\"CreditTextNormal\">Chance</td>";
		html += "<td width=85 align=center>Power</font></td>";
		html += "</tr>";
		html += "</table>";
		html += "<img src=\"L2UI.SquareBlank\" width=600 height=1/>";
		html += "<img src=\"L2UI.SquareGray\" width=600 height=2/>";
		html += "<img src=\"L2UI.SquareBlank\" width=600 height=1/>";
		int i = 0;
		int f = 0;
		int objectsInPage = 3;
		if (skills != null)
		{
			for (int key : skills)
			{
				if ((i < ((page - 1) * objectsInPage)) || (i >= (page * objectsInPage)))
				{
					i++;
					continue;
				}
				double values[] = SkillsBalanceManager.getInstance().getBalance(key, forOlympiad);
				String targetClassName = "All";
				if ((int) values[3] > -1)
				{
					targetClassName = ClassId.getClassId((int) values[3]).name();
					targetClassName = targetClassName.substring(0, 1).toUpperCase() + targetClassName.substring(1);
				}
				L2Skill sk = SkillData.getInstance().getInfo((int) values[2], 1);
				html += "<table width=600 align=center " + ((f % 2) == 0 ? "bgcolor=000000" : "") + ">";
				html += "</tr><tr><td><img width=1 height=3 src=\"L2UI.SquareBlank\"/></td></tr>";
				html += "<tr>";
				
				html += "<td width=30 align=center><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";delete;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + key + ";" + sk.getId() + "; " + search + ";" + page + "\" back=\"L2UI_CT1.Button_DF_Delete_Down\" width=14 height=14 fore=\"L2UI_CT1.Button_DF_Delete\" ></td>";
				html += "<td width=400>";
				html += "<font color=2E8424 name=\"CreditTextNormal\">" + sk.getName() + "</font><font color=888888 name=\"CreditTextNormal\"> -></font> <font color=ED792C name=\"CreditTextNormal\">" + targetClassName + "</font>";
				html += "</font></td>";
				html += "<td>";
				html += "<font color=888888><table>";
				String h1 = "<tr>";
				String h2 = "<tr>";
				String h3 = "<tr>";
				
				for (int h = 0; h < 2; h++)
				{
					int val = (int) ((values[h] - 1) * 100);
					h1 += "<td width=35 align=center><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";increase;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + key + ";" + sk.getId() + ";" + h + ";" + (values[h] + 0.1) + "; " + search + "\" back=\"L2UI_CH3.upbutton_down\" width=14 height=14 fore=\"L2UI_CH3.UpButton\" ></td>";
					h2 += "<td width=85 align=center><font name=\"CreditTextNormal\" color=\"D2B48C\">" + (val >= 0 ? "+" + val : val) + "%</font></td>";
					h3 += "<td width=35 align=center><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";increase;" + (forOlympiad ? 1 : 0) + ";" + classId + ";" + key + ";" + sk.getId() + ";" + h + ";" + (values[h] - 0.1) + "; " + search + "\" back=\"L2UI_CH3.downbutton_down\" width=14 height=14 fore=\"L2UI_ch3.DownButton\" ></td>";
				}
				html += h1 + "</tr>" + h2 + "</tr>" + h3 + "</tr>";
				html += "</table></font>";
				html += "</td>";
				html += "</tr><tr><td><img width=1 height=5 src=\"L2UI.SquareBlank\"/></td></tr>";
				html += "</table>";
				html += "<img src=\"L2UI.SquareGray\" width=600 height=1/>";
				i++;
				f++;
			}
		}
		if ((i == 0) || (f == 0))
		{
			html += "<table width=501 align=center bgcolor=000000>";
			html += "<tr><td align=center width=602><font color=CF1616 name=\"CreditTextNormal\">No balances found!</font></td></tr>";
			html += "</table>";
			html += "<img src=\"L2UI.SquareGray\" width=600 height=1/>";
		}
		html += "<table bgcolor=000000><tr>";
		if (page > 1)
		{
			html += "<td><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";search;" + (forOlympiad ? 1 : 0) + ";" + classId + "; " + search + ";" + (page - 1) + "\" width=\"14\" height=\"14\" back=\"L2UI_CT1.Button_DF_Left_Down\" fore=\"L2UI_CT1.Button_DF_Left\" ></td>";
		}
		else
		{
			html += "<td width=14></td>";
		}
		html += "<td width=20 align=center><font color=CBCBCB name=\"CreditTextNormal\">" + page + "</font></td>";
		if ((skills != null) && ((page * objectsInPage) < skills.size()))
		{
			html += "<td><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";search;" + (forOlympiad ? 1 : 0) + ";" + classId + "; " + search + ";" + (page + 1) + "\" width=\"14\" height=\"14\" width=\"14\" height=\"14\" back=\"L2UI_CT1.Button_DF_Right_Down\" fore=\"L2UI_CT1.Button_DF_Right\" ></td>";
		}
		else
		{
			html += "<td width=14></td>";
		}
		html += "</tr></table>";
		html += "<button value=\"Back\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";main\" width=\"74\" height=\"22\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" >";
		html += "</center>";
		html += "<font name=\"CreditTextNormal\" color=\"999999\">Indicated search keywords [" + search + "]</font><br>";
		return html;
	}
	
	public String showAddPage(int classId, int race, boolean forOlympiad)
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
		String content = "<br>";
		content += "<table width=\"640\">";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=40 height=10></td></tr>";
		content += "<tr>";
		content += "<td><table>";
		content += "<tr><td width=100></td><td></td><td width=200><font name=\"CreditTextNormal\" color=\"FF7E00\">Target Class Id</font></td></tr>";
		content += "<tr><td width=100></td><td></td><td><img src=\"L2UI.SquareBlank\" width=40 height=10></td></tr>";
		content += "<tr><td width=100></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";0\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 0 ? "_checked" : "") + "\">";
		content += "</td><td><font name=\"CreditTextNormal\" color=D2B48C>Human</td></tr>";
		content += "<tr><td width=100></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";1\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 1 ? "_checked" : "") + "\">";
		content += "</td><td>Elf</td></tr>";
		content += "<tr><td width=100></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";2\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 2 ? "_checked" : "") + "\">";
		content += "</td><td>Dark Elf</td></tr>";
		content += "<tr><td width=100></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";3\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 3 ? "_checked" : "") + "\">";
		content += "</td><td>Orc</td></tr>";
		content += "<tr><td width=100></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";4\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 4 ? "_checked" : "") + "\">";
		content += "</td><td>Dwarf</td></tr>";
		content += "<tr><td width=100></td><td><img src=\"L2UI.SquareBlank\" width=1 height=3/>";
		content += "<button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 1 : 0) + ";" + classId + ";5\" width=12 height=12 back=\"L2UI.CheckBox_checked\" fore=\"L2UI.CheckBox" + (race == 5 ? "_checked" : "") + "\">";
		content += "</td><td>Kamael</font></td></tr>";
		content += "<tr><td width=100></td><td></td><td><combobox var=\"classId\" list=\"All;" + classes + "\" width=110></td></tr>";
		content += "</table></td>";
		content += "<td><table>";
		content += "<tr><td><img src=\"L2UI.SquareBlank\" width=40 height=100></td></tr>";
		content += "<tr><td width=100></td><td><edit var=\"skillId\" width=140 height=15></td><td width=200><font color=\"E52B50\" name=\"CreditTextNormal\">Skill Id</font></td></tr>";
		content += "</table></td>";
		content += "</tr>";
		content += "</table><br><br>";
		content += "<center><br>";
		content += "<table width=170><tr><td width=20 align=center><img width=1 height=3 src=\"L2UI.SquareBlank\"/><button value=\"\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";addpage;" + (forOlympiad ? 0 : 1) + ";" + classId + ";" + race + "\" width=\"14\" height=\"14\" back=\"L2UI.CheckBox" + (forOlympiad ? "" : "_checked") + "\" fore=\"L2UI.CheckBox" + (forOlympiad ? "_checked" : "") + "\"/></td><td width=150 align=left><font name=ScreenMessageSmall color=BABABA>For Olympiad</font></td></tr></table>";
		content += "<br>";
		content += "<button value=\"Add\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";add;" + (forOlympiad ? 1 : 0) + "; $classId ; $skillId ;" + race + "\" width=\"74\" height=\"22\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
		content += "</center><br>";
		content += "<table width=500><tr><td width=500 align=right>";
		content += "<button value=\"Back\" action=\"bypass " + SKILLS_BALANCE_BBS_CMD + ";main;0\" width=\"74\" height=\"22\" back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
		content += "</td></tr></table><br>";
		content += "</font>";
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
	public static SkillsBalanceBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillsBalanceBBSManager _instance = new SkillsBalanceBBSManager();
	}
}