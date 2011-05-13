package net.namedfork.bukkit.Tips;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author zydeco
 */
public class TipSet implements Runnable {
    private final Server server;
    private final World world;
    private final int delay, period;
    private final String[] tips;
    private final Random rng;
    private int nextTip;
    
    public TipSet(Server server, World world, int delay, int period, List<String> tips, long seed) {
        this.server = server;
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
        
        if (getWorld() == null) {
            // tip for everyone
            getServer().broadcastMessage(tip);
        } else {
            // tip for world
            Iterator<Player> i = getWorld().getPlayers().iterator();
            while (i.hasNext()) {
                i.next().sendMessage(tip);
            }
        }
    }
    
    // replace $ with \u00A7 for color codes, and $$ with $
    private String formatTip(String tip) {
        return tip.replace("$", "\u00A7").replace("\u00A7$", "$");
    }
    
    public Server getServer() {
        return server;
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
