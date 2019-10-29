package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExStopMoveInAirShip extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _shipObjId;
	private final int x, y, z, h;
	
	public ExStopMoveInAirShip(L2PcInstance player, int shipObjId)
	{
		_activeChar = player;
		_shipObjId = shipObjId;
		x = player.getInVehiclePosition().getX();
		y = player.getInVehiclePosition().getY();
		z = player.getInVehiclePosition().getZ();
		h = player.getHeading();
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
				writeH(0x6E);
				break;
			case GC:
			case SL:
				writeH(0x6F);
				break;
		}
		
		writeD(_activeChar.getObjectId());
		writeD(_shipObjId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(h);
	}
}
