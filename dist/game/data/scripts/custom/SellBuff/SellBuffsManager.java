/*
 * Copyright (C) 2004-2014 L2J Server
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
package custom.SellBuff;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import l2r.gameserver.util.HtmlUtil;
import l2r.gameserver.util.Util;
import l2r.util.data.xml.IXmlReader.IXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sell Buffs Manager
 * @author St3eT
 */
public final class SellBuffsManager implements IXmlReader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SellBuffsManager.class);
	private static final Set<Integer> ALLOWED_BUFFS = new HashSet<>();
	private static final Set<Integer> ALLOWED_ZONES = new HashSet<>();
	private static final String htmlFolder = "data/scripts/custom/SellBuff/";
	private static final String xmlFile = "data/scripts/custom/SellBuff/sellBuffData.xml";
	
	static boolean SELLBUFF_ENABLED = true;
	static int SELLBUFF_PAYMENT_ID = 57;
	static long SELLBUFF_MIN_PRICE = 100000;
	static long SELLBUFF_MAX_PRICE = 100000000;
	static double SELLBUFF_MP_MULTIPLER = 1;
	static int SELLBUFF_MAX_BUFFS = 15;
	static boolean SELLBUFF_FREE_FOR_SAME_IP = false;
	static boolean SELLBUFF_FREE_FOR_SAME_CLAN = false;
	static boolean SELLBUFF_FREE_FOR_FRIENDLIST = false;
	
	// Selling buffs system
	private final Set<Integer> _playersIsSellingBuffs;
	private final Map<Integer, List<SellBuffHolder>> _playersSellingBuffs;
	
	protected SellBuffsManager()
	{
		load();
		_playersSellingBuffs = new HashMap<>();
		_playersIsSellingBuffs = ConcurrentHashMap.newKeySet();
		if (SELLBUFF_ENABLED)
		{
			SellBuff.main(null); // Now load the handlers.
		}
	}
	
	@Override
	public void load()
	{
		ALLOWED_BUFFS.clear();
		ALLOWED_ZONES.clear();
		parseDatapackFile(xmlFile);
		LOGGER.info(": Loaded " + ALLOWED_BUFFS.size() + " allowed buffs. Available " + (ALLOWED_ZONES.isEmpty() ? " everywhere." : ("in " + ALLOWED_ZONES.size() + " zones.")));
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if ("list".equalsIgnoreCase(node.getNodeName()))
			{
				for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
				{
					if ("config".equalsIgnoreCase(list_node.getNodeName()))
					{
						final NamedNodeMap attrs = list_node.getAttributes();
						SELLBUFF_ENABLED = parseBoolean(attrs, "enabled", false);
						SELLBUFF_PAYMENT_ID = parseInteger(attrs, "payItemId", 57);
						SELLBUFF_MIN_PRICE = parseInteger(attrs, "minPrice", 100000);
						SELLBUFF_MAX_PRICE = parseInteger(attrs, "maxPrice", 100000000);
						SELLBUFF_MP_MULTIPLER = parseDouble(attrs, "mpMultiplier", 1.);
						SELLBUFF_MAX_BUFFS = parseInteger(attrs, "maxBuffs", 15);
						SELLBUFF_FREE_FOR_SAME_IP = parseBoolean(attrs, "freeForSameIp", false);
						SELLBUFF_FREE_FOR_SAME_CLAN = parseBoolean(attrs, "freeForSameClan", false);
						SELLBUFF_FREE_FOR_FRIENDLIST = parseBoolean(attrs, "freeForFriendlist", false);
					}
					else if ("skill".equalsIgnoreCase(list_node.getNodeName()))
					{
						final NamedNodeMap attrs = list_node.getAttributes();
						int skillId = parseInteger(attrs, "id", 0);
						
						if (skillId > 0)
						{
							ALLOWED_BUFFS.add(skillId);
						}
					}
					else if ("zone".equalsIgnoreCase(list_node.getNodeName()))
					{
						final NamedNodeMap attrs = list_node.getAttributes();
						int zoneId = parseInteger(attrs, "id", 0);
						
						if (zoneId > 0)
						{
							ALLOWED_ZONES.add(zoneId);
						}
					}
				}
			}
		}
	}
	
	public void sendSellMenu(L2PcInstance player)
	{
		final String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlFolder + (isSellingBuffs(player) ? "BuffMenu_already.html" : "BuffMenu.html"));
		Util.sendCBHtml(player, html);
	}
	
	public void sendBuffChoiceMenu(L2PcInstance player, int index)
	{
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlFolder + "BuffChoice.html");
		html = html.replace("%list%", buildSkillMenu(player, index));
		Util.sendCBHtml(player, html);
	}
	
	public void sendBuffEditMenu(L2PcInstance player)
	{
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlFolder + "BuffChoice.html");
		html = html.replace("%list%", buildEditMenu(player));
		Util.sendCBHtml(player, html);
	}
	
	public void sendBuffMenu(L2PcInstance player, L2PcInstance seller, int index)
	{
		if (!isSellingBuffs(seller) || getSellingBuffs(seller).isEmpty())
		{
			return;
		}
		
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), htmlFolder + "BuffBuyMenu.html");
		html = html.replace("%list%", buildBuffMenu(player, seller, index));
		Util.sendCBHtml(player, html);
	}
	
	public void startSellBuffs(L2PcInstance player, String title)
	{
		player.sitDown();
		setIsSellingBuffs(player, true);
		player.setPrivateStoreType(PrivateStoreType.PACKAGE_SELL);
		player.getSellList().setTitle(title);
		player.getSellList().setPackaged(true);
		player.broadcastUserInfo();
		player.broadcastPacket(new ExPrivateStoreSetWholeMsg(player));
		sendSellMenu(player);
	}
	
	public void stopSellBuffs(L2PcInstance player)
	{
		setIsSellingBuffs(player, false);
		player.setPrivateStoreType(PrivateStoreType.NONE);
		player.standUp();
		player.broadcastUserInfo();
		sendSellMenu(player);
	}
	
	private String buildBuffMenu(L2PcInstance player, L2PcInstance seller, int index)
	{
		final int ceiling = index + 10;
		int nextIndex = -1;
		int previousIndex = -1;
		int emptyFields = 0;
		final StringBuilder sb = new StringBuilder();
		final List<SellBuffHolder> sellList = new ArrayList<>();
		
		int count = 0;
		for (SellBuffHolder holder : getSellingBuffs(seller))
		{
			count++;
			if ((count > index) && (count <= ceiling))
			{
				sellList.add(holder);
			}
		}
		
		if (count > 10)
		{
			if (count > (index + 10))
			{
				nextIndex = index + 10;
			}
		}
		
		if (index >= 10)
		{
			previousIndex = index - 10;
		}
		
		emptyFields = ceiling - sellList.size();
		
		sb.append("<br>");
		sb.append(HtmlUtil.getMpGauge(250, (long) seller.getCurrentMp(), seller.getMaxMp(), false));
		sb.append("<br>");
		
		sb.append("<table border=0 cellpadding=0 cellspacing=0 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		sb.append("<tr><td><br><br><br></td></tr>");
		sb.append("<tr>");
		sb.append("<td fixwidth=\"10\"></td>");
		sb.append("<td> <button action=\"\" value=\"Icon\" width=75 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Icon
		sb.append("<td> <button action=\"\" value=\"Name\" width=175 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Name
		sb.append("<td> <button action=\"\" value=\"Level\" width=85 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Leve
		sb.append("<td> <button action=\"\" value=\"MP Cost\" width=100 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Price
		sb.append("<td> <button action=\"\" value=\"Price\" width=200 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Price
		sb.append("<td> <button action=\"\" value=\"Action\" width=100 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Action
		sb.append("<td fixwidth=\"20\"></td>");
		sb.append("</tr>");
		
		for (SellBuffHolder holder : sellList)
		{
			final L2Skill skill = seller.getKnownSkill(holder.getSkillId());
			if (skill == null)
			{
				emptyFields++;
				continue;
			}
			
			final L2Item item = ItemData.getInstance().getTemplate(SELLBUFF_PAYMENT_ID);
			
			sb.append("<tr>");
			sb.append("<td fixwidth=\"20\"></td>");
			sb.append("<td align=center><img src=\"" + getSkillIcon(skill) + "\" width=\"32\" height=\"32\"></td>");
			sb.append("<td align=left>" + skill.getName() + (skill.getLevel() > 100 ? "<font color=\"LEVEL\"> + " + (skill.getLevel() % 100) + "</font></td>" : "</td>"));
			sb.append("<td align=center>" + ((skill.getLevel() > 100) ? SkillData.getInstance().getMaxLevel(skill.getId()) : skill.getLevel()) + "</td>");
			sb.append("<td align=center> <font color=\"1E90FF\">" + (int) (skill.getMpConsume() * SELLBUFF_MP_MULTIPLER) + "</font></td>");
			sb.append("<td align=center> " + Util.formatAdena(holder.getPrice()) + " <font color=\"LEVEL\"> " + (item != null ? item.getName() : "") + "</font> </td>");
			sb.append("<td align=center fixwidth=\"50\"><button value=\"Buy Buff\" action=\"bypass -h sellbuffbuyskill " + seller.getObjectId() + " " + skill.getId() + " " + index + "\" width=\"85\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr>");
			sb.append("<tr><td><br><br></td></tr>");
		}
		
		for (int i = 0; i < emptyFields; i++)
		{
			sb.append("<tr>");
			sb.append("<td fixwidth=\"20\"></td>");
			sb.append("<td align=center></td>");
			sb.append("<td align=left></td>");
			sb.append("<td align=center></td>");
			sb.append("<td align=center></font></td>");
			sb.append("<td align=center></td>");
			sb.append("<td align=center fixwidth=\"50\"></td>");
			sb.append("</tr>");
			sb.append("<tr><td><br><br></td></tr>");
		}
		
		sb.append("</table>");
		
		sb.append("<table width=\"250\" border=\"0\">");
		sb.append("<tr>");
		
		if (previousIndex > -1)
		{
			sb.append("<td align=left><button value=\"Previous Page\" action=\"bypass -h sellbuffbuymenu " + seller.getObjectId() + " " + previousIndex + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		if (nextIndex > -1)
		{
			sb.append("<td align=right><button value=\"Next Page\" action=\"bypass -h sellbuffbuymenu " + seller.getObjectId() + " " + nextIndex + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	
	private String buildEditMenu(L2PcInstance player)
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<table border=0 cellpadding=0 cellspacing=0 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		sb.append("<tr><td><br><br><br></td></tr>");
		sb.append("<tr>");
		sb.append("<td fixwidth=\"10\"></td>");
		sb.append("<td> <button action=\"\" value=\"Icon\" width=75 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Icon
		sb.append("<td> <button action=\"\" value=\"Name\" width=150 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Name
		sb.append("<td> <button action=\"\" value=\"Level\" width=75 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Level
		sb.append("<td> <button action=\"\" value=\"Old Price\" width=100 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Old price
		sb.append("<td> <button action=\"\" value=\"New Price\" width=125 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // New price
		sb.append("<td> <button action=\"\" value=\"Action\" width=125 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Change Price
		sb.append("<td> <button action=\"\" value=\"Remove\" width=85 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Remove Buff
		sb.append("<td fixwidth=\"20\"></td>");
		sb.append("</tr>");
		
		if (getSellingBuffs(player).isEmpty())
		{
			sb.append("</table>");
			sb.append("<br><br><br>");
			sb.append("You don't have added any buffs yet!");
		}
		else
		{
			for (SellBuffHolder holder : getSellingBuffs(player))
			{
				final L2Skill skill = player.getKnownSkill(holder.getSkillId());
				if (skill == null)
				{
					continue;
				}
				
				sb.append("<tr>");
				sb.append("<td fixwidth=\"20\"></td>");
				sb.append("<td align=center><img src=\"" + getSkillIcon(skill) + "\" width=\"32\" height=\"32\"></td>"); // Icon
				sb.append("<td align=left>" + skill.getName() + (skill.getLevel() > 100 ? "<font color=\"LEVEL\"> + " + (skill.getLevel() % 100) + "</font></td>" : "</td>")); // Name + enchant
				sb.append("<td align=center>" + ((skill.getLevel() > 100) ? SkillData.getInstance().getMaxLevel(skill.getId()) : skill.getLevel()) + "</td>"); // Level
				sb.append("<td align=center> " + Util.formatAdena(holder.getPrice()) + " </td>"); // Price show
				sb.append("<td align=center><edit var=\"price_" + skill.getId() + "\" width=120 type=\"number\"></td>"); // Price edit
				sb.append("<td align=center><button value=\"Edit\" action=\"bypass -h sellbuffchangeprice " + skill.getId() + " $price_" + skill.getId() + "\" width=\"85\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				sb.append("<td align=center><button value=\" X \" action=\"bypass -h sellbuffremove " + skill.getId() + "\" width=\"26\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				sb.append("</tr>");
				sb.append("<tr><td><br><br></td></tr>");
			}
			sb.append("</table>");
		}
		
		return sb.toString();
	}
	
	private String buildSkillMenu(L2PcInstance player, int index)
	{
		final int ceiling = index + 10;
		int nextIndex = -1;
		int previousIndex = -1;
		final StringBuilder sb = new StringBuilder();
		final List<L2Skill> skillList = new ArrayList<>();
		
		int count = 0;
		for (L2Skill skill : player.getAllSkills())
		{
			if (ALLOWED_BUFFS.contains(skill.getId()) && !isInSellList(player, skill))
			{
				count++;
				
				if ((count > index) && (count <= ceiling))
				{
					skillList.add(skill);
				}
			}
		}
		
		if (count > 10)
		{
			if (count > (index + 10))
			{
				nextIndex = index + 10;
			}
		}
		
		if (index >= 10)
		{
			previousIndex = index - 10;
		}
		
		sb.append("<table border=0 cellpadding=0 cellspacing=0 background=\"L2UI_CH3.refinewnd_back_Pattern\">");
		sb.append("<tr><td><br><br><br></td></tr>");
		sb.append("<tr>");
		sb.append("<td fixwidth=\"10\"></td>");
		sb.append("<td> <button action=\"\" value=\"Icon\" width=100 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Icon
		sb.append("<td> <button action=\"\" value=\"Name\" width=175 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Name
		sb.append("<td> <button action=\"\" value=\"Level\" width=150 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Leve
		sb.append("<td> <button action=\"\" value=\"Price\" width=150 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Price
		sb.append("<td> <button action=\"\" value=\"Action\" width=125 height=23 back=\"L2UI_CT1.OlympiadWnd_DF_Watch_Down\" fore=\"L2UI_CT1.OlympiadWnd_DF_Watch\"> </td>"); // Action
		sb.append("<td fixwidth=\"20\"></td>");
		sb.append("</tr>");
		
		if (skillList.isEmpty())
		{
			sb.append("</table>");
			sb.append("<br><br><br>");
			sb.append("At this moment you cant add any buffs!");
		}
		else
		{
			for (L2Skill skill : skillList)
			{
				sb.append("<tr>");
				sb.append("<td fixwidth=\"20\"></td>");
				sb.append("<td align=center><img src=\"" + getSkillIcon(skill) + "\" width=\"32\" height=\"32\"></td>");
				sb.append("<td align=left>" + skill.getName() + (skill.getLevel() > 100 ? "<font color=\"LEVEL\"> + " + (skill.getLevel() % 100) + "</font></td>" : "</td>"));
				sb.append("<td align=center>" + ((skill.getLevel() > 100) ? SkillData.getInstance().getMaxLevel(skill.getId()) : skill.getLevel()) + "</td>");
				sb.append("<td align=center><edit var=\"price_" + skill.getId() + "\" width=120 type=\"number\"></td>");
				sb.append("<td align=center fixwidth=\"50\"><button value=\"Add Buff\" action=\"bypass -h sellbuffaddskill " + skill.getId() + " $price_" + skill.getId() + "\" width=\"85\" height=\"26\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				sb.append("</tr>");
				sb.append("<tr><td><br><br></td></tr>");
			}
			sb.append("</table>");
		}
		
		sb.append("<table width=\"250\" border=\"0\">");
		sb.append("<tr>");
		
		if (previousIndex > -1)
		{
			sb.append("<td align=left><button value=\"Previous Page\" action=\"bypass -h sellbuffadd " + previousIndex + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		
		if (nextIndex > -1)
		{
			sb.append("<td align=right><button value=\"Next Page\" action=\"bypass -h sellbuffadd " + nextIndex + "\" width=\"100\" height=\"30\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}
	
	public boolean isInSellList(L2PcInstance player, L2Skill skill)
	{
		return getSellingBuffs(player).stream().filter(h -> (h.getSkillId() == skill.getId())).findFirst().orElse(null) != null;
	}
	
	public boolean isSellingBuffs(L2PcInstance player)
	{
		return _playersIsSellingBuffs.contains(player.getObjectId());
	}
	
	public void setIsSellingBuffs(L2PcInstance player, boolean val)
	{
		if (val)
		{
			_playersIsSellingBuffs.add(player.getObjectId());
		}
		else
		{
			_playersIsSellingBuffs.remove(player.getObjectId());
		}
	}
	
	public List<SellBuffHolder> getSellingBuffs(L2PcInstance player)
	{
		return _playersSellingBuffs.computeIfAbsent(player.getObjectId(), k -> new ArrayList<>());
	}
	
	public boolean canStartSellBuffs(L2PcInstance player)
	{
		if (player.isAlikeDead())
		{
			player.sendMessage("You can't sell buffs in fake death!");
			return false;
		}
		else if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("You can't sell buffs with Olympiad status!");
			return false;
		}
		else if (player.isCursedWeaponEquipped() || (player.getKarma() > 0))
		{
			player.sendMessage("You can't sell buffs in Chaotic state!");
			return false;
		}
		else if (player.isInDuel())
		{
			player.sendMessage("You can't sell buffs in Duel state!");
			return false;
		}
		else if (player.getFishingEx().isFishing())
		{
			player.sendMessage("You can't sell buffs while fishing.");
			return false;
		}
		else if (player.isMounted() || player.isFlyingMounted() || player.isFlying())
		{
			player.sendMessage("You can't sell buffs in Mounth state!");
			return false;
		}
		else if (player.isTransformed())
		{
			player.sendMessage("You can't sell buffs in Transform state!");
			return false;
		}
		else if (!ALLOWED_ZONES.isEmpty() && ZoneManager.getInstance().getZones(player).stream().mapToInt(L2ZoneType::getId).noneMatch(ALLOWED_ZONES::contains))
		{
			player.sendMessage("You are not allowed to sell buffs here! Please go to the specified zones for selling buffs.");
			return false;
		}
		else if (player.isInsideZone(ZoneIdType.NO_STORE) || !player.isInsideZone(ZoneIdType.PEACE) || player.isJailed())
		{
			player.sendMessage("You can't sell buffs here!");
			return false;
		}
		return true;
	}
	
	static String getSkillIcon(L2Skill skill)
	{
		return skill.getId() > 999 ? ("Icon.skill" + skill.getId()) : ("Icon.skill0" + skill.getId());
	}
	
	/**
	 * Gets the single instance of {@code SellBuffsManager}.
	 * @return single instance of {@code SellBuffsManager}
	 */
	public static final SellBuffsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SellBuffsManager _instance = new SellBuffsManager();
	}
	
	public static void main(String[] args)
	{
		getInstance();
	}
}