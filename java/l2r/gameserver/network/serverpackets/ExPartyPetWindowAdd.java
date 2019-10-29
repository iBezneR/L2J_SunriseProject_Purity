package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Summon;

public final class ExPartyPetWindowAdd extends L2GameServerPacket
{
	private final L2Summon _summon;
	
	public ExPartyPetWindowAdd(L2Summon summon)
	{
		_summon = summon;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x18);
				break;
		}
		
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getDisplayId() + 1000000);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_summon.getSummonType());
				break;
			case GC:
			case SL:
				writeC(_summon.getSummonType());
				break;
		}
		
		writeD(_summon.getOwner().getObjectId());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeS(_summon.getName());
				break;
		}
		
		writeD((int) _summon.getCurrentHp());
		writeD(_summon.getMaxHp());
		writeD((int) _summon.getCurrentMp());
		writeD(_summon.getMaxMp());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_summon.getLevel());
				break;
		}
	}
}
