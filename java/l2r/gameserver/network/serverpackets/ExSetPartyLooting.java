package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.enums.PartyDistributionType;

public class ExSetPartyLooting extends L2GameServerPacket
{
	private final int _result;
	private final PartyDistributionType _partyDistributionType;
	
	public ExSetPartyLooting(int result, PartyDistributionType partyDistributionType)
	{
		_result = result;
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
				writeH(0xBF);
				break;
			case H5:
				writeH(0xC0);
				break;
			case GC:
			case SL:
				writeH(0xC1);
				break;
		}
		
		writeD(_result);
		writeD(_partyDistributionType.getId());
	}
}
