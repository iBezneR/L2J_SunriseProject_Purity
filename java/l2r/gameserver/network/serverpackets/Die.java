package l2r.gameserver.network.serverpackets;

import gr.sr.configsEngine.configs.impl.ChaoticZoneConfigs;
import gr.sr.configsEngine.configs.impl.FlagZoneConfigs;
import gr.sr.interf.SunriseEvents;
import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.xml.impl.AdminData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.L2AccessLevel;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2SiegeClan;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.clanhall.SiegableHall;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Die extends L2GameServerPacket
{
	private final int _charObjId;
	private boolean _toVillage;
	private final boolean _sweepable;
	private L2AccessLevel _access = AdminData.getInstance().getAccessLevel(0);
	private L2Clan _clan;
	private final L2Character _activeChar;
	private boolean _isJailed;
	private boolean _useFeather = false;
	
	private List<Integer> _items = null;
	private boolean _hideAnimation;
	private boolean _itemsEnabled;
	
	public Die(L2Character cha)
	{
		_charObjId = cha.getObjectId();
		_activeChar = cha;
		if (cha.isPlayer())
		{
			L2PcInstance player = (L2PcInstance) cha;
			_access = player.getAccessLevel();
			_clan = player.getClan();
			_isJailed = player.isJailed();
		}
		_toVillage = !cha.isPendingRevive();
		_sweepable = cha.isSweepActive();
		
		if (cha.isPlayer())
		{
			L2PcInstance activeChar = cha.getActingPlayer();
			if (activeChar.isInsideZone(ZoneIdType.ZONE_CHAOTIC) && ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE_AUTO_REVIVE)
			{
				_toVillage = false;
			}
			
			if (activeChar.isInsideZone(ZoneIdType.FLAG) && FlagZoneConfigs.ENABLE_FLAG_ZONE_AUTO_REVIVE)
			{
				_toVillage = false;
			}
			
			if (SunriseEvents.isInEvent(activeChar))
			{
				if (!SunriseEvents.canShowToVillageWindow(activeChar))
				{
					_toVillage = false;
				}
			}
		}
	}
	
	public void setHideAnimation(boolean val)
	{
		_hideAnimation = val;
	}
	
	public void addItem(int itemId)
	{
		if (_items == null)
		{
			_items = new ArrayList<>(8);
		}
		
		if (_items.size() < 8)
		{
			_items.add(itemId);
		}
		else
		{
			throw new IndexOutOfBoundsException("Die packet doesn't support more then 8 items!");
		}
	}
	
	public List<Integer> getItems()
	{
		return _items != null ? _items : Collections.emptyList();
	}
	
	public void setItemsEnabled(boolean val)
	{
		_itemsEnabled = val;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x06);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x00);
				break;
		}
		
		writeD(_charObjId);
		writeD(_toVillage ? 0x01 : 0x00);
		
		if (_activeChar.isPlayer())
		{
			if (!OlympiadManager.getInstance().isRegistered(_activeChar.getActingPlayer()) && !_activeChar.isOnEvent())
			{
				_useFeather = _activeChar.getInventory().haveItemForSelfResurrection();
			}
			
			// Verify if player can use fixed resurrection without Feather
			if (_access.allowFixedRes())
			{
				_useFeather = true;
			}
		}
		
		if (_toVillage && (_clan != null) && !_isJailed)
		{
			boolean isInCastleDefense = false;
			boolean isInFortDefense = false;
			
			L2SiegeClan siegeClan = null;
			Castle castle = CastleManager.getInstance().getCastle(_activeChar);
			Fort fort = FortManager.getInstance().getFort(_activeChar);
			SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(_activeChar);
			if ((castle != null) && castle.getSiege().isInProgress())
			{
				// siege in progress
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && castle.getSiege().checkIsDefender(_clan))
				{
					isInCastleDefense = true;
				}
			}
			else if ((fort != null) && fort.getSiege().isInProgress())
			{
				// siege in progress
				siegeClan = fort.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && fort.getSiege().checkIsDefender(_clan))
				{
					isInFortDefense = true;
				}
			}
			
			writeD(_clan.getHideoutId() > 0 ? 0x01 : 0x00); // 6d 01 00 00 00 - to hide away
			writeD((_clan.getCastleId() > 0) || isInCastleDefense ? 0x01 : 0x00); // 6d 02 00 00 00 - to castle
			// vGodFather territory flag fix
			//@formatter:off
			writeD((TerritoryWarManager.getInstance().getHQForClan(_clan) != null)
				|| (TerritoryWarManager.getInstance().getFlagForClan(_clan) != null)
				|| ((siegeClan != null) && !isInCastleDefense && !isInFortDefense && !siegeClan.getFlag().isEmpty())
				|| ((hall != null) && hall.getSiege().checkIsAttacker(_clan)) ? 0x01 : 0x00); // 6d 03 00 00 00 - to siege HQ
			//@formatter:on
			writeD(_sweepable ? 0x01 : 0x00); // sweepable (blue glow)
			writeD(_useFeather ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					writeD((_clan.getFortId() > 0) || isInFortDefense ? 0x01 : 0x00); // 6d 05 00 00 00 - to fortress
					break;
			}
		}
		else
		{
			writeD(0x00); // 6d 01 00 00 00 - to hide away
			writeD(0x00); // 6d 02 00 00 00 - to castle
			writeD(0x00); // 6d 03 00 00 00 - to siege HQ
			writeD(_sweepable ? 0x01 : 0x00); // sweepable (blue glow)
			writeD(_useFeather ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					writeD(0x00); // 6d 05 00 00 00 - to fortress
					break;
			}
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeC(0); // show die animation
				writeD(0); // agathion ress button
				writeD(0); // additional free space
				break;
			case GC:
			case SL:
				writeD(0x00); // Disables use Feather button for X seconds
				writeD(0x00); // Adventure's Song
				writeC(_hideAnimation ? 0x01 : 0x00);
				
				writeD(_itemsEnabled ? 0x01 : 0x00);
				writeD(getItems().size());
				getItems().forEach(this::writeD);
				break;
		}
	}
}
