package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExSpawnEmitter extends L2GameServerPacket
{
	private final int _playerObjectId;
	private final int _npcObjectId;
	
	public ExSpawnEmitter(int playerObjectId, int npcObjectId)
	{
		_playerObjectId = playerObjectId;
		_npcObjectId = npcObjectId;
	}
	
	public ExSpawnEmitter(L2PcInstance player, L2Npc npc)
	{
		this(player.getObjectId(), npc.getObjectId());
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
				writeH(0x5D);
				break;
			case GC:
			case SL:
				writeH(0x5E);
				break;
		}
		
		writeD(_npcObjectId);
		writeD(_playerObjectId);
		writeD(0x00); // ?
	}
}
