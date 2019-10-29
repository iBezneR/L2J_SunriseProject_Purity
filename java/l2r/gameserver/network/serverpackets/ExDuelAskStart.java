package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExDuelAskStart extends L2GameServerPacket
{
	private final String _requestorName;
	private final int _partyDuel;
	
	public ExDuelAskStart(String requestor, int partyDuel)
	{
		_requestorName = requestor;
		_partyDuel = partyDuel;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x4B);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x4C);
				break;
			case GC:
			case SL:
				writeH(0x4D);
				break;
		}
		
		writeS(_requestorName);
		writeD(_partyDuel);
	}
}
