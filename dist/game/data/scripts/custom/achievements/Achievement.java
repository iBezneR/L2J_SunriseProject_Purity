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
package custom.achievements;

import custom.erengine.ErBonus;
import custom.erengine.ErReward;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.CommonSkill;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.serverpackets.MagicSkillUse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Erlandys
 */
public class Achievement
{
	int _categoryId;
	String _category;
	String _name;
	String _icon;
	int _titleColor;
	String _titleColorHTML;
	String _type;
	String _shortDesc;
	String _longDesc;
	int _levelsCount;
	String _title;
	boolean _isDaily;
	boolean _giveTitle;
	boolean _isWithTime;
	HashMap<Integer, AchievementLevel> _achievementLevels;
	ArrayList<AchievementICond> _conditions;
	
	public Achievement(int categoryId, String category, String name, String type)
	{
		_categoryId = categoryId;
		_category = category;
		_name = name;
		_icon = "icon.NOIMAGE";
		_titleColor = 0xFFFF77;
		_titleColorHTML = "77FFFF";
		_type = type;
		_shortDesc = "No information.";
		_longDesc = "No information.";
		_title = name;
		_levelsCount = 0;
		_isDaily = false;
		_giveTitle = true;
		_isWithTime = false;
		_achievementLevels = new HashMap<>();
		_conditions = new ArrayList<>();
	}
	
	public void doSet(String name, String val)
	{
		name = name.toLowerCase();
		switch (name)
		{
			case "title":
				_title = val;
				break;
			case "titlecolor":
				_titleColor = Integer.parseInt(val.substring(4) + val.substring(2, 4) + val.substring(0, 2), 16); // BGR
				_titleColorHTML = val; // RGB
				break;
			case "icon":
				_icon = val;
				break;
			case "shortdesc":
				_shortDesc = val;
				break;
			case "longdesc":
				_longDesc = val;
				break;
			case "isdaily":
				_isDaily = val.equalsIgnoreCase("true");
				break;
			case "givetitle":
				_giveTitle = val.equalsIgnoreCase("true");
				break;
			case "iswithtime":
				_isWithTime = val.equalsIgnoreCase("true");
				break;
		}
	}
	
	public String getCategory()
	{
		return _category;
	}
	
	public int getCategoryId()
	{
		return _categoryId;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getIcon()
	{
		return _icon;
	}

	public int getTitleColor()
	{
		return _titleColor;
	}

	public String getTitleColorHTML()
	{
		return _titleColorHTML;
	}
	
	public String getType()
	{
		return _type;
	}
	
	public int getLevelsCount()
	{
		return _levelsCount;
	}
	
	public int getRequiredValue(int level)
	{
		if (_achievementLevels.get(level) == null)
		{
			return -1;
		}
		return _achievementLevels.get(level).getRequiredValue();
	}
	
	public AchievementLevel getLevel(int level)
	{
		if (_achievementLevels.containsKey(level))
			return _achievementLevels.get(level);
		return null;
	}
	
	public void addLevel(int level, AchievementLevel al)
	{
		_achievementLevels.put(level, al);
		if (al.hasConditions() && al.isExtra())
			return;
		_levelsCount++;
	}
	
	public String getShortDesc()
	{
		return _shortDesc;
	}
	
	public String getLongDesc()
	{
		return _longDesc;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public boolean isDaily()
	{
		return _isDaily;
	}
	
	public boolean isWithTime()
	{
		return _isWithTime;
	}
	
	public boolean giveTitle()
	{
		return _giveTitle;
	}
	
	public boolean isVisible(L2PcInstance activeChar)
	{
		if (_conditions.size() < 1)
			return true;
		
		for (AchievementICond cond : _conditions)
		{
			if (!cond.checkCond(activeChar))
				return false;
		}
		return true;
	}
	
	public int getLevelByPoints(long count)
	{
		for (int i = _achievementLevels.size(); i > 0; i--)
		{
			if (_achievementLevels.get(i).getRequiredValue() <= count)
			{
				return i;
			}
		}
		return 0;
	}
	
	public void setConditions(ArrayList<AchievementICond> list)
	{
		_conditions = list;
	}

	public void sendGift(L2PcInstance player, int level)
	{
		AchievementLevel l = getLevel(level);
		if (l == null)
		{
			return;
		}
		player.sendMessage("You have achieved [" + getName() + "] " + level + " level.");
		for (ErReward r : l.getRewards())
		{
			r.giveReward(player);
		}
		for (ErBonus b : l.getBonuses())
		{
			player.getPlayerBonuses().addBonus(b);
		}
		// Fires Fireworks when player receives reward for completing achievements
		L2Skill skill = CommonSkill.FIREWORK.getSkill();
		if (skill != null)
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
		}
	}
}
