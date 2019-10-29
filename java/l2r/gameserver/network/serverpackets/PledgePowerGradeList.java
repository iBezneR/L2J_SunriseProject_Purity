package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Clan.RankPrivs;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private final RankPrivs[] _privs;
	
	public PledgePowerGradeList(RankPrivs[] privs)
	{
		_privs = privs;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x3B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x3C);
				break;
			case GC:
			case SL:
				writeH(0x3D);
				break;
		}
		
		writeD(_privs.length);
		for (RankPrivs temp : _privs)
		{
			writeD(temp.getRank());
			writeD(temp.getParty());
		}
	}
}
