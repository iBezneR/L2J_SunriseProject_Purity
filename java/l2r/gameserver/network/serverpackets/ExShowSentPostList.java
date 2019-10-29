package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.instancemanager.MailManager;
import l2r.gameserver.model.entity.Message;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExShowSentPostList extends L2GameServerPacket
{
	private final List<Message> _outbox;
	
	public ExShowSentPostList(int objectId)
	{
		_outbox = MailManager.getInstance().getOutbox(objectId);
	}
	
	@Override
	protected void writeImpl()
	{
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case IL:
			case GF:
				return;
		}
		
		writeC(0xFE);
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case EPILOGUE:
			case FREYA:
			case H5:
				writeH(0xAC);
				break;
			case GC:
			case SL:
				writeH(0xAD);
				break;
		}
		
		writeD((int) (System.currentTimeMillis() / 1000));
		if ((_outbox != null) && (_outbox.size() > 0))
		{
			writeD(_outbox.size());
			for (Message msg : _outbox)
			{
				writeD(msg.getId());
				writeS(msg.getSubject());
				writeS(msg.getReceiverName());
				writeD(msg.isLocked() ? 0x01 : 0x00);
				writeD(msg.getExpirationSeconds());
				writeD(msg.isUnread() ? 0x01 : 0x00);
				writeD(0x01);
				writeD(msg.hasAttachments() ? 0x01 : 0x00);
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GC:
					case SL:
						writeD(0x00);
						break;
				}
			}
		}
		else
		{
			writeD(0x00);
		}
	}
}
