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
package l2r.gameserver.model.actor.templates;

import l2r.Config;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.enums.AISkillScope;
import l2r.gameserver.enums.AIType;
import l2r.gameserver.enums.Race;
import l2r.gameserver.enums.Sex;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.drops.DropListScope;
import l2r.gameserver.model.drops.IDropItem;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.interfaces.IIdentifiable;
import l2r.gameserver.model.skills.L2Skill;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPC template.
 * @author NosBit
 */
public final class L2NpcTemplate extends L2CharTemplate implements IIdentifiable
{
	private int _id;
	private int _displayId;
	private byte _level;
	private String _type;
	private String _name;
	private boolean _usingServerSideName;
	private String _title;
	private boolean _usingServerSideTitle;
	private StatsSet _parameters;
	private Sex _sex;
	private int _chestId;
	private int _rhandId;
	private int _lhandId;
	private int _weaponEnchant;
	private double _expRate;
	private double _sp;
	private double _raidPoints;
	private boolean _unique;
	private boolean _attackable;
	private boolean _targetable;
	private boolean _undying;
	private boolean _showName;
	private boolean _flying;
	private boolean _canMove;
	private boolean _noSleepMode;
	private boolean _passableDoor;
	private boolean _hasSummoner;
	private boolean _canBeSown;
	private int _corpseTime;
	private AIType _aiType;
	private int _aggroRange;
	private int _clanHelpRange;
	private int _dodge;
	private boolean _isChaos;
	private boolean _isAggressive;
	private int _soulShot;
	private int _spiritShot;
	private int _soulShotChance;
	private int _spiritShotChance;
	private int _minSkillChance;
	private int _maxSkillChance;
	private int _primarySkillId;
	private int _shortRangeSkillId;
	private int _shortRangeSkillChance;
	private int _longRangeSkillId;
	private int _longRangeSkillChance;
	private Set<Integer> _clans;
	private Set<Integer> _ignoreClanNpcIds;
	private Map<DropListScope, List<IDropItem>> _dropLists;
	private double _collisionRadiusGrown;
	private double _collisionHeightGrown;
	
	private final List<ClassId> _teachInfo = new ArrayList<>();
	
	private final Map<Integer, L2Skill> _skills = new ConcurrentHashMap<>();
	// Skill AI
	private final List<L2Skill> _buffSkills = new ArrayList<>();
	private final List<L2Skill> _negativeSkills = new ArrayList<>();
	private final List<L2Skill> _debuffSkills = new ArrayList<>();
	private final List<L2Skill> _atkSkills = new ArrayList<>();
	private final List<L2Skill> _rootSkills = new ArrayList<>();
	private final List<L2Skill> _stunskills = new ArrayList<>();
	private final List<L2Skill> _sleepSkills = new ArrayList<>();
	private final List<L2Skill> _paralyzeSkills = new ArrayList<>();
	private final List<L2Skill> _fossilSkills = new ArrayList<>();
	private final List<L2Skill> _floatSkills = new ArrayList<>();
	private final List<L2Skill> _immobilizeSkills = new ArrayList<>();
	private final List<L2Skill> _healSkills = new ArrayList<>();
	private final List<L2Skill> _resSkills = new ArrayList<>();
	private final List<L2Skill> _dotSkills = new ArrayList<>();
	private final List<L2Skill> _cotSkills = new ArrayList<>();
	private final List<L2Skill> _universalSkills = new ArrayList<>();
	private final List<L2Skill> _manaSkills = new ArrayList<>();
	private final List<L2Skill> _longRangeSkills = new ArrayList<>();
	private final List<L2Skill> _shortRangeSkills = new ArrayList<>();
	private final List<L2Skill> _generalSkills = new ArrayList<>();
	private final List<L2Skill> _suicideSkills = new ArrayList<>();
	
