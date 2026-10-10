package com.nutrisafio.mobs;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Plantilla de herreria con su descripcion (como las plantillas normales). */
public class TemplateItem extends Item {
    public TemplateItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Plantilla de herreria").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("Mejora: Armadura de Ojos").withStyle(ChatFormatting.DARK_RED));
        lines.add(Component.literal("Usala en la mesa de herreria").withStyle(ChatFormatting.DARK_GRAY));
    }
}
