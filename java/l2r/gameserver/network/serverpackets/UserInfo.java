package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.data.xml.impl.ExperienceData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.network.handlers.types.UserInfoType;

public final class UserInfo extends AbstractMaskPacket<UserInfoType>
{
	private final L2PcInstance _activeChar;
	private int _relation;
	private int _airShipHelm;
	
	private final int _runSpd, _walkSpd;
	private final int _swimRunSpd, _swimWalkSpd;
	private final int _flyRunSpd, _flyWalkSpd;
	private final double _moveMultiplier;
	
	private String _title;
	
	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	
	private int _initSize = 5;
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	protected void onNewMaskAdded(UserInfoType component)
	{
		calcBlockSize(component);
	}
	
	private void calcBlockSize(UserInfoType type)
	{
		switch (type)
		{
			case BASIC_INFO:
			{
				_initSize += type.getBlockLength() + (_activeChar.getAppearance().getVisibleName().length() * 2);
				break;
			}
			case CLAN:
			{
				_initSize += type.getBlockLength() + (_title.length() * 2);
				break;
			}
			default:
			{
				_initSize += type.getBlockLength();
				break;
			}
		}
	}
	
	public UserInfo(L2PcInstance cha)
	{
		_activeChar = cha;
		
		int _territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(cha);
		_relation = _activeChar.isClanLeader() ? 0x40 : 0;
		if (_activeChar.getSiegeState() == 1)
		{
			if (_territoryId == 0)
			{
				_relation |= 0x180;
			}
			else
			{
				_relation |= 0x1000;
			}
		}
		if (_activeChar.getSiegeState() == 2)
		{
			_relation |= 0x80;
		}
		// _isDisguised = TerritoryWarManager.getInstance().isDisguised(character.getObjectId());
		if (_activeChar.isInAirShip() && _activeChar.getAirShip().isCaptain(_activeChar))
		{
			_airShipHelm = _activeChar.getAirShip().getHelmItemId();
		}
		else
		{
			_airShipHelm = 0;
		}
		
		_moveMultiplier = cha.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(cha.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(cha.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(cha.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = cha.isFlying() ? _runSpd : 0;
		_flyWalkSpd = cha.isFlying() ? _walkSpd : 0;
		
		_title = _activeChar.getTitle();
		if (_activeChar.isGM() && _activeChar.isInvisible())
		{
			_title = "Invisible";
		}
		
		addComponentType(UserInfoType.values());
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x04);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x32);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(_activeChar.getObjectId());
				writeD(_initSize);
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GC:
						writeH(23);
						break;
					case SL:
						writeH(24);
						break;
				}
				
				writeB(_masks);
				
				if (containsMask(UserInfoType.RELATION))
				{
					writeD(_relation);
				}
				
				if (containsMask(UserInfoType.BASIC_INFO))
				{
					final String name = _activeChar.getAppearance().getVisibleName();
					writeH(UserInfoType.BASIC_INFO.getBlockLength() + (name.length() * 2));
					writeString(name);
					
					writeC(_activeChar.isGM() ? 0x01 : 0x00);
					writeC(_activeChar.getRace().ordinal());
					writeC(_activeChar.getAppearance().getSex() ? 0x01 : 0x00);
					writeD(_activeChar.getBaseClass());
					writeD(_activeChar.getClassId().getId());
					writeC(_activeChar.getLevel());
				}
				
				if (containsMask(UserInfoType.BASE_STATS))
				{
					writeH(18);
					writeH(_activeChar.getSTR());
					writeH(_activeChar.getDEX());
					writeH(_activeChar.getCON());
					writeH(_activeChar.getINT());
					writeH(_activeChar.getWIT());
					writeH(_activeChar.getMEN());
					
					// writeH(_activeChar.getLUC());
					// writeH(_activeChar.getCHA());
					writeH(0x00);
					writeH(0x00);
				}
				
				if (containsMask(UserInfoType.MAX_HPCPMP))
				{
					writeH(14);
					writeD(_activeChar.getMaxHp());
					writeD(_activeChar.getMaxMp());
					writeD(_activeChar.getMaxCp());
				}
				
				if (containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
				{
					writeH(38);
					writeD((int) Math.round(_activeChar.getCurrentHp()));
					writeD((int) Math.round(_activeChar.getCurrentMp()));
					writeD((int) Math.round(_activeChar.getCurrentCp()));
					writeQ(_activeChar.getSp());
					writeQ(_activeChar.getExp());
					writeF((float) (_activeChar.getExp() - ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel())) / (ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel())));
				}
				
				if (containsMask(UserInfoType.ENCHANTLEVEL))
				{
					writeH(4);
					writeC(_activeChar.isMounted() || (_airShipHelm != 0) ? 0 : _activeChar.getInventory().getWeaponEnchant());
					// writeC(_armorEnchant);
					
					writeC(0x00);
				}
				
				if (containsMask(UserInfoType.APPAREANCE))
				{
					writeH(15);
					writeD(_activeChar.getAppearance().getHairStyle());
					writeD(_activeChar.getAppearance().getHairColor());
					writeD(_activeChar.getAppearance().getFace());
					
					// writeC(_activeChar.isHairAccessoryEnabled() ? 0x01 : 0x00);
					writeC(0x01);
				}
				
				if (containsMask(UserInfoType.STATUS))
				{
					writeH(6);
					writeC(_activeChar.getMountType().ordinal());
					writeC(_activeChar.getPrivateStoreType().getId());
					writeC(_activeChar.hasCrystallization() ? 1 : 0);
					// writeC(_activeChar.getAbilityPoints() - _activeChar.getAbilityPointsUsed());
					writeC(127);
				}
				
				if (containsMask(UserInfoType.STATS))
				{
					writeH(56);
					writeH(_activeChar.getActiveWeaponItem() != null ? 40 : 20);
					
					writeD((int) _activeChar.getPAtk(null));
					writeD((int) _activeChar.getPAtkSpd());
					writeD((int) _activeChar.getPDef(null));
					writeD(_activeChar.getEvasionRate(null));
					writeD(_activeChar.getAccuracy());
					writeD(_activeChar.getCriticalHit(null, null));
					writeD((int) _activeChar.getMAtk(null, null));
					
					writeD(_activeChar.getMAtkSpd());
					writeD((int) _activeChar.getPAtkSpd());
					
					// writeD(_activeChar.getMagicEvasionRate());
					writeD(100);
					writeD((int) _activeChar.getMDef(null, null));
					// writeD(_activeChar.getMagicAccuracy());
					writeD(100);
					writeD(_activeChar.getMCriticalHit(null, null));
				}
				
				if (containsMask(UserInfoType.ELEMENTALS))
				{
					writeH(14);
					writeH(_activeChar.getDefenseElementValue(Elementals.FIRE));
					writeH(_activeChar.getDefenseElementValue(Elementals.WATER));
					writeH(_activeChar.getDefenseElementValue(Elementals.WIND));
					writeH(_activeChar.getDefenseElementValue(Elementals.EARTH));
					writeH(_activeChar.getDefenseElementValue(Elementals.HOLY));
					writeH(_activeChar.getDefenseElementValue(Elementals.DARK));
				}
				
				if (containsMask(UserInfoType.POSITION))
				{
					writeH(18);
					writeD(_activeChar.getX());
					writeD(_activeChar.getY());
					writeD(_activeChar.getZ());
					writeD(_activeChar.isInVehicle() ? _activeChar.getVehicle().getObjectId() : 0);
				}
				
				if (containsMask(UserInfoType.SPEED))
				{
					writeH(18);
					writeH(_runSpd);
					writeH(_walkSpd);
					writeH(_swimRunSpd);
					writeH(_swimWalkSpd);
					writeH(_flyRunSpd);
					writeH(_flyWalkSpd);
					writeH(_flyRunSpd);
					writeH(_flyWalkSpd);
				}
				
				if (containsMask(UserInfoType.MULTIPLIER))
				{
					writeH(18);
					writeF(_moveMultiplier);
					writeF(_activeChar.getAttackSpeedMultiplier());
				}
				
				if (containsMask(UserInfoType.COL_RADIUS_HEIGHT))
				{
					writeH(18);
					writeF(_activeChar.getCollisionRadius());
					writeF(_activeChar.getCollisionHeight());
				}
				
				if (containsMask(UserInfoType.ATK_ELEMENTAL))
				{
					writeH(5);
					
					byte attackAttribute = _activeChar.getAttackElement();
					writeC(attackAttribute);
					writeH(_activeChar.getAttackElementValue(attackAttribute));
				}
				
				if (containsMask(UserInfoType.CLAN))
				{
					writeH(UserInfoType.CLAN.getBlockLength() + (_title.length() * 2));
					writeString(_title);
					
					writeH(_activeChar.getPledgeType());
					writeD(_activeChar.getClanId());
					writeD(_activeChar.getClanCrestLargeId());
					writeD(_activeChar.getClanCrestId());
					writeD(_activeChar.getClanPrivileges().getBitmask());
					writeC(_activeChar.isClanLeader() ? 0x01 : 0x00);
					writeD(_activeChar.getAllyId());
					writeD(_activeChar.getAllyCrestId());
					writeC(_activeChar.isInPartyMatchRoom() ? 1 : 0);
				}
				
