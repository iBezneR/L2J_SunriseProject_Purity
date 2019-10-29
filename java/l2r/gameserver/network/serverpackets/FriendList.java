package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.data.sql.CharNameTable;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.network.handler.ServerTypeConfigs;

public class FriendList extends L2GameServerPacket
{
	private final List<FriendInfo> _info;
	
	private static class FriendInfo
	{
		int _objId;
		String _name;
		int _level;
		int _classId;
		boolean _online;
		
		public FriendInfo(int objId, String name, boolean online, int level, int classId)
		{
			_objId = objId;
			_name = name;
			_online = online;
			_level = level;
			_classId = classId;
		}
	}
	
	public FriendList(L2PcInstance player)
	{
		_info = new ArrayList<>(player.getFriendList().size());
		for (int objId : player.getFriendList())
		{
			final String name = CharNameTable.getInstance().getNameById(objId);
			final L2PcInstance player1 = L2World.getInstance().getPlayer(objId);
			boolean online = false;
			int level = 0;
			int classId = 0;
			
			if (player1 != null)
			{
				online = true;
				level = player1.getLevel();
				classId = player1.getClassId().getId();
			}
			else
			{
				level = CharNameTable.getInstance().getLevelById(objId);
				classId = CharNameTable.getInstance().getClassIdById(objId);
			}
			
			_info.add(new FriendInfo(objId, name, online, level, classId));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeC(0xFA);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
			case GC:
			case SL:
				writeC(0x75);
				break;
		}
		
		writeD(_info.size());
		for (FriendInfo info : _info)
		{
			writeD(info._objId); // character id
			writeS(info._name);
			writeD(info._online ? 0x01 : 0x00); // online
			writeD(info._online ? info._objId : 0x00); // object id if online
			
			switch (ServerTypeConfigs.SERVER_TYPE)
			{
				case GC:
				case SL:
					writeD(info._level);
					writeD(info._classId);
					writeH(0x00);
					break;
			}
		}
	}
}
