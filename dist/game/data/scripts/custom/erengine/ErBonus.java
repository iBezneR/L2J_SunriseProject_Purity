/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.erengine;

import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * @author Erlandys
 */

public class ErBonus extends ErObject
{
	boolean _itemTypeChance, _addingPercent;
	long _time;

	public ErBonus(int id, ErObjectType type, boolean itemTypeChance, boolean addingPercent, int itemId, int minCount, int maxCount, double chance, long time, String icon)
	{
		super(id, type, itemId, minCount, maxCount, chance, icon);
		_itemTypeChance = itemTypeChance;
		_addingPercent = addingPercent;
		_time = time;
	}
	
	public boolean isItemChance()
	{
		return _itemTypeChance;
	}
	
	public boolean isAddingPercent()
	{
		return _addingPercent;
	}
	
	public long getTime()
	{
		return _time;
	}
	
	public static int readBonus(Node a, ArrayList<ErBonus> bonuses, int bonusId)
	{
		ErObjectType bonusType = null;
		int itemId = 0, minCount = 0, maxCount = 0;
		double chance = 0;
		boolean itemTypeChance = true;
		boolean addingPercent = true;
		long time = 0;
		String icon = "icon.NOIMAGE";
		if (a.getAttributes().getNamedItem("type") != null)
		{
			String r = a.getAttributes().getNamedItem("type").getNodeValue();
			try {
				bonusType = ErObjectType.valueOf(r);
			}
			catch (EnumConstantNotPresentException ecnpe)
			{
				System.out.println("Bonus type: [" + r + "] does not exist!");
				return -1;
			}
		}
		if (a.getAttributes().getNamedItem("id") != null)
		{
			itemId = Integer.parseInt(a.getAttributes().getNamedItem("id").getNodeValue());
		}
		if (a.getAttributes().getNamedItem("min") != null)
		{
			minCount = Integer.parseInt(a.getAttributes().getNamedItem("min").getNodeValue());
		}
		if (a.getAttributes().getNamedItem("max") != null)
		{
			maxCount = Integer.parseInt(a.getAttributes().getNamedItem("max").getNodeValue());
		}
		if (a.getAttributes().getNamedItem("chance") != null)
		{
			chance = Double.parseDouble(a.getAttributes().getNamedItem("chance").getNodeValue());
		}
		if (bonusType != null && bonusType.equals(ErObjectType.Item) && a.getAttributes().getNamedItem("itemBonusType") != null)
		{
			itemTypeChance = a.getAttributes().getNamedItem("itemBonusType").getNodeValue().equalsIgnoreCase("chance");
		}
		if (a.getAttributes().getNamedItem("addingType") != null)
		{
			addingPercent = a.getAttributes().getNamedItem("addingType").getNodeValue().equalsIgnoreCase("percent");
		}
		if (a.getAttributes().getNamedItem("time") != null)
		{
			time = Long.parseLong(a.getAttributes().getNamedItem("time").getNodeValue()) * 1000;
		}
		if (a.getAttributes().getNamedItem("icon") != null)
		{
			icon = a.getAttributes().getNamedItem("icon").getNodeValue();
		}
		bonuses.add(new ErBonus(bonusId, bonusType, itemTypeChance, addingPercent, itemId, minCount, maxCount, chance, time, icon));
		return ++bonusId;
	}
}