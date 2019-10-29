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

import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.model.drops.DropListScope;
import l2r.gameserver.model.drops.GeneralDropItem;
import l2r.gameserver.model.drops.GroupedGeneralDropItem;
import l2r.gameserver.model.items.L2Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author vGodFather
 */
public class DropInfoHandler
{
	private static final Logger _log = LoggerFactory.getLogger(DropInfoHandler.class);
	
	private final Map<Integer, ArrayList<DropInfoHolder>> allItemDropIndex = new HashMap<>();
	private final Map<Integer, String> itemNameMap = new HashMap<>();
	public static final Set<Integer> HERBS = new HashSet<>();
	
	public DropInfoHandler()
	{
	
	}
	
	public void load()
	{
		loadHerbList();
		loadNpcsDrop(true);
		_log.info(getClass().getSimpleName() + ": Loaded " + allItemDropIndex.size() + " drop data for calculator.");
	}
	
	private static void loadHerbList()
	{
		HERBS.addAll(new Range(8154, 8158).values()); // HERBs
		HERBS.addAll(new Range(8600, 8615).values()); // HERBs
		HERBS.addAll(new Range(8952, 8954).values()); // HERBs
		HERBS.addAll(new Range(10655, 10658).values()); // HERBs
		HERBS.addAll(new Range(10655, 10658).values()); // HERBs
		HERBS.add(13028);// Vitality Replenishing Herb
		HERBS.addAll(new Range(10432, 10434).values());// Kertin's Herb
	}
	
	public void addDropInfo(int itemId, DropInfoHolder drop)
	{
		ArrayList<DropInfoHolder> list;
		if ((list = getDrop(itemId)) == null)
		{
			list = new ArrayList<>();
			allItemDropIndex.put(itemId, list);
		}
		
		if (!list.contains(drop))
		{
			list.add(drop);
		}
	}
	
	public void loadNpcsDrop(boolean isCustom)
	{
		NpcTable.getInstance().getNpcs().forEach((id, npc) ->
		{
			if ((npc.getDropLists() != null) && !DropCalculatorConfigs.RESTRICTED_MOB_DROPLIST_IDS.contains(npc.getId()))
			{
				npc.getDropLists().forEach((droptype, droplist) ->
				{
					droplist.forEach((drop) ->
					{
						if (drop instanceof GeneralDropItem)
						{
							GeneralDropItem gd = (GeneralDropItem) drop;
							if (!HERBS.contains(gd.getItemId()))
							{
								addDropInfo(gd.getItemId(), new DropInfoHolder(npc.getId(), npc.getName(), npc.getLevel(), gd.getMin(), gd.getMax(), Math.min(100, gd.getChance()), droptype == DropListScope.CORPSE));
							}
						}
						else if (drop instanceof GroupedGeneralDropItem)
						{
							GroupedGeneralDropItem ggd = (GroupedGeneralDropItem) drop;
							ggd.getItems().forEach((gd) ->
							{
								if (!HERBS.contains(gd.getItemId()))
								{
									addDropInfo(gd.getItemId(), new DropInfoHolder(npc.getId(), npc.getType(), npc.getLevel(), gd.getMin(), gd.getMax(), Math.min(100, gd.getChance()), droptype == DropListScope.CORPSE));
								}
							});
						}
					});
				});
			}
		});
		
		for (L2Item item : ItemData.getInstance().getAllTemItems())
		{
			if (item != null)
			{
				itemNameMap.put(item.getId(), item.getName());
			}
		}
		
		allItemDropIndex.values().forEach(list -> list.sort((o1, o2) -> NpcTable.getInstance().getTemplate(o1.getNpcId()).getLevel() - NpcTable.getInstance().getTemplate(o2.getNpcId()).getLevel()));
	}
	
	public ArrayList<DropInfoHolder> getDrop(int itemId)
	{
		return allItemDropIndex.get(itemId);
	}
	
	public Map<Integer, ArrayList<DropInfoHolder>> getInfo()
	{
		return allItemDropIndex;
	}
	
	public Comparator<DropInfoHolder> compareByChances = (o1, o2) ->
	{
		double level1 = o1.getChance();
		double level2 = o2.getChance();
		
		return level1 < level2 ? 1 : level1 == level2 ? 0 : -1;
	};
	
	public static DropInfoHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DropInfoHandler _instance = new DropInfoHandler();
	}
}
