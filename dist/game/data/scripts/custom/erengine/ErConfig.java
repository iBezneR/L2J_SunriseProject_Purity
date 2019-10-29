package custom.erengine;

import l2r.gameserver.model.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ErConfig
{
	public static HashMap<ErEngine, Boolean> ENGINES = new HashMap<>();
	protected static int ENGINES_COUNT = 0;
	protected static int ENGINES_FOR_CONFIGS_COUNT = 0;
	
	/** Captcha Configs */
	public static boolean CAPTCHA_ENABLED;
	public static boolean CAPTCHA_SECOND_SECURITY_LEVEL;
	public static int CAPTCHA_MIN_MONSTERS_COUNT;
	public static int CAPTCHA_MAX_MONSTERS_COUNT;
	public static int CAPTCHA_IDLE_TIME;
	public static int CAPTCHA_DELAY;
	public static int CAPTCHAS_LENGTH;
	public static int CAPTCHA_FAIL;
	
	/** Class Balancer Configs */
	public static long CLASS_BALANCER_UPDATE_DELAY;
	public static boolean CLASS_BALANCER_AFFECTS_SECOND_PROFFESION;
	public static boolean CLASS_BALANCER_AFFECTS_MONSTERS;
	
	/** Forum Configs */
	public static boolean FORUM_IN_WHOLE_COMMUNITY_BOARD;
	public static String FORUM_TAB;
	public static int FORUM_INFORMATION_MANAGEMENT;
	public static boolean FORUM_INCREASE_VIEWS_FOR_AUTHOR_VIEW;
	public static boolean FORUM_AUTHOR_CAN_CLOSE_TOPIC;
	public static int FORUM_TOPICS_LIMIT_IN_PAGE;
	public static int FORUM_MESSAGES_LIMIT_IN_PAGE;
	
	/** Skills Balancer Configs */
	public static long SKILLS_BALANCER_UPDATE_DELAY;
	public static boolean SKILLS_BALANCER_AFFECTS_SECOND_PROFFESION;
	public static boolean SKILLS_BALANCER_AFFECTS_MONSTERS;

	/** Drop/Npc Database Configs */
	public static boolean DNDATABASE_IN_WHOLE_COMMUNITY_BOARD;
	public static String DNDATABASE_TAB;
	public static boolean DNDATABASE_SEARCH_FOR_NPCS;
	public static ArrayList<Integer> DNDATABASE_EXCLUDE_MONSTERS = new ArrayList<>();
	public static ArrayList<Integer> DNDATABASE_EXCLUDE_ITEMS = new ArrayList<>();
	public static boolean DNDATABASE_ALLOW_TELEPORT;
	public static boolean DNDATABASE_TELEPORT_ONLY_FROM_PEACE;
	public static boolean DNDATABASE_TELEPORT_ONLY_PREMIUM;
	public static boolean DNDATABASE_TELEPORT_MONSTERS;
	public static boolean DNDATABASE_TELEPORT_RAID_BOSSES;
	public static boolean DNDATABASE_TELEPORT_GRAND_BOSSES;
	public static ArrayList<Integer> DNDATABASE_TELEPORT_EXCEPT_MONSTERS = new ArrayList<>();
	public static ArrayList<Integer> DNDATABASE_TELEPORT_INCLUDE_MONSTERS = new ArrayList<>();
	public static ArrayList<Integer> DNDATABASE_TELEPORT_PREMIUM = new ArrayList<>();
	public static HashMap<Integer, Location> DNDATABASE_TELEPORT_LOCATIONS = new HashMap<>();

	/** Raid Boss Stats Configs */
	public static boolean RBSTATS_IN_WHOLE_COMMUNITY_BOARD;
	public static String RBSTATS_TAB;
	public static String RBSTATS_FIRST_PAGE;
	public static boolean RBSTATS_ALLOW_RAID_BOSSES;
	public static boolean RBSTATS_ALLOW_GRAND_BOSSES;
	public static boolean RBSTATS_ALLOW_CLAN_BOSSES;
	public static boolean RBSTATS_SHOW_ALL_RAID_BOSSES;
	public static boolean RBSTATS_SHOW_ALL_GRAND_BOSSES;
	public static ArrayList<Integer> RBSTATS_INCLUDE_CLAN_BOSSES = new ArrayList<>();
	public static ArrayList<Integer> RBSTATS_INCLUDE_RAID_BOSSES = new ArrayList<>();
	public static ArrayList<Integer> RBSTATS_INCLUDE_GRAND_BOSSES = new ArrayList<>();
	public static ArrayList<Integer> RBSTATS_EXCLUDE_RAID_BOSSES = new ArrayList<>();
	public static ArrayList<Integer> RBSTATS_EXCLUDE_GRAND_BOSSES = new ArrayList<>();
	public static boolean RBSTATS_SHOW_EXACT_RESPAWN_TIME;
	public static String RBSTATS_TIME_FORMAT;

	protected static void loadEngines()
	{
		for (ErEngine ee : ErEngine.values())
		{
			if (ee.equals(ErEngine.ErEngine))
			{
				continue;
			}
			boolean exists = false;
			if (ee.isFinished())
			{
				try
				{
					Class.forName(ee.getClassPath());
					exists = true;
					ENGINES_COUNT++;
					if (ee.useConfig())
					{
						ENGINES_FOR_CONFIGS_COUNT++;
					}
				}
				catch (ClassNotFoundException e)
				{
				}
			}
			ENGINES.put(ee, exists);
		}
	}
	
	protected static void loadConfig()
	{
		if (ENGINES_FOR_CONFIGS_COUNT > 0)
		{
			final String Erlandys = "./config/Erlandys.properties";
			
			boolean update = false;
			
			if (ENGINES.get(ErEngine.Captcha) && !ErGlobalVariables.getInstance().getBoolean("CaptchaConfigInitialized"))
			{
				update = true;
			}
			
			if (ENGINES.get(ErEngine.ClassBalancer) && !ErGlobalVariables.getInstance().getBoolean("CBalancerConfigInitialized"))
			{
				update = true;
			}
			
			if (ENGINES.get(ErEngine.Forum) && !ErGlobalVariables.getInstance().getBoolean("ForumConfigInitialized"))
			{
				update = true;
			}
			
			if (ENGINES.get(ErEngine.SkillsBalancer) && !ErGlobalVariables.getInstance().getBoolean("SBalancerConfigInitialized"))
			{
				update = true;
			}

			if (ENGINES.get(ErEngine.DNDatabase) && !ErGlobalVariables.getInstance().getBoolean("DNDatabaseConfigInitialized"))
			{
				update = true;
			}

			if (ENGINES.get(ErEngine.RaidBossStats) && !ErGlobalVariables.getInstance().getBoolean("RBStatsConfigInitialized"))
			{
				update = true;
			}

			if (!(new File(Erlandys)).exists() || update)
			{
				generateConfigFile("./config/", "Erlandys", ".properties");
			}
			
			try
			{
				ErProperties ErlandysSettings = load(Erlandys);
				
				if (ENGINES.get(ErEngine.Captcha))
				{
					CAPTCHA_ENABLED = ErlandysSettings.getProperty("CaptchaEnabled", true);
					CAPTCHA_SECOND_SECURITY_LEVEL = ErlandysSettings.getProperty("CaptchaSecondSecurityLevel", false);
					CAPTCHA_MIN_MONSTERS_COUNT = ErlandysSettings.getProperty("CaptchaMinMonstersCount", 100);
					CAPTCHA_MAX_MONSTERS_COUNT = ErlandysSettings.getProperty("CaptchaMaxMonstersCount", 150);
					CAPTCHA_IDLE_TIME = ErlandysSettings.getProperty("CaptchaIdleTime", 300) * 1000;
					CAPTCHA_DELAY = ErlandysSettings.getProperty("CaptchaDelay", 60) * 1000;
					CAPTCHAS_LENGTH = ErlandysSettings.getProperty("CaptchaLength", 4);
					CAPTCHA_FAIL = ErlandysSettings.getProperty("CaptchaFails", 3);
				}
				
				if (ENGINES.get(ErEngine.ClassBalancer))
				{
					CLASS_BALANCER_UPDATE_DELAY = ErlandysSettings.getProperty("ClassBalancerUpdateDelay", 300) * 1000;
					CLASS_BALANCER_AFFECTS_SECOND_PROFFESION = ErlandysSettings.getProperty("ClassBalancerAffectSecondProffesion", false);
					CLASS_BALANCER_AFFECTS_MONSTERS = ErlandysSettings.getProperty("ClassBalancerAffectMonsters", false);
				}
				
				if (ENGINES.get(ErEngine.Forum))
				{
					FORUM_IN_WHOLE_COMMUNITY_BOARD = ErlandysSettings.getProperty("ForumInWholeCommunityBoard", false);
					FORUM_TAB = ErlandysSettings.getProperty("ForumTab", "_bbshome");
					FORUM_INFORMATION_MANAGEMENT = ErlandysSettings.getProperty("ForumInformationManagement", 60);
					FORUM_INCREASE_VIEWS_FOR_AUTHOR_VIEW = ErlandysSettings.getProperty("ForumIncreaseViewsForAuthorView", false);
					FORUM_AUTHOR_CAN_CLOSE_TOPIC = ErlandysSettings.getProperty("ForumAuthorCanCloseTopic", false);
					FORUM_TOPICS_LIMIT_IN_PAGE = ErlandysSettings.getProperty("ForumTopicsLimitInPage", 10);
					FORUM_MESSAGES_LIMIT_IN_PAGE = ErlandysSettings.getProperty("ForumMessagesLimitInPage", 10);
				}
				
				if (ENGINES.get(ErEngine.SkillsBalancer))
				{
					SKILLS_BALANCER_UPDATE_DELAY = ErlandysSettings.getProperty("SkillsBalancerUpdateDelay", 300) * 1000;
					SKILLS_BALANCER_AFFECTS_SECOND_PROFFESION = ErlandysSettings.getProperty("SkillsBalancerAffectSecondProffesion", false);
					SKILLS_BALANCER_AFFECTS_MONSTERS = ErlandysSettings.getProperty("SkillsBalancerAffectMonsters", false);
				}

				if (ENGINES.get(ErEngine.DNDatabase))
				{
					DNDATABASE_IN_WHOLE_COMMUNITY_BOARD = ErlandysSettings.getProperty("DNDatabaseInWholeCBoard", false);
					DNDATABASE_TAB = ErlandysSettings.getProperty("DNDatabaseTab", "_bbshome");
					DNDATABASE_SEARCH_FOR_NPCS = ErlandysSettings.getProperty("DNDatabaseSearchForNpcs", false);
					DNDATABASE_EXCLUDE_MONSTERS.clear();
					String info[] = ErlandysSettings.getProperty("DNDatabaseExcludedMonsters", "").split(";");
					for (String i : info)
					{
						DNDATABASE_EXCLUDE_MONSTERS.add(Integer.parseInt(i));
					}
					DNDATABASE_EXCLUDE_ITEMS.clear();
					info = ErlandysSettings.getProperty("DNDatabaseExcludedItems", "").split(";");
					for (String i : info)
					{
						DNDATABASE_EXCLUDE_ITEMS.add(Integer.parseInt(i));
					}
					DNDATABASE_ALLOW_TELEPORT = ErlandysSettings.getProperty("DNDatabaseAllowTeleport", false);
					DNDATABASE_TELEPORT_ONLY_FROM_PEACE = ErlandysSettings.getProperty("DNDatabaseTeleportOnlyFromPeace", false);
					DNDATABASE_TELEPORT_ONLY_PREMIUM = ErlandysSettings.getProperty("DNDatabaseTeleportOnlyPremium", false);
					DNDATABASE_TELEPORT_MONSTERS = ErlandysSettings.getProperty("DNDatabaseTeleportAllowMonsters", false);
					DNDATABASE_TELEPORT_RAID_BOSSES = ErlandysSettings.getProperty("DNDatabaseTeleportAllowRaidBosses", false);
					DNDATABASE_TELEPORT_GRAND_BOSSES = ErlandysSettings.getProperty("DNDatabaseTeleportAllowGrandBosses", false);
					DNDATABASE_TELEPORT_EXCEPT_MONSTERS.clear();
					info = ErlandysSettings.getProperty("DNDatabaseTeleportExceptMonsters", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						DNDATABASE_TELEPORT_EXCEPT_MONSTERS.add(Integer.parseInt(i));
					}
					DNDATABASE_TELEPORT_INCLUDE_MONSTERS.clear();
					info = ErlandysSettings.getProperty("DNDatabaseTeleportIncludeMonsters", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						DNDATABASE_TELEPORT_INCLUDE_MONSTERS.add(Integer.parseInt(i));
					}
					DNDATABASE_TELEPORT_PREMIUM.clear();
					info = ErlandysSettings.getProperty("DNDatabaseTeleportMonsterForPremium", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						DNDATABASE_TELEPORT_PREMIUM.add(Integer.parseInt(i));
					}
					DNDATABASE_TELEPORT_LOCATIONS.clear();
					info = ErlandysSettings.getProperty("DNDatabaseTeleportLocations", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						String splittedData[] = i.split(",");
						DNDATABASE_TELEPORT_LOCATIONS.put(Integer.parseInt(splittedData[0]), new Location(Integer.parseInt(splittedData[1]), Integer.parseInt(splittedData[2]), Integer.parseInt(splittedData[3])));
					}
				}

				if (ENGINES.get(ErEngine.RaidBossStats))
				{
					RBSTATS_IN_WHOLE_COMMUNITY_BOARD = ErlandysSettings.getProperty("RBStatsInWholeCBoard", false);
					RBSTATS_TAB = ErlandysSettings.getProperty("RBStatsTab", "_bbshome");
					RBSTATS_FIRST_PAGE = ErlandysSettings.getProperty("RBStatsFirstPage", "random");
					RBSTATS_ALLOW_RAID_BOSSES = ErlandysSettings.getProperty("RBStatsAllowRaidBosses", false);
					RBSTATS_ALLOW_GRAND_BOSSES = ErlandysSettings.getProperty("RBStatsAllowGrandBosses", false);
					RBSTATS_ALLOW_CLAN_BOSSES = ErlandysSettings.getProperty("RBStatsAllowClanBosses", false);
					RBSTATS_SHOW_ALL_RAID_BOSSES = ErlandysSettings.getProperty("RBStatsShowAllRaidBosses", false);
					RBSTATS_SHOW_ALL_GRAND_BOSSES = ErlandysSettings.getProperty("RBStatsShowAllGrandBosses", false);
					RBSTATS_SHOW_EXACT_RESPAWN_TIME = ErlandysSettings.getProperty("RBStatsShowExactRespawnTime", false);
					RBSTATS_TIME_FORMAT = ErlandysSettings.getProperty("RBStatsTimeFormat", "MM.dd HH:mm");
					RBSTATS_INCLUDE_CLAN_BOSSES.clear();
					String info[] = ErlandysSettings.getProperty("RBStatsIncludeClanBosses", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						RBSTATS_INCLUDE_CLAN_BOSSES.add(Integer.parseInt(i));
					}
					RBSTATS_INCLUDE_RAID_BOSSES.clear();
					info = ErlandysSettings.getProperty("RBStatsIncludeRaidBosses", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						RBSTATS_INCLUDE_RAID_BOSSES.add(Integer.parseInt(i));
					}
					RBSTATS_INCLUDE_GRAND_BOSSES.clear();
					info = ErlandysSettings.getProperty("RBStatsIncludeGrandBosses", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						RBSTATS_INCLUDE_GRAND_BOSSES.add(Integer.parseInt(i));
					}
					RBSTATS_EXCLUDE_RAID_BOSSES.clear();
					info = ErlandysSettings.getProperty("RBStatsExcludeRaidBosses", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						RBSTATS_EXCLUDE_RAID_BOSSES.add(Integer.parseInt(i));
					}
					RBSTATS_EXCLUDE_GRAND_BOSSES.clear();
					info = ErlandysSettings.getProperty("RBStatsExcludeGrandBosses", "").split(";");
					for (String i : info)
					{
						if (i == null || i.equals(""))
							continue;
						RBSTATS_EXCLUDE_GRAND_BOSSES.add(Integer.parseInt(i));
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + Erlandys + " File.");
			}
		}
	}
	
	private static void generateConfigFile(String dest, String name, String ending)
	{
		String text = "";
		if (ENGINES.get(ErEngine.Captcha))
		{
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Captcha\n";
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Enables captcha system.\n";
			text += "# Default: True\n";
			text += "CaptchaEnable = True\n";
			text += "\n";
			text += "# Enables second security level for captcha.\n";
			text += "# First one is for captcha key to be shown through ScreenMessage on center, second one in image.\n";
			text += "# Default: True\n";
			text += "CaptchaSecondSecurityLevel = False\n";
			text += "\n";
			text += "# Min monsters count required for captcha to appear.\n";
			text += "# Default: 100\n";
			text += "CaptchaMinMonstersCount = 1\n";
			text += "\n";
			text += "# Max monsters count required for captcha to appear.\n";
			text += "# Default: 100\n";
			text += "CaptchaMaxMonstersCount = 5\n";
			text += "\n";
			text += "# Idle time (seconds) for player to not to kill any monster, for kills to refresh.\n";
			text += "# Default: 300 (5 minutes)\n";
			text += "CaptchaIdleTime = 300\n";
			text += "\n";
			text += "# Captchas delay, how much time has player to input captcha (seconds).\n";
			text += "# Default: 60\n";
			text += "CaptchaDelay = 60\n";
			text += "\n";
			text += "# Captchas length. How much numbers player will have to input.\n";
			text += "# Default: 4\n";
			text += "CaptchaLength = 4\n";
			text += "\n";
			text += "# How many times player can mistake, inputting captcha and after those x times, gets disconnect.\n";
			text += "# Default: 3\n";
			text += "CaptchaFails = 3\n";
			text += "\n";
			ErGlobalVariables.getInstance().setData("CaptchaConfigInitialized", true);
		}
		
		if (ENGINES.get(ErEngine.ClassBalancer))
		{
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Class Balancer Settings:\n";
			text += "# ---------------------------------------------------------------------------\n";
			text += "# This is for update delay, when update all changes to the database (seconds)\n";
			text += "# NOTE: Smaller time amount, more work for server, so change if you know how\n";
			text += "# Default: 300 (5minutes)\n";
			text += "ClassBalancerUpdateDelay = 300\n";
			text += "\n";
			text += "# Enable or Disable class balancer work for second class\n";
			text += "# Example: if balance will be made for Ghost Hunter and this config will be enabled so same balances will work for Abyss Walker\n";
			text += "# Default: false\n";
			text += "ClassBalancerAffectSecondProffesion = false\n";
			text += "\n";
			text += "# Enable or Disable balance for monsters (to change this you must create balance vs All)\n";
			text += "# Default: false\n";
			text += "ClassBalancerAffectMonsters = false\n";
			text += "\n";
			ErGlobalVariables.getInstance().setData("CBalancerConfigInitialized", true);
		}
		
		if (ENGINES.get(ErEngine.DNDatabase))
		{
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Drop/Npcs database:\n";
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Enable or disable Drop/Npcs database in whole community board.\n";
			text += "# Default: false\n";
			text += "DNDatabaseInWholeCBoard = false\n";
			text += "\n";
			text += "# Set the community board tab for database.\n";
			text += "# Default: _bbshome\n";
			text += "DNDatabaseTab = _bbshome\n";
			text += "\n";
			text += "# Enable or Disable search for npcs.\n";
			text += "# Default: false\n";
			text += "DNDatabaseSearchForNpcs = false\n";
			text += "\n";
			text += "# Set npc/monster ids, for engine to exclude them from search.\n";
			text += "# Usage: npcId;npcId;npcId....\n";
			text += "# Default: 29019;\n";
			text += "DNDatabaseExcludedMonsters = 29019;\n";
			text += "\n";
			text += "# Set item ids, for engine to exclude them from search.\n";
			text += "# Usage: itemId;itemId;itemId....\n";
			text += "# Default: 57;\n";
			text += "DNDatabaseExcludedItems = 57;\n";
			text += "\n";
			text += "# Enable / Disable function, to teleport to monsters.\n";
			text += "# Default: false\n";
			text += "DNDatabaseAllowTeleport = false\n";
			text += "\n";
			text += "# Enable / Disable do teleport only from peace zone.\n";
			text += "# Default: false\n";
			text += "DNDatabaseTeleportOnlyFromPeace = false\n";
			text += "\n";
			text += "# Enable / Disable making teleport only for Premium players.\n";
			text += "# Default: false\n";
			text += "DNDatabaseTeleportOnlyPremium = false\n";
			text += "\n";
			text += "# Enable / Disable doing teleport to Monsters.\n";
			text += "# Default: false\n";
			text += "DNDatabaseTeleportAllowMonsters = false\n";
			text += "\n";
			text += "# Enable / Disable doing teleport to Raid Bosses.\n";
			text += "# Default: false\n";
			text += "DNDatabaseTeleportAllowRaidBosses = false\n";
			text += "\n";
			text += "# Enable / Disable doing teleport to Grand Bosses.\n";
			text += "# Default: false\n";
			text += "DNDatabaseTeleportAllowGrandBosses = false\n";
			text += "\n";
			text += "# Make a list, which monsters will be excluded to teleport to.\n";
			text += "# Usage: npcId;npcId;npcId\n";
			text += "# Default: 29022;29020\n";
			text += "DNDatabaseTeleportExceptMonsters = 29022;29020\n";
			text += "\n";
			text += "# Make a list, which monsters will be included to teleport to.\n";
			text += "# All of above configs except \"DNDatabaseAllowTeleport\"  will be ignored.\n";
			text += "# Usage: npcId;npcId;npcId\n";
			text += "# Default: 29023;29024\n";
			text += "DNDatabaseTeleportIncludeMonsters = 29023;29024\n";
			text += "\n";
			text += "# Make a list, which monsters will require Premium for teleportation.\n";
			text += "# Usage: npcId;npcId;npcId\n";
			text += "# Default: 29023;29024\n";
			text += "DNDatabaseTeleportMonsterForPremium = 29023;29024\n";
			text += "\n";
			text += "# If spawn location is not declared, or is not good, make a list, which monsters\n";
			text += "# will have different teleport locations\n";
			text += "# Usage: npcId,x,y,z;npcId,x,y,z;npcId,x,y,z\n";
			text += "# Default: 29023,0,0,0;29024,0,0,0\n";
			text += "DNDatabaseTeleportLocations = 29023,0,0,0;29024,0,0,0\n";
			text += "\n";
			ErGlobalVariables.getInstance().setData("DNDatabaseConfigInitialized", true);
		}
		
		if (ENGINES.get(ErEngine.Forum))
		{
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Forum\n";
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Enables forum to be in whole community board.\n";
			text += "# Default: False\n";
			text += "ForumInWholeCommunityBoard = False\n";
			text += "\n";
			text += "# Changes the tab of forum in community board.\n";
			text += "# Default: _bbshome\n";
			text += "ForumTab = _bbshome\n";
			text += "\n";
			text += "# After how much seconds information will be updated to the database.\n";
			text += "# Default: 60\n";
			text += "ForumInformationManagement = 60\n";
			text += "\n";
			text += "# Should forum increase views after author will view its own topic.\n";
			text += "# Default: False\n";
			text += "ForumIncreaseViewsForAuthorView = False\n";
			text += "\n";
			text += "# Should author of the topic be able to close it.\n";
			text += "# Default: False\n";
			text += "ForumAuthorCanCloseTopic = False\n";
			text += "\n";
			text += "# Topics limit in one page.\n";
			text += "# Default: 10\n";
			text += "ForumTopicsLimitInPage = 10\n";
			text += "\n";
			text += "# Messages limit in one page.\n";
			text += "# Default: 10\n";
			text += "ForumMessagesLimitInPage = 10\n";
			text += "\n";
			ErGlobalVariables.getInstance().setData("ForumConfigInitialized", true);
		}

		if (ENGINES.get(ErEngine.RaidBossStats))
		{
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Raid Boss Stats:\n";
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Enable or disable Raid Boss Stats in whole community board.\n";
			text += "# Default: false\n";
			text += "RBStatsInWholeCBoard = false\n";
			text += "\n";
			text += "# Set the community board tab for RBStats.\n";
			text += "# Default: _bbshome\n";
			text += "RBStatsTab = _bbshome\n";
			text += "\n";
			text += "# Choose what will be opened firstly when player will open RB Stats.\n";
			text += "# Choices: random / grand / raid / clan\n";
			text += "# random: open one of choices\n";
			text += "# g: will open Grand Bosses\n";
			text += "# r: will open Raid Bosses\n";
			text += "# c: will open Clan Bosses\n";
			text += "# Default: random\n";
			text += "RBStatsFirstPage = random\n";
			text += "\n";
			text += "# Enable / Disable button, which will open Raid Bosses section.\n";
			text += "# Default: true\n";
			text += "RBStatsAllowRaidBosses = true\n";
			text += "\n";
			text += "# Enable / Disable button, which will open Grand Bosses section.\n";
			text += "# Default: true\n";
			text += "RBStatsAllowGrandBosses = true\n";
			text += "\n";
			text += "# Enable / Disable button, which will open Clan Bosses section.\n";
			text += "# Default: false\n";
			text += "RBStatsAllowClanBosses = false\n";
			text += "\n";
			text += "# Enable / Disable showing all Raid Bosses in RB section.\n";
			text += "# Default: false\n";
			text += "RBStatsShowAllRaidBosses = false\n";
			text += "\n";
			text += "# Enable / Disable showing all Grand Bosses in Grand section.\n";
			text += "# Default: false\n";
			text += "RBStatsShowAllGrandBosses = false\n";
			text += "\n";
			text += "# Show either exact respawn date, or interval from when to when raid may appear.\n";
			text += "# Default: false\n";
			text += "RBStatsShowExactRespawnTime = false\n";
			text += "\n";
			text += "# Set the time format for respawn date. Check in google for 'java simple date format'.\n";
			text += "# Default: MM.dd HH:mm\n";
			text += "RBStatsTimeFormat = MM.dd HH:mm\n";
			text += "\n";
			text += "# Make a list, which bosses will appear in Clan Section.\n";
			text += "# Usage: npcId;npcId;npcId\n";
			text += "# Default: 29022;29020\n";
			text += "RBStatsIncludeClanBosses = 29022;29020\n";
			text += "\n";
			text += "# Make a list, which bosses will appear in Raid Boss Section.\n";
			text += "# Usage: npcId;npcId;npcId\n";
			text += "# Default: 29023;29024\n";
			text += "RBStatsIncludeRaidBosses = 29023;29024\n";
			text += "\n";
			text += "# Make a list, which bosses will appear in Grand Boss Section.\n";
			text += "# Usage: npcId;npcId;npcId\n";
			text += "# Default: 29023;29024\n";
			text += "RBStatsIncludeGrandBosses = 29023;29024\n";
			text += "\n";
			text += "# Make a list, which bosses will be excluded from shown in Raid Boss section.\n";
			text += "# Usage: npcId;npcId;npcId\n";
			text += "# Default: 29023;29024\n";
			text += "RBStatsExcludeRaidBosses = 29023;29024\n";
			text += "\n";
			text += "# Make a list, which bosses will be excluded from shown in Grand Boss section.\n";
			text += "# Usage: npcId;npcId;npcId\n";
			text += "# Default: 29023;29024\n";
			text += "RBStatsExcludeGrandBosses = 29023;29024\n";
			text += "\n";
			ErGlobalVariables.getInstance().setData("RBStatsConfigInitialized", true);
		}

		if (ENGINES.get(ErEngine.SkillsBalancer))
		{
			text += "# ---------------------------------------------------------------------------\n";
			text += "# Skills Balancer Settings:\n";
			text += "# ---------------------------------------------------------------------------\n";
			text += "# This is for update delay, when update all changes to the database (seconds)\n";
			text += "# NOTE: Smaller time amount, more work for server, so change if you know how\n";
			text += "# Default: 300 (5minutes)\n";
			text += "SkillsBalancerUpdateDelay = 300\n";
			text += "\n";
			text += "# Enable or Disable skills balancer work for second class\n";
			text += "# Example: if balance will be made for Ghost Hunter and this config will be enabled so same balances will work for Abyss Walker\n";
			text += "# Default: false\n";
			text += "SkillsBalancerAffectSecondProffesion = false\n";
			text += "\n";
			text += "# Enable or Disable balance for monsters (to change this you must create balance vs All)\n";
			text += "# Default: false\n";
			text += "SkillsBalancerAffectMonsters = false\n";
			text += "\n";
			ErGlobalVariables.getInstance().setData("SBalancerConfigInitialized", true);
		}
		ErUtils.generateFile(dest, name, ending, text);
	}
	
	private static ErProperties load(String filename)
	{
		return load(new File(filename));
	}
	
	private static ErProperties load(File file)
	{
		ErProperties result = new ErProperties();
		
		try
		{
			result.load(file);
		}
		catch (IOException e)
		{
			System.out.println("Error loading config : " + file.getName() + "!");
		}
		
		return result;
	}
}
