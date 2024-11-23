package net.zhaiji.majobroom.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zhaiji.majobroom.Majobroom;

import java.util.function.Supplier;

public class InitCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Majobroom.MOD_ID);

    public static final Supplier<CreativeModeTab> MAJOBROOM_TAB = CREATIVE_MODE_TAB.register(
            "majobroom_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(InitItem.MAJO_BROOM.get()))
                    .title(Component.translatable("creativetab.majobroom.majobroom_tab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        InitItem.ITEMS.getEntries().forEach(itemDeferredHolder -> {
                            output.accept(itemDeferredHolder.get());
                        });
                    }))
                    .build()
    );
}
