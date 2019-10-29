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

/**
 * @author Erlandys
 */

public class ErObject
{
	public enum ErObjectType {
		Item,
		SkillPoints,
		ClanPoints,
		Experience,
		WeaponEnchant,
		ArmorEnchant,
		SkillsEnchant
	}
	
	int _id, _itemId, _minCount, _maxCount;
	double _chance;
	String _icon;
	ErObjectType _type;
	
	public ErObject(int id, ErObjectType type, int itemId, int minCount, int maxCount, double chance, String icon)
	{
		_id = id;
		_type = type;
		_itemId = itemId;
		_minCount = minCount;
		_maxCount = maxCount;
		_chance = chance;
		_icon = icon;
	}

	public int getId()
	{
		return _id;
	}

	public ErObjectType getType()
	{
		return _type;
	}
	
	public void setType(ErObjectType type)
	{
		_type = type;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	public int getMinCount()
	{
		return _minCount;
	}
	
	public void setMinCount(int minCount)
	{
		_minCount = minCount;
	}
	
	public int getMaxCount()
	{
		return _maxCount;
	}
	
	public void setMaxCount(int maxCount)
	{
		_maxCount = maxCount;
	}
	
	public double getChance()
	{
		return _chance;
	}
	
	public void setChance(double chance)
	{
		_chance = chance;
	}

	public String getIcon()
	{
		return _icon;
	}
}