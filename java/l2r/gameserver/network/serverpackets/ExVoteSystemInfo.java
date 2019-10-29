package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExVoteSystemInfo extends L2GameServerPacket
{
	private final int _receivedRec, _givingRec, _bonusTimeLeft, _bonusPercent;
	private final boolean _showTimer;
	
	public ExVoteSystemInfo(L2PcInstance player)
	{
		_receivedRec = player.getRecomLeft();
		_givingRec = player.getRecomHave();
		_bonusTimeLeft = player.getRecomBonusTime();
		_bonusPercent = player.getRecomBonus();
		_showTimer = !player.isRecomTimerActive() || player.isHourglassEffected();
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case FREYA:
				writeH(0xC8);
				break;
			case H5:
				writeH(0xC9);
				break;
			case GC:
			case SL:
				writeH(0xCA);
				break;
		}
		
		writeD(_receivedRec);
		writeD(_givingRec);
		writeD(_bonusTimeLeft);
		writeD(_bonusPercent);
		writeD(_showTimer ? 0x01 : 0x00); // 0-show timer, 1-paused (if _bonusTime > 0) otherwise Quit
	}
}
