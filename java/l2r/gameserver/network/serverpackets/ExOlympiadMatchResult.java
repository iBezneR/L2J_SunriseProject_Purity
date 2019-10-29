package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.model.entity.olympiad.OlympiadInfo;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExOlympiadMatchResult extends L2GameServerPacket
{
	private final boolean _tie;
	private int _winTeam; // 1,2
	private int _loseTeam = 2;
	private final List<OlympiadInfo> _winnerList;
	private final List<OlympiadInfo> _loserList;
	
	public ExOlympiadMatchResult(boolean tie, int winTeam, List<OlympiadInfo> winnerList, List<OlympiadInfo> loserList)
	{
		_tie = tie;
		_winTeam = winTeam;
		_winnerList = winnerList;
		_loserList = loserList;
		
		if (_winTeam == 2)
		{
			_loseTeam = 1;
		}
		else if (_winTeam == 0)
		{
			_winTeam = 1;
		}
	}
	
	@Override
	protected void writeImpl()
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
		
		writeD(0x01); // Type 0 = Match List, 1 = Match Result
		
		writeD(_tie ? 1 : 0); // 0 - win, 1 - tie
		writeS(_winnerList.get(0).getName());
		writeD(_winTeam);
		writeD(_winnerList.size());
		for (OlympiadInfo info : _winnerList)
		{
			writeS(info.getName());
			writeS(info.getClanName());
			writeD(info.getClanId());
			writeD(info.getClassId());
			writeD(info.getDamage());
			writeD(info.getCurrentPoints());
			writeD(info.getDiffPoints());
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					writeD(0x00);
					break; // Helios
			}
		}
		
		writeD(_loseTeam);
		writeD(_loserList.size());
		for (OlympiadInfo info : _loserList)
		{
			writeS(info.getName());
			writeS(info.getClanName());
			writeD(info.getClanId());
			writeD(info.getClassId());
			writeD(info.getDamage());
			writeD(info.getCurrentPoints());
			writeD(info.getDiffPoints());
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					writeD(0x00);
					break; // Helios
			}
		}
	}
}
