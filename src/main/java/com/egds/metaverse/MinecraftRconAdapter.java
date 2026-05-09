package com.egds.metaverse;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Minecraft RCON adapter for metaverse digital-twin sync.
 *
 * <p>Assembles RCON (Remote Console) protocol packets targeting
 * a virtual Minecraft server and asynchronously raises a build
 * event that constructs "Hello World" in the night sky at
 * Y=200 using wool blocks. No real network socket is opened;
 * packet assembly is logged and the future resolves locally.
 */
@Component
public class MinecraftRconAdapter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(
                    MinecraftRconAdapter.class);

    /** RCON packet type identifier for authentication. */
    private static final int RCON_TYPE_LOGIN = 3;

    /** RCON packet type identifier for command execution. */
    private static final int RCON_TYPE_COMMAND = 2;

    /** Sky layer Y-coordinate for block placement. */
    private static final int BUILD_Y = 200;

    /** Character column width in blocks. */
    private static final int CHAR_WIDTH = 5;

    /** Horizontal gap between characters in blocks. */
    private static final int CHAR_GAP = 1;

    /** Additional gap inserted between words in blocks. */
    private static final int WORD_GAP = 2;

    /** Byte size of each integer field in an RCON packet. */
    private static final int RCON_FIELD_SIZE = 4;

    /**
     * Asynchronously assembles RCON packets and simulates
     * building "Hello World" in a Minecraft sky at
     * Y={@value #BUILD_Y}.
     *
     * @param correlationId request correlation identifier
     * @return future that completes when all packets are built
     */
    public CompletableFuture<Void> buildHelloWorldAsync(
            final String correlationId) {
        return CompletableFuture.runAsync(
                () -> executeBuild(correlationId));
    }

    private void executeBuild(final String correlationId) {
        LOG.info(
                "RCON digital-twin sync started "
                        + "correlationId={}",
                correlationId);
        byte[] loginPkt = assemblePacket(
                1, RCON_TYPE_LOGIN, "egds-rcon-secret");
        LOG.debug(
                "RCON login packet assembled len={}",
                loginPkt.length);
        String[] words = {"Hello", "World"};
        int xOffset = 0;
        for (String word : words) {
            for (char ch : word.toCharArray()) {
                String cmd = String.format(
                        "/fill %d %d %d %d %d %d"
                                + " minecraft:white_wool",
                        xOffset, BUILD_Y, 0,
                        xOffset + CHAR_WIDTH, BUILD_Y, 0);
                byte[] cmdPkt = assemblePacket(
                        2, RCON_TYPE_COMMAND, cmd);
                LOG.info(
                        "RCON cmd char='{}'"
                                + " xOffset={} pktLen={}",
                        ch, xOffset, cmdPkt.length);
                xOffset += CHAR_WIDTH + CHAR_GAP;
            }
            xOffset += WORD_GAP;
        }
        LOG.info(
                "RCON digital-twin sync complete "
                        + "correlationId={}",
                correlationId);
    }

    /**
     * Assembles a minimal RCON protocol packet per the
     * Source RCON protocol specification (little-endian).
     *
     * @param requestId packet request identifier
     * @param type      packet type (login or command)
     * @param payload   UTF-8 payload string
     * @return raw packet bytes in little-endian byte order
     */
    public byte[] assemblePacket(
            final int requestId,
            final int type,
            final String payload) {
        byte[] payloadBytes =
                payload.getBytes(StandardCharsets.UTF_8);
        int bodyLen = RCON_FIELD_SIZE + RCON_FIELD_SIZE
                + payloadBytes.length + 2;
        ByteBuffer buf =
                ByteBuffer.allocate(RCON_FIELD_SIZE + bodyLen);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(bodyLen);
        buf.putInt(requestId);
        buf.putInt(type);
        buf.put(payloadBytes);
        buf.put((byte) 0);
        buf.put((byte) 0);
        return buf.array();
    }
}
