/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.erengine;

import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.util.Rnd;
import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * @author Erlandys
 */

public class ErReward extends ErObject
{
	boolean _party;

	public ErReward(int id, ErObjectType type, int itemId, int minCount, int maxCount, double chance, boolean party, String icon)
	{
		super(id, type, itemId, minCount, maxCount, chance, icon);
		_party = party;
	}

	public boolean isForParty()
	{
		return _party;
	}

	public void isForParty(boolean party)
	{
		_party = party;
	}

	public void giveReward(L2PcInstance player)
	{
		if (getType().equals(ErObjectType.Item))
		{
			giveItems(player);
		}
		else if (getType().equals(ErObjectType.ClanPoints))
		{
			giveClanPoints(player);
		}
		else if (getType().equals(ErObjectType.SkillPoints))
		{
			giveSkillPoints(player);
		}
		else if (getType().equals(ErObjectType.Experience))
		{
			giveExperience(player);
		}
	}

	private void giveClanPoints(L2PcInstance player)
	{
		if (player.getClan() == null)
		{
			return;
		}
		double _chance = getChance() * 1000;
		int min = getMinCount();
		int max = getMaxCount();
		if (min > max)
		{
			max = min;
		}
		int count = Rnd.get(min, max);
		if (Rnd.get(0, 100000) < _chance)
		{
			if (player.getClan() != null)
			{
				player.getClan().addReputationScore(count, true);
			}
		}
	}

	private void giveSkillPoints(L2PcInstance player)
	{
		double _chance = getChance() * 1000;
		int min = getMinCount();
		int max = getMaxCount();
		if (min > max)
		{
			max = min;
		}
		int count = Rnd.get(min, max);
		if (Rnd.get(0, 100000) <= _chance)
		{
			player.getStat().addExpAndSp(0, count);
		}
	}

	private void giveExperience(L2PcInstance player)
	{
		double _chance = getChance() * 1000;
		int min = getMinCount();
		int max = getMaxCount();
		if (min > max)
		{
			max = min;
		}
		long count = Rnd.get(min, max);
		if (Rnd.get(0, 100000) <= _chance)
		{
			player.getStat().addExpAndSp(count, 0);
		}
	}

	private void giveItems(L2PcInstance player)
	{
		double _chance = getChance() * 1000;
		int _itemId = getItemId();
		int min = getMinCount();
		int max = getMaxCount();
		if (min > max)
		{
			max = min;
		}
		int count = Rnd.get(min, max);
		if (Rnd.get(0, 100000) < _chance)
		{
			L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), _itemId);
			item.setCount(count);
			player.addItem("RewardItem", item, player, true);
		}
	}

	@Override
	public String toString()
	{
		return "type: " + _type.toString() + ", itemId: " + _itemId + ", min: " + _minCount + ", max: " + _maxCount + ", chance: " + _chance + ", forParty: " + _party;
	}

	public static int readReward(Node a, ArrayList<ErReward> rewards, int rewardId, boolean withParty)
	{
		ErObjectType rewardType = null;
		int itemId = 0, minCount = 0, maxCount = 0;
		double chance = 0;
		boolean party = false;
		String icon = "icon.NOIMAGE";
		if (a.getAttributes().getNamedItem("type") != null)
		{
			String r = a.getAttributes().getNamedItem("type").getNodeValue();
			try {
				rewardType = ErObjectType.valueOf(r);
			}
			catch (EnumConstantNotPresentException ecnpe)
			{
				System.out.println("Reward type: [" + r + "] does not exist!");
				return -1;
			}
		}
		if (a.getAttributes().getNamedItem("id") != null)
		{
			itemId = Integer.parseInt(a.getAttributes().getNamedItem("id").getNodeValue());
		}
		if (a.getAttributes().getNamedItem("min") != null)
		{
			minCount = Integer.parseInt(a.getAttributes().getNamedItem("min").getNodeValue());
		}
		if (a.getAttributes().getNamedItem("max") != null)
		{
			maxCount = Integer.parseInt(a.getAttributes().getNamedItem("max").getNodeValue());
		}
		if (a.getAttributes().getNamedItem("chance") != null)
		{
			chance = Double.parseDouble(a.getAttributes().getNamedItem("chance").getNodeValue());
		}
		if (withParty && a.getAttributes().getNamedItem("party") != null)
		{
			party = a.getAttributes().getNamedItem("party").getNodeValue().equalsIgnoreCase("true");
		}
		if (a.getAttributes().getNamedItem("icon") != null)
		{
			icon = a.getAttributes().getNamedItem("icon").getNodeValue();
		}
		rewards.add(new ErReward(rewardId, rewardType, itemId, minCount, maxCount, chance, party, icon));
		return ++rewardId;
	}
}