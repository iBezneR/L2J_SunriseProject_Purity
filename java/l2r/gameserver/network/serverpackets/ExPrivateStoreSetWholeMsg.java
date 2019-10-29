package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExPrivateStoreSetWholeMsg extends L2GameServerPacket
{
	private final int _objectId;
	private final String _msg;
	
	public ExPrivateStoreSetWholeMsg(L2PcInstance player, String msg)
	{
		_objectId = player.getObjectId();
		_msg = msg;
	}
	
	public ExPrivateStoreSetWholeMsg(L2PcInstance player)
	{
		this(player, player.getSellList().getTitle());
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
				writeH(0x80);
				break;
			case GC:
			case SL:
				writeH(0x81);
				break;
		}
		
		writeD(_objectId);
		writeS(_msg);
	}
}
