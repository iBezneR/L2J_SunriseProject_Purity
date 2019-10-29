/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2r.gameserver.handler.vote;

import custom.erengine.ErBonus;
import custom.erengine.ErReward;
import custom.vote.IVote;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Erlandys
 */
public class TopZone extends IVote
{
	public TopZone(int id, String type, String link, String linkToCheck, int refreshesAfter, HashMap<Integer, ArrayList<ErReward>> allRewards, HashMap<Integer, ArrayList<ErBonus>> allBonuses, ArrayList<Integer> rewardIds, boolean withIP)
	{
		super(id, type, link, linkToCheck, refreshesAfter, allRewards, allBonuses, rewardIds, withIP);
	}
	
	@Override
	public int getVoteCount()
	{
		int votes = 0;
		try
		{
			URL oracle = new URL(getLinkToCheck());
			URLConnection yc = oracle.openConnection();
			yc.setRequestProperty("User-Agent", "L2TopZone");
			try (BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));)
			{
				String inputLine;
				while ((inputLine = in.readLine()) != null)
				{
					if (inputLine.contains("<span class=\"label label-info\"><i class=\"fa fa-fw fa-lg fa-thumbs-up\"></i>"))
					{
						String line = inputLine.split("thumbs-up\"></i>")[1].split("</span>")[0].replaceAll(" ", "");
						votes = Integer.parseInt(line);
						return votes;
					}
				}
				in.close();
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		return votes;
	}
	
	@Override
	public int getVoteCount(L2PcInstance player)
	{
		return 0;
	}
}