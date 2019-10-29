package l2r.gameserver.network.serverpackets;

import custom.npctopc.NpcToPcManager;
import gr.sr.datatables.FakePcsTable;
import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.gameserver.data.sql.ClanTable;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.data.xml.impl.PlayerTemplateData;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.Team;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.TownManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.actor.*;
import l2r.gameserver.model.actor.instance.L2GuardInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2PcTemplate;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.model.skills.VisualEffect;
import l2r.gameserver.model.zone.type.L2TownZone;
import l2r.gameserver.network.handlers.types.NpcInfoType;

import java.text.DecimalFormat;
import java.util.Set;

public abstract class AbstractNpcInfo extends AbstractMaskPacket<NpcInfoType>
{
	protected int _x, _y, _z, _heading;
	protected int _idTemplate;
	protected boolean _isAttackable, _isSummoned;
	protected int _mAtkSpd, _pAtkSpd;
	protected int _runSpd;
	protected int _walkSpd;
	protected final int _swimRunSpd, _swimWalkSpd;
	protected final int _flyRunSpd, _flyWalkSpd;
	protected double _moveMultiplier;
	
	protected int _rhand, _lhand, _chest, _enchantEffect;
	protected double _collisionHeight, _collisionRadius;
	protected String _name = "";
	protected String _title = "";
	
	// Flags
	private static final int IS_IN_COMBAT = 1 << 0;
	private static final int IS_ALIKE_DEAD = 1 << 1;
	private static final int IS_TARGETABLE = 1 << 2;
	private static final int IS_SHOW_NAME = 1 << 3;
	
	public AbstractNpcInfo(L2Character cha)
	{
		_isSummoned = cha.isShowSummonAnimation();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = (int) cha.getPAtkSpd();
		_moveMultiplier = cha.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(cha.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(cha.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(cha.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(cha.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = cha.isFlying() ? _runSpd : 0;
		_flyWalkSpd = cha.isFlying() ? _walkSpd : 0;
	}
	
	/**
	 * Packet for Npcs
	 */
	public static class NpcInfo extends AbstractNpcInfo
	{
		private final byte[] _masks = new byte[]
		{
			(byte) 0x00,
			(byte) 0x0C,
			(byte) 0x0C,
			(byte) 0x00,
			(byte) 0x00
		};
		
		private int _initSize = 0;
		private int _blockSize = 0;
		
		@Override
		protected byte[] getMasks()
		{
			return _masks;
		}
		
		@Override
		protected void onNewMaskAdded(NpcInfoType component)
		{
			calcBlockSize(_npc, component);
		}
		
		private void calcBlockSize(L2Npc npc, NpcInfoType type)
		{
			switch (type)
			{
				case ATTACKABLE:
				case UNKNOWN1:
				{
					_initSize += type.getBlockLength();
					break;
				}
				case TITLE:
				{
					_initSize += type.getBlockLength() + (_title.length() * 2);
					break;
				}
				case NAME:
				{
					_blockSize += type.getBlockLength() + (_name.length() * 2);
					break;
				}
				default:
				{
					_blockSize += type.getBlockLength();
					break;
				}
			}
		}
		
		private final L2Npc _npc;
		private int _clanCrest = 0;
		private int _clanLargeCrest = 0;
		private int _allyCrest = 0;
		private int _allyId = 0;
		private int _clanId = 0;
		private int _displayEffect = 0;
		
		private int _statusMask = 0;
		
		public NpcInfo(L2Npc cha, L2Character attacker)
		{
			super(cha);
			_npc = cha;
			
			_idTemplate = cha.getTemplate().getDisplayId(); // On every subclass
			_rhand = cha.getRightHandItem(); // On every subclass
			_lhand = cha.getLeftHandItem(); // On every subclass
			_enchantEffect = cha.getEnchantEffect();
			_collisionHeight = cha.getCollisionHeight();// On every subclass
			_collisionRadius = cha.getCollisionRadius();// On every subclass
			_isAttackable = cha.isAutoAttackable(attacker);
			if (cha.getTemplate().isUsingServerSideName())
			{
				_name = cha.getName();// On every subclass
			}
			
			if (_npc.isInvisible())
			{
				_title = "Invisible";
			}
			else if (Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
			{
				_title = (Config.L2JMOD_CHAMP_TITLE); // On every subclass
			}
			else if (cha.getTemplate().isUsingServerSideTitle())
			{
				_title = cha.getTemplate().getTitle(); // On every subclass
			}
			else
			{
				_title = cha.getTitle(); // On every subclass
			}

			if (Config.SHOW_NPC_LVL && (_npc instanceof L2MonsterInstance) && ((L2Attackable) _npc).canShowLevelInTitle())
			{
				String t = "Lv " + cha.getLevel() + (cha.isAggressive() ? "*" : "");
				if ((_title != null) && !_title.isEmpty())
				{
					t += " " + _title;
				}

				_title = t;
			}
			
			addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);
			
			if (_npc.getHeading() > 0)
			{
				addComponentType(NpcInfoType.HEADING);
			}
			
			if ((_npc.getStat().getPAtkSpd() > 0) || (_npc.getStat().getMAtkSpd() > 0))
			{
				addComponentType(NpcInfoType.ATK_CAST_SPEED);
			}
			
			if (_npc.getRunSpeed() > 0)
			{
				addComponentType(NpcInfoType.SPEED_MULTIPLIER);
			}
			
			if ((_npc.getLeftHandItem() > 0) || (_npc.getRightHandItem() > 0))
			{
				addComponentType(NpcInfoType.EQUIPPED);
			}
			
			if (_npc.getTeam() != Team.NONE)
			{
				addComponentType(NpcInfoType.TEAM);
			}
			
			if (_npc.getDisplayEffect() > 0)
			{
				addComponentType(NpcInfoType.DISPLAY_EFFECT);
			}
			
			if (_npc.isInsideZone(ZoneIdType.WATER) || _npc.isFlying())
			{
				addComponentType(NpcInfoType.SWIM_OR_FLY);
			}
			
			if (_npc.isFlying())
			{
				addComponentType(NpcInfoType.FLYING);
			}
			
			// if (_npc.getCloneObjId() > 0)
			// {
			// addComponentType(NpcInfoType.CLONE);
			// }
			
			if (_npc.getMaxHp() > 0)
			{
				addComponentType(NpcInfoType.MAX_HP);
			}
			
			if (_npc.getMaxMp() > 0)
			{
				addComponentType(NpcInfoType.MAX_MP);
			}
			
			if (_npc.getCurrentHp() <= _npc.getMaxHp())
			{
				addComponentType(NpcInfoType.CURRENT_HP);
			}
			
			if (_npc.getCurrentMp() <= _npc.getMaxMp())
			{
				addComponentType(NpcInfoType.CURRENT_MP);
			}
			
			if (!_name.isEmpty())
			{
				addComponentType(NpcInfoType.NAME);
			}
			
			if (!_title.isEmpty() || _npc.isTrap())
			{
				addComponentType(NpcInfoType.TITLE);
			}
			
			// if (_npc.getNameString() != null)
			// {
			// addComponentType(NpcInfoType.NAME_NPCSTRINGID);
			// }
			
			// if (_npc.getTitleString() != null)
			// {
			// addComponentType(NpcInfoType.TITLE_NPCSTRINGID);
			// }
			
			if (_npc.getKarma() != 0)
			{
				addComponentType(NpcInfoType.REPUTATION);
			}
			
			// if (!_abnormalVisualEffects.isEmpty() || npc.isInvisible())
			// {
			addComponentType(NpcInfoType.ABNORMALS);
			// }
			
			if (_npc.getEnchantEffect() > 0)
			{
				addComponentType(NpcInfoType.ENCHANT);
			}
			
			// if (_npc.getTransformationDisplayId() > 0)
			// {
			// addComponentType(NpcInfoType.TRANSFORMATION);
			// }
			
			if (_npc.isShowSummonAnimation())
			{
				addComponentType(NpcInfoType.SUMMONED);
			}
			
			addComponentType(NpcInfoType.COLOR_EFFECT);
			
			if (_npc.getPvpFlag() > 0)
			{
				addComponentType(NpcInfoType.PVP_FLAG);
			}
			
			// npc crest of owning clan/ally of castle
			if (cha.isNpc() && !cha.isAttackable() && cha.isInsideZone(ZoneIdType.TOWN) && (Config.SHOW_CREST_WITHOUT_QUEST || cha.getCastle().getShowNpcCrest()) && (cha.getCastle().getOwnerId() != 0))
			{
				// vGodFather
				L2TownZone town = TownManager.getTown(_x, _y, _z);
				int townId = town != null ? town.getTownId() : 33;
				if ((townId != 33) && (townId != 22) && (townId != 19))
				{
					L2Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
					if (clan != null)
					{
						_clanCrest = clan.getCrestId();
						_clanLargeCrest = clan.getCrestLargeId();
						_clanId = clan.getId();
						_allyCrest = clan.getAllyCrestId();
						_allyId = clan.getAllyId();
						
						addComponentType(NpcInfoType.CLAN);
					}
				}
			}
			
			_displayEffect = cha.getDisplayEffect();
			
			if (cha.isInCombat())
			{
				_statusMask |= IS_IN_COMBAT;
			}
			if (cha.isDead())
			{
				_statusMask |= IS_ALIKE_DEAD;
			}
			if (cha.isTargetable())
			{
				_statusMask |= IS_TARGETABLE;
			}
			if (cha.isShowName())
			{
				_statusMask |= IS_SHOW_NAME;
			}
			
			if (_statusMask != 0)
			{
				addComponentType(NpcInfoType.VISUAL_STATE);
			}
		}
		
		@Override
		protected void writeImpl()
		{
			NpcToPcManager manager = NpcToPcManager.getInstance();
			if ((_npc instanceof L2NpcInstance) && manager.npcExist(((L2NpcInstance) _npc).getId()))
			{
				int npcId = ((L2NpcInstance) _npc).getId();
				int _clanCrest = 0, _clanId = 0, _allyCrest = 0, _allyId = 0;
				if (manager.getInt(npcId, "ClanId") != 0)
				{
					L2Clan clan = ClanTable.getInstance().getClan(manager.getInt(npcId, "ClanId"));
					if (clan != null)
					{
						_clanCrest = clan.getCrestId();
						_clanId = clan.getId();
						_allyCrest = clan.getAllyCrestId();
						_allyId = clan.getAllyId();
					}
				}
				L2PcTemplate charInfo = PlayerTemplateData.getInstance().getTemplate(manager.getInt(npcId, "classid"));

				writeC(0x31);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(0x00);
				writeD(_npc.getObjectId());
				writeS(manager.getString(npcId, "Name"));
				writeD(manager.getInt(npcId, "Race"));
				writeD(manager.getInt(npcId, "Gender"));

				writeD(manager.getInt(npcId, "ClassId"));

				writeD(0x00);
				writeD(manager.getInt(npcId, "Hair2"));
				writeD(manager.getInt(npcId, "RHand"));
				writeD(manager.getInt(npcId, "LHand"));
				writeD(manager.getInt(npcId, "Gloves"));
				writeD(manager.getInt(npcId, "Chest"));
				writeD(manager.getInt(npcId, "Legs"));
				writeD(manager.getInt(npcId, "Feet"));
				writeD(manager.getInt(npcId, "Cloak"));
				writeD(manager.getInt(npcId, "RHand"));
				writeD(manager.getInt(npcId, "Hair1"));
				writeD(manager.getInt(npcId, "Hair2"));
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);

				// c6 new h's
				writeD(0x00);
				writeD(0x00);
				writeD(manager.getInt(npcId, "UseAugmentation"));
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(manager.getInt(npcId, "UseAugmentation"));
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x01);

				writeD(manager.getInt(npcId, "PvPFlag"));
				writeD(manager.getInt(npcId, "Karma"));

				writeD(_mAtkSpd);
				writeD(_pAtkSpd);

				writeD(0x00);

				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(_runSpd); // swim run speed
				writeD(_walkSpd); // swim walk speed
				writeD(_runSpd); // fl run speed
				writeD(_walkSpd); // fl walk speed
				writeD(_runSpd); // fly run speed
				writeD(_walkSpd); // fly walk speed
				writeF(_npc.getMovementSpeedMultiplier());
				writeF(_npc.getAttackSpeedMultiplier());

				if (manager.getInt(npcId, "MountType") > 0)
				{
					writeF(NpcTable.getInstance().getTemplate(manager.getInt(npcId, "MountNpcId")).getCollisionRadius());
					writeF(NpcTable.getInstance().getTemplate(manager.getInt(npcId, "MountNpcId")).getCollisionHeight());
				}
				else
				{
					writeF(manager.getInt(npcId, "Gender") == 0 ? charInfo.getfCollisionRadius() : charInfo._fCollisionRadiusFemale);
					writeF(manager.getInt(npcId, "Gender") == 0 ? charInfo.getfCollisionHeight() : charInfo._fCollisionHeightFemale);
				}
				writeD(manager.getInt(npcId, "HairStyle"));
				writeD(manager.getInt(npcId, "HairColor"));
				writeD(manager.getInt(npcId, "Face"));

				writeS(manager.getString(npcId, "Title"));

				writeD(_clanId); // clan id
				writeD(_clanCrest); // crest id
				writeD(_allyId); // ally id
				writeD(_allyCrest); // all crest

				writeC(manager.getBoolean(npcId, "IsSitting") ? 0 : 1); // standing = 1 sitting = 0
				writeC(manager.getBoolean(npcId, "IsRunning") ? 1 : 0); // running = 1 walking = 0
				writeC(manager.getBoolean(npcId, "IsInCombat") ? 1 : 0);
				writeC(manager.getBoolean(npcId, "IsAlikeDead") ? 1 : 0);

				writeC(0); // invisible = 1 visible =0

				writeC(manager.getInt(npcId, "MountType")); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount

				writeC(_npc instanceof L2Attackable ? 0x00 : 0x01); // 1 - sellshop

				writeH(manager.getIntegerArray(npcId, "Cubics").size());
				for (int id : manager.getIntegerArray(npcId, "Cubics"))
				{
					writeH(id);
				}

				writeC(manager.getBoolean(npcId, "IsInPartyChatRoom") ? 1 : 0);

				int abnormalEffects = 0x00;
				for (int effect : manager.getIntegerArray(npcId, "AbnormalEffects"))
				{
					abnormalEffects |= effect;
				}
				writeD(abnormalEffects);

				writeC(0x00);
				writeH(manager.getInt(npcId, "Recommendations")); // Blue value for name (0 = white, 255 = pure blue)
				writeD(1000000);
				writeD(manager.getInt(npcId, "ClassId"));

				writeD(0);

				writeC(manager.getInt(npcId, "Enchant"));

				writeC(manager.getInt(npcId, "Circle")); // team circle around feet 1= Blue, 2 = red

				writeD(0x00);
				writeC(manager.getBoolean(npcId, "IsNoble") ? 1 : 0); // Symbol on char menu ctrl+I
				writeC(manager.getBoolean(npcId, "isHero") ? 1 : 0); // Hero Aura

				writeC(manager.getBoolean(npcId, "IsFishing") ? 1 : 0); // 0x01: Fishing Mode (Cant be undone by setting back to 0)
				writeD(manager.getInt(npcId, "FishingX"));
				writeD(manager.getInt(npcId, "FishingY"));
				writeD(manager.getInt(npcId, "FishingZ"));

				writeD(Integer.decode("0x" + manager.getString(npcId, "NameColor")));

				writeD(_heading); // isRunning() as in UserInfo?

				writeD(manager.getInt(npcId, "PledgeClass"));
				writeD(manager.getInt(npcId, "PledgeType"));

				writeD(Integer.decode("0x" + manager.getString(npcId, "TitleColor")));

				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);

				int specialEffects = 0x00;
				for (int effect : manager.getIntegerArray(npcId, "SpecialEffects"))
				{
					specialEffects |= effect;
				}
				writeD(specialEffects);
				return;
			}
			FakePc fpc = FakePcsTable.getInstance().getFakePc(_npc.getId());
			if (fpc != null)
			{
				writeC(0x31);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(0x00); // vehicle id
				writeD(_npc.getObjectId());
				writeS(fpc.name); // visible name
				writeD(fpc.race);
				writeD(fpc.sex);
				writeD(fpc.clazz);
				
				writeD(fpc.pdUnder);
				writeD(fpc.pdHead);
				writeD(fpc.pdRHand);
				writeD(fpc.pdLHand);
				writeD(fpc.pdGloves);
				writeD(fpc.pdChest);
				writeD(fpc.pdLegs);
				writeD(fpc.pdFeet);
				writeD(fpc.pdBack);
				writeD(fpc.pdLRHand);
				writeD(fpc.pdHair);
				writeD(fpc.pdHair2);
				writeD(fpc.pdRBracelet);
				writeD(fpc.pdLBracelet);
				writeD(fpc.pdDeco1);
				writeD(fpc.pdDeco2);
				writeD(fpc.pdDeco3);
				writeD(fpc.pdDeco4);
				writeD(fpc.pdDeco5);
				writeD(fpc.pdDeco6);
				writeD(0x00); // belt
				
				writeD(fpc.pdUnderAug);
				writeD(fpc.pdHeadAug);
				writeD(fpc.pdRHandAug);
				writeD(fpc.pdLHandAug);
				writeD(fpc.pdGlovesAug);
				writeD(fpc.pdChestAug);
				writeD(fpc.pdLegsAug);
				writeD(fpc.pdFeetAug);
				writeD(fpc.pdBackAug);
				writeD(fpc.pdLRHandAug);
				writeD(fpc.pdHairAug);
				writeD(fpc.pdHair2Aug);
				writeD(fpc.pdRBraceletAug);
				writeD(fpc.pdLBraceletAug);
				writeD(fpc.pdDeco1Aug);
				writeD(fpc.pdDeco2Aug);
				writeD(fpc.pdDeco3Aug);
				writeD(fpc.pdDeco4Aug);
				writeD(fpc.pdDeco5Aug);
				writeD(fpc.pdDeco6Aug);
				writeD(0x00); // belt aug
				writeD(0x00);
				writeD(0x01);
				
				writeD(fpc.pvpFlag);
				writeD(fpc.karma);
				
				writeD(_mAtkSpd);
				writeD(_pAtkSpd);
				
				writeD(0x00);
				
				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(_runSpd); // swim run speed
				writeD(_walkSpd); // swim walk speed
				writeD(_runSpd); // fly run speed
				writeD(_walkSpd); // fly walk speed
				writeD(_runSpd);
				writeD(_walkSpd);
				writeF(_npc.getMovementSpeedMultiplier()); // _activeChar.getProperMultiplier()
				writeF(_npc.getAttackSpeedMultiplier()); // _activeChar.getAttackSpeedMultiplier()
				
				// TODO: add handling of mount collision
				L2PcTemplate pctmpl = PlayerTemplateData.getInstance().getTemplate(fpc.clazz);
				writeF(fpc.sex == 0 ? pctmpl.getfCollisionRadius() : pctmpl._fCollisionRadiusFemale);
				writeF(fpc.sex == 0 ? pctmpl.getfCollisionHeight() : pctmpl._fCollisionHeightFemale);
				
				writeD(fpc.hairStyle);
				writeD(fpc.hairColor);
				writeD(fpc.face);
				
				if ((_npc instanceof L2MonsterInstance) || (_npc instanceof L2GuardInstance))
				{
					writeS(fpc.title + (fpc.title.isEmpty() ? "" : " - ") + "HP " + new DecimalFormat("#").format((100.0 * _npc.getCurrentHp()) / _npc.getMaxHp()) + "%");
				}
				else
				{
					writeS(fpc.title);
				}
				
				writeD(0x00); // clan id
				writeD(0x00); // clan crest id
				writeD(0x00); // ally id
				writeD(0x00); // ally crest id
				
				writeC(0x01); // standing = 1 sitting = 0
				writeC(_npc.isRunning() ? 1 : 0); // running = 1 walking = 0
				writeC(_npc.isInCombat() ? 1 : 0);
				writeC(_npc.isAlikeDead() ? 1 : 0);
				
				writeC(fpc.invisible); // invisible = 1 visible =0
				
				writeC(fpc.mount); // 1 on strider 2 on wyvern 3 on Great Wolf 0 no mount
				writeC(0x00); // 1 - sellshop
				writeH(0x00); // cubic count
				// for (int id : allCubics)
				// writeH(id);
				writeC(0x00); // find party members
				writeD(0x00); // abnormal effect
				writeC(0x00); // isFlying() ? 2 : 0
				writeH(0x00); // getRecomHave(): Blue value for name (0 = white, 255 = pure blue)
				writeD(1000000); // getMountNpcId() + 1000000
				writeD(fpc.clazz);
				writeD(0x00); // ?
				writeC(fpc.enchantEffect);
				writeC(fpc.team); // team circle around feet 1= Blue, 2 = red
				writeD(0x00); // getClanCrestLargeId()
				writeC(0x00); // isNoble(): Symbol on char menu ctrl+I
				writeC(fpc.hero); // Hero Aura
				writeC(fpc.fishing); // 0x01: Fishing Mode (Cant be undone by setting back to 0)
				writeD(fpc.fishingX);
				writeD(fpc.fishingY);
				writeD(fpc.fishingZ);
				
				writeD(fpc.nameColor);
				writeD(_heading);
				writeD(0x00); // pledge class
				writeD(0x00); // pledge type
				
				if (_npc instanceof L2MonsterInstance)
				{
					if (((100 * _npc.getCurrentHp()) / _npc.getMaxHp()) >= 75)
					{
						writeD(Integer.decode("0x" + "00ff00"));
					}
					if ((((100 * _npc.getCurrentHp()) / _npc.getMaxHp()) < 75) && (((100 * _npc.getCurrentHp()) / _npc.getMaxHp()) >= 40))
					{
						writeD(Integer.decode("0x" + "0080ff"));
					}
					if (((100 * _npc.getCurrentHp()) / _npc.getMaxHp()) < 40)
					{
						writeD(Integer.decode("0x" + "3737ff"));
					}
				}
				else
				{
					writeD(fpc.titleColor);
				}
				
				writeD(0x00); // cursed weapon level
				writeD(0x00); // reputation score
				writeD(0x00); // transformation id
				writeD(0x00); // agathion id
				writeD(0x01); // T2 ?
				writeD(0x00); // special effect
				// writeD(0x00); // territory Id
				// writeD(0x00); // is Disguised
				// writeD(0x00); // territory Id
			}
			else
			{
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
						writeC(0x16);
						break;
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
					case GC:
					case SL:
						writeC(0x0c);
						break;
				}
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GC:
					case SL:
						writeD(_npc.getObjectId());
						writeC(_npc.isShowSummonAnimation() ? 0x02 : 0x00); // // 0=teleported 1=default 2=summoned
						writeH(37); // mask_bits_37
						writeB(_masks);
						
						// Block 1
						writeC(_initSize);
						
						if (containsMask(NpcInfoType.ATTACKABLE))
						{
							writeC(_npc.isAttackable() && !(_npc instanceof L2GuardInstance) ? 0x01 : 0x00);
						}
						if (containsMask(NpcInfoType.UNKNOWN1))
						{
							writeD(0x00); // unknown
						}
						if (containsMask(NpcInfoType.TITLE))
						{
							writeS(_title);
						}
						
						// Block 2
						writeH(_blockSize);
						if (containsMask(NpcInfoType.ID))
						{
							writeD(_npc.getTemplate().getDisplayId() + 1000000);
						}
						if (containsMask(NpcInfoType.POSITION))
						{
							writeD(_npc.getX());
							writeD(_npc.getY());
							writeD(_npc.getZ());
						}
						if (containsMask(NpcInfoType.HEADING))
						{
							writeD(_npc.getHeading());
						}
						if (containsMask(NpcInfoType.UNKNOWN2))
						{
							writeD(0x00); // Unknown
						}
						if (containsMask(NpcInfoType.ATK_CAST_SPEED))
						{
							writeD((int) _npc.getPAtkSpd());
							writeD(_npc.getMAtkSpd());
						}
						if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
						{
							writeE((float) _npc.getStat().getMovementSpeedMultiplier());
							writeE(_npc.getStat().getAttackSpeedMultiplier());
						}
						if (containsMask(NpcInfoType.EQUIPPED))
						{
							writeD(_npc.getRightHandItem());
							writeD(0x00); // Armor id?
							writeD(_npc.getLeftHandItem());
						}
						if (containsMask(NpcInfoType.ALIVE))
						{
							writeC(_npc.isDead() ? 0x00 : 0x01);
						}
						if (containsMask(NpcInfoType.RUNNING))
						{
							writeC(_npc.isRunning() ? 0x01 : 0x00);
						}
						if (containsMask(NpcInfoType.SWIM_OR_FLY))
						{
							writeC(_npc.isInsideZone(ZoneIdType.WATER) ? 0x01 : _npc.isFlying() ? 0x02 : 0x00);
						}
						if (containsMask(NpcInfoType.TEAM))
						{
							writeC(_npc.getTeam().getId());
						}
						if (containsMask(NpcInfoType.ENCHANT))
						{
							writeD(_npc.getEnchantEffect());
						}
						if (containsMask(NpcInfoType.FLYING))
						{
							writeD(_npc.isFlying() ? 0x01 : 00);
						}
						if (containsMask(NpcInfoType.CLONE))
						{
							writeD(0x00); // _npc.getCloneObjId() Player ObjectId with Decoy
						}
						if (containsMask(NpcInfoType.COLOR_EFFECT))
						{
							writeD(_npc.getColorEffect()); // Color effect
						}
						if (containsMask(NpcInfoType.DISPLAY_EFFECT))
						{
							writeD(_npc.getDisplayEffect());
						}
						if (containsMask(NpcInfoType.TRANSFORMATION))
						{
							writeD(0x00); // _npc.getTransformationDisplayId() Transformation ID
						}
						if (containsMask(NpcInfoType.CURRENT_HP))
						{
							writeD((int) _npc.getCurrentHp());
						}
						if (containsMask(NpcInfoType.CURRENT_MP))
						{
							writeD((int) _npc.getCurrentMp());
						}
						if (containsMask(NpcInfoType.MAX_HP))
						{
							writeD(_npc.getMaxHp());
						}
						if (containsMask(NpcInfoType.MAX_MP))
						{
							writeD(_npc.getMaxMp());
						}
						if (containsMask(NpcInfoType.SUMMONED))
						{
							writeC(0x00); // 2 - do some animation on spawn
						}
						if (containsMask(NpcInfoType.UNKNOWN12))
						{
							writeD(0x00);
							writeD(0x00);
						}
						if (containsMask(NpcInfoType.NAME))
						{
							writeS(_name);
						}
						if (containsMask(NpcInfoType.NAME_NPCSTRINGID))
						{
							// final NpcStringId nameString = _npc.getNameString();
							// writeD(nameString != null ? nameString.getId() : -1); // NPCStringId for name
							writeD(-1);
						}
						if (containsMask(NpcInfoType.TITLE_NPCSTRINGID))
						{
							// final NpcStringId titleString = _npc.getTitleString();
							// writeD(titleString != null ? titleString.getId() : -1); // NPCStringId for title
							writeD(-1);
						}
						if (containsMask(NpcInfoType.PVP_FLAG))
						{
							writeC(_npc.getPvpFlag()); // PVP flag
						}
						if (containsMask(NpcInfoType.REPUTATION))
						{
							writeD(_npc.getKarma()); // Reputation
						}
						if (containsMask(NpcInfoType.CLAN))
						{
							writeD(_clanId);
							writeD(_clanCrest);
							writeD(_clanLargeCrest);
							writeD(_allyId);
							writeD(_allyCrest);
						}
						
						if (containsMask(NpcInfoType.VISUAL_STATE))
						{
							writeC(_statusMask);
						}
						
						if (containsMask(NpcInfoType.ABNORMALS))
						{
							Set<Integer> _abnormalVisualEffects = _npc._abnormalEffects;
							writeH(_abnormalVisualEffects.size() + (_npc.isInvisible() ? 1 : 0));
							for (Integer abnormalVisualEffect : _abnormalVisualEffects)
							{
								writeH(abnormalVisualEffect);
							}
							if (_npc.isInvisible())
							{
								writeH(VisualEffect.STEALTH.getId());
							}
						}
						return;
				}
				
				writeD(_npc.getObjectId());
				writeD(_idTemplate + 1000000); // npctype id
				writeD(_isAttackable ? 1 : 0);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(_heading);
				writeD(0x00);
				writeD(_mAtkSpd);
				writeD(_pAtkSpd);
				writeD(_runSpd);
				writeD(_walkSpd);
				writeD(_swimRunSpd);
				writeD(_swimWalkSpd);
				writeD(_flyRunSpd);
				writeD(_flyWalkSpd);
				writeD(_flyRunSpd);
				writeD(_flyWalkSpd);
				writeF(_moveMultiplier);
				writeF(_npc.getAttackSpeedMultiplier());
				writeF(_collisionRadius);
				writeF(_collisionHeight);
				writeD(_rhand); // right hand weapon
				writeD(_chest);
				writeD(_lhand); // left hand weapon
				writeC(1); // name above char 1=true ... ??
				writeC(_npc.isRunning() ? 1 : 0);
				writeC(_npc.isInCombat() ? 1 : 0);
				writeC(_npc.isAlikeDead() ? 1 : 0);
				writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case H5:
						writeD(-1); // High Five NPCString ID
						break;
				}
				
				writeS(_name);
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case H5:
						writeD(-1); // High Five NPCString ID
						break;
				}
				
