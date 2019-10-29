package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class OnEventTrigger extends L2GameServerPacket
{
	private final int _emitterId;
	private final int _enabled;
	
	public OnEventTrigger(int id, boolean enabled)
	{
		_emitterId = id;
		_enabled = enabled ? 1 : 0;
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
			case H5:
			case GC:
			case SL:
				writeC(0xCD);
				break;
		}
		
		writeD(_emitterId);
		writeC(_enabled);
	}
}