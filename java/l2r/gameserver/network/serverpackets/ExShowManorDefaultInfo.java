package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.model.L2Seed;

import gr.sr.network.handler.ServerTypeConfigs;

public final class ExShowManorDefaultInfo extends L2GameServerPacket
{
	private final List<L2Seed> _crops;
	private final boolean _hideButtons;
	
	public ExShowManorDefaultInfo(boolean hideButtons)
	{
		_crops = CastleManorManager.getInstance().getCrops();
		_hideButtons = hideButtons;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x1E);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeH(0x25);
				break;
		}
		
		writeC(_hideButtons ? 0x01 : 0x00); // Hide "Seed Purchase" and "Crop Sales" buttons
		writeD(_crops.size());
		for (L2Seed crop : _crops)
		{
			writeD(crop.getCropId()); // crop Id
			writeD(crop.getLevel()); // level
			writeD(crop.getSeedReferencePrice()); // seed price
			writeD(crop.getCropReferencePrice()); // crop price
			writeC(1); // Reward 1 type
			writeD(crop.getReward(1)); // Reward 1 itemId
			writeC(1); // Reward 2 type
			writeD(crop.getReward(2)); // Reward 2 itemId
		}
	}
}