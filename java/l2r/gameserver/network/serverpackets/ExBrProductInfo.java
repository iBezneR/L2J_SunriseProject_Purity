package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.data.xml.impl.ProductItemData;
import l2r.gameserver.model.primeshop.L2ProductItem;
import l2r.gameserver.model.primeshop.L2ProductItemComponent;

/**
 * @author vGodFather
 */
public class ExBrProductInfo extends L2GameServerPacket
{
	private final L2ProductItem _productId;
	
	public ExBrProductInfo(int id)
	{
		_productId = ProductItemData.getInstance().getProduct(id);
	}
	
	@Override
	protected void writeImpl()
	{
		if (_productId == null)
		{
			return;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
				writeH(0xA9);
				break;
			case EPILOGUE:
				writeH(0xBA);
				break;
			case FREYA:
				writeH(0xCB);
				break;
			case H5:
				writeH(0xD7);
				break;
			case GC:
			case SL:
				writeH(0xD8);
				break;
		}
		
		writeD(_productId.getProductId());
		writeD(_productId.getPoints());
		writeD(_productId.getComponents().size());
		
		for (L2ProductItemComponent com : _productId.getComponents())
		{
			writeD(com.getId());
			writeD(com.getCount());
			writeD(com.getWeight());
			writeD(com.isDropable() ? 1 : 0);
		}
	}
}