package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private int _mask;
	
	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_mask = _activeChar.getMessageRefusal() || _activeChar.isChatBanned() || _activeChar.isSilenceMode() ? 1 : 0;
		_mask |= _activeChar.isInsideZone(ZoneIdType.DANGER_AREA) ? 2 : 0;
		_mask |= _activeChar.hasCharmOfCourage() ? 4 : 0;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xF3);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xF9);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeC(_activeChar.getCharges()); // 1-7 increase force, lvl
				writeD(_activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
				writeC(_activeChar.getExpertiseWeaponPenalty()); // Weapon Grade Penalty [1-4]
				writeC(_activeChar.getExpertiseArmorPenalty()); // Armor Grade Penalty [1-4]
				writeC(0); // Death Penalty [1-15, 0 = disabled)], not used anymore in Ertheia
				writeC(_activeChar.getChargedSouls());
				writeC(_mask);
				return;
		}
		
		writeD(_activeChar.getCharges()); // 1-7 increase force, lvl
		writeD(_activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeD((_activeChar.getMessageRefusal() || _activeChar.isChatBanned() || _activeChar.isSilenceMode()) ? 1 : 0); // 1 = block all chat
		writeD(_activeChar.isInsideZone(ZoneIdType.DANGER_AREA) ? 1 : 0); // 1 = danger area
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				int penalty = Math.max(_activeChar.getExpertiseWeaponPenalty(), _activeChar.getExpertiseArmorPenalty());
				writeD(Math.min(penalty, 1)); // 1 = grade penalty weapon ?
				writeD(_activeChar.hasCharmOfCourage() ? 1 : 0); // 1 = charm of courage
				writeD(_activeChar.getDeathPenaltyBuffLevel()); // 1-15 death penalty
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(Math.min(_activeChar.getExpertiseWeaponPenalty(), 4)); // 1 = grade penalty weapon ?
				writeD(Math.min(_activeChar.getExpertiseArmorPenalty(), 4)); // 1 = grade penalty armor ?
				writeD(_activeChar.hasCharmOfCourage() ? 1 : 0); // 1 = charm of courage
				writeD(_activeChar.getDeathPenaltyBuffLevel()); // 1-15 death penalty
				writeD(_activeChar.getChargedSouls()); // soul count
				break;
		}
	}
}
