package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.TeleportBookmark;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExGetBookMarkInfoPacket extends L2GameServerPacket
{
	private final L2PcInstance player;
	
	public ExGetBookMarkInfoPacket(L2PcInstance cha)
	{
		player = cha;
	}
	
	@Override
	protected final void writeImpl()
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
				writeH(0x84);
				break;
			case GC:
			case SL:
				writeH(0x85);
				break;
		}
		
		writeD(0x00); // Dummy
		writeD(player.getBookmarkslot());
		writeD(player.getTeleportBookmarks().size());
		
		for (TeleportBookmark tpbm : player.getTeleportBookmarks())
		{
			writeD(tpbm.getId());
			writeD(tpbm.getX());
			writeD(tpbm.getY());
			writeD(tpbm.getZ());
			writeS(tpbm.getName());
			writeD(tpbm.getIcon());
			writeS(tpbm.getTag());
		}
	}
}