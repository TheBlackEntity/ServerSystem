package me.entity303.serversystem.commands.executable;

import me.entity303.serversystem.main.ServerSystem;
import me.entity303.serversystem.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class WarpsCommand extends MessageUtils implements CommandExecutor {

    public WarpsCommand(ServerSystem plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if (this.plugin.getPermissions().getCfg().getBoolean("Permissions.warps.required"))
            if (!this.isAllowed(cs, "warps.permission")) {
                cs.sendMessage(this.getPrefix() + this.getNoPermission(this.Perm("warps.permission")));
                return true;
            }

        StringBuilder warpBuilder = new StringBuilder();
        String seperator = this.plugin.getMessages().getMessage(label, cmd.getName(), cs, null, "Warps.Format.Separator");
        String warpFormat = this.plugin.getMessages().getMessage(label, cmd.getName(), cs, null, "Warps.Format.Format");

        if (this.plugin.getWarpManager().getWarps().size() <= 0) {
            cs.sendMessage(this.plugin.getMessages().getPrefix() + this.plugin.getMessages().getMessage(label, cmd.getName(), cs, null, "Home.NoHomes"));
            return true;
        }

        List<String> warps = this.plugin.getWarpManager().getWarps();

        for (String warp : warps)
            warpBuilder.append(warpFormat.replace("<SEPERATOR>", seperator).replace("<Warp>", warp));

        if (warpBuilder.toString().toLowerCase().startsWith(seperator)) warpBuilder.delete(0, seperator.length());

        String warpMessage = this.plugin.getMessages().getMessage(label, cmd.getName(), cs, null, "Warps.Format.Message").replace("<AMOUNT>", String.valueOf(warps.size())).replace("<WARPS>", warpBuilder.toString());

        cs.sendMessage(this.plugin.getMessages().getPrefix() + warpMessage);
        return true;
    }
}
