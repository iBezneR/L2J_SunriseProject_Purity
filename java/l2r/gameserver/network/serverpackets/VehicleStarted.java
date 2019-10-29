package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;

public class VehicleStarted extends L2GameServerPacket
{
	private final int _objectId;
	private final int _state;
	
	public VehicleStarted(L2Character boat, int state)
	{
		_objectId = boat.getObjectId();
		_state = state;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xBA);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xC0);
				break;
		}
		
		writeD(_objectId);
		writeD(_state);
	}
}
