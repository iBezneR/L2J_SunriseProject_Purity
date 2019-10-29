package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class PledgeSkillListAdd extends L2GameServerPacket
{
	private final int _id;
	private final int _lvl;
	
	public PledgeSkillListAdd(int id, int lvl)
	{
		_id = id;
		_lvl = lvl;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x3A);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x3B);
				break;
		}
		
		writeD(_id);
		writeD(_lvl);
	}
}