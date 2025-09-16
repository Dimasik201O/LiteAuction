package org.dimasik.liteauction.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum CategoryType {
    ALL("Все подряд", new HashSet<>()),
    TOOLS("Инструменты", new HashSet<>(Arrays.asList("pickaxe", "axe", "shovel", "hoe", "flint_and_steel"))),
    WEAPONS("Оружие", new HashSet<>(Arrays.asList("sword", "bow", "crossbow", "trident", "arrow"))),
    BLOCKS("Блоки", new HashSet<>(Arrays.asList("block", "stone", "dirt", "sand", "ore"))),
    DECORATIVE_BLOCKS("Декоративные блоки", new HashSet<>(Arrays.asList("glass", "flower"))),
    MECHANISMS("Механизмы", new HashSet<>(Arrays.asList("tnt", "fire_charge", "piston", "lever", "tripwire_hook", "rail", "hopper", "observer", "redstone", "slime", "minecart"))),
    ALCHEMY("Алхимия", new HashSet<>(Arrays.asList("glass_bottle", "glowstone_dust", "golden_carrot", "gunpowder", "brown_mushroom", "fermented_spider_eye", "blaze_powder", "magma_cream", "nether_wart", "turtle_helmet", "brewing_stand", "phantom_membrane", "red_mushroom", "dragon_breath", "sugar", "glistering_melon_slice", "ghast_tear", "rabbit_foot"))),
    FOOD("Еда", new HashSet<>(Arrays.asList("apple", "melon", "berries", "chorus", "carrot", "potato", "porkchop", "beef", "beetroot", "mutton", "chicken", "rabbit", "cod", "salmon", "tropical_fish", "pufferfish", "bread", "cookie", "cake", "pumpkin", "mushroom_stew", "rabbit_stew", "honey_bottle"))),
    POTIONS("Зелья", new HashSet<>(List.of("potion"))),
    ENCHANTING("Зачарование", new HashSet<>(Arrays.asList("enchant", "enchantment", "book"))),
    ARMOR("Броня", new HashSet<>(Arrays.asList("helmet", "chestplate", "leggings", "boots", "shield", "elytra"))),
    UNIQUE("Уникальные предметы", new HashSet<>(Arrays.asList("spawner", "_spawn_egg"))),
    DONATE("Донатные вещи", new HashSet<>(Arrays.asList("spawner", "_spawn_egg"))),
    XP_KEEPER("Хранитель опыта", new HashSet<>(Arrays.asList("expbottle", "experience"))),
    SPHERES_TALISMANS("Сферы и талисманы", new HashSet<>(List.of("pdc: holysfers:1"))),
    JEWELS("Драгоценности", new HashSet<>(Arrays.asList("jewel", "gem", "diamond", "emerald", "ruby"))),
    PLANTS("Растительность", new HashSet<>(Arrays.asList("flower", "sapling", "seeds", "crop"))),
    MISC("Разное", new HashSet<>(List.of("stone", "dirt"))),
    MOB_LOOT("Лут с мобов", new HashSet<>(Arrays.asList("spider_eye", "rotten_flesh", "string", "bone")));

    private final String displayName;
    private final Set<String> tags;

    public CategoryType relative(boolean next) {
        CategoryType[] values = values();
        int currentOrdinal = this.ordinal();
        int nextOrdinal;

        if (next) {
            nextOrdinal = currentOrdinal + 1;
            if (nextOrdinal >= values.length) {
                nextOrdinal = 0;
            }
        } else {
            nextOrdinal = currentOrdinal - 1;
            if (nextOrdinal < 0) {
                nextOrdinal = values.length - 1;
            }
        }

        return values[nextOrdinal];
    }
}