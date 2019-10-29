package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExCubeGameExtendedChangePoints extends L2GameServerPacket
{
	int _timeLeft;
	int _bluePoints;
	int _redPoints;
	boolean _isRedTeam;
	L2PcInstance _player;
	int _playerPoints;
	
	public ExCubeGameExtendedChangePoints(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, L2PcInstance player, int playerPoints)
	{
		_timeLeft = timeLeft;
		_bluePoints = bluePoints;
		_redPoints = redPoints;
		_isRedTeam = isRedTeam;
		_player = player;
		_playerPoints = playerPoints;
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x98);
				break;
			case GC:
			case SL:
				writeH(0x99);
				break;
		}
		
		writeD(0x00);
		
		writeD(_timeLeft);
		writeD(_bluePoints);
		writeD(_redPoints);
		
		writeD(_isRedTeam ? 0x01 : 0x00);
		writeD(_player.getObjectId());
		writeD(_playerPoints);
	}
}