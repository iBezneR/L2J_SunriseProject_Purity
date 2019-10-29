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
package custom.erengine;

import l2r.L2DatabaseFactory;
import l2r.gameserver.GameServer;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;

public class ErUtils
{
	public ErUtils()
	{
		load();
	}

	private void load()
	{
		GameServer.printSection("Erlandys Engines Start");
		ErGlobalVariables.getInstance();
		ErConfig.loadEngines();
		ErConfig.loadConfig();
		AdminCommandHandler.getInstance().registerHandler(new AdminErParams());
		boolean startBonusCheck = false;
		
		for (Map.Entry<ErEngine, Boolean> entry : ErConfig.ENGINES.entrySet())
		{
			if (entry.getValue())
			{
				try
				{
					Class.forName(entry.getKey().getClassPath()).getDeclaredMethod("getInstance").invoke(null);
					if (entry.getKey().useBonuses())
						startBonusCheck = true;
				}
				catch (Exception e)
				{
					System.out.println(e.toString());
				}
			}
		}
		
		if (startBonusCheck)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> doBonusCheck(), 15000, 15000);
		}

		GameServer.printSection("Erlandys Engines End");
	}

	void doBonusCheck()
	{
		Collection<L2PcInstance> players = L2World.getInstance().getPlayers();
		if (players != null)
		{
			for (L2PcInstance player : players)
			{
				if (player == null)
				{
					continue;
				}
				try
				{
					Object o = player.getClass().getMethod("getPlayerBonuses").invoke(player);
					if (o != null)
					{
						Class.forName("custom.erengine.ErPlayerBonuses").getMethod("doBonusCheck").invoke(o);
					}
				}
				catch (final Throwable t)
				{
					t.printStackTrace();
				}
			}
		}
	}

	public void shutDown()
	{
		if (ErConfig.ENGINES.get(ErEngine.ClassBalancer))
		{
			try
			{
				Class.forName("custom.classbalancer.ClassBalanceManager").getDeclaredMethod("getInstance").invoke(null).getClass().getMethod("updateBalances");
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Forum))
		{
			try
			{
				Class.forName("custom.forum.ForumParser").getDeclaredMethod("getInstance").invoke(null).getClass().getMethod("updateDatabase");
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.NpcToPc))
		{
			try
			{
				Class.forName("custom.npctopc.NpcToPcManager").getDeclaredMethod("getInstance").invoke(null).getClass().getMethod("rewriteToXml");
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.SkillsBalancer))
		{
			try
			{
				Class.forName("custom.skillsbalancer.SkillsBalanceManager").getDeclaredMethod("getInstance").invoke(null).getClass().getMethod("updateBalances");
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
		ErGlobalVariables.getInstance().updateVariables();
	}

	public static void generateFile(String dest, String name, String ending, String text)
	{
		File f = new File(dest + name + ending);
		if (f.exists())
		{
			File f1 = new File(dest + name + "_1" + ending);
			if (f1.exists())
			{
				f1.delete();
			}
			f.renameTo(f1);
			System.out.println("ErEngine: Renaming [" + name + "] to [" + name + "_1] in [" + dest + "].");
		}

		File fileCreate = new File(dest + name + ending);
		try
		{
			fileCreate.createNewFile();
		}
		catch (IOException e)
		{
			(new File(dest)).mkdirs();
			try
			{
				fileCreate.createNewFile();
			}
			catch (IOException e1)
			{
				System.out.println(e1.toString());
			}
		}

		f.setWritable(true, false);

		try (BufferedWriter output = new BufferedWriter(new FileWriter(new File(dest + name + ending))))
		{
			output.write(text);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("ErEngine: Successfully created file [" + name + ending + "] in [" + dest + "]!");
	}

	public static void generateTable(String name, String table)
	{
		if (!ErGlobalVariables.getInstance().getBoolean(name))
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 Statement statement = con.createStatement();)
			{
				statement.executeUpdate(table);
				statement.close();
			}
			catch (Exception e)
			{
				System.out.println("Could not create table: " + e.getMessage());
			}
			ErGlobalVariables.getInstance().setData(name, true);
		}
	}

	public static void generateTables(String name, String... table)
	{
		if (!ErGlobalVariables.getInstance().getBoolean(name))
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
                 Statement statement = con.createStatement();)
			{
				for (String t : table)
				{
					if ((t == null) || (t.length() < 1) || t.equals(""))
					{
						continue;
					}
					statement.addBatch(t);
				}
				statement.executeBatch();
				statement.close();
			}
			catch (Exception e)
			{
				System.out.println("Could not create table: " + e.getMessage());
			}
			ErGlobalVariables.getInstance().setData(name, true);
		}
	}

	public void doPlayerRestore(L2PcInstance player)
	{
		if (ErConfig.ENGINES.get(ErEngine.Achievements))
		{
			try
			{
				player.getClass().getMethod("setAchievementPlayer", Class.forName("custom.achievements.AchievementPlayer")).invoke(player, Class.forName("custom.achievements.AchievementPlayer").getConstructor(L2PcInstance.class).newInstance(player));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Buffer))
		{
			try
			{
				player.getClass().getMethod("setBufferPlayer", Class.forName("custom.buffer.BufferPlayer")).invoke(player, Class.forName("custom.buffer.BufferPlayer").getConstructor(L2PcInstance.class).newInstance(player));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Captcha))
		{
			try
			{
				player.getClass().getMethod("setCPlayer", Class.forName("custom.captcha.CaptchaPlayer")).invoke(player, Class.forName("custom.captcha.CaptchaPlayer").getConstructor(L2PcInstance.class).newInstance(player));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Museum))
		{
			try
			{
				Class.forName("custom.museum.MuseumManager").getMethod("restoreDataForChar", L2PcInstance.class).invoke(Class.forName("custom.museum.MuseumManager").getDeclaredMethod("getInstance").invoke(null), player);
				player.getClass().getMethod("refreshMuseumOnlineTime").invoke(player);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Vote))
		{
			try
			{
				player.getClass().getMethod("setVotingPlayer", Class.forName("custom.vote.VotingPlayer")).invoke(player, Class.forName("custom.vote.VotingPlayer").getConstructor(L2PcInstance.class).newInstance(player));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Museum) || ErConfig.ENGINES.get(ErEngine.Vote) || ErConfig.ENGINES.get(ErEngine.Achievements))
		{
			try
			{
				player.getClass().getMethod("setPlayerBonuses", Class.forName("custom.erengine.ErPlayerBonuses")).invoke(player, Class.forName("custom.erengine.ErPlayerBonuses").getConstructor(L2PcInstance.class).newInstance(player));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void doPlayerStore(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		if (ErConfig.ENGINES.get(ErEngine.Achievements))
		{
			try
			{
				Object b = player.getClass().getMethod("getAchievementPlayer").invoke(player);
				if (b != null)
				{
					Class.forName("custom.achievements.AchievementPlayer").getMethod("updateAchievements").invoke(b);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Buffer))
		{
			try
			{
				Object b = player.getClass().getMethod("getBufferPlayer").invoke(player);
				if (b != null)
				{
					Class.forName("custom.buffer.BufferPlayer").getMethod("updateBufferSchemes").invoke(b);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Museum))
		{
			try
			{
				Object b = Class.forName("custom.museum.MuseumManager").getDeclaredMethod("getInstance").invoke(null);
				if (b != null)
				{
					Class.forName("custom.museum.MuseumManager").getMethod("updateDataForChar", L2PcInstance.class).invoke(b, player);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Vote))
		{
			try
			{
				Object b = player.getClass().getMethod("getVotingPlayer").invoke(player);
				if (b != null)
				{
					Class.forName("custom.vote.VotingPlayer").getMethod("updateVotes").invoke(b);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if (ErConfig.ENGINES.get(ErEngine.Museum) || ErConfig.ENGINES.get(ErEngine.Vote) || ErConfig.ENGINES.get(ErEngine.Achievements))
		{
			try
			{
				Object b = player.getClass().getMethod("getPlayerBonuses").invoke(player);
				if (b != null)
				{
					Class.forName("custom.erengine.ErPlayerBonuses").getMethod("updateBonuses").invoke(b);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void doPlayerClean(L2PcInstance player)
	{
	}

	public boolean onTutorialBypass(L2PcInstance player, String command)
	{
		if (ErConfig.ENGINES.get(ErEngine.Captcha) && command.startsWith("captcha"))
		{
			try
			{
				Class.forName("custom.captcha.Captcha").getMethod("onBypass", String.class, L2PcInstance.class).invoke(Class.forName("captcha.Captcha").getDeclaredMethod("getInstance").invoke(null), command, player);
				return true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	protected void showList(L2PcInstance player)
	{
		String text = "<html><body>";
		text += "<br>";
		text += "<center><font color=\"888888\" name=\"CreditTextNormal\">Currently you have " + ErConfig.ENGINES_COUNT + "/" + (ErEngine.values().length - 1) + " engines</font><br1>";
		text += "<font color=\"AAAAAA\" name=\"CreditTextNormal\">Available engines</font></center>";
		text += "<br>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<table bgcolor=\"111111\">";
		text += "<tr>";
		text += "<td width=\"190\" align=\"center\">";
		text += "<font name=\"CreditTextNormal\" color=\"99FF33\">Erlandys Engine</font>";
		text += "</td>";
		text += "<td width=\"100\" align=\"center\">";
		text += "<button value=\"Read More\" action=\"bypass -h admin_erengine 0\" width=\"75\" height=\"21\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\" />";
		text += "</td>";
		text += "<td width=\"10\"></td>";
		text += "</tr>";
		text += "</table>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"6\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		int i = 0;
		for (ErEngine ee : ErEngine.values())
		{
			if (ee.getId() == 0)
			{
				continue;
			}
			if (ee.getId() > 1)
			{
				text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
				text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"1\"/>";
				text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
			}
			text += "<table bgcolor=\"" + ((i % 2) == 1 ? "111111" : "050505") + "\">";
			text += "<tr>";
			text += "<td width=\"190\" align=\"center\">";
			text += "<font name=\"CreditTextNormal\" color=\"" + (ErConfig.ENGINES.get(ee) ? "99FF33" : "992200") + "\">" + ee.getName() + "</font>" + (ee.isFinished() ? "" : "<font name=\"CreditTextNormal\" color=\"FF9900\"> (In progress)</font>");
			text += "</td>";
			text += "<td width=\"100\" align=\"center\">";
			text += "<button value=\"Read More\" action=\"bypass -h admin_erengine " + (ee.getId()) + "\" width=\"75\" height=\"21\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\" />";
			text += "</td>";
			text += "<td width=\"10\"></td>";
			text += "</tr>";
			text += "</table>";
			i++;
		}
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"15\"/>";
		text += "<center><font color=\"AAAAAA\" name=\"CreditTextNormal\">Made by Erlandys<br1>";
		text += "Skype: Erlandys56</font></center>";
		text += "</body></html>";
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		htm.setHtml(text);
		player.sendPacket(htm);
	}

	protected void showInfo(L2PcInstance player, ErEngine engine)
	{
		boolean engineExists = (engine.getId() == 0) || ErConfig.ENGINES.get(engine);
		String text = "<html><body>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"3\"/>";
		text += "<table bgcolor=\"111111\">";
		text += "<tr>";
		text += "<td width=\"300\" align=\"center\">";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"5\"/>";
		text += "<font name=\"ScreenMessageSmall\" color=\"" + (engineExists ? "99FF33" : "992200") + "\">" + engine.getName() + "</font>" + (engine.isFinished() ? "" : "<font color=\"FF9900\"> (In progress)</font>");
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"5\"/>";
		text += "</td>";
		text += "</tr>";
		text += "</table>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"3\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"15\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<table bgcolor=\"050505\">";
		text += "<tr>";
		text += "<td width=\"300\" align=\"center\">";
		text += "<font color=\"888888\" name=\"CreditTextNormal\">Basic information</font>";
		text += "</td>";
		text += "</tr>";
		text += "</table>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"6\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"1\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<table bgcolor=\"111111\" width=\"300\">";
		text += "<tr>";
		text += "<td fixwidth=\"280\" align=\"justify\"><font name=\"CreditTextNormal\">";
		if (engine.getInformation().equals(""))
		{
			text += "No information yet!";
		}
		else
		{
			text += engine.getInformation();
		}
		text += "</font></td>";
		text += "<td fixwidth=\"20\"></td>";
		text += "</tr>";
		text += "</table>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"1\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"6\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<table bgcolor=\"050505\">";
		text += "<tr>";
		text += "<td width=\"300\" align=\"center\">";
		text += "<font color=\"888888\" name=\"CreditTextNormal\">Commands</font>";
		text += "</td>";
		text += "</tr>";
		text += "</table>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"6\"/>";
		int i = 0;
		for (String command : engine.getCommands())
		{
			if (i > 0)
			{
				text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
				text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"1\"/>";
				text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
			}
			String bypass = command.replace("//", "admin_");
			text += "<table bgcolor=\"" + ((i % 2) == 1 ? "050505" : "111111") + "\">";
			text += "<tr>";
			text += "<td width=\"140\" align=\"center\">";
			text += "<font color=\"BBBBBB\" name=\"CreditTextNormal\">" + command + "</font>";
			text += "</td>";
			text += "<td width=\"160\" align=\"left\">";
			if (engineExists)
			{
				text += "<button value=\"Use\" action=\"bypass -h " + bypass + "\" width=\"75\" height=\"21\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\" />";
			}
			else
			{
				text += "<table background=\"L2UI_CT1.Button_DF_Disable\" width=\"75\" height=\"19\"><tr><td align=\"center\" width=\"75\" height=\"19\"><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"2\"><font color=\"E4DABC\">Use</font><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"2\"></td></tr></table>";
			}
			text += "</td>";
			text += "</tr>";
			text += "</table>";
			text += "<table bgcolor=\"" + ((i % 2) == 1 ? "050505" : "111111") + "\">";
			text += "<tr>";
			text += "<td width=\"300\" align=\"center\">";
			text += "<font color=\"CC9966\" name=\"CreditTextNormal\">" + (engine.getCommandsInfo().length < i ? "No information." : engine.getCommandInfo(i)) + "</font>";
			text += "</td>";
			text += "</tr>";
			text += "</table>";
			i++;
		}
		if (i == 0)
		{
			text += "<table bgcolor=\"000000\">";
			text += "<tr>";
			text += "<td width=\"300\" align=\"center\">";
			text += "<font name=\"CreditTextNormal\">No admin commands listed.</font>";
			text += "</td>";
			text += "</tr>";
			text += "</table>";
		}
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"2\"/>";
		text += "<img src=\"L2UI.SquareGray\" width=\"295\" height=\"3\"/>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"5\"/>";
		text += "<center><button value=\"Back\" action=\"bypass -h admin_erengine\" width=\"135\" height=\"21\" fore=\"L2UI_CT1.Button_DF\" back=\"L2UI_CT1.Button_DF_Down\" /></center>";
		text += "<img src=\"L2UI.SquareBlank\" width=\"295\" height=\"5\"/>";
		text += "<center><font color=\"AAAAAA\" name=\"CreditTextNormal\">Made by Erlandys<br1>";
		text += "Skype: Erlandys56</font></center>";
		text += "</body></html>";
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		htm.setHtml(text);
		player.sendPacket(htm);
	}

	public static ErUtils getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final ErUtils _instance = new ErUtils();
	}
}