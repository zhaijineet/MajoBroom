package net.zhaiji.majobroom.register;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zhaiji.majobroom.Majobroom;
import net.zhaiji.majobroom.entity.MajoBroomEntity;

public class InitItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Majobroom.MOD_ID);

    public static final DeferredItem<Item> MAJO_BROOM = ITEMS.register(
            "majo_broom",
            () -> new Item(new Item.Properties().stacksTo(1)){
                @Override
                public InteractionResult useOn(UseOnContext context) {
                    if (context.getClickedFace() != Direction.DOWN) {
                        Level world = context.getLevel();
                        BlockPos clickedPos = new BlockPlaceContext(context).getClickedPos();
                        AABB boundingBox = MajoBroomEntity.TYPE.getDimensions().makeBoundingBox(Vec3.atBottomCenterOf(clickedPos));
                        if (world.noCollision(boundingBox) && world.getEntities(null, boundingBox).isEmpty()) {
                            ItemStack stack = context.getItemInHand();
                            if (world instanceof ServerLevel serverWorld) {
                                MajoBroomEntity broom = MajoBroomEntity.TYPE.create(serverWorld, (e) -> {
                                    if (stack.get(DataComponents.CUSTOM_NAME) != null) {
                                        e.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
                                    }
                                }, context.getClickedPos(), MobSpawnType.SPAWN_EGG, true, true);
                                if (broom == null) {
                                    return InteractionResult.FAIL;
                                }
                                world.addFreshEntity(broom);
                                world.playSound(null, broom.getX(), broom.getY(), broom.getZ(), SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                            }
                            stack.shrink(1);
                            return InteractionResult.sidedSuccess(world.isClientSide);
                        }
                    }
                    return InteractionResult.FAIL;
                }
            }
    );
}
