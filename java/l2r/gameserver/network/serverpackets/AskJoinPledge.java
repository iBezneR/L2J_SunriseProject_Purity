package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class AskJoinPledge extends L2GameServerPacket
{
	private final L2PcInstance _requestor;
	private final String _subPledgeName;
	private final int _pledgeType;
	private final String _pledgeName;
	
	public AskJoinPledge(L2PcInstance requestor, String subPledgeName, int pledgeType, String pledgeName)
	{
		_requestor = requestor;
		_subPledgeName = subPledgeName;
		_pledgeType = pledgeType;
		_pledgeName = pledgeName;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x32);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x2C);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_requestor.getObjectId());
				if (_subPledgeName != null)
				{
					writeS(_pledgeType > 0 ? _subPledgeName : _pledgeName);
				}
				if (_pledgeType != 0)
				{
					writeD(_pledgeType);
				}
				writeS(_pledgeName);
				break;
			case GC:
			case SL:
				writeD(_requestor.getObjectId());
				writeS(_requestor.getName());
				writeS(_pledgeName);
				if (_pledgeType != 0)
				{
					writeD(_pledgeType);
				}
				break;
		}
	}
}
