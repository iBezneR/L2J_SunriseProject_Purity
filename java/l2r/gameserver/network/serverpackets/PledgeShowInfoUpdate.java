package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.model.L2Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private final L2Clan _clan;
	
	public PledgeShowInfoUpdate(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x88);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x8E);
				break;
		}
		
		// sending empty data so client will ask all the info in response ;)
		writeD(_clan.getId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(Config.SERVER_ID);
				break;
		}
		
		writeD(_clan.getCrestId());
		writeD(_clan.getLevel()); // clan level
		writeD(_clan.getCastleId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0x00); // castle state ?
				break;
		}
		
		writeD(_clan.getHideoutId());
		writeD(_clan.getFortId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore()); // clan reputation score
		writeD(0x00); // ?
		writeD(0x00); // ?
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName()); // c5
		writeD(_clan.getAllyCrestId()); // c5
		writeD(_clan.isAtWar() ? 1 : 0); // c5
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0x00); // TODO: Find me!
				writeD(0x00); // TODO: Find me!
				break;
		}
	}
}
