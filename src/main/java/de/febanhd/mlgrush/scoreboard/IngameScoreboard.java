package de.febanhd.mlgrush.scoreboard;

import de.febanhd.mlgrush.game.GameSession;
import org.bukkit.entity.Player;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class IngameScoreboard {

    public static void send(Player player, GameSession gameSession) {

        Player otherPlayer = player.equals(gameSession.getPlayer1()) ? gameSession.getPlayer2() : gameSession.getPlayer1();
        int wins = gameSession.getPoints(player);
        int losses = gameSession.getPoints(otherPlayer);

        final Scoreboard scoreboard = new Scoreboard();
        final ScoreboardObjective obj = scoreboard.registerObjective("abc", IScoreboardCriteria.b);
        obj.setDisplayName("§6MLG§eRush");
        final PacketPlayOutScoreboardObjective createpacket = new PacketPlayOutScoreboardObjective(obj, 0);
        final PacketPlayOutScoreboardDisplayObjective display = new PacketPlayOutScoreboardDisplayObjective(1, obj);
        final ScoreboardScore s1 = new ScoreboardScore(scoreboard, obj, " ");
        final ScoreboardScore s2 = new ScoreboardScore(scoreboard, obj, "§eMap");
        final ScoreboardScore s3 = new ScoreboardScore(scoreboard, obj, "§7> §6" + gameSession.getMapTemplate().getName());
        final ScoreboardScore s5 = new ScoreboardScore(scoreboard, obj, "  ");
        final ScoreboardScore s6 = new ScoreboardScore(scoreboard, obj, "§eSiege");
        final ScoreboardScore s7 = new ScoreboardScore(scoreboard, obj, "§7> §6" + wins + "/" + gameSession.getPointsForWin());
        final ScoreboardScore s8 = new ScoreboardScore(scoreboard, obj, "   ");
        final ScoreboardScore s9 = new ScoreboardScore(scoreboard, obj, "§eVerloren");
        final ScoreboardScore s10 = new ScoreboardScore(scoreboard, obj, "§7> §6" + losses);
        final ScoreboardScore s11 = new ScoreboardScore(scoreboard, obj, "    ");
        s1.setScore(10);
        s2.setScore(9);
        s3.setScore(8);
        s5.setScore(7);
        s6.setScore(6);
        s7.setScore(5);
        s8.setScore(4);
        s9.setScore(3);
        s10.setScore(2);
        s11.setScore(1);
        final PacketPlayOutScoreboardObjective removePacket = new PacketPlayOutScoreboardObjective(obj, 1);
        final PacketPlayOutScoreboardScore pa1 = new PacketPlayOutScoreboardScore(s1);
        final PacketPlayOutScoreboardScore pa2 = new PacketPlayOutScoreboardScore(s2);
        final PacketPlayOutScoreboardScore pa3 = new PacketPlayOutScoreboardScore(s3);
        final PacketPlayOutScoreboardScore pa4 = new PacketPlayOutScoreboardScore(s5);
        final PacketPlayOutScoreboardScore pa5 = new PacketPlayOutScoreboardScore(s6);
        final PacketPlayOutScoreboardScore pa6 = new PacketPlayOutScoreboardScore(s7);
        final PacketPlayOutScoreboardScore pa7 = new PacketPlayOutScoreboardScore(s8);
        final PacketPlayOutScoreboardScore pa8 = new PacketPlayOutScoreboardScore(s9);
        final PacketPlayOutScoreboardScore pa9 = new PacketPlayOutScoreboardScore(s10);
        final PacketPlayOutScoreboardScore pa10 = new PacketPlayOutScoreboardScore(s11);
        sendPacket(removePacket, player);
        sendPacket(createpacket, player);
        sendPacket(display, player);
        sendPacket(pa1, player);
        sendPacket(pa2, player);
        sendPacket(pa3, player);
        sendPacket(pa4, player);
        sendPacket(pa5, player);
        sendPacket(pa6, player);
        sendPacket(pa7, player);
        sendPacket(pa8, player);
        sendPacket(pa9, player);
        sendPacket(pa10, player);
    }

    private static void sendPacket(final Packet<?> packet, final Player p) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }
}
