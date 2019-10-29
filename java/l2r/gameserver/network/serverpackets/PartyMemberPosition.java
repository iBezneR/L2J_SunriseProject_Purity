package l2r.gameserver.network.serverpackets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.network.handler.ServerTypeConfigs;

public class PartyMemberPosition extends L2GameServerPacket
{
	private final Map<Integer, Location> locations = new ConcurrentHashMap<>();
	
	public PartyMemberPosition(L2Party party)
	{
		reuse(party);
	}
	
	public void reuse(L2Party party)
	{
		locations.clear();
		for (L2PcInstance member : party.getMembers())
		{
			if (member == null)
			{
				continue;
			}
			locations.put(member.getObjectId(), member.getLocation());
		}
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xA7);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0xBA);
				break;
		}
		
		writeD(locations.size());
		for (Map.Entry<Integer, Location> entry : locations.entrySet())
		{
			Location loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
}
