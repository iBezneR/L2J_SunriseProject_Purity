package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExSubPledgeSkillAdd extends L2GameServerPacket
{
	private final int _type;
	private final int _skillId;
	private final int _skillLevel;
	
	public ExSubPledgeSkillAdd(int type, int skillId, int skillLevel)
	{
		_type = type;
		_skillId = skillId;
		_skillLevel = skillLevel;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x76);
				break;
			case GC:
			case SL:
				writeH(0x77);
				break;
		}
		
		writeD(_type);
		writeD(_skillId);
		writeD(_skillLevel);
	}
}
