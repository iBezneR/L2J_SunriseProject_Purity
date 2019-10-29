package handlers;

import gr.sr.configsEngine.configs.impl.*;
import gr.sr.voteEngine.RewardVote;
import handlers.actionhandlers.*;
import handlers.actionshifthandlers.*;
import handlers.admincommandhandlers.*;
import handlers.bypasshandlers.*;
import handlers.chathandlers.*;
import handlers.itemhandlers.*;
import handlers.playeractions.*;
import handlers.punishmenthandlers.BanHandler;
import handlers.punishmenthandlers.ChatBanHandler;
import handlers.punishmenthandlers.JailHandler;
import handlers.skillhandlers.*;
import handlers.targethandlers.*;
import handlers.telnethandlers.*;
import handlers.usercommandhandlers.*;
import handlers.voicedcommandhandlers.*;
import l2r.Config;
import l2r.gameserver.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master handler.
 * @author vGodFather
 */
public class MasterHandler
{
	private static final Logger _log = LoggerFactory.getLogger(MasterHandler.class);
	
	private static final Class<?>[] ACTION_HANDLERS =
	{
		// Action Handlers
		L2ArtefactInstanceAction.class,
		L2DecoyAction.class,
		L2DoorInstanceAction.class,
		L2ItemInstanceAction.class,
		L2NpcAction.class,
		L2PcInstanceAction.class,
		L2PetInstanceAction.class,
		L2StaticObjectInstanceAction.class,
		L2SummonAction.class,
		L2TrapAction.class,
	};

	private static final Class<?>[] ACTION_SHIFT_HANDLERS =
	{
		// Action Shift Handlers
		L2DoorInstanceActionShift.class,
		L2ItemInstanceActionShift.class,
		L2NpcActionShift.class,
		L2PcInstanceActionShift.class,
		L2StaticObjectInstanceActionShift.class,
		L2SummonActionShift.class,
	};

	private static final Class<?>[] ADMIN_HANDLERS =
	{
		// Admin Command Handlers
		AdminAdmin.class,
		AdminAnnouncements.class,
		AdminBBS.class,
		AdminBuffs.class,
		AdminCamera.class,
		AdminChangeAccessLevel.class,
		AdminCheckBots.class,
		AdminCHSiege.class,
		AdminClan.class,
		AdminCreateItem.class,
		AdminCursedWeapons.class,
		AdminCustomCreateItem.class,
		AdminDebug.class,
		AdminDelete.class,
		AdminDisconnect.class,
		AdminDoorControl.class,
		AdminEditChar.class,
		AdminEffects.class,
		AdminElement.class,
		AdminEnchant.class,
		AdminExpSp.class,
		AdminFightCalculator.class,
		AdminFortSiege.class,
		AdminGamePoints.class,
		AdminGeodata.class,
		AdminGm.class,
		AdminGmChat.class,
		AdminGraciaSeeds.class,
		AdminGrandBoss.class,
		AdminHeal.class,
		AdminHellbound.class,
		AdminHtml.class,
		AdminHWIDBan.class,
		AdminInstance.class,
		AdminInstanceZone.class,
		AdminInventory.class,
		AdminInvul.class,
		AdminKick.class,
		AdminKill.class,
		AdminLevel.class,
		AdminLogin.class,
		AdminMammon.class,
		AdminManor.class,
		AdminMenu.class,
		AdminMessages.class,
		AdminMobGroup.class,
		AdminMonsterRace.class,
		AdminOlympiad.class,
		AdminPathNode.class,
		AdminPcCondOverride.class,
		AdminPetition.class,
		AdminPForge.class,
		AdminPledge.class,
		AdminPolymorph.class,
		AdminPremium.class,
		AdminPunishment.class,
		AdminQuest.class,
		AdminReload.class,
		AdminRepairChar.class,
		AdminRes.class,
		AdminRide.class,
		AdminScan.class,
		AdminShop.class,
		AdminShowQuests.class,
		AdminShutdown.class,
		AdminSiege.class,
		AdminSkill.class,
		AdminSpawn.class,
		AdminSummon.class,
		AdminTarget.class,
		AdminTargetSay.class,
		AdminTeleport.class,
		AdminTerritoryWar.class,
		AdminTest.class,
		AdminUnblockIp.class,
		AdminVitality.class,
		AdminZone.class,
		AdminLogsViewer.class,
	};

