package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Summon;

public class ExPartyPetWindowDelete extends L2GameServerPacket
{
	private final L2Summon _summon;
	
	public ExPartyPetWindowDelete(L2Summon summon)
	{
		_summon = summon;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x6A);
				break;
			case GC:
			case SL:
				writeH(0x6B);
				break;
		}
		
		writeD(_summon.getObjectId());
		writeD(_summon.getOwner().getObjectId());
		writeS(_summon.getName());
	}
}
