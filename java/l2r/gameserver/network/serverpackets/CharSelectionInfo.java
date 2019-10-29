/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.data.sql.ClanTable;
import l2r.gameserver.data.xml.impl.ExperienceData;
import l2r.gameserver.model.CharSelectInfoPackage;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.network.L2GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CharSelectionInfo extends L2GameServerPacket
{
	private static Logger _log = LoggerFactory.getLogger(CharSelectionInfo.class);
	private final String _loginName;
	private final int _sessionId;
	private int _activeId;
	private final List<CharSelectInfoPackage> _characterPackages;
	
	/**
	 * Constructor for CharSelectionInfo.
	 * @param loginName
	 * @param sessionId
	 */
	public CharSelectionInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = -1;
	}
	
	public CharSelectionInfo(String loginName, int sessionId, int activeId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = activeId;
	}
	
	public List<CharSelectInfoPackage> getCharInfo()
	{
		return _characterPackages;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x13);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x09);
				break;
		}
		
		int size = (_characterPackages.size());
		writeD(size);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				// Can prevent players from creating new characters (if 0); (if 1, the client will ask if chars may be created (0x13)
				// Response: (0x0D) )
				writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT);
				writeC(size == Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT ? 0x01 : 0x00);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
				writeC(0x01); // 0=can't play, 1=can play free until level 85, 2=100% free play
				writeD(0x02); // if 1, Korean client
				writeC(0x00); // Balthus Knights, if 1 suggests premium account
				break;
			case SL:
				writeC(0x01); // 0=can't play, 1=can play free until level 85, 2=100% free play
				writeD(0x02); // if 1, Korean client
				writeC(0x00); // Gift message for inactive accounts // 152
				writeC(0x00); // Balthus Knights, if 1 suggests premium account
				break;
		}
		
		long lastAccess = 0L;
		
		if (_activeId == -1)
		{
			for (int i = 0; i < size; i++)
			{
				final CharSelectInfoPackage charInfoPackage = _characterPackages.get(i);
				if (lastAccess < charInfoPackage.getLastAccess())
				{
					lastAccess = charInfoPackage.getLastAccess();
					_activeId = i;
				}
			}
		}
		
		for (int i = 0; i < size; i++)
		{
			final CharSelectInfoPackage charInfoPackage = _characterPackages.get(i);
			
			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getObjectId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00); // Builder Level
			
			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			writeD(charInfoPackage.getBaseClassId());
			
			writeD(0x01); // GameServerName
			
			writeD(charInfoPackage.getX());
			writeD(charInfoPackage.getY());
			writeD(charInfoPackage.getZ());
			
			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case FREYA:
				case H5:
					writeD(charInfoPackage.getSp());
					break;
				case GC:
				case SL:
					writeQ(charInfoPackage.getSp());
					break;
			}
			
			writeQ(charInfoPackage.getExp());
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case H5:
				case GC:
				case SL:
					writeF((float) (charInfoPackage.getExp() - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())) / (ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())));
					break;
			}
			
			writeD(charInfoPackage.getLevel());
			
			writeD(charInfoPackage.getKarma());
			writeD(charInfoPackage.getPkKills());
			writeD(charInfoPackage.getPvPKills());
			
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					writeD(0x00); // Ertheia
					writeD(0x00); // Ertheia
					break;
			}
			
			for (int slot : getPaperdollOrder())
			{
				writeD(charInfoPackage.getPaperdollItemId(slot));
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
					for (int slot : getPaperdollOrder())
					{
						writeD(charInfoPackage.getPaperdollItemId(slot));
					}
					break;
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case SL:
					writeD(0x00); // Salvation
					writeD(0x00); // Salvation
					writeD(0x00); // Salvation
					writeD(0x00); // Salvation
					writeD(0x00); // Salvation
					break;
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					
					for (int slot : getPaperdollOrderVisualId())
					{
						writeD(charInfoPackage.getPaperdollItemVisualId(slot));
					}
					
					writeH(0x00); // Upper Body enchant level
					writeH(0x00); // Lower Body enchant level
					writeH(0x00); // Headgear enchant level
					writeH(0x00); // Gloves enchant level
					writeH(0x00); // Boots enchant level
					break;
			}
			
			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());
			
			writeF(charInfoPackage.getMaxHp()); // hp max
			writeF(charInfoPackage.getMaxMp()); // mp max
			
			writeD(charInfoPackage.getDeleteTimer() > 0 ? (int) ((charInfoPackage.getDeleteTimer() - System.currentTimeMillis()) / 1000) : 0);
			writeD(charInfoPackage.getClassId());
			writeD(i == _activeId ? 0x01 : 0x00); // c3 auto-select char
			
			writeC(Math.min(charInfoPackage.getEnchantEffect(), 127));
			writeD(charInfoPackage.getAugmentationId());
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					// Secondary augment id
					writeD(charInfoPackage.getAugmentationId());
					break;
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
				case GC:
				case SL:
					writeD(0x00); // Currently on retail when you are on character select you don't see your transformation.
					break;
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case FREYA:
				case H5:
				case GC:
				case SL:
					// Implementing it will be waster of resources.
					writeD(0x00); // Pet ID
					writeD(0x00); // Pet Level
					writeD(0x00); // Pet Max Food
					writeD(0x00); // Pet Current Food
					writeF(0x00); // Pet Max HP
					writeF(0x00); // Pet Max MP
					break;
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case H5:
				case GC:
				case SL:
					writeD(charInfoPackage.getVitalityPoints());
					break;
			}
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					writeD(100/* (int) Config.RATE_VITALITY_EXP_MULTIPLIER * 100 */); // Vitality Percent
					writeD(1/* charInfoPackage.getVitalityItemsUsed() */); // Remaining vitality item uses
					writeD(charInfoPackage.getAccessLevel() == -100 ? 0x00 : 0x01); // Char is active or not
					writeC(charInfoPackage.isNoble() ? 0x01 : 0x00); // noble
					writeC(Hero.getInstance().isHero(charInfoPackage.getObjectId()) ? 0x01 : 0x00); // Hero glow
					writeC(0/* charInfoPackage.isHairAccessoryEnabled() ? 0x01 : 0x00 */); // Show hair accessory if enabled
					break;
			}
		}
	}
	
	private static List<CharSelectInfoPackage> loadCharacterSelectInfo(String loginName)
	{
		final List<CharSelectInfoPackage> characterList = new ArrayList<>();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE account_name=? ORDER BY createDate"))
		{
			statement.setString(1, loginName);
			try (ResultSet charList = statement.executeQuery())
			{
				while (charList.next())// fills the package
				{
					CharSelectInfoPackage charInfopackage = restoreChar(charList);
					if (charInfopackage != null)
					{
						characterList.add(charInfopackage);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not restore char info: " + e.getMessage(), e);
		}
		return characterList;
	}
	
	private static void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int ObjectId, int activeClassId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level FROM character_subclasses WHERE charId=? && class_id=? ORDER BY charId"))
		{
			statement.setInt(1, ObjectId);
			statement.setInt(2, activeClassId);
			try (ResultSet charList = statement.executeQuery())
			{
				if (charList.next())
				{
					charInfopackage.setExp(charList.getLong("exp"));
					charInfopackage.setSp(charList.getInt("sp"));
					charInfopackage.setLevel(charList.getInt("level"));
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not restore char subclass info: " + e.getMessage(), e);
		}
	}
	
	public static CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception
	{
		int objectId = chardata.getInt("charId");
		String name = chardata.getString("char_name");
		
		// See if the char must be deleted
		long deletetime = chardata.getLong("deletetime");
		if (deletetime > 0)
		{
			if (System.currentTimeMillis() > deletetime)
			{
				L2Clan clan = ClanTable.getInstance().getClan(chardata.getInt("clanid"));
				if (clan != null)
				{
					clan.removeClanMember(objectId, 0);
				}
				
				L2GameClient.deleteCharByObjId(objectId);
				return null;
			}
		}
		
		CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
		charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
		charInfopackage.setKarma(chardata.getInt("karma"));
		charInfopackage.setPkKills(chardata.getInt("pkkills"));
		charInfopackage.setPvPKills(chardata.getInt("pvpkills"));
		charInfopackage.setFace(chardata.getInt("face"));
		charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
		charInfopackage.setHairColor(chardata.getInt("haircolor"));
		charInfopackage.setSex(chardata.getInt("sex"));
		
		charInfopackage.setExp(chardata.getLong("exp"));
		charInfopackage.setSp(chardata.getInt("sp"));
		charInfopackage.setVitalityPoints(chardata.getInt("vitality_points"));
		charInfopackage.setClanId(chardata.getInt("clanid"));
		
		charInfopackage.setRace(chardata.getInt("race"));
		
		final int baseClassId = chardata.getInt("base_class");
		final int activeClassId = chardata.getInt("classid");
		
		charInfopackage.setX(chardata.getInt("x"));
		charInfopackage.setY(chardata.getInt("y"));
		charInfopackage.setZ(chardata.getInt("z"));
		
		if (Config.L2JMOD_MULTILANG_ENABLE)
		{
			String lang = chardata.getString("language");
			if (!Config.L2JMOD_MULTILANG_ALLOWED.contains(lang))
			{
				lang = Config.L2JMOD_MULTILANG_DEFAULT;
			}
			charInfopackage.setHtmlPrefix("data/lang/" + lang + "/");
		}
		
		// if is in subclass, load subclass exp, sp, lvl info
		if (baseClassId != activeClassId)
		{
			loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
		}
		
		charInfopackage.setClassId(activeClassId);
		
		// Get the augmentation id for equipped weapon
		int weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		if (weaponObjId < 1)
		{
			weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		}
		
		if (weaponObjId > 0)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?"))
			{
				statement.setInt(1, weaponObjId);
				try (ResultSet result = statement.executeQuery())
				{
					if (result.next())
					{
						int augment = result.getInt("augAttributes");
						charInfopackage.setAugmentationId(augment == -1 ? 0 : augment);
					}
				}
			}
			catch (Exception e)
			{
				_log.warn("Could not restore augmentation info: " + e.getMessage(), e);
			}
		}
		
		// Check if the base class is set to zero and also doesn't match with the current active class, otherwise send the base class ID. This prevents chars created before base class was introduced from being displayed incorrectly.
		if ((baseClassId == 0) && (activeClassId > 0))
		{
			charInfopackage.setBaseClassId(activeClassId);
		}
		else
		{
			charInfopackage.setBaseClassId(baseClassId);
		}
		
		charInfopackage.setDeleteTimer(deletetime);
		charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
		
		charInfopackage.setNoble(chardata.getInt("nobless") == 1);
		return charInfopackage;
	}
	
	private static final int[] PAPERDOLL_ORDER_SL = new int[]
	{
		Inventory.PAPERDOLL_UNDER,
		Inventory.PAPERDOLL_REAR,
		Inventory.PAPERDOLL_LEAR,
		Inventory.PAPERDOLL_NECK,
		Inventory.PAPERDOLL_RFINGER,
		Inventory.PAPERDOLL_LFINGER,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_CLOAK,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_HAIR2,
		Inventory.PAPERDOLL_RBRACELET,
		Inventory.PAPERDOLL_LBRACELET,
		Inventory.PAPERDOLL_DECO1,
		Inventory.PAPERDOLL_DECO2,
		Inventory.PAPERDOLL_DECO3,
		Inventory.PAPERDOLL_DECO4,
		Inventory.PAPERDOLL_DECO5,
		Inventory.PAPERDOLL_DECO6,
		Inventory.PAPERDOLL_BELT,
		Inventory.PAPERDOLL_BROOCH,
		Inventory.PAPERDOLL_BROOCH_JEWEL1,
		Inventory.PAPERDOLL_BROOCH_JEWEL2,
		Inventory.PAPERDOLL_BROOCH_JEWEL3,
		Inventory.PAPERDOLL_BROOCH_JEWEL4,
		Inventory.PAPERDOLL_BROOCH_JEWEL5,
		Inventory.PAPERDOLL_BROOCH_JEWEL6
	};
	
	private static final int[] PAPERDOLL_ORDER_VISUAL_ID = new int[]
	{
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_HAIR2,
	};
	
	@Override
	protected int[] getPaperdollOrder()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return PAPERDOLL_ORDER_IL;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				return PAPERDOLL_ORDER;
			case GC:
				return PAPERDOLL_ORDER_GC;
			case SL:
				return PAPERDOLL_ORDER_SL;
		}
		return PAPERDOLL_ORDER;
	}
	
	@Override
	public int[] getPaperdollOrderVisualId()
	{
		return PAPERDOLL_ORDER_VISUAL_ID;
	}
}
