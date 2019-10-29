/*
 * Copyright (C) 2004-2014 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.SellBuff;

import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.handler.BypassHandler;
import l2r.gameserver.handler.IBypassHandler;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.handler.VoicedCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.events.AbstractScript;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.util.Util;

import java.util.StringTokenizer;

/**
 * Sell Buffs voice command
 * @author St3eT
 */
public class SellBuff implements IVoicedCommandHandler, IBypassHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"sellbuff",
		"sellbuffs",
	};
	
	private static final String[] BYPASS_COMMANDS =
	{
		"sellbuffadd",
		"sellbuffaddskill",
		"sellbuffedit",
		"sellbuffchangeprice",
		"sellbuffremove",
		"sellbuffbuymenu",
		"sellbuffbuyskill",
		"sellbuffstart",
		"sellbuffstop",
	};
	
	private SellBuff()
	{
		if (SellBuffsManager.SELLBUFF_ENABLED)
		{
			BypassHandler.getInstance().registerHandler(this);
			VoicedCommandHandler.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		String cmd = "";
		String params = "";
		final StringTokenizer st = new StringTokenizer(command, " ");
		
		if (st.hasMoreTokens())
		{
			cmd = st.nextToken();
		}
		
		while (st.hasMoreTokens())
		{
			params += st.nextToken() + (st.hasMoreTokens() ? " " : "");
		}
		
		if (cmd.isEmpty())
		{
			return false;
		}
		return useBypass(cmd, activeChar, params);
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		switch (command)
		{
			case "sellbuff":
			case "sellbuffs":
			{
				SellBuffsManager.getInstance().sendSellMenu(activeChar);
				break;
			}
		}
		return true;
	}
	
	public boolean useBypass(String command, L2PcInstance activeChar, String params)
	{
		if (!SellBuffsManager.SELLBUFF_ENABLED)
		{
			return false;
		}
		
		switch (command)
		{
			case "sellbuffstart":
			{
				if (SellBuffsManager.getInstance().isSellingBuffs(activeChar) || (params == null) || params.isEmpty())
				{
					return false;
				}
				else if (SellBuffsManager.getInstance().getSellingBuffs(activeChar).isEmpty())
				{
					activeChar.sendMessage("Your list of buffs is empty, please add some buffs first!");
					return false;
				}
				else
				{
					String title = "BUFF SELL: ";
					final StringTokenizer st = new StringTokenizer(params, " ");
					while (st.hasMoreTokens())
					{
						title += st.nextToken() + " ";
					}
					
					if (title.length() > 40)
					{
						activeChar.sendMessage("Your title cannot exceed 29 characters in length. Please try again.");
						return false;
					}
					
					SellBuffsManager.getInstance().startSellBuffs(activeChar, title);
				}
				break;
			}
			case "sellbuffstop":
			{
				if (SellBuffsManager.getInstance().isSellingBuffs(activeChar))
				{
					SellBuffsManager.getInstance().stopSellBuffs(activeChar);
				}
				break;
			}
			case "sellbuffadd":
			{
				if (!SellBuffsManager.getInstance().isSellingBuffs(activeChar))
				{
					int index = 0;
					if ((params != null) && !params.isEmpty() && Util.isDigit(params))
					{
						index = Integer.parseInt(params);
					}
					
					SellBuffsManager.getInstance().sendBuffChoiceMenu(activeChar, index);
				}
				break;
			}
			case "sellbuffedit":
			{
				if (!SellBuffsManager.getInstance().isSellingBuffs(activeChar))
				{
					SellBuffsManager.getInstance().sendBuffEditMenu(activeChar);
				}
				break;
			}
			case "sellbuffchangeprice":
			{
				if (!SellBuffsManager.getInstance().isSellingBuffs(activeChar) && (params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					
					int skillId = -1;
					int price = -1;
					
					if (st.hasMoreTokens())
					{
						skillId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						try
						{
							price = Integer.parseInt(st.nextToken());
						}
						catch (NumberFormatException e)
						{
							activeChar.sendMessage("Too big price! Maximal price is " + SellBuffsManager.SELLBUFF_MAX_PRICE);
							SellBuffsManager.getInstance().sendBuffEditMenu(activeChar);
						}
					}
					
					if ((skillId == -1) || (price == -1))
					{
						return false;
					}
					
					final L2Skill skillToChange = activeChar.getKnownSkill(skillId);
					if (skillToChange == null)
					{
						return false;
					}
					
					final SellBuffHolder holder = SellBuffsManager.getInstance().getSellingBuffs(activeChar).stream().filter(h -> (h.getSkillId() == skillToChange.getId())).findFirst().orElse(null);
					if ((holder != null))
					{
						activeChar.sendMessage("Price of " + activeChar.getKnownSkill(holder.getSkillId()).getName() + " has been changed to " + price + "!");
						holder.setPrice(price);
						SellBuffsManager.getInstance().sendBuffEditMenu(activeChar);
					}
				}
				break;
			}
			case "sellbuffremove":
			{
				if (!SellBuffsManager.getInstance().isSellingBuffs(activeChar) && (params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					
					int skillId = -1;
					
					if (st.hasMoreTokens())
					{
						skillId = Integer.parseInt(st.nextToken());
					}
					
					if ((skillId == -1))
					{
						return false;
					}
					
					final L2Skill skillToRemove = activeChar.getKnownSkill(skillId);
					if (skillToRemove == null)
					{
						return false;
					}
					
					final SellBuffHolder holder = SellBuffsManager.getInstance().getSellingBuffs(activeChar).stream().filter(h -> (h.getSkillId() == skillToRemove.getId())).findFirst().orElse(null);
					if ((holder != null) && SellBuffsManager.getInstance().getSellingBuffs(activeChar).remove(holder))
					{
						activeChar.sendMessage("L2Skill " + activeChar.getKnownSkill(holder.getSkillId()).getName() + " has been removed!");
						SellBuffsManager.getInstance().sendBuffEditMenu(activeChar);
					}
				}
				break;
			}
			case "sellbuffaddskill":
			{
				if (!SellBuffsManager.getInstance().isSellingBuffs(activeChar) && (params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					
					int skillId = -1;
					long price = -1;
					
					if (st.hasMoreTokens())
					{
						skillId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						try
						{
							price = Integer.parseInt(st.nextToken());
						}
						catch (NumberFormatException e)
						{
							activeChar.sendMessage("Too big price! Maximal price is " + SellBuffsManager.SELLBUFF_MIN_PRICE);
							SellBuffsManager.getInstance().sendBuffEditMenu(activeChar);
						}
					}
					
					if ((skillId == -1) || (price == -1))
					{
						return false;
					}
					
					final L2Skill skillToAdd = activeChar.getKnownSkill(skillId);
					if (skillToAdd == null)
					{
						return false;
					}
					else if (price < SellBuffsManager.SELLBUFF_MIN_PRICE)
					{
						activeChar.sendMessage("Too small price! Minimal price is " + SellBuffsManager.SELLBUFF_MIN_PRICE);
						return false;
					}
					else if (price > SellBuffsManager.SELLBUFF_MAX_PRICE)
					{
						activeChar.sendMessage("Too big price! Maximal price is " + SellBuffsManager.SELLBUFF_MAX_PRICE);
						return false;
					}
					else if (SellBuffsManager.getInstance().getSellingBuffs(activeChar).size() >= SellBuffsManager.SELLBUFF_MAX_BUFFS)
					{
						activeChar.sendMessage("You already reached max count of buffs! Max buffs is: " + SellBuffsManager.SELLBUFF_MAX_BUFFS);
						return false;
					}
					else if (!SellBuffsManager.getInstance().isInSellList(activeChar, skillToAdd))
					{
						SellBuffsManager.getInstance().getSellingBuffs(activeChar).add(new SellBuffHolder(skillToAdd.getId(), price));
						activeChar.sendMessage(skillToAdd.getName() + " has been added!");
						SellBuffsManager.getInstance().sendBuffChoiceMenu(activeChar, 0);
					}
				}
				break;
			}
			case "sellbuffbuymenu":
			{
				if ((params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					
					int objId = -1;
					int index = 0;
					if (st.hasMoreTokens())
					{
						objId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						index = Integer.parseInt(st.nextToken());
					}
					
					final L2PcInstance seller = L2World.getInstance().getPlayer(objId);
					if (seller != null)
					{
						if (!SellBuffsManager.getInstance().isSellingBuffs(seller) || !activeChar.isInsideRadius(seller, L2Npc.INTERACTION_DISTANCE, true, true))
						{
							return false;
						}
						
						SellBuffsManager.getInstance().sendBuffMenu(activeChar, seller, index);
					}
				}
				break;
			}
			case "sellbuffbuyskill":
			{
				if ((params != null) && !params.isEmpty())
				{
					final StringTokenizer st = new StringTokenizer(params, " ");
					int objId = -1;
					int skillId = -1;
					int index = 0;
					
					if (st.hasMoreTokens())
					{
						objId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						skillId = Integer.parseInt(st.nextToken());
					}
					
					if (st.hasMoreTokens())
					{
						index = Integer.parseInt(st.nextToken());
					}
					
					if ((skillId == -1) || (objId == -1))
					{
						return false;
					}
					
					final L2PcInstance seller = L2World.getInstance().getPlayer(objId);
					if (seller == null)
					{
						return false;
					}
					
					final L2Skill skillToBuy = seller.getKnownSkill(skillId);
					if (!SellBuffsManager.getInstance().isSellingBuffs(seller) || !Util.checkIfInRange(L2Npc.INTERACTION_DISTANCE, activeChar, seller, true) || (skillToBuy == null))
					{
						return false;
					}
					
					if (seller.getCurrentMp() < (skillToBuy.getMpConsume() * SellBuffsManager.SELLBUFF_MP_MULTIPLER))
					{
						activeChar.sendMessage(seller.getName() + " has no enough mana for " + skillToBuy.getName() + "!");
						SellBuffsManager.getInstance().sendBuffMenu(activeChar, seller, index);
						return false;
					}
					
					final SellBuffHolder holder = SellBuffsManager.getInstance().getSellingBuffs(seller).stream().filter(h -> (h.getSkillId() == skillToBuy.getId())).findFirst().orElse(null);
					if (holder != null)
					{
						//@formatter:off
						final boolean isFree = (SellBuffsManager.SELLBUFF_FREE_FOR_SAME_IP && activeChar.getIPAddress().equals(seller.getIPAddress())) 
											|| (SellBuffsManager.SELLBUFF_FREE_FOR_SAME_CLAN && (activeChar.getClanId() > 0) && (activeChar.getClanId() == seller.getClanId()))
											|| (SellBuffsManager.SELLBUFF_FREE_FOR_FRIENDLIST && activeChar.getFriendList().contains(seller.getObjectId()));
						//@formatter:on
						if (isFree)
						{
							seller.reduceCurrentMp(skillToBuy.getMpConsume() * SellBuffsManager.SELLBUFF_MP_MULTIPLER);
							skillToBuy.getEffects(seller, activeChar);
						}
						else
						{
							if (AbstractScript.getQuestItemsCount(activeChar, SellBuffsManager.SELLBUFF_PAYMENT_ID) >= holder.getPrice())
							{
								AbstractScript.takeItems(activeChar, SellBuffsManager.SELLBUFF_PAYMENT_ID, holder.getPrice());
								AbstractScript.giveItems(seller, SellBuffsManager.SELLBUFF_PAYMENT_ID, holder.getPrice());
								seller.reduceCurrentMp(skillToBuy.getMpConsume() * SellBuffsManager.SELLBUFF_MP_MULTIPLER);
								skillToBuy.getEffects(seller, activeChar);
							}
							else
							{
								final L2Item item = ItemData.getInstance().getTemplate(SellBuffsManager.SELLBUFF_PAYMENT_ID);
								if (item != null)
								{
									activeChar.sendMessage("Not enough " + item.getName() + "!");
								}
								else
								{
									activeChar.sendMessage("Not enough items!");
								}
							}
						}
					}
					SellBuffsManager.getInstance().sendBuffMenu(activeChar, seller, index);
				}
				break;
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	@Override
	public String[] getBypassList()
	{
		return BYPASS_COMMANDS;
	}
	
	public static void main(String[] args)
	{
		new SellBuff();
	}
}