/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import l2r.Config;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.*;
import l2r.gameserver.network.SystemMessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands: - kill = kills target L2Character - kill_monster = kills target non-player - kill <radius> = If radius is specified, then ALL players only in that radius will be killed. - kill_monster <radius> = If radius is specified, then ALL non-players only in
 * that radius will be killed.
 * @version $Revision: 1.2.4.5 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminKill implements IAdminCommandHandler
{
	private static Logger _log = LoggerFactory.getLogger(AdminKill.class);
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_kill",
		"admin_kill_monster"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_kill"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if (st.hasMoreTokens())
			{
				String firstParam = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(firstParam);
				if (plyr != null)
				{
					if (st.hasMoreTokens())
					{
						try
						{
							int radius = Integer.parseInt(st.nextToken());
							for (L2Character knownChar : plyr.getKnownList().getKnownCharactersInRadius(radius))
							{
								if ((knownChar instanceof L2ControllableMobInstance) || (knownChar instanceof L2DoorInstance) || (knownChar == activeChar))
								{
									continue;
								}
								
								kill(activeChar, knownChar);
							}
							
							activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
							return true;
						}
						catch (NumberFormatException e)
						{
							activeChar.sendMessage("Invalid radius.");
							return false;
						}
					}
					kill(activeChar, plyr);
				}
				else
				{
					try
					{
						int radius = Integer.parseInt(firstParam);
						
						for (L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if ((knownChar instanceof L2ControllableMobInstance) || (knownChar == activeChar))
							{
								continue;
							}
							kill(activeChar, knownChar);
						}
						
						activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
						return true;
					}
					catch (NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //kill <player_name | radius>");
						return false;
					}
				}
			}
			else
			{
				L2Object obj = activeChar.getTarget();
				if ((obj instanceof L2ControllableMobInstance) || !(obj instanceof L2Character))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				else
				{
					kill(activeChar, (L2Character) obj);
				}
			}
		}
		return true;
	}
	
	private void kill(L2PcInstance activeChar, L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			if (!((L2PcInstance) target).isGM())
			{
				target.stopAllEffects(); // e.g. invincibility effect
			}
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar, null);
		}
		else
		{
			boolean targetIsInvul = false;
			if (target.isInvul())
			{
				targetIsInvul = true;
				target.setIsInvul(false);
			}
			
			double reduce = (Config.L2JMOD_CHAMPION_ENABLE && target.isChampion()) ? Config.L2JMOD_CHAMPION_HP : 1;
			// L2MonsterInstance HP Admin Kill Multiplier
			if ((target instanceof L2MonsterInstance) && !target.isRaid())
			{
				if (Config.MONSTER_INDIVIDUAL_HP_MULTIPLIER.containsKey(target.getId()))
				{
					reduce *= Config.MONSTER_INDIVIDUAL_HP_MULTIPLIER.get(target.getId());
				}
				else
				{
					reduce *= Config.MONSTER_HP_MULTIPLIER;
				}
			}
			// L2RaidBossInstance HP Admin Kill Multiplier
			if (target instanceof L2RaidBossInstance)
			{
				if (Config.RAID_INDIVIDUAL_HP_MULTIPLIER.containsKey(target.getId()))
				{
					reduce *= Config.RAID_INDIVIDUAL_HP_MULTIPLIER.get(target.getId());
				}
				else
				{
					reduce *= Config.RAID_HP_MULTIPLIER;
				}
			}
			// Minion HP Admin Kill Multiplier
			if (target.isMinion())
			{
				if (Config.MINION_INDIVIDUAL_HP_MULTIPLIER.containsKey(target.getId()))
				{
					reduce *= Config.MINION_INDIVIDUAL_HP_MULTIPLIER.get(target.getId());
				}
				else
				{
					reduce *= Config.MINION_HP_MULTIPLIER;
				}
			}
			// L2GrandBossInstance HP Admin Kill Multiplier
			if (target instanceof L2GrandBossInstance)
			{
				if (Config.GRAND_INDIVIDUAL_HP_MULTIPLIER.containsKey(target.getId()))
				{
					reduce *= Config.GRAND_INDIVIDUAL_HP_MULTIPLIER.get(target.getId());
				}
				else
				{
					reduce *= Config.GRAND_HP_MULTIPLIER;
				}
			}
			// L2DefenderInstance HP Admin Kill Multiplier
			if (target instanceof L2DefenderInstance)
			{
				if (Config.DEFENDER_INDIVIDUAL_HP_MULTIPLIER.containsKey(target.getId()))
				{
					reduce *= Config.DEFENDER_INDIVIDUAL_HP_MULTIPLIER.get(target.getId());
				}
				else
				{
					reduce *= Config.DEFENDER_HP_MULTIPLIER;
				}
			}
			target.reduceCurrentHp((target.getMaxHp() * reduce) + 1, activeChar, null);
			
			if (targetIsInvul)
			{
				target.setIsInvul(true);
			}
		}
		if (Config.DEBUG)
		{
			_log.info("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ")" + " killed character " + target.getObjectId());
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
