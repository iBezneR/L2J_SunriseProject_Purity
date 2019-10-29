package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class PartySmallWindowDelete extends L2GameServerPacket
{
	private final L2PcInstance _member;
	
	public PartySmallWindowDelete(L2PcInstance member)
	{
		_member = member;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x51);
				break;
		}
		
		writeD(_member.getObjectId());
		writeS(_member.getName());
	}
}
