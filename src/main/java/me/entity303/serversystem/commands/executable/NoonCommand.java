package me.entity303.serversystem.commands.executable;


import me.entity303.serversystem.main.ServerSystem;
import me.entity303.serversystem.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoonCommand extends MessageUtils implements CommandExecutor {

    public NoonCommand(ServerSystem plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (args.length == 0) if (cs instanceof Player) if (this.isPermAllowed(cs, this.Perm("noon"))) {
            ((Player) cs).getWorld().setTime(6000);
            cs.sendMessage(this.getPrefix() + this.getMessage("Time.Success", label, "time", cs, null).replace("<WORLD>", ((Player) cs).getWorld().getName()).replace("<TIME>", this.getTime("Noon")));
        } else cs.sendMessage(this.getPrefix() + this.getNoPermission(this.Perm("noon")));
        else
            cs.sendMessage(this.getPrefix() + this.getSyntax("Time", "time", "time", cs, null));
        else if (this.isPermAllowed(cs, this.Perm("noon"))) {
            World w = Bukkit.getWorld(args[0]);
            if (w != null) {
                w.setTime(6000);
                cs.sendMessage(this.getPrefix() + this.getMessage("Time.Success", label, "time", cs, null).replace("<WORLD>", w.getName()).replace("<TIME>", this.getTime("Noon")));
            } else
                cs.sendMessage(this.getPrefix() + this.getMessage("Time.NoWorld", label, "time", cs, null).replace("<WORLD>", args[0]));
        } else if (cs instanceof Player) if (this.isPermAllowed(cs, this.Perm("noon"))) {
            ((Player) cs).getWorld().setTime(6000);
            cs.sendMessage(this.getPrefix() + this.getMessage("Time.Success", label, "time", cs, null).replace("<WORLD>", ((Player) cs).getWorld().getName()).replace("<TIME>", this.getTime("Noon")));
        } else cs.sendMessage(this.getPrefix() + this.getNoPermission(this.Perm("noon")));
        else
            cs.sendMessage(this.getPrefix() + this.getNoPermission(this.Perm("noon")));
        return true;
    }

    private String getTime(String time) {
        return this.plugin.getMessages().getCfg().getString("Messages.Misc.Times." + time);
    }
}
