package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2FenceInstance;

public class ExColosseumFenceInfoPacket extends L2GameServerPacket
{
	private final L2FenceInstance _fence;
	
	public ExColosseumFenceInfoPacket(L2FenceInstance fence)
	{
		_fence = fence;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x09);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x03);
				break;
		}
		
		writeD(_fence.getObjectId());
		writeD(_fence.getType());
		writeD(_fence.getX());
		writeD(_fence.getY());
		writeD(_fence.getZ());
		writeD(_fence.getWidth());
		writeD(_fence.getLength());
	}
}