	private static final Class<?>[] BYPASS_HANDLERS =
	{
		// Bypass Handlers
		ArenaBuff.class,
		Augment.class,
		Buy.class,
		BuyShadowItem.class,
		ChatLink.class,
		ClanWarehouse.class,
		ElcardiaBuff.class,
		Festival.class,
		Freight.class,
		ItemAuctionLink.class,
		Link.class,
		Loto.class,
		Multisell.class,
		NpcViewMod.class,
		Observation.class,
		OlympiadManagerLink.class,
		OlympiadObservation.class,
		PlayerHelp.class,
		PrivateWarehouse.class,
		QuestLink.class,
		QuestList.class,
		ReleaseAttribute.class,
		RemoveDeathPenalty.class,
		RentPet.class,
		Rift.class,
		SkillList.class,
		SupportBlessing.class,
		SupportMagic.class,
		TerritoryStatus.class,
		VoiceCommand.class,
		Wear.class,
	};

	private static final Class<?>[] CHAT_HANDLERS =
	{
		// Chat Handlers
		ChatAll.class,
		ChatAlliance.class,
		ChatBattlefield.class,
		ChatClan.class,
		ChatHeroVoice.class,
		ChatParty.class,
		ChatPartyMatchRoom.class,
		ChatPartyRoomAll.class,
		ChatPartyRoomCommander.class,
		ChatPetition.class,
		ChatShout.class,
		ChatTell.class,
		ChatTrade.class,
	};

	private static final Class<?>[] ITEM_HANDLERS =
	{
		// Item Handlers
		(BufferConfigs.ENABLE_ITEM_BUFFER ? AioItemBuff.class : null),
		(AioItemsConfigs.ENABLE_AIO_NPCS ? AioItemNpcs.class : null),
		BeastSoulShot.class,
		BeastSpiritShot.class,
		BlessedSpiritShot.class,
		Book.class,
            BoxItem.class,
		Bypass.class,
		Calculator.class,
		CharmOfCourage.class,
		ChristmasTree.class,
		Disguise.class,
		Elixir.class,
		EnchantAttribute.class,
		EnchantScrolls.class,
		EventItem.class,
		ExtractableItems.class,
		FishShots.class,
		Harvester.class,
		ItemSkills.class,
		ItemSkillsTemplate.class,
		ManaPotion.class,
		Maps.class,
		MercTicket.class,
		NicknameColor.class,
		PetFood.class,
		Recipes.class,
		RollingDice.class,
		Seed.class,
		SevenSignsRecord.class,
		SoulShots.class,
		SpecialXMas.class,
		SpiritShot.class,
		SummonItems.class,
		TeleportBookmark.class,
	};

	private static final Class<?>[] PUNISHMENT_HANDLERS =
	{
		// Punishment Handlers
		BanHandler.class,
		ChatBanHandler.class,
		JailHandler.class,
	};

	private static final Class<?>[] SKILL_HANDLERS =
	{
		// Skill Handlers
		Blow.class,
		ChainHeal.class,
		Continuous.class,
		Disablers.class,
		Dummy.class,
		Mdam.class,
		Pdam.class,
		Unlock.class,
	};

	private static final Class<?>[] USER_COMMAND_HANDLERS =
	{
		// User Command Handlers
		ChannelDelete.class,
		ChannelInfo.class,
		ChannelLeave.class,
		ClanPenalty.class,
		ClanWarsList.class,
		Dismount.class,
		InstanceZone.class,
		Loc.class,
		Mount.class,
		MyBirthday.class,
		OlympiadStat.class,
		PartyInfo.class,
		SiegeStatus.class,
		Time.class,
		Unstuck.class,
	};

	private static final Class<?>[] TARGET_HANDLERS =
	{
		// Target Handlers
		Area.class,
		AreaCorpseMob.class,
		AreaFriendly.class,
		AreaSummon.class,
		Aura.class,
		AuraCorpseMob.class,
		AuraFriendly.class,
		AuraUndeadEnemy.class,
		BehindArea.class,
		BehindAura.class,
		Clan.class,
		ClanMember.class,
		CommandChannel.class,
		CorpseClan.class,
		CorpseMob.class,
		CorpsePet.class,
		CorpsePlayer.class,
		EnemySummon.class,
		FlagPole.class,
		FrontArea.class,
		FrontAura.class,
		Ground.class,
		Holy.class,
		One.class,
		OwnerPet.class,
		Party.class,
		PartyClan.class,
		PartyMember.class,
		PartyNotMe.class,
		PartyOther.class,
		PartyTarget.class,
		Pet.class,
		Self.class,
		Siege.class,
		Summon.class,
		TargetParty.class,
		Unlockable.class,
	};

	private static final Class<?>[] TELNET_HANDLERS =
	{
		// Telnet Handlers
		ChatsHandler.class,
		DebugHandler.class,
		HelpHandler.class,
		PlayerHandler.class,
		ReloadHandler.class,
		ServerHandler.class,
		StatusHandler.class,
		ThreadHandler.class,
	};

