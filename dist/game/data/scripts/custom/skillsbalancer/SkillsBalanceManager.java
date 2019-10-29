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
package custom.skillsbalancer;

import custom.erengine.ErConfig;
import custom.erengine.ErUtils;
import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.SkillData;
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
public class SkillsBalanceManager
{
	public static final Logger _log = Logger.getLogger(SkillsBalanceManager.class.getName());
	
	ScheduledFuture<?> _updateThread;
	HashMap<Integer, double[]> _olympiadBalances;
	HashMap<Integer, double[]> _balances;
	HashMap<Integer, Integer> _secondProffessions;
	HashMap<Boolean, HashMap<Integer, ArrayList<Integer>>> _dataForIngameBalancer;
	HashMap<Integer, Integer> _updates;
	HashMap<Integer, Integer> _olympiadUpdates;
	HashMap<Boolean, HashMap<Integer, String>> _usedSkillNames;
	
	public SkillsBalanceManager()
	{
		generateTable();
		_balances = new HashMap<>();
		_olympiadBalances = new HashMap<>();
		_updates = new HashMap<>();
		_olympiadUpdates = new HashMap<>();
		_dataForIngameBalancer = new HashMap<>();
		_dataForIngameBalancer.put(true, new HashMap<Integer, ArrayList<Integer>>());
		_dataForIngameBalancer.put(false, new HashMap<Integer, ArrayList<Integer>>());
		_usedSkillNames = new HashMap<>();
		_usedSkillNames.put(true, new HashMap<Integer, String>());
		_usedSkillNames.put(false, new HashMap<Integer, String>());
		AdminCommandHandler.getInstance().registerHandler(new AdminSkillsBalance());
		loadSecondProffessions();
		loadBalances();
		_updateThread = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable() {@Override public void run() {updateBalances(); }}, ErConfig.SKILLS_BALANCER_UPDATE_DELAY, ErConfig.SKILLS_BALANCER_UPDATE_DELAY);
	}
	
	public void loadBalances()
	{
		_log.log(Level.INFO, "Initializing skills balancer...");
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM skills_balance"))
		{
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int key = rset.getInt("key");
					int skillId = rset.getInt("skillId");
					int targetClassId = rset.getInt("targetClassId");
					boolean forOlympiad = rset.getInt("forOlympiad") == 1;
					double values[] =
					{
						rset.getDouble("chance"),
						rset.getDouble("power"),
						skillId,
						targetClassId
					};
					if (forOlympiad)
					{
						_olympiadBalances.put(key, values);
					}
					else
					{
						_balances.put(key, values);
					}
					if (!_dataForIngameBalancer.get(forOlympiad).containsKey(skillId))
					{
						_dataForIngameBalancer.get(forOlympiad).put(skillId, new ArrayList<Integer>());
					}
					if (!_dataForIngameBalancer.get(forOlympiad).get(skillId).contains(key))
					{
						_dataForIngameBalancer.get(forOlympiad).get(skillId).add(key);
					}
					L2Skill sk = SkillData.getInstance().getInfo(skillId, 1);
					if (sk != null)
					{
						_usedSkillNames.get(forOlympiad).put(skillId, sk.getName());
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Failed loading skills balances.", e);
		}
		_log.log(Level.INFO, "Successfully loaded " + (_balances.size() + _olympiadBalances.size()) + " skills balances.");
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
		if (!ErConfig.SKILLS_BALANCER_AFFECTS_SECOND_PROFFESION)
		{
			return cId;
		}
		if (_secondProffessions.containsKey(cId))
		{
			return _secondProffessions.get(cId);
		}
		return cId;
	}
	
	public double getBalance(int skillId, int classId, int type, boolean forOlympiad)
	{
		classId = getClassId(classId);
		if (!forOlympiad)
		{
			if (_balances.containsKey((skillId * (classId < 0 ? -1 : 1)) + (classId * 65536)))
			{
				return _balances.get((skillId * (classId < 0 ? -1 : 1)) + (classId * 65536))[type];
			}
		}
		else
		{
			if (_olympiadBalances.containsKey((skillId * (classId < 0 ? -1 : 1)) + (classId * 65536)))
			{
				return _olympiadBalances.get((skillId * (classId < 0 ? -1 : 1)) + (classId * 65536))[type];
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
			if (_balances.containsKey(classId * -1))
			{
				return _balances.get(classId * -1)[type];
			}
		}
		else
		{
			if (_olympiadBalances.containsKey(classId * -1))
			{
				return _olympiadBalances.get(classId * -1)[type];
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
	
	public ArrayList<Integer> getBalanceForIngame(int skillId, boolean forOlympiad)
	{
		if (_dataForIngameBalancer.get(forOlympiad).containsKey(skillId))
		{
			return _dataForIngameBalancer.get(forOlympiad).get(skillId);
		}
		return null;
	}
	
	public void updateBalance(int key, int skillId, int targetClassId, int type, double value, boolean forOlympiad)
	{
		HashMap<Integer, double[]> balances = forOlympiad ? _olympiadBalances : _balances;
		if (!balances.containsKey(key))
		{
			double[] data =
			{
				1,
				1,
				(skillId < 0 ? -skillId : skillId),
				targetClassId
			};
			data[type] = value;
			balances.put(key, data);
			if (!_dataForIngameBalancer.get(forOlympiad).containsKey(skillId))
			{
				_dataForIngameBalancer.get(forOlympiad).put(skillId, new ArrayList<Integer>());
			}
			if (!_dataForIngameBalancer.get(forOlympiad).get(skillId).contains(key))
			{
				_dataForIngameBalancer.get(forOlympiad).get(skillId).add(key);
			}
			if (!_usedSkillNames.containsKey(skillId))
			{
				L2Skill sk = SkillData.getInstance().getInfo(skillId, 1);
				if (sk != null)
				{
					_usedSkillNames.get(forOlympiad).put(skillId, sk.getName());
				}
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
	
	public void updateBalance(int key, int skillId, int targetClassId, double[] values, boolean forOlympiad)
	{
		HashMap<Integer, double[]> balances = forOlympiad ? _olympiadBalances : _balances;
		balances.put(key, values);
		
		if (!_dataForIngameBalancer.get(forOlympiad).containsKey(skillId))
		{
			_dataForIngameBalancer.get(forOlympiad).put(skillId, new ArrayList<Integer>());
		}
		if (!_dataForIngameBalancer.get(forOlympiad).get(skillId).contains(key))
		{
			_dataForIngameBalancer.get(forOlympiad).get(skillId).add(key);
		}
		if (!_usedSkillNames.containsKey(skillId))
		{
			L2Skill sk = SkillData.getInstance().getInfo(skillId, 1);
			if (sk != null)
			{
				_usedSkillNames.get(forOlympiad).put(skillId, sk.getName());
			}
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
	
	public void removeBalance(int key, int skillId, int targetClassId, boolean forOlympiad)
	{
		int rSkillId = skillId < 0 ? -skillId : skillId;
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
		if (_dataForIngameBalancer.get(forOlympiad).containsKey(rSkillId) && _dataForIngameBalancer.get(forOlympiad).get(rSkillId).contains(key))
		{
			int i = _dataForIngameBalancer.get(forOlympiad).get(rSkillId).indexOf(key);
			_dataForIngameBalancer.get(forOlympiad).get(rSkillId).remove(i);
		}
		if (_dataForIngameBalancer.get(forOlympiad).containsKey(rSkillId) && (_dataForIngameBalancer.get(forOlympiad).get(rSkillId).size() < 1))
		{
			_dataForIngameBalancer.get(forOlympiad).remove(rSkillId);
		}
		if (_dataForIngameBalancer.get(forOlympiad).containsKey(rSkillId))
		{
			_usedSkillNames.remove(skillId);
		}
	}
	
	public HashMap<Integer, String> getSkillNames(boolean forOlympiad)
	{
		return _usedSkillNames.get(forOlympiad);
	}
	
	public ArrayList<Integer> getSkillsByName(boolean forOlympiad, String name, int classId)
	{
		ArrayList<Integer> skills = new ArrayList<>();
		name = name.toLowerCase();
		for (Map.Entry<Integer, String> entry : _usedSkillNames.get(forOlympiad).entrySet())
		{
			if (entry.getValue().toLowerCase().contains(name))
			{
				skills.add(entry.getKey());
			}
		}
		ArrayList<Integer> usedSkills = new ArrayList<>();
		for (int skillId : skills)
		{
			if (classId >= 0)
			{
				int key = (skillId * (classId < 0 ? -1 : 1)) + (classId * 65536);
				ArrayList<Integer> keys = _dataForIngameBalancer.get(forOlympiad).get(skillId);
				if (keys.contains(key))
				{
					usedSkills.add(key);
				}
			}
			else
			{
				if (_dataForIngameBalancer.get(forOlympiad).containsKey(skillId))
				{
					usedSkills.addAll(_dataForIngameBalancer.get(forOlympiad).get(skillId));
				}
			}
		}
		
		return usedSkills;
	}
	
	public ArrayList<Integer> getUsedSkillsById(boolean forOlympiad, int skillId, int classId)
	{
		if (!_dataForIngameBalancer.get(forOlympiad).containsKey(skillId))
		{
			return null;
		}
		if (classId == -1)
		{
			return _dataForIngameBalancer.get(forOlympiad).get(skillId);
		}
		int key = (skillId * (classId < 0 ? -1 : 1)) + (classId * 65536);
		if (_dataForIngameBalancer.get(forOlympiad).get(skillId).contains(key))
		{
			ArrayList<Integer> r = new ArrayList<>();
			r.add(key);
			return r;
		}
		return null;
	}
	
	public void updateBalances()
	{
		_log.info("Skills balances updating to database!");
		for (Map.Entry<Integer, Integer> entry : _updates.entrySet())
		{
			if (entry.getValue() == 0)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("REPLACE INTO skills_balance (skills_balance.key, forOlympiad, power, chance, skillId, targetClassId) values (?, ?, ?, ?, ?, ?)"))
				{
					double[] data = _balances.get(entry.getKey());
					statement.setInt(1, entry.getKey());
					statement.setInt(2, 0);
					statement.setDouble(3, data[0]);
					statement.setDouble(4, data[1]);
					statement.setInt(5, (int) data[2]);
					statement.setInt(6, (int) data[3]);
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Could not update skill balances[" + entry.getKey() + "]: " + e.getMessage(), e);
				}
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM skills_balance WHERE skills_balance.key=?"))
				{
					statement.setInt(1, entry.getKey());
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Could not delete skill balances[" + entry.getKey() + "]: " + e.getMessage(), e);
				}
			}
		}
		for (Map.Entry<Integer, Integer> entry : _olympiadUpdates.entrySet())
		{
			if (entry.getValue() == 0)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("REPLACE INTO skills_balance (skills_balance.key, forOlympiad, power, chance, skillId, targetClassId) values (?, ?, ?, ?, ?, ?)"))
				{
					double[] data = _olympiadBalances.get(entry.getKey());
					statement.setInt(1, entry.getKey());
					statement.setInt(2, 1);
					statement.setDouble(3, data[0]);
					statement.setDouble(4, data[1]);
					statement.setInt(5, (int) data[2]);
					statement.setInt(6, (int) data[3]);
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Could not update skill balances[" + entry.getKey() + "]: " + e.getMessage(), e);
				}
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM skills_balance WHERE skills_balance.key=?"))
				{
					statement.setInt(1, entry.getKey());
					statement.executeUpdate();
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "Could not delete skill balances[" + entry.getKey() + "]: " + e.getMessage(), e);
				}
			}
		}
		_updates.clear();
		_olympiadUpdates.clear();
	}

	public double getCalcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, double damage) {
		if (skill != null) {
			int skillId = skill.getId();
			double svsAll[] = getBalance((skillId * -1) - 65536, false);
			if ((svsAll != null) && (ErConfig.SKILLS_BALANCER_AFFECTS_MONSTERS || (target instanceof L2Playable)))
			{
				damage *= svsAll[1];
			}
			if ((target instanceof L2PcInstance) || (target instanceof L2Summon))
			{
				L2PcInstance t = target instanceof L2PcInstance ? target.getActingPlayer() : ((L2Summon) target).getOwner();
				int targetClassId = getClassId(t.getClassId().getId());
				double vsTarget[] = getBalance(skillId + (targetClassId * 65536), t.isInOlympiadMode());
				if (vsTarget != null)
				{
					damage *= vsTarget[1];
				}
			}
		}
		return damage;
	}

	public double getCalcPhysDamage(L2Character attacker, L2Character target, L2Skill skill, double damage) {
		if (skill != null) {
			int skillId = skill.getId();
			double svsAll[] = getBalance((skillId * -1) - 65536, false);
			if ((svsAll != null) && (ErConfig.SKILLS_BALANCER_AFFECTS_MONSTERS || (target instanceof L2Playable)))
			{
				damage *= svsAll[1];
			}
			if ((target instanceof L2PcInstance) || (target instanceof L2Summon))
			{
				L2PcInstance t = target instanceof L2PcInstance ? target.getActingPlayer() : ((L2Summon) target).getOwner();
				int targetClassId = getClassId(t.getClassId().getId());
				double vsTarget[] = getBalance(skillId + (targetClassId * 65536), t.isInOlympiadMode());
				if (vsTarget != null)
				{
					damage *= vsTarget[1];
				}
			}
		}
		return damage;
	}

	public double getCalcMagicDamage(L2Character attacker, L2Character target, L2Skill skill, double damage) {
		if (skill != null)
		{
			int skillId = skill.getId();
			double svsAll[] = getBalance((skillId * -1) - 65536, false);
			if ((svsAll != null) && (ErConfig.SKILLS_BALANCER_AFFECTS_MONSTERS || (target instanceof L2Playable)))
			{
				damage *= svsAll[1];
			}
			if ((target instanceof L2PcInstance) || (target instanceof L2Summon))
			{
				L2PcInstance t = target instanceof L2PcInstance ? target.getActingPlayer() : ((L2Summon) target).getOwner();
				int targetClassId = getClassId(t.getClassId().getId());
				double vsTarget[] = getBalance(skillId + (targetClassId * 65536), t.isInOlympiadMode());
				if (vsTarget != null)
				{
					damage *= vsTarget[1];
				}
			}
		}
		return damage;
	}

	public double getCalcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill, double damage) {
		if (skill != null)
		{
			int skillId = skill.getId();
			double svsAll[] = getBalance((skillId * -1) - 65536, false);
			if ((svsAll != null) && (ErConfig.SKILLS_BALANCER_AFFECTS_MONSTERS || (target instanceof L2Playable)))
			{
				damage *= svsAll[0];
			}
			if ((target instanceof L2PcInstance) || (target instanceof L2Summon))
			{
				L2PcInstance t = target instanceof L2PcInstance ? target.getActingPlayer() : ((L2Summon) target).getOwner();
				int targetClassId = getClassId(t.getClassId().getId());
				double vsTarget[] = getBalance(skillId + (targetClassId * 65536), t.isInOlympiadMode());
				if (vsTarget != null)
				{
					damage *= vsTarget[0];
				}
			}
		}
		return damage;
	}
	
	private void generateTable() {
		String table = "CREATE TABLE skills_balance (\n";
		table += "  `key` int(10) NOT NULL,\n";
		table += "  forOlympiad tinyint(1) NOT NULL DEFAULT 0,\n";
		table += "  `power` double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  chance double(10,2) NOT NULL DEFAULT 1.00,\n";
		table += "  skillId int(6) NOT NULL DEFAULT -1,\n";
		table += "  targetClassId int(3) NOT NULL DEFAULT -1,\n";
		table += "  PRIMARY KEY (`key`,forOlympiad)\n";
		table += ") ENGINE=InnoDB DEFAULT CHARSET=utf8;\n";
		ErUtils.generateTable("SBalancerTableInitialized", table);
	}
	
	public static SkillsBalanceManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillsBalanceManager _instance = new SkillsBalanceManager();
	}
}
