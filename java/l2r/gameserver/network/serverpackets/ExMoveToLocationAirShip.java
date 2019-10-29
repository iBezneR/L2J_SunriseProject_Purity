package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;

public class ExMoveToLocationAirShip extends L2GameServerPacket
{
	private final int _objId, _tx, _ty, _tz, _x, _y, _z;
	
	public ExMoveToLocationAirShip(L2Character cha)
	{
		_objId = cha.getObjectId();
		_tx = cha.getXdestination();
		_ty = cha.getYdestination();
		_tz = cha.getZdestination();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
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
				writeH(0x65);
				break;
			case GC:
			case SL:
				writeH(0x66);
				break;
		}
		
		writeD(_objId);
		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}