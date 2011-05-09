package net.namedfork.bukkit.Tips;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Tips for Bukkit
 *
 * @author Jesús A. Álvarez
 */
public class Tips extends JavaPlugin {
    private ArrayList<TipSet> tipSets;
    private PermissionHandler permissionHandler;
    
    public void onEnable() {
        Logger logger = getServer().getLogger();
        
        // Load config
        loadConfig(null);
        
        // setup permissions
        setupPermissions();

        // commands
        getCommand("tips").setExecutor(new TipsCommand(this));
        
        PluginDescriptionFile pdfFile = this.getDescription();
        logger.log(Level.INFO, "[Tips] version " + pdfFile.getVersion() + " enabled");
    }
    
    public void onDisable() {
        
    }
    
    // returns list of tip sets for a world, global sets if w == null or null on error
    private List<TipSet> tipSetsForWorld(World w) {
        try {
            // get list of tips for world from config
            List tipList = getConfiguration().getList(w == null?"global":w.getName());
            if (tipList == null) return null;
            List<TipSet> tipSets = new ArrayList<TipSet>(tipList.size());
            // create TipSet for every item
            for(Iterator i = tipList.iterator(); i.hasNext(); ) {
                HashMap t = (HashMap)i.next();
                int period = ((Number)t.get("period")).intValue();
                int delay = t.containsKey("delay")?((Number)t.get("delay")).intValue():period;
                tipSets.add(new TipSet(getServer(), w, delay, period, (List<String>)t.get("tips"), 0));
            }
            return tipSets;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }
    
    private void setupPermissions() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

        if (permissionHandler == null) {
            if (permissionsPlugin != null) {
                permissionHandler = ((Permissions) permissionsPlugin).getHandler();
            } else {
                getServer().getLogger().log(Level.INFO, "[MCWBans] Permissions plugin not found");
            }
        }
    }
    
    public boolean hasPermission(Player player, String permission, boolean def) {
        if (permissionHandler == null) return def;
        System.out.println("checking permission " + permission + " for " + player.getName() + ": " + (permissionHandler.has(player, permission)?"has":"no"));
        return permissionHandler.has(player, permission);
    }
    
    public void loadConfig(CommandSender sender) {
        Logger logger = getServer().getLogger();
        tipSets = new ArrayList<TipSet>();
        String msg;
        
        // unschedule current tips
        getServer().getScheduler().cancelTasks(this);
        
        // load config
        getConfiguration().load();

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
    }
}

