package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.sql.ClanTable;
import l2r.gameserver.instancemanager.ClanHallManager;
import l2r.gameserver.model.entity.clanhall.AuctionableHall;

import java.util.Map;

public class ExShowAgitInfo extends L2GameServerPacket
{
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
			case GC:
			case SL:
				writeH(0x16);
				break;
		}
		
		Map<Integer, AuctionableHall> clannhalls = ClanHallManager.getInstance().getAllAuctionableClanHalls();
		writeD(clannhalls.size());
		for (AuctionableHall ch : clannhalls.values())
		{
			writeD(ch.getId());
			writeS(ch.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(ch.getOwnerId()).getName()); // owner clan name
			writeS(ch.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(ch.getOwnerId()).getLeaderName()); // leader name
			writeD(ch.getGrade() > 0 ? 0x00 : 0x01); // 0 - auction 1 - war clanhall 2 - ETC (rainbow spring clanhall)
		}
	}
}
