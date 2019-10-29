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
package l2r.gameserver.taskmanager.tasks;

import gr.sr.configsEngine.configs.impl.PcBangConfigs;
import l2r.Config;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.taskmanager.Task;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2r.gameserver.taskmanager.TaskTypes;
import l2r.util.Rnd;

/**
 * @author ProGramMoS, Erlandys
 */
public class PcPointUpdater extends Task
{
    private static final String NAME = "pc_bang_points";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void onTimeElapsed(ExecutedTask task)
    {
        if (!Config.PCB_ENABLE)
        {
            return;
        }
        int score;
        for (L2PcInstance activeChar : L2World.getInstance().getPlayers())
        {
            //if ((activeChar == null) || activeChar.getClient().isDetached() || !activeChar.isOnline() || (activeChar.getLevel() < Config.PCB_MIN_LEVEL))
            if ((activeChar == null) || activeChar.getClient() == null || activeChar.getClient().isDetached() || !activeChar.isOnline() || (activeChar.getLevel() < Config.PCB_MIN_LEVEL))
            {
                continue;
            }

            score = Rnd.get(Config.PCB_POINT_MIN, Config.PCB_POINT_MAX);
            int playerspoints = activeChar.getPcBangPoints();
            int over;

            if (activeChar.isPremium())
            {
                score *= Config.PCB_BONUS_FOR_PREMIUM;
            }

            if (Rnd.get(100) <= Config.PCB_CHANCE_DUAL_POINT)
            {
                // If player has more or equal MAX_PC_BANG_POINTS then the engine stops generating points
                if (playerspoints >= PcBangConfigs.MAX_PC_BANG_POINTS)
                {
                    return;
                }
                else
                {
                    score *= 2;

                    activeChar.addPcBangScore(score);
                }
                // If the total bang points exceede MAX_PC_BANG_POINTS value then the total score is reduced by the exceeding amount
                int sum = (playerspoints + score);
                if (sum > PcBangConfigs.MAX_PC_BANG_POINTS)
                {
                    over = (sum - PcBangConfigs.MAX_PC_BANG_POINTS);
                    activeChar.reducePcBangScore(over);
                }

                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE);
                sm.addInt(score);
                activeChar.sendPacket(sm);

                activeChar.updatePcBangWnd(score, true, true);
            }
            else
            {

                int sum = (playerspoints + score);

                if (playerspoints >= PcBangConfigs.MAX_PC_BANG_POINTS)
                {
                    return;
                }
                else
                {
                    activeChar.addPcBangScore(score);
                }
                // If the total bang points exceede MAX_PC_BANG_POINTS value then the total score is reduced by the exceeding amount
                if (sum > PcBangConfigs.MAX_PC_BANG_POINTS)
                {
                    over = (sum - PcBangConfigs.MAX_PC_BANG_POINTS);
                    activeChar.reducePcBangScore(over);
                }

                SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT);
                sm.addInt(score);
                activeChar.sendPacket(sm);

                activeChar.updatePcBangWnd(score, true, false);
            }
        }
    }

    @Override
    public void initializate()
    {
        super.initializate();
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, String.valueOf(Config.PCB_INTERVAL * 1000), String.valueOf(Config.PCB_INTERVAL * 1000), "");
    }
}