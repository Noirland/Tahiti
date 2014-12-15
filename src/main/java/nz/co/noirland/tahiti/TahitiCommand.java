package nz.co.noirland.tahiti;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;

public class TahitiCommand extends Command {

    private ServerInfo fallback = Tahiti.inst().getFallbackServer();
    private ServerInfo def = Tahiti.inst().getDefaultServer();


    public TahitiCommand() {
        super("tahiti", "tahiti.force");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean res = Tahiti.inst().toggleForce();
        if(res) {
            sender.sendMessage(ChatColor.DARK_RED + "Tahiti is now forced!");
            Tahiti.move(def, fallback);
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Tahiti is no longer forced.");
            Tahiti.move(fallback, def);
        }
    }
}