				if (containsMask(UserInfoType.SOCIAL))
				{
					writeH(22);
					writeC(_activeChar.getPvpFlag());
					writeD(_activeChar.getKarma()); // Reputation
					writeC(_activeChar.isNoble() ? 1 : 0);
					writeC(_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA) ? 1 : 0);
					writeC(_activeChar.getPledgeClass());
					writeD(_activeChar.getPkKills());
					writeD(_activeChar.getPvpKills());
					writeH(_activeChar.getRecomLeft());
					writeH(_activeChar.getRecomHave());
				}
				
				if (containsMask(UserInfoType.VITA_FAME))
				{
					writeH(15);
					writeD(_activeChar.getVitalityPoints());
					writeC(0x00); // Vita Bonus
					writeD(_activeChar.getFame());
					// writeD(_activeChar.getRaidbossPoints());
					writeD(0x00);
				}
				
				if (containsMask(UserInfoType.SLOTS))
				{
					switch (ServerTypeConfigs.SERVER_TYPE)
					{
						case GC:
							writeH(9);
							break;
						case SL:
							writeH(11);
							break;
					}
					
					writeC(_activeChar.getInventory().getTalismanSlots()); // Confirmed
					// writeC(_activeChar.getInventory().getBroochJewelSlots()); // Confirmed
					writeC(0x00); // Confirmed
					writeC(_activeChar.getTeam().getId()); // Confirmed
					writeC(0x00); // (1 = Red, 2 = White, 3 = White Pink) dotted ring on the floor
					writeC(0x00);
					writeC(0x00);
					writeC(0x00);
					
					switch (ServerTypeConfigs.SERVER_TYPE)
					{
						case SL:
							// if (_activeChar.getInventory().getAgathionSlots() > 0)
							// {
							// writeC(0x01);
							// writeC(_activeChar.getInventory().getAgathionSlots() - 1);
							// }
							// else
							// {
							writeC(0x00);
							writeC(0x00);
							// }
							break;
					}
				}
				
				if (containsMask(UserInfoType.MOVEMENTS))
				{
					writeH(4);
					writeC(_activeChar.isInsideZone(ZoneIdType.WATER) ? 1 : _activeChar.isFlyingMounted() ? 2 : 0);
					writeC(_activeChar.isRunning() ? 0x01 : 0x00);
				}
				
				if (containsMask(UserInfoType.COLOR))
				{
					writeH(10);
					writeD(_activeChar.getAppearance().getNameColor());
					writeD(_activeChar.getAppearance().getTitleColor());
				}
				
				if (containsMask(UserInfoType.INVENTORY_LIMIT))
				{
					writeH(9);
					writeH(0x00);
					writeH(0x00);
					writeH(_activeChar.getInventoryLimit());
					writeC(_activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()) : 0);
				}
				
				if (containsMask(UserInfoType.TRUE_HERO))
				{
					writeH(9);
					writeD(0x00);
					writeH(0x00);
					writeC(_activeChar.isHero() ? 100 : 0x00);
				}
				return;
		}
		
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_activeChar.getVehicle() != null ? _activeChar.getVehicle().getObjectId() : 0);
		
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getAppearance().getVisibleName());
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
		
		writeD(_activeChar.getBaseClass());
		
		writeD(_activeChar.getLevel());
		writeQ(_activeChar.getExp());
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case H5:
				writeF((float) (_activeChar.getExp() - ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel())) / (ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(_activeChar.getLevel()))); // High Five exp %
				break;
		}
		
		writeD(_activeChar.getSTR());
		writeD(_activeChar.getDEX());
		writeD(_activeChar.getCON());
		writeD(_activeChar.getINT());
		writeD(_activeChar.getWIT());
		writeD(_activeChar.getMEN());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getCurrentMp());
		writeD(_activeChar.getSp());
		writeD(_activeChar.getCurrentLoad());
		writeD(_activeChar.getMaxLoad());
		
		writeD(_activeChar.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equipped
		
		for (int slot : getPaperdollOrder())
		{
			writeD(_activeChar.getInventory().getPaperdollObjectId(slot));
		}
		
		for (int slot : getPaperdollOrder())
		{
			writeD(_activeChar.getInventory().getPaperdollItemVisualDisplayId(slot));
		}
		
		for (int slot : getPaperdollOrder())
		{
			writeD(_activeChar.getInventory().getPaperdollAugmentationId(slot));
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_activeChar.getInventory().getTalismanSlots());
				writeD(_activeChar.getInventory().canEquipCloak() ? 1 : 0);
				break;
		}
		
		writeD((int) _activeChar.getPAtk(null));
		writeD((int) _activeChar.getPAtkSpd());
		writeD((int) _activeChar.getPDef(null));
		writeD(_activeChar.getEvasionRate(null));
		writeD(_activeChar.getAccuracy());
		writeD(_activeChar.getCriticalHit(null, null));
		writeD((int) _activeChar.getMAtk(null, null));
		
		writeD(_activeChar.getMAtkSpd());
		writeD((int) _activeChar.getPAtkSpd());
		
		writeD((int) _activeChar.getMDef(null, null));
		
		writeD(_activeChar.getPvpFlag()); // 0-non-pvp 1-pvp = violet name
		writeD(_activeChar.getKarma());
		
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(_moveMultiplier);
		writeF(_activeChar.getAttackSpeedMultiplier());
		
		writeF(_activeChar.getCollisionRadius());
		writeF(_activeChar.getCollisionHeight());
		
		writeD(_activeChar.getAppearance().getHairStyle());
		writeD(_activeChar.getAppearance().getHairColor());
		writeD(_activeChar.getAppearance().getFace());
		writeD(_activeChar.isGM() ? 1 : 0); // builder level
		
		if (_activeChar.getPoly().isMorphed())
		{
			L2NpcTemplate polyObj = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());
			if (polyObj != null)
			{
				_title += " - " + polyObj.getName();
			}
		}
		writeS(_title);
		
		writeD(_activeChar.getClanId());
		writeD(_activeChar.getClanCrestId());
		writeD(_activeChar.getAllyId());
		writeD(_activeChar.getAllyCrestId()); // ally crest id
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(_relation);
		writeC(_activeChar.getMountType().ordinal()); // mount type
		writeC(_activeChar.getPrivateStoreType().getId());
		writeC(_activeChar.hasCrystallization() ? 1 : 0);
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getPvpKills());
		
		writeH(_activeChar.getCubics().size());
		for (int id : _activeChar.getCubics().keySet())
		{
			writeH(id);
		}
		
		writeC(_activeChar.isInPartyMatchRoom() ? 1 : 0);
		
		writeD(_activeChar.isInvisible() ? _activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _activeChar.getAbnormalEffect());
		writeC(_activeChar.isInsideZone(ZoneIdType.WATER) ? 1 : _activeChar.isFlyingMounted() ? 2 : 0);
		
		writeD(_activeChar.getClanPrivileges().getBitmask());
		
		writeH(_activeChar.getRecomLeft()); // c2 recommendations remaining
		writeH(_activeChar.getRecomHave()); // c2 recommendations received
		writeD(_activeChar.getMountNpcId() > 0 ? _activeChar.getMountNpcId() + 1000000 : 0);
		writeH(_activeChar.getInventoryLimit());
		
		writeD(_activeChar.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(_activeChar.getMaxCp());
		writeD((int) _activeChar.getCurrentCp());
		writeC(_activeChar.isMounted() || (_airShipHelm != 0) ? 0 : _activeChar.getEnchantEffect());
		
		writeC(_activeChar.getTeam().getId());
		
		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0); // 0x01: symbol on char menu ctrl+I
		writeC(_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA) ? 1 : 0); // 0x01: Hero Aura
		
		writeC(_activeChar.getFishingEx().isFishing() ? 1 : 0); // Fishing Mode
		writeD(_activeChar.getFishingEx().getFishx()); // fishing x
		writeD(_activeChar.getFishingEx().getFishy()); // fishing y
		writeD(_activeChar.getFishingEx().getFishz()); // fishing z
		writeD(_activeChar.getAppearance().getNameColor());
		
		// new c5
		writeC(_activeChar.isRunning() ? 0x01 : 0x00); // changes the Speed display on Status Window
		
		writeD(_activeChar.getPledgeClass()); // changes the text above CP on Status Window
		writeD(_activeChar.getPledgeType());
		
		writeD(_activeChar.getAppearance().getTitleColor());
		
		writeD(_activeChar.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()) : 0);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				// T1 Starts
				writeD(_activeChar.getTransformationDisplayId());
				
				byte attackAttribute = _activeChar.getAttackElement();
				writeH(attackAttribute);
				writeH(_activeChar.getAttackElementValue(attackAttribute));
				writeH(_activeChar.getDefenseElementValue(Elementals.FIRE));
				writeH(_activeChar.getDefenseElementValue(Elementals.WATER));
				writeH(_activeChar.getDefenseElementValue(Elementals.WIND));
				writeH(_activeChar.getDefenseElementValue(Elementals.EARTH));
				writeH(_activeChar.getDefenseElementValue(Elementals.HOLY));
				writeH(_activeChar.getDefenseElementValue(Elementals.DARK));
				
				writeD(_activeChar.getAgathionId());
				
				// T2 Starts
				writeD(_activeChar.getFame()); // Fame
				writeD(_activeChar.isMinimapAllowed() ? 1 : 0); // Minimap on Hellbound
				writeD(_activeChar.getVitalityPoints()); // Vitality Points
				writeD(_activeChar.getSpecialEffect());
				// writeD(_territoryId); // CT2.3
				// writeD((_isDisguised ? 0x01: 0x00)); // CT2.3
				// writeD(_territoryId); // CT2.3
				break;
		}
	}
}
