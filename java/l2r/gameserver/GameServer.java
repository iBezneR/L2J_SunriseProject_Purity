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
package l2r.gameserver;

import com.l2jserver.mmocore.SelectorThread;
import custom.erengine.ErUtils;
import gr.sr.configsEngine.ConfigsController;
import gr.sr.dressmeEngine.DressMeLoader;
import gr.sr.interf.SunriseEvents;
import gr.sr.main.PlayerValues;
import gr.sr.main.SunriseInfo;
import gr.sr.main.SunriseServerMods;
import gr.sr.network.handler.AbstractPacketHandler;
import gr.sr.network.handler.ServerTypeConfigs;
import gr.sr.network.handler.types.ServerType;
import l2r.*;
import l2r.features.auctionEngine.managers.AuctionHouseManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.communitybbs.SunriseBoards.dropCalc.DropCalculatorConfigs;
import l2r.gameserver.communitybbs.SunriseBoards.dropCalc.DropInfoHandler;
import l2r.gameserver.custom.GiftCodesManager;
import l2r.gameserver.dao.factory.impl.DAOFactory;
import l2r.gameserver.data.EventDroplist;
import l2r.gameserver.data.SpawnTable;
import l2r.gameserver.data.sql.*;
import l2r.gameserver.data.xml.impl.*;
import l2r.gameserver.handler.EffectHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.*;
import l2r.gameserver.instancemanager.petition.PetitionManager;
import l2r.gameserver.model.AutoSpawnHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.PartyMatchRoomList;
import l2r.gameserver.model.PartyMatchWaitingList;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.events.EventDispatcher;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.pathfinding.PathFinding;
import l2r.gameserver.script.faenor.FaenorScriptEngine;
import l2r.gameserver.scripting.L2ScriptEngineManager;
import l2r.gameserver.taskmanager.KnownListUpdateTaskManager;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.status.Status;
import l2r.util.DeadLockDetector;
import l2r.util.IPv4Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Calendar;
import java.util.logging.LogManager;

public class GameServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final AbstractPacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public AbstractPacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.error(getClass().getSimpleName() + ": Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		EventDispatcher.getInstance();
		
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		
		if (Config.PATHFINDING > 0)
		{
			PathFinding.getInstance();
		}
		
		printSection("World");
		// start game time control early
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		AnnouncementsTable.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Data");
		ActionData.getInstance();
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();
		
		printSection("Effects");
		EffectHandler.getInstance().executeScript();
		
		printSection("Enchant Skill Groups");
		EnchantSkillGroupsData.getInstance();
		
		printSection("Skill Trees");
		SkillTreesData.getInstance();
		
		printSection("Skills");
		SkillData.getInstance();
		SkillIconData.getInstance();

		printSection("Items");
		ItemData.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		OptionData.getInstance();
		EnchantItemHPBonusData.getInstance();
		MerchantPriceConfigData.getInstance().loadInstances();
		BuyListData.getInstance();
		MultisellData.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();
		
		printSection("Product Items");
		ProductItemData.getInstance();
		
		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceData.getInstance();
		PlayerXpPercentLostData.getInstance();
		KarmaData.getInstance();
		HitConditionBonusData.getInstance();
		PlayerTemplateData.getInstance();
		CharNameTable.getInstance();
		AdminData.getInstance();
		RaidBossPointsManager.getInstance();
		PetData.getInstance();
		CharSummonTable.getInstance().init();
		
		printSection("Clans");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("NPCs");
		SkillLearnData.getInstance();
		NpcTable.getInstance();
		WalkingManager.getInstance();
		StaticObjectsData.getInstance();
		ZoneManager.getInstance();
		DoorData.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		GrandBossManager.getInstance().initZones();
		EventDroplist.getInstance();
		
		printSection("Auction Manager");
		ItemAuctionManager.getInstance();
		
		printSection("Olympiad");
		if (Config.ENABLE_OLYMPIAD)
		{
			Olympiad.getInstance();
			Hero.getInstance();
		}
		else
		{
			_log.info("Olympiad is disabled by config.");
		}
		
		printSection("Seven Signs");
		SevenSigns.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleportLocationTable.getInstance();
		PlayerValues.checkPlayers();
		UIData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		BotReportTable.getInstance();
		
		printSection("Scripts");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		
		try
		{
			printSection("Datapack Scripts");
			if (!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
			{
				L2ScriptEngineManager.getInstance().executeScriptList(new File(Config.DATAPACK_ROOT, "data/scripts.ini"));
			}
		}
		catch (IOException ioe)
		{
			_log.error(getClass().getSimpleName() + ": Failed loading scripts.ini, scripts are not going to be loaded!");
		}
		
		printSection("Gracia Seeds");
		SoDManager.getInstance();
		SoIManager.getInstance();
		
		printSection("Spawns");
		SpawnTable.getInstance().load();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		RaidBossSpawnManager.getInstance();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		FortSiegeManager.getInstance();
		SiegeScheduleData.getInstance();
		
		MerchantPriceConfigData.getInstance().updateReferences();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		
		QuestManager.getInstance().report();
		
		printSection("Others");
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		TaskManager.getInstance();
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		PunishmentManager.getInstance();
		
		// Sunrise systems section
		printSection("Event Engine");
		SunriseEvents.start();
		
		printSection("Sunrise Systems");
		SunriseServerMods.getInstance().checkSunriseMods();
		
		if (DropCalculatorConfigs.ENABLE_DROP_CALCULATOR)
		{
			DropInfoHandler.getInstance().load();
		}

		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		ErUtils.getInstance();

		// Auction House Manager
		AuctionHouseManager.getInstance();
		// DressMe Load
		DressMeLoader.load();

		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		KnownListUpdateTaskManager.getInstance();
		
		GiftCodesManager.getInstance();

		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.getInstance().restoreOfflineTraders();
		}
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the allocation pool
		long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info(getClass().getSimpleName() + ": Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		Toolkit.getDefaultToolkit().beep();
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		InetAddress serverAddr = Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*") ? null : InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
		
		_gamePacketHandler = ServerType.getPacketHandler(ServerTypeConfigs.SERVER_TYPE);
		_selectorThread = new SelectorThread<>(Config.SELECTOR_CONFIG, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		_selectorThread.openServerSocket(serverAddr, Config.PORT_GAME);
		_selectorThread.start();
		_log.info(getClass().getSimpleName() + ": is now listening on: " + Config.GAMESERVER_HOSTNAME + ":" + Config.PORT_GAME);
		
		_log.info("Maximum Numbers of Connected players: " + Config.MAXIMUM_ONLINE_USERS);
		_log.info("Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		
		SunriseInfo.load();
		printSection("UPnP");
		UPnPService.getInstance();
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 61)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
	
	@SuppressWarnings("resource")
	private static void checkFreePorts()
	{
		boolean binded = false;
		while (!binded)
		{
			try
			{
				ServerSocket ss;
				if (Config.GAMESERVER_HOSTNAME.equalsIgnoreCase("*"))
				{
					ss = new ServerSocket(Config.PORT_GAME);
				}
				else
				{
					ss = new ServerSocket(Config.PORT_GAME, 50, InetAddress.getByName(Config.GAMESERVER_HOSTNAME));
				}
				ss.close();
				binded = true;
			}
			catch (Exception e)
			{
				_log.warn("Port " + Config.PORT_GAME + " is already binded. Please free it and restart server.");
				binded = false;
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e2)
				{
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load();
		ServerTypeConfigs.getInstance().loadConfigs();
		FloodProtectorsConfig.load();
		// Sunrise configs load section
		DropCalculatorConfigs.getInstance().loadConfigs();
		ConfigsController.getInstance().reloadSunriseConfigs();
		// Check binding address
		checkFreePorts();
		_log.info("Sunrise Configs Loaded...");
		
		printSection("Database");
		DAOFactory.getInstance();
		Class.forName(Config.DATABASE_DRIVER).newInstance();
		L2DatabaseFactory.getInstance().getConnection().close();
		
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info(GameServer.class.getSimpleName() + ": Telnet server is currently disabled.");
		}
	}
}
