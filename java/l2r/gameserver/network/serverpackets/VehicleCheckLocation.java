package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;

public class VehicleCheckLocation extends L2GameServerPacket
{
	private final L2Character _boat;
	
	public VehicleCheckLocation(L2Character boat)
	{
		_boat = boat;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x5B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x6D);
				break;
		}
		
		writeD(_boat.getObjectId());
		writeD(_boat.getX());
		writeD(_boat.getY());
		writeD(_boat.getZ());
		writeD(_boat.getHeading());
	}
}
