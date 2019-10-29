package custom.achievements;

import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class AdminAchievements implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_reloadach"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equalsIgnoreCase("admin_reloadach"))
		{
			AchievementsParser.getInstance().loadXML();
			activeChar.sendMessage("Achievements were successfully reloaded!");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}