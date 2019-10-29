package l2r.gameserver.network.serverpackets;

import gr.sr.network.handler.ServerTypeConfigs;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.olympiad.Participant;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private Participant _par = null;
	private int _curHp;
	private int _maxHp;
	private int _curCp;
	private int _maxCp;
	
	public ExOlympiadUserInfo(L2PcInstance player)
	{
		_player = player;
		if (_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = _player.getMaxHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}
	
	public ExOlympiadUserInfo(Participant par)
	{
		_par = par;
		_player = par.getPlayer();
		if (_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = _player.getMaxHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x29);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x7A);
				break;
			case GC:
			case SL:
				writeH(0x7B);
				break;
		}
		
		if (_player != null)
		{
			writeC(_player.getOlympiadSide());
			writeD(_player.getObjectId());
			writeS(_player.getName());
			writeD(_player.getClassId().getId());
		}
		else
		{
			writeC(_par.getSide());
			writeD(_par.getObjectId());
			writeS(_par.getName());
			writeD(_par.getBaseClass());
		}
		
		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curCp);
		writeD(_maxCp);
	}
}