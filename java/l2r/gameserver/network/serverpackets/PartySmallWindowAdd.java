package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class PartySmallWindowAdd extends L2GameServerPacket
{
	private final L2PcInstance _member;
	private final L2Party _party;
	
	public PartySmallWindowAdd(L2PcInstance member, L2Party party)
	{
		_member = member;
		_party = party;
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
				writeC(0x4F);
				break;
		}
		
		writeD(_party.getLeaderObjectId()); // c3
		writeD(_party.getDistributionType().getId());// writeD(0x04); ?? //c3
		writeD(_member.getObjectId());
		writeS(_member.getName());
		
		writeD((int) _member.getCurrentCp()); // c4
		writeD(_member.getMaxCp()); // c4
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_member.getLevel());
				writeD(_member.getClassId().getId());
				writeD(0x00); // ?
				writeD(0x00); // ?
				break;
			case GC:
			case SL:
				writeD(_member.getVitalityPoints());
				writeC(_member.getLevel());
				writeH(_member.getClassId().getId());
				writeC(0x00);
				writeH(_member.getRace().ordinal());
				break;
		}
	}
}
