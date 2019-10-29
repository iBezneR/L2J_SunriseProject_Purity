package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class StopRotation extends L2GameServerPacket
{
	private final int _charObjId, _degree, _speed;
	
	public StopRotation(int objectId, int degree, int speed)
	{
		_charObjId = objectId;
		_degree = degree;
		_speed = speed;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x63);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x61);
				break;
		}
		
		writeD(_charObjId);
		writeD(_degree);
		writeD(_speed);
		writeC(0); // ?
	}
}
