package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExBrGamePoint extends L2GameServerPacket
{
	private final int _objId;
	private long _points;
	
	public ExBrGamePoint(L2PcInstance player)
	{
		_objId = player.getObjectId();
		
		if (Config.GAME_POINT_ITEM_ID == -1)
		{
			_points = player.getGamePoints();
		}
		else
		{
			_points = player.getInventory().getInventoryItemCount(Config.GAME_POINT_ITEM_ID, -100);
		}
	}
	
	@Override
	public void writeImpl()
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
				writeH(0xA7);
				break;
			case EPILOGUE:
				writeH(0xB8);
				break;
			case FREYA:
				writeH(0xC9);
				break;
			case H5:
				writeH(0xD5);
				break;
			case GC:
			case SL:
				writeH(0xD6);
				break;
		}
		
		writeD(_objId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeQ(_points);
				break;
			case GC:
			case SL:
				writeD((int) _points);
				break;
		}
		
		writeD(0x00);
	}
}