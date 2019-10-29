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
package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.xml.impl.SkillTreesData;
import l2r.gameserver.model.L2SkillLearn;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.AcquireSkillType;
import l2r.gameserver.model.holders.ItemHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Acquire Skill List server packet implementation.
 */
public final class AcquireSkillList extends L2GameServerPacket
{
	private final List<Skill> _skills = new ArrayList<>();
	private AcquireSkillType _skillType;
	
	@SuppressWarnings("unused")
	private L2PcInstance _activeChar;
	private List<L2SkillLearn> _learnable;
	
	/**
	 * Private class containing learning skill information.
	 */
	private static class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int spCost;
		public int requirements;
		
		public Skill(int pId, int pNextLevel, int pMaxLevel, int pSpCost, int pRequirements)
		{
			id = pId;
			nextLevel = pNextLevel;
			maxLevel = pMaxLevel;
			spCost = pSpCost;
			requirements = pRequirements;
		}
	}
	
	public AcquireSkillList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_learnable = SkillTreesData.getInstance().getAvailableSkills(activeChar, activeChar.getClassId(), false, false);
		_learnable.addAll(SkillTreesData.getInstance().getNextAvailableSkills(activeChar, activeChar.getClassId(), false, false));
	}
	
	public AcquireSkillList(AcquireSkillType type)
	{
		_skillType = type;
	}
	
	public void addSkill(int id, int nextLevel, int maxLevel, int spCost, int requirements)
	{
		_skills.add(new Skill(id, nextLevel, maxLevel, spCost, requirements));
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(0x90);
				writeH(_learnable.size());
				for (L2SkillLearn skill : _learnable)
				{
					writeD(skill.getSkillId());
					writeD(skill.getSkillLevel());
					writeQ(skill.getLevelUpSp());
					writeC(skill.getGetLevel());
					writeC(0x00); // skill.getDualClassLevel()
					writeC(skill.getRequiredItems().size());
					for (ItemHolder item : skill.getRequiredItems())
					{
						writeD(item.getId());
						writeQ(item.getCount());
					}
					
					// final List<Skill> skillRem = skill.getRemoveSkills().stream().map(_activeChar::getKnownSkill).filter(Objects::nonNull).collect(Collectors.toList());
					
					writeC(0x00); // skillRem.size()
					// for (Skill skillRemove : skillRem)
					// {
					// writeD(skillRemove.getId());
					// writeD(skillRemove.getLevel());
					// }
				}
				return;
		}
		
		if (_skills.isEmpty())
		{
			return;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x8A);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeC(0x90);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_skillType.ordinal());
				writeD(_skills.size());
				break;
		}
		
		for (Skill temp : _skills)
		{
			writeD(temp.id);
			writeD(temp.nextLevel);
			writeD(temp.maxLevel);
			writeD(temp.spCost);
			writeD(temp.requirements);
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					if (_skillType == AcquireSkillType.SUBPLEDGE)
					{
						writeD(0); // TODO: ?
					}
					break;
			}
		}
	}
}