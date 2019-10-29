package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Summon;

public class PetStatusShow extends L2GameServerPacket
{
	private final int _summonType;
	private final int _summonObjId;
	
	public PetStatusShow(L2Summon summon)
	{
		_summonType = summon.getSummonType();
		_summonObjId = summon.getObjectId();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xB0);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xB1);
				break;
		}
		
		writeD(_summonType);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_summonObjId);
				break;
		}
	}
}
