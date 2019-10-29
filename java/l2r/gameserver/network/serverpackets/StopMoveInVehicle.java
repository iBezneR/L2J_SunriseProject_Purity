package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class StopMoveInVehicle extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _boatId;
	private final Location _pos;
	private final int _heading;
	
	public StopMoveInVehicle(L2PcInstance player, int boatId)
	{
		_charObjId = player.getObjectId();
		_boatId = boatId;
		_pos = player.getInVehiclePosition();
		_heading = player.getHeading();
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x72);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x7F);
				break;
		}
		
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
		writeD(_heading);
	}
}
