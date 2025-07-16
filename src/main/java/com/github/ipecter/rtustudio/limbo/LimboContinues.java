package com.github.ipecter.rtustudio.limbo;

import com.github.ipecter.rtustudio.limbo.command.ReloadCommand;
import com.github.ipecter.rtustudio.limbo.configuration.SettingConfig;
import com.github.ipecter.rtustudio.limbo.listener.PlayerLogin;
import com.github.ipecter.rtustudio.limbo.listener.ServerPing;
import com.github.ipecter.rtustudio.limbo.server.QueueServer;
import com.github.ipecter.rtustudio.limbo.server.ReconnectServer;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedList;


@Slf4j(topic = "LimboContinues")
public class LimboContinues {

    @Getter
    private static ComponentSerializer<Component, Component, String> serializer;
    @Getter
    private final ProxyServer server;
    @Getter
    private final Path dir;
    private final CommandMeta commandMeta;
    @Getter
    private final SettingConfig settingConfig;
    @Getter
    public LinkedList<LimboPlayer> players = new LinkedList<>();
    @Getter
    public LinkedList<LimboPlayer> priority = new LinkedList<>();

    public LinkedList<LimboPlayer> getAllPlayers() {
        LinkedList<LimboPlayer> list = new LinkedList<>(priority);
        list.addAll(players);
        return list;
    }

    public int indexOf(LimboPlayer player) {
        LinkedList<LimboPlayer> list = getAllPlayers();
        for (int index = 0; index < list.size(); index++) {
            LimboPlayer lp = list.get(index);
            if (lp == null) continue;
            Player pp = lp.getProxyPlayer();
            if (pp.getUniqueId().equals(player.getProxyPlayer().getUniqueId())) return index;
        }
        verbose(player + " is no found!");
        return -1;
    }

    @Getter
    private ReconnectServer reconnect;
    @Getter
    private QueueServer queue;

    @Inject
    public LimboContinues(ProxyServer server, @DataDirectory Path dir) {
        this.server = server;
        this.commandMeta = server.getCommandManager().metaBuilder("limbocontinues").plugin(this).build();
        this.dir = dir;

        this.settingConfig = new SettingConfig(this);
        serializer = this.settingConfig.getConfig().getSerializer().getSerializer();
    }

    public Logger getLogger() {
        return log;
    }

    public void verbose(String message) {
        if (settingConfig.getConfig().isDebug()) {
            log.info(message);
        }
    }

    @Subscribe
    private void onInitialize(ProxyInitializeEvent event) {
        reconnect = new ReconnectServer(this);
        queue = new QueueServer(this);
        reload();

        server.getEventManager().register(this, new PlayerLogin(this));
        server.getEventManager().register(this, new ServerPing(this));

        server.getCommandManager().register(commandMeta, new ReloadCommand(this));
    }

    public void reload() {
        settingConfig.reload();
        serializer = this.settingConfig.getConfig().getSerializer().getSerializer();

        queue.reload();
        reconnect.reload();
    }
}