	private static final Class<?>[] PLAYER_ACTION_HANDLERS_ =
	{
		// Action Handlers
		AirshipAction.class,
		BotReport.class,
		PetAttack.class,
		PetHold.class,
		PetMove.class,
		PetSkillUse.class,
		PetStop.class,
		PrivateStore.class,
		Ride.class,
		RunWalk.class,
		ServitorAttack.class,
		ServitorHold.class,
		ServitorMove.class,
		ServitorSkillUse.class,
		ServitorStop.class,
		SitStand.class,
		SocialAction.class,
		UnsummonPet.class,
		UnsummonServitor.class,
	};
	
	private static final Class<?>[] VOICED_COMMAND_HANDLERS =
	{
		// Voiced Command Handlers
		(AioItemsConfigs.ALLOW_AIO_ITEM_COMMAND && AioItemsConfigs.ENABLE_AIO_NPCS ? AioItemVCmd.class : null),
		(AntibotConfigs.ENABLE_ANTIBOT_SYSTEMS ? Antibot.class : null),
		(Config.BANKING_SYSTEM_ENABLED ? Banking.class : null),
		(CustomServerConfigs.ENABLE_CHARACTER_CONTROL_PANEL ? CcpVCmd.class : null),
		(Config.L2JMOD_ALLOW_CHANGE_PASSWORD ? ChangePassword.class : null),
		(Config.L2JMOD_CHAT_ADMIN ? ChatAdmin.class : null),
		(Config.L2JMOD_DEBUG_VOICE_COMMAND ? Debug.class : null),
		(CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS ? EvenlyDistributeItems.class : null),
		(Config.L2JMOD_HELLBOUND_STATUS ? Hellbound.class : null),
		(BufferConfigs.ENABLE_ITEM_BUFFER && PremiumServiceConfigs.USE_PREMIUM_SERVICE ? ItemBufferVCmd.class : null),
		(Config.L2JMOD_MULTILANG_ENABLE && Config.L2JMOD_MULTILANG_VOICED_ALLOW ? Lang.class : null),
		(CustomServerConfigs.ALLOW_ONLINE_COMMAND ? OnlineVCmd.class : null),
		(PremiumServiceConfigs.USE_PREMIUM_SERVICE ? PremiumVCmd.class : null),
		(ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE ? PvpZoneVCmd.class : null),
		(CustomServerConfigs.ALLOW_REPAIR_COMMAND ? RepairVCmd.class : null),
		(CustomServerConfigs.ALLOW_TELEPORTS_COMMAND ? TeleportsVCmd.class : null),
		PingVCmd.class,
		(Config.L2JMOD_ALLOW_WEDDING ? Wedding.class : null),
		(GetRewardVoteSystemConfigs.ENABLE_VOTE_SYSTEM ? RewardVote.class : null),
	};

	private void loadHandlers(IHandler<?, ?> handler, Class<?>[] classes)
	{
		for (Class<?> c : classes)
		{
			if (c == null)
			{
				continue;
			}

			try
			{
				handler.registerByClass(c);
			}
			catch (Exception ex)
			{
				_log.error("Failed loading handler {}!", c.getSimpleName(), ex);
			}
		}
		
		_log.info("{}: Loaded {} handlers.", handler.getClass().getSimpleName(), handler.size());
	}

	public MasterHandler()
	{
		final long startCache = System.currentTimeMillis();
		loadHandlers(VoicedCommandHandler.getInstance(), VOICED_COMMAND_HANDLERS);
		loadHandlers(ActionHandler.getInstance(), ACTION_HANDLERS);
		loadHandlers(ActionShiftHandler.getInstance(), ACTION_SHIFT_HANDLERS);
		loadHandlers(SkillHandler.getInstance(), SKILL_HANDLERS);
		loadHandlers(PlayerActionHandler.getInstance(), PLAYER_ACTION_HANDLERS_);
		loadHandlers(AdminCommandHandler.getInstance(), ADMIN_HANDLERS);
		loadHandlers(BypassHandler.getInstance(), BYPASS_HANDLERS);
		loadHandlers(ChatHandler.getInstance(), CHAT_HANDLERS);
		loadHandlers(ItemHandler.getInstance(), ITEM_HANDLERS);
		loadHandlers(PunishmentHandler.getInstance(), PUNISHMENT_HANDLERS);
		loadHandlers(UserCommandHandler.getInstance(), USER_COMMAND_HANDLERS);
		loadHandlers(TargetHandler.getInstance(), TARGET_HANDLERS);
		loadHandlers(TelnetHandler.getInstance(), TELNET_HANDLERS);
		_log.info(MasterHandler.class.getSimpleName() + " loaded. (GenTime: {} ms) ", (System.currentTimeMillis() - startCache));
	}

	public static void main(String[] args)
	{
		new MasterHandler();
	}
}