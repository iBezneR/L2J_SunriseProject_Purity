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
package l2r.gameserver.model.drops.strategy;

import l2r.Config;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.drops.GeneralDropItem;
import l2r.gameserver.model.itemcontainer.Inventory;

/**
 * @author Battlecruiser
 */
public interface IChanceMultiplierStrategy
{
	public static final IChanceMultiplierStrategy DROP = DEFAULT_STRATEGY(Config.RATE_DEATH_DROP_CHANCE_MULTIPLIER);
	public static final IChanceMultiplierStrategy SPOIL = DEFAULT_STRATEGY(Config.RATE_CORPSE_DROP_CHANCE_MULTIPLIER);
	public static final IChanceMultiplierStrategy STATIC = (item, victim) -> 1;
	
	public static final IChanceMultiplierStrategy QUEST = (item, victim) ->
	{
		double championmult;
		if ((item.getItemId() == Inventory.ADENA_ID) || (item.getItemId() == Inventory.ANCIENT_ADENA_ID))
		{
			championmult = Config.L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE;
		}
		else
		{
			championmult = Config.L2JMOD_CHAMPION_REWARDS_CHANCE;
		}
		
		return (Config.L2JMOD_CHAMPION_ENABLE && (victim != null) && victim.isChampion()) ? (Config.RATE_QUEST_DROP * championmult) : Config.RATE_QUEST_DROP;
	};
	
	public static IChanceMultiplierStrategy DEFAULT_STRATEGY(final double defaultMultiplier)
	{
		return (item, victim) ->
		{
			float multiplier = 1;
			if (victim.isChampion())
			{
				multiplier *= item.getItemId() != Inventory.ADENA_ID ? Config.L2JMOD_CHAMPION_REWARDS_CHANCE : Config.L2JMOD_CHAMPION_ADENAS_REWARDS_CHANCE;
			}
			Float dropChanceMultiplier = Config.RATE_DROP_CHANCE_MULTIPLIER.get(item.getItemId());
			if (dropChanceMultiplier != null)
			{
				multiplier *= dropChanceMultiplier;
			}
			else if (ItemData.getInstance().getTemplate(item.getItemId()).hasExImmediateEffect())
			{
				switch (item.getItemId())
				{
					case 8600: // Herb of Life
					case 8601: // Greater Herb of Life
					case 8602: // Superior Herb of Life
						multiplier *= Config.RATE_DROP_HP_HERBS;
						break;
					case 8603: // Herb of Mana
					case 8604: // Greater Herb of Mana
					case 8605: // Superior Herb of Mana
						multiplier *= Config.RATE_DROP_MP_HERBS;
						break;
					case 8612: // Herb of the Warrior
					case 8613: // Herb of the Mystic
					case 8614: // Herb of Recovery
						multiplier *= Config.RATE_DROP_SPECIAL_HERBS;
						break;
					case 13028: // Vitality Replenishing Herb
						multiplier *= Config.RATE_DROP_VITALITY_HERBS;
						break;
					default: // Every other herbs e.g. Herb of Power, Herb of Magic
						multiplier *= Config.RATE_DROP_COMMON_HERBS;
						break;
				}
			}
			else if (victim.isRaid())
			{
				multiplier *= Config.RATE_RAID_DROP_CHANCE_MULTIPLIER;
			}
			else
			{
				multiplier *= defaultMultiplier;
			}
			return multiplier;
		};
	}
	
	public double getChanceMultiplier(GeneralDropItem item, L2Character victim);
}
