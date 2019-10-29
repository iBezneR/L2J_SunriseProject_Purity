package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Clan;

public final class PledgeStatusChanged extends L2GameServerPacket
{
	private final L2Clan _clan;
	
	public PledgeStatusChanged(L2Clan clan)
	{
		_clan = clan;
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
				writeC(0xCD);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0x00);
				break;
		}
		writeD(_clan.getLeaderId());
		writeD(_clan.getId());
		writeD(_clan.getCrestId());
		writeD(_clan.getAllyId());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.getCrestLargeId());
		writeD(0x00); // pledge type ?
	}
}
