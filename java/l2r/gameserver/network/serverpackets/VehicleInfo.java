package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2BoatInstance;

public class VehicleInfo extends L2GameServerPacket
{
	private final int _objId, _x, _y, _z, _heading;
	
	public VehicleInfo(L2BoatInstance boat)
	{
		_objId = boat.getObjectId();
		_x = boat.getX();
		_y = boat.getY();
		_z = boat.getZ();
		_heading = boat.getHeading();
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x59);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x60);
				break;
		}
		
		writeD(_objId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
}
