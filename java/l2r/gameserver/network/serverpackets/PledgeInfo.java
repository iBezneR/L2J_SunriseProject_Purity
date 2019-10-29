package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.model.L2Clan;

public class PledgeInfo extends L2GameServerPacket
{
	private final L2Clan _clan;
	
	public PledgeInfo(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x83);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x89);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(Config.SERVER_ID);
				break;
		}
		
		writeD(_clan.getId());
		writeS(_clan.getName());
		writeS(_clan.getAllyName());
	}
}
