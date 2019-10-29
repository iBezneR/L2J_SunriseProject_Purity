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

import java.util.HashMap;

/**
 * @author Erlandys
 */
public class AchievementCategory
{
	HashMap<Integer, Achievement> _achievements;
	String _name;
	String _progressIconsSmall[];
	String _progressIcons[];
	
	public AchievementCategory(String name)
	{
		_name = name;
		_achievements = new HashMap<>();
		_progressIcons = new String[]
		{
			"L2UI_CT1.Gauge_DF_Large_cp_Left",
			"L2UI_CT1.Gauge_DF_Large_cp_Center",
			"L2UI_CT1.Gauge_DF_Large_cp_Right",
			"L2UI_CT1.Gauge_DF_Large_cp_bg_Left",
			"L2UI_CT1.Gauge_DF_Large_cp_bg_Center",
			"L2UI_CT1.Gauge_DF_Large_cp_bg_Right"
		};
		_progressIconsSmall = new String[]
		{
			"L2UI_CT1.Gauge_DF_Small_cp_Left",
			"L2UI_CT1.Gauge_DF_Small_cp_Center",
			"L2UI_CT1.Gauge_DF_Small_cp_Right",
			"L2UI_CT1.Gauge_DF_Small_cp_bg_Left",
			"L2UI_CT1.Gauge_DF_Small_cp_bg_Center",
			"L2UI_CT1.Gauge_DF_Small_cp_bg_Right"
		};
	}
	
	public String[] getProgressIcons()
	{
		return _progressIcons;
	}
	
	public String[] getProgressIconsSmall()
	{
		return _progressIconsSmall;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public Achievement getAchievement(int id)
	{
		return _achievements.get(id);
	}
	
	public void addAchievement(int id, Achievement achievement)
	{
		_achievements.put(id, achievement);
	}
	
	public HashMap<Integer, Achievement> getAchievements()
	{
		return _achievements;
	}
	
	public void doSet(String name, String value)
	{
		name = name.toLowerCase();
		switch (name)
		{
			case "progressbarsmallleftunfilled":
				_progressIconsSmall[3] = value;
				break;
			case "progressbarsmallcenterunfilled":
				_progressIconsSmall[4] = value;
				break;
			case "progressbarsmallrightunfilled":
				_progressIconsSmall[5] = value;
				break;
			case "progressbarsmallleftfilled":
				_progressIconsSmall[0] = value;
				break;
			case "progressbarsmallcenterfilled":
				_progressIconsSmall[1] = value;
				break;
			case "progressbarsmallrightfilled":
				_progressIconsSmall[2] = value;
				break;
			case "progressbarleftunfilled":
				_progressIcons[3] = value;
				break;
			case "progressbarcenterunfilled":
				_progressIcons[4] = value;
				break;
			case "progressbarrightunfilled":
				_progressIcons[5] = value;
				break;
			case "progressbarleftfilled":
				_progressIcons[0] = value;
				break;
			case "progressbarcenterfilled":
				_progressIcons[1] = value;
				break;
			case "progressbarrightfilled":
				_progressIcons[2] = value;
				break;
		}
	}
}
