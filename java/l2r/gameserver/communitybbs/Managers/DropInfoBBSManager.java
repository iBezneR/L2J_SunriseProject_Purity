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
package l2r.gameserver.communitybbs.Managers;

import gr.sr.imageGeneratorEngine.ImagesCache;
import l2r.Config;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.communitybbs.SunriseBoards.dropCalc.DropCalculatorConfigs;
import l2r.gameserver.communitybbs.SunriseBoards.dropCalc.DropInfoFunctions;
import l2r.gameserver.communitybbs.SunriseBoards.dropCalc.DropInfoHandler;
import l2r.gameserver.communitybbs.SunriseBoards.dropCalc.DropInfoHolder;
import l2r.gameserver.data.SpawnTable;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.RadarControl;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.util.Util;

import java.util.List;
import java.util.StringTokenizer;

/**
 * @author vGodFather
 */
public class DropInfoBBSManager extends BaseBBSManager
{
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		if (!DropCalculatorConfigs.ENABLE_DROP_CALCULATOR)
		{
			activeChar.sendMessage("Drop Calculator is disabled by admin.");
			return;
		}
		
		StringTokenizer st = new StringTokenizer(command, "_");
		st.nextToken();
		
		String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/DropCalculator/bbs_dropCalcMain.htm");
		if (html == null)
		{
			html = "<html><body><br><br><center>404 :File not found: 'data/html/CommunityBoard/DropCalculator/bbs_dropCalcMain.htm'</center></body></html>";
		}
		
		if (command.equals("_bbssearchdropCalc"))
		{
			showMainPage(activeChar);
			return;
		}
		else if (command.startsWith("_bbssearchdropItemsByName"))
		{
			if (!st.hasMoreTokens())
			{
				showMainPage(activeChar);
				return;
			}
			String itemName = st.nextToken().trim();
			int itemsPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			int sortMethod = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
			html = showDropItemsByNamePage(activeChar, itemName, itemsPage, sortMethod);
		}
		else if (command.startsWith("_bbssearchdropMonstersByItem"))
		{
			int itemId = Integer.parseInt(st.nextToken());
			int monstersPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			int sortMethod = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
			html = showDropMonstersByItem(activeChar, itemId, monstersPage, sortMethod);
		}
		else if (command.startsWith("_bbssearchdropMonsterDetailsByItem"))
		{
			int monsterId = Integer.parseInt(st.nextToken());
			html = showdropMonsterDetailsByItem(activeChar, monsterId);
			separateAndSend(html, activeChar);
			if (st.hasMoreTokens())
			{
				manageButton(activeChar, Integer.parseInt(st.nextToken()), monsterId);
			}
			return;
		}
		else if (command.startsWith("_bbssearchdropMonstersByName"))
		{
			if (!st.hasMoreTokens())
			{
				showMainPage(activeChar);
				return;
			}
			String monsterName = st.nextToken().trim();
			int monsterPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			int sortMethod = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
			html = showDropMonstersByName(activeChar, monsterName, monsterPage, sortMethod);
		}
		else if (command.startsWith("_bbssearchdropMonsterDetailsByName"))
		{
			int chosenMobId = Integer.parseInt(st.nextToken());
			html = showDropMonsterDetailsByName(activeChar, chosenMobId);
			separateAndSend(html, activeChar);
			if (st.hasMoreTokens())
			{
				manageButton(activeChar, Integer.parseInt(st.nextToken()), chosenMobId);
			}
			return;
		}
		else if (command.startsWith("_bbssearchNpcDropList"))
		{
			activeChar.setQuickVar("DCDropType", command.split("_")[2]);
			DropInfoFunctions.showNpcDropList(activeChar, command.split("_")[2], Integer.parseInt(command.split("_")[3]), Integer.parseInt(command.split("_")[4]));
			return;
		}
		else if (command.startsWith("_bbssearchShowSkills"))
		{
			DropInfoFunctions.showNpcSkillList(activeChar, Integer.parseInt(command.split("_")[2]), Integer.parseInt(command.split("_")[3]));
			return;
		}
		separateAndSend(html, activeChar);
	}
	
	private void showMainPage(L2PcInstance player)
	{
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/CommunityBoard/DropCalculator/bbs_dropCalcMain.htm");
		separateAndSend(html, player);
	}
	
	private String showDropMonstersByName(L2PcInstance player, String monsterName, int page, int sort)
	{
		player.setQuickVar("DCMonsterSort", sort);
		player.setQuickVar("DCMonsterName", monsterName);
		player.setQuickVar("DCMonstersPage", page);
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/CommunityBoard/DropCalculator/bbs_dropMonstersByName.htm");
		return replaceMonstersByName(html, monsterName, page, sort);
	}
	
	private static String replaceMonstersByName(String html, String monsterName, int page, int sort)
	{
		String newHtml = html;
		
		List<L2NpcTemplate> npcTemplates = DropInfoFunctions.getNpcsContainingString(monsterName);
		npcTemplates = DropInfoFunctions.sortMonsters(npcTemplates, sort);
		
		int npcIndex = 0;
		
		for (int i = 0; i < 12; i++)
		{
			npcIndex = i + ((page - 1) * 12);
			L2NpcTemplate npc = npcTemplates.size() > npcIndex ? npcTemplates.get(npcIndex) : null;
			
			newHtml = newHtml.replace("<?name_" + i + "?>", npc != null ? DropInfoFunctions.getName(npc.getName()) : "...");
			newHtml = newHtml.replace("<?drop_" + i + "?>", npc != null ? String.valueOf(DropInfoFunctions.getDropsCount(npc, false)) : "...");
			newHtml = newHtml.replace("<?spoil_" + i + "?>", npc != null ? String.valueOf(DropInfoFunctions.getDropsCount(npc, true)) : "...");
			newHtml = newHtml.replace("<?bp_" + i + "?>", npc != null ? "<button value=\"show\" action=\"bypass _bbssearchdropMonsterDetailsByName_" + npc.getId() + "\" width=40 height=12 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></button>" : "...");
		}
		
		newHtml = newHtml.replace("<?previous?>", page > 1 ? "<button action=\"bypass _bbssearchdropMonstersByName_" + monsterName + "_" + (page - 1) + "_" + sort + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\"></button>" : "<br>");
		newHtml = newHtml.replace("<?next?>", npcTemplates.size() > (npcIndex + 1) ? "<button action=\"bypass _bbssearchdropMonstersByName_" + monsterName + "_" + (page + 1) + "_" + sort + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\"></button>" : "<br>");
		
		newHtml = newHtml.replace("<?search?>", monsterName);
		newHtml = newHtml.replace("<?size?>", Util.formatAdena(npcTemplates.size()));
		newHtml = newHtml.replace("<?page?>", String.valueOf(page));
		newHtml = newHtml.replace("<?monsterName?>", sort == 0 ? "<font color=\"bbbbbb\">Monster Name</font>" : "<a action=\"bypass _bbssearchdropMonstersByName_" + monsterName + "_" + page + "_" + 0 + "\"><font color=\"bbbbbb\">Monster Name</font></a>");
		newHtml = newHtml.replace("<?droppingItems?>", sort == 1 ? "<font color=\"bbbbbb\">Dropping Items</font>" : "<a action=\"bypass _bbssearchdropMonstersByName_" + monsterName + "_" + page + "_" + 1 + "\"><font color=\"bbbbbb\">Dropping Items</font></a>");
		newHtml = newHtml.replace("<?spoilingItems?>", sort == 2 ? "<font color=\"bbbbbb\">Spoiling Items</font>" : "<a action=\"bypass _bbssearchdropMonstersByName_" + monsterName + "_" + page + "_" + 2 + "\"><font color=\"bbbbbb\">Spoiling Items</font></a>");
		
		return newHtml;
	}
	
	private String showDropItemsByNamePage(L2PcInstance player, String itemName, int page, int sort)
	{
		player.setQuickVar("DCItemSort", sort);
		player.setQuickVar("DCItemName", itemName);
		player.setQuickVar("DCItemsPage", page);
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/CommunityBoard/DropCalculator/bbs_dropItemsByName.htm");
		return replaceItemsByNamePage(html, itemName, page, sort);
	}
	
	private String replaceItemsByNamePage(String html, String itemName, int page, int sort)
	{
		String newHtml = html;
		
		List<L2Item> itemsByName = DropInfoFunctions.getItemsByNameContainingString(itemName);
		itemsByName = DropInfoFunctions.sortItems(itemsByName, sort);
		
		int itemIndex = 0;
		
		for (int i = 0; i < 12; i++)
		{
			itemIndex = i + ((page - 1) * 12);
			L2Item item = itemsByName.size() > itemIndex ? itemsByName.get(itemIndex) : null;
			
			newHtml = newHtml.replace("<?name_" + i + "?>", item != null ? DropInfoFunctions.getName(item.getName()) : "...");
			newHtml = newHtml.replace("<?drop_" + i + "?>", item != null ? String.valueOf(DropInfoFunctions.getDroplistsCountByItemId(item.getId(), false)) : "...");
			newHtml = newHtml.replace("<?spoil_" + i + "?>", item != null ? String.valueOf(DropInfoFunctions.getDroplistsCountByItemId(item.getId(), true)) : "...");
			newHtml = newHtml.replace("<?bp_" + i + "?>", item != null ? "<button value=\"show\" action=\"bypass _bbssearchdropMonstersByItem_" + item.getId() + "_1\" width=40 height=12 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\">" : "...");
		}
		
		newHtml = newHtml.replace("<?previous?>", page > 1 ? "<button action=\"bypass _bbssearchdropItemsByName_" + itemName + "_" + (page - 1) + "_" + sort + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\">" : "<br>");
		newHtml = newHtml.replace("<?next?>", itemsByName.size() > (itemIndex + 1) ? "<button action=\"bypass _bbssearchdropItemsByName_" + itemName + "_" + (page + 1) + "_" + sort + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\">" : "<br>");
		
		newHtml = newHtml.replace("<?search?>", itemName);
		newHtml = newHtml.replace("<?size?>", Util.formatAdena(itemsByName.size()));
		newHtml = newHtml.replace("<?page?>", String.valueOf(page));
		newHtml = newHtml.replace("<?itemName?>", sort == 0 ? "<font color=\"bbbbbb\">Name</font>" : "<a action=\"bypass _bbssearchdropItemsByName_" + itemName + "_" + page + "_" + 0 + "\"><font color=\"bbbbbb\">Name</font></a>");
		newHtml = newHtml.replace("<?dropLists?>", sort == 1 ? "<font color=\"bbbbbb\">Number of Drop Lists</font>" : "<a action=\"bypass _bbssearchdropItemsByName_" + itemName + "_" + page + "_" + 1 + "\"><font color=\"bbbbbb\">Number of Drop Lists</font></a>");
		newHtml = newHtml.replace("<?spoilLists?>", sort == 2 ? "<font color=\"bbbbbb\">Number of Spoil Lists</font>" : "<a action=\"bypass _bbssearchdropItemsByName_" + itemName + "_" + page + "_" + 2 + "\"><font color=\"bbbbbb\">Number of Spoil Lists</font></a>");
		return newHtml;
	}
	
	private String showDropMonstersByItem(L2PcInstance player, int itemId, int page, int sort)
	{
		player.setQuickVar("DCMonster2Sort", sort);
		player.setQuickVar("DCItemId", itemId);
		player.setQuickVar("DCMonstersPage", page);
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/CommunityBoard/DropCalculator/bbs_dropMonstersByItem.htm");
		return replaceMonstersByItemPage(player, html, itemId, page, sort);
	}
	
	private String replaceMonstersByItemPage(L2PcInstance player, String html, int itemId, int page, int sort)
	{
		String newHtml = html;
		
		List<DropInfoHolder> templates = DropInfoHandler.getInstance().getDrop(itemId);
		templates = DropInfoFunctions.sortMonsters2(templates, sort);
		
		int npcIndex = 0;
		
		for (int i = 0; i < 12; i++)
		{
			npcIndex = i + ((page - 1) * 12);
			DropInfoHolder drops = templates.size() > npcIndex ? templates.get(npcIndex) : null;
			L2NpcTemplate npc = templates.size() > npcIndex ? NpcTable.getInstance().getTemplate(templates.get(npcIndex).getNpcId()) : null;
			
			newHtml = newHtml.replace("<?name_" + i + "?>", npc != null ? DropInfoFunctions.getName(npc.getName()) : "...");
			newHtml = newHtml.replace("<?level_" + i + "?>", npc != null ? String.valueOf(npc.getLevel()) : "...");
			newHtml = newHtml.replace("<?type_" + i + "?>", (npc != null) && (drops != null) ? drops.isSweep() ? "Spoil" : "Drop" : "...");
			newHtml = newHtml.replace("<?count_" + i + "?>", (npc != null) && (drops != null) ? DropInfoFunctions.getMinMaxDropCounts(npc, itemId, drops.isSweep()) : "...");
			newHtml = newHtml.replace("<?chance_" + i + "?>", (npc != null) && (drops != null) ? DropInfoFunctions.getDropChance(npc, itemId, drops.isSweep()) : "...");
			newHtml = newHtml.replace("<?bp_" + i + "?>", npc != null ? "<button value=\"show\" action=\"bypass _bbssearchdropMonsterDetailsByItem_" + npc.getId() + "\" width=40 height=12 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\">" : "...");
		}
		
		newHtml = newHtml.replace("<?previous?>", page > 1 ? "<button action=\"bypass _bbssearchdropMonstersByItem_" + itemId + "_" + (page - 1) + "_" + sort + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\">" : "<br>");
		newHtml = newHtml.replace("<?next?>", templates.size() > (npcIndex + 1) ? "<button action=\"bypass _bbssearchdropMonstersByItem_" + itemId + "_" + (page + 1) + "_" + sort + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\">" : "<br>");
		
		newHtml = newHtml.replace("<?search?>", player.getQuickVar("DCItemName", ItemData.getInstance().getTemplate(itemId).getName()));
		newHtml = newHtml.replace("<?item?>", ItemData.getInstance().getTemplate(itemId).getName());
		newHtml = newHtml.replace("<?size?>", Util.formatAdena(templates.size()));
		newHtml = newHtml.replace("<?back?>", String.valueOf(player.getQuickVarI("DCItemsPage", 1)));
		newHtml = newHtml.replace("<?page?>", String.valueOf(page));
		
		newHtml = newHtml.replace("<?monsterName?>", sort == 0 ? "<font color=\"bbbbbb\">Name</font>" : "<a action=\"bypass _bbssearchdropMonstersByItem_" + itemId + "_" + page + "_" + 0 + "\"><font color=\"bbbbbb\">Name</font></a>");
		newHtml = newHtml.replace("<?level?>", sort == 1 ? "<font color=\"bbbbbb\">Level</font>" : "<a action=\"bypass _bbssearchdropMonstersByItem_" + itemId + "_" + page + "_" + 1 + "\"><font color=\"bbbbbb\">Level</font></a>");
		newHtml = newHtml.replace("<?chance?>", sort == 2 ? "<font color=\"bbbbbb\">Chance</font>" : "<a action=\"bypass _bbssearchdropMonstersByItem_" + itemId + "_" + page + "_" + 2 + "\"><font color=\"bbbbbb\">Chance</font></a>");
		newHtml = newHtml.replace("<?type?>", sort == 3 ? "<font color=\"bbbbbb\">Type</font>" : "<a action=\"bypass _bbssearchdropMonstersByItem_" + itemId + "_" + page + "_" + 3 + "\"><font color=\"bbbbbb\">Type</font></a>");
		newHtml = newHtml.replace("<?count?>", sort == 4 ? "<font color=\"bbbbbb\">Count [Min...Max]</font>" : "<a action=\"bypass _bbssearchdropMonstersByItem_" + itemId + "_" + page + "_" + 4 + "\"><font color=\"bbbbbb\">Count [Min...Max]</font></a>");
		
		newHtml = newHtml.replace("<?sort?>", String.valueOf(player.getQuickVarI("DCItemSort", 0)));
		return newHtml;
	}
	
	private String showdropMonsterDetailsByItem(L2PcInstance player, int monsterId)
	{
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/CommunityBoard/DropCalculator/bbs_dropMonsterDetailsByItem.htm");
		return replaceMonsterDetails(player, html, monsterId);
	}
	
	private String showDropMonsterDetailsByName(L2PcInstance player, int monsterId)
	{
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/CommunityBoard/DropCalculator/bbs_dropMonsterDetailsByName.htm");
		return replaceMonsterDetails(player, html, monsterId);
	}
	
	private String replaceMonsterDetails(L2PcInstance player, String html, int monsterId)
	{
		String newHtml = html;
		
		int itemId = player.getQuickVarI("DCItemId", -1);
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(monsterId);
		L2Item item = itemId > -1 ? ItemData.getInstance().getTemplate(itemId) : null;
		
		newHtml = newHtml.replace("<?name?>", String.valueOf(player.getQuickVar("DCMonsterName")));
		newHtml = newHtml.replace("<?monster_name?>", template.getName());
		newHtml = newHtml.replace("<?item?>", item != null ? item.getName() : "...");
		newHtml = newHtml.replace("<?item_id?>", item != null ? String.valueOf(item.getId()) : "...");
		newHtml = newHtml.replace("<?back?>", String.valueOf(player.getQuickVarI("DCMonstersPage")));
		newHtml = newHtml.replace("<?monster?>", String.valueOf(monsterId));
		newHtml = newHtml.replace("<?level?>", String.valueOf(template.getLevel()));
		newHtml = newHtml.replace("<?aggro?>", template.isAggressive() ? "TRUE" : "FALSE");
		
		newHtml = newHtml.replace("<?hp?>", Util.formatAdena((int) template.getBaseHpMax()));
		newHtml = newHtml.replace("<?mp?>", Util.formatAdena((int) template.getBaseMpMax()));
		
		newHtml = newHtml.replace("<?drop?>", item != null ? DropInfoFunctions.getDropChance(template, item.getId(), false) : "...");
		newHtml = newHtml.replace("<?spoil?>", item != null ? DropInfoFunctions.getDropChance(template, item.getId(), true) : "...");
		
		newHtml = newHtml.replace("<?droping?>", String.valueOf(DropInfoFunctions.getDropsCount(template, false)));
		newHtml = newHtml.replace("<?spoiling?>", String.valueOf(DropInfoFunctions.getDropsCount(template, true)));
		
		newHtml = newHtml.replace("<?sort?>", String.valueOf(player.getQuickVarI("DCMonsterSort")));
		newHtml = newHtml.replace("<?sort2?>", String.valueOf(player.getQuickVarI("DCMonster2Sort")));
		
		newHtml = newHtml.replace("<?image?>", "Crest.crest_" + String.valueOf(Config.SERVER_ID) + "_" + String.valueOf(monsterId));
		
		ImagesCache.getInstance().sendImageToPlayer(player, monsterId);
		return newHtml;
	}
	
	private void manageButton(L2PcInstance player, int buttonId, int monsterId)
	{
		switch (buttonId)
		{
			case 1:
				player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
				break;
			case 2:// Show Drops
				DropInfoFunctions.showNpcDropList(player, "DEATH", monsterId, 1);
				break;
			case 3:// Teleport To Monster
				if (DropCalculatorConfigs.ENABLE_TELEPORT_FUNCTION)
				{
					if (DropCalculatorConfigs.ALLOW_TELEPORT_FROM_PEACE_ZONE_ONLY && !player.isInsideZone(ZoneIdType.PEACE))
					{
						player.sendMessage("Teleport is only allowed from peace zones only.");
						return;
					}
					
					L2Npc aliveInstance = DropInfoFunctions.getAliveNpc(monsterId);
					if ((aliveInstance != null) && !DropCalculatorConfigs.RESTRICTED_TELEPORT_IDS.contains(aliveInstance.getId()))
					{
						if (!DropCalculatorConfigs.ALLOW_FREE_TELEPORT && !player.destroyItemByItemId("DropCalc", DropCalculatorConfigs.TELEPORT_PRICE[0], DropCalculatorConfigs.TELEPORT_PRICE[1], player, true))
						{
							player.sendMessage("Incorrect item count.");
							return;
						}
						
						player.teleToLocation(aliveInstance.getLocation());
					}
					else
					{
						player.sendMessage("Monster isn't alive or teleport is not allowed.");
					}
				}
				else
				{
					player.sendMessage("Teleport function is disabled.");
				}
				break;
			case 4:// Show Monster on Map
				player.sendPacket(new CreatureSay(1, Say2.PARTYROOM_COMMANDER, "Info", "Open Map to see Locations"));
				
				for (L2Spawn loc : SpawnTable.getInstance().getSpawns(monsterId))
				{
					player.sendPacket(new RadarControl(0, 1, loc.getX(), loc.getY(), loc.getZ()));
				}
				break;
			case 5: // Npc stats
				DropInfoFunctions.showStats(player, monsterId);
				break;
		}
	}
	
	@Override
	protected void separateAndSend(String html, L2PcInstance acha)
	{
		html = html.replace("\t", "");
		if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (8180 * 2))
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			acha.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (8180 * 3))
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			acha.sendPacket(new ShowBoard(html.substring(8180, 8180 * 2), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8180 * 2, html.length()), "103"));
		}
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	
	}
	
	public static DropInfoBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DropInfoBBSManager _instance = new DropInfoBBSManager();
	}
}
