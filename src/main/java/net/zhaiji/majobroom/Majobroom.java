package net.zhaiji.majobroom;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.zhaiji.majobroom.register.InitCreativeModeTab;
import net.zhaiji.majobroom.register.InitEntity;
import net.zhaiji.majobroom.register.InitItem;

@Mod(Majobroom.MOD_ID)
public class Majobroom {
    public static final String MOD_ID = "majobroom";

    public Majobroom(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MajoBroomConfig.SPEC);
        InitItem.ITEMS.register(modEventBus);
        InitCreativeModeTab.CREATIVE_MODE_TAB.register(modEventBus);
        InitEntity.ENTITY_TYPES.register(modEventBus);
    }
}
