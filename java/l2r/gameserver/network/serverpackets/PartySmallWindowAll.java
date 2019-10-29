package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public final class PartySmallWindowAll extends L2GameServerPacket
{
	private final L2Party _party;
	private final L2PcInstance _exclude;
	
	public PartySmallWindowAll(L2PcInstance exclude, L2Party party)
	{
		_exclude = exclude;
		_party = party;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x4E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x4E);
				break;
		}
		
		writeD(_party.getLeaderObjectId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_party.getDistributionType().getId());
				writeD(_party.getMemberCount() - 1);
				break;
			case GC:
			case SL:
				writeC(_party.getDistributionType().getId());
				writeC(_party.getMemberCount() - 1);
				break;
		}
		
		for (L2PcInstance member : _party.getMembers())
		{
			if ((member != null) && (member != _exclude))
			{
				writeD(member.getObjectId());
				writeS(member.getName());
				
				writeD((int) member.getCurrentCp()); // c4
				writeD(member.getMaxCp()); // c4
				
				writeD((int) member.getCurrentHp());
				writeD(member.getMaxHp());
				writeD((int) member.getCurrentMp());
				writeD(member.getMaxMp());
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
						writeD(member.getLevel());
						writeD(member.getClassId().getId());
						writeD(0x00);// writeD(0x01); ??
						writeD(member.getRace().ordinal());
						writeD(0x00); // T2.3
						writeD(0x00); // T2.3
						break;
					case GC:
					case SL:
						writeD(member.getVitalityPoints());
						writeC(member.getLevel());
						writeH(member.getClassId().getId());
						writeC(0x00);
						writeH(member.getRace().ordinal());
						break;
				}
				
				if (member.hasSummon())
				{
					writeD(member.getSummon().getObjectId());
					writeD(member.getSummon().getId() + 1000000);
					
					switch (ServerTypeConfigs.SERVER_TYPE)
					{
						case IL:
						case GF:
						case EPILOGUE:
						case FREYA:
						case H5:
							writeD(member.getSummon().getSummonType());
							break;
						case GC:
						case SL:
							writeC(member.getSummon().getSummonType());
							break;
					}
					
					writeS(member.getSummon().getName());
					writeD((int) member.getSummon().getCurrentHp());
					writeD(member.getSummon().getMaxHp());
					writeD((int) member.getSummon().getCurrentMp());
					writeD(member.getSummon().getMaxMp());
					
					switch (ServerTypeConfigs.SERVER_TYPE)
					{
						case IL:
						case GF:
						case EPILOGUE:
						case FREYA:
						case H5:
							writeD(member.getSummon().getLevel());
							break;
						case GC:
						case SL:
							writeC(member.getSummon().getLevel());
							break;
					}
				}
				else
				{
					writeD(0x00);
				}
			}
		}
	}
}
