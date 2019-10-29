package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _boatId;
	private final Location _destination;
	private final Location _origin;
	
	public MoveToLocationInVehicle(L2PcInstance player, Location destination, Location origin)
	{
		_charObjId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x71);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x7E);
				break;
		}
		
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_origin.getX());
		writeD(_origin.getY());
		writeD(_origin.getZ());
	}
}
