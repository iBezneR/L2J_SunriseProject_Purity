package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public final class MagicSkillCanceld extends L2GameServerPacket
{
	private final int _objectId;
	
	public MagicSkillCanceld(int objectId)
	{
		_objectId = objectId;
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
				writeC(0x49);
				break;
		}
		
		writeD(_objectId);
	}
}
