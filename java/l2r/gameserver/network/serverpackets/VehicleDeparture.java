package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2BoatInstance;

public class VehicleDeparture extends L2GameServerPacket
{
	private final int _objId, _x, _y, _z, _moveSpeed, _rotationSpeed;
	
	public VehicleDeparture(L2BoatInstance boat)
	{
		_objId = boat.getObjectId();
		_x = boat.getXdestination();
		_y = boat.getYdestination();
		_z = boat.getZdestination();
		_moveSpeed = (int) boat.getMoveSpeed();
		_rotationSpeed = (int) boat.getStat().getRotationSpeed();
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x5A);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x6C);
				break;
		}
		
		writeD(_objId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
