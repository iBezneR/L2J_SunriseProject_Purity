package l2r.gameserver.network.clientpackets;

import custom.museum.MuseumManager;
import gr.sr.antibotEngine.AntibotSystem;
import gr.sr.configsEngine.configs.impl.CustomServerConfigs;
import gr.sr.configsEngine.configs.impl.SecuritySystemConfigs;
import gr.sr.interf.SunriseEvents;
import gr.sr.main.EnterWorldCustomHandler;
import gr.sr.main.PlayerValues;
import gr.sr.protection.Protection;
import gr.sr.protection.network.ProtectionManager;
import gr.sr.securityEngine.SecurityActions;
import gr.sr.securityEngine.SecurityType;
import gr.sr.utils.Tools;
import l2r.Config;
import l2r.gameserver.LoginServerThread;
import l2r.gameserver.SevenSigns;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.data.sql.AnnouncementsTable;
import l2r.gameserver.data.xml.impl.AdminData;
import l2r.gameserver.data.xml.impl.SkillTreesData;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.TeleportWhereType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.*;
import l2r.gameserver.instancemanager.petition.PetitionManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2ClassMasterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Couple;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.FortSiege;
import l2r.gameserver.model.entity.Siege;
import l2r.gameserver.model.entity.clanhall.AuctionableHall;
import l2r.gameserver.model.entity.clanhall.SiegableHall;
import l2r.gameserver.model.entity.olympiad.Olympiad;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.CommonSkill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.handlers.gc.packets.out.ExAdenaInvenCount;
import l2r.gameserver.network.handlers.gc.packets.out.ExUserInfoEquipSlot;
import l2r.gameserver.network.handlers.gc.packets.out.ExUserInfoInvenWeight;
import l2r.gameserver.network.handlers.types.MacroUpdateType;
import l2r.gameserver.network.serverpackets.*;

