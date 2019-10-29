package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.instancemanager.MailManager;
import l2r.gameserver.model.entity.Message;

import gr.sr.network.handler.ServerTypeConfigs;

public class ExShowReceivedPostList extends L2GameServerPacket
{
	private final List<Message> _inbox;
	private static final int MESSAGE_FEE = 100;
	private static final int MESSAGE_FEE_PER_SLOT = 1000;
	
	public ExShowReceivedPostList(int objectId)
	{
		_inbox = MailManager.getInstance().getInbox(objectId);
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
				writeH(0xAA);
				break;
			case GC:
			case SL:
				writeH(0xAB);
				break;
		}
		
		writeD((int) (System.currentTimeMillis() / 1000));
		if ((_inbox != null) && (_inbox.size() > 0))
		{
			writeD(_inbox.size());
			for (Message msg : _inbox)
			{
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case GC:
					case SL:
						writeD(0); // msg.getMailType().ordinal()
						// if (msg.getMailType() == MailType.COMMISSION_ITEM_SOLD)
						// {
						// writeD(SystemMessageId.THE_ITEM_YOU_REGISTERED_HAS_BEEN_SOLD.getId());
						// }
						// else if (msg.getMailType() == MailType.COMMISSION_ITEM_RETURNED)
						// {
						// writeD(SystemMessageId.THE_REGISTRATION_PERIOD_FOR_THE_ITEM_YOU_REGISTERED_HAS_EXPIRED.getId());
						// }
						break;
				}
				
				writeD(msg.getId());
				writeS(msg.getSubject());
				writeS(msg.getSenderName());
				writeD(msg.isLocked() ? 0x01 : 0x00);
				writeD(msg.getExpirationSeconds());
				writeD(msg.isUnread() ? 0x01 : 0x00);
				writeD(0x01);
				writeD(msg.hasAttachments() ? 0x01 : 0x00);
				writeD(msg.isReturned() ? 0x01 : 0x00);
				
				switch (ServerTypeConfigs.SERVER_TYPE)
				{
					case EPILOGUE:
					case FREYA:
					case H5:
						writeD(msg.getSendBySystem());
						break;
				}
				
				writeD(0x00);
			}
		}
		else
		{
			writeD(0x00);
		}
		
		switch (ServerTypeConfigs.SERVER_TYPE)
		{
			case GC:
			case SL:
				writeD(MESSAGE_FEE);
				writeD(MESSAGE_FEE_PER_SLOT);
				break;
		}
	}
}
