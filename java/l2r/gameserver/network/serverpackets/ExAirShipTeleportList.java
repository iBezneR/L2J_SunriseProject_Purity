package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.VehiclePathPoint;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExAirShipTeleportList extends L2GameServerPacket
{
	private final int _dockId;
	private final VehiclePathPoint[][] _teleports;
	private final int[] _fuelConsumption;
	
	public ExAirShipTeleportList(int dockId, VehiclePathPoint[][] teleports, int[] fuelConsumption)
	{
		_dockId = dockId;
		_teleports = teleports;
		_fuelConsumption = fuelConsumption;
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
				writeH(0x9A);
				break;
			case GC:
			case SL:
				writeH(0x9B);
				break;
		}
		
		writeD(_dockId);
		if (_teleports != null)
		{
			writeD(_teleports.length);
			
			VehiclePathPoint[] path;
			VehiclePathPoint dst;
			for (int i = 0; i < _teleports.length; i++)
			{
				writeD(i - 1);
				writeD(_fuelConsumption[i]);
				path = _teleports[i];
				dst = path[path.length - 1];
				writeD(dst.getX());
				writeD(dst.getY());
				writeD(dst.getZ());
			}
		}
		else
		{
			writeD(0);
		}
	}
}