package handlers.itemhandlers;

import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExChooseInventoryAttributeItem;

public class EnchantAttribute implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		if (activeChar.isCastingNow())
		{
			return false;
		}
		
		if (activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.ENCHANTMENT_ALREADY_IN_PROGRESS);
			return false;
		}
		
		// vGodFather: missing check allowing spam attribute stones
		if (activeChar.getActiveEnchantAttrItemId() != L2PcInstance.ID_NONE)
		{
			return false;
		}
		
		activeChar.setActiveEnchantAttrItemId(item.getObjectId());
		activeChar.sendPacket(new ExChooseInventoryAttributeItem(activeChar, item));
		return true;
	}
}