	/**
	 * Constructor of L2Character.
	 * @param set The StatsSet object to transfer data to the method
	 */
	public L2NpcTemplate(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void set(StatsSet set)
	{
		super.set(set);
		_id = set.getInt("id");
		_displayId = set.getInt("displayId", _id);
		_level = set.getByte("level", (byte) 70);
		_type = set.getString("type", "L2Npc");
		_name = set.getString("name", "");
		_usingServerSideName = set.getBoolean("usingServerSideName", false);
		_title = set.getString("title", "");
		_usingServerSideTitle = set.getBoolean("usingServerSideTitle", false);
		setRace(set.getEnum("race", Race.class, Race.NONE));
		_sex = set.getEnum("sex", Sex.class, Sex.ETC);
		
		_chestId = set.getInt("chestId", 0);
		_rhandId = set.getInt("rhandId", 0);
		_lhandId = set.getInt("lhandId", 0);
		_weaponEnchant = set.getInt("weaponEnchant", 0);
		
		_expRate = set.getDouble("expRate", 0);
		_sp = set.getDouble("sp", 0);
		_raidPoints = set.getDouble("raidPoints", 0);
		
		_unique = set.getBoolean("unique", false);
		_attackable = set.getBoolean("attackable", true);
		_targetable = set.getBoolean("targetable", true);
		_undying = set.getBoolean("undying", true);
		_showName = set.getBoolean("showName", true);
		_flying = set.getBoolean("flying", false);
		_canMove = set.getBoolean("canMove", true);
		_noSleepMode = set.getBoolean("noSleepMode", false);
		_passableDoor = set.getBoolean("passableDoor", false);
		_hasSummoner = set.getBoolean("hasSummoner", false);
		_canBeSown = set.getBoolean("canBeSown", false);
		
		_corpseTime = set.getInt("corpseTime", Config.DEFAULT_CORPSE_TIME);
		
		_aiType = set.getEnum("aiType", AIType.class, AIType.FIGHTER);
		_aggroRange = set.getInt("aggroRange", 0);
		_clanHelpRange = set.getInt("clanHelpRange", 0);
		_dodge = set.getInt("dodge", 0);
		_isChaos = set.getBoolean("isChaos", false);
		_isAggressive = set.getBoolean("isAggressive", true);
		
		_soulShot = set.getInt("soulShot", 200);
		_spiritShot = set.getInt("spiritShot", 200);
		_soulShotChance = set.getInt("shotShotChance", 0);
		_spiritShotChance = set.getInt("spiritShotChance", 0);
		
		_minSkillChance = set.getInt("minSkillChance", 7);
		_maxSkillChance = set.getInt("maxSkillChance", 15);
		_primarySkillId = set.getInt("primarySkillId", 0);
		_shortRangeSkillId = set.getInt("shortRangeSkillId", 0);
		_shortRangeSkillChance = set.getInt("shortRangeSkillChance", 0);
		_longRangeSkillId = set.getInt("longRangeSkillId", 0);
		_longRangeSkillChance = set.getInt("longRangeSkillChance", 0);
		
		_collisionRadiusGrown = set.getDouble("collisionRadiusGrown", 0);
		_collisionHeightGrown = set.getDouble("collisionHeightGrown", 0);
	}
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public void setLevel(byte level)
	{
		_level = level;
	}
	
	public String getType()
	{
		return _type;
	}
	
	public boolean isType(String type)
	{
		return getType().equalsIgnoreCase(type);
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean isUsingServerSideName()
	{
		return _usingServerSideName;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public boolean isUsingServerSideTitle()
	{
		return _usingServerSideTitle;
	}
	
	public StatsSet getParameters()
	{
		return _parameters;
	}
	
	public void setParameters(StatsSet set)
	{
		_parameters = set;
	}
	
	public Sex getSex()
	{
		return _sex;
	}
	
	public int getChestId()
	{
		return _chestId;
	}
	
	public int getRHandId()
	{
		return _rhandId;
	}
	
	public int getLHandId()
	{
		return _lhandId;
	}
	
	public int getWeaponEnchant()
	{
		return _weaponEnchant;
	}
	
	public double getExpRate()
	{
		return _expRate;
	}
	
	public double getSP()
	{
		return _sp;
	}
	
	public double getRaidPoints()
	{
		return _raidPoints;
	}
	
	public boolean isUnique()
	{
		return _unique;
	}
	
	public boolean isAttackable()
	{
		return _attackable;
	}
	
	public boolean isTargetable()
	{
		return _targetable;
	}
	
	public boolean isUndying()
	{
		return _undying;
	}
	
	public boolean isShowName()
	{
		return _showName;
	}
	
	public boolean isFlying()
	{
		return _flying;
	}
	
	public boolean canMove()
	{
		return _canMove;
	}
	
	public boolean isNoSleepMode()
	{
		return _noSleepMode;
	}
	
	public boolean isPassableDoor()
	{
		return _passableDoor;
	}
	
	public boolean hasSummoner()
	{
		return _hasSummoner;
	}
	
	public boolean canBeSown()
	{
		return _canBeSown;
	}
	
	public int getCorpseTime()
	{
		return _corpseTime;
	}
	
	public AIType getAIType()
	{
		return _aiType;
	}
	
	public int getAggroRange()
	{
		return _aggroRange > Config.MAX_AGGRO_RANGE ? Config.MAX_AGGRO_RANGE : _aggroRange;
	}
	
	public int getClanHelpRange()
	{
		return _clanHelpRange;
	}
	
	public int getDodge()
	{
		return _dodge;
	}
	
	public boolean isChaos()
	{
		return _isChaos;
	}
	
	public boolean isAggressive()
	{
		return _isAggressive;
	}
	
	public int getSoulShot()
	{
		return _soulShot;
	}
	
	public int getSpiritShot()
	{
		return _spiritShot;
	}
	
	public int getSoulShotChance()
	{
		return _soulShotChance;
	}
	
	public int getSpiritShotChance()
	{
		return _spiritShotChance;
	}
	
	public int getMinSkillChance()
	{
		return _minSkillChance;
	}
	
	public int getMaxSkillChance()
	{
		return _maxSkillChance;
	}
	
	public int getPrimarySkillId()
	{
		return _primarySkillId;
	}
	
	public int getShortRangeSkillId()
	{
		return _shortRangeSkillId;
	}
	
	public int getShortRangeSkillChance()
	{
		return _shortRangeSkillChance;
	}
	
	public int getLongRangeSkillId()
	{
		return _longRangeSkillId;
	}
	
	public int getLongRangeSkillChance()
	{
		return _longRangeSkillChance;
	}
	
	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	public Set<Integer> getClans()
	{
		return _clans;
	}
	
	/**
	 * @param clans A sorted array of clan ids
	 */
	public void setClans(Set<Integer> clans)
	{
		_clans = clans != null ? Collections.unmodifiableSet(clans) : null;
	}
	
	/**
	 * @param clanName clan name to check if it belongs to this NPC template clans.
	 * @param clanNames clan names to check if they belong to this NPC template clans.
	 * @return {@code true} if at least one of the clan names belong to this NPC template clans, {@code false} otherwise.
	 */
	public boolean isClan(String clanName, String... clanNames)
	{
		// Using local variable for the sake of reloading since it can be turned to null.
		final Set<Integer> clans = _clans;
		
		if (clans == null)
		{
			return false;
		}
		
		int clanId = NpcTable.getInstance().getClanId("ALL");
		if (clans.contains(clanId))
		{
			return true;
		}
		
		clanId = NpcTable.getInstance().getClanId(clanName);
		if (clans.contains(clanId))
		{
			return true;
		}
		
		for (String name : clanNames)
		{
			clanId = NpcTable.getInstance().getClanId(name);
			if (clans.contains(clanId))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param clans A set of clan names to check if they belong to this NPC template clans.
	 * @return {@code true} if at least one of the clan names belong to this NPC template clans, {@code false} otherwise.
	 */
	public boolean isClan(Set<Integer> clans)
	{
		// Using local variable for the sake of reloading since it can be turned to null.
		final Set<Integer> clanSet = _clans;
		
		if ((clanSet == null) || (clans == null))
		{
			return false;
		}
		
		int clanId = NpcTable.getInstance().getClanId("ALL");
		if (clanSet.contains(clanId))
		{
			return true;
		}
		
		for (Integer id : clans)
		{
			if (clanSet.contains(id))
			{
				return true;
			}
		}
		return false;
	}
	
	public Set<Integer> getIgnoreClanNpcIds()
	{
		return _ignoreClanNpcIds;
	}
	
	/**
	 * @param ignoreClanNpcIds the ignore clan npc ids
	 */
	public void setIgnoreClanNpcIds(Set<Integer> ignoreClanNpcIds)
	{
		_ignoreClanNpcIds = ignoreClanNpcIds != null ? Collections.unmodifiableSet(ignoreClanNpcIds) : null;
	}
	
	public Map<DropListScope, List<IDropItem>> getDropLists()
	{
		return _dropLists;
	}
	
	public void setDropLists(Map<DropListScope, List<IDropItem>> dropLists)
	{
		_dropLists = dropLists != null ? Collections.unmodifiableMap(dropLists) : null;
	}
	
	public List<IDropItem> getDropList(DropListScope dropListScope)
	{
		Map<DropListScope, List<IDropItem>> dropLists = _dropLists;
		return dropLists != null ? dropLists.get(dropListScope) : null;
	}
	
	public Collection<ItemHolder> calculateDrops(DropListScope dropListScope, L2Character victim, L2Character killer)
	{
		List<IDropItem> dropList = getDropList(dropListScope);
		if (dropList == null)
		{
			return null;
		}
		
		Collection<ItemHolder> calculatedDrops = null;
		for (IDropItem dropItem : dropList)
		{
			final Collection<ItemHolder> drops = dropItem.calculateDrops(victim, killer);
			if ((drops == null) || drops.isEmpty())
			{
				continue;
			}
			
			if (calculatedDrops == null)
			{
				calculatedDrops = new LinkedList<>();
			}
			
			calculatedDrops.addAll(drops);
		}
		
		return calculatedDrops;
	}
	
	public double getCollisionRadiusGrown()
	{
		return _collisionRadiusGrown;
	}
	
	public double getCollisionHeightGrown()
	{
		return _collisionHeightGrown;
	}
	
	public static boolean isAssignableTo(Class<?> sub, Class<?> clazz)
	{
		// If clazz represents an interface
		if (clazz.isInterface())
		{
			// check if obj implements the clazz interface
			Class<?>[] interfaces = sub.getInterfaces();
			for (Class<?> interface1 : interfaces)
			{
				if (clazz.getName().equals(interface1.getName()))
				{
					return true;
				}
			}
		}
		else
		{
			do
			{
				if (sub.getName().equals(clazz.getName()))
				{
					return true;
				}
				
				sub = sub.getSuperclass();
			}
			while (sub != null);
		}
		return false;
	}
	
	/**
	 * Checks if obj can be assigned to the Class represented by clazz.<br>
	 * This is true if, and only if, obj is the same class represented by clazz, or a subclass of it or obj implements the interface represented by clazz.
	 * @param obj
	 * @param clazz
	 * @return {@code true} if the object can be assigned to the class, {@code false} otherwise
	 */
	public static boolean isAssignableTo(Object obj, Class<?> clazz)
	{
		return L2NpcTemplate.isAssignableTo(obj.getClass(), clazz);
	}
	
	public boolean canTeach(ClassId classId)
	{
		// If the player is on a third class, fetch the class teacher
		// information for its parent class.
		if (classId.level() == 3)
		{
			return _teachInfo.contains(classId.getParent());
		}
		return _teachInfo.contains(classId);
	}
	
	public List<ClassId> getTeachInfo()
	{
		return _teachInfo;
	}
	
	public void addTeachInfo(List<ClassId> teachInfo)
	{
		_teachInfo.addAll(teachInfo);
	}
	
	public void addAtkSkill(L2Skill skill)
	{
		_atkSkills.add(skill);
	}
	
	public void addBuffSkill(L2Skill skill)
	{
		_buffSkills.add(skill);
	}
	
	public void addCOTSkill(L2Skill skill)
	{
		_cotSkills.add(skill);
	}
	
	public void addDebuffSkill(L2Skill skill)
	{
		_debuffSkills.add(skill);
	}
	
	public void addDOTSkill(L2Skill skill)
	{
		_dotSkills.add(skill);
	}
	
	public void addFloatSkill(L2Skill skill)
	{
		_floatSkills.add(skill);
	}
	
	public void addFossilSkill(L2Skill skill)
	{
		_fossilSkills.add(skill);
	}
	
	public void addGeneralSkill(L2Skill skill)
	{
		_generalSkills.add(skill);
	}
	
	public void addHealSkill(L2Skill skill)
	{
		_healSkills.add(skill);
	}
	
	public void addImmobiliseSkill(L2Skill skill)
	{
		_immobilizeSkills.add(skill);
	}
	
	public void addManaHealSkill(L2Skill skill)
	{
		_manaSkills.add(skill);
	}
	
	public void addNegativeSkill(L2Skill skill)
	{
		_negativeSkills.add(skill);
	}
	
	public void addParalyzeSkill(L2Skill skill)
	{
		_paralyzeSkills.add(skill);
	}
	
	public void addRangeSkill(L2Skill skill)
	{
		if ((skill.getCastRange() <= 150) && (skill.getCastRange() > 0))
		{
			_shortRangeSkills.add(skill);
		}
		else if (skill.getCastRange() > 150)
		{
			_longRangeSkills.add(skill);
		}
	}
	
	public void addResSkill(L2Skill skill)
	{
		_resSkills.add(skill);
	}
	
	public void addRootSkill(L2Skill skill)
	{
		_rootSkills.add(skill);
	}
	
	public void addSkill(L2Skill skill)
	{
		if (!skill.isPassive())
		{
			if (skill.isSuicideAttack())
			{
				addSuicideSkill(skill);
			}
			else
			{
				addGeneralSkill(skill);
				switch (skill.getSkillType())
				{
					case BUFF:
						addBuffSkill(skill);
						break;
					case DEBUFF:
						addDebuffSkill(skill);
						addCOTSkill(skill);
						addRangeSkill(skill);
						break;
					case ROOT:
						addRootSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case SLEEP:
						addSleepSkill(skill);
						addImmobiliseSkill(skill);
						break;
					case STUN:
						addRootSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case PARALYZE:
						addParalyzeSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case PDAM:
					case MDAM:
					case BLOW:
					case DRAIN:
					case CHARGEDAM:
						addAtkSkill(skill);
						addUniversalSkill(skill);
						addRangeSkill(skill);
						break;
					case POISON:
					case DOT:
					case MDOT:
					case BLEED:
						addDOTSkill(skill);
						addRangeSkill(skill);
						break;
					case MUTE:
					case FEAR:
						addCOTSkill(skill);
						addRangeSkill(skill);
						break;
					default:
						if (skill.hasEffectType(L2EffectType.CANCEL, L2EffectType.DISPEL, L2EffectType.DISPEL_BY_SLOT))
						{
							addNegativeSkill(skill);
							addRangeSkill(skill);
						}
						else if (skill.hasEffectType(L2EffectType.HEAL, L2EffectType.HEAL_PERCENT))
						{
							addHealSkill(skill);
						}
						else if (skill.hasEffectType(L2EffectType.RESURRECTION))
						{
							addResSkill(skill);
						}
						else if (skill.hasEffectType(L2EffectType.MAGICAL_ATTACK_MP, L2EffectType.PHYSICAL_ATTACK, L2EffectType.PHYSICAL_ATTACK_HP_LINK, L2EffectType.DEATH_LINK))
						{
							addAtkSkill(skill);
							addUniversalSkill(skill);
							addRangeSkill(skill);
						}
						else
						{
							addUniversalSkill(skill);
						}
						break;
				}
			}
		}
		_skills.put(skill.getId(), skill);
	}
	
	public void addSleepSkill(L2Skill skill)
	{
		_sleepSkills.add(skill);
	}
	
	public void addStunSkill(L2Skill skill)
	{
		_stunskills.add(skill);
	}
	
	public void addSuicideSkill(L2Skill skill)
	{
		_suicideSkills.add(skill);
	}
	
	public void addUniversalSkill(L2Skill skill)
	{
		_universalSkills.add(skill);
	}
	
	/**
	 * @return the general skills.
	 */
	public List<L2Skill> getGeneralskills()
	{
		return _generalSkills;
	}
	
	/**
	 * @return the heal skills.
	 */
	public List<L2Skill> getHealSkills()
	{
		return _healSkills;
	}
	
	/**
	 * @return the long range skills.
	 */
	public List<L2Skill> getLongRangeSkills()
	{
		return _longRangeSkills;
	}
	
	/**
	 * @return the short range skills.
	 */
	public List<L2Skill> getShortRangeSkills()
	{
		return _shortRangeSkills;
	}
	
	/**
	 * @return the cost over time skills.
	 */
	public List<L2Skill> getCostOverTimeSkills()
	{
		return _cotSkills;
	}
	
	/**
	 * @return the debuff skills.
	 */
	public List<L2Skill> getDebuffSkills()
	{
		return _debuffSkills;
	}
	
	/**
	 * @return the negative skills.
	 */
	public List<L2Skill> getNegativeSkills()
	{
		return _negativeSkills;
	}
	
	/**
	 * @return the attack skills.
	 */
	public List<L2Skill> getAtkSkills()
	{
		return _atkSkills;
	}
	
	/**
	 * @return the immobilize skills.
	 */
	public List<L2Skill> getImmobiliseSkills()
	{
		return _immobilizeSkills;
	}
	
	/**
	 * @return the resurrection skills.
	 */
	public List<L2Skill> getResSkills()
	{
		return _resSkills;
	}
	
	public List<L2Skill> getSuicideSkills()
	{
		return _suicideSkills;
	}
	
	/**
	 * @return the buff skills.
	 */
	public List<L2Skill> getBuffSkills()
	{
		return _buffSkills;
	}
	
	/**
	 * @return the universal skills.
	 */
	public List<L2Skill> getUniversalSkills()
	{
		return _universalSkills;
	}
	
	/**
	 * @param aiSkillScope
	 * @return
	 */
	public List<L2Skill> getAISkills(AISkillScope aiSkillScope)
	{
		switch (aiSkillScope)
		{
			case ATTACK:
				return _atkSkills;
			case BUFF:
				return _buffSkills;
			case COT:
				return _cotSkills;
			case DEBUFF:
				return _debuffSkills;
			case GENERAL:
				return _generalSkills;
			case HEAL:
				return _healSkills;
			case IMMOBILIZE:
				return _immobilizeSkills;
			case LONG_RANGE:
				return _longRangeSkills;
			case NEGATIVE:
				return _negativeSkills;
			case RES:
				return _resSkills;
			case SHORT_RANGE:
				return _shortRangeSkills;
			case SUICIDE:
				return _suicideSkills;
			case UNIVERSAL:
				return _universalSkills;
			default:
				return null;
		}
	}
}
