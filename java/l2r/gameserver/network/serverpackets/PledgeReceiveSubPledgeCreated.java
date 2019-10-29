package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Clan.SubPledge;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
	private final SubPledge _subPledge;
	private final L2Clan _clan;
	
	public PledgeReceiveSubPledgeCreated(SubPledge subPledge, L2Clan clan)
	{
		_subPledge = subPledge;
		_clan = clan;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x3F);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x40);
				break;
			case GC:
			case SL:
				writeH(0x41);
				break;
		}
		
		writeD(0x01);
		writeD(_subPledge.getId());
		writeS(_subPledge.getName());
		writeS(getLeaderName());
	}
	
	private String getLeaderName()
	{
		int LeaderId = _subPledge.getLeaderId();
		if ((_subPledge.getId() == L2Clan.SUBUNIT_ACADEMY) || (LeaderId == 0))
		{
			return "";
		}
		else if (_clan.getClanMember(LeaderId) == null)
		{
			_log.warn("SubPledgeLeader: " + LeaderId + " is missing from clan: " + _clan.getName() + "[" + _clan.getId() + "]");
			return "";
		}
		else
		{
			return _clan.getClanMember(LeaderId).getName();
		}
	}
}
