package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;

import java.util.Arrays;
import java.util.List;

public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _skillId;
	private final int _skillLevel;
	private final List<L2Object> _targets;
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Object... targets)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		
		//@formatter:off 
		if (targets == null)
		{
			targets = new L2Object[] { cha };
		}
		//@formatter:on 
		_targets = Arrays.asList(targets);
	}
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
	{
		this(cha, skillId, skillId, cha);
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x76);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x54);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0); // MagicSkillUse castingType
				break;
		}
		
		writeD(_charObjId);
		writeD(_skillId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_skillLevel);
				break;
			case GC:
			case SL:
				writeD(_skillLevel > 100 ? SkillData.getInstance().getMaxLevel(_skillId) : _skillLevel);
				break;
		}
		
		writeD(_targets.size());
		for (L2Object target : _targets)
		{
			writeD(target.getObjectId());
		}
	}
}
