package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.TradeItem;

public final class TradeOtherAdd extends AbstractItemPacket
{
	private final TradeItem _item;
	
	public TradeOtherAdd(TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x21);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x1B);
				break;
		}
		
		writeH(1); // item count
		writeH(0);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeItem(_item, true);
				break;
			case GC:
			case SL:
				writeItem(_item, false);
				break;
		}
	}
}