public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__11_ENTERWORLD = "[C] 11 EnterWorld";
	
	private static final double MIN_HP = 0.5;
	
	private static final int COMBAT_FLAG = 9819;
	
	private final int[][] tracert = new int[5][4];
	
	@Override
	protected void readImpl()
	{
		readB(new byte[32]); // Unknown Byte Array
		readD(); // Unknown Value
		readD(); // Unknown Value
		readD(); // Unknown Value
		readD(); // Unknown Value
		readB(new byte[32]); // Unknown Byte Array
		readD(); // Unknown Value
		for (int i = 0; i < 5; i++)
		{
			for (int o = 0; o < 4; o++)
			{
				tracert[i][o] = readC();
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if ((activeChar == null) || !PlayerValues.isPlayer())
		{
			_log.warn("EnterWorld failed! activeChar returned 'null'.");
			getClient().closeNow();
			return;
		}
		
		final String[] address = new String[5];
		for (int i = 0; i < 5; i++)
		{
			address[i] = tracert[i][0] + "." + tracert[i][1] + "." + tracert[i][2] + "." + tracert[i][3];
		}
		
		LoginServerThread.getInstance().sendClientTracert(activeChar.getAccountName(), address);
		
		getClient().setClientTracert(tracert);
		
		// Restore to instanced area if enabled
		if (Config.RESTORE_PLAYER_INSTANCE)
		{
			activeChar.setInstanceId(InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId()));
		}
		else
		{
			int instanceId = InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId());
			if (instanceId > 0)
			{
				InstanceManager.getInstance().getInstance(instanceId).removePlayer(activeChar.getObjectId());
			}
		}
		
		if (Config.DEBUG)
		{
			if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
			{
				_log.warn("User already exists in Object ID map! User " + activeChar.getName() + " is a character clone.");
			}
		}
		
		// Apply special GM properties to the GM when entering
		if (activeChar.isGM())
		{
			if (SecuritySystemConfigs.ENABLE_ADMIN_SECURITY_SYSTEM)
			{
				if (SecuritySystemConfigs.ADMIN_OBJECT_ID_LIST.contains(activeChar.getObjectId()))
				{
					activeChar.getPcAdmin().setIsSafeAdmin(true);
					_log.info("Safe Admin: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") has been logged in.");
				}
				else
				{
					activeChar.getPcAdmin().punishUnSafeAdmin();
					_log.warn("WARNING: Unsafe Admin: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") has been logged in.");
					_log.warn("If you have enabled some punishment, He will be punished.");
				}
			}
			
			if (Config.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
			{
				activeChar.setIsInvul(true);
			}
			
			if (Config.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
			{
				activeChar.setInvisible(true);
			}
			
			if (Config.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
			{
				activeChar.setSilenceMode(true);
			}
			
			if (Config.GM_STARTUP_DIET_MODE && AdminData.getInstance().hasAccess("admin_diet", activeChar.getAccessLevel()))
			{
				activeChar.setDietMode(true);
			}
			
			if (Config.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
			{
				AdminData.getInstance().addGm(activeChar, false);
			}
			else
			{
				AdminData.getInstance().addGm(activeChar, true);
			}
			
			if (Config.GM_GIVE_SPECIAL_SKILLS)
			{
				SkillTreesData.getInstance().addSkills(activeChar, false);
			}
			
			if (Config.GM_GIVE_SPECIAL_AURA_SKILLS)
			{
				SkillTreesData.getInstance().addSkills(activeChar, true);
			}
		}
		
		// Set dead status if applies
		if (activeChar.getCurrentHp() < MIN_HP)
		{
			activeChar.setIsDead(true);
		}
		
		boolean showClanNotice = false;
		
		final L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			activeChar.sendPacket(new PledgeSkillList(clan));
			
			notifyClanMembers(activeChar);
			
			notifySponsorOrApprentice(activeChar);
			
			final AuctionableHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if ((clanHall != null) && !clanHall.getPaid() && (clan.getWarehouse().getAdena() < clanHall.getLease()))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
				sm.addInt(clanHall.getLease());
				activeChar.sendPacket(sm);
			}
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.isInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(clan))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getCastle().getResidenceId());
				}
				else if (siege.checkIsDefender(clan))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getCastle().getResidenceId());
				}
			}
			
			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				if (!siege.isInProgress())
				{
					continue;
				}
				
				if (siege.checkIsAttacker(clan))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getFort().getResidenceId());
				}
				else if (siege.checkIsDefender(clan))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getFort().getResidenceId());
				}
			}
			
			for (SiegableHall hall : CHSiegeManager.getInstance().getConquerableHalls().values())
			{
				if (!hall.isInSiege())
				{
					continue;
				}
				
				if (hall.isRegistered(clan))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(hall.getId());
					activeChar.setIsInHideoutSiege(true);
				}
			}
			
			sendPacket(new PledgeShowMemberListAll(clan, activeChar));
			sendPacket(new PledgeStatusChanged(clan));
			
			// Residential skills support
			if (clan.getCastleId() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(clan).giveResidentialSkills(activeChar);
			}
			
			if (clan.getFortId() > 0)
			{
				FortManager.getInstance().getFortByOwner(clan).giveResidentialSkills(activeChar);
			}
			
			showClanNotice = clan.isNoticeEnabled();
		}
		
		if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar) > 0)
		{
			if (TerritoryWarManager.getInstance().isTWInProgress())
			{
				activeChar.setSiegeState((byte) 1);
			}
			activeChar.setSiegeSide(TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar));
		}
		
		// Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod() && (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) != SevenSigns.CABAL_NULL))
		{
			int cabal = SevenSigns.getInstance().getPlayerCabal(activeChar.getObjectId());
			if (cabal != SevenSigns.CABAL_NULL)
			{
				if (cabal == SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
				{
					activeChar.addSkill(CommonSkill.THE_VICTOR_OF_WAR.getSkill());
				}
				else
				{
					activeChar.addSkill(CommonSkill.THE_VANQUISHED_OF_WAR.getSkill());
				}
			}
		}
		else
		{
			activeChar.removeSkill(CommonSkill.THE_VICTOR_OF_WAR.getSkill());
			activeChar.removeSkill(CommonSkill.THE_VANQUISHED_OF_WAR.getSkill());
		}
		
		if (Config.ENABLE_VITALITY && Config.RECOVER_VITALITY_ON_RECONNECT)
		{
			float points = (Config.RATE_RECOVERY_ON_RECONNECT * (System.currentTimeMillis() - activeChar.getLastAccess())) / 60000;
			if (points > 0)
			{
				activeChar.updateVitalityPoints(points, false, true);
			}
		}
		
		// Send Macro List
		activeChar.getMacros().sendUpdate(MacroUpdateType.LIST);
		
		// Send Item List
		activeChar.sendItemList(false);
		
		// Send GG check
		// activeChar.queryGameGuard();
		
		// Send Teleport Bookmark List
		sendPacket(new ExGetBookMarkInfoPacket(activeChar));
		
		// Send Shortcuts
		sendPacket(new ShortCutInit(activeChar));
		
		// Send Action list
		activeChar.sendPacket(ExBasicActionList.STATIC_PACKET);
		
		// Send Skill list
		activeChar.sendSkillList();
		
		// Send Dye Information
		activeChar.sendPacket(new HennaInfo(activeChar));
		
		Quest.playerEnter(activeChar);
		
		if (!Config.DISABLE_TUTORIAL)
		{
			loadTutorial(activeChar);
		}
		
		activeChar.sendPacket(new QuestList(activeChar));
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}
		
		activeChar.entering = false;
		activeChar.broadcastUserInfo();
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		// Send Inventory Info
		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
		
		// Send Adena / Inventory Count Info
		activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
		
		// Send Equipped Items
		activeChar.sendPacket(new ExUserInfoEquipSlot(activeChar));
		
		// Handle soulshots, disable all on EnterWorld
		activeChar.sendPacket(new ExAutoSoulShot(0, true, 0));
		activeChar.sendPacket(new ExAutoSoulShot(0, true, 1));
		activeChar.sendPacket(new ExAutoSoulShot(0, true, 2));
		activeChar.sendPacket(new ExAutoSoulShot(0, true, 3));
		
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		if (activeChar.isTransformed())
		{
			// Required double send for fix Mounted H5+
			// We will force server to send update again with low priority
			activeChar.broadcastUserInfo(false);
		}
		
		activeChar.getInventory().applyItemSkills();
		
		// Wedding Checks
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar, activeChar.getPartnerId());
		}
		
		if (activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin();
		}
		
		activeChar.updateEffectIcons();
		
		// Expand Skill
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		
		sendPacket(new FriendList(activeChar));
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addCharName(activeChar);
		for (int id : activeChar.getFriendList())
		{
			final L2Object obj = L2World.getInstance().findObject(id);
			if (obj != null)
			{
				obj.sendPacket(sm);
			}
		}
		
		activeChar.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		
		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		AnnouncementsTable.getInstance().showAnnouncements(activeChar);
		
		if (showClanNotice)
		{
			final NpcHtmlMessage notice = new NpcHtmlMessage();
			notice.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/clanNotice.htm");
			
			String message = activeChar.getClan().getNotice();
			message = message.replace(">", "&gt;");
			message = message.replace("<", "&lt;");
			message = message.replace("\n", "<br1>");
			
			notice.replace("%clan_name%", activeChar.getClan().getName());
			notice.replace("%notice_text%", message);
			notice.disableValidation();
			sendPacket(notice);
		}
		else if (Config.SERVER_NEWS)
		{
			String serverNews = HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/servnews.htm");
			if (serverNews != null)
			{
				sendPacket(new NpcHtmlMessage(serverNews));
			}
		}
		
		if (Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
		}
		
		if (activeChar.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}
		
		activeChar.onPlayerEnter();
		
		sendPacket(new SkillCoolTime(activeChar));
		sendPacket(new ExVoteSystemInfo(activeChar));
		sendPacket(new ExShowContactList(activeChar));
		
		for (L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if (!activeChar.isGM())
			{
				if (item.isEquipable())
				{
					if ((item.getEnchantLevel() > SecuritySystemConfigs.MAX_ENCHANT_LEVEL) && (SecuritySystemConfigs.MAX_ENCHANT_LEVEL > 0))
					{
						// Delete over enchanted item(s)
						activeChar.destroyItem("Security", item, activeChar, true);
						SecurityActions.startSecurity(activeChar, SecurityType.ENCHANT_EXPLOIT);
					}
				}
			}
			
			if (item.isTimeLimitedItem())
			{
				item.scheduleLifeTimeTask();
			}
			if (item.isShadowItem() && item.isEquipped())
			{
				item.decreaseMana(false);
			}
		}
		
		for (L2ItemInstance whItem : activeChar.getWarehouse().getItems())
		{
			if (whItem.isTimeLimitedItem())
			{
				whItem.scheduleLifeTimeTask();
			}
		}
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		}
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		}
		
		// remove combat flag before teleporting
		final L2ItemInstance combatFlag = activeChar.getInventory().getItemByItemId(COMBAT_FLAG);
		if (combatFlag != null)
		{
			final Fort fort = FortManager.getInstance().getFort(activeChar);
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getResidenceId());
			}
			else
			{
				final int slot = activeChar.getInventory().getSlotFromItem(combatFlag);
				activeChar.getInventory().unEquipItemInBodySlot(slot);
				activeChar.destroyItem("CombatFlag", combatFlag, null, true);
			}
		}
		
		// Attacker or spectator logging in to a siege zone.
		// Actually should be checked for inside castle only?
		if (!activeChar.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && activeChar.isInsideZone(ZoneIdType.SIEGE) && (!activeChar.isInSiege() || (activeChar.getSiegeState() < 2)))
		{
			activeChar.teleToLocation(TeleportWhereType.TOWN);
		}
		
		if (Config.ALLOW_MAIL)
		{
			if (MailManager.getInstance().hasUnreadPost(activeChar))
			{
				sendPacket(ExNoticePostArrived.valueOf(false));
			}
		}
		
		SunriseEvents.onLogin(activeChar);
		
		if (Config.WELCOME_MESSAGE_ENABLED)
		{
			activeChar.sendPacket(new ExShowScreenMessage(Config.WELCOME_MESSAGE_TEXT, Config.WELCOME_MESSAGE_TIME));
		}
		
		/** Start of Custom Section */
		EnterWorldCustomHandler.getInstance().extraItemsCheck(activeChar);
		EnterWorldCustomHandler.getInstance().extraMessages(activeChar);
		EnterWorldCustomHandler.getInstance().extraNotifies(activeChar);
		EnterWorldCustomHandler.getInstance().clanLeaderSystem(activeChar);
		EnterWorldCustomHandler.getInstance().checkAutoSoulshot(activeChar);
		EnterWorldCustomHandler.getInstance().checkPremiumAndPcBangSystems(activeChar);
		EnterWorldCustomHandler.getInstance().initializeColorSystem(activeChar);
		EnterWorldCustomHandler.getInstance().checkIfBot(activeChar);
		AntibotSystem.checkOnEnterBot(activeChar);
		if (Olympiad.getInstance().playerInStadia(activeChar))
		{
			activeChar.doRevive();
			activeChar.teleToLocation(TeleportWhereType.TOWN);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium");
		}
		if (Protection.isProtectionOn())
		{
			ProtectionManager.SendSpecialSting(getClient());
		}
		/** End of Custom Section */
		
		L2ClassMasterInstance.showQuestionMark(activeChar);
		
		int birthday = activeChar.checkBirthDay();
		if (birthday == 0)
		{
			activeChar.sendPacket(SystemMessageId.YOUR_BIRTHDAY_GIFT_HAS_ARRIVED);
			// activeChar.sendPacket(new ExBirthdayPopup()); Removed in H5?
		}
		else if (birthday != -1)
		{
			final SystemMessage sm1 = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY);
			sm1.addInt(birthday);
			activeChar.sendPacket(sm1);
		}
		
		if (!activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(ExNotifyPremiumItem.STATIC_PACKET);
		}
		
		// vGodFather: Missing retail messages
		DayNightSpawnManager.getInstance().shadowFeelMsg(activeChar);
		
		if (CustomServerConfigs.DUAL_BOX_IN_GAME > 0)
		{
			int inGameClients = 0;
			for (L2PcInstance inGameChar : L2World.getInstance().getPlayers())
			{
				if ((inGameChar.getClient() != null) && !inGameChar.getClient().isDetached() && Tools.isDualBox(activeChar, inGameChar))
				{
					inGameClients++;
				}
				
				if (inGameClients > CustomServerConfigs.DUAL_BOX_IN_GAME)
				{
					activeChar.logout();
					break;
				}
			}
		}
		
		if (!activeChar.getAchievementPlayer().isAchievementFinished("login_for_first_time"))
		{
			activeChar.getAchievementPlayer().setAchievementPoints("login_for_first_time", 1);
		}

		if (!activeChar.getAchievementPlayer().isAchievementFinished("play_time"))
		{
			activeChar.getAchievementPlayer().startCountingAchievement("play_time");
		}

		MuseumManager.getInstance().giveReward(activeChar);

		if (Config.PCB_ENABLE)
		{
			activeChar.showPcBangWindow();
		}

		// Unstuck players that had client open when server crashed.
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void engage(L2PcInstance cha)
	{
		final int chaId = cha.getObjectId();
		for (Couple cl : CoupleManager.getInstance().getCouples())
		{
			if ((cl.getPlayer1Id() == chaId) || (cl.getPlayer2Id() == chaId))
			{
				if (cl.getMaried())
				{
					cha.setMarried(true);
				}
				
				cha.setCoupleId(cl.getId());
				
				if (cl.getPlayer1Id() == chaId)
				{
					cha.setPartnerId(cl.getPlayer2Id());
				}
				else
				{
					cha.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}
	
	/**
	 * @param cha
	 * @param partnerId
	 */
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		final L2PcInstance partner = L2World.getInstance().getPlayer(cha.getPartnerId());
		if (partner != null)
		{
			partner.sendMessage("Your Partner has logged in.");
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		final L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			final L2PcInstance sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());
			if (sponsor != null)
			{
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			final L2PcInstance apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());
			if (apprentice != null)
			{
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}
	
	private static void loadTutorial(L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Quest.TUTORIAL);
		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__11_ENTERWORLD;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
