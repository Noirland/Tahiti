package nz.co.noirland.tahiti;

import net.md_5.bungee.api.config.ServerInfo;

public class ServerCheckTask implements Runnable {

    @Override
    public void run() {
        if(Tahiti.inst().isForced()) return;

        ServerInfo server = Tahiti.inst().getDefaultServer();

        if(Tahiti.getPing(server) == null) return;

        ServerInfo fallback = Tahiti.inst().getFallbackServer();
        Tahiti.move(fallback, server);
    }
}
