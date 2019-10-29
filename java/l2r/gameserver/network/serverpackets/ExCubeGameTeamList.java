package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExCubeGameTeamList extends L2GameServerPacket
{
	// Players Lists
	List<L2PcInstance> _bluePlayers;
	List<L2PcInstance> _redPlayers;
	
	// Common Values
	int _roomNumber;
	
	public ExCubeGameTeamList(List<L2PcInstance> redPlayers, List<L2PcInstance> bluePlayers, int roomNumber)
	{
		_redPlayers = redPlayers;
		_bluePlayers = bluePlayers;
		_roomNumber = roomNumber - 1;
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
		
		writeD(0x00);
		
		writeD(_roomNumber);
		writeD(0xffffffff);
		
		writeD(_bluePlayers.size());
		for (L2PcInstance player : _bluePlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
		}
		writeD(_redPlayers.size());
		for (L2PcInstance player : _redPlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
		}
	}
}