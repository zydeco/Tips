/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
    
    public TipSet(Server server, World world, int delay, int period, List<String> tips, long seed) {
        this.server = server;
        this.world = world;
        this.delay = delay;
        this.period = period;
        this.tips = tips.toArray(new String[0]);
        this.rng = new Random(seed == 0?(long)((Math.random()-0.5)*2*Long.MAX_VALUE):seed);
        
        // format tips
        for(int i=0; i < this.tips.length; i++) {
            this.tips[i] = formatTip(this.tips[i]);
        }
    }
    
    public void run() {
        // find a tip
        if (tips.length == 0) return;
        String tip = tips.length == 1? tips[0]:tips[rng.nextInt(tips.length)];
        
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
