package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class FriendAddRequest extends L2GameServerPacket
{
	private final String _requestorName;
	
	public FriendAddRequest(String requestorName)
	{
		_requestorName = requestorName;
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
				writeC(0x83);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeS(_requestorName);
				writeD(0x00);
				break;
			case GC:
			case SL:
				writeC(0x01);
				writeS(_requestorName);
				break;
		}
	}
}
