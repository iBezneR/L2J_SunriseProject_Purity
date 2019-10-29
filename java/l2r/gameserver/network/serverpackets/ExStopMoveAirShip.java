package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;

public class ExStopMoveAirShip extends L2GameServerPacket
{
	private final int _objectId, _x, _y, _z, _heading;
	
	public ExStopMoveAirShip(L2Character ship)
	{
		_objectId = ship.getObjectId();
		_x = ship.getX();
		_y = ship.getY();
		_z = ship.getZ();
		_heading = ship.getHeading();
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
				writeH(0x66);
				break;
			case GC:
			case SL:
				writeH(0x67);
				break;
		}
		
		writeD(_objectId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
}
