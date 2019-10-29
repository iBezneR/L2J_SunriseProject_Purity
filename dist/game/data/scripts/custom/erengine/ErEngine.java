package custom.erengine;

public enum ErEngine {
	ErEngine(0, "Erlandys Engine", "", false,
			"This is crucial engine to use any other engines. Allows you to get into all other engines with ease. "
			+ "Also includes information about every engine (even not bought ones).",
			new String[] {"//erengine", "//reloadconfigs"},
			new String[] {"Calls up this intreface.", "Reload Erlandys.properties file."},
			true, false),
	Buffer(1, "Buffer", "custom.buffer.BufferParser", false,
			"This buffer is pretty same to all others, except some parts:<br1>"
			+ "Firstly, you do not have to edit any HTML files, XML modifies them for you.<br1>"
			+ "Secondly, you can modify even categories by yourself."
			+ "Thirdly, you can add enchanted buffs to buffer and they will be shown.",
			new String[] {"//reloadbuffer"},
			new String[] {"Reloads all buffs from Buffer.xml file."},
			true, false),
	Captcha(2, "Captcha", "custom.captcha.Captcha", true,
			"Captcha is also called server side anti bot system. "
			+ "It allows you to choose when Captcha (anti bot) window will be shown to player. "
			+ "You have to choose MIN / MAX monsters count for it. "
			+ "It will show window with 14 buttons on it and with 1 image or code on screen/in messages. "
			+ "And player will have to input the code by pressing buttons (generated automatically)."
			+ "3 mistakes and player will be kicked. Timer is also in. Player is invulnerable while inputting captcha.",
			new String[] {},
			new String[] {},
			true, false),
	ClassBalancer(3, "Class Balancer", "custom.classbalancer.ClassBalanceManager", true,
			"This engine helps you to balance your classes. All you have to do is call up the interface, "
			+ "add balance and edit the CLASS vs CLASS damage percents.<br1>You can edit in 7 ways:<br1>"
			+ "Normal, Critical, Magic, MCritical, Blow, PhysSkillDam, PhysSkillDamCrit",
			new String[] {"//classbalancer", "//cbalancerupdate", "//cbalancerreload"},
			new String[] {"Calls up balancer interface.", "Updates current balances to the database.", "Restores balances from the database."},
			true, false),
	Forum(4, "Ingame Forum", "custom.forum.ForumParser", true,
			"This feature is very usefull for players, since they can use forum, without "
			+ "going erevents.out of the game. You can choose in which tab Forum will be and forum will be shown there. "
			+ "Forum is totally separated from outside of server. So it will not cause any lags or information leak. "
			+ "Also players do not have to register to write anything in there. "
			+ "Forum is fully editable, you can create new sections with types, for admins or for authors only and etc., "
			+ "also you can create topics, close, open, stick, unstick or remove them. Post in topics and so on.",
			new String[] {},
			new String[] {},
			true, false),
	Museum(5, "Museum", "custom.museum.MuseumManager", false,
			"Museum engine is simply, the statistics engine. "
			+ "Which gives ability to have lots of categories, with "
			+ "different sections. Also ability to have daily/weekly/monthly tops "
			+ "with rewards as items, bonuses or statues.",
			new String[] {"//reloadmuseum"},
			new String[] {"Reloads museum categories from XML file."},
			true, true),
	NpcToPc(6, "Npc to Pc", "custom.npctopc.NpcToPcManager", false,
			"This feature helps you to change your NPCs appearance into player like one. "
			+ "You can modify practically everything in there, what you can change to player. "
			+ "Also you get ingame editor, which has all the data you need, all you have to do "
			+ "is just choose what you want and you will get it.<br1>"
			+ "Couple editable features:<br1>"
			+ "Abnormal Effects.<br1>"
			+ "Fishing position.<br1>"
			+ "Set Augment for weapon.<br1>"
			+ "Make player dead/sitting/in combat....",
			new String[] {"//reloadnpctopc", "//updatenpctopc"},
			new String[] {"Restores data from Npc_to_Pc.xml file.", "Stores data to Npc_to_Pc.xml file."},
			true, false),
	SkillsBalancer(7, "Skills Balancer", "custom.skillsbalancer.SkillsBalanceManager", true,
			"This engine helps you to balance your skills. All you have to do is call up the interface, "
			+ "add balance and edit the SKILL vs CLASS damage or chance percents. Also you can add balance to affect all targeted classes.",
			new String[] {"admin_skillsbalancer", "admin_sbalancerupdate", "admin_sbalancerreload"},
			new String[] {"Calls up interface.", "Updates balances to database.", "Restores all balances from database."},
			true, false),
	Vote(8, "Vote Engine", "custom.vote.VoteManager", false,
			"This vote manager is totally individual, which has ability "
			+ "to contain up to multiple vote sites instantly. Rewards "
			+ "can be given differently, you can give ability to player, "
			+ "to choose which reward he wants, also you can add rewards "
			+ "as bonuses, which will give bonus like +10% adena drop for "
			+ "next 2h.",
			new String[] {"//reloadvote"},
			new String[] {"Reloads vote configs from XML file."},
			true, true),
	Images(9, "Server Images", "custom.images.ImagesConverter", false,
			"This engine helps you to add images to server without modifying client. "
			+ "All you have to do is to put image into 'data/images' folder and in html "
			+ "you can call it with only %%ImageName%% (in button as fore='%%imageName%%' or in image src='%%imageName%%'.",
			new String[] {"//reloadimages"},
			new String[] {"Reloads images from folder and sends to chars."}, true, false),
	DNDatabase(10, "Drop/Npc Data", "custom.dndatabase.DNDatabase", true,
			"This engine allows players to look through all npcs and drops, "
			+ "using only community board. Player will be able to see "
			+ "drop chances, drops and other stuff about npc.",
			new String[] {"//reloaddnd"},
			new String[] {"Reloads items/npcs for DNDatabase."}, true, false),
	RaidBossStats(11, "Raid Boss Stats", "custom.rbstats.RBStatsManager",
			true,
			"This engine shows Raid Boss information, are they alive or not, "
			+ "if boss is down, engine shows not exact time, when "
			+ "boss will revive, but time stamp between earliest and "
			+ "latest spawn times.",
			new String[] {"//reloadrbstats"},
			new String[] {"Reloads loaded raids for RBStats."},
			true, false),
	CharOptions(12, "custom.Char. Options", "", false,
			"",
			new String[] {},
			new String[] {},
			false, false),
	Achievements(13, "Achievements", "custom.achievements.AchievementsParser", false,
			"This engine allows you to make achievements for players, "
					+ "with different types of requirements and etc.",
			new String[] {"//reloadach"},
			new String[] {"Reloads achievements from XML."},
			true, true),
	EventSystem(13, "EventSystem", "custom.erevents.engine.EventsDataManager", false,
			"Event system.",
			new String[] {},
			new String[] {},
			true, false);
	
	int _id;
	String _name;
	String _classPath;
	boolean _useConfig;
	String _information;
	String _commands[];
	String _commandsInfo[];
	boolean _finished;
	boolean _useBonuses;
	
	private ErEngine(int id, String name, String classPath, boolean useConfig, String information, String commands[], String commandsInfo[], boolean finished, boolean useBonuses)
	{
		_id = id;
		_name = name;
		_classPath = classPath;
		_useConfig = useConfig;
		_information = information;
		_commands = commands;
		_commandsInfo = commandsInfo;
		_finished = finished;
		_useBonuses = useBonuses;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getClassPath()
	{
		return _classPath;
	}
	
	public boolean useConfig()
	{
		return _useConfig;
	}
	
	public String getInformation()
	{
		return _information;
	}
	
	public String[] getCommands()
	{
		return _commands;
	}
	
	public String[] getCommandsInfo()
	{
		return _commandsInfo;
	}
	
	public String getCommand(int id)
	{
		return _commands[id];
	}
	
	public String getCommandInfo(int id)
	{
		return _commandsInfo[id];
	}
	
	public boolean isFinished()
	{
		return _finished;
	}
	
	public boolean useBonuses()
	{
		return _useBonuses;
	}
}