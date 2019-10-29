package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.model.Location;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExCursedWeaponLocation extends L2GameServerPacket
{
	private final List<CursedWeaponInfo> _cursedWeaponInfo;
	
	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x46);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x47);
				break;
			case GC:
			case SL:
				writeH(0x48);
				break;
		}
		
		if (!_cursedWeaponInfo.isEmpty())
		{
			writeD(_cursedWeaponInfo.size());
			for (CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeD(w.id);
				writeD(w.activated);
				
				writeD(w.pos.getX());
				writeD(w.pos.getY());
				writeD(w.pos.getZ());
			}
		}
		else
		{
			writeD(0);
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case EPILOGUE:
				case FREYA:
				case H5:
					writeD(0);
					break;
			}
		}
	}
	
	public static class CursedWeaponInfo
	{
		public Location pos;
		public int id;
		public int activated; // 0 - not activated ? 1 - activated
		
		public CursedWeaponInfo(Location p, int ID, int status)
		{
			pos = p;
			id = ID;
			activated = status;
		}
	}
}