package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	private final int _playerId;
	private final List<L2Effect> _effects = new ArrayList<>();
	
	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		_playerId = player.getObjectId();
	}
	
	public void addSkill(L2Effect info)
	{
		_effects.add(info);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
				writeH(0x2A);
				break;
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0x7B);
				break;
			case GC:
			case SL:
				writeH(0x7C);
				break;
		}
		
		writeD(_playerId);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
			case EPILOGUE:
			case FREYA:
			case H5:
				writeD(_effects.size());
				for (L2Effect info : _effects)
				{
					if ((info != null) && info.getInUse())
					{
						writeD(info.getSkill().getDisplayId());
						writeH(info.getSkill().getDisplayLevel());
						writeD(info.getRemainingTime());
					}
				}
				break;
			case GC:
			case SL:
				writeD(_effects.size());
				for (L2Effect info : _effects)
				{
					writeD(info.getSkill().getDisplayId());
					writeH(info.getSkill().getDisplayLevel());
					writeH(info.getSkill().getSubLevel());
					writeD(0/* info.getSkill().getAbnormalType().getClientId() */); // TODO
					writeOptionalD(info.getSkill().isAura() ? -1 : info.getRemainingTime());
				}
				break;
		}
	}
}
