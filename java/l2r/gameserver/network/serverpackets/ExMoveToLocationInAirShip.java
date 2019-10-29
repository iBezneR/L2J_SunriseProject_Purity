package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExMoveToLocationInAirShip extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _airShipId;
	private final Location _destination;
	private final int _heading;
	
	public ExMoveToLocationInAirShip(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_airShipId = player.getAirShip().getObjectId();
		_destination = player.getInVehiclePosition();
		_heading = player.getHeading();
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
				writeH(0x6D);
				break;
			case GC:
			case SL:
				writeH(0x6E);
				break;
		}
		
		writeD(_charObjId);
		writeD(_airShipId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_heading);
	}
}