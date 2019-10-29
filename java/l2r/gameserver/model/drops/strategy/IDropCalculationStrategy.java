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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.drops.GeneralDropItem;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.util.Rnd;

import java.util.Collections;
import java.util.List;

/**
 * @author Battlecruiser
 */
public interface IDropCalculationStrategy
{
	public static final IDropCalculationStrategy DEFAULT_STRATEGY = (item, victim, killer) ->
	{
		double chance = item.getChance(victim, killer);
		if (killer instanceof L2PcInstance)
		{
			chance = killer.getActingPlayer().getPlayerBonuses().getBonus(item.getItemId(), true, (int) chance);
		}
		if (chance > (Rnd.nextDouble() * 100))
		{
			int amountMultiply = 1;
			if (item.isPreciseCalculated() && (chance > 100))
			{
				amountMultiply = (int) chance / 100;
				if ((chance % 100) > (Rnd.nextDouble() * 100))
				{
					amountMultiply++;
				}
			}
			
			long amount = (Rnd.get(item.getMin(victim), item.getMax(victim)) * amountMultiply);
			// Museum and Achievements Engine Bonus
			if (killer instanceof L2PcInstance)
			{
				amount = killer.getActingPlayer().getPlayerBonuses().getBonus(item.getItemId(), false, (int) amount);
			}
			
			return Collections.singletonList(new ItemHolder(item.getItemId(), amount));
		}
		
		return null;
	};
	
	public List<ItemHolder> calculateDrops(GeneralDropItem item, L2Character victim, L2Character killer);
}
