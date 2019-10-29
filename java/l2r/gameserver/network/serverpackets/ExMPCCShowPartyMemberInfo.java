package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private final L2Party _party;
	
	public ExMPCCShowPartyMemberInfo(L2Party party)
	{
		_party = party;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x4A);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x4B);
				break;
			case GC:
			case SL:
				writeH(0x4C);
				break;
		}
		
		writeD(_party.getMemberCount());
		for (L2PcInstance pc : _party.getMembers())
		{
			writeS(pc.getName());
			writeD(pc.getObjectId());
			writeD(pc.getClassId().getId());
		}
	}
}
