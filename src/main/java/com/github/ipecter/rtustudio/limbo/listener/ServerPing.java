package com.github.ipecter.rtustudio.limbo.listener;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import com.github.ipecter.rtustudio.limbo.configuration.QueueConfig;
import com.github.ipecter.rtustudio.limbo.configuration.SettingConfig;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;

public class ServerPing {

    private final LimboContinues plugin;
    private final QueueConfig.Config queueConfig;

    public ServerPing(LimboContinues plugin) {
        this.plugin = plugin;
        this.queueConfig = plugin.getQueue().getConfig();
    }

    @Subscribe
    private void onPing(ProxyPingEvent e) {
        QueueConfig.Config.Queue.MaxPlayer mp = queueConfig.getQueue().getMaxPlayer();
        if (mp.isEnabled()) {
            com.velocitypowered.api.proxy.server.ServerPing.Builder pong = e.getPing().asBuilder();
            pong.maximumPlayers(mp.getSize());
            //pong.onlinePlayers(); TODO: 접속한 유저 표시
            e.setPing(pong.build());
        }
    }

}
