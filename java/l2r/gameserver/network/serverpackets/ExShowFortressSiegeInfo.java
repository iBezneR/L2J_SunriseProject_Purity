package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.instancemanager.FortSiegeManager;
import l2r.gameserver.model.FortSiegeSpawn;
import l2r.gameserver.model.entity.Fort;

import java.util.List;

public class ExShowFortressSiegeInfo extends L2GameServerPacket
{
	private final int _fortId;
	private final int _size;
	private final Fort _fort;
	private int _csize;
	private final int _csize2;
	
	public ExShowFortressSiegeInfo(Fort fort)
	{
		_fort = fort;
		_fortId = fort.getResidenceId();
		_size = fort.getFortSize();
		List<FortSiegeSpawn> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(_fortId);
		if (commanders != null)
		{
			_csize = commanders.size();
		}
		_csize2 = _fort.getSiege().getCommanders().size();
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
			case GC:
			case SL:
				writeH(0x17);
				break;
		}
		
		writeD(_fortId); // Fortress Id
		writeD(_size); // Total Barracks Count
		if (_csize > 0)
		{
			switch (_csize)
			{
				case 3:
					switch (_csize2)
					{
						case 0:
							writeD(0x03);
							break;
						case 1:
							writeD(0x02);
							break;
						case 2:
							writeD(0x01);
							break;
						case 3:
							writeD(0x00);
							break;
					}
					break;
				case 4: // TODO: change 4 to 5 once control room supported
					switch (_csize2)
					// TODO: once control room supported, update writeD(0x0x) to support 5th room
					{
						case 0:
							writeD(0x05);
							break;
						case 1:
							writeD(0x04);
							break;
						case 2:
							writeD(0x03);
							break;
						case 3:
							writeD(0x02);
							break;
						case 4:
							writeD(0x01);
							break;
					}
					break;
			}
		}
		else
		{
			for (int i = 0; i < _size; i++)
			{
				writeD(0x00);
			}
		}
	}
}
