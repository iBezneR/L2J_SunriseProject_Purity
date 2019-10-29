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

import custom.erengine.ErObject.ErObjectType;
import l2r.L2DatabaseFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Erlandys
 */

public class ErPlayerBonuses
{
	int _lastId;
	
	// Experience
	public int _addingPercentExperience;
	public int _addingAmountExperience;
	// Skill Points
	public int _addingPercentSkillPoints;
	public int _addingAmountSkillPoints;
	// Clan Points
	public int _addingPercentClanPoints;
	public int _addingAmountClanPoints;
	// Weapons Enchant
	public int _addingPercentWeaponsEnchant;
	public int _addingAmountWeaponsEnchant;
	// Armors Enchant
	public int _addingPercentArmorsEnchant;
	public int _addingAmountArmorsEnchant;
	// Skills Enchant
	public int _addingPercentSkillsEnchant;
	public int _addingAmountSkillsEnchant;
	// Items
	public HashMap<Integer, Integer> _addingDropPercentItems;
	public HashMap<Integer, Integer> _addingChancePercentItems;
	public HashMap<Integer, Integer> _addingDropAmountItems;
	public HashMap<Integer, Integer> _addingChanceAmountItems;
	
	public ArrayList<Bonus> _expirations;
	public ArrayList<Bonus> _expiredBonuses;
	
	L2PcInstance _player;
	
	public ErPlayerBonuses(L2PcInstance player)
	{
		_lastId = 0;
		
		_player = player;
		
		_addingPercentExperience = 0;
		_addingAmountExperience = 0;
		
		_addingPercentSkillPoints = 0;
		_addingAmountSkillPoints = 0;
		
		_addingPercentClanPoints = 0;
		_addingAmountClanPoints = 0;
		
		_addingPercentWeaponsEnchant = 0;
		_addingAmountWeaponsEnchant = 0;
		
		_addingPercentArmorsEnchant = 0;
		_addingAmountArmorsEnchant = 0;
		
		_addingPercentSkillsEnchant = 0;
		_addingAmountSkillsEnchant = 0;
		
		_addingDropPercentItems = new HashMap<>();
		_addingChancePercentItems = new HashMap<>();
		_addingDropAmountItems = new HashMap<>();
		_addingChanceAmountItems = new HashMap<>();
		
		_expirations = new ArrayList<>();
		_expiredBonuses = new ArrayList<>();
		restoreBonuses();
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	public void addBonus(ErBonus bonus)
	{
		if (bonus.getType().equals(ErObjectType.Item))
		{
			addBonus(bonus.getItemId(), Rnd.get(bonus.getMinCount(), bonus.getMaxCount()), bonus.isAddingPercent(), bonus.isItemChance(), bonus.getTime(), false);
		}
		else
		{
			addBonus(bonus.getType(), Rnd.get(bonus.getMinCount(), bonus.getMaxCount()), bonus.isAddingPercent(), bonus.getTime(), false);
		}
	}
	
	public void addBonus(ErObjectType type, int count, boolean percent, long expiresAfter, boolean restore)
	{
		if (!restore)
		{
			_expirations.add(new Bonus(++_lastId, type, 0, count, percent, false, System.currentTimeMillis() + expiresAfter));
		}
		if (type.equals(ErObjectType.Experience))
		{
			if (percent)
			{
				_addingPercentExperience += count;
			}
			else
			{
				_addingAmountExperience += count;
			}
		}
		else if (type.equals(ErObjectType.SkillPoints))
		{
			if (percent)
			{
				_addingPercentSkillPoints += count;
			}
			else
			{
				_addingAmountSkillPoints += count;
			}
		}
		else if (type.equals(ErObjectType.ClanPoints))
		{
			if (percent)
			{
				_addingPercentClanPoints += count;
			}
			else
			{
				_addingAmountClanPoints += count;
			}
		}
		else if (type.equals(ErObjectType.WeaponEnchant))
		{
			if (percent)
			{
				_addingPercentWeaponsEnchant += count;
			}
			else
			{
				_addingAmountWeaponsEnchant += count;
			}
		}
		else if (type.equals(ErObjectType.ArmorEnchant))
		{
			if (percent)
			{
				_addingPercentArmorsEnchant += count;
			}
			else
			{
				_addingAmountArmorsEnchant += count;
			}
		}
		else if (type.equals(ErObjectType.SkillsEnchant))
		{
			if (percent)
			{
				_addingPercentSkillsEnchant += count;
			}
			else
			{
				_addingAmountSkillsEnchant += count;
			}
		}
	}
	
	public void addBonus(int itemId, int count, boolean percent, boolean itemChance, long expiresAfter, boolean restore)
	{
		if (!restore)
		{
			_expirations.add(new Bonus(++_lastId, ErObjectType.Item, itemId, count, percent, itemChance, System.currentTimeMillis() + expiresAfter));
		}
		if (percent)
		{
			if (itemChance)
			{
				if (_addingChancePercentItems.containsKey(itemId))
				{
					count += _addingChancePercentItems.get(itemId);
				}
				_addingChancePercentItems.put(itemId, count);
			}
			else
			{
				if (_addingDropPercentItems.containsKey(itemId))
				{
					count += _addingDropPercentItems.get(itemId);
				}
				_addingDropPercentItems.put(itemId, count);
			}
		}
		else
		{
			if (itemChance)
			{
				if (_addingChanceAmountItems.containsKey(itemId))
				{
					count += _addingChanceAmountItems.get(itemId);
				}
				_addingChanceAmountItems.put(itemId, count);
			}
			else
			{
				if (_addingDropAmountItems.containsKey(itemId))
				{
					count += _addingDropAmountItems.get(itemId);
				}
				_addingDropAmountItems.put(itemId, count);
			}
		}
	}
	
	public int getBonus(ErObjectType type, int count)
	{
		if (type.equals(ErObjectType.Experience))
		{
			count *= (_addingPercentExperience / 100.0) + 1;
			count += _addingAmountExperience;
		}
		else if (type.equals(ErObjectType.SkillPoints))
		{
			count *= (_addingPercentSkillPoints / 100.0) + 1;
			count += _addingAmountSkillPoints;
		}
		else if (type.equals(ErObjectType.ClanPoints))
		{
			count *= (_addingPercentClanPoints / 100.0) + 1;
			count += _addingAmountClanPoints;
		}
		else if (type.equals(ErObjectType.WeaponEnchant))
		{
			count *= (_addingPercentWeaponsEnchant / 100.0) + 1;
			count += _addingAmountWeaponsEnchant;
		}
		else if (type.equals(ErObjectType.ArmorEnchant))
		{
			count *= (_addingPercentArmorsEnchant / 100.0) + 1;
			count += _addingAmountArmorsEnchant;
		}
		else if (type.equals(ErObjectType.SkillsEnchant))
		{
			count *= (_addingPercentSkillsEnchant / 100.0) + 1;
			count += _addingAmountSkillsEnchant;
		}
		return count;
	}
	
	public int getBonus(int id, boolean isChance, int count)
	{
		if (isChance)
		{
			int percent = _addingChancePercentItems.containsKey(id) ? _addingChancePercentItems.get(id) : 0;
			int amount = _addingChanceAmountItems.containsKey(id) ? _addingChanceAmountItems.get(id) : 0;
			count *= (percent / 100.0) + 1;
			count += amount;
		}
		else
		{
			int percent = _addingDropPercentItems.containsKey(id) ? _addingDropPercentItems.get(id) : 0;
			int amount = _addingDropAmountItems.containsKey(id) ? _addingDropAmountItems.get(id) : 0;
			count *= (percent / 100.0) + 1;
			count += amount;
		}
		return count;
	}
	
	public void doBonusCheck()
	{
		int s = _expiredBonuses.size();
		for (Bonus bonus : _expirations)
		{
			if (bonus.getExpireDate() < System.currentTimeMillis())
			{
				if (bonus.getType().equals(ErObjectType.Experience))
				{
					if (bonus.isAddingPercent())
					{
						_addingPercentExperience -= bonus.getCount();
					}
					else
					{
						_addingAmountExperience -= bonus.getCount();
					}
				}
				else if (bonus.getType().equals(ErObjectType.SkillPoints))
				{
					if (bonus.isAddingPercent())
					{
						_addingPercentSkillPoints -= bonus.getCount();
					}
					else
					{
						_addingAmountSkillPoints -= bonus.getCount();
					}
				}
				else if (bonus.getType().equals(ErObjectType.ClanPoints))
				{
					if (bonus.isAddingPercent())
					{
						_addingPercentClanPoints -= bonus.getCount();
					}
					else
					{
						_addingAmountClanPoints -= bonus.getCount();
					}
				}
				else if (bonus.getType().equals(ErObjectType.WeaponEnchant))
				{
					if (bonus.isAddingPercent())
					{
						_addingPercentWeaponsEnchant -= bonus.getCount();
					}
					else
					{
						_addingAmountWeaponsEnchant -= bonus.getCount();
					}
				}
				else if (bonus.getType().equals(ErObjectType.ArmorEnchant))
				{
					if (bonus.isAddingPercent())
					{
						_addingPercentArmorsEnchant -= bonus.getCount();
					}
					else
					{
						_addingAmountArmorsEnchant -= bonus.getCount();
					}
				}
				else if (bonus.getType().equals(ErObjectType.SkillsEnchant))
				{
					if (bonus.isAddingPercent())
					{
						_addingPercentSkillsEnchant -= bonus.getCount();
					}
					else
					{
						_addingAmountSkillsEnchant -= bonus.getCount();
					}
				}
				else if (bonus.getType().equals(ErObjectType.Item))
				{
					if (bonus.isAddingPercent())
					{
						if (bonus.isItemChance())
						{
							if (_addingChancePercentItems.containsKey(bonus.getItemId()))
							{
								int c = _addingChancePercentItems.get(bonus.getItemId()) - bonus.getCount();
								if (c < 1)
								{
									_addingChancePercentItems.remove(bonus.getItemId());
								}
								else
								{
									_addingChancePercentItems.put(bonus.getItemId(), c);
								}
							}
						}
						else
						{
							if (_addingDropPercentItems.containsKey(bonus.getItemId()))
							{
								int c = _addingDropPercentItems.get(bonus.getItemId()) - bonus.getCount();
								if (c < 1)
								{
									_addingDropPercentItems.remove(bonus.getItemId());
								}
								else
								{
									_addingDropPercentItems.put(bonus.getItemId(), c);
								}
							}
						}
					}
					else
					{
						if (bonus.isItemChance())
						{
							if (_addingChanceAmountItems.containsKey(bonus.getItemId()))
							{
								int c = _addingChanceAmountItems.get(bonus.getItemId()) - bonus.getCount();
								if (c < 1)
								{
									_addingChanceAmountItems.remove(bonus.getItemId());
								}
								else
								{
									_addingChanceAmountItems.put(bonus.getItemId(), c);
								}
							}
						}
						else
						{
							if (_addingDropAmountItems.containsKey(bonus.getItemId()))
							{
								int c = _addingDropAmountItems.get(bonus.getItemId()) - bonus.getCount();
								if (c < 1)
								{
									_addingDropAmountItems.remove(bonus.getItemId());
								}
								else
								{
									_addingDropAmountItems.put(bonus.getItemId(), c);
								}
							}
						}
					}
				}
				_expiredBonuses.add(bonus);
			}
		}
		if (_expiredBonuses.size() > s)
		{
			for (int i = s; i < _expiredBonuses.size(); i++)
			{
				_expirations.remove(_expiredBonuses.get(i));
			}
		}
	}
	
	private void addBonus(Bonus bonus, boolean restore)
	{
		if (bonus.getType().equals(ErObjectType.Item))
		{
			addBonus(bonus.getItemId(), bonus.getCount(), bonus.isAddingPercent(), bonus.isItemChance(), bonus.getExpireDate(), restore);
		}
		else
		{
			addBonus(bonus.getType(), bonus.getCount(), bonus.isAddingPercent(), bonus.getExpireDate(), restore);
		}
	}
	
	public void restoreBonuses()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			try (PreparedStatement statement = con.prepareStatement("SELECT * FROM er_character_bonuses WHERE objectId = ?");)
			{
				statement.setInt(1, _player.getObjectId());
				try (ResultSet rset = statement.executeQuery();)
				{
					while (rset.next())
					{
						int id = rset.getInt("id");
						Bonus bonus = new Bonus(id, ErObjectType.valueOf(rset.getString("type")), rset.getInt("itemId"), rset.getInt("count"), rset.getBoolean("addingPercent"), rset.getBoolean("itemTypeChance"), rset.getLong("expireDate"));
						if (id > _lastId)
						{
							_lastId = id;
						}
						if (bonus.getExpireDate() < System.currentTimeMillis())
						{
							_expiredBonuses.add(bonus);
						}
						else
						{
							addBonus(bonus, true);
							_expirations.add(bonus);
						}
					}
					statement.close();
					rset.close();
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Failed loading character bonuses data.\n" + e);
		}
	}
	
	public void updateBonuses()
	{
		if (_player == null)
		{
			return;
		}
		doBonusCheck();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("REPLACE er_character_bonuses SET id=?, objectId=?, type=?, itemId=?, count=?, itemTypeChance=?, addingPercent=?, expireDate=?;");)
		{
			
			for (Bonus bonus : _expirations)
			{
				statement.setInt(1, bonus.getId());
				statement.setInt(2, _player.getObjectId());
				statement.setString(3, bonus.getType().toString());
				statement.setInt(4, bonus.getItemId());
				statement.setInt(5, bonus.getCount());
				statement.setInt(6, bonus.isItemChance() ? 1 : 0);
				statement.setInt(7, bonus.isAddingPercent() ? 1 : 0);
				statement.setLong(8, bonus.getExpireDate());
				statement.addBatch();
			}
			statement.executeBatch();
			statement.close();
			
			if (_expiredBonuses.size() > 0)
			{
				try (PreparedStatement statement1 = con.prepareStatement("DELETE FROM er_character_bonuses WHERE id=? AND objectId=?");)
				{
					
					for (Bonus bonus : _expiredBonuses)
					{
						statement1.setInt(1, bonus.getId());
						statement1.setInt(2, _player.getObjectId());
						statement1.addBatch();
					}
					statement1.executeBatch();
					statement1.close();
					_expiredBonuses.clear();
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Could not store char bonuses data: " + e.getMessage());
		}
	}
	
	class Bonus
	{
		int _id;
		ErObjectType _type;
		int _itemId;
		int _count;
		boolean _percent;
		boolean _itemChance;
		long _expireDate;
		
		public Bonus(int id, ErObjectType type, int itemId, int count, boolean percent, boolean itemChance, long expireDate)
		{
			_id = id;
			_type = type;
			_itemId = itemId;
			_count = count;
			_percent = percent;
			_itemChance = itemChance;
			_expireDate = expireDate;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public ErObjectType getType()
		{
			return _type;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public int getCount()
		{
			return _count;
		}
		
		public boolean isAddingPercent()
		{
			return _percent;
		}
		
		public boolean isItemChance()
		{
			return _itemChance;
		}
		
		public long getExpireDate()
		{
			return _expireDate;
		}
	}
}