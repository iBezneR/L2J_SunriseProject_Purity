package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.Location;
import l2r.gameserver.model.interfaces.ILocational;

import gr.sr.network.handler.ServerTypeConfigs;

public final class ExShowTrace extends L2GameServerPacket
{
	private final List<Location> _locations = new ArrayList<>();
	
	public void addLocation(int x, int y, int z)
	{
		_locations.add(new Location(x, y, z));
	}
	
	public void addLocation(ILocational loc)
	{
		addLocation(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x67);
				break;
			case GC:
			case SL:
				writeH(0x68);
				break;
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case H5:
			case GC:
			case SL:
				writeH(0); // type broken in H5
				writeD(0); // time broken in H5
				break;
		}
		
		writeH(_locations.size());
		for (Location loc : _locations)
		{
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GF:
				case EPILOGUE:
				case FREYA:
					writeH(0);
					break;
			}
		}
	}
}
