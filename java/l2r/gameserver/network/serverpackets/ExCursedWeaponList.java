package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;

import java.util.List;

public class ExCursedWeaponList extends L2GameServerPacket
{
	private final List<Integer> _cursedWeaponIds;
	
	public ExCursedWeaponList(List<Integer> cursedWeaponIds)
	{
		_cursedWeaponIds = cursedWeaponIds;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x45);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x46);
				break;
			case GC:
			case SL:
				writeH(0x47);
				break;
		}
		
		writeD(_cursedWeaponIds.size());
		_cursedWeaponIds.forEach(this::writeD);
	}
}