package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExCubeGameChangeTeam extends L2GameServerPacket
{
	L2PcInstance _player;
	boolean _fromRedTeam;
	
	public ExCubeGameChangeTeam(L2PcInstance player, boolean fromRedTeam)
	{
		_player = player;
		_fromRedTeam = fromRedTeam;
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
				writeH(0x97);
				break;
			case GC:
			case SL:
				writeH(0x98);
				break;
		}
		
		writeD(0x05);
		
		writeD(_player.getObjectId());
		writeD(_fromRedTeam ? 0x01 : 0x00);
		writeD(_fromRedTeam ? 0x00 : 0x01);
	}
}