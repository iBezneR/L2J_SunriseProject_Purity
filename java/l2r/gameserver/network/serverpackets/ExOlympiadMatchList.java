package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.entity.olympiad.*;
import l2r.gameserver.model.entity.olympiad.tasks.OlympiadGameTask;

import java.util.ArrayList;
import java.util.List;

public class ExOlympiadMatchList extends L2GameServerPacket
{
	private final List<OlympiadGameTask> _games = new ArrayList<>();
	
	public ExOlympiadMatchList()
	{
		OlympiadGameTask task;
		for (int i = 0; i < OlympiadGameManager.getInstance().getNumberOfStadiums(); i++)
		{
			task = OlympiadGameManager.getInstance().getOlympiadTask(i);
			if (task != null)
			{
				if (!task.isGameStarted() || task.isBattleFinished())
				{
					continue; // initial or finished state not shown
				}
				_games.add(task);
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case H5:
				writeH(0xD4);
				break;
			case GC:
			case SL:
				writeH(0xD5);
				break;
		}
		
		writeD(0x00); // Type 0 = Match List, 1 = Match Result
		
		writeD(_games.size());
		writeD(0x00);
		
		for (OlympiadGameTask curGame : _games)
		{
			AbstractOlympiadGame game = curGame.getGame();
			if (game != null)
			{
				writeD(game.getStadiumId()); // Stadium Id (Arena 1 = 0)
				
				if (game instanceof OlympiadGameNonClassed)
				{
					writeD(1);
				}
				else if (game instanceof OlympiadGameClassed)
				{
					writeD(2);
				}
				else if (game instanceof OlympiadGameTeams)
				{
					writeD(-1);
				}
				else
				{
					writeD(0);
				}
				
				writeD(curGame.getState()); // (1 = Standby, 2 = Playing)
				writeS(game.getPlayerNames()[0]); // Player 1 Name
				writeS(game.getPlayerNames()[1]); // Player 2 Name
			}
		}
	}
}
