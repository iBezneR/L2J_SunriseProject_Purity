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
package l2r.gameserver.model.actor.stat;

import gr.sr.configsEngine.configs.impl.FormulasConfigs;
import l2r.Config;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2DefenderInstance;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Calculator;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.MoveType;
import l2r.gameserver.model.stats.Stats;

public class CharStat
{
	private final L2Character _activeChar;
	private long _exp = 0;
	private int _sp = 0;
	private byte _level = 1;
	/** Creature's maximum buff count. */
	private int _maxBuffCount = Config.BUFFS_MAX_AMOUNT;
	
	public CharStat(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}
	
	/**
	 * Calculate the new value of the state with modifiers that will be applied on the targeted L2Character.<BR>
	 * <B><U> Concept</U> :</B><BR A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...) : <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * When the calc method of a calculator is launched, each mathematical function is called according to its priority <B>_order</B>.<br>
	 * Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order.<br>
	 * The result of the calculation is stored in the value property of an Env class instance.<br>
	 * @param stat The stat to calculate the new value with modifiers
	 * @param initVal The initial value of the stat before applying modifiers
	 * @param target The L2Charcater whose properties will be used in the calculation (ex : CON, INT...)
	 * @param skill The L2Skill whose properties will be used in the calculation (ex : Level...)
	 * @return
	 */
	public final double calcStat(Stats stat, double initVal, L2Character target, L2Skill skill)
	{
		double value = initVal;
		if (stat == null)
		{
			return value;
		}
		
		final int id = stat.ordinal();
		
		final Calculator c = _activeChar.getCalculators()[id];
		
		// If no Func object found, no modifier is applied
		if ((c == null) || (c.size() == 0))
		{
			return value;
		}
		
		// Apply transformation stats.
		if (getActiveChar().isPlayer() && getActiveChar().isTransformed() && (getActiveChar().getTransformation() != null))
		{
			double val = getActiveChar().getTransformation().getStat(getActiveChar().getActingPlayer(), stat);
			if (val > 0)
			{
				value = val;
			}
		}
		
		// Create and init an Env object to pass parameters to the Calculator
		final Env env = new Env();
		env.setCharacter(_activeChar);
		env.setTarget(target);
		env.setSkill(skill);
		env.setValue(value);
		
		// Launch the calculation
		c.calc(env);
		
		// avoid some troubles with negative stats (some stats should never be negative)
		if (env.getValue() <= 0)
		{
			switch (stat)
			{
				case MAX_HP:
				case MAX_MP:
				case MAX_CP:
				case MAGIC_DEFENCE:
				case POWER_DEFENCE:
				case POWER_ATTACK:
				case MAGIC_ATTACK:
				case POWER_ATTACK_SPEED:
				case MAGIC_ATTACK_SPEED:
				case SHIELD_DEFENCE:
				case STAT_CON:
				case STAT_DEX:
				case STAT_INT:
				case STAT_MEN:
				case STAT_STR:
				case STAT_WIT:
					env.setValue(1);
			}
		}
		return env.getValue();
	}
	
	/**
	 * @return the Accuracy (base+modifier) of the L2Character in function of the Weapon Expertise Penalty.
	 */
	public int getAccuracy()
	{
		return (int) Math.round(calcStat(Stats.ACCURACY_COMBAT, 0, null, null));
	}
	
	public L2Character getActiveChar()
	{
		return _activeChar;
	}
	
	/**
	 * @return the Attack Speed multiplier (base+modifier) of the L2Character to get proper animations.
	 */
	public final float getAttackSpeedMultiplier()
	{
		return (float) (((1.1) * getPAtkSpd()) / _activeChar.getTemplate().getBasePAtkSpd());
	}
	
	/**
	 * @return the CON of the L2Character (base+modifier).
	 */
	public final int getCON()
	{
		double CON = calcStat(Stats.STAT_CON, _activeChar.getTemplate().getBaseCON());
		// Champion CON Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			CON += Config.L2JMOD_CHAMPION_CON_BONUS;
		}
		// L2MonsterInstance CON Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_CON_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				CON *= Config.MONSTER_INDIVIDUAL_CON_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				CON *= Config.MONSTER_CON_MULTIPLIER;
			}
		}
		// L2RaidBossInstance CON Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_CON_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				CON *= Config.RAID_INDIVIDUAL_CON_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				CON *= Config.RAID_CON_MULTIPLIER;
			}
		}
		// Minion CON Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_CON_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				CON *= Config.MINION_INDIVIDUAL_CON_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				CON *= Config.MINION_CON_MULTIPLIER;
			}
		}
		// L2GrandBossInstance CON Multiplier
		if (_activeChar instanceof L2GrandBossInstance)
		{
			if (Config.GRAND_INDIVIDUAL_CON_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				CON *= Config.GRAND_INDIVIDUAL_CON_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				CON *= Config.GRAND_CON_MULTIPLIER;
			}
		}
		// L2DefenderInstance CON Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_CON_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				CON *= Config.DEFENDER_INDIVIDUAL_CON_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				CON *= Config.DEFENDER_CON_MULTIPLIER;
			}
		}
		return (int) CON;
	}
	
	/**
	 * @param target
	 * @param init
	 * @return the Critical Damage rate (base+modifier) of the L2Character.
	 */
	public final double getCriticalDmg(L2Character target, double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
	}
	
	/**
	 * @param target
	 * @param skill
	 * @return the Critical Hit rate (base+modifier) of the L2Character.
	 */
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		double val = (int) calcStat(Stats.CRITICAL_RATE, _activeChar.getTemplate().getBaseCritRate(), target, skill);
		if (!_activeChar.canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			val = Math.min(val, FormulasConfigs.MAX_PCRIT_RATE);
		}
		return (int) (val + .5);
	}
	
	/**
	 * @param base
	 * @return the Critical Hit Pos rate of the L2Character
	 */
	public int getCriticalHitPos(int base)
	{
		return (int) calcStat(Stats.CRITICAL_RATE_POS, base);
	}
	
	/**
	 * @return the DEX of the L2Character (base+modifier).
	 */
	public final int getDEX()
	{
		double DEX = calcStat(Stats.STAT_DEX, _activeChar.getTemplate().getBaseDEX());
		// Champion DEX Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			DEX += Config.L2JMOD_CHAMPION_DEX_BONUS;
		}
		// L2MonsterInstance DEX Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_DEX_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				DEX *= Config.MONSTER_INDIVIDUAL_DEX_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				DEX *= Config.MONSTER_DEX_MULTIPLIER;
			}
		}
		// L2RaidBossInstance DEX Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_DEX_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				DEX *= Config.RAID_INDIVIDUAL_DEX_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				DEX *= Config.RAID_DEX_MULTIPLIER;
			}
		}
		// Minion DEX Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_DEX_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				DEX *= Config.MINION_INDIVIDUAL_DEX_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				DEX *= Config.MINION_DEX_MULTIPLIER;
			}
		}
		// L2GrandBossInstance DEX Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_DEX_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				DEX *= Config.GRAND_INDIVIDUAL_DEX_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				DEX *= Config.GRAND_DEX_MULTIPLIER;
			}
		}
		// L2DefenderInstance DEX Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_DEX_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				DEX *= Config.DEFENDER_INDIVIDUAL_DEX_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				DEX *= Config.DEFENDER_DEX_MULTIPLIER;
			}
		}
		
		return (int) DEX;
	}
	
	/**
	 * @param target
	 * @return the Attack Evasion rate (base+modifier) of the L2Character.
	 */
	public int getEvasionRate(L2Character target)
	{
		int val = (int) Math.round(calcStat(Stats.EVASION_RATE, 0, target, null));
		
		if (!_activeChar.canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			val = Math.min(val, FormulasConfigs.MAX_EVASION);
		}
		
		return val;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long value)
	{
		_exp = value;
	}
	
	/**
	 * @return the INT of the L2Character (base+modifier).
	 */
	public int getINT()
	{
		double INT = calcStat(Stats.STAT_INT, _activeChar.getTemplate().getBaseINT());
		// Champion INT Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			INT += Config.L2JMOD_CHAMPION_INT_BONUS;
		}
		// L2MonsterInstance INT Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_INT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				INT *= Config.MONSTER_INDIVIDUAL_INT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				INT *= Config.MONSTER_INT_MULTIPLIER;
			}
		}
		// L2RaidBossInstance INT Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_INT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				INT *= Config.RAID_INDIVIDUAL_INT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				INT *= Config.RAID_INT_MULTIPLIER;
			}
		}
		// Minion INT Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_INT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				INT *= Config.MINION_INDIVIDUAL_INT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				INT *= Config.MINION_INT_MULTIPLIER;
			}
		}
		// L2GrandBossInstance INT Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_INT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				INT *= Config.GRAND_INDIVIDUAL_INT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				INT *= Config.GRAND_INT_MULTIPLIER;
			}
		}
		// L2DefenderInstance INT Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_INT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				INT *= Config.DEFENDER_INDIVIDUAL_INT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				INT *= Config.DEFENDER_INT_MULTIPLIER;
			}
		}
		
		return (int) INT;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public void setLevel(byte value)
	{
		_level = value;
	}
	
	/**
	 * @param skill
	 * @return the Magical Attack range (base+modifier) of the L2Character.
	 */
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
		{
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		}
		
		return _activeChar.getTemplate().getBaseAttackRange();
	}
	
	public int getMaxCp()
	{
		return (int) calcStat(Stats.MAX_CP, _activeChar.getTemplate().getBaseCpMax());
	}
	
	public int getMaxRecoverableCp()
	{
		return (int) calcStat(Stats.MAX_RECOVERABLE_CP, getMaxCp());
	}
	
	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, _activeChar.getTemplate().getBaseHpMax());
	}
	
	public int getMaxRecoverableHp()
	{
		return (int) calcStat(Stats.MAX_RECOVERABLE_HP, getMaxHp());
	}
	
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, _activeChar.getTemplate().getBaseMpMax());
	}
	
	public int getMaxRecoverableMp()
	{
		return (int) calcStat(Stats.MAX_RECOVERABLE_MP, getMaxMp());
	}
	
	/**
	 * Return the MAtk (base+modifier) of the L2Character.<br>
	 * <B><U>Example of use</U>: Calculate Magic damage
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 * @return
	 */
	public int getMAtk(L2Character target, L2Skill skill)
	{
		float bonusAtk = 1;
		// Champion M Attack Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			bonusAtk *= Config.L2JMOD_CHAMPION_MATK;
		}
		if (_activeChar.isRaid())
		{
			bonusAtk *= Config.RAID_MATTACK_MULTIPLIER;
		}
		// L2MonsterInstance Magical Attack Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_MATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.MONSTER_INDIVIDUAL_MATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				// L2MonsterInstance Level Dependent M Attack Bonus
				if (Config.MONSTER_LEVEL_DEPENDENT_M_ATK_MULTIPLIER.containsKey(_activeChar.getLevel()))
				{
					bonusAtk *= Config.MONSTER_LEVEL_DEPENDENT_M_ATK_MULTIPLIER.get(_activeChar.getLevel());
				}
				else
				// L2MonsterInstance Global M Attack Bonus
				{
					bonusAtk *= Config.MONSTER_MATK_MULTIPLIER;
				}
			}
		}
		// L2RaidBossInstance Magical Attack Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_MATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.RAID_INDIVIDUAL_MATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.RAID_MATK_MULTIPLIER;
			}
		}
		// Minion Magical Attack Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_MATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.MINION_INDIVIDUAL_MATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.MINION_MATK_MULTIPLIER;
			}
		}
		// L2GrandBossInstance Magical Attack Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_MATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.GRAND_INDIVIDUAL_MATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.GRAND_MATK_MULTIPLIER;
			}
		}
		// L2DefenderInstance Magical Attack Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_MATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.DEFENDER_INDIVIDUAL_MATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.DEFENDER_MATK_MULTIPLIER;
			}
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_ATTACK, _activeChar.getTemplate().getBaseMAtk() * bonusAtk, target, skill);
	}
	
	/**
	 * @return the MAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 */
	public int getMAtkSpd()
	{
		float bonusSpdAtk = 1;
		// Champion Casting Speed
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			bonusSpdAtk *= Config.L2JMOD_CHAMPION_CASTING_SPD_ATK;
		}
		// L2MonsterInstance Casting Speed Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_CAST_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusSpdAtk *= Config.MONSTER_INDIVIDUAL_CAST_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				// L2MonsterInstance Level Dependent Casting Speed Bonus
				if (Config.MONSTER_LEVEL_DEPENDENT_CST_SPD_MULTIPLIER.containsKey(_activeChar.getLevel()))
				{
					bonusSpdAtk *= Config.MONSTER_LEVEL_DEPENDENT_CST_SPD_MULTIPLIER.get(_activeChar.getLevel());
				}
				else
				// L2MonsterInstance Global Casting Speed Bonus
				{
					bonusSpdAtk *= Config.MONSTER_CAST_SPD_MULTIPLIER;
				}
			}
		}
		// L2RaidBossInstance Casting Speed Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_CAST_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusSpdAtk *= Config.RAID_INDIVIDUAL_CAST_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusSpdAtk *= Config.RAID_CAST_SPD_MULTIPLIER;
			}
		}
		// Minion Casting Speed Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_CAST_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusSpdAtk *= Config.MINION_INDIVIDUAL_CAST_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusSpdAtk *= Config.MINION_CAST_SPD_MULTIPLIER;
			}
		}
		// L2GrandBossInstance Casting Speed Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_CAST_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusSpdAtk *= Config.GRAND_INDIVIDUAL_CAST_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusSpdAtk *= Config.GRAND_CAST_SPD_MULTIPLIER;
			}
		}
		// L2DefenderInstance Casting Speed Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_CAST_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusSpdAtk *= Config.DEFENDER_INDIVIDUAL_CAST_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusSpdAtk *= Config.DEFENDER_CAST_SPD_MULTIPLIER;
			}
		}
		
		double val = calcStat(Stats.MAGIC_ATTACK_SPEED, _activeChar.getTemplate().getBaseMAtkSpd() * bonusSpdAtk);
		if ((val > FormulasConfigs.MAX_MATK_SPEED) && !_activeChar.canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			val = FormulasConfigs.MAX_MATK_SPEED;
		}
		return (int) val;
	}
	
	/**
	 * @param target
	 * @param skill
	 * @return the Magic Critical Hit rate (base+modifier) of the L2Character.
	 */
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		int val = (int) calcStat(Stats.MCRITICAL_RATE, 1, target, skill) * 10;
		
		if (!_activeChar.canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			val = Math.min(val, FormulasConfigs.MAX_MCRIT_RATE);
		}
		
		return val;
	}
	
	/**
	 * <B><U>Example of use </U>: Calculate Magic damage.
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 * @return the MDef (base+modifier) of the L2Character against a skill in function of abnormal effects in progress.
	 */
	public int getMDef(L2Character target, L2Skill skill)
	{
		// Get the base MDef of the L2Character
		double defence = _activeChar.getTemplate().getBaseMDef();
		// Champion Magic Defense Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			defence *= Config.L2JMOD_CHAMPION_MDEF;
		}
		// Calculate modifier for Raid Bosses
		if (_activeChar.isRaid())
		{
			defence *= Config.RAID_MDEFENCE_MULTIPLIER;
		}
		// L2MonsterInstance Magical Defense Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_MDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.MONSTER_INDIVIDUAL_MDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				// L2MonsterInstance Level Dependent Magic Defense Bonus
				if (Config.MONSTER_LEVEL_DEPENDENT_M_DEF_MULTIPLIER.containsKey(_activeChar.getLevel()))
				{
					defence *= Config.MONSTER_LEVEL_DEPENDENT_M_DEF_MULTIPLIER.get(_activeChar.getLevel());
				}
				else
				// L2MonsterInstance Global Magic Defense Bonus
				{
					defence *= Config.MONSTER_MDEF_MULTIPLIER;
				}
			}
		}
		// L2RaidBossInstance Magical Defense Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_MDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.RAID_INDIVIDUAL_MDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				defence *= Config.RAID_MDEF_MULTIPLIER;
			}
		}
		// Minion Magical Defense Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_MDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.MINION_INDIVIDUAL_MDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				defence *= Config.MINION_MDEF_MULTIPLIER;
			}
		}
		// L2GrandBossInstance Magical Defense Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_MDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.GRAND_INDIVIDUAL_MDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				defence *= Config.GRAND_MDEF_MULTIPLIER;
			}
		}
		// L2DefenderInstance Magical Defense Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_MDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.DEFENDER_INDIVIDUAL_MDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				defence *= Config.DEFENDER_MDEF_MULTIPLIER;
			}
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}
	
	/**
	 * @return the MEN of the L2Character (base+modifier).
	 */
	public final int getMEN()
	{
		double MEN = calcStat(Stats.STAT_MEN, _activeChar.getTemplate().getBaseMEN());
		// Champion MEN Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			MEN += Config.L2JMOD_CHAMPION_MEN_BONUS;
		}
		// L2MonsterInstance MEN Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_MEN_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				MEN *= Config.MONSTER_INDIVIDUAL_MEN_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				MEN *= Config.MONSTER_MEN_MULTIPLIER;
			}
		}
		// L2RaidBossInstance MEN Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_MEN_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				MEN *= Config.RAID_INDIVIDUAL_MEN_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				MEN *= Config.RAID_MEN_MULTIPLIER;
			}
		}
		// Minion MEN Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_MEN_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				MEN *= Config.MINION_INDIVIDUAL_MEN_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				MEN *= Config.MINION_MEN_MULTIPLIER;
			}
		}
		// L2GrandBossInstance MEN Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_MEN_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				MEN *= Config.GRAND_INDIVIDUAL_MEN_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				MEN *= Config.GRAND_MEN_MULTIPLIER;
			}
		}
		// L2DefenderInstance MEN Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_MEN_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				MEN *= Config.DEFENDER_INDIVIDUAL_MEN_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				MEN *= Config.DEFENDER_MEN_MULTIPLIER;
			}
		}
		
		return (int) MEN;
	}
	
	public double getMovementSpeedMultiplier()
	{
		double baseSpeed;
		if (_activeChar.isInsideZone(ZoneIdType.WATER))
		{
			baseSpeed = getBaseMoveSpeed(_activeChar.isRunning() ? MoveType.FAST_SWIM : MoveType.SLOW_SWIM);
		}
		else
		{
			baseSpeed = getBaseMoveSpeed(_activeChar.isRunning() ? MoveType.RUN : MoveType.WALK);
		}
		return getMoveSpeed() * (1. / baseSpeed);
	}
	
	/**
	 * @return the RunSpeed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 */
	public double getRunSpeed()
	{
		double baseRunSpd = _activeChar.isInsideZone(ZoneIdType.WATER) ? getSwimRunSpeed() : getBaseMoveSpeed(MoveType.RUN);
		if (baseRunSpd <= 0)
		{
			return 0;
		}
		// Champion Movement Speed Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			baseRunSpd *= Config.L2JMOD_CHAMPION_SPEED_BONUS;
		}
		// L2MonsterInstance Speed Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseRunSpd *= Config.MONSTER_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				// L2MonsterInstance Level Dependent Speed Bonus
				if (Config.MONSTER_LEVEL_DEPENDENT_SPD_MULTIPLIER.containsKey(_activeChar.getLevel()))
				{
					baseRunSpd *= Config.MONSTER_LEVEL_DEPENDENT_SPD_MULTIPLIER.get(_activeChar.getLevel());
				}
				else
				// L2MonsterInstance Global Speed Bonus
				{
					baseRunSpd *= Config.MONSTER_SPEED_MULTIPLIER;
				}
			}
		}
		// L2RaidBossInstance Speed Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseRunSpd *= Config.RAID_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				baseRunSpd *= Config.RAID_SPEED_MULTIPLIER;
			}
		}
		// Minion Speed Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseRunSpd *= Config.MINION_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				baseRunSpd *= Config.MINION_SPEED_MULTIPLIER;
			}
		}
		// L2GrandBossInstance Speed Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseRunSpd *= Config.GRAND_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				baseRunSpd *= Config.GRAND_SPEED_MULTIPLIER;
			}
		}
		// L2DefenderInstance Speed Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseRunSpd *= Config.DEFENDER_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				baseRunSpd *= Config.DEFENDER_SPEED_MULTIPLIER;
			}
		}
		
		return calcStat(Stats.MOVE_SPEED, baseRunSpd, null, null);
	}
	
	/**
	 * @return the WalkSpeed (base+modifier) of the L2Character.
	 */
	public double getWalkSpeed()
	{
		double baseWalkSpd = _activeChar.isInsideZone(ZoneIdType.WATER) ? getSwimWalkSpeed() : getBaseMoveSpeed(MoveType.WALK);
		if (baseWalkSpd <= 0)
		{
			return 0;
		}
		// Champion Movement Speed Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			baseWalkSpd *= Config.L2JMOD_CHAMPION_SPEED_BONUS;
		}
		// L2MonsterInstance Speed Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseWalkSpd *= Config.MONSTER_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				// L2MonsterInstance Level Dependent Speed Bonus
				if (Config.MONSTER_LEVEL_DEPENDENT_SPD_MULTIPLIER.containsKey(_activeChar.getLevel()))
				{
					baseWalkSpd *= Config.MONSTER_LEVEL_DEPENDENT_SPD_MULTIPLIER.get(_activeChar.getLevel());
				}
				else
				// L2MonsterInstance Global Speed Bonus
				{
					baseWalkSpd *= Config.MONSTER_SPEED_MULTIPLIER;
				}
			}
		}
		// L2RaidBossInstance Speed Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseWalkSpd *= Config.RAID_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				baseWalkSpd *= Config.RAID_SPEED_MULTIPLIER;
			}
		}
		// Minion Speed Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseWalkSpd *= Config.MINION_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				baseWalkSpd *= Config.MINION_SPEED_MULTIPLIER;
			}
		}
		// L2GrandBossInstance Speed Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseWalkSpd *= Config.GRAND_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				baseWalkSpd *= Config.GRAND_SPEED_MULTIPLIER;
			}
		}
		// L2DefenderInstance Speed Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_SPEED_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				baseWalkSpd *= Config.DEFENDER_INDIVIDUAL_SPEED_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				baseWalkSpd *= Config.DEFENDER_SPEED_MULTIPLIER;
			}
		}
		
		return calcStat(Stats.MOVE_SPEED, baseWalkSpd);
	}
	
	/**
	 * @return the SwimRunSpeed (base+modifier) of the L2Character.
	 */
	public double getSwimRunSpeed()
	{
		final double baseRunSpd = getBaseMoveSpeed(MoveType.FAST_SWIM);
		if (baseRunSpd <= 0)
		{
			return 0;
		}
		
		return calcStat(Stats.MOVE_SPEED, baseRunSpd, null, null);
	}
	
	/**
	 * @return the SwimWalkSpeed (base+modifier) of the L2Character.
	 */
	public double getSwimWalkSpeed()
	{
		final double baseWalkSpd = getBaseMoveSpeed(MoveType.SLOW_SWIM);
		if (baseWalkSpd <= 0)
		{
			return 0;
		}
		
		return calcStat(Stats.MOVE_SPEED, baseWalkSpd);
	}
	
	/**
	 * @param type movement type
	 * @return the base move speed of given movement type.
	 */
	public double getBaseMoveSpeed(MoveType type)
	{
		return _activeChar.getTemplate().getBaseMoveSpeed(type);
	}
	
	/**
	 * @return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the L2Character in function of the movement type.
	 */
	public double getMoveSpeed()
	{
		if (_activeChar.isInsideZone(ZoneIdType.WATER))
		{
			return _activeChar.isRunning() ? getSwimRunSpeed() : getSwimWalkSpeed();
		}
		return _activeChar.isRunning() ? getRunSpeed() : getWalkSpeed();
	}
	
	/**
	 * @param skill
	 * @return the MReuse rate (base+modifier) of the L2Character.
	 */
	public final double getMReuseRate(L2Skill skill)
	{
		return calcStat(Stats.MAGIC_REUSE_RATE, 1, null, skill);
	}
	
	/**
	 * @param target
	 * @return the PAtk (base+modifier) of the L2Character.
	 */
	public int getPAtk(L2Character target)
	{
		float bonusAtk = 1;
		// Champions P Attack Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			bonusAtk *= Config.L2JMOD_CHAMPION_PATK;
		}
		if (_activeChar.isRaid())
		{
			bonusAtk *= Config.RAID_PATTACK_MULTIPLIER;
		}
		// L2MonsterInstance Physical Attack Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_PATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.MONSTER_INDIVIDUAL_PATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				// L2MonsterInstance Level Physical Attack Bonus
				if (Config.MONSTER_LEVEL_DEPENDENT_P_ATK_MULTIPLIER.containsKey(_activeChar.getLevel()))
				{
					bonusAtk *= Config.MONSTER_LEVEL_DEPENDENT_P_ATK_MULTIPLIER.get(_activeChar.getLevel());
				}
				else
				// L2MonsterInstance Global Physical Attack Bonus
				{
					bonusAtk *= Config.MONSTER_PATK_MULTIPLIER;
				}
			}
		}
		// L2RaidBossInstance Physical Attack Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_PATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.RAID_INDIVIDUAL_PATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.RAID_PATK_MULTIPLIER;
			}
		}
		// Minion Physical Attack Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_PATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.MINION_INDIVIDUAL_PATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.MINION_PATK_MULTIPLIER;
			}
		}
		// L2GrandBossInstance Physical Attack Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_PATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.GRAND_INDIVIDUAL_PATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.GRAND_PATK_MULTIPLIER;
			}
		}
		// L2DefenderInstance Physical Attack Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_PATK_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.DEFENDER_INDIVIDUAL_PATK_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.DEFENDER_PATK_MULTIPLIER;
			}
		}
		
		return (int) calcStat(Stats.POWER_ATTACK, _activeChar.getTemplate().getBasePAtk() * bonusAtk, target, null);
	}
	
	/**
	 * @param target
	 * @return the PAtk Modifier against animals.
	 */
	public final double getPAtkAnimals(L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PAtk Modifier against dragons.
	 */
	public final double getPAtkDragons(L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PAtk Modifier against insects.
	 */
	public final double getPAtkInsects(L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PAtk Modifier against monsters.
	 */
	public final double getPAtkMonsters(L2Character target)
	{
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PAtk Modifier against plants.
	 */
	public final double getPAtkPlants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PAtk Modifier against giants.
	 */
	public final double getPAtkGiants(L2Character target)
	{
		return calcStat(Stats.PATK_GIANTS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PAtk Modifier against magic creatures.
	 */
	public final double getPAtkMagicCreatures(L2Character target)
	{
		return calcStat(Stats.PATK_MCREATURES, 1, target, null);
	}
	
	/**
	 * @return the PAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 */
	public double getPAtkSpd()
	{
		float bonusAtk = 1;
		// Champion Physical Attack Speed Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			bonusAtk *= Config.L2JMOD_CHAMPION_PHYSICAL_SPD_ATK;
		}
		// L2MonsterInstance Attack Speed Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			// Individual Monster Bonus Multiplier
			if (Config.MONSTER_INDIVIDUAL_ATK_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.MONSTER_INDIVIDUAL_ATK_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				// L2MonsterInstance Level Dependent Bonus
				if (Config.MONSTER_LEVEL_DEPENDENT_ATK_SPD_MULTIPLIER.containsKey(_activeChar.getLevel()))
				{
					bonusAtk *= Config.MONSTER_LEVEL_DEPENDENT_ATK_SPD_MULTIPLIER.get(_activeChar.getLevel());
				}
				else
				// L2MonsterInstance Global Attack Speed Bonus
				{
					bonusAtk *= Config.MONSTER_ATK_SPD_MULTIPLIER;
				}
			}
		}
		// L2RaidBossInstance Attack Speed Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_ATK_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.RAID_INDIVIDUAL_ATK_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.RAID_ATK_SPD_MULTIPLIER;
			}
		}
		// Minion Attack Speed Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_ATK_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.MINION_INDIVIDUAL_ATK_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.MINION_ATK_SPD_MULTIPLIER;
			}
		}
		// L2GrandBossInstance Attack Speed Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_ATK_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.GRAND_INDIVIDUAL_ATK_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.GRAND_ATK_SPD_MULTIPLIER;
			}
		}
		// L2DefenderInstance Attack Speed Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_ATK_SPD_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				bonusAtk *= Config.DEFENDER_INDIVIDUAL_ATK_SPD_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				bonusAtk *= Config.DEFENDER_ATK_SPD_MULTIPLIER;
			}
		}
		
		return Math.round(calcStat(Stats.POWER_ATTACK_SPEED, _activeChar.getTemplate().getBasePAtkSpd() * bonusAtk, null, null));
	}
	
	/**
	 * @param target
	 * @return the PDef Modifier against animals.
	 */
	public final double getPDefAnimals(L2Character target)
	{
		return calcStat(Stats.PDEF_ANIMALS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PDef Modifier against dragons.
	 */
	public final double getPDefDragons(L2Character target)
	{
		return calcStat(Stats.PDEF_DRAGONS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PDef Modifier against insects.
	 */
	public final double getPDefInsects(L2Character target)
	{
		return calcStat(Stats.PDEF_INSECTS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PDef Modifier against monsters.
	 */
	public final double getPDefMonsters(L2Character target)
	{
		return calcStat(Stats.PDEF_MONSTERS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PDef Modifier against plants.
	 */
	public final double getPDefPlants(L2Character target)
	{
		return calcStat(Stats.PDEF_PLANTS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PDef Modifier against giants.
	 */
	public final double getPDefGiants(L2Character target)
	{
		return calcStat(Stats.PDEF_GIANTS, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PDef Modifier against giants.
	 */
	public final double getPDefMagicCreatures(L2Character target)
	{
		return calcStat(Stats.PDEF_MCREATURES, 1, target, null);
	}
	
	/**
	 * @param target
	 * @return the PDef (base+modifier) of the L2Character.
	 */
	public int getPDef(L2Character target)
	{
		double defence = 1;
		// Champion Physical Defense Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			defence *= Config.L2JMOD_CHAMPION_PDEF;
		}
		// L2MonsterInstance Physical Defense Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_PDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.MONSTER_INDIVIDUAL_PDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				// L2MonsterInstance Level Physical Defense Bonus
				if (Config.MONSTER_LEVEL_DEPENDENT_P_DEF_MULTIPLIER.containsKey(_activeChar.getLevel()))
				{
					defence *= Config.MONSTER_LEVEL_DEPENDENT_P_DEF_MULTIPLIER.get(_activeChar.getLevel());
				}
				else
				// L2MonsterInstance Global Physical Defense Bonus
				{
					defence *= Config.MONSTER_PDEF_MULTIPLIER;
				}
			}
		}
		// L2RaidBossInstance Physical Defense Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_PDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.RAID_INDIVIDUAL_PDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				defence *= Config.RAID_PDEF_MULTIPLIER;
			}
		}
		// Minion Physical Defense Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_PDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.MINION_INDIVIDUAL_PDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				defence *= Config.MINION_PDEF_MULTIPLIER;
			}
		}
		// L2GrandBossInstance Physical Defense Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_PDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.GRAND_INDIVIDUAL_PDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				defence *= Config.GRAND_PDEF_MULTIPLIER;
			}
		}
		// L2DefenderInstance Physical Defense Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_PDEF_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				defence *= Config.DEFENDER_INDIVIDUAL_PDEF_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				defence *= Config.DEFENDER_PDEF_MULTIPLIER;
			}
		}
		
		return (int) calcStat(Stats.POWER_DEFENCE, ((_activeChar.isRaid()) ? _activeChar.getTemplate().getBasePDef() * Config.RAID_PDEFENCE_MULTIPLIER : _activeChar.getTemplate().getBasePDef()) * defence, target, null);
	}
	
	/**
	 * @return the Physical Attack range (base+modifier) of the L2Character.
	 */
	public final int getPhysicalAttackRange()
	{
		final L2Weapon weapon = _activeChar.getActiveWeaponItem();
		int baseAttackRange;
		if (_activeChar.isTransformed() && _activeChar.isPlayer())
		{
			baseAttackRange = _activeChar.getTransformation().getBaseAttackRange(_activeChar.getActingPlayer());
		}
		else if (weapon != null)
		{
			baseAttackRange = weapon.getBaseAttackRange();
		}
		else
		{
			baseAttackRange = _activeChar.getTemplate().getBaseAttackRange();
		}
		
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, baseAttackRange, null, null);
	}
	
	public int getPhysicalAttackAngle()
	{
		final L2Weapon weapon = _activeChar.getActiveWeaponItem();
		final int baseAttackAngle;
		if (weapon != null)
		{
			baseAttackAngle = weapon.getBaseAttackAngle();
		}
		else
		{
			baseAttackAngle = 120;
		}
		return baseAttackAngle;
	}
	
	/**
	 * @param target
	 * @return the weapon reuse modifier.
	 */
	public final double getWeaponReuseModifier(L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	/**
	 * @return the ShieldDef rate (base+modifier) of the L2Character.
	 */
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0);
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int value)
	{
		_sp = value;
	}
	
	/**
	 * @return the STR of the L2Character (base+modifier).
	 */
	public final int getSTR()
	{
		double STR = calcStat(Stats.STAT_STR, _activeChar.getTemplate().getBaseSTR());
		// Champion STR Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			STR += Config.L2JMOD_CHAMPION_STR_BONUS;
		}
		// L2MonsterInstance STR Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_STR_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				STR *= Config.MONSTER_INDIVIDUAL_STR_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				STR *= Config.MONSTER_STR_MULTIPLIER;
			}
		}
		// L2RaidBossInstance STR Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_STR_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				STR *= Config.RAID_INDIVIDUAL_STR_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				STR *= Config.RAID_STR_MULTIPLIER;
			}
		}
		// Minion STR Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_STR_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				STR *= Config.MINION_INDIVIDUAL_STR_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				STR *= Config.MINION_STR_MULTIPLIER;
			}
		}
		// L2GrandBossInstance STR Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_STR_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				STR *= Config.GRAND_INDIVIDUAL_STR_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				STR *= Config.GRAND_STR_MULTIPLIER;
			}
		}
		// L2DefenderInstance STR Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_STR_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				STR *= Config.DEFENDER_INDIVIDUAL_STR_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				STR *= Config.DEFENDER_STR_MULTIPLIER;
			}
		}
		
		return (int) STR;
	}
	
	/**
	 * @return the WIT of the L2Character (base+modifier).
	 */
	public final int getWIT()
	{
		double WIT = calcStat(Stats.STAT_WIT, _activeChar.getTemplate().getBaseWIT());
		// Champion WIT Bonus
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.isChampion())
		{
			WIT += Config.L2JMOD_CHAMPION_WIT_BONUS;
		}
		// L2MonsterInstance WIT Multiplier
		if ((_activeChar instanceof L2MonsterInstance) && !_activeChar.isRaid() && !_activeChar.isMinion())
		{
			if (Config.MONSTER_INDIVIDUAL_WIT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				WIT *= Config.MONSTER_INDIVIDUAL_WIT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				WIT *= Config.MONSTER_WIT_MULTIPLIER;
			}
		}
		// L2RaidBossInstance WIT Multiplier
		if (_activeChar.isRaid() && !_activeChar.isMinion() && !(_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.RAID_INDIVIDUAL_WIT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				WIT *= Config.RAID_INDIVIDUAL_WIT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				WIT *= Config.RAID_WIT_MULTIPLIER;
			}
		}
		// Minion WIT Multiplier
		if (_activeChar.isMinion())
		{
			if (Config.MINION_INDIVIDUAL_WIT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				WIT *= Config.MINION_INDIVIDUAL_WIT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				WIT *= Config.MINION_WIT_MULTIPLIER;
			}
		}
		// L2GrandBossInstance WIT Multiplier
		if ((_activeChar instanceof L2GrandBossInstance))
		{
			if (Config.GRAND_INDIVIDUAL_WIT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				WIT *= Config.GRAND_INDIVIDUAL_WIT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				WIT *= Config.GRAND_WIT_MULTIPLIER;
			}
		}
		// L2DefenderInstance WIT Multiplier
		if ((_activeChar instanceof L2DefenderInstance))
		{
			if (Config.DEFENDER_INDIVIDUAL_WIT_MULTIPLIER.containsKey(_activeChar.getId()))
			{
				WIT *= Config.DEFENDER_INDIVIDUAL_WIT_MULTIPLIER.get(_activeChar.getId());
			}
			else
			{
				WIT *= Config.DEFENDER_WIT_MULTIPLIER;
			}
		}
		
		return (int) WIT;
	}
	
	/**
	 * @param skill
	 * @return the mpConsume.
	 */
	public final int getMpConsume(L2Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		double mpConsume = skill.getMpConsume();
		double nextDanceMpCost = Math.ceil(skill.getMpConsume() / 2.);
		if (skill.isDance())
		{
			if (Config.DANCE_CONSUME_ADDITIONAL_MP && (_activeChar != null) && (_activeChar.getDanceCount() > 0))
			{
				mpConsume += _activeChar.getDanceCount() * nextDanceMpCost;
			}
		}
		
		mpConsume = calcStat(Stats.MP_CONSUME, mpConsume, null, skill);
		
		if (skill.isDance())
		{
			return (int) calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume);
		}
		else if (skill.isMagic())
		{
			return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume);
		}
		else
		{
			return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume);
		}
	}
	
	/**
	 * @param skill
	 * @return the mpInitialConsume.
	 */
	public final int getMpInitialConsume(L2Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		
		double mpConsume = calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
		
		if (skill.isDance())
		{
			return (int) calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume);
		}
		else if (skill.isMagic())
		{
			return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume);
		}
		else
		{
			return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume);
		}
	}
	
	public byte getAttackElement()
	{
		L2ItemInstance weaponInstance = _activeChar.getActiveWeaponInstance();
		// 1st order - weapon element
		if ((weaponInstance != null) && (weaponInstance.getAttackElementType() >= 0))
		{
			return weaponInstance.getAttackElementType();
		}
		
		// temp fix starts
		int tempVal = 0, stats[] =
		{
			0,
			0,
			0,
			0,
			0,
			0
		};
		
		byte returnVal = -2;
		stats[0] = (int) calcStat(Stats.FIRE_POWER, _activeChar.getTemplate().getBaseFire());
		stats[1] = (int) calcStat(Stats.WATER_POWER, _activeChar.getTemplate().getBaseWater());
		stats[2] = (int) calcStat(Stats.WIND_POWER, _activeChar.getTemplate().getBaseWind());
		stats[3] = (int) calcStat(Stats.EARTH_POWER, _activeChar.getTemplate().getBaseEarth());
		stats[4] = (int) calcStat(Stats.HOLY_POWER, _activeChar.getTemplate().getBaseHoly());
		stats[5] = (int) calcStat(Stats.DARK_POWER, _activeChar.getTemplate().getBaseDark());
		
		for (byte x = 0; x < 6; x++)
		{
			if (stats[x] > tempVal)
			{
				returnVal = x;
				tempVal = stats[x];
			}
		}
		
		return returnVal;
		// temp fix ends
		
		/*
		 * uncomment me once deadlocks in getAllEffects() fixed return _activeChar.getElementIdFromEffects();
		 */
	}
	
	public int getAttackElementValue(byte attackAttribute)
	{
		switch (attackAttribute)
		{
			case Elementals.FIRE:
				return (int) calcStat(Stats.FIRE_POWER, _activeChar.getTemplate().getBaseFire());
			case Elementals.WATER:
				return (int) calcStat(Stats.WATER_POWER, _activeChar.getTemplate().getBaseWater());
			case Elementals.WIND:
				return (int) calcStat(Stats.WIND_POWER, _activeChar.getTemplate().getBaseWind());
			case Elementals.EARTH:
				return (int) calcStat(Stats.EARTH_POWER, _activeChar.getTemplate().getBaseEarth());
			case Elementals.HOLY:
				return (int) calcStat(Stats.HOLY_POWER, _activeChar.getTemplate().getBaseHoly());
			case Elementals.DARK:
				return (int) calcStat(Stats.DARK_POWER, _activeChar.getTemplate().getBaseDark());
			default:
				return 0;
		}
	}
	
	public int getDefenseElementValue(byte defenseAttribute)
	{
		switch (defenseAttribute)
		{
			case Elementals.FIRE:
				return (int) calcStat(Stats.FIRE_RES, _activeChar.getTemplate().getBaseFireRes());
			case Elementals.WATER:
				return (int) calcStat(Stats.WATER_RES, _activeChar.getTemplate().getBaseWaterRes());
			case Elementals.WIND:
				return (int) calcStat(Stats.WIND_RES, _activeChar.getTemplate().getBaseWindRes());
			case Elementals.EARTH:
				return (int) calcStat(Stats.EARTH_RES, _activeChar.getTemplate().getBaseEarthRes());
			case Elementals.HOLY:
				return (int) calcStat(Stats.HOLY_RES, _activeChar.getTemplate().getBaseHolyRes());
			case Elementals.DARK:
				return (int) calcStat(Stats.DARK_RES, _activeChar.getTemplate().getBaseDarkRes());
			default:
				return 0;
		}
	}
	
	/**
	 * Gets the maximum buff count.
	 * @return the maximum buff count
	 */
	public int getMaxBuffCount()
	{
		return _maxBuffCount;
	}
	
	/**
	 * Sets the maximum buff count.
	 * @param buffCount the buff count
	 */
	public void setMaxBuffCount(int buffCount)
	{
		_maxBuffCount = buffCount;
	}
}
