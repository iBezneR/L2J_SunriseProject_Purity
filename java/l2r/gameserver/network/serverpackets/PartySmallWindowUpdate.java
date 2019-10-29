package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.handlers.types.PartySmallWindowUpdateType;

public final class PartySmallWindowUpdate extends AbstractMaskPacket<PartySmallWindowUpdateType>
{
	private final L2PcInstance _member;
	private int _flags = 0;
	
	public PartySmallWindowUpdate(L2PcInstance member)
	{
		_member = member;
		
		// if (addAllFlags)
		// {
		addComponentType(PartySmallWindowUpdateType.values());
		// }
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
				writeC(0x52);
				break;
		}
		
		writeD(_member.getObjectId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeH(_flags);
				if (containsMask(PartySmallWindowUpdateType.CURRENT_CP))
				{
					writeD((int) _member.getCurrentCp()); // c4
				}
				if (containsMask(PartySmallWindowUpdateType.MAX_CP))
				{
					writeD(_member.getMaxCp()); // c4
				}
				if (containsMask(PartySmallWindowUpdateType.CURRENT_HP))
				{
					writeD((int) _member.getCurrentHp());
				}
				if (containsMask(PartySmallWindowUpdateType.MAX_HP))
				{
					writeD(_member.getMaxHp());
				}
				if (containsMask(PartySmallWindowUpdateType.CURRENT_MP))
				{
					writeD((int) _member.getCurrentMp());
				}
				if (containsMask(PartySmallWindowUpdateType.MAX_MP))
				{
					writeD(_member.getMaxMp());
				}
				if (containsMask(PartySmallWindowUpdateType.LEVEL))
				{
					writeC(_member.getLevel());
				}
				if (containsMask(PartySmallWindowUpdateType.CLASS_ID))
				{
					writeH(_member.getClassId().getId());
				}
				if (containsMask(PartySmallWindowUpdateType.PARTY_SUBSTITUTE))
				{
					writeC(0x00);
				}
				if (containsMask(PartySmallWindowUpdateType.VITALITY_POINTS))
				{
					writeD(_member.getVitalityPoints());
				}
				return;
		}
		
		writeS(_member.getName());
		
		writeD((int) _member.getCurrentCp()); // c4
		writeD(_member.getMaxCp()); // c4
		
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		writeD(_member.getLevel());
		writeD(_member.getClassId().getId());
	}
	
	@Override
	protected void addMask(int mask)
	{
		_flags |= mask;
	}
	
	@Override
	public boolean containsMask(PartySmallWindowUpdateType component)
	{
		return containsMask(_flags, component);
	}
	
	@Override
	protected byte[] getMasks()
	{
		return new byte[0];
	}
}
