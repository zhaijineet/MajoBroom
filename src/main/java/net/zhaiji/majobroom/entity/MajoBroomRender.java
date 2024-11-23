package net.zhaiji.majobroom.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.zhaiji.majobroom.Majobroom;
import org.slf4j.Logger;

public class MajoBroomRender extends EntityRenderer<MajoBroomEntity> {
    private static final ResourceLocation BROOM_TEXTURE = ResourceLocation.fromNamespaceAndPath(Majobroom.MOD_ID, "textures/entity/broom.png");
    private static final Logger LOGGER = LogUtils.getLogger();

    private final EntityModel<MajoBroomEntity> majoBroomEntityEntityModel;

    public MajoBroomRender(EntityRendererProvider.Context context) {
        super(context);
        majoBroomEntityEntityModel = new MajoBroomModel(context.bakeLayer(MajoBroomModel.LAYER));
    }

    @Override
    public ResourceLocation getTextureLocation(MajoBroomEntity entityBroom) {
        return BROOM_TEXTURE;
    }

    @Override
    public void render(MajoBroomEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.0f, 1.0f, 1.0f);
        poseStack.translate(0.0, -1.0, 0.0);

        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

        RenderType renderType = majoBroomEntityEntityModel.renderType(getTextureLocation(entity));
        VertexConsumer buffer = bufferIn.getBuffer(renderType);
        majoBroomEntityEntityModel.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}