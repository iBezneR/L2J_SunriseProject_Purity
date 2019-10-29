package l2r.gameserver.communitybbs.SunriseBoards.dropCalc;

import gr.sr.configsEngine.AbstractConfigs;
import gr.sr.utils.FileProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vGodFather
 */
public class DropCalculatorConfigs extends AbstractConfigs
{
	private static final String DROP_CALC_CONFIG_FILE = "./config/extra/DropCalculator.ini";
	
	public static boolean ENABLE_DROP_CALCULATOR;
	public static boolean ENABLE_TELEPORT_FUNCTION;
	public static boolean ALLOW_TELEPORT_FROM_PEACE_ZONE_ONLY;
	public static boolean ALLOW_FREE_TELEPORT;
	public static int[] TELEPORT_PRICE;
	public static List<Integer> RESTRICTED_TELEPORT_IDS;
	public static List<Integer> RESTRICTED_MOB_DROPLIST_IDS;
	
	@Override
	public void loadConfigs()
	{
		// Load drop calculator Server L2Properties file (if exists)
		FileProperties DropCalcProperties = new FileProperties();
		final File dropCalc = new File(DROP_CALC_CONFIG_FILE);
		try (InputStream is = new FileInputStream(dropCalc))
		{
			DropCalcProperties.load(is);
		}
		catch (Exception e)
		{
			_log.error("Error while loading drop calculator system settings!", e);
		}
		
		ENABLE_DROP_CALCULATOR = Boolean.parseBoolean(DropCalcProperties.getProperty("EnableDropCalculator", "False"));
		ENABLE_TELEPORT_FUNCTION = Boolean.parseBoolean(DropCalcProperties.getProperty("EnableTeleportFunction", "False"));
		ALLOW_TELEPORT_FROM_PEACE_ZONE_ONLY = Boolean.parseBoolean(DropCalcProperties.getProperty("AllowTeleportFromPeaceOnly", "False"));
		ALLOW_FREE_TELEPORT = Boolean.parseBoolean(DropCalcProperties.getProperty("AllowFreeTeleport", "False"));
		
		final String[] prices = DropCalcProperties.getProperty("TeleportPrice", "57,1000").split(",");
		TELEPORT_PRICE = new int[prices.length];
		for (int i = 0; i < prices.length; i++)
		{
			TELEPORT_PRICE[i] = Integer.parseInt(prices[i]);
		}
		
		String[] modIds = DropCalcProperties.getProperty("RestrictedMobIdsForTeleport", "25188;29163").trim().split(";");
		RESTRICTED_TELEPORT_IDS = new ArrayList<>(modIds.length);
		for (String modId : modIds)
		{
			try
			{
				RESTRICTED_TELEPORT_IDS.add(Integer.parseInt(modId));
			}
			catch (NumberFormatException nfe)
			{
				_log.warn(DropCalculatorConfigs.class.getSimpleName() + ": Wrong mob Id RestrictedMobIdsForTeleport passed: " + modId);
				_log.warn(nfe.getMessage());
			}
		}
		
		String[] dropIds = DropCalcProperties.getProperty("RestrictedMobIdsInDropCalc", "25188;29163").trim().split(";");
		RESTRICTED_MOB_DROPLIST_IDS = new ArrayList<>(dropIds.length);
		for (String dropId : dropIds)
		{
			try
			{
				RESTRICTED_MOB_DROPLIST_IDS.add(Integer.parseInt(dropId));
			}
			catch (NumberFormatException nfe)
			{
				_log.warn(DropCalculatorConfigs.class.getSimpleName() + ": Wrong mob Id RestrictedMobIdsInDropCalc passed: " + dropId);
				_log.warn(nfe.getMessage());
			}
		}
	}
	
	public static DropCalculatorConfigs getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DropCalculatorConfigs _instance = new DropCalculatorConfigs();
	}
}