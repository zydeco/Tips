package net.namedfork.bukkit.Tips;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TipsCommand implements CommandExecutor {
    protected Tips plugin;
    
    protected TipsCommand(Tips plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender.hasPermission("tips.reload")) {
            if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
                // reload
                plugin.loadConfig(sender);
                return true;
            } else return false;
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            return true;
        }
    }
}
