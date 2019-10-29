package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class TutorialEnableClientEvent extends L2GameServerPacket
{
	private int _eventId = 0;
	
	public TutorialEnableClientEvent(int event)
	{
		_eventId = event;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xA2);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xA8);
				break;
		}
		
		writeD(_eventId);
	}
}
