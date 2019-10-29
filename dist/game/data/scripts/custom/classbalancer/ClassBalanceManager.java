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
package custom.classbalancer;

import custom.erengine.ErConfig;
import custom.erengine.ErUtils;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.skills.L2Skill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Erlandys
 */
public class ClassBalanceManager
{
	public static final Logger _log = Logger.getLogger(ClassBalanceManager.class.getName());
	HashMap<Integer, double[]> _balances;
	HashMap<Integer, double[]> _olympiadBalances;
	HashMap<Integer, Integer> _updates;
	HashMap<Integer, Integer> _olympiadUpdates;
	HashMap<Integer, Integer> _secondProffessions;
	HashMap<Boolean, HashMap<Integer, ArrayList<Integer>>> _dataForIngameBalancer;
	ScheduledFuture<?> _updateThread;
	
	public ClassBalanceManager()
	{
		generateTable();
		_balances = new HashMap<>();
		_olympiadBalances = new HashMap<>();
		_updates = new HashMap<>();
		_olympiadUpdates = new HashMap<>();
		_dataForIngameBalancer = new HashMap<>();
		_dataForIngameBalancer.put(true, new HashMap<Integer, ArrayList<Integer>>());
		_dataForIngameBalancer.put(false, new HashMap<Integer, ArrayList<Integer>>());
		AdminCommandHandler.getInstance().registerHandler(new AdminClassBalance());
		loadBalances();
		loadSecondProffessions();
		_updateThread = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> updateBalances(), ErConfig.CLASS_BALANCER_UPDATE_DELAY, ErConfig.CLASS_BALANCER_UPDATE_DELAY);
	}
	
	public void loadBalances()
	{
		_balances.clear();
		_olympiadBalances.clear();
		_dataForIngameBalancer.get(true).clear();
		_dataForIngameBalancer.get(false).clear();
		_updates.clear();
		_olympiadUpdates.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM class_balance"))
		{
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int key = rset.getInt("key");
					int classId = rset.getInt("classId");
					int targetClassId = rset.getInt("targetClassId");
					boolean forOlympiad = rset.getInt("forOlympiad") == 1;
					double values[] =
					{
						rset.getDouble("normal"),
						rset.getDouble("normalCrit"),
						rset.getDouble("magic"),
						rset.getDouble("magicCrit"),
						rset.getDouble("blow"),
						rset.getDouble("physSkill"),
						rset.getDouble("physSkillCrit"),
						classId,
						targetClassId
					};
					if (!forOlympiad)
					{
						_balances.put(key, values);
					}
					else
					{
						_olympiadBalances.put(key, values);
					}
					if (!_dataForIngameBalancer.get(forOlympiad).containsKey(classId))
					{
						_dataForIngameBalancer.get(forOlympiad).put(classId, new ArrayList<Integer>());
					}
					if (targetClassId >= 0)
					{
						if (!_dataForIngameBalancer.get(forOlympiad).containsKey(targetClassId))
						{
							_dataForIngameBalancer.get(forOlympiad).put(targetClassId, new ArrayList<Integer>());
						}
						if (!_dataForIngameBalancer.get(forOlympiad).get(targetClassId).contains(key))
						{
							_dataForIngameBalancer.get(forOlympiad).get(targetClassId).add(key);
						}
					}
					if (!_dataForIngameBalancer.get(forOlympiad).get(classId).contains(key))
					{
						_dataForIngameBalancer.get(forOlympiad).get(classId).add(key);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed loading class balances.", e);
		}
		_log.log(Level.INFO, "Successfully loaded " + (_balances.size() + _olympiadBalances.size()) + " balances.");
	}
	
	public void loadSecondProffessions()
	{
		_secondProffessions = new HashMap<>();
		for (ClassId cId : ClassId.values())
		{
			if (cId.level() < 3)
			{
				continue;
			}
			_secondProffessions.put(cId.getParent().getId(), cId.getId());
		}
	}
	
	public int getClassId(int cId)
	{
		if (!ErConfig.CLASS_BALANCER_AFFECTS_SECOND_PROFFESION)
		{
			return cId;
		}
		if (_secondProffessions.containsKey(cId))
		{
			return _secondProffessions.get(cId);
		}
		return cId;
	}
	
	public double getBalance(int classId, int targetClassId, int type, boolean forOlympiad)
	{
		classId = getClassId(classId);
		targetClassId = getClassId(targetClassId);
		if (!forOlympiad)
		{
			if (_balances.containsKey((classId * 256) + targetClassId))
			{
				return _balances.get((classId * 256) + targetClassId)[type];
			}
		}
		else
		{
			if (_olympiadBalances.containsKey((classId * 256) + targetClassId))
			{
				return _olympiadBalances.get((classId * 256) + targetClassId)[type];
			}
		}
		return 1;
	}
	
	public double[] getBalance(int key, boolean forOlympiad)
	{
		if (!forOlympiad)
		{
			if (_balances.containsKey(key))
			{
				return _balances.get(key);
			}
		}
		else
		{
			if (_olympiadBalances.containsKey(key))
			{
				return _olympiadBalances.get(key);
			}
		}
		return null;
	}
	
	public double getBalanceToAll(int classId, int type, boolean forOlympiad)
	{
		classId = getClassId(classId);
		if (!forOlympiad)
		{
			if (_balances.containsKey(classId * -256))
			{
				return _balances.get(classId * -256)[type];
			}
		}
		else
		{
			if (_olympiadBalances.containsKey(classId * -256))
			{
				return _olympiadBalances.get(classId * -256)[type];
			}
		}
		return 1;
	}
	
	public HashMap<Integer, double[]> getAllBalances(boolean forOlympiad)
	{
		return forOlympiad ? _olympiadBalances : _balances;
	}
	
	public HashMap<Integer, ArrayList<Integer>> getAllBalancesForIngame(boolean forOlympiad)
	{
		return _dataForIngameBalancer.get(forOlympiad);
	}
	
	public ArrayList<Integer> getBalanceForIngame(int classId, boolean forOlympiad)
	{
		classId = getClassId(classId);
		if (_dataForIngameBalancer.get(forOlympiad).containsKey(classId))
		{
			return _dataForIngameBalancer.get(forOlympiad).get(classId);
		}
		return null;
	}
	
	public void updateBalance(int key, int classId, int targetClassId, int type, double value, boolean forOlympiad)
	{
		HashMap<Integer, double[]> balances = forOlympiad ? _olympiadBalances : _balances;
		if (!balances.containsKey(key))
		{
			double[] data =
			{
				1,
				1,
				1,
				1,
				1,
				1,
				1,
				(classId < 0 ? -classId : classId),
				targetClassId
			};
			data[type] = value;
			balances.put(key, data);
			if (!_dataForIngameBalancer.get(forOlympiad).containsKey(classId))
			{
				_dataForIngameBalancer.get(forOlympiad).put(classId, new ArrayList<Integer>());
			}
			if (targetClassId >= 0)
			{
				if (!_dataForIngameBalancer.get(forOlympiad).containsKey(targetClassId))
				{
					_dataForIngameBalancer.get(forOlympiad).put(targetClassId, new ArrayList<Integer>());
				}
				if (!_dataForIngameBalancer.get(forOlympiad).get(targetClassId).contains(key))
				{
					_dataForIngameBalancer.get(forOlympiad).get(targetClassId).add(key);
				}
			}
			if (!_dataForIngameBalancer.get(forOlympiad).get(classId).contains(key))
			{
				_dataForIngameBalancer.get(forOlympiad).get(classId).add(key);
			}
		}
		else
		{
			balances.get(key)[type] = value;
		}
		if (forOlympiad)
		{
			_olympiadUpdates.put(key, 0);
		}
		else
		{
			_updates.put(key, 0);
		}
	}
	
	public void updateBalance(int key, int classId, int targetClassId, double[] values, boolean forOlympiad)
	{
		HashMap<Integer, double[]> balances = forOlympiad ? _olympiadBalances : _balances;
		balances.put(key, values);
		
		if (!_dataForIngameBalancer.get(forOlympiad).containsKey(classId))
		{
			_dataForIngameBalancer.get(forOlympiad).put(classId, new ArrayList<Integer>());
		}
		if (targetClassId >= 0)
		{
			if (!_dataForIngameBalancer.get(forOlympiad).containsKey(targetClassId))
			{
				_dataForIngameBalancer.get(forOlympiad).put(targetClassId, new ArrayList<Integer>());
			}
			if (!_dataForIngameBalancer.get(forOlympiad).get(targetClassId).contains(key))
			{
				_dataForIngameBalancer.get(forOlympiad).get(targetClassId).add(key);
			}
		}
		if (!_dataForIngameBalancer.get(forOlympiad).get(classId).contains(key))
		{
			_dataForIngameBalancer.get(forOlympiad).get(classId).add(key);
		}
		if (forOlympiad)
		{
			_olympiadUpdates.put(key, 0);
		}
		else
		{
			_updates.put(key, 0);
		}
	}
	
	public void removeBalance(int key, int classId, int targetClassId, boolean forOlympiad)
	{
		int rClassId = classId < 0 ? -classId : classId;
		if (!forOlympiad)
		{
			if (_balances.containsKey(key))
			{
				_balances.remove(key);
				_updates.put(key, 1);
			}
		}
		else
		{
			if (_olympiadBalances.containsKey(key))
			{
				_olympiadBalances.remove(key);
				_olympiadUpdates.put(key, 1);
			}
		}
		if (_dataForIngameBalancer.get(forOlympiad).containsKey(rClassId) && _dataForIngameBalancer.get(forOlympiad).get(rClassId).contains(key))
		{
			int i = _dataForIngameBalancer.get(forOlympiad).get(rClassId).indexOf(key);
			_dataForIngameBalancer.get(forOlympiad).get(rClassId).remove(i);
		}
		if (_dataForIngameBalancer.get(forOlympiad).containsKey(targetClassId) && _dataForIngameBalancer.get(forOlympiad).get(targetClassId).contains(key))
		{
			int i = _dataForIngameBalancer.get(forOlympiad).get(targetClassId).indexOf(key);
			_dataForIngameBalancer.get(forOlympiad).get(targetClassId).remove(i);
		}
	}
	
	public void updateBalances()
	{
		_log.info("Class balances updating to database!");
		for (Map.Entry<Integer, Integer> entry : _updates.entrySet())
		{
			if (entry.getValue() == 0)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("REPLACE INTO class_balance (class_balance.key, forOlympiad, normal, normalCrit, magic, magicCrit, blow, physSkill, physSkillCrit, classId, targetClassId) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"))
				{
					double[] data = _balances.get(entry.getKey());
					statement.setInt(1, entry.getKey());
					statement.setInt(2, 0);
					statement.setDouble(3, data[0]);
					statement.setDouble(4, data[1]);
					statement.setDouble(5, data[2]);
					statement.setDouble(6, data[3]);
					statement.setDouble(7, data[4]);
					statement.setDouble(8, data[5]);
					statement.setDouble(9, data[6]);
					statement.setInt(10, (int) data[7]);
					statement.setInt(11, (int) data[8]);
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Could not update class balances[" + entry.getKey() + "]: " + e.getMessage(), e);
				}
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM class_balance WHERE class_balance.key=?"))
				{
					statement.setInt(1, entry.getKey());
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Could not delete class balances[" + entry.getKey() + "]: " + e.getMessage(), e);
				}
			}
		}
		for (Map.Entry<Integer, Integer> entry : _olympiadUpdates.entrySet())
		{
			if (entry.getValue() == 0)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("REPLACE INTO class_balance (class_balance.key, forOlympiad, normal, normalCrit, magic, magicCrit, blow, physSkill, physSkillCrit, classId, targetClassId) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"))
				{
					double[] data = _olympiadBalances.get(entry.getKey());
					statement.setInt(1, entry.getKey());
					statement.setInt(2, 1);
					statement.setDouble(3, data[0]);
					statement.setDouble(4, data[1]);
					statement.setDouble(5, data[2]);
					statement.setDouble(6, data[3]);
					statement.setDouble(7, data[4]);
					statement.setDouble(8, data[5]);
					statement.setDouble(9, data[6]);
					statement.setInt(10, (int) data[7]);
					statement.setInt(11, (int) data[8]);
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Could not update class balances[" + entry.getKey() + "]: " + e.getMessage(), e);
				}
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM class_balance WHERE class_balance.key=?"))
				{
					statement.setInt(1, entry.getKey());
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Could not delete class balances[" + entry.getKey() + "]: " + e.getMessage(), e);
				}
			}
		}
		_updates.clear();
		_olympiadUpdates.clear();
	}
	
	public double getCalcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, double damage)
	{
		if ((attacker instanceof L2PcInstance) || (attacker instanceof L2Summon))
		{
			L2PcInstance player = attacker instanceof L2PcInstance ? attacker.getActingPlayer() : ((L2Summon) attacker).getOwner();
			int playerClassId = getClassId(player.getClassId().getId());
			double vsAll[] = getBalance(playerClassId * -256, false);
			if ((vsAll != null) && (ErConfig.CLASS_BALANCER_AFFECTS_MONSTERS || (target instanceof L2Playable)))
			{
				damage *= vsAll[4];
			}
			if ((target instanceof L2PcInstance) || (target instanceof L2Summon))
			{
				L2PcInstance t = target instanceof L2PcInstance ? target.getActingPlayer() : ((L2Summon) target).getOwner();
				int targetClassId = getClassId(t.getClassId().getId());
				double vsTarget[] = getBalance((playerClassId * 256) + targetClassId, player.isInOlympiadMode());
				if (vsTarget != null)
				{
					damage *= vsTarget[4];
				}
			}
		}
		return damage;
	}
	
	public double getCalcPhysDamage(L2Character attacker, L2Character target, L2Skill skill, double damage, boolean crit)
	{
		if ((attacker instanceof L2PcInstance) || (attacker instanceof L2Summon))
		{
			L2PcInstance player = attacker instanceof L2PcInstance ? attacker.getActingPlayer() : ((L2Summon) attacker).getOwner();
			int playerClassId = getClassId(player.getClassId().getId());
			double vsAll[] = getBalance(playerClassId * -256, false);
			if ((vsAll != null) && (ErConfig.CLASS_BALANCER_AFFECTS_MONSTERS || (target instanceof L2Playable)))
			{
				if ((skill != null) && crit)
				{
					damage *= vsAll[6];
				}
				else if ((skill != null) && !crit)
				{
					damage *= vsAll[5];
				}
				else if ((skill == null) && crit)
				{
					damage *= vsAll[1];
				}
				else if ((skill == null) && !crit)
				{
					damage *= vsAll[0];
				}
			}
			if ((target instanceof L2PcInstance) || (target instanceof L2Summon))
			{
				L2PcInstance t = target instanceof L2PcInstance ? target.getActingPlayer() : ((L2Summon) target).getOwner();
				int targetClassId = getClassId(t.getClassId().getId());
				double vsTarget[] = getBalance((playerClassId * 256) + targetClassId, player.isInOlympiadMode());
				if (vsTarget != null)
				{
					if ((skill != null) && crit)
					{
						damage *= vsTarget[6];
					}
					else if ((skill != null) && !crit)
					{
						damage *= vsTarget[5];
					}
					else if ((skill == null) && crit)
					{
						damage *= vsTarget[1];
					}
					else if ((skill == null) && !crit)
					{
						damage *= vsTarget[0];
					}
				}
			}
		}
		return damage;
	}
	
	public double getCalcMagicDamage(L2Character attacker, L2Character target, L2Skill skill, double damage, boolean mcrit)
	{
		if ((attacker instanceof L2PcInstance) || (attacker instanceof L2Summon))
		{
			L2PcInstance player = attacker instanceof L2PcInstance ? attacker.getActingPlayer() : ((L2Summon) attacker).getOwner();
			int playerClassId = getClassId(player.getClassId().getId());
			double vsAll[] = getBalance(playerClassId * -256, false);
			if ((vsAll != null) && (ErConfig.CLASS_BALANCER_AFFECTS_MONSTERS || (target instanceof L2Playable)))
			{
				damage *= mcrit ? vsAll[3] : vsAll[2];
			}
			if ((target instanceof L2PcInstance) || (target instanceof L2Summon))
			{
				L2PcInstance t = target instanceof L2PcInstance ? target.getActingPlayer() : ((L2Summon) target).getOwner();
				int targetClassId = getClassId(t.getClassId().getId());
				double vsTarget[] = getBalance((playerClassId * 256) + targetClassId, player.isInOlympiadMode());
				if (vsTarget != null)
				{
					damage *= mcrit ? vsTarget[3] : vsTarget[2];
				}
			}
		}
		return damage;
	}
	
	private void generateTable()
	{
		String table = "CREATE TABLE class_balance (\n";
		table += "  `key` int(10) NOT NULL,\n";
		table += "  forOlympiad tinyint(1) NOT NULL DEFAULT 0,\n";
		table += "  normal double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  normalCrit double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  magic double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  magicCrit double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  blow double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  physSkill double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  physSkillCrit double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  classId int(3) NOT NULL DEFAULT -1,\n";
		table += "  targetClassId int(3) NOT NULL DEFAULT -1,\n";
		table += "  PRIMARY KEY (`key`,forOlympiad)\n";
		table += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";
		ErUtils.generateTable("CBalancerTableInitialized", table);
	}
	
	public static ClassBalanceManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClassBalanceManager _instance = new ClassBalanceManager();
	}
}
