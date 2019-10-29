package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class L2FriendSay extends L2GameServerPacket
{
	private final String _sender, _receiver, _message;
	
	public L2FriendSay(String sender, String reciever, String message)
	{
		_sender = sender;
		_receiver = reciever;
		_message = message;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xFD);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x78);
				break;
		}
		
		writeD(0); // ??
		writeS(_receiver);
		writeS(_sender);
		writeS(_message);
	}
}
