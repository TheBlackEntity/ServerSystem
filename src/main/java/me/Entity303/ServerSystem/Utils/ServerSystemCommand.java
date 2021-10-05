package me.Entity303.ServerSystem.Utils;

import me.Entity303.ServerSystem.Main.ss;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ServerSystemCommand {
    protected final ss plugin;

    public ServerSystemCommand(ss plugin) {
        this.plugin = plugin;
    }

    public String getPrefix() {
        return this.plugin.getMessages().getPrefix();
    }

    public String getMessage(String action, String label, String command, CommandSender sender, CommandSender target) {
        return this.plugin.getMessages().getMessage(label, command, sender, target, action);
    }

    public String getMessageWithStringTarget(String action, String label, String command, CommandSender sender, String target) {
        return this.plugin.getMessages().getMessageWithStringTarget(label, command, sender, target, action);
    }

    public String getMessage(String action, String label, String command, String sender, CommandSender target) {
        return this.plugin.getMessages().getMessage(label, command, sender, target, action);
    }

    public String getMessage(String action, String label, String command, CommandSender sender, CommandSender target, boolean colorless) {
        return this.plugin.getMessages().getMessage(label, command, sender, target, action, colorless);
    }

    public String getMiscMessage(String action, String label, String command, CommandSender sender, CommandSender target) {
        return this.plugin.getMessages().getMiscMessage(label, command, sender, target, action);
    }

    public String getRules(String label, String command, CommandSender sender, CommandSender target) {
        String rules = this.plugin.getRulesConfig().getString("Rules");
        if (sender == null) throw new IllegalArgumentException("Sender cannot be null!");
        if (target == null) target = sender;

        String senderName = sender.getName();
        String senderDisplayName;

        if (sender instanceof Player) senderDisplayName = ((Player) sender).getDisplayName();
        else senderDisplayName = senderName;

        String targetName = target.getName();
        String targetDisplayName;

        if (target instanceof Player) targetDisplayName = ((Player) target).getDisplayName();
        else targetDisplayName = targetName;

        if (senderName.equalsIgnoreCase("console") || senderName.equalsIgnoreCase("konsole")) {
            senderName = this.plugin.getMessages().getCfg().getString("Messages.Misc.BanSystem.ConsoleName");
            senderDisplayName = senderName;
        }

        if (targetName.equalsIgnoreCase("console") || targetName.equalsIgnoreCase("konsole")) {
            targetName = this.plugin.getMessages().getCfg().getString("Messages.Misc.BanSystem.ConsoleName");
            targetDisplayName = targetName;
        }

        try {
            String s = ChatColor.translateAlternateColorCodes('&', rules.replace("<LABEL>", label).replace("<COMMAND>", command).replace("<SENDER>", senderName).replace("<TARGET>", targetName).replace("<SENDERDISPLAY>", senderDisplayName).replace("<TARGETDISPLAY>", targetDisplayName).replace("<BREAK>", "\n"));
            return s;
        } catch (NullPointerException ignored) {
            return "Error! Path: Rules";
        }
    }

    public boolean isAllowed(CommandSender cs, String action) {
        return this.plugin.getPermissions().hasPerm(cs, action);
    }

    public boolean isAllowed(CommandSender cs, String action, Boolean noFuck) {
        return this.plugin.getPermissions().hasPerm(cs, action, noFuck);
    }

    public boolean isPermAllowed(CommandSender cs, String permission) {
        return this.plugin.getPermissions().hasPermString(cs, permission);
    }

    public boolean isPermAllowed(CommandSender cs, String permission, Boolean noFuck) {
        return this.plugin.getPermissions().hasPermString(cs, permission, noFuck);
    }

    public String Perm(String action) {
        return this.plugin.getPermissions().Perm(action);
    }

    public String getNoPermission(String permission) {
        return this.plugin.getMessages().getNoPermission(permission);
    }

    public String getSyntax(String action, String label, String command, CommandSender sender, CommandSender target) {
        return this.plugin.getMessages().getSyntax(label, command, sender, target, action);
    }

    public String getSyntaxWithStringTarget(String action, String label, String command, CommandSender sender, String target) {
        return this.plugin.getMessages().getSyntaxWithStringTarget(label, command, sender, target, action);
    }

    public String getNoTarget(String targetName) {
        return this.plugin.getMessages().getNoTarget(targetName);
    }

    public String getOnlyPlayer() {
        return this.plugin.getMessages().getOnlyPlayer();
    }

    public void sendNoPermissionInfo(CommandSender cs) {
        this.plugin.log(ChatColor.translateAlternateColorCodes('&', this.plugin.getMessages().getCfg().getString("Messages.Misc.NoPermissionInfo")).replace("<SENDER>", cs.getName()));
    }

    protected String getBanSystem(String action) {
        return this.plugin.getMessages().getCfg().getString("Messages.Misc.BanSystem." + action);
    }

    public Player getPlayer(CommandSender sender, String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null) return null;
        if (sender instanceof Player)
            if (!this.plugin.getVanish().isVanish(player) || this.isAllowed(sender, "vanish.see", true)) return player;
            else return null;
        return player;
    }

    public Player getPlayer(CommandSender sender, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;
        if (sender instanceof Player)
            if (!this.plugin.getVanish().isVanish(player) || this.isAllowed(sender, "vanish.see", true)) return player;
            else return null;
        return player;
    }

    public double getMaxHealth(Player player) {
        return this.plugin.getVersionManager().isV119() ? player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : player.getMaxHealth();
    }
}