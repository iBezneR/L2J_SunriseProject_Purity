package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2DoorInstance;

public final class DoorStatusUpdate extends L2GameServerPacket
{
	private final L2DoorInstance _door;
	
	public DoorStatusUpdate(L2DoorInstance door)
	{
		_door = door;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x4D);
				break;
		}
		
		writeD(_door.getObjectId());
		writeD(_door.isOpened() ? 0 : 1);
		writeD(_door.getDamage());
		writeD(_door.isEnemy() ? 1 : 0);
		writeD(_door.getId());
		writeD((int) _door.getCurrentHp());
		writeD(_door.getMaxHp());
	}
}