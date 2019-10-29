package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2AirShipInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExAirShipStopMove extends L2GameServerPacket
{
	private final int _playerId, _airShipId, _x, _y, _z;
	
	public ExAirShipStopMove(L2PcInstance player, L2AirShipInstance ship, int x, int y, int z)
	{
		_playerId = player.getObjectId();
		_airShipId = ship.getObjectId();
		_x = x;
		_y = y;
		_z = z;
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
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x66);
				break;
			case GC:
			case SL:
				writeH(0x67);
				break;
		}
		
		writeD(_airShipId);
		writeD(_playerId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}