package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExVariationResult extends L2GameServerPacket
{
	private final int _stat12;
	private final int _stat34;
	private final int _unk3;
	
	public ExVariationResult(int unk1, int unk2, int unk3)
	{
		_stat12 = unk1;
		_stat34 = unk2;
		_unk3 = unk3;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x55);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x56);
				break;
			case GC:
			case SL:
				writeH(0x57);
				break;
		}
		
		writeD(_stat12);
		writeD(_stat34);
		writeD(_unk3);
	}
}