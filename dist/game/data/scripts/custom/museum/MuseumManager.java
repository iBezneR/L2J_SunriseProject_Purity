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
package custom.museum;

import custom.erengine.ErBonus;
import custom.erengine.ErGlobalVariables;
import custom.erengine.ErReward;
import custom.erengine.ErUtils;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2MuseumStatueInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Erlandys
 */
public class MuseumManager
{
	public static final Logger _log = Logger.getLogger(MuseumManager.class.getName());
	public static boolean WHOLE_COMMUNITY_BOARD = false;
	public static String COMMUNITY_BOARD_FIRST_TAB = "_bbshome";
	public static boolean SEPARATED_TABS = false;
	public static String COMMUNITY_BOARD_SECOND_TAB = "_bbsfavorite";
	private final HashMap<Integer, String> _categoryNames;
	private final HashMap<Integer, MuseumCategory> _categories;
	private final HashMap<Integer, ArrayList<MuseumCategory>> _categoriesByCategoryId;
	private final HashMap<Integer, ArrayList<Integer>> _playersWithReward;
	private int totalTimeStatuesRefresh = 3600;
	private int refreshTotal = 3600;
	
	public enum RefreshTime
	{
		Total,
		Monthly,
		Weekly,
		Daily,
		Refresh
	}
	
	public MuseumManager()
	{
		_categoryNames = new HashMap<>();
		_categories = new HashMap<>();
		_categoriesByCategoryId = new HashMap<>();
		_playersWithReward = new HashMap<>();
		generateTables();
		generateXMLFile();
		loadCategories();
		long monthlyUpdate = Math.max(100, ErGlobalVariables.getInstance().getLong("museum_monthly") - System.currentTimeMillis());
		long weeklyUpdate = Math.max(100, ErGlobalVariables.getInstance().getLong("museum_weekly") - System.currentTimeMillis());
		long dailyUpdate = Math.max(100, ErGlobalVariables.getInstance().getLong("museum_daily") - System.currentTimeMillis());
		
		AdminCommandHandler.getInstance().registerHandler(new AdminMuseum());
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new UpdateStats(RefreshTime.Refresh), totalTimeStatuesRefresh * 1000, totalTimeStatuesRefresh * 1000);
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new UpdateStats(RefreshTime.Total), refreshTotal * 1000, refreshTotal * 1000);
		ThreadPoolManager.getInstance().scheduleGeneral(new UpdateStats(RefreshTime.Monthly), monthlyUpdate);
		ThreadPoolManager.getInstance().scheduleGeneral(new UpdateStats(RefreshTime.Weekly), weeklyUpdate);
		ThreadPoolManager.getInstance().scheduleGeneral(new UpdateStats(RefreshTime.Daily), dailyUpdate);
		UpdateStats st = new UpdateStats(RefreshTime.Refresh);
		st.run();
	}
	
	public class UpdateStats implements Runnable
	{
		RefreshTime _time;
		
		public UpdateStats(RefreshTime time)
		{
			_time = time;
		}
		
		@Override
		public void run()
		{
			long time = 0;
			
			switch (_time)
			{
				case Monthly:
					Calendar c = Calendar.getInstance();
					c.set(Calendar.MONTH, c.get(Calendar.MONTH) + 1);
					c.set(Calendar.DAY_OF_MONTH, 1);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					time = Math.max(100, c.getTimeInMillis() - System.currentTimeMillis());
					ErGlobalVariables.getInstance().setData("museum_monthly", c.getTimeInMillis());
					ThreadPoolManager.getInstance().scheduleGeneral(new UpdateStats(RefreshTime.Monthly), time);
					break;
				case Weekly:
					c = Calendar.getInstance();
					c.set(Calendar.DAY_OF_WEEK, 2);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					if (c.getTimeInMillis() < System.currentTimeMillis())
					{
						c.setTimeInMillis(c.getTimeInMillis() + 604800000);
					}
					time = Math.max(100, c.getTimeInMillis() - System.currentTimeMillis());
					ErGlobalVariables.getInstance().setData("museum_weekly", c.getTimeInMillis());
					ThreadPoolManager.getInstance().scheduleGeneral(new UpdateStats(_time), time);
					break;
				case Daily:
					c = Calendar.getInstance();
					c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.SECOND, 0);
					time = Math.max(100, c.getTimeInMillis() - System.currentTimeMillis());
					ErGlobalVariables.getInstance().setData("museum_daily", c.getTimeInMillis());
					ThreadPoolManager.getInstance().scheduleGeneral(new UpdateStats(_time), time);
					break;
				default:
					break;
			}
			
			if (!_time.equals(RefreshTime.Refresh))
			{
				cleanLastTops(_time);
			}
			refreshTops();
			restoreLastTops(_time);
		}
		
		void refreshTops()
		{
			for (L2PcInstance player : L2World.getInstance().getPlayers())
			{
				updateDataForChar(player);
				if (!_time.equals(RefreshTime.Refresh))
				{
					player.getMuseumPlayer().resetData(_time);
				}
			}
			refreshTopsFromDatabase(_time);
		}
	}
	
	public void giveRewards()
	{
		if (_playersWithReward.size() == 0)
		{
			return;
		}
		ArrayList<Integer> withReward = new ArrayList<>();
		for (Map.Entry<Integer, ArrayList<Integer>> entry : _playersWithReward.entrySet())
		{
			L2PcInstance player = L2World.getInstance().getPlayer(entry.getKey());
			if ((player == null) || !player.isOnline())
			{
				continue;
			}
			ArrayList<Integer> cats = entry.getValue();
			for (int catId : cats)
			{
				if (!_categories.containsKey(catId))
				{
					withReward.add(entry.getKey());
					continue;
				}
				MuseumCategory cat = _categories.get(catId);
				if (cat == null)
				{
					withReward.add(entry.getKey());
					continue;
				}
				for (ErReward reward : cat.getRewards())
				{
					reward.giveReward(player);
					withReward.add(entry.getKey());
				}
				for (ErBonus bonus : cat.getBonuses())
				{
					player.getPlayerBonuses().addBonus(bonus);
				}
			}
		}
		for (int i : withReward)
		{
			_playersWithReward.remove(i);
		}
	}
	
	public void giveReward(L2PcInstance player)
	{
		if (!_playersWithReward.containsKey(player.getObjectId()))
		{
			return;
		}
		ArrayList<Integer> cats = _playersWithReward.get(player.getObjectId());
		if (cats.size() < 1)
		{
			_playersWithReward.remove(player.getObjectId());
			return;
		}
		for (int catId : cats)
		{
			if (!_categories.containsKey(catId))
			{
				continue;
			}
			MuseumCategory cat = _categories.get(catId);
			for (ErReward reward : cat.getRewards())
			{
				reward.giveReward(player);
			}
			for (ErBonus bonus : cat.getBonuses())
			{
				player.getPlayerBonuses().addBonus(bonus);
			}
		}
		_playersWithReward.remove(player.getObjectId());
	}

	public void restoreLastTops(RefreshTime time)
	{
		for (MuseumCategory cat : getAllCategories().values())
		{
			int i = 1;
			if (!cat.getRefreshTime().equals(time) && !time.equals(RefreshTime.Refresh))
			{
				continue;
			}
			cat.getAllStatuePlayers().clear();
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				 PreparedStatement statement = con.prepareStatement("SELECT * FROM museum_last_statistics as mls INNER JOIN museum_statistics as ms ON ms.objectId=mls.objectId WHERE mls.category = ? AND mls.category = ms.category AND mls.count > 0 AND mls.timer = '" + cat.getRefreshTime().name().toLowerCase() + "' ORDER BY mls.count DESC LIMIT 5"))
			{
				statement.setString(1, cat.getType());
				// Retrieve the L2PcInstance from the characters table of the database
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						int objectId = rset.getInt("objectId");
						String name = rset.getString("name");
						long count = rset.getLong("count");
						cat.getAllStatuePlayers().put(i, new TopPlayer(objectId, name, count));
						if (i == 1)
						{
							spawnStatue(cat);
						}
						i++;
					}
				}
			} catch (Exception e)
			{
				_log.log(Level.SEVERE, "Failed loading character museum data.", e);
			}
		}
	}
	
	public void spawnStatue(MuseumCategory cat)
	{
		for (L2MuseumStatueInstance statue : cat.getAllSpawnedStatues())
		{
			statue.deleteMe();
		}
		cat.getAllSpawnedStatues().clear();
		if (cat.getAllStatuePlayers().size() > 0)
		{
			TopPlayer player = cat.getAllStatuePlayers().get(1);
			for (Location loc : cat.getStatueSpawns())
			{
				L2MuseumStatueInstance statue = new L2MuseumStatueInstance(NpcTable.getInstance().getTemplate(30001), player.getObjectId(), (cat.getCategoryId() * 256) + cat.getTypeId());
				statue.setXYZ(loc.getX(), loc.getY(), loc.getZ());
				statue.setHeading(loc.getHeading());
				statue.spawnMe();
				cat.getAllSpawnedStatues().add(statue);
			}
		}
	}
	
	public void cleanLastTops(RefreshTime time)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM museum_last_statistics WHERE timer='" + time.name().toLowerCase() + "'"))
		{
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store char museum data: " + e.getMessage(), e);
		}
	}
	
	public void refreshTopsFromDatabase(RefreshTime time)
	{
		_playersWithReward.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			for (MuseumCategory cat : _categories.values())
			{
				if (!cat.getRefreshTime().equals(time) && !time.equals(RefreshTime.Refresh))
				{
					continue;
				}
				cat.getAllTops().clear();
				cat.getAllTotalTops().clear();
				cat.getAllStatuePlayers().clear();
				int i = 1;
				int h = 1;
				try (
                        PreparedStatement statement = con.prepareStatement("SELECT * FROM museum_statistics WHERE category = ? AND " + (cat.getRefreshTime().name().toLowerCase()) + "_count > 0 AND is_gm = 0 ORDER BY " + (cat.getRefreshTime().name().toLowerCase()) + "_count DESC LIMIT " + (cat.getRefreshTime().equals(RefreshTime.Total) ? 20 : 10));)
				{
					
					statement.setString(1, cat.getType());
					// Retrieve the L2PcInstance from the characters table of the database
					try (ResultSet rset = statement.executeQuery();)
					{
						while (rset.next())
						{
							int objectId = rset.getInt("objectId");
							String name = rset.getString("name");
							long count = rset.getLong(cat.getRefreshTime().name().toLowerCase() + "_count");
							boolean hasReward = rset.getBoolean("hasReward");
							if (hasReward)
							{
								if (!_playersWithReward.containsKey(objectId))
								{
									_playersWithReward.put(objectId, new ArrayList<Integer>());
								}
								_playersWithReward.get(objectId).add((cat.getCategoryId() * 256) + cat.getTypeId());
							}
							
							if (time.equals(RefreshTime.Total))
							{
								cat.getAllTotalTops().put(i, new TopPlayer(objectId, name, count));
							}
							else if (time.equals(RefreshTime.Refresh))
							{
								if (cat.equals(RefreshTime.Total))
								{
									cat.getAllTotalTops().put(i, new TopPlayer(objectId, name, count));
								}
								else
								{
									cat.getAllTops().put(i, new TopPlayer(objectId, name, count));
								}
							}
							
							if ((i < 6) && time.equals(cat.getRefreshTime()))
							{
								try (PreparedStatement stat = con.prepareStatement("REPLACE museum_last_statistics SET objectId=" + objectId + ", name='" + name + "', category='" + cat.getType() + "', count=" + count + ", timer='" + time.name().toLowerCase() + "';");)
								{
									stat.execute();
									stat.close();
								}
								if (i == 1)
								{
									try (PreparedStatement stat = con.prepareStatement("UPDATE museum_statistics SET hasReward = 1 WHERE objectId = " + objectId + " AND category = '" + cat.getType() + "'");)
									{
										stat.execute();
										stat.close();
									}
									if (!_playersWithReward.containsKey(objectId))
									{
										_playersWithReward.put(objectId, new ArrayList<Integer>());
									}
									_playersWithReward.get(objectId).add((cat.getCategoryId() * 256) + cat.getTypeId());
								}
							}
							i++;
						}
						statement.close();
						rset.close();
					}
				}
				if (!cat.getRefreshTime().equals(RefreshTime.Total))
				{
					try (PreparedStatement statement1 = con.prepareStatement("SELECT * FROM museum_statistics WHERE category = ? AND total_count > 0 AND is_gm = 0 ORDER BY total_count DESC LIMIT 10");)
					{
						statement1.setString(1, cat.getType());
						// Retrieve the L2PcInstance from the characters table of the database
						try (ResultSet rset1 = statement1.executeQuery();)
						{
							while (rset1.next())
							{
								int objectId = rset1.getInt("objectId");
								String name = rset1.getString("name");
								long count = rset1.getLong("total_count");
								cat.getAllTotalTops().put(h, new TopPlayer(objectId, name, count));
								h++;
							}
							statement1.close();
							rset1.close();
						}
					}
				}
			}
			if (!time.equals(RefreshTime.Total) && !time.equals(RefreshTime.Refresh))
			{
				try (PreparedStatement statement = con.prepareStatement("UPDATE museum_statistics SET " + time.name().toLowerCase() + "_count = 0");)
				{
					statement.execute();
					statement.close();
				}
				for (MuseumCategory cat : _categories.values())
				{
					if (!cat.getRefreshTime().equals(time) || cat.equals(RefreshTime.Total))
					{
						continue;
					}
					int i = 1;
					try (
                            PreparedStatement statement = con.prepareStatement("SELECT * FROM museum_statistics WHERE category = ? AND " + (cat.getRefreshTime().name().toLowerCase()) + "_count > 0 AND is_gm = 0 ORDER BY " + (cat.getRefreshTime().name().toLowerCase()) + "_count DESC LIMIT " + (cat.getRefreshTime().equals(RefreshTime.Total) ? 20 : 10));)
					{
						statement.setString(1, cat.getType());
						// Retrieve the L2PcInstance from the characters table of the database
						try (ResultSet rset = statement.executeQuery();)
						{
							while (rset.next())
							{
								int objectId = rset.getInt("objectId");
								String name = rset.getString("name");
								long count = rset.getLong(cat.getRefreshTime().name().toLowerCase() + "_count");
								
								cat.getAllTops().put(i, new TopPlayer(objectId, name, count));
								
								i++;
							}
							rset.close();
							statement.close();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store char museum data: " + e.getMessage(), e);
		}
		restoreLastTops(time);
		giveRewards();
	}
	
	public void loadCategories()
	{
		_log.info(getClass().getSimpleName() + ": Initializing");
		_categoryNames.clear();
		_categories.clear();
		_categoriesByCategoryId.clear();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File("data/MuseumCategories.xml");
		Document doc = null;
		
		if (file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not parse MuseumCategories.xml file: " + e.getMessage(), e);
				return;
			}
			
			int categoryId = 0;
			
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("set"))
				{
					String name = d.getAttributes().getNamedItem("name").getNodeValue();
					String val = d.getAttributes().getNamedItem("val").getNodeValue();
					if (name.equalsIgnoreCase("refreshAllStatisticsIn"))
					{
						refreshTotal = Integer.parseInt(val);
					}
					else if (name.equalsIgnoreCase("WholeCommunityBoard"))
					{
						WHOLE_COMMUNITY_BOARD = val.equalsIgnoreCase("true");
					}
					else if (name.equalsIgnoreCase("SeparateTabs"))
					{
						SEPARATED_TABS = val.equalsIgnoreCase("true");
					}
					else if (name.equalsIgnoreCase("TabInCommunityBoard"))
					{
						COMMUNITY_BOARD_FIRST_TAB = val;
					}
					else if (name.equalsIgnoreCase("SecondTabInCommunityBoard"))
					{
						COMMUNITY_BOARD_SECOND_TAB = val;
					}
					else if (name.equalsIgnoreCase("totalTimeStatuesRefresh"))
					{
						totalTimeStatuesRefresh = Integer.parseInt(val);
					}
				}
				if (d.getNodeName().equalsIgnoreCase("category"))
				{
					ArrayList<MuseumCategory> list = new ArrayList<>();
					String categoryName = d.getAttributes().getNamedItem("name").getNodeValue();
					int typeId = 0;
					for (Node h = d.getFirstChild(); h != null; h = h.getNextSibling())
					{
						if (h.getNodeName().equalsIgnoreCase("type"))
						{
							String typeName = h.getAttributes().getNamedItem("name").getNodeValue();
							String type = h.getAttributes().getNamedItem("type").getNodeValue();
							String refreshTime = h.getAttributes().getNamedItem("refreshTime").getNodeValue();
							boolean timer = false;
							if (h.getAttributes().getNamedItem("timer") != null)
							{
								timer = Boolean.parseBoolean(h.getAttributes().getNamedItem("timer").getNodeValue());
							}
							String additionalText = "";
							if (h.getAttributes().getNamedItem("additionalText") != null)
							{
								additionalText = h.getAttributes().getNamedItem("additionalText").getNodeValue();
							}
							ArrayList<Location> statueSpawns = new ArrayList<>();
							ArrayList<ErReward> rewards = new ArrayList<>();
							ArrayList<ErBonus> bonuses = new ArrayList<>();
							int rewardId = 0;
							int bonusId = 0;
							for (Node a = h.getFirstChild(); a != null; a = a.getNextSibling())
							{
								if (a.getNodeName().equalsIgnoreCase("spawn"))
								{
									int x = Integer.parseInt(a.getAttributes().getNamedItem("x").getNodeValue());
									int y = Integer.parseInt(a.getAttributes().getNamedItem("y").getNodeValue());
									int z = Integer.parseInt(a.getAttributes().getNamedItem("z").getNodeValue());
									int heading = a.getAttributes().getNamedItem("heading") != null ? Integer.parseInt(a.getAttributes().getNamedItem("heading").getNodeValue()) : 0;
									statueSpawns.add(new Location(x, y, z, heading));
								}
								else if (a.getNodeName().equalsIgnoreCase("reward"))
								{
									int rew = ErReward.readReward(a, rewards, rewardId, false);
									if (rew == -1)
									{
										continue;
									}
									rewardId = rew;
								}
								else if (a.getNodeName().equalsIgnoreCase("bonus"))
								{
									int rew = ErBonus.readBonus(a, bonuses, bonusId);
									if (rew == -1)
									{
										continue;
									}
									bonusId = rew;
								}
							}
							int key = (categoryId * 256) + typeId;
							MuseumCategory category = new MuseumCategory(categoryId, typeId, categoryName, typeName, type, refreshTime, timer, additionalText, statueSpawns, rewards, bonuses);
							list.add(category);
							_categories.put(key, category);
							typeId++;
						}
					}
					_categoriesByCategoryId.put(categoryId, list);
					_categoryNames.put(categoryId, categoryName);
					categoryId++;
				}
			}
		}
		_log.info(getClass().getSimpleName() + ": Successfully loaded " + _categoryNames.size() + " categories and " + _categories.size() + " post categories.");
	}
	
	public HashMap<Integer, String> getAllCategoryNames()
	{
		return _categoryNames;
	}
	
	public HashMap<Integer, MuseumCategory> getAllCategories()
	{
		return _categories;
	}
	
	public ArrayList<MuseumCategory> getAllCategoriesByCategoryId(int id)
	{
		if (_categoriesByCategoryId.containsKey(id))
		{
			return _categoriesByCategoryId.get(id);
		}
		return null;
	}
	
	public void restoreDataForChar(L2PcInstance player)
	{
		HashMap<String, long[]> data = new HashMap<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM museum_statistics WHERE objectId = ?");)
		{
			// Retrieve the L2PcInstance from the characters table of the database
			statement.setInt(1, player.getObjectId());
			try (ResultSet rset = statement.executeQuery();)
			{
				while (rset.next())
				{
					long d[] =
					{
						rset.getLong("total_count"),
						rset.getLong("monthly_count"),
						rset.getLong("weekly_count"),
						rset.getLong("daily_count")
					};
					String category = rset.getString("category");
					data.put(category, d);
				}
				statement.close();
				rset.close();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed loading character museum data.", e);
		}
		
		// player.setMuseumPlayer(new MuseumPlayer(player.getObjectId(), player.getName(), data));
		try
		{
			player.getClass().getMethod("setMuseumPlayer", Class.forName("custom.museum.MuseumPlayer")).invoke(player, Class.forName("custom.museum.MuseumPlayer").getConstructor(int.class, String.class, HashMap.class).newInstance(player.getObjectId(), player.getName(), data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void updateDataForChar(L2PcInstance player)
	{
		MuseumPlayer mp = null;
		try
		{
			mp = (MuseumPlayer) player.getClass().getMethod("getMuseumPlayer").invoke(player);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if ((player == null) || (mp == null))
		{
			return;
		}
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE museum_statistics SET objectId=?, name=?, category=?, total_count=?, monthly_count=?, weekly_count=?, daily_count=?, is_gm=?, hasReward=0;");)
		{
			for (Map.Entry<String, long[]> entry : mp.getData().entrySet())
			{
				statement.setInt(1, player.getObjectId());
				statement.setString(2, player.getName());
				statement.setString(3, entry.getKey());
				statement.setLong(4, entry.getValue()[0]);
				statement.setLong(5, entry.getValue()[1]);
				statement.setLong(6, entry.getValue()[2]);
				statement.setLong(7, entry.getValue()[3]);
				statement.setInt(8, player.isGM() ? 1 : 0);
				statement.addBatch();
			}
			statement.executeBatch();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store char museum data: " + e.getMessage(), e);
		}
	}
	
	public void reloadConfigs()
	{
		loadCategories();
		restoreLastTops(RefreshTime.Refresh);
	}
	
	public String parsecmd(String command)
	{
		if (MuseumManager.WHOLE_COMMUNITY_BOARD)
		{
			command = "_bbsmuseum";
		}
		if (MuseumManager.SEPARATED_TABS && command.startsWith(MuseumManager.COMMUNITY_BOARD_FIRST_TAB))
		{
			command = "_bbsmuseum;main";
		}
		if (MuseumManager.SEPARATED_TABS && command.startsWith(MuseumManager.COMMUNITY_BOARD_SECOND_TAB))
		{
			command = "_bbsmuseum;personal";
		}
		if (!MuseumManager.SEPARATED_TABS && command.startsWith(MuseumManager.COMMUNITY_BOARD_FIRST_TAB))
		{
			command = "_bbsmuseum";
		}
		return command;
	}
	
	public String showStatue(MuseumCategory category)
	{
		String html = "";
		html += "<br><br><br><center><table><tr><td width=25></td><td><table border=1 bgcolor=000000><tr><td>";
		html += "<br><center><font color=b7b8b2>" + category.getTypeName() + "</font></center>";
		html += "<table><tr>";
		if (!category.getRefreshTime().equals(RefreshTime.Total))
		{
			html += "<td align=center width=260>";
			html += "<button value=\"" + category.getRefreshTime().name() + " Rankings\" action=\"\" fore=\"L2UI_CH3.FrameBackMid\" back=\"L2UI_CH3.FrameBackMid\" width=\"257\" height=\"20\"/>";
			html += "</td>";
		}
		html += "<td align=center width=" + (category.getRefreshTime().equals(RefreshTime.Total) ? 520 : 260) + ">";
		html += "<button value=\"Total Rankings\" action=\"\" fore=\"L2UI_CH3.FrameBackMid\" back=\"L2UI_CH3.FrameBackMid\" width=\"257\" height=\"20\"/>";
		html += "</td>";
		html += "</tr><tr>";
		// First Row
		if (!category.getRefreshTime().equals(RefreshTime.Total))
		{
			html += "<td align=center width=260>";
			
			for (int i = 0; i < 5; i++)
			{
				String name = "No information.";
				String value = "No information.";
				int cellSpacing = -1;
				if (category.getAllStatuePlayers().size() > i)
				{
					TopPlayer player = category.getAllStatuePlayers().get(i + 1);
					if (player != null)
					{
						name = player.getName();
						long count = player.getCount();
						value = MuseumBBSManager.getInstance().convertToValue(count, category.isTimer(), category.getAdditionalText());
						cellSpacing = (count > 999 ? -3 : -2);
					}
				}
				String bgColor = i == 0 ? "bgcolor=FFFF00" : ((i % 2) == 1 ? "bgcolor=000000" : "");
				String numberColor = i == 0 ? "ffca37" : "dededf";
				String nameColor = i == 0 ? "eac842" : "e2e2e0";
				String valueColor = i == 0 ? "eee79f" : "a78d6c";
				html += "<table width=250 " + bgColor + " height=42><tr>";
				html += "<td width=50 align=center><font color=" + numberColor + " name=ScreenMessageLarge />" + (i < 1 ? "{" + (i + 1) + "}" : (i + 1)) + "</font></td>";
				html += "<td width=200 align=left>";
				html += "<table cellspacing=" + (cellSpacing) + "><tr><td width=200><font color=" + nameColor + " name=ScreenMessageSmall>" + name + "</font></td></tr><tr><td width=200><font color=" + valueColor + " name=ScreenMessageSmall>" + value + "</font></td></tr></table>";
				html += "<img src=\"L2UI.SquareBlank\" width=1 height=5/></td>";
				html += "";
				html += "</tr></table><img src=\"L2UI.SquareGray\" width=250 height=1/>";
			}
			
			html += "</td>";
		}
		// Second Row
		html += "<td align=center width=" + (category.getRefreshTime().equals(RefreshTime.Total) ? 520 : 260) + ">";
		
		for (int i = 0; i < 5; i++)
		{
			String name = "No information.";
			String value = "No information.";
			int cellSpacing = -1;
			if (category.getAllTotalTops().size() > i)
			{
				TopPlayer player = category.getAllTotalTops().get(i + 1);
				if (player != null)
				{
					name = player.getName();
					long count = player.getCount();
					value = MuseumBBSManager.getInstance().convertToValue(count, category.isTimer(), category.getAdditionalText());
					cellSpacing = (count > 999 ? -3 : -2);
				}
			}
			String bgColor = i == 0 ? "bgcolor=FFFF00" : ((i % 2) == 1 ? "bgcolor=000000" : "");
			String numberColor = i == 0 ? "ffca37" : "dededf";
			String nameColor = i == 0 ? "eac842" : "e2e2e0";
			String valueColor = i == 0 ? "eee79f" : "a78d6c";
			html += "<table width=250 " + bgColor + " height=42><tr>";
			html += "<td width=50 align=center><font color=" + numberColor + " name=ScreenMessageLarge />" + (i < 1 ? "{" + (i + 1) + "}" : (i + 1)) + "</font></td>";
			html += "<td width=200 align=left>";
			html += "<table cellspacing=" + (cellSpacing) + "><tr><td width=200><font color=" + nameColor + " name=ScreenMessageSmall>" + name + "</font></td></tr><tr><td width=200><font color=" + valueColor + " name=ScreenMessageSmall>" + value + "</font></td></tr></table>";
			html += "<img src=\"L2UI.SquareBlank\" width=1 height=5/></td>";
			html += "";
			html += "</tr></table><img src=\"L2UI.SquareGray\" width=250 height=1/>";
		}
		html += "</td>";
		html += "</tr></table><br><br></td></tr></table></td></tr></table>";
		html += "</center>";
		return html;
	}
	
	private void generateTables()
	{
		String text = "";
		text += "CREATE TABLE museum_statistics (\n";
		text += "  objectId int(10) unsigned NOT NULL DEFAULT 0,\n";
		text += "  `name` varchar(50) NOT NULL,\n";
		text += "  category varchar(30) NOT NULL,\n";
		text += "  monthly_count bigint(13) NOT NULL DEFAULT 0,\n";
		text += "  weekly_count bigint(13) NOT NULL DEFAULT 0,\n";
		text += "  daily_count bigint(13) NOT NULL DEFAULT 0,\n";
		text += "  total_count bigint(13) NOT NULL DEFAULT 0,\n";
		text += "  hasReward smallint(1) unsigned NOT NULL DEFAULT 0,\n";
		text += "  is_gm smallint(1) NOT NULL DEFAULT 0,\n";
		text += "  PRIMARY KEY (objectId,category)\n";
		text += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";
		String text1 = "";
		text1 += "CREATE TABLE museum_last_statistics (\n";
		text1 += "  objectId int(10) NOT NULL DEFAULT 0,\n";
		text1 += "  `name` varchar(50) NOT NULL,\n";
		text1 += "  category varchar(50) NOT NULL,\n";
		text1 += "  timer varchar(15) NOT NULL,\n";
		text1 += "  count bigint(13) NOT NULL DEFAULT 0,\n";
		text1 += "  PRIMARY KEY (objectId,category,timer)\n";
		text1 += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";
		ErUtils.generateTables("MuseumTablesInitialised", text, text1);
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
	
	private void generateXMLFile()
	{
		if (ErGlobalVariables.getInstance().getBoolean("MuseumXMLInitialized"))
		{
			return;
		}
		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		text += "<list xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xxx.xsd\">\n";
		text += "	<!-- Also available to use from npc (L2MuseumInstance), but SeparatedTabs must be turned off, because in html certain tabs will not appear. -->\n";
		text += "	<!-- Allows museum to be in all community board tabs. -->\n";
		text += "	<set name=\"WholeCommunityBoard\" val=\"false\" />\n";
		text += "	<!-- If museum is not in all tabs, then select the tab in which it is. -->\n";
		text += "	<set name=\"TabInCommunityBoard\" val=\"_bbshome\" />\n";
		text += "	<!-- If museum is not in all tabs, this allows personal and rankings data to be in separated tabs. -->\n";
		text += "	<set name=\"SeparateTabs\" val=\"true\" />\n";
		text += "	<!-- If museum is in separated tabs, then select second tab (for personal data). -->\n";
		text += "	<set name=\"SecondTabInCommunityBoard\" val=\"_bbsloc\" />\n";
		text += "	<!-- Time in seconds, when rankings are refreshed. -->\n";
		text += "	<set name=\"TotalTimeStatuesRefresh\" val=\"3600\" />\n";
		text += "	<!-- Time in seconds, when total type rankings are refreshed and selected new statues for total time. -->\n";
		text += "	<set name=\"RefreshAllStatisticsIn\" val=\"3600\" />\n";
		text += "	\n";
		text += "	<!-- \n";
		text += "	Category:\n";
		text += "		name = is the name of the category, shown in the left side, both in personal and ranking datas.\n";
		text += "	Type:\n";
		text += "		name = is the name shown in there, below the category (it means the type will be under that category which is above).\n";
		text += "		type = this is the type which allows engine to detect which thing to rank and calculate, available types:\n";
		text += "			xp / adena / play_duration / private_store_sales / quests_clear / monster_kills / monster_kill_xp / monster_deats / raid_kill_xxx (instead of xxx, write raid/grand boss id), pvp_victories, pvp_defeats, pk_victories, pk_defeats\n";
		text += "	 	refreshTime = this detects the refresh timing:\n";
		text += "	 		monthly - refreshes every months 1st day at 00:00\n";
		text += "	 		weekly - refreshes every Monday at 00:00\n";
		text += "	 		daily - refreshes every day at 00:00\n";
		text += "	 		total - refreshes x time run from server start (using RefreshAllStatisticsIn config above)\n";
		text += "	 	additionalText = ability to add certain word to collected variable, like adding \"Round(s)\", instead of writing \"PvP: 45\", will write \"PvP: 45 Round(s)\"\n";
		text += "	 	timer = true / false. Enable or disable turning normal numbers into time, like play_duration, instead of 300, will write: 5 mins.\n";
		text += "	 Spawn:\n";
		text += "	 	allows to spawn statue of certain type, in all of locations (multiple spawns for one categorie available)\n";
		text += "	 	x / y / z / heading - coordinates\n";
		text += "	 Reward:\n";
		text += "	 	allows to give reward for winner of period (monthly/weekly/daily/total), the reward must be taken until next period change.\n";
		text += "	 	type = Item / Experience / SkillPoints / ClanPoints\n";
		text += "	 	id (only for Item type) = item ID\n";
		text += "	 	minCount = is minimum count of giving reward\n";
		text += "	 	maxCount = is maximum count of giving reward\n";
		text += "	 	chance = 1-100 is the chance for reward being given.\n";
		text += "	 Bonus:\n";
		text += "	 	allows to give bonus for winner of period (monthly/weekly/daily/total), the bonus must be taken until next period change.\n";
		text += "	 	type = Item / Experience / SkillPoints / ClanPoints\n";
		text += "	 	id (only for Item type) = item ID\n";
		text += "	 	min = is minimum count of bonus (percent/value)\n";
		text += "	 	max = is maximum count of bonus (percent/value)\n";
		text += "	 	chance = 1-100 is the chance for bonus being given\n";
		text += "	 	itemBonusType (only for Item type) = drop (increases drop amount) / chance (increases chance to drop)\n";
		text += "	 	addingType = is the type of how the addition will be calculated - percent (will be increased by percent - 50% by 50%, will be 75%) / amount (will be increased by value - 50% by 50, will be 100%)\n";
		text += "	 	time = the amount of time (in seconds), bonus will be given for player\n";
		text += "	 -->\n";
		text += "	<category name=\"General\">\n";
		text += "		<type name=\"Acquired XP\" type=\"xp\" refreshTime=\"weekly\">\n";
		text += "			<spawn x=\"1000\" y=\"1000\" z=\"1000\" heading=\"1000\" />\n";
		text += "		</type>\n";
		text += "		<type name=\"Acquired Adena\" type=\"adena\" refreshTime=\"monthly\"/>\n";
		text += "		<type name=\"Play Duration\" type=\"play_duration\" refreshTime=\"daily\" timer=\"true\">\n";
		text += "			<spawn x=\"28529\" y=\"10573\" z=\"-4234\" heading=\"1000\" />\n";
		text += "		</type>\n";
		text += "		<type name=\"Private Store Sales\" type=\"private_stores_sales\" refreshTime=\"weekly\" additionalText=\"Round(s)\"/>\n";
		text += "		<type name=\"Quests Clear\" type=\"quests_clear\" refreshTime=\"weekly\" additionalText=\"Round(s)\"/>\n";
		text += "	</category>\n";
		text += "	<category name=\"Hunting Grounds\">\n";
		text += "		<type name=\"Number of Monster Killings\" type=\"monster_kills\" refreshTime=\"total\" additionalText=\"Round(s)\" >\n";
		text += "			<spawn x=\"28892\" y=\"11029\" z=\"-4233\" heading=\"1000\" />\n";
		text += "			<reward type=\"Experience\" min=\"500000\" max=\"1000000\" chance=\"100\"/>\n";
		text += "			<reward type=\"ClanPoints\" min=\"500\" max=\"1000\" chance=\"100\" />\n";
		text += "			<reward type=\"SkillPoints\" min=\"500000\" max=\"1000000\" chance=\"100\"/>\n";
		text += "			<reward id=\"57\" type=\"Item\" min=\"30\" max=\"40\" chance=\"100\" party=\"true\"/>\n";
		text += "			\n";
		text += "			<bonus id=\"57\" type=\"Item\" itemBonusType=\"drop\" addingType=\"percent\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\"/>\n";
		text += "			<bonus id=\"57\" type=\"Item\" itemBonusType=\"chance\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\"/>\n";
		text += "			\n";
		text += "			<bonus type=\"Experience\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\"/>\n";
		text += "			<bonus type=\"SkillPoints\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\"/>\n";
		text += "			<bonus type=\"ClanPoints\" addingType=\"amount\" min=\"10\" max=\"10\" chance=\"100\" time=\"3600\"/>\n";
		text += "		</type>\n";
		text += "		<type name=\"Monster Kill XP\" type=\"monster_kill_xp\" refreshTime=\"daily\" />\n";
		text += "		<type name=\"Number of Deaths by Monsters\" type=\"monster_deaths\" refreshTime=\"total\" additionalText=\"Round(s)\" />\n";
		text += "	</category>\n";
		text += "	<category name=\"Raid\">\n";
		text += "		<type name=\"Baium\" type=\"raid_kill_22222\" refreshTime=\"total\" additionalText=\"Round(s)\" />\n";
		text += "		<type name=\"Antharas\" type=\"raid_kill_22222\" refreshTime=\"total\" additionalText=\"Round(s)\" />\n";
		text += "		<type name=\"Valakas\" type=\"raid_kill_22222\" refreshTime=\"total\" additionalText=\"Round(s)\" />\n";
		text += "	</category>\n";
		text += "	<category name=\"PVP\">\n";
		text += "		<type name=\"PK Victory Count\" type=\"pk_victories\" refreshTime=\"total\" additionalText=\"People\" />\n";
		text += "		<type name=\"PvP Victory Count\" type=\"pvp_victories\" refreshTime=\"total\" additionalText=\"People\" />\n";
		text += "		<type name=\"PK Defeat Count\" type=\"pk_defeats\" refreshTime=\"total\" additionalText=\"Round(s)\" />\n";
		text += "		<type name=\"PvP Defeat Count\" type=\"pvp_defeats\" refreshTime=\"total\" additionalText=\"Round(s)\" />\n";
		text += "	</category>\n";
		text += "</list>\n";
		
		ErGlobalVariables.getInstance().setData("MuseumXMLInitialized", true);
		ErUtils.generateFile("data/", "MuseumCategories", ".xml", text);
	}
	
	public static MuseumManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MuseumManager _instance = new MuseumManager();
	}
}
