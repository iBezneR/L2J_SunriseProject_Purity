package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.entity.Fort;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExShowFortressInfo extends L2GameServerPacket
{
	public static final ExShowFortressInfo STATIC_PACKET = new ExShowFortressInfo();
	
	private ExShowFortressInfo()
	{
	
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
			case GC:
			case SL:
				writeH(0x15);
				break;
		}
		
		final List<Fort> forts = FortManager.getInstance().getForts();
		writeD(forts.size());
		for (Fort fort : forts)
		{
			L2Clan clan = fort.getOwnerClan();
			writeD(fort.getResidenceId());
			writeS(clan != null ? clan.getName() : "");
			writeD(fort.getSiege().isInProgress() ? 0x01 : 0x00);
			// Time of possession
			writeD(fort.getOwnedTime());
		}
	}
}
