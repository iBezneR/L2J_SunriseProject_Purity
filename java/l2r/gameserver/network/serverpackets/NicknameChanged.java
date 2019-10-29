package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;

public class NicknameChanged extends L2GameServerPacket
{
	private final String _title;
	private final int _objectId;
	
	public NicknameChanged(L2Character cha)
	{
		_objectId = cha.getObjectId();
		_title = cha.getTitle();
	}
	
	@Override
	protected void writeImpl()
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
				writeC(0xCC);
				break;
		}
		
		writeD(_objectId);
		writeS(_title);
	}
}
