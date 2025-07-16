package com.github.ipecter.rtustudio.limbo.handler;

import com.github.ipecter.rtustudio.limbo.LimboContinues;
import com.github.ipecter.rtustudio.limbo.configuration.QueueConfig;
import com.github.ipecter.rtustudio.limbo.configuration.SettingConfig;
import com.github.ipecter.rtustudio.limbo.protocol.packets.PlaySound;
import com.velocitypowered.api.proxy.server.PingOptions;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class QueueHandler extends QueuedHandler {

    private final QueueConfig.Config config;
    private final PingOptions pingOptions;

    private final PlaySound queueSound;
    private final PlaySound offlineSound;
    private final PlaySound connectSound;

    private int remain = -1;
    private State state = null;

    public QueueHandler(LimboContinues plugin, RegisteredServer server) {
        super(plugin, server);
        this.config = plugin.getQueue().getConfig();
        this.pingOptions = PingOptions.builder().timeout(Duration.ofMillis(config.getServer().getTimeout())).build();
        int x = config.getWorld().getLocation().getX();
        int y = config.getWorld().getLocation().getY();
        int z = config.getWorld().getLocation().getZ();
        this.offlineSound = config.getOffline().getSound().setPosition(x, y, z);
        this.queueSound = config.getQueue().getSound().setPosition(x, y, z);
        this.connectSound = config.getConnect().getSound().setPosition(x, y, z);
    }

    @Override
    public void onJoin(Limbo server, LimboPlayer player) {
        player.disableFalling();
        player.setGameMode(config.getWorld().getGamemode());
        player.getScheduledExecutor().schedule(this::tick, config.getServer().getCheck(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void onMove(double x, double y, double z) {
        offlineSound.setPosition(x, y, z);
        queueSound.setPosition(x, y, z);
        connectSound.setPosition(x, y, z);
    }

    private void tick() {
        if (!tick) return;

        server.ping(pingOptions).whenComplete((ping, exception) -> {
            LinkedList<LimboPlayer> list = plugin.getAllPlayers();
            QueueConfig.Config.Server serverConfig = config.getServer();
            if (exception != null) {
                Title title = Title.title(
                        LimboContinues.getSerializer().deserialize(config.getOffline().getTitle().getTitle()),
                        LimboContinues.getSerializer().deserialize(config.getOffline().getTitle().getSubtitle()),
                        Title.Times.times(Duration.ZERO, Duration.ofMillis(30000), Duration.ZERO));
                Component message = LimboContinues.getSerializer().deserialize(config.getOffline().getMessage());
                player.getProxyPlayer().showTitle(title);
                if (this.state != State.OFFLINE) {
                    player.getProxyPlayer().sendMessage(message);
                    player.writePacket(offlineSound);
                }
                this.state = State.OFFLINE;
                player.getScheduledExecutor().schedule(this::tick, serverConfig.getCheck(), TimeUnit.MILLISECONDS);
            } else {
                boolean isFull = false;
                QueueConfig.Config.Queue.MaxPlayer mp = config.getQueue().getMaxPlayer();
                if (ping.getPlayers().isPresent() && !mp.getBypass().contains(player.getProxyPlayer().getUsername())) {
                    ServerPing.Players players = ping.getPlayers().get();
                    int max = mp.isEnabled() ? Math.min(mp.getSize(), players.getMax()) : players.getMax();
                    isFull = players.getOnline() >= max;
                }
                if (!(isFull || list.isEmpty())) {
                    Title title = Title.title(
                            LimboContinues.getSerializer().deserialize(config.getConnect().getTitle().getTitle()),
                            LimboContinues.getSerializer().deserialize(config.getConnect().getTitle().getSubtitle()),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(30000), Duration.ZERO));
                    Component message = LimboContinues.getSerializer().deserialize(config.getConnect().getMessage());
                    player.getProxyPlayer().showTitle(title);
                    if (this.state != State.CONNECT) player.getProxyPlayer().sendMessage(message);
                    this.state = State.CONNECT;
                    player.getScheduledExecutor().schedule(() -> {
                        player.writePacket(connectSound);
                        player.getProxyPlayer().resetTitle();
                        player.disconnect(server);
                    }, serverConfig.getDelay(), TimeUnit.MILLISECONDS);
                } else {
                    int index = plugin.indexOf(player);
                    Title title = Title.title(
                            LimboContinues.getSerializer().deserialize(
                                    MessageFormat.format(config.getQueue().getTitle().getTitle(), index + 1)),
                            LimboContinues.getSerializer().deserialize(
                                    MessageFormat.format(config.getQueue().getTitle().getSubtitle(), index + 1)),
                            Title.Times.times(Duration.ZERO, Duration.ofMillis(30000), Duration.ZERO));
                    Component message = LimboContinues.getSerializer().deserialize(
                            MessageFormat.format(config.getQueue().getMessage(), index + 1));
                    player.getProxyPlayer().showTitle(title);
                    if (this.remain != index) {
                        player.getProxyPlayer().sendMessage(message);
                        player.writePacket(queueSound);
                    }
                    this.remain = index;
                    this.state = State.QUEUE;
                    player.getScheduledExecutor().schedule(this::tick, serverConfig.getCheck(), TimeUnit.MILLISECONDS);
                }
            }
        });
    }

    enum State {
        OFFLINE, QUEUE, CONNECT
    }
}
