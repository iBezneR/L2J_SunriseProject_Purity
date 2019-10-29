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
package l2r.gameserver.communitybbs.SunriseBoards.dropCalc;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.drops.DropListScope;
import l2r.gameserver.model.drops.GeneralDropItem;
import l2r.gameserver.model.drops.GroupedGeneralDropItem;
import l2r.gameserver.model.drops.IDropItem;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.stats.MoveType;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.util.Util;

import java.io.Serializable;
import java.util.*;

/**
 * @author vGodFather
 */
public class DropInfoFunctions
{
	public static String getMinMaxDropCounts(L2NpcTemplate monster, int itemId, boolean drop)
	{
		long min = getDropMinMaxCounts(monster, itemId, drop, true);
		long max = getDropMinMaxCounts(monster, itemId, drop, false);
		String formattedCounts = "[" + min + "..." + max + ']';
		return formattedCounts;
	}
	
	private static long getDropMinMaxCounts(L2NpcTemplate template, int itemId, boolean isSpoil, boolean min)
	{
		long chance = 0;
		for (DropInfoHolder drop : DropInfoHandler.getInstance().getDrop(itemId))
		{
			if (drop.getNpcId() == template.getId())
			{
				if (drop.isSweep() && isSpoil)
				{
					if (min)
					{
						return drop.getMin();
					}
					return drop.getMax();
				}
				else if (!drop.isSweep() && !isSpoil)
				{
					if (min)
					{
						return drop.getMin();
					}
					return drop.getMax();
				}
			}
		}
		return chance;
	}
	
	public static String getDropChance(L2NpcTemplate template, int itemId, boolean isSpoil)
	{
		double chance = 0.0;
		for (DropInfoHolder drop : DropInfoHandler.getInstance().getDrop(itemId))
		{
			if (drop.getNpcId() == template.getId())
			{
				if (drop.isSweep() && isSpoil)
				{
					return Util.formatDouble(drop.getChance(), "#.##");
				}
				else if (!drop.isSweep() && !isSpoil)
				{
					return Util.formatDouble(drop.getChance(), "#.##");
				}
			}
		}
		return Util.formatDouble(chance, "#.##");
	}
	
	public static int getDropsCount(L2NpcTemplate template, boolean spoil)
	{
		int dropCounts = 0;
		for (ArrayList<DropInfoHolder> drop : DropInfoHandler.getInstance().getInfo().values())
		{
			for (int i = 0; i < drop.size(); i++)
			{
				DropInfoHolder itemDrop = drop.get(i);
				if (itemDrop.getNpcId() == template.getId())
				{
					if (itemDrop.isSweep() && spoil)
					{
						dropCounts++;
					}
					else if (!itemDrop.isSweep() && !spoil)
					{
						dropCounts++;
					}
				}
			}
		}
		return dropCounts;
	}
	
	public static void showNpcDropList(L2PcInstance activeChar, String dropType, int npcId, int page)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Unknown npc template id " + npcId);
			return;
		}
		
		final StringBuilder replyMSG = new StringBuilder(2900);
		replyMSG.append("<html><title>Show droplist page ");
		replyMSG.append(page);
		replyMSG.append("</title><body><br1><center><font color=\"LEVEL\">");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcId);
		replyMSG.append(")<br>");
		replyMSG.append(dropType.equals("CORPSE") ? "<button value=. value=\"Show Drops\" action=\"bypass -h _bbssearchNpcDropList_DEATH_" + npcId + "_" + 1 + "\" width=280 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>" : "<td width=120><button value=\"Show Spoils\" action=\"bypass -h _bbssearchNpcDropList_CORPSE_" + npcId + "_" + 1 + "\" width=280 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		
		int myPage = 1;
		int i = 0;
		int shown = 0;
		boolean hasMore = false;
		if (npcData.getDropLists() != null)
		{
			GeneralDropItem generalDrop;
			GroupedGeneralDropItem groupDrop;
			List<IDropItem> cat = npcData.getDropList(DropListScope.valueOf(dropType));
			int category = 0;
			if (cat != null)
			{
				for (int list = 0; list < cat.size(); list++)
				{
					IDropItem drop = cat.get(list);
					if (shown == 10)
					{
						hasMore = true;
						break;
					}
					
					if (drop instanceof GeneralDropItem)
					{
						generalDrop = (GeneralDropItem) drop;
						if (DropInfoHandler.HERBS.contains(generalDrop.getItemId()))
						{
							continue;
						}
						
						if (myPage != page)
						{
							i++;
							if (i == 10)
							{
								myPage++;
								i = 0;
							}
							continue;
						}
						if (shown == 10)
						{
							hasMore = true;
							break;
						}
						
						final L2Item item = ItemData.getInstance().getTemplate(generalDrop.getItemId());
						replyMSG.append("<br><center><img src=\"l2ui.squaregray\" width=\"280\" height=\"2\"></center>");
						replyMSG.append("<center><table border=0 cellpadding=0 cellspacing=0 width=\"280\" height=\"40\" bgcolor=\"2E2E2E\">");
						replyMSG.append("<tr>");
						replyMSG.append("<td FIXWIDTH=32>");
						replyMSG.append("<table");
						replyMSG.append("<tr>");
						replyMSG.append("<td width=32 align=right valign=top>");
						replyMSG.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"" + item.getIcon() + "\">");
						replyMSG.append("<tr>");
						replyMSG.append("<td width=32 height=32 align=center valign=top>");
						replyMSG.append("<button value=. action=\"bypass -h _bbssearchdropMonstersByItem_" + item.getId() + "_" + 1 + "\" width=32 height=32 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\">");
						replyMSG.append("</td>");
						replyMSG.append("</tr>");
						replyMSG.append("</table>");
						replyMSG.append("</td>");
						replyMSG.append("</tr>");
						replyMSG.append("</table>");
						replyMSG.append("</td>");
						replyMSG.append("<td FIXWIDTH=200 align=center valign=top>");
						replyMSG.append("<font color=\"F4FA58\" name=\"hs9\">" + getNameLong(item.getName()) + "</font>");
						replyMSG.append("<br1><font color=\"5882FA\">Chances " + (dropType.equals("DEATH") ? "Drop" : "Spoil") + ": " + Util.formatDouble(generalDrop.getChance(), "#.##") + "% Group: N/A ID: N/A");
						replyMSG.append("</td>");
						replyMSG.append("</tr>");
						replyMSG.append("</table>");
						replyMSG.append("</center>");
						shown++;
					}
					else
					{
						category++;
						groupDrop = (GroupedGeneralDropItem) drop;
						for (GeneralDropItem items : groupDrop.getItems())
						{
							if (DropInfoHandler.HERBS.contains(items.getItemId()))
							{
								continue;
							}
							
							if (myPage != page)
							{
								i++;
								if (i == 10)
								{
									myPage++;
									i = 0;
								}
								continue;
							}
							if (shown == 10)
							{
								hasMore = true;
								break;
							}
							
							final L2Item item = ItemData.getInstance().getTemplate(items.getItemId());
							replyMSG.append("<br><center><img src=\"l2ui.squaregray\" width=\"280\" height=\"2\"></center>");
							replyMSG.append("<center><table border=0 cellpadding=0 cellspacing=0 width=\"280\" height=\"40\" bgcolor=\"2E2E2E\">");
							replyMSG.append("<tr>");
							replyMSG.append("<td FIXWIDTH=32>");
							replyMSG.append("<table");
							replyMSG.append("<tr>");
							replyMSG.append("<td width=32 align=right valign=top>");
							replyMSG.append("<table border=0 cellspacing=0 cellpadding=0 width=32 height=32 background=\"" + item.getIcon() + "\">");
							replyMSG.append("<tr>");
							replyMSG.append("<td width=32 height=32 align=center valign=top>");
							replyMSG.append("<button value=. action=\"bypass -h _bbssearchdropMonstersByItem_" + item.getId() + "_" + 1 + "\" width=32 height=32 back=\"L2UI_CT1.ItemWindow_DF_Frame_Down\" fore=\"L2UI_CT1.ItemWindow_DF_Frame\">");
							replyMSG.append("</td>");
							replyMSG.append("</tr>");
							replyMSG.append("</table>");
							replyMSG.append("</td>");
							replyMSG.append("</tr>");
							replyMSG.append("</table>");
							replyMSG.append("</td>");
							replyMSG.append("<td FIXWIDTH=200 align=center valign=top>");
							replyMSG.append("<font color=\"F4FA58\" name=\"hs9\">" + getNameLong(item.getName()) + "</font>");
							replyMSG.append("<br1><font color=\"5882FA\">Chances " + (dropType.equals("DEATH") ? "Drop" : "Spoil") + ": " + Util.formatDouble(items.getChance(), "#.##") + "% Group: " + Util.formatDouble(groupDrop.getChance(), "#.##") + "% ID: " + category);
							replyMSG.append("</td>");
							replyMSG.append("</tr>");
							replyMSG.append("</table>");
							replyMSG.append("</center>");
							shown++;
						}
					}
				}
			}
		}
		
		replyMSG.append("<br><table width=280 bgcolor=666666 border=0><tr>");
		
		if (page > 1)
		{
			replyMSG.append("<td width=120><button action=\"bypass -h _bbssearchNpcDropList_" + activeChar.getQuickVar("DCDropType", "DEATH") + "_" + npcId + "_" + (page - 1) + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\"></td>");
			
			if (!hasMore)
			{
				replyMSG.append("<td width=100 align=center>Page ");
				replyMSG.append(page);
				replyMSG.append("</td><td width=70></td></tr>");
			}
		}
		if (hasMore)
		{
			if (page <= 1)
			{
				replyMSG.append("<td width=120></td>");
			}
			
			replyMSG.append("<td width=100 align=center>Page ");
			replyMSG.append(page);
			replyMSG.append("</td>");
			
			replyMSG.append("<td width=120 align=right><button action=\"bypass -h _bbssearchNpcDropList_" + activeChar.getQuickVar("DCDropType", "DEATH") + "_" + npcId + "_" + (page + 1) + "\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\"></td></tr>");
		}
		replyMSG.append("</table>");
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public static void showNpcSkillList(L2PcInstance activeChar, int npcId, int page)
	{
		L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Template id unknown: " + npcId);
			return;
		}
		
		Map<Integer, L2Skill> skills = new HashMap<>(npcData.getSkills());
		int _skillsize = skills.size();
		
		int MaxSkillsPerPage = 20;
		int MaxPages = _skillsize / MaxSkillsPerPage;
		if (_skillsize > (MaxSkillsPerPage * MaxPages))
		{
			MaxPages++;
		}
		
		if (page > MaxPages)
		{
			page = MaxPages;
		}
		
		int SkillsStart = MaxSkillsPerPage * page;
		int SkillsEnd = _skillsize;
		if ((SkillsEnd - SkillsStart) > MaxSkillsPerPage)
		{
			SkillsEnd = SkillsStart + MaxSkillsPerPage;
		}
		
		StringBuffer replyMSG = new StringBuffer("<html><title>NPC Skill List</title><body><center><font color=\"LEVEL\">");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcData.getId());
		replyMSG.append("): ");
		replyMSG.append(_skillsize);
		replyMSG.append(" skills</font></center><table width=300 bgcolor=666666><tr>");
		
		for (int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			if (page == x)
			{
				replyMSG.append("<td>Page ");
				replyMSG.append(pagenr);
				replyMSG.append("</td>");
			}
			else
			{
				replyMSG.append("<td><a action=\"bypass -h _bbssearchShowSkills_");
				replyMSG.append(npcData.getId());
				replyMSG.append("_");
				replyMSG.append(x);
				replyMSG.append("\"> Page ");
				replyMSG.append(pagenr);
				replyMSG.append(" </a></td>");
			}
		}
		
		replyMSG.append("</tr></table><br><table width=\"100%\" border=0><tr><td>Skill name [skill id-skill lvl]</td></tr>");
		Iterator<L2Skill> skillite = skills.values().iterator();
		
		for (int i = 0; i < SkillsStart; i++)
		{
			if (skillite.hasNext())
			{
				skillite.next();
			}
		}
		
		int cnt = SkillsStart;
		L2Skill sk;
		while (skillite.hasNext())
		{
			cnt++;
			if (cnt > SkillsEnd)
			{
				break;
			}
			
			sk = skillite.next();
			replyMSG.append("<tr><td width=240>");
			if (sk.getSkillType() == L2SkillType.NOTDONE)
			{
				replyMSG.append("<font color=\"777777\">" + sk.getName() + "</font>");
			}
			else
			{
				replyMSG.append(sk.getName());
			}
			replyMSG.append(" [");
			replyMSG.append(sk.getId());
			replyMSG.append("-");
			replyMSG.append(sk.getLevel());
			replyMSG.append("]</td></tr>");
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage();
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public static void showStats(L2PcInstance activeChar, int npcId)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		String html1 = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/DropCalculator/bbs_mobStats.htm");
		L2NpcTemplate target = NpcTable.getInstance().getTemplate(npcId);
		
		if (target != null)
		{
			// Combat Stats
			html1 = html1.replace("%patk%", Util.formatDouble(target.getBasePAtk(), "#.##"));
			html1 = html1.replace("%matk%", Util.formatDouble(target.getBaseMAtk(), "#.##"));
			html1 = html1.replace("%pdef%", Util.formatDouble(target.getBasePDef(), "#.##"));
			html1 = html1.replace("%mdef%", Util.formatDouble(target.getBaseMDef(), "#.##"));
			html1 = html1.replace("%accu%", "N/A");
			html1 = html1.replace("%evas%", "N/A");
			html1 = html1.replace("%crit%", Util.formatDouble(target.getBaseCritRate(), "#.##"));
			html1 = html1.replace("%rspd%", Util.formatDouble(target.getBaseMoveSpeed(MoveType.RUN), "#.##"));
			html1 = html1.replace("%aspd%", Util.formatDouble(target.getBasePAtkSpd(), "#.##"));
			html1 = html1.replace("%cspd%", Util.formatDouble(target.getBaseMAtkSpd(), "#.##"));
			
			// Basic Stats
			html1 = html1.replace("%str%", Util.formatDouble(target.getBaseSTR(), "#.##"));
			html1 = html1.replace("%dex%", Util.formatDouble(target.getBaseDEX(), "#.##"));
			html1 = html1.replace("%con%", Util.formatDouble(target.getBaseCON(), "#.##"));
			html1 = html1.replace("%int%", Util.formatDouble(target.getBaseINT(), "#.##"));
			html1 = html1.replace("%wit%", Util.formatDouble(target.getBaseWIT(), "#.##"));
			html1 = html1.replace("%men%", Util.formatDouble(target.getBaseMEN(), "#.##"));
			
			// Elements Stats
			html1 = html1.replace("%ele_atk%", "N/A");
			html1 = html1.replace("%ele_atk_value%", "N/A");
			html1 = html1.replace("%ele_dfire%", Util.formatDouble(target.getBaseFireRes(), "#.##"));
			html1 = html1.replace("%ele_dwater%", Util.formatDouble(target.getBaseWaterRes(), "#.##"));
			html1 = html1.replace("%ele_dwind%", Util.formatDouble(target.getBaseWindRes(), "#.##"));
			html1 = html1.replace("%ele_dearth%", Util.formatDouble(target.getBaseEarthRes(), "#.##"));
			html1 = html1.replace("%ele_dholy%", Util.formatDouble(target.getBaseHolyRes(), "#.##"));
			html1 = html1.replace("%ele_ddark%", Util.formatDouble(target.getBaseDarkRes(), "#.##"));
		}
		
		html.setHtml(html1.toString());
		activeChar.sendPacket(html);
	}
	
	public static int getDroplistsCountByItemId(int itemId, boolean isSpoil)
	{
		int dropCounts = 0;
		for (DropInfoHolder drop : DropInfoHandler.getInstance().getDrop(itemId))
		{
			if (drop.isSweep() && isSpoil)
			{
				dropCounts++;
			}
			else if (!drop.isSweep() && !isSpoil)
			{
				dropCounts++;
			}
		}
		return dropCounts;
	}
	
	public static String getName(String name)
	{
		if (name.length() > 36)
		{
			return name.substring(0, 35) + "...";
		}
		return name;
	}
	
	public static String getNameLong(String name)
	{
		if (name.length() > 36)
		{
			return name.substring(0, 35) + "...";
		}
		return name;
	}
	
	public static L2Npc getAliveNpc(int npcId)
	{
		List<L2Npc> instances = L2World.getInstance().getAllByNpcId(npcId);
		return instances.isEmpty() ? null : instances.get(0);
	}
	
	public static List<L2Item> getItemsByNameContainingString(String itemName)
	{
		List<L2Item> itemsByName = new ArrayList<>();
		for (L2Item item : ItemData.getInstance().getAllTemItems())
		{
			if ((item != null) && item.getName().toLowerCase().contains(itemName.toLowerCase()) && (DropInfoHandler.getInstance().getDrop(item.getId()) != null))
			{
				itemsByName.add(item);
			}
		}
		return itemsByName;
	}
	
	public static List<L2NpcTemplate> getNpcsContainingString(String monsterName)
	{
		List<L2NpcTemplate> npcTemplates = new ArrayList<>();
		for (ArrayList<DropInfoHolder> npc : DropInfoHandler.getInstance().getInfo().values())
		{
			for (int i = 0; i < npc.size(); i++)
			{
				if (npc.get(i).getName().toLowerCase().contains(monsterName.toLowerCase()))
				{
					if (!npcTemplates.contains(NpcTable.getInstance().getTemplate(npc.get(i).getNpcId())))
					{
						npcTemplates.add(NpcTable.getInstance().getTemplate(npc.get(i).getNpcId()));
					}
				}
			}
		}
		return npcTemplates;
	}
	
	public static List<L2Item> sortItems(List<L2Item> itemsByName, int sort)
	{
		Collections.sort(itemsByName, new ItemComparator(sort));
		return itemsByName;
	}
	
	private static class ItemComparator implements Comparator<L2Item>, Serializable
	{
		private static final long serialVersionUID = -6389059445439769861L;
		private final int sort;
		
		protected ItemComparator(int sort)
		{
			this.sort = sort;
		}
		
		@Override
		public int compare(L2Item o1, L2Item o2)
		{
			switch (sort)
			{
				case 0: // By name
					return o1.getName().compareTo(o2.getName());
				case 1: // By drops count
					return Integer.compare(getDroplistsCountByItemId(o2.getId(), false), getDroplistsCountByItemId(o1.getId(), false));
				case 2:// By spoil count
					return Integer.compare(getDroplistsCountByItemId(o2.getId(), true), getDroplistsCountByItemId(o1.getId(), true));
				default:
					return o1.getName().compareTo(o2.getName());
			}
		}
	}
	
	public static List<L2NpcTemplate> sortMonsters(List<L2NpcTemplate> npcTemplates, int sort)
	{
		Collections.sort(npcTemplates, new MonsterComparator(sort));
		return npcTemplates;
	}
	
	private static class MonsterComparator implements Comparator<L2NpcTemplate>, Serializable
	{
		private static final long serialVersionUID = 2116090903265145828L;
		private final int sort;
		
		protected MonsterComparator(int sort)
		{
			this.sort = sort;
		}
		
		@Override
		public int compare(L2NpcTemplate o1, L2NpcTemplate o2)
		{
			switch (sort)
			{
				case 0: // By name
					return o1.getName().compareTo(o2.getName());
				case 1:// By drops count
					return Integer.compare(getDropsCount(o2, false), getDropsCount(o1, false));
				case 2:// By spoil count
					return Integer.compare(getDropsCount(o2, true), getDropsCount(o1, true));
				default:
					return o1.getName().compareTo(o2.getName());
			}
		}
	}
	
	public static List<DropInfoHolder> sortMonsters2(List<DropInfoHolder> templates, int sort)
	{
		Collections.sort(templates, new MonstersComparator(sort));
		return templates;
	}
	
	private static class MonstersComparator implements Comparator<DropInfoHolder>, Serializable
	{
		private static final long serialVersionUID = -3803552841261367731L;
		private final int sort;
		
		protected MonstersComparator(int sort)
		{
			this.sort = sort;
		}
		
		@Override
		public int compare(DropInfoHolder o1, DropInfoHolder o2)
		{
			switch (sort)
			{
				case 0: // By name
					return o1.getName().compareTo(o2.getName());
				case 1:// By level
					return Integer.compare(o2.getLevel(), o1.getLevel());
				case 2:// By chance
					return Double.compare(o2.getChance(), o1.getChance());
				case 3:// By type
					return Boolean.compare(o2.isSweep(), o1.isSweep());
				case 4:// By drop count
					return Long.compare(o2.getMin() + o2.getMax(), o1.getMin() + o1.getMax());
				default:
					return o1.getName().compareTo(o2.getName());
			}
		}
	}
}
