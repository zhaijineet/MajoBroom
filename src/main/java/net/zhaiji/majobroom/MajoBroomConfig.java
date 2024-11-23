package net.zhaiji.majobroom;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Majobroom.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MajoBroomConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder()
            .comment("设置")
            .push("Config");

    private static final ModConfigSpec.DoubleValue VALUE = BUILDER
            .comment("速度")
            .defineInRange(
                    "Speed",
                    0.3,
                    0.0,
                    5.0
            );

    public static final ModConfigSpec SPEC = BUILDER.pop().build();

    public static double Speed;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        Speed = VALUE.get();
    }
}
