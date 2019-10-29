package handlers;

import ai.sunriseNpc.AchievementManager.AchievementManager;
import ai.sunriseNpc.BetaManager.BetaManager;
import ai.sunriseNpc.BonusDealer.BonusDealer;
import ai.sunriseNpc.CasinoManager.CasinoManager;
import ai.sunriseNpc.CastleManager.CastleManager;
import ai.sunriseNpc.DelevelManager.DelevelManager;
import ai.sunriseNpc.GrandBossManager.GrandBossManager;
import ai.sunriseNpc.NoblesseManager.NoblesseManager;
import ai.sunriseNpc.PointsManager.PointsManager;
import ai.sunriseNpc.PremiumManager.PremiumManager;
import ai.sunriseNpc.ReportManager.ReportManager;
import ai.sunriseNpc.TransmutationManager.TransmutationManager;
import gr.sr.handler.ABLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class SunriseNpcsLoader extends ABLoader
{
    private final Class<?>[] SCRIPTS =
            {
                    AchievementManager.class,
                    BonusDealer.class,
                    BetaManager.class,
                    CasinoManager.class,
                    CastleManager.class,
                    DelevelManager.class,
                    GrandBossManager.class,
                    NoblesseManager.class,
                    PointsManager.class,
                    PremiumManager.class,
                    ReportManager.class,
                    TransmutationManager.class,
            };

    public SunriseNpcsLoader()
    {
        loadScripts();
        }

            @Override
            public Class<?>[] getScripts()
            {
        return SCRIPTS;
    }
}
