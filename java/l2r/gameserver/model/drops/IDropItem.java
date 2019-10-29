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
package l2r.gameserver.model.drops;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.holders.ItemHolder;

import java.util.List;

/**
 * @author NosBit
 */
public interface IDropItem
{
	
	/**
	 * Calculates drops of this drop item.
	 * @param victim the victim
	 * @param killer the killer
	 * @return {@code null} or empty collection if there are no drops, a collection containing all items to drop otherwise
	 */
	public List<ItemHolder> calculateDrops(L2Character victim, L2Character killer);
}
