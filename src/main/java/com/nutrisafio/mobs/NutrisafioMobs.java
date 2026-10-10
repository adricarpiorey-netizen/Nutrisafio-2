package com.nutrisafio.mobs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Nutrisafio Mobs: mobs nuevos del servidor Nutrisafio.
 * - Jester: bufon que explota y deja la pantalla llena de confeti.
 * - Beast: perro demonio de la Noche de Sangre.
 * - Broken Soul: alma rota neutral del Nether, suelta Huesos de Wither.
 * - Quibungo: el mas peligroso; te agarra, te sacude y te muerde.
 */
public class NutrisafioMobs implements ModInitializer {
    public static final String MOD_ID = "nutrisafio_mobs";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    // ---- Jester ----
    public static final EntityType<JesterEntity> JESTER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, id("jester"),
            EntityType.Builder.of(JesterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build("jester"));

    public static final Item JESTER_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM, id("jester_spawn_egg"),
            new SpawnEggItem(JESTER, 0x6B0F2E, 0xD9A441, new Item.Properties()));

    // ---- Beast ----
    public static final EntityType<BeastEntity> BEAST = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, id("beast"),
            EntityType.Builder.of(BeastEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 1.6F)
                    .clientTrackingRange(10)
                    .build("beast"));

    public static final Item BEAST_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM, id("beast_spawn_egg"),
            new SpawnEggItem(BEAST, 0x8E0F0F, 0xF2EBC4, new Item.Properties()));

    // ---- Broken Soul ----
    public static final EntityType<BrokenSoulEntity> BROKEN_SOUL = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, id("broken_soul"),
            EntityType.Builder.of(BrokenSoulEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.9F)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .build("broken_soul"));

    public static final Item BROKEN_SOUL_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM, id("broken_soul_spawn_egg"),
            new SpawnEggItem(BROKEN_SOUL, 0x1A1A1A, 0xA10E0E, new Item.Properties()));

    // ---- Quibungo ----
    public static final EntityType<QuibungoEntity> QUIBUNGO = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, id("quibungo"),
            EntityType.Builder.of(QuibungoEntity::new, MobCategory.MONSTER)
                    .sized(1.8F, 3.5F)
                    .clientTrackingRange(10)
                    .build("quibungo"));

    public static final Item QUIBUNGO_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM, id("quibungo_spawn_egg"),
            new SpawnEggItem(QUIBUNGO, 0x7A1C14, 0xE8B79A, new Item.Properties()));

    // ---- Items ----
    public static final Item WITHER_BONE = Registry.register(
            BuiltInRegistries.ITEM, id("wither_bone"), new Item(new Item.Properties()));

    public static final Item QUIBUNGO_TOOTH = Registry.register(
            BuiltInRegistries.ITEM, id("quibungo_tooth"), new Item(new Item.Properties()));

    // Gota de Sangre: la sueltan a veces el Beast (40%) y el Quibungo (60%)
    public static final Item BLOOD_DROP = Registry.register(
            BuiltInRegistries.ITEM, id("blood_drop"), new Item(new Item.Properties()));

    // Corazon: se come como una chuleta y da Regeneracion I por 20 segundos.
    // Se puede comer aunque tengas la barra de comida llena.
    public static final Item HEART = Registry.register(
            BuiltInRegistries.ITEM, id("heart"),
            new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(0.6F)
                    .alwaysEdible()
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 0), 1.0F)
                    .build())));

    // Corazon Superior: Regeneracion II y Resistencia II por 1 minuto y medio.
    public static final Item GREATER_HEART = Registry.register(
            BuiltInRegistries.ITEM, id("greater_heart"),
            new Item(new Item.Properties().rarity(Rarity.RARE).food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(0.6F)
                    .alwaysEdible()
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, 90 * 20, 1), 1.0F)
                    .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 90 * 20, 1), 1.0F)
                    .build())));

    // Corazon Dorado: +1 corazon de vida maxima para siempre (tope 10 extra).
    public static final Item GOLDEN_HEART = Registry.register(
            BuiltInRegistries.ITEM, id("golden_heart"),
            new GoldenHeartItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.6F)
                            .alwaysEdible()
                            .build())));

    // Plantilla de Mejora Maldita: para mejorar la armadura en la mesa de herreria
    public static final Item EYE_UPGRADE_TEMPLATE = Registry.register(
            BuiltInRegistries.ITEM, id("eye_upgrade_template"),
            new TemplateItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ---- Armadura Maldita (como netherita + 1 corazon por pieza) ----
    public static final Item CURSED_HELMET = Registry.register(
            BuiltInRegistries.ITEM, id("cursed_helmet"),
            new CursedArmor.Piece(ArmorItem.Type.HELMET, new Item.Properties()
                    .durability(ArmorItem.Type.HELMET.getDurability(37)).fireResistant()));

    public static final Item CURSED_CHESTPLATE = Registry.register(
            BuiltInRegistries.ITEM, id("cursed_chestplate"),
            new CursedArmor.Piece(ArmorItem.Type.CHESTPLATE, new Item.Properties()
                    .durability(ArmorItem.Type.CHESTPLATE.getDurability(37)).fireResistant()));

    public static final Item CURSED_LEGGINGS = Registry.register(
            BuiltInRegistries.ITEM, id("cursed_leggings"),
            new CursedArmor.Piece(ArmorItem.Type.LEGGINGS, new Item.Properties()
                    .durability(ArmorItem.Type.LEGGINGS.getDurability(37)).fireResistant()));

    public static final Item CURSED_BOOTS = Registry.register(
            BuiltInRegistries.ITEM, id("cursed_boots"),
            new CursedArmor.Piece(ArmorItem.Type.BOOTS, new Item.Properties()
                    .durability(ArmorItem.Type.BOOTS.getDurability(37)).fireResistant()));

    // ---- Cofre con Candado ----
    public static final Block LOCKED_CHEST = Registry.register(
            BuiltInRegistries.BLOCK, id("locked_chest"),
            new LockedChestBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F, 1200.0F)   // no lo rompen las explosiones
                    .sound(SoundType.WOOD)));

    public static final Item LOCKED_CHEST_ITEM = Registry.register(
            BuiltInRegistries.ITEM, id("locked_chest"), new BlockItem(LOCKED_CHEST, new Item.Properties()));

    public static final BlockEntityType<LockedChestBlockEntity> LOCKED_CHEST_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, id("locked_chest"),
            BlockEntityType.Builder.of(LockedChestBlockEntity::new, LOCKED_CHEST).build(null));

    // ---- Pestana propia en el menu creativo ----
    public static final CreativeModeTab TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, id("nutrisafio_mobs"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(WITHER_BONE))
                    .title(Component.translatable("itemGroup.nutrisafio_mobs"))
                    .displayItems((params, output) -> {
                        output.accept(JESTER_SPAWN_EGG);
                        output.accept(BEAST_SPAWN_EGG);
                        output.accept(BROKEN_SOUL_SPAWN_EGG);
                        output.accept(QUIBUNGO_SPAWN_EGG);
                        output.accept(WITHER_BONE);
                        output.accept(QUIBUNGO_TOOTH);
                        output.accept(BLOOD_DROP);
                        output.accept(LOCKED_CHEST_ITEM);
                        output.accept(EYE_UPGRADE_TEMPLATE);
                        output.accept(CURSED_HELMET);
                        output.accept(CURSED_CHESTPLATE);
                        output.accept(CURSED_LEGGINGS);
                        output.accept(CURSED_BOOTS);
                        output.accept(HEART);
                        output.accept(GREATER_HEART);
                        output.accept(GOLDEN_HEART);
                    })
                    .build());

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(JESTER, JesterEntity.createAttributes());

        // Aparece como un monstruo normal: de noche / en la oscuridad, sobre el suelo
        SpawnPlacements.register(JESTER, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        // Peso 30: menos seguido que un creeper (100)
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
                MobCategory.MONSTER, JESTER, 30, 1, 1);

        // Beast: no aparece solo; solo durante la Noche de Sangre (ver BloodNightSpawner)
        FabricDefaultAttributeRegistry.register(BEAST, BeastEntity.createAttributes());
        ServerTickEvents.END_SERVER_TICK.register(BloodNightSpawner::tick);

        // Broken Soul: en todo el Nether, poco comun (peso 8; el Enderman del Nether tiene 10)
        FabricDefaultAttributeRegistry.register(BROKEN_SOUL, BrokenSoulEntity.createAttributes());
        SpawnPlacements.register(BROKEN_SOUL, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules);
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(),
                MobCategory.MONSTER, BROKEN_SOUL, 8, 1, 1);

        // Quibungo: de noche en el mundo normal, tan raro como una bruja (peso 5)
        FabricDefaultAttributeRegistry.register(QUIBUNGO, QuibungoEntity.createAttributes());
        SpawnPlacements.register(QUIBUNGO, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
                MobCategory.MONSTER, QUIBUNGO, 5, 1, 1);

        // Los corazones extra del Corazon Dorado se conservan al morir
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
                GoldenHeartItem.setExtraHearts(newPlayer, GoldenHeartItem.extraHearts(oldPlayer)));

        // Paquete que avisa al jugador que le muestre el confeti
        PayloadTypeRegistry.playS2C().register(ConfettiPayload.TYPE, ConfettiPayload.CODEC);

        // Todo esta en su propia pestana del menu creativo (ver TAB arriba)
    }
}
