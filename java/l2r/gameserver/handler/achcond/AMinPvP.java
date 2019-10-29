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

/**
 * @author Erlandys
 */
public class AMinPvP extends AchievementICond
{
	int _minPvp;
	
	public AMinPvP(String name, String value)
	{
		super(name, value);
		_minPvp = Integer.parseInt(value);
	}
	
	@Override
	public boolean checkCond(L2PcInstance activeChar)
	{
		return activeChar.getPvpKills() >= _minPvp;
	}
	
}
