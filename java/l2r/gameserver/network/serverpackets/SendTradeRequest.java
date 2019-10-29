package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class SendTradeRequest extends L2GameServerPacket
{
	private final int _senderID;
	
	public SendTradeRequest(int senderID)
	{
		_senderID = senderID;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x5E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x70);
				break;
		}
		
		writeD(_senderID);
	}
}
