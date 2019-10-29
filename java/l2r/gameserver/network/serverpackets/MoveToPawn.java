package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;

public class MoveToPawn extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _targetId;
	private final int _distance;
	private final int _x, _y, _z, _tx, _ty, _tz;
	
	public MoveToPawn(L2Character cha, L2Character target, int distance)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x60);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x72);
				break;
		}
		
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_distance);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
	}
}
