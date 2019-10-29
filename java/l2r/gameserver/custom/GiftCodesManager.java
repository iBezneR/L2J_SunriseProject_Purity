/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.custom;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.handler.VoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Erlandys
 */
public class GiftCodesManager
{
	protected static final Logger _log = Logger.getLogger(GiftCodesManager.class.getName());
	ArrayList<String> _giftCodes;
	
	public GiftCodesManager()
	{
		_giftCodes = new ArrayList<>();
		load();
		VoicedCommandHandler.getInstance().registerHandler(new GiftCode());
	}
	
	public void load()
	{
		_giftCodes.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             Statement s = con.createStatement();
             ResultSet rs = s.executeQuery("SELECT code FROM giftcodes"))
		{
			while (rs.next())
			{
				_giftCodes.add(rs.getString("code"));
			}
			_log.info(getClass().getSimpleName() + ": Loaded: " + _giftCodes.size() + " gift codes");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception: GiftCodesManager.load(): " + e.getMessage(), e);
		}
	}
	
	public void giveGift(L2PcInstance player, String giftCode)
	{
		if (!_giftCodes.contains(giftCode))
		{
			player.sendMessage("This gift code does not exist!");
			return;
		}
		if (Config.GIFT_CODES_TYPES.get(giftCode.length()))
		{
			String uCodes[] = player.getVariables().getString("UsedGiftCodes", "").split(";");
			if (uCodes.length > 0)
			{
				for (String c : uCodes)
				{
					if (c.length() < 1)
					{
						continue;
					}
					if (Integer.parseInt(c) == giftCode.length())
					{
						player.sendMessage("You have already used this type of code!");
						return;
					}
				}
			}
		}
		
		int[] reward = Config.GIFT_CODES_REWARDS.get(giftCode.length()).get(Rnd.get(Config.GIFT_CODES_REWARDS.get(giftCode.length()).size()));
		player.sendMessage("Congratulations, you have received a gift!");
		if (reward[0] != -1)
		{
			player.addItem("CodesGoingIn", reward[0], Rnd.get(reward[1], reward[2]), null, true);
		}
		else
		{
			player.setGamePoints(player.getGamePoints() + (Rnd.get(reward[1], reward[2])));
		}
		if (Config.GIFT_CODES_TYPES.get(giftCode.length()))
		{
			player.getVariables().set("UsedGiftCodes", player.getVariables().getString("UsedGiftCodes", "") + giftCode.length() + ";");
		}
		removeCode(giftCode);
	}
	
	public void removeCode(String code)
	{
		_giftCodes.remove(code);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("DELETE FROM giftcodes WHERE code=?"))
		{
			statement.setString(1, code);
			statement.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not delete code data: " + e.getMessage(), e);
		}
	}
	
	public static GiftCodesManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GiftCodesManager _instance = new GiftCodesManager();
	}
}
