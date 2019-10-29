package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ValidateLocationInVehicle extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _boatObjId;
	private final int _heading;
	private final Location _pos;
	
	public ValidateLocationInVehicle(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_boatObjId = player.getBoat().getObjectId();
		_heading = player.getHeading();
		_pos = player.getInVehiclePosition();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x73);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x80);
				break;
		}
		
		writeD(_charObjId);
		writeD(_boatObjId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
		writeD(_heading);
	}
}
