package net.dzikoysk.funnyguilds.config;

import eu.okaeri.configs.OkaeriConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import net.dzikoysk.funnyguilds.config.sections.items.ItemsConfiguration;
import net.dzikoysk.funnyguilds.config.tablist.TablistConfiguration;
import net.dzikoysk.funnyguilds.config.tablist.TablistPage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigurationValidation {

    private ConfigurationValidation() {
    }

    private static void require(boolean condition, String key) {
        if (!condition) { throw new IllegalArgumentException("Invalid configuration: " + key); }
    }

    public static void validate(PluginConfiguration config) {
        require(config.createNameMinLength <= config.createNameLength, "config.yml: create-name-min-length > create-name-length");
        require(config.createTagMinLength <= config.createTagLength, "config.yml: create-tag-min-length > create-tag-length");
        require(config.playerNameMinLength <= config.playerNameMaxLength, "config.yml: player-name-min-length > player-name-max-length");
        require(config.war.maxLives >= config.war.lives, "config.yml: war.max-lives < war.lives");
        require(config.war.lives > 0 && config.war.heartLives > 0, "config.yml: war.lives / war.heart-lives must be positive");
        require(!config.war.antiFarming.minVictimAge.isNegative() && !config.war.antiFarming.victimCooldown.isNegative(), "config.yml: war.anti-farming durations must not be negative");
        require(config.dataInterval > 0 && config.rankingUpdateInterval > 0, "config.yml: data-interval / ranking-update-interval must be positive");
        require(config.regionSize > 0 && config.enlargeSize >= 0, "config.yml: region-size / enlarge-size");
        require(Double.isFinite(config.assistKillerShare) && config.assistKillerShare >= 0 && config.assistKillerShare <= 1, "config.yml: assist-killer-share must be in [0,1]");
        require(Double.isFinite(config.buyLife.money) && config.buyLife.money >= 0, "config.yml: buy-life.money must be finite and nonnegative");
        require(Double.isFinite(config.buyLife.scalingCost.costMultiplierPerLife) && config.buyLife.scalingCost.costMultiplierPerLife >= 0, "config.yml: buy-life.scaling-cost.cost-multiplier-per-life");
        require(config.scoreboard.nametag.updateRate.getSeconds() >= 1 && config.scoreboard.dummy.updateRate.getSeconds() >= 1,
                "config.yml: scoreboard update rates must be at least 1s");
        require(config.scoreboard.queueConfiguration.updateRate > 0 && config.scoreboard.queueConfiguration.maxUpdatesInTick > 0, "config.yml: scoreboard queue limits must be positive");
        require(config.dynmapHook.updateInterval > 0, "config.yml: dynmap-hook.update-interval must be positive");
        require(config.mysql.usersTableName.matches("[A-Za-z0-9_]+") && config.mysql.guildsTableName.matches("[A-Za-z0-9_]+")
                && config.mysql.regionsTableName.matches("[A-Za-z0-9_]+"), "config.yml: mysql table names must contain only letters, digits and underscores");
    }

    public static void validate(TablistConfiguration config) {
        require(config.updateInterval > 0, "tablist.yml: update-interval must be positive");
        require(config.pages != null && (!config.animated || !config.pages.isEmpty()), "tablist.yml: animated requires nonempty pages");
        slots(config.cells, "tablist.yml: cells");
        for (TablistPage page : config.pages) {
            require(page != null && page.cycles() > 0, "tablist.yml: pages.cycles must be positive");
            if (page.cells() != null) { slots(page.cells(), "tablist.yml: pages.cells"); }
        }
        config.heads.textures.forEach((range, texture) -> require(range.getMinRange().intValue() >= 1
                && range.getMaxRange().intValue() <= 80 && range.getMinRange().intValue() <= range.getMaxRange().intValue(),
                "tablist.yml: heads.textures range must be within 1..80"));
    }

    private static void slots(Map<Integer, ?> values, String key) {
        require(values != null && values.keySet().stream().allMatch(slot -> slot != null && slot >= 1 && slot <= 80), key + " slots must be within 1..80");
    }

    public static void validate(ItemsConfiguration config) {
        require(!config.getGuildItemSets().isEmpty(), "items.yml: guild-item-sets is empty");
        config.getGuildItemSets().forEach((name, set) -> {
            require(set.requiredMoney >= 0 && Double.isFinite(set.requiredMoney) && set.requiredLevel >= 0 && set.requiredRank >= 0, "items.yml: guild-item-sets." + name + " requirements");
            set.getItems().forEach((key, amount) -> require(amount != null && amount > 0 && config.getLibraryItem(key).isPresent(), "items.yml: guild-item-sets." + name + "." + key));
        });
        config.guildItemLibrary.forEach((key, value) -> require(value != null && Material.matchMaterial(value.material) != null, "items.yml: guild-item-library." + key + ".material"));
    }

    public static void requireReloadable(PluginConfiguration current, PluginConfiguration candidate) {
        requireReloadable(current.saveToString(), candidate.saveToString());
    }

    static void requireReloadable(String current, String candidate) {
        try {
            YamlConfiguration before = new YamlConfiguration();
            YamlConfiguration after = new YamlConfiguration();
            before.loadFromString(current);
            after.loadFromString(candidate);
            for (String key : List.of("data-model", "mysql", "commands", "regions-enabled", "heart", "region-size", "enlarge-size", "enlarge-items", "default-locale", "available-locales", "disabled-hooks")) {
                require(Objects.equals(normalize(before.get(key)), normalize(after.get(key))), "config.yml: " + key + " changed; restart the server to apply it");
            }
        }
        catch (org.bukkit.configuration.InvalidConfigurationException exception) {
            throw new IllegalArgumentException("Cannot validate reload configuration", exception);
        }
    }

    private static Object normalize(Object value) {
        if (!(value instanceof ConfigurationSection section)) { return value; }
        Map<String, Object> result = new TreeMap<>();
        section.getKeys(false).forEach(key -> result.put(key, normalize(section.get(key))));
        return result;
    }

    /** Retain configuration object identities used by injected services. */
    public static void copyValues(OkaeriConfig source, OkaeriConfig target) {
        try {
            for (Class<?> type = source.getClass(); type != OkaeriConfig.class && type != null; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || field.isSynthetic()) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = field.get(source);
                    Object existing = field.get(target);
                    if (value instanceof OkaeriConfig sourceSection && existing instanceof OkaeriConfig targetSection) {
                        copyValues(sourceSection, targetSection);
                    }
                    else {
                        field.set(target, value);
                    }
                }
            }
        }
        catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot apply validated configuration", exception);
        }
    }
}
