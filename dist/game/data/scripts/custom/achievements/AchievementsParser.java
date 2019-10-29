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
package custom.achievements;

import custom.erengine.ErBonus;
import custom.erengine.ErGlobalVariables;
import custom.erengine.ErReward;
import custom.erengine.ErUtils;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Erlandys
 */
public class AchievementsParser
{
	private final static Logger _log = Logger.getLogger(AchievementsParser.class.getName());
	HashMap<Integer, AchievementCategory> _categories;
	HashMap<String, Achievement> _achievements;
	
	public static boolean LEAVE_TITLE_COLOR = false;
	public static boolean ALLOW_MEMBER_CHANGE_TITLE = false;
	public static int TITLE_BAN = 2;
	public static String COMMUNITY_BOARD_TAB = "_bbshome";
	public static boolean WHOLE_COMMUNITY = false;
	
	String _locationOfHandlers = "l2r.gameserver.handler.achcond";
	
	protected AchievementsParser()
	{
		_categories = new HashMap<>();
		_achievements = new HashMap<>();
		generateSQL();
		generateXMLFiles();
		generateHTMLFiles();
		loadXML();
		AdminCommandHandler.getInstance().registerHandler(new AdminAchievements());
	}
	
	public void loadXML()
	{
		for (L2PcInstance pl : L2World.getInstance().getPlayers())
		{
			if ((pl == null) || (pl.getAchievementPlayer() == null))
			{
				continue;
			}
			pl.getAchievementPlayer().updateAchievements();
		}
		_categories.clear();
		_achievements.clear();
		try
		{
			_log.info(getClass().getSimpleName() + ": Loading achievements.");
			File dir = new File("data/achievements");
			if (!dir.exists() || !dir.isDirectory())
			{
				return;
			}
			for (File file : dir.listFiles())
			{
				Document doc = null;
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				try
				{
					doc = factory.newDocumentBuilder().parse(file);
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Could not parse " + file.getName() + " file: " + e.getMessage(), e);
					return;
				}
				Node n = doc.getFirstChild();
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("set"))
					{
						String setName = d.getAttributes().getNamedItem("name").getNodeValue();
						String setVal = d.getAttributes().getNamedItem("value").getNodeValue();
						fixConfig(setName, setVal);
					}
					else if (d.getNodeName().equalsIgnoreCase("category") && !file.getName().equals("Achievements.xml"))
					{
						int categoryId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
						String categoryName = d.getAttributes().getNamedItem("name").getNodeValue();
						AchievementCategory ac = new AchievementCategory(categoryName);
						int achievementId = 0;
						
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if (c.getNodeName().equalsIgnoreCase("set"))
							{
								String setName = c.getAttributes().getNamedItem("name").getNodeValue();
								String setVal = c.getAttributes().getNamedItem("value").getNodeValue();
								ac.doSet(setName, setVal);
							}
							else if (c.getNodeName().equalsIgnoreCase("achievement"))
							{
								String name = c.getAttributes().getNamedItem("name").getNodeValue();
								String type = c.getAttributes().getNamedItem("type").getNodeValue();
								Achievement achievement = new Achievement(categoryId, categoryName, name, type);
								
								for (Node h = c.getFirstChild(); h != null; h = h.getNextSibling())
								{
									if (h.getNodeName().equalsIgnoreCase("set"))
									{
										String setName = h.getAttributes().getNamedItem("name").getNodeValue();
										String setVal = h.getAttributes().getNamedItem("value").getNodeValue();
										achievement.doSet(setName, setVal);
									}
									else if (h.getNodeName().equalsIgnoreCase("cond"))
									{
										achievement.setConditions(readCond(h));
									}
									else if (h.getNodeName().equalsIgnoreCase("point"))
									{
										int level = Integer.parseInt(h.getAttributes().getNamedItem("level").getNodeValue());
										int requiredValue = Integer.parseInt(h.getAttributes().getNamedItem("requiredValue").getNodeValue());
										boolean isExtra = h.getAttributes().getNamedItem("isExtra") != null ? h.getAttributes().getNamedItem("isExtra").getNodeValue().equalsIgnoreCase("true") : false;
										AchievementLevel al = new AchievementLevel(level, requiredValue, isExtra);
										
										for (Node f = h.getFirstChild(); f != null; f = f.getNextSibling())
										{
											if (f.getNodeName().equalsIgnoreCase("set"))
											{
												String setName = f.getAttributes().getNamedItem("name").getNodeValue();
												String setVal = f.getAttributes().getNamedItem("value").getNodeValue();
												al.doSet(setName, setVal);
											}
											else if (f.getNodeName().equalsIgnoreCase("rewards"))
											{
												int rewardId = 0;
												int bonusId = 0;
												for (Node e = f.getFirstChild(); e != null; e = e.getNextSibling())
												{
													if (e.getNodeName().equalsIgnoreCase("reward"))
													{
														int rew = ErReward.readReward(e, al.getRewards(), rewardId, false);
														if (rew == -1)
														{
															continue;
														}
														rewardId = rew;
													}
													else if (e.getNodeName().equalsIgnoreCase("bonus"))
													{
														int rew = ErBonus.readBonus(e, al.getBonuses(), bonusId);
														if (rew == -1)
														{
															continue;
														}
														bonusId = rew;
													}
												}
											}
											else if (f.getNodeName().equalsIgnoreCase("cond"))
											{
												al.setConditions(readCond(f));
											}
										}
										achievement.addLevel(level, al);
									}
								}
								ac.addAchievement(achievementId, achievement);
								_achievements.put(type, achievement);
								achievementId++;
							}
						}
						_categories.put(categoryId, ac);
					}
				}
			}
			_log.info(getClass().getSimpleName() + ": Successfully loaded " + _categories.size() + " achievement categories and " + _achievements.size() + " achievements.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		for (L2PcInstance pl : L2World.getInstance().getPlayers())
		{
			pl.getAchievementPlayer().readAchievements();
		}
	}
	
	private ArrayList<AchievementICond> readCond(Node n) throws Exception
	{
		ArrayList<AchievementICond> list = new ArrayList<>();
		for (Node f = n.getFirstChild(); f != null; f = f.getNextSibling())
		{
			if (f.getNodeName().equalsIgnoreCase("set"))
			{
				String name = f.getAttributes().getNamedItem("name").getNodeValue();
				String val = f.getAttributes().getNamedItem("val").getNodeValue();
				AchievementICond cond = null;
				try
				{
					Constructor<?> c = Class.forName(_locationOfHandlers + ".A" + name).getConstructor(String.class, String.class);
					cond = (AchievementICond) c.newInstance(name, val);
				}
				catch (ClassNotFoundException e)
				{
					_log.info(getClass().getSimpleName() + ": Condition handler was not found: \"" + _locationOfHandlers + ".A" + name + "\".");
					continue;
				}
				list.add(cond);
			}
		}
		return list;
	}
	
	void fixConfig(String name, String val)
	{
		name = name.toLowerCase();
		switch (name)
		{
			case "leavetitlecolor":
				LEAVE_TITLE_COLOR = val.equalsIgnoreCase("true");
				break;
			case "allowmemberchangetitle":
				ALLOW_MEMBER_CHANGE_TITLE = val.equalsIgnoreCase("true");
				break;
			case "titleban":
				TITLE_BAN = Integer.parseInt(val);
				break;
			case "tabincommunityboard":
				COMMUNITY_BOARD_TAB = val;
				break;
			case "wholecommunity":
				WHOLE_COMMUNITY = val.equalsIgnoreCase("true");
				break;
		}
	}
	
	public HashMap<Integer, AchievementCategory> getCategories()
	{
		return _categories;
	}
	
	public AchievementCategory getCategory(int id)
	{
		return _categories.get(id);
	}
	
	public Achievement getAchievement(String name)
	{
		return _achievements.get(name);
	}
	
	private void generateSQL()
	{
		String text = "";
		text += "CREATE TABLE er_character_achievements (\n";
		text += "  objectId int(10) NOT NULL DEFAULT '0',\n";
		text += "  `type` varchar(50) NOT NULL,\n";
		text += "  `count` bigint(15) unsigned NOT NULL DEFAULT '0',\n";
		text += "  `level` int(5) unsigned DEFAULT '0',\n";
		text += "  finished smallint(1) unsigned DEFAULT '0',\n";
		text += "  is_daily smallint(1) unsigned NOT NULL DEFAULT '0',\n";
		text += "  next_refresh bigint(15) unsigned DEFAULT '0',\n";
		text += "  unclaimed_rewards varchar(50) DEFAULT '',\n";
		text += "  has_title smallint(1) NOT NULL DEFAULT '0',\n";
		text += "  title_changed bigint(15) NOT NULL DEFAULT '0',\n";
		text += "  PRIMARY KEY (objectId,`type`,is_daily)\n";
		text += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";
		ErUtils.generateTable("AchievementsTableInitialised", text);
		
		text = "CREATE TABLE er_character_bonuses (\n";
		text += "  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n";
		text += "  objectId int(10) NOT NULL,\n";
		text += "  `type` varchar(50) DEFAULT NULL,\n";
		text += "  itemId int(10) DEFAULT NULL,\n";
		text += "  count int(10) DEFAULT NULL,\n";
		text += "  itemTypeChance tinyint(1) DEFAULT NULL,\n";
		text += "  addingPercent tinyint(1) DEFAULT NULL,\n";
		text += "  expireDate bigint(15) DEFAULT NULL,\n";
		text += "  PRIMARY KEY (`id`)\n";
		text += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		ErUtils.generateTable("PlayerBonusesInitialised", text);
	}
	
	private void generateXMLFiles()
	{
		if (ErGlobalVariables.getInstance().getBoolean("AchievementsXMLsInitialized"))
		{
			return;
		}
		String text = "";
		text += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list>\n";
		text += "	<!-- If you use npc, leave this \"EMPTY\" (Npc instance -> L2EAchievements) -->\n";
		text += "	<!-- This sets, the tab for community board (can be found in ShowBoard serverpacket). -->\n";
		text += "	<set name=\"TabInCommunityBoard\" value=\"_bbshome\" />\n";
		text += "	<!-- Enable / Disable whole community board to be only for vote. -->\n";
		text += "	<set name=\"WholeCommunity\" value=\"false\" />\n";
		text += "	\n";
		text += "	<set name=\"LeaveTitleColor\" value=\"false\" />\n";
		text += "	<!-- This is used only if the member you want to change title, has achievement title. -->\n";
		text += "	<set name=\"AllowMemberChangeTitle\" value=\"false\" />\n";
		text += "	<!-- After you change title achievement, how long you will not be able to change again (hours). -->\n";
		text += "	<set name=\"TitleBan\" value=\"2\" />\n";
		text += "	\n";
		text += "	<!-- Available condition types:\n";
		text += "		ForClasses, ForRaces, IsCLeader, IsHero, IsInArena, IsInClan, IsInPeaceZone, IsNoble, IsSubclass, MaxLevel, MinClanLevel, MinKarma, MinLevel, MinPk, MinPvP, PlayTime\n";
		text += "	-->\n";
		text += "	\n";
		text += "	<!-- Explanations: -->\n";
		text += "	\n";
		text += "	<!-- category part: -->\n";
		text += "	<!-- id must be unique. -->\n";
		text += "	<!-- name must be unique and will be shown in achievements as Category Name. -->\n";
		text += "	\n";
		text += "	<!-- achievement part: -->\n";
		text += "	<!-- name will be shown as that Achievement Name. -->\n";
		text += "	<!-- type is used, to track from core, what will be counted (monster_kills, champion_kills, raid_kills, raid_id_kills_20899, enchant, blessed_enchant, craft, login_for_first_time, play_time, marriage, gain_noble, gain_hero, pvp, deaths, sprees, spreesc, duel) -->\n";
		text += "	<!-- settings for achievement: -->\n";
		text += "	<!-- title -> the title which player will get -->\n";
		text += "	<!-- titleColor -> the title color which will be applied to player -->\n";
		text += "	<!-- icon -> shown icon next to achievement -->\n";
		text += "	<!-- shortDesc -> description, which is shown in the list of achievements. Possible changes -> %value% changes to next required value. -->\n";
		text += "	<!-- isWithTime (optional) -> if achievement is counted by time (like played time or something like that). -->\n";
		text += "	<!-- giveTitle (optional) -> can be enabled/disabled, to give title after accomplishing the achievement. -->\n";
		text += "	<!-- isDaily -> if achievement will be refreshed every day (at 00:00), all points will vanish. (Cannot be used same type 2 times) -->\n";
		text += "	\n";
		text += "	<!-- point part: -->\n";
		text += "	<!-- level, which level it applies -->\n";
		text += "	<!-- requiredValue, required value for this level -->\n";
		text += "	<!-- isExtra (optional), if is extra, then level does not count as base of achievement,\n";
		text += "		 it is shown, only if player meets conditions (WARNING: Does not make extra level in\n";
		text += "		 middle of levels (for example, 2nd level is extra, 3rd not anymore)).\n";
		text += "		 Extra level is hidden until player meets requirements.\n";
		text += "		 To get the title, player does not have to accomplish extra levels! -->\n";
		text += "	\n";
		text += "	<!-- REWARD: -->\n";
		text += "	<!-- id: is used only when you want to give ITEM as reward, then it is itemId -->\n";
		text += "	<!-- type: can be - \n";
		text += "		 Item\n";
		text += "		 ClanPoints\n";
		text += "		 SkillPoints\n";
		text += "		 Experience -->\n";
		text += "	<!-- min: describes minimum amount of item given to player -->\n";
		text += "	<!-- max: describes maximum amount of item given to player -->\n";
		text += "	<!-- chance: describes the chance of probability that item will be given to player -->\n";
		text += "	\n";
		text += "	<!-- BONUS: -->\n";
		text += "	<!-- id: is used only when you want to give ITEM as reward, then it is itemId -->\n";
		text += "	<!-- type: can be - \n";
		text += "		 Item\n";
		text += "		 ClanPoints\n";
		text += "		 SkillPoints\n";
		text += "		 Experience\n";
		text += "		 WeaponEnchant \n";
		text += "		 ArmorEnchant\n";
		text += "		 SkillsEnchant -->\n";
		text += "	<!-- addingType: can be -\n";
		text += "		 percent: bonus will be calculated with percents,\n";
		text += "		 for example: adena drop chance will be increased not by 10% from 60% to 70%, but 60% by 10%, so it will be 66%.\n";
		text += "		 amount: bonus will be added by amount -->\n";
		text += "	<!-- min: describes minimum amount of bonus given to player -->\n";
		text += "	<!-- max: describes maximum amount of bonus given to player -->\n";
		text += "	<!-- chance: describes the chance of probability that bonus will be given to player -->\n";
		text += "	<!-- itemBonusType: (is used only when adding bonus with Item type) can be -\n";
		text += "		 chance: to increase chance to drop that item\n";
		text += "		 drop: to increase amount of item to be dropped -->\n";
		text += "	<!-- time: (seconds) for how long bonus will be added -->\n";
		text += "	\n";
		text += "	<!-- Example: -->\n";
		text += "	\n";
		text += "	<category id=\"0\" name=\"Example\">\n";
		text += "		<set name=\"progressBarSmallCenterUnfilled\" value=\"L2UI_CH3.br_bar2_cp1\" />\n";
		text += "		<set name=\"progressBarSmallCenterFilled\" value=\"L2UI_CH3.br_bar2_cp\" />\n";
		text += "		<set name=\"progressBarCenterUnfilled\" value=\"L2UI_CH3.br_bar1_cp1\" />\n";
		text += "		<set name=\"progressBarCenterFilled\" value=\"L2UI_CH3.br_bar1_cp\" />\n";
		text += "		\n";
		text += "		<!-- This achievement will be shown, only when player gets 500 pvps and becomes clan leader. -->\n";
		text += "		<!-- If then, player will get some points and lost his title as clan leader, -->\n";
		text += "		<!-- achievement still will be shown, but it will be locked, until player again meets all conditions. -->\n";
		text += "		\n";
		text += "		<!-- Achievement has 4 levels, 3 as base, and one for extra. -->\n";
		text += "		<!-- First 2 levels, does not have any conditions and the third one, will be locked, until player becomes noble. -->\n";
		text += "		<!-- Extra level will be shown, only if player gains noble and hero statuses (also player must finish the base levels.) -->\n";
		text += "		<!-- Third and fourth levels has their descriptions, since we need to fill the description, about being nobles and hero. -->\n";
		text += "		<achievement name=\"Example\" type=\"example_type\">\n";
		text += "			<set name=\"title\" value=\"Example\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4295\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Get %value% examples.\" />\n";
		text += "			<set name=\"longDesc\" value=\"When you get %value% of examples,'br'you will accomplish it.\" />\n";
		text += "			\n";
		text += "			<cond>\n";
		text += "				<set name=\"MinPvp\" val=\"500\" />\n";
		text += "				<set name=\"IsCLeader\" val=\"true\" />\n";
		text += "			</cond>\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"5\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"10\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"15\">\n";
		text += "				<set name=\"shortDesc\" value=\"Get %value% examples, while being nobles.\" />\n";
		text += "				<set name=\"longDesc\" value=\"Get %value% examples, while being nobles.\" />\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<bonus id=\"57\" type=\"Item\" itemBonusType=\"chance\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\"/>\n";
		text += "				</rewards>\n";
		text += "				<cond>\n";
		text += "					<set name=\"IsNoble\" val=\"true\" />\n";
		text += "				</cond>\n";
		text += "			</point>\n";
		text += "			<point level=\"4\" requiredValue=\"20\" isExtra=\"true\">\n";
		text += "				<set name=\"shortDesc\" value=\"Get %value% examples, while being nobles and hero.\" />\n";
		text += "				<set name=\"longDesc\" value=\"Get %value% examples, while being nobles and hero.\" />\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<bonus type=\"SkillPoints\" addingType=\"percent\" min=\"10\" max=\"20\" chance=\"100\" time=\"3600\"/>\n";
		text += "				</rewards>\n";
		text += "				<cond>\n";
		text += "					<set name=\"IsNoble\" val=\"true\" />\n";
		text += "					<set name=\"IsHero\" val=\"true\" />\n";
		text += "				</cond>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "	</category>\n";
		text += "</list>\n";
		
		ErUtils.generateFile("data/achievements/", "Achievements", ".xml", text);
		
		text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list>\n";
		text += "	<category id=\"0\" name=\"PvP\">\n";
		text += "		<set name=\"progressBarSmallCenterUnfilled\" value=\"L2UI_CH3.br_bar2_hp1\" />\n";
		text += "		<set name=\"progressBarSmallCenterFilled\" value=\"L2UI_CH3.br_bar2_hp\" />\n";
		text += "		<set name=\"progressBarCenterUnfilled\" value=\"L2UI_CH3.br_bar1_hp1\" />\n";
		text += "		<set name=\"progressBarCenterFilled\" value=\"L2UI_CH3.br_bar1_hp\" />\n";
		text += "			\n";
		text += "		<achievement name=\"Deaths Bringer\" type=\"pvp\">\n";
		text += "			<set name=\"title\" value=\"Deaths Bringer\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill5076_c\" />\n";
		text += "			<set name=\"shortDesc\" value=\"You have to collect %value% pvps.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Your goal is to kill as much players'br'as you can.\" />\n";
		text += "			<point level=\"1\" requiredValue=\"10\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"15\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"20\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"4\" requiredValue=\"25\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "				<cond>\n";
		text += "					<set name=\"IsNoble\" val=\"true\"/>\n";
		text += "				</cond>\n";
		text += "			</point>\n";
		text += "			<point level=\"5\" requiredValue=\"30\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "				<cond>\n";
		text += "					<set name=\"IsNoble\" val=\"true\"/>\n";
		text += "				</cond>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Deaths Collector\" type=\"deaths\">\n";
		text += "			<set name=\"title\" value=\"Deaths Collector\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4336\" />\n";
		text += "			<set name=\"shortDesc\" value=\"You have to get killed %value% times.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Your goal is to get killed in pvp.\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"10\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"20\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"30\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Sprees Maintainer\" type=\"sprees\">\n";
		text += "			<set name=\"title\" value=\"Sprees Maintainer\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4410_max\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Get %value% kills in a row.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Try to make higher spree as possible.\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Sprees Collector\" type=\"spreesc\">\n";
		text += "			<set name=\"title\" value=\"Sprees Collector\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4381\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Get %value% sprees.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Try to get as much sprees as possible.'br'Counts only after 3 kills in a row.\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Gladiator\" type=\"duel\">\n";
		text += "			<set name=\"title\" value=\"Gladiator\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill5075\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Win %value% duels.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Win %value% duels and be a gladiator!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Support\" type=\"resurrect\">\n";
		text += "			<set name=\"title\" value=\"Support\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill5086\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Ressurrect %value% players.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Resurrect atleast %value% players!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Bishop\" type=\"heal\">\n";
		text += "			<set name=\"title\" value=\"Bishop\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill5041\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Heal atleast %value% HP for players.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Restore atleast %value% health for players!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "	</category>\n";
		text += "</list>\n";
		
		ErUtils.generateFile("data/achievements/", "PvP", ".xml", text);
		
		text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list>\n";
		text += "	<category id=\"1\" name=\"Farm\">\n";
		text += "		<set name=\"progressBarSmallCenterUnfilled\" value=\"L2UI_CH3.br_bar2_cp1\" />\n";
		text += "		<set name=\"progressBarSmallCenterFilled\" value=\"L2UI_CH3.br_bar2_cp\" />\n";
		text += "		<set name=\"progressBarCenterUnfilled\" value=\"L2UI_CH3.br_bar1_cp1\" />\n";
		text += "		<set name=\"progressBarCenterFilled\" value=\"L2UI_CH3.br_bar1_cp\" />\n";
		text += "		\n";
		text += "		<achievement name=\"Farmer\" type=\"monster_kills\">\n";
		text += "			<set name=\"title\" value=\"Farmer\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4295\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Kill %value% monsters.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Try to kill %value% monsters at your league!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"5\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"10\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"15\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"4\" requiredValue=\"20\">\n";
		text += "				<set name=\"shortDesc\" value=\"Kill %value% monsters, being Noble.\" />\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "				<cond>\n";
		text += "					<set name=\"IsNoble\" val=\"true\" />\n";
		text += "				</cond>\n";
		text += "			</point>\n";
		text += "			<point level=\"5\" requiredValue=\"25\" isExtra=\"true\">\n";
		text += "				<set name=\"shortDesc\" value=\"Kill %value% monsters, being Noble and hero.\" />\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "				<cond>\n";
		text += "					<set name=\"IsNoble\" val=\"true\" />\n";
		text += "					<set name=\"IsHero\" val=\"true\" />\n";
		text += "				</cond>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Champion\" type=\"champion_kills\">\n";
		text += "			<set name=\"title\" value=\"Champion\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4462\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Slay %value% monsters champions.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Try to kill %value% monster champions at your league!\" />\n";
		text += "			<cond>\n";
		text += "				<set name=\"IsNoble\" val=\"true\" />\n";
		text += "				<set name=\"IsHero\" val=\"true\" />\n";
		text += "			</cond>\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Raids Slayer\" type=\"raid_kills\">\n";
		text += "			<set name=\"title\" value=\"Raid Slayer\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4689\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Kill %value% raids.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Slay %value% raid bosses!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"10\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"50\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Antharas Slayer\" type=\"raid_id_kills_20899\">\n";
		text += "			<set name=\"title\" value=\"Antharas Slayer\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4289\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Kill Antharas once!\" />\n";
		text += "			<set name=\"longDesc\" value=\"Slay Antharas atleast once!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"1\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"57\" min=\"100\" max=\"1000\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" />\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "	</category>\n";
		text += "</list>\n";
		
		ErUtils.generateFile("data/achievements/", "Farm", ".xml", text);
		
		text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list>\n";
		text += "	<category id=\"2\" name=\"Items\">\n";
		text += "		<achievement name=\"Blessed Enchanter\" type=\"enchant\">\n";
		text += "			<set name=\"title\" value=\"Enchanter\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4273_new\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Enchant your weapon up to +%value% with normal scroll.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Try to make atleast 1 item up to +%value%'br' with normal scroll.\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"3\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"8\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"12\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"16\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Enchanter\" type=\"blessed_enchant\">\n";
		text += "			<set name=\"title\" value=\"Enchanter\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4427\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Enchant your weapon up to %value%'br' with blessed scroll!\" />\n";
		text += "			<set name=\"longDesc\" value=\"Try to make atleast 1 item up to +%value% with blessed scroll.\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"3\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"8\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"12\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"3\" requiredValue=\"16\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Craftsmen\" type=\"craft\">\n";
		text += "			<set name=\"title\" value=\"Craftsmen\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4416_dwarf\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Craft %value% items.\" />\n";
		text += "			<set name=\"longDesc\" value=\"Try to craft %value% items.\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"10\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"50\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "	</category>\n";
		text += "</list>\n";
		
		ErUtils.generateFile("data/achievements/", "Items", ".xml", text);
		
		text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list>\n";
		text += "	<category id=\"3\" name=\"Starters\">\n";
		text += "		<achievement name=\"Scout\" type=\"login_for_first_time\">\n";
		text += "			<set name=\"title\" value=\"Scout\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4416_etc\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Login for the first time!\" />\n";
		text += "			<set name=\"longDesc\" value=\"You just have to login'br'for the first time!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"1\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Wanderer\" type=\"play_time\">\n";
		text += "			<set name=\"isWithTime\" value=\"true\" />\n";
		text += "			<set name=\"title\" value=\"Wanderer\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4416_none\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Play for %valueTime%\" />\n";
		text += "			<set name=\"longDesc\" value=\"Just try to playing %valueTime%'br'time and you will receive rewards.\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"600\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"3600\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "	</category>\n";
		text += "</list>\n";
		
		ErUtils.generateFile("data/achievements/", "Starters", ".xml", text);
		
		text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list>\n";
		text += "	<category id=\"4\" name=\"Daily\">\n";
		text += "		<achievement name=\"Monsters Killer\" type=\"monster_kills_daily\">\n";
		text += "			<set name=\"giveTitle\" value=\"false\" />\n";
		text += "			<set name=\"isDaily\" value=\"true\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill4299\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Kill %value% monsters!\" />\n";
		text += "			<set name=\"longDesc\" value=\"Kill %value% monsters'br'in one day!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"50\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"1\" requiredValue=\"100\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"1\" requiredValue=\"150\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"1\" requiredValue=\"200\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "	</category>\n";
		text += "</list>\n";
		
		ErUtils.generateFile("data/achievements/", "Daily", ".xml", text);
		
		text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list>\n";
		text += "	<category id=\"5\" name=\"Other\">\n";
		text += "		<achievement name=\"Married\" type=\"marriage\">\n";
		text += "			<set name=\"title\" value=\"Married\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill3260\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Marry your love!\" />\n";
		text += "			<set name=\"longDesc\" value=\"Just make your life easier'br'by marrying your love!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"1\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Noble\" type=\"gain_noble\">\n";
		text += "			<set name=\"title\" value=\"Noble\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill1323\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Get noble!\" />\n";
		text += "			<set name=\"longDesc\" value=\"Make yourself noble!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"1\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "		<achievement name=\"Hero\" type=\"gain_hero\">\n";
		text += "			<set name=\"title\" value=\"Hero\" />\n";
		text += "			<set name=\"titleColor\" value=\"FF00FF\" />\n";
		text += "			<set name=\"icon\" value=\"Icon.skill3123\" />\n";
		text += "			<set name=\"shortDesc\" value=\"Get hero!\" />\n";
		text += "			<set name=\"longDesc\" value=\"Win the olympiad and gain'br'hero status several times!\" />\n";
		text += "			\n";
		text += "			<point level=\"1\" requiredValue=\"1\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "			<point level=\"2\" requiredValue=\"3\">\n";
		text += "				<rewards>\n";
		text += "					<reward type=\"Item\" id=\"58\" min=\"1\" max=\"1\" chance=\"100\" />\n";
		text += "				</rewards>\n";
		text += "			</point>\n";
		text += "		</achievement>\n";
		text += "	</category>\n";
		text += "</list>\n";
		
		ErUtils.generateFile("data/achievements/", "Other", ".xml", text);
		
		ErGlobalVariables.getInstance().setData("AchievementsXMLsInitialized", true);
	}
	
	private void generateHTMLFiles()
	{
		if (ErGlobalVariables.getInstance().getBoolean("AchievementsHTMLsInitialized"))
		{
			return;
		}
		String text = "";
		
		text += "<html>\n";
		text += "<body>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"8\" />\n";
		text += "<center>\n";
		text += "%%categories%%\n";
		text += "<table width=\"500\" bgcolor=\"000000\" cellspacing=\"0\" cellpadding=\"0\">\n";
		text += "<tr>\n";
		text += "<td width=\"500\" align=\"center\">\n";
		text += "<img src=\"L2UI.SquareGray\" width=\"500\" height=\"2\" />\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"10\" />\n";
		text += "%title%\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "<tr>\n";
		text += "<td width=\"500\" align=\"center\">\n";
		text += "%titleTimer%\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "<tr>\n";
		text += "<td width=\"500\">\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"10\" />\n";
		text += "%%achievements%%\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "<tr><td><img src=\"L2UI.SquareBlank\" width=\"1\" height=\"10\" /></td></tr>\n";
		text += "</table>\n";
		text += "</center>\n";
		text += "<br><br>\n";
		text += "</body>\n";
		text += "</html>\n";
		ErUtils.generateFile("data/html/cboard/Achievements/", "Menu", ".htm", text);
		
		text = "<html>\n";
		text += "<body>\n";
		text += "<center>\n";
		text += "<table width=\"285\">\n";
		text += "<tr>\n";
		text += "<td align=\"center\">\n";
		text += "<table width=\"230\" bgcolor=\"000000\" cellspacing=\"0\" cellpadding=\"0\">\n";
		text += "<tr>\n";
		text += "<td width=\"230\">\n";
		text += "<img src=\"L2UI.SquareGray\" width=\"230\" height=\"2\"/>\n";
		text += "<table width=\"230\" bgcolor=\"000000\">\n";
		text += "<tr>\n";
		text += "<td width=\"230\" align=\"center\">\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "<font color=\"5D8AA8\">%%category%% Section</font>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "<img src=\"L2UI.SquareGray\" width=\"230\" height=\"1\"/>\n";
		text += "<table width=\"230\">\n";
		text += "<tr>\n";
		text += "<td width=\"230\" align=\"center\">\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "%%name%%<br1>\n";
		text += "<img src=\"%%icon%%\" width=\"32\" height=\"32\" /><br>\n";
		text += "\n";
		text += "%%progress%%\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "<font color=\"C2B280\">%%count%%</font>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "<img src=\"L2UI.SquareGray\" width=\"230\" height=\"1\"/>\n";
		text += "<table width=\"230\" bgcolor=\"000000\">\n";
		text += "<tr>\n";
		text += "<td width=\"230\" align=\"center\">\n";
		text += "<font color=\"CCCCCC\">\n";
		text += "<br>\n";
		text += "%%longDesc%%</font>\n";
		text += "<br>\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "<img src=\"L2UI.SquareGray\" width=\"230\" height=\"1\"/>\n";
		text += "<table width=\"230\">\n";
		text += "<tr>\n";
		text += "<td width=\"230\" align=\"center\">\n";
		text += "<br>\n";
		text += "<font color=\"CCCCCC\">\n";
		text += "Special gifts awaits you after<br1>\n";
		text += "accomplishing every achievement<br1>\n";
		text += "level. Title is waiting to be used!<br>\n";
		text += "</font>\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "<img src=\"L2UI.SquareGray\" width=\"230\" height=\"2\"/>\n";
		text += "<img src=\"L2UI.SquareBlank\" width=\"1\" height=\"5\"/>\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "</td>\n";
		text += "</tr>\n";
		text += "</table>\n";
		text += "</center>\n";
		text += "</body>\n";
		text += "</html>\n";
		
		ErUtils.generateFile("data/html/cboard/Achievements/", "Achievement", ".htm", text);
		ErGlobalVariables.getInstance().setData("AchievementsHTMLsInitialized", true);
	}
	
	public static final AchievementsParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AchievementsParser _instance = new AchievementsParser();
	}
}
