package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExFishingEnd extends L2GameServerPacket
{
	private final boolean _win;
	private final L2Character _activeChar;
	
	public ExFishingEnd(boolean win, L2PcInstance character)
	{
		_win = win;
		_activeChar = character;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x14);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x1F);
				break;
		}
		
		writeD(_activeChar.getObjectId());
		writeC(_win ? 1 : 0);
	}
}