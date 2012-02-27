package net.namedfork.bukkit.Tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Tips for Bukkit
 *
 * @author Jesús A. Álvarez
 */
public class Tips extends JavaPlugin {
    private ArrayList<TipSet> tipSets;
    private Random rng;
    
    public void onEnable() {
        Logger logger = getServer().getLogger();
        rng = new Random(System.currentTimeMillis());
        
        // Load config
        loadConfig(null);

        // commands
        getCommand("tips").setExecutor(new TipsCommand(this));
        
        PluginDescriptionFile pdfFile = this.getDescription();
        logger.log(Level.INFO, "[Tips] version " + pdfFile.getVersion() + " enabled");
    }
    
    public void onDisable() {
        // unschedule current tips
        getServer().getScheduler().cancelTasks(this);
    }
    
    // returns list of tip sets for a world, global sets if w == null or null on error
    private List<TipSet> tipSetsForWorld(World w) {
        try {
            // get list of tips for world from config
            List tipList = getConfig().getList(w == null?"global":w.getName());
            if (tipList == null) return null;
            List<TipSet> tipSets = new ArrayList<TipSet>(tipList.size());
            // create TipSet for every item
            for(Iterator i = tipList.iterator(); i.hasNext(); ) {
                HashMap t = (HashMap)i.next();
                int period = ((Number)t.get("period")).intValue();
                int delay = t.containsKey("delay")?((Number)t.get("delay")).intValue():period;
                boolean isRandom = !(t.containsKey("random") && ((Boolean)t.get("random")).booleanValue() == false);
                tipSets.add(new TipSet((String)t.get("id"), w, delay, period, (List<String>)t.get("tips"), isRandom?rng.nextLong():0));
            }
            return tipSets;
        } catch (Exception e) {
            System.out.println("tipSetsForWorld: "+ e.toString());
            return null;
        }
    }
    
    public boolean loadConfig(CommandSender sender) {
        Logger logger = getServer().getLogger();
        tipSets = new ArrayList<TipSet>();
        String msg;
        
        // unschedule current tips
        getServer().getScheduler().cancelTasks(this);
        
        // load config
        try {
            this.reloadConfig();
        } catch (Exception e) {
            msg = "Invalid tips configuration file.";
            logger.log(Level.INFO, "[Tips] " + msg);
            if (sender instanceof Player) sender.sendMessage(ChatColor.RED + msg);
            return false;
        }
        
        // load global tips
        List<TipSet> worldTips = tipSetsForWorld(null);
        if (worldTips != null) tipSets.addAll(worldTips);
        msg = "Loaded " + (worldTips == null?0:worldTips.size()) + " global tip set(s)";
        logger.log(Level.INFO, "[Tips] " + msg);
        if (sender instanceof Player) sender.sendMessage(ChatColor.AQUA + msg);
        
        // load tips by world
        for(Iterator<World> i = getServer().getWorlds().iterator(); i.hasNext(); ) {
            World w = i.next();
            worldTips = tipSetsForWorld(w);
            if (worldTips != null) tipSets.addAll(worldTips);
            msg = "Loaded " + (worldTips == null?0:worldTips.size()) + " tip set(s) for world " + w.getName();
            logger.log(Level.INFO, "[Tips] " + msg);
            if (sender instanceof Player) sender.sendMessage(ChatColor.AQUA + msg);
        }

        // schedule tips
        for(Iterator<TipSet> i = tipSets.iterator(); i.hasNext(); ) {
            TipSet tipSet = i.next();
            getServer().getScheduler().scheduleSyncRepeatingTask(this, tipSet, tipSet.getDelay()*60*20, tipSet.getPeriod()*60*20);
        }
        
        return true;
    }
}

