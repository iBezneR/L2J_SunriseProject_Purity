package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExBaseAttributeCancelResult extends L2GameServerPacket
{
	private final int _objId;
	private final byte _attribute;
	
	public ExBaseAttributeCancelResult(int objId, byte attribute)
	{
		_objId = objId;
		_attribute = attribute;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x75);
				break;
			case GC:
			case SL:
				writeH(0x76);
				break;
		}
		
		writeD(0x01); // result
		writeD(_objId);
		writeD(_attribute);
	}
}
