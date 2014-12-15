package nz.co.noirland.tahiti;

import com.google.common.collect.Iterables;
import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.*;

public class Tahiti extends Plugin implements Listener {

    private static Tahiti inst;

    private ServerInfo defaultServer;
    private ServerInfo fallbackServer;
    private boolean forced = false;

    public static final BaseComponent[] TITLE = new ComponentBuilder("WELCOME TO TAHITI")
            .color(ChatColor.GOLD)
            .bold(true)
            .create();
    public static final BaseComponent[] SUBTITLE = new ComponentBuilder("You'll be automatically sent back when Noirland's up")
            .color(ChatColor.DARK_RED)
            .italic(true)
            .create();

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

        getProxy().getScheduler().schedule(this, new ServerCheckTask(), 0, 10, TimeUnit.SECONDS);
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new TahitiCommand());
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing response = event.getResponse();
        if (getPing(getDefaultServer()) == null || forced) {
            response.setDescription(getFallbackServer().getMotd());
        }
    }

    @EventHandler
    public void onKick(ServerKickEvent event) {
        String kickReason = BaseComponent.toPlainText(event.getKickReasonComponent());
        if(!kickReason.contains("[TAHITI]")) return;

        event.setCancelled(true);
        event.setCancelServer(getFallbackServer());
    }

    @EventHandler
    public void onJoin(ServerConnectedEvent event) {
        if(event.getServer().getInfo() != getFallbackServer()) return;

        getProxy().createTitle().stay(200).title(TITLE).subTitle(SUBTITLE).send(event.getPlayer());
    }

    public boolean toggleForce() {
        forced = !forced;

        return forced;
    }

    public boolean isForced() {
        return forced;
    }

    public ServerInfo getDefaultServer() {
        return defaultServer;
    }

    public ServerInfo getFallbackServer() {
        return fallbackServer;
    }

    public static ServerPing getPing(ServerInfo info) {
        final Exchanger<ServerPing> ex = new Exchanger<>();

        info.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing result, Throwable error) {
                try {
                    ex.exchange(result);
                } catch (InterruptedException e) { }
            }
        });

        try {
            return ex.exchange(null);
        } catch (InterruptedException e) {
            return null;
        }
    }

    public static void move(ServerInfo from, ServerInfo to) {
        for(final ProxiedPlayer player : from.getPlayers()) {
            player.connect(to);
        }
    }
}