				writeS(_title);
				writeD(0x00); // Title color 0=client default
				writeD(0x00); // pvp flag
				writeD(0x00); // karma
				
				writeD(_npc.isInvisible() ? _npc.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _npc.getAbnormalEffect());
				writeD(_clanId); // clan id
				writeD(_clanCrest); // crest id
				writeD(_allyId); // ally id
				writeD(_allyCrest); // all crest
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
						writeC(_npc.isInsideZone(ZoneIdType.WATER) ? 1 : _npc.isFlying() ? 2 : 0); // C2
						break;
				}
				
				writeC(Config.L2JMOD_CHAMPION_ENABLE_AURA ? _npc.isChampion() && _npc.isAggressive() ? 2 : _npc.isChampion() && !_npc.isAggressive() ? 1 : _npc.getTeam().getId() : _npc.getTeam().getId());
				
				writeF(_collisionRadius);
				writeF(_collisionHeight);
				writeD(_enchantEffect); // C4
				writeD(_npc.isFlying() ? 1 : 0); // C6
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case IL:
						return;
				}
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GF:
					case EPILOGUE:
					case FREYA:
					case H5:
						writeD(0x00);
						writeD(_npc.getColorEffect()); // CT1.5 Pet form and skills, Color effect
						writeC(_npc.isTargetable() ? 0x01 : 0x00);
						writeC(_npc.isShowName() ? 0x01 : 0x00);
						writeD(_npc.getSpecialEffect());
						writeD(_displayEffect);
						break;
				}
			}
		}
	}
	
	/**
	 * Packet for summons
	 */
	public static class SummonInfo extends AbstractNpcInfo
	{
		private final byte[] _masks = new byte[]
		{
			(byte) 0x00,
			(byte) 0x0C,
			(byte) 0x0C,
			(byte) 0x00,
			(byte) 0x00
		};
		
		private int _initSize = 0;
		private int _blockSize = 0;
		
		@Override
		protected byte[] getMasks()
		{
			return _masks;
		}
		
		@Override
		protected void onNewMaskAdded(NpcInfoType component)
		{
			calcBlockSize(_summon, component);
		}
		
		private void calcBlockSize(L2Summon summon, NpcInfoType type)
		{
			switch (type)
			{
				case ATTACKABLE:
				case UNKNOWN1:
				{
					_initSize += type.getBlockLength();
					break;
				}
				case TITLE:
				{
					_initSize += type.getBlockLength() + (_title.length() * 2);
					break;
				}
				case NAME:
				{
					_blockSize += type.getBlockLength() + (_name.length() * 2);
					break;
				}
				default:
				{
					_blockSize += type.getBlockLength();
					break;
				}
			}
		}
		
		private final L2Summon _summon;
		private final int _form;
		private final int _val;
		private final boolean _isSummoned;
		
		private int _clanCrest = 0;
		private int _clanLargeCrest = 0;
		private int _allyCrest = 0;
		private int _allyId = 0;
		private int _clanId = 0;
		private int _statusMask = 0;
		
		public SummonInfo(L2Summon cha, L2Character attacker, int val)
		{
			super(cha);
			
			_summon = cha;
			_val = val;
			_form = cha.getFormId();
			
			_isSummoned = cha.isShowSummonAnimation();
			
			_isAttackable = cha.isAutoAttackable(attacker);
			_rhand = cha.getWeapon();
			_lhand = 0;
			_chest = cha.getArmor();
			_enchantEffect = cha.getTemplate().getWeaponEnchant();
			_name = cha.getName();
			_title = (cha.getOwner() != null) && cha.getOwner().isOnline() ? cha.getOwner().getName() : "";
			_idTemplate = cha.getTemplate().getDisplayId();
			_collisionHeight = cha.getTemplate().getfCollisionHeight();
			_collisionRadius = cha.getTemplate().getfCollisionRadius();
			setInvisible(cha.isInvisible());
			
			if (cha.getOwner() != null)
			{
				L2Clan clan = cha.getOwner().getClan();
				if (clan != null)
				{
					_clanId = clan.getId();
					_clanCrest = clan.getCrestId();
					_clanLargeCrest = clan.getCrestLargeId();
					_allyId = clan.getAllyId();
					_allyCrest = clan.getAllyCrestId();
				}
			}
			
			if (_summon.isInCombat())
			{
				_statusMask |= IS_IN_COMBAT;
			}
			if (_summon.isDead())
			{
				_statusMask |= IS_ALIKE_DEAD;
			}
			if (_summon.isTargetable())
			{
				_statusMask |= IS_TARGETABLE;
			}
			
			_statusMask |= 0x08;
			
			addComponentType(NpcInfoType.values());
		}
		
		@Override
		protected void writeImpl()
		{
			boolean gmSeeInvis = false;
			if (_invisible)
			{
				final L2PcInstance activeChar = getClient().getActiveChar();
				if ((activeChar != null) && activeChar.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS))
				{
					gmSeeInvis = true;
				}
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					writeC(0x0C);
					break;
				case GC:
				case SL:
					writeC(0x8B);
					break;
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					writeD(_summon.getObjectId());
					writeC(_val); // 0=teleported 1=default 2=summoned
					writeH(37); // mask_bits_37
					writeB(_masks);
					
					// Block 1
					writeC(_initSize);
					
					if (containsMask(NpcInfoType.ATTACKABLE))
					{
						writeC(_isAttackable ? 0x01 : 0x00);
					}
					if (containsMask(NpcInfoType.UNKNOWN1))
					{
						writeD(0x00); // unknown
					}
					if (containsMask(NpcInfoType.TITLE))
					{
						writeS(_title);
					}
					
					// Block 2
					writeH(_blockSize);
					if (containsMask(NpcInfoType.ID))
					{
						writeD(_summon.getTemplate().getDisplayId() + 1000000);
					}
					if (containsMask(NpcInfoType.POSITION))
					{
						writeD(_summon.getX());
						writeD(_summon.getY());
						writeD(_summon.getZ());
					}
					if (containsMask(NpcInfoType.HEADING))
					{
						writeD(_summon.getHeading());
					}
					if (containsMask(NpcInfoType.UNKNOWN2))
					{
						writeD(0x00); // Unknown
					}
					if (containsMask(NpcInfoType.ATK_CAST_SPEED))
					{
						writeD((int) _summon.getPAtkSpd());
						writeD(_summon.getMAtkSpd());
					}
					if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
					{
						writeE((float) _summon.getStat().getMovementSpeedMultiplier());
						writeE(_summon.getStat().getAttackSpeedMultiplier());
					}
					if (containsMask(NpcInfoType.EQUIPPED))
					{
						writeD(_summon.getWeapon());
						writeD(_summon.getArmor()); // Armor id?
						writeD(0x00);
					}
					if (containsMask(NpcInfoType.ALIVE))
					{
						writeC(_summon.isDead() ? 0x00 : 0x01);
					}
					if (containsMask(NpcInfoType.RUNNING))
					{
						writeC(_summon.isRunning() ? 0x01 : 0x00);
					}
					if (containsMask(NpcInfoType.SWIM_OR_FLY))
					{
						writeC(_summon.isInsideZone(ZoneIdType.WATER) ? 0x01 : _summon.isFlying() ? 0x02 : 0x00);
					}
					if (containsMask(NpcInfoType.TEAM))
					{
						writeC(_summon.getTeam().getId());
					}
					if (containsMask(NpcInfoType.ENCHANT))
					{
						writeD(_summon.getTemplate().getWeaponEnchant());
					}
					if (containsMask(NpcInfoType.FLYING))
					{
						writeD(_summon.isFlying() ? 0x01 : 00);
					}
					if (containsMask(NpcInfoType.CLONE))
					{
						writeD(0x00); // Player ObjectId with Decoy
					}
					if (containsMask(NpcInfoType.COLOR_EFFECT))
					{
						// No visual effect
						writeD(0x00); // Unknown
					}
					if (containsMask(NpcInfoType.DISPLAY_EFFECT))
					{
						writeD(0x00);
					}
					if (containsMask(NpcInfoType.TRANSFORMATION))
					{
						writeD(0x00); // _summon.getTransformationDisplayId() Transformation ID
					}
					if (containsMask(NpcInfoType.CURRENT_HP))
					{
						writeD((int) _summon.getCurrentHp());
					}
					if (containsMask(NpcInfoType.CURRENT_MP))
					{
						writeD((int) _summon.getCurrentMp());
					}
					if (containsMask(NpcInfoType.MAX_HP))
					{
						writeD(_summon.getMaxHp());
					}
					if (containsMask(NpcInfoType.MAX_MP))
					{
						writeD(_summon.getMaxMp());
					}
					if (containsMask(NpcInfoType.SUMMONED))
					{
						writeC(_summon.isShowSummonAnimation() ? 0x02 : 00); // 2 - do some animation on spawn
					}
					if (containsMask(NpcInfoType.UNKNOWN12))
					{
						writeD(0x00);
						writeD(0x00);
					}
					if (containsMask(NpcInfoType.NAME))
					{
						writeS(_summon.getName());
					}
					if (containsMask(NpcInfoType.NAME_NPCSTRINGID))
					{
						writeD(-1); // NPCStringId for name
					}
					if (containsMask(NpcInfoType.TITLE_NPCSTRINGID))
					{
						writeD(-1); // NPCStringId for title
					}
					if (containsMask(NpcInfoType.PVP_FLAG))
					{
						writeC(_summon.getPvpFlag()); // PVP flag
					}
					if (containsMask(NpcInfoType.REPUTATION))
					{
						writeD(_summon.getKarma()); // Name color
					}
					if (containsMask(NpcInfoType.CLAN))
					{
						writeD(_clanId);
						writeD(_clanCrest);
						writeD(_clanLargeCrest);
						writeD(_allyId);
						writeD(_allyCrest);
					}
					
					if (containsMask(NpcInfoType.VISUAL_STATE))
					{
						writeC(_statusMask);
					}
					
					if (containsMask(NpcInfoType.ABNORMALS))
					{
						Set<Integer> _abnormalVisualEffects = _summon._abnormalEffects;
						writeH(_abnormalVisualEffects.size() + (_summon.isInvisible() ? 1 : 0));
						for (Integer abnormalVisualEffect : _abnormalVisualEffects)
						{
							writeH(abnormalVisualEffect);
						}
						if (_summon.isInvisible())
						{
							writeH(VisualEffect.STEALTH.getId());
						}
					}
					return;
			}
			
			writeD(_summon.getObjectId());
			writeD(_idTemplate + 1000000); // npctype id
			writeD(_isAttackable ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_swimRunSpd);
			writeD(_swimWalkSpd);
			writeD(_flyRunSpd);
			writeD(_flyWalkSpd);
			writeD(_flyRunSpd);
			writeD(_flyWalkSpd);
			writeF(_moveMultiplier);
			writeF(_summon.getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand); // right hand weapon
			writeD(_chest);
			writeD(_lhand); // left hand weapon
			writeC(0x01); // name above char 1=true ... ??
			writeC(0x01); // always running 1=running 0=walking
			writeC(_summon.isInCombat() ? 1 : 0);
			writeC(_summon.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : _val); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case H5:
					writeD(-1); // High Five NPCString ID
					break;
			}
			
			writeS(_name);
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case H5:
					writeD(-1); // High Five NPCString ID
					break;
			}
			
			writeS(_title);
			writeD(0x01);// Title color 0=client default
			
			writeD(_summon.getPvpFlag());
			writeD(_summon.getKarma());
			
			writeD(gmSeeInvis ? _summon.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask() : _summon.getAbnormalEffect());
			
			writeD(0x00); // clan id
			writeD(0x00); // crest id
			writeD(0x00); // C2
			writeD(0x00); // C2
			writeC(_summon.isInsideZone(ZoneIdType.WATER) ? 1 : _summon.isFlying() ? 2 : 0); // C2
			
			writeC(_summon.getTeam().getId());
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_enchantEffect); // C4
			writeD(0x00); // C6
			writeD(0x00);
			writeD(_form); // CT1.5 Pet form and skills
			writeC(0x01);
			writeC(0x01);
			writeD(_summon.getSpecialEffect());
		}
	}
}
