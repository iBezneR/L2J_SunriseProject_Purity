package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.xml.impl.SkillData;

import java.util.ArrayList;
import java.util.List;

public final class SkillList extends L2GameServerPacket
{
	private final List<Skill> _skills = new ArrayList<>();
	
	static class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		public boolean disabled;
		public boolean enchanted;
		
		Skill(int pId, int pLevel, boolean pPassive, boolean pDisabled, boolean pEnchanted)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
			disabled = pDisabled;
			enchanted = pEnchanted;
		}
	}
	
	public void addSkill(int id, int level, boolean passive, boolean disabled, boolean enchanted)
	{
		_skills.add(new Skill(id, level, passive, disabled, enchanted));
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x58);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x5F);
				break;
		}
		
		writeD(_skills.size());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
				for (Skill temp : _skills)
				{
					writeD(temp.passive ? 1 : 0);
					writeD(temp.level);
					writeD(temp.id);
					writeC(temp.disabled ? 1 : 0);
				}
				break;
			case EPILOGUE:
			case FREYA:
			case H5:
				for (Skill temp : _skills)
				{
					writeD(temp.passive ? 1 : 0);
					writeD(temp.level);
					writeD(temp.id);
					writeC(temp.disabled ? 1 : 0);
					writeC(temp.enchanted ? 1 : 0);
				}
				break;
			case GC:
			case SL:
				for (Skill temp : _skills)
				{
					writeD(temp.passive ? 1 : 0);
					writeH(temp.level > 100 ? SkillData.getInstance().getMaxLevel(temp.id) : temp.level);
					writeH(temp.level > 100 ? ((temp.level + (1000 * (temp.level / 100))) - ((temp.level / 100) * 100)) : 0); // sublevel
					writeD(temp.id);
					writeD((temp.id * 1000) + temp.level); // TODO GOD ReuseDelayShareGroupID
					writeC(temp.disabled ? 1 : 0); // iSkillDisabled
					writeC(temp.enchanted ? 1 : 0); // CanEnchant
				}
				writeD(0x00); // last learned skill
				break;
		}
	}
}
