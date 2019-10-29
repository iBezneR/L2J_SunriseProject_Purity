/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package custom.skillsbalancer;

import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class AdminSkillsBalance implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
			"admin_skillsbalancer",
			"admin_sbalancerupdate",
			"admin_sbalancerreload"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equalsIgnoreCase("admin_skillsbalancer"))
		{
			SkillsBalanceBBSManager.getInstance().cbByPass(command, activeChar);
			return true;
		}
		else if (command.equalsIgnoreCase("admin_sbalancerupdate"))
		{
			SkillsBalanceManager.getInstance().updateBalances();
			activeChar.sendMessage("Skills balances were successfully updated!");
			return true;
		}
		else if (command.equalsIgnoreCase("admin_sbalancerreload"))
		{
			SkillsBalanceManager.getInstance().loadBalances();
			activeChar.sendMessage("Skills balances were successfully reloaded from database!");
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
