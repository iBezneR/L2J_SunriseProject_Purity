package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.sql.CharNameTable;
import l2r.gameserver.model.L2World;

public class FriendStatusPacket extends L2GameServerPacket
{
	private final boolean _online;
	private final int _objid;
	private final String _name;
	
	public FriendStatusPacket(int objId)
	{
		_objid = objId;
		_name = CharNameTable.getInstance().getNameById(objId);
		_online = L2World.getInstance().getPlayer(objId) != null;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xFC);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x77);
				break;
		}
		
		writeD(_online ? 1 : 0);
		writeS(_name);
		writeD(_objid);
	}
}
