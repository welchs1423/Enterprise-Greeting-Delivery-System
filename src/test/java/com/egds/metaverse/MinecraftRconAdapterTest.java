package com.egds.metaverse;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("MinecraftRconAdapter")
class MinecraftRconAdapterTest {

    @Test
    @DisplayName("assemblePacket returns non-empty byte array")
    void assemblePacketReturnsNonEmptyBytes() {
        MinecraftRconAdapter adapter =
                new MinecraftRconAdapter();
        byte[] packet = adapter.assemblePacket(
                1, 3, "test");
        assertThat(packet).isNotEmpty();
    }

    @Test
    @DisplayName("assemblePacket length matches RCON structure")
    void assemblePacketHasCorrectLength() {
        MinecraftRconAdapter adapter =
                new MinecraftRconAdapter();
        String payload = "hello";
        byte[] packet = adapter.assemblePacket(
                1, 2, payload);
        int expectedLen = 4 + 4 + 4 + payload.length() + 2;
        assertThat(packet).hasSize(expectedLen);
    }

    @Test
    @DisplayName("assemblePacket uses little-endian byte order")
    void assemblePacketUsesLittleEndian() {
        MinecraftRconAdapter adapter =
                new MinecraftRconAdapter();
        byte[] packet = adapter.assemblePacket(1, 3, "");
        int bodyLen = 4 + 4 + 0 + 2;
        assertThat(packet[0]).isEqualTo((byte) bodyLen);
        assertThat(packet[1]).isEqualTo((byte) 0);
    }

    @Test
    @DisplayName("buildHelloWorldAsync completes without exception")
    void buildHelloWorldAsyncCompletesWithoutException() {
        MinecraftRconAdapter adapter =
                new MinecraftRconAdapter();
        CompletableFuture<Void> future =
                adapter.buildHelloWorldAsync("test-rcon-001");
        assertThatCode(future::get)
                .doesNotThrowAnyException();
    }
}
