package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopMsg extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	
	public RecipeShopMsg(L2PcInstance player)
	{
		_activeChar = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xDB);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xE1);
				break;
		}
		
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getStoreName());
	}
}
