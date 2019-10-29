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
package l2r.gameserver.handler.achcond;

import custom.achievements.AchievementICond;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;

/**
 * @author Erlandys
 */
public class AForRaces extends AchievementICond
{
	ArrayList<Integer> _races;
	
	public AForRaces(String name, String value)
	{
		super(name, value);
		_races = new ArrayList<>();
		for (String cId : value.trim().split(","))
		{
			_races.add(Integer.parseInt(cId));
		}
	}
	
	@Override
	public boolean checkCond(L2PcInstance activeChar)
	{
		return _races.contains(activeChar.getRace().ordinal());
	}
	
}
