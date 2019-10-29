package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.actor.instance.L2ServitorInstance;

public class PetStatusUpdate extends L2GameServerPacket
{
	private final L2Summon _summon;
	private int _maxFed, _curFed;
	
	public PetStatusUpdate(L2Summon summon)
	{
		_summon = summon;
		if (_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (_summon instanceof L2ServitorInstance)
		{
			L2ServitorInstance sum = (L2ServitorInstance) _summon;
			_curFed = sum.getTimeRemaining();
			_maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xB5);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xB6);
				break;
		}
		
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeS(_summon.getTitle());
		writeD(_curFed);
		writeD(_maxFed);
		writeD((int) _summon.getCurrentHp());
		writeD(_summon.getMaxHp());
		writeD((int) _summon.getCurrentMp());
		writeD(_summon.getMaxMp());
		writeD(_summon.getLevel());
		writeQ(_summon.getStat().getExp());
		writeQ(_summon.getExpForThisLevel()); // 0% absolute value
		writeQ(_summon.getExpForNextLevel()); // 100% absolute value
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(0x01); // TODO: Find me!
				break;
		}
	}
}
