package com.example.mapart.runtime;

import com.example.mapart.MapArtMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class DebugReporter {
    private static final String CHAT_PREFIX = "[MapArt Debug] ";

    private final Path logPath;

    public DebugReporter() {
        this(defaultPath());
    }

    public DebugReporter(Path logPath) {
        this.logPath = logPath;
    }

    public Path logPath() {
        return logPath;
    }

    public void logToChatAndFile(String message) {
        log(message, true);
    }

    public void logToFile(String message) {
        log(message, false);
    }

    private void log(String message, boolean includeChat) {
        String trimmedMessage = message == null ? "" : message.trim();
        if (trimmedMessage.isEmpty()) {
            return;
        }

        String line = Instant.now() + " " + trimmedMessage;
        appendLine(line);
        MapArtMod.LOGGER.info("[debug] {}", trimmedMessage);

        if (!includeChat) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        client.execute(() -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal(CHAT_PREFIX + trimmedMessage), false);
            }
        });
    }

    private void appendLine(String line) {
        try {
            Path parent = logPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(logPath, line + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            MapArtMod.LOGGER.warn("Failed to append debug log file {}.", logPath, exception);
        }
    }

    private static Path defaultPath() {
        try {
            return FabricLoader.getInstance().getConfigDir().resolve("mapartrunner-debug.log");
        } catch (Exception ignored) {
            return Path.of("config", "mapartrunner-debug.log");
        }
    }
}
