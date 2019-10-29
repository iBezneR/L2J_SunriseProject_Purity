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

import java.util.ArrayList;


/**
 * @author Erlandys
 */
public class AchievementLevel
{
	int _level;
	int _requiredValue;
	boolean _isExtra;
	String _shortDesc;
	String _longDesc;
	
	ArrayList<ErReward> _rewards;
	ArrayList<ErBonus> _bonuses;
	ArrayList<AchievementICond> _conditions;
	
	public AchievementLevel(int level, int requiredValue, boolean isExtra)
	{
		_level = level;
		_requiredValue = requiredValue;
		_isExtra = isExtra;
		_shortDesc = null;
		_longDesc = null;
		_rewards = new ArrayList<>();
		_bonuses = new ArrayList<>();
		_conditions = new ArrayList<>();
	}
	
	protected void doSet(String name, String val)
	{
		name = name.toLowerCase();
		
		switch (name) {
			case "shortdesc":
				_shortDesc = val;
				break;
			case "longdesc":
				_longDesc = val;
				break;
		}
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getRequiredValue()
	{
		return _requiredValue;
	}
	
	public boolean isExtra()
	{
		return _isExtra;
	}
	
	public String getShortDesc()
	{
		return _shortDesc;
	}
	
	public String getLongDesc()
	{
		return _longDesc;
	}
	
	public ArrayList<ErReward> getRewards()
	{
		return _rewards;
	}
	
	public ArrayList<ErBonus> getBonuses()
	{
		return _bonuses;
	}
	
	protected void setConditions(ArrayList<AchievementICond> list)
	{
		_conditions = list;
	}
	
	protected boolean isVisible(L2PcInstance activeChar)
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
	
	protected boolean hasConditions()
	{
		return _conditions.size() > 0;
	}
}
