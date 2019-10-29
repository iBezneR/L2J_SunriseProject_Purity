package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.sql.ClanTable;
import l2r.gameserver.model.L2Clan;

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final int _tab;
	
	public PledgeReceiveWarList(L2Clan clan, int tab)
	{
		_clan = clan;
		_tab = tab;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			// TODO grand crude packet implement me
			case GC:
			case SL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x3E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x3F);
				break;
			case GC:
			case SL:
				writeH(0x40);
				break;
		}
		
		writeD(_tab); // type : 0 = Declared, 1 = Under Attack
		writeD(0x00); // page
		writeD(_tab == 0 ? _clan.getWarList().size() : _clan.getAttackerList().size());
		for (Integer i : _tab == 0 ? _clan.getWarList() : _clan.getAttackerList())
		{
			L2Clan clan = ClanTable.getInstance().getClan(i);
			if (clan == null)
			{
				continue;
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					writeS(clan.getName());
					writeD(_tab); // ??
					writeD(_tab); // ??
					break;
				case GC:
				case SL:
					// writeS(clan.getName());
					// writeD(clanWar.getState().ordinal()); // type: 0 = Declaration, 1 = Blood Declaration, 2 = In War, 3 = Victory, 4 = Defeat, 5 = Tie, 6 = Error
					// writeD(clanWar.getRemainingTime()); // Time if friends to start remaining
					// writeD(clanWar.getKillDifference(_clan)); // Score
					// writeD(0); // @TODO: Recent change in points
					// writeD(clanWar.getKillToStart()); // Friends to start war left
					break;
			}
		}
	}
}
