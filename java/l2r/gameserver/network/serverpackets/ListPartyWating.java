package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.PartyMatchRoom;
import l2r.gameserver.model.PartyMatchRoomList;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.List;

public class ListPartyWating extends L2GameServerPacket
{
	private final L2PcInstance _cha;
	private final int _loc;
	private final int _lim;
	private final List<PartyMatchRoom> _rooms;
	
	public ListPartyWating(L2PcInstance player, int auto, int location, int limit)
	{
		_cha = player;
		_loc = location;
		_lim = limit;
		_rooms = new ArrayList<>();
	}
	
	@Override
	protected final void writeImpl()
	{
		for (PartyMatchRoom room : PartyMatchRoomList.getInstance().getRooms())
		{
			if ((room.getMembers() < 1) || (room.getOwner() == null) || !room.getOwner().isOnline() || (room.getOwner().getPartyRoom() != room.getId()))
			{
				PartyMatchRoomList.getInstance().deleteRoom(room.getId());
				continue;
			}
			if ((_loc > 0) && (_loc != room.getLocation()))
			{
				continue;
			}
			if ((_lim == 0) && ((_cha.getLevel() < room.getMinLvl()) || (_cha.getLevel() > room.getMaxLvl())))
			{
				continue;
			}
			_rooms.add(room);
		}
		int size = _rooms.size();
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0x96);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x9C);
				break;
		}
		
		writeD(size > 0 ? 0x01 : 0x00);
		writeD(_rooms.size());
		for (PartyMatchRoom room : _rooms)
		{
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case IL:
				case GF:
				case EPILOGUE:
				case FREYA:
					writeD(room.getId());
					writeS(room.getTitle());
					writeD(room.getLocation());
					writeD(room.getMinLvl());
					writeD(room.getMaxLvl());
					writeD(room.getMembers());
					writeD(room.getMaxMembers());
					writeS(room.getOwner().getName());
					continue;
				case H5:
				case GC:
				case SL:
					writeD(room.getId());
					writeS(room.getTitle());
					writeD(room.getLocation());
					writeD(room.getMinLvl());
					writeD(room.getMaxLvl());
					writeD(room.getMaxMembers());
					writeS(room.getOwner().getName());
					writeD(room.getMembers());
					for (L2PcInstance member : room.getPartyMembers())
					{
						writeD(member != null ? member.getClassId().getId() : 0x00);
						writeS(member != null ? member.getName() : "Not Found");
					}
					continue;
			}
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(L2World.getInstance().getPartyCount()); // Helios
				writeD(L2World.getInstance().getPartyMemberCount()); // Helios
				break;
		}
	}
}
