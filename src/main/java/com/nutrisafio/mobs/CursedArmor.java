package com.nutrisafio.mobs;

import com.google.common.base.Suppliers;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Armadura Maldita: igual de fuerte que la de netherita,
 * y cada pieza puesta te da 1 corazon extra (4 piezas = 4 corazones).
 * Se consigue en la mesa de herreria: pieza de diamante + Plantilla de Mejora Maldita + lingote de netherita.
 */
public final class CursedArmor {
    private CursedArmor() {}

    public static final Holder<ArmorMaterial> MATERIAL = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL, NutrisafioMobs.id("cursed"),
            new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        // Mismos valores que la netherita
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 11);
                    }),
                    15,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(Items.NETHERITE_INGOT),
                    List.of(new ArmorMaterial.Layer(NutrisafioMobs.id("cursed"))),
                    3.0F,
                    0.1F));

    /** Pieza de armadura que ademas suma 1 corazon (2 de vida) de vida maxima. */
    public static class Piece extends ArmorItem {
        private final Supplier<ItemAttributeModifiers> modifiers;

        public Piece(ArmorItem.Type type, Item.Properties properties) {
            super(MATERIAL, type, properties);
            this.modifiers = Suppliers.memoize(() -> {
                ItemAttributeModifiers base = super.getDefaultAttributeModifiers();
                ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                for (ItemAttributeModifiers.Entry e : base.modifiers()) {
                    builder.add(e.attribute(), e.modifier(), e.slot());
                }
                builder.add(Attributes.MAX_HEALTH,
                        new AttributeModifier(NutrisafioMobs.id("cursed_health_" + type.getName()), 2.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.bySlot(type.getSlot()));
                return builder.build();
            });
        }

        @Override
        public ItemAttributeModifiers getDefaultAttributeModifiers() {
            return this.modifiers.get();
        }
    }

    /** Herramienta pequena para armar el mapa (igual que net.minecraft.Util.make). */
    private static final class Util {
        static <T> T make(T object, java.util.function.Consumer<T> init) {
            init.accept(object);
            return object;
        }
    }
}
