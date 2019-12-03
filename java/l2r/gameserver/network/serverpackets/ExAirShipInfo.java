package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.actor.instance.L2AirShipInstance;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExAirShipInfo extends L2GameServerPacket
{
	// store some parameters, because they can be changed during broadcast
	private final L2AirShipInstance _ship;
	private final int _x, _y, _z, _heading, _moveSpeed, _rotationSpeed, _captain, _helm;
	
	public ExAirShipInfo(L2AirShipInstance ship)
	{
		_ship = ship;
		_x = ship.getX();
		_y = ship.getY();
		_z = ship.getZ();
		_heading = ship.getHeading();
		_moveSpeed = (int) ship.getStat().getMoveSpeed();
		_rotationSpeed = (int) ship.getStat().getRotationSpeed();
		_captain = ship.getCaptainId();
		_helm = ship.getHelmObjectId();
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
				writeH(0x60);
				break;
			case GC:
			case SL:
				writeH(0x61);
				break;
		}
		
		writeD(_ship.getObjectId());
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		
		writeD(_captain);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_helm);
		if (_helm != 0)
		{
			// TODO: unhardcode these!
			writeD(0x16e); // Controller X
			writeD(0x00); // Controller Y
			writeD(0x6b); // Controller Z
			writeD(0x15c); // Captain X
			writeD(0x00); // Captain Y
			writeD(0x69); // Captain Z
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		
		writeD(_ship.getFuel());
		writeD(_ship.getMaxFuel());
	}
}