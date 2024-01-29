package me.monkey_cat.bungeecordwhitelistct.utils.config;

import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileConfig {
    protected final File file;
    protected final String defaultPath;
    protected final FileYamlConfiguration configuration;
    protected long lastModified;

    public FileConfig(Path configPath, String defaultPath) {
        this.file = configPath.toFile();
        this.defaultPath = defaultPath;
        configuration = new FileYamlConfiguration(this.file);
        configuration.load();
    }

    public void save() {
        configuration.save();
    }

    public boolean tryLoad() {
        if (file.isFile()) {
            final long fileLastMod = file.lastModified();

            if (fileLastMod == lastModified) return false;
            if (configuration.load()) {
                try {
                    parse();
                    lastModified = fileLastMod;
                    return true;
                } catch (Exception ignored) {
                }
            }
        }

        writeDefault();
        return tryLoad();
    }

    public void writeDefault() {
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(defaultPath)) {
            Files.delete(file.toPath());
            Files.copy(Objects.requireNonNull(in), file.toPath());
        } catch (Exception ignored) {
        }
    }

    public void parse() {
    }

    protected Map<String, Set<String>> getItem(String path, boolean reload) {
        if (reload) tryLoad();

        Map<String, Set<String>> data = new HashMap<>();
        configuration.getMap(path).forEach((s, o) -> {
            if (o instanceof List<?>) {
                List<String> names = ((List<?>) o).stream().map(Object::toString).toList();
                data.put(s, new HashSet<>(names));
            }
        });

        return data;
    }

    protected Map<String, Set<String>> getItem(String path) {
        return getItem(path, true);
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}