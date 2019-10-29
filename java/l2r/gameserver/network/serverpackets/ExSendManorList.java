package l2r.gameserver.network.serverpackets;

import java.util.Comparator;
import java.util.List;

import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.model.entity.Castle;

import gr.sr.network.handler.ServerTypeConfigs;

public final class ExSendManorList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x1B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x22);
				break;
		}
		
		final List<Castle> castles = CastleManager.getInstance().getCastles();
		castles.sort(Comparator.comparing(Castle::getResidenceId));
		
		writeD(castles.size());
		for (Castle castle : castles)
		{
			writeD(castle.getResidenceId());
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					writeS(castle.getName().toLowerCase());
					break;
			}
		}
	}
}