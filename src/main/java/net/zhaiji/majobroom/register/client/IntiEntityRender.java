package net.zhaiji.majobroom.register.client;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.zhaiji.majobroom.Majobroom;
import net.zhaiji.majobroom.entity.MajoBroomEntity;
import net.zhaiji.majobroom.entity.MajoBroomModel;
import net.zhaiji.majobroom.entity.MajoBroomRender;

@EventBusSubscriber(modid = Majobroom.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class IntiEntityRender {
    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityRenderers.register(MajoBroomEntity.TYPE, MajoBroomRender::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MajoBroomModel.LAYER, MajoBroomModel::createBodyLayer);
    }
}
