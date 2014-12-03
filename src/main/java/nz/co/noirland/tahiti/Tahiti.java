package nz.co.noirland.tahiti;

import com.google.common.collect.Iterables;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class Tahiti extends Plugin implements Listener {

    private static Tahiti inst;

    private ServerInfo defaultServer;
    private ServerInfo fallbackServer;

    private static ScheduledTask checkTask;

    public static BaseComponent RECONNECT_MESSAGE;

    static {
        RECONNECT_MESSAGE = new TextComponent("You have been reconnected to the main server.");
        RECONNECT_MESSAGE.setColor(ChatColor.RED);
    }

    public static Tahiti inst() {
        return inst;
    }

    @Override
    public void onEnable() {
        inst = this;
        ListenerInfo info = Iterables.getFirst(getProxy().getConfig().getListeners(), null);
        if(info == null) {
            getLogger().severe("No listeners registered!");
            return;
        }

        defaultServer = getProxy().getServerInfo(info.getDefaultServer());
        fallbackServer = getProxy().getServerInfo(info.getFallbackServer());

        checkTask = getProxy().getScheduler().schedule(this, new ServerCheckTask(), 0, 1, TimeUnit.MINUTES);
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        checkTask.cancel();
    }

    @EventHandler
    public void onKick(ServerKickEvent event) {
        ServerInfo from = event.getKickedFrom();

        if(from != getDefaultServer()) return;
        String kickReason = BaseComponent.toPlainText(event.getKickReasonComponent());
        if(!kickReason.contains("[SHUTDOWN]")) return;

        event.setCancelled(true);
        event.setCancelServer(getFallbackServer());
    }

    public ServerInfo getDefaultServer() {
        return defaultServer;
    }

    public ServerInfo getFallbackServer() {
        return fallbackServer;
    }
}
