package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;

public class ValidateLocation extends L2GameServerPacket
{
	private final int _charObjId;
	private final Location _loc;
	
	public ValidateLocation(L2Object obj)
	{
		_charObjId = obj.getObjectId();
		_loc = obj.getLocation();
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x61);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x79);
				break;
		}
		
		writeD(_charObjId);
		writeLoc(_loc);
		writeD(_loc.getHeading());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(0xFF); // TODO find me
				break;
		}
	}
}
