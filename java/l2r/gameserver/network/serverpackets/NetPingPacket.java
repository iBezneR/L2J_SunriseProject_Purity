package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author vGodFather
 */
public class NetPingPacket extends L2GameServerPacket
{
	private final int _objId;
	
	public NetPingPacket(L2PcInstance cha)
	{
		_objId = cha.getObjectId();
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xD3);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xD9);
				break;
		}
		
		writeD(_objId);
	}
}