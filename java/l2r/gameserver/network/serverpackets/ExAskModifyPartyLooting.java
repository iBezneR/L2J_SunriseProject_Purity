package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.enums.PartyDistributionType;

public class ExAskModifyPartyLooting extends L2GameServerPacket
{
	private final String _requestor;
	private final PartyDistributionType _partyDistributionType;
	
	public ExAskModifyPartyLooting(String name, PartyDistributionType partyDistributionType)
	{
		_requestor = name;
		_partyDistributionType = partyDistributionType;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case FREYA:
				writeH(0xBE);
				break;
			case H5:
				writeH(0xBF);
				break;
			case GC:
			case SL:
				writeH(0xC0);
				break;
		}
		
		writeS(_requestor);
		writeD(_partyDistributionType.getId());
	}
}
