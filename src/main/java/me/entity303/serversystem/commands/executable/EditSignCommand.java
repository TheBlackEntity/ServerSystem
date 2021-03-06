package me.entity303.serversystem.commands.executable;

import me.entity303.serversystem.main.ServerSystem;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EditSignCommand implements CommandExecutor {
    private final ServerSystem plugin;

    public EditSignCommand(ServerSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (this.plugin.getVersionStuff().getSignEdit() == null) {
            sender.sendMessage(this.plugin.getMessages().getPrefix() + this.plugin.getMessages().getMessage(label, cmd.getName(), sender, null, "EditSign.NotAvailable"));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.plugin.getMessages().getPrefix() + this.plugin.getMessages().getOnlyPlayer());
            return true;
        }
        Player p = (Player) sender;
        if (!this.plugin.getPermissions().hasPerm(p, "editschild.players")) {
            p.sendMessage(this.plugin.getMessages().getPrefix() + this.plugin.getMessages().getNoPermission(this.plugin.getPermissions().Perm("editschild.players")));
            return true;
        }
        Block block = p.getTargetBlock(null, 5);
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            this.plugin.getVersionStuff().getSignEdit().editSign(p, sign);
        } else
            p.sendMessage(this.plugin.getMessages().getPrefix() + this.plugin.getMessages().getMessage(label, cmd.getName(), sender, null, "EditSign.SignNeeded"));
        return true;
    }
}
