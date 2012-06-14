package net.namedfork.bukkit.Tips;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author zydeco
 */
public class TipSet implements Runnable {
    private final World world;
    private final int delay, period;
    private final String[] tips;
    private final Random rng;
    private final String id;
    private final Permission perm;
    private int nextTip;
    
    public TipSet(String id, World world, int delay, int period, List<String> tips, long seed) {
        this.id = id;
        this.world = world;
        this.delay = delay;
        this.period = period;
        this.tips = tips.toArray(new String[0]);
        this.rng = seed == 0?null:new Random(seed);
        nextTip = -1;
        
        // format tips
        for(int i=0; i < this.tips.length; i++) {
            this.tips[i] = formatTip(this.tips[i]);
        }
        
        // register permission
        if (id != null) {
            PluginManager pm = Bukkit.getServer().getPluginManager();
            String permName = "tips.receive."+id;
            if (pm.getPermission(permName) == null) {
                perm = new Permission(permName, PermissionDefault.FALSE);
                pm.addPermission(perm);
            } else {
                perm = pm.getPermission(permName);
            }
        } else {
            perm = null;
        }
        
    }
    
    public void run() {
        // find a tip
        if (tips.length == 0) return;
        else if (tips.length == 1) {
            nextTip = 0;
        } else if (rng != null) {
            nextTip = rng.nextInt(tips.length);
        } else {
            nextTip = (nextTip + 1) % tips.length;
        }
        final String tip = tips[nextTip];
        
        if (world == null && perm == null) {
            // tip for everyone
            Player players[] = Bukkit.getServer().getOnlinePlayers();
            for(Player p: players) {
                p.sendMessage(tip);
            }
        } else if (world == null && perm != null) {
            // global tip with permission
            Player players[] = Bukkit.getServer().getOnlinePlayers();
            for(Player p: players) {
                if (p.hasPermission(perm) || p.hasPermission("tips.receive.*")) {
                    p.sendMessage(tip);
                }
            }
        } else {
            // tip for world
            Iterator<Player> i = world.getPlayers().iterator();
            while (i.hasNext()) {
                Player p = i.next();
                if (perm == null || p.hasPermission(perm) || p.hasPermission("tips.receive.*")) {
                    p.sendMessage(tip);
                }
            }
        }
    }
    
    // replace $ with \u00A7 for color codes, and $$ with $
    private String formatTip(String tip) {
        return tip.replace("$", "\u00A7").replace("\u00A7$", "$");
    }
    
    public World getWorld() {
        return world;
    }

    public int getDelay() {
        return delay;
    }
    
    public int getPeriod() {
        return period;
    }

    public String[] getTips() {
        return tips;
    }
    
    
}
