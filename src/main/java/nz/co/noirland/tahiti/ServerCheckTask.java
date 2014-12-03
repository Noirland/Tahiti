package nz.co.noirland.tahiti;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.SynchronousQueue;

public class ServerCheckTask implements Runnable {

    @Override
    public void run() {
        ServerInfo server = Tahiti.inst().getDefaultServer();

        final SynchronousQueue<Boolean> queue = new SynchronousQueue<>(); // Whether server is non-responsive

        server.ping(new Callback<ServerPing>() {
            @Override
            public void done(ServerPing result, Throwable error) {
                try {
                    queue.put(error != null);
                } catch (InterruptedException ignored) {}
            }
        });

        try {
            if(queue.take()) return;
        } catch (InterruptedException e) {
            return;
        }

        ServerInfo fallback = Tahiti.inst().getFallbackServer();
        for(final ProxiedPlayer player : fallback.getPlayers()) {
            player.connect(server, new Callback<Boolean>() {
                @Override
                public void done(Boolean result, Throwable error) {
                    if(result) {
                        player.sendMessage(Tahiti.RECONNECT_MESSAGE);
                    }
                }
            });
        }
    }
}
