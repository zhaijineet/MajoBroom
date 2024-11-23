package net.zhaiji.majobroom.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zhaiji.majobroom.MajoBroomConfig;
import net.zhaiji.majobroom.register.InitItem;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MajoBroomEntity extends Entity {
    public static final EntityType<MajoBroomEntity> TYPE = EntityType.Builder.<MajoBroomEntity>of(MajoBroomEntity::new, MobCategory.MISC)
            .fireImmune()
            .sized(1.3f, 0.5f)
            .build("majo_broom");

    public static final Logger LOGGER = LogUtils.getLogger();

    private boolean keyForward = false;
    private boolean keyBack = false;
    private boolean keyLeft = false;
    private boolean keyRight = false;
    private boolean keyUp = false;
    private boolean keyDown = false;

    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;

    public MajoBroomEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public MajoBroomEntity(Level level) {
        super(TYPE, level);
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyForward() {
        return Minecraft.getInstance().options.keyUp.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyBack() {
        return Minecraft.getInstance().options.keyDown.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyLeft() {
        return Minecraft.getInstance().options.keyLeft.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyRight() {
        return Minecraft.getInstance().options.keyRight.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyUp() {
        return Minecraft.getInstance().options.keyJump.isDown();
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean keyDown() {
        return Minecraft.getInstance().options.keySprint.isDown();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }

    /**
     * lerp是从boat类里直接拿的
     * 有问题再改（）
     * 但其实我感觉不写也没关系 希望有大佬解答
     */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = steps;
    }

    @Override
    public double lerpTargetX() {
        return this.lerpSteps > 0 ? this.lerpX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.lerpSteps > 0 ? this.lerpY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.lerpSteps > 0 ? (float) this.lerpXRot : this.getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return this.lerpSteps > 0 ? (float) this.lerpYRot : this.getYRot();
    }

    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            this.lerpSteps--;
        }
    }

    // 获取实体掉落物品
    public Item getDropItem() {
        return InitItem.MAJO_BROOM.get();
    }

    // 获取控制乘客
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.getFirstPassenger() instanceof Player player) {
            return player;
        }
        return null;
    }

    // 是否可以添加乘客
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    // 是否可以交互
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    // 是否可以碰撞
    @Override
    public boolean isPushable() {
        return true;
    }

    // 与扫帚交互
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!player.isDiscrete() && this.canAddPassenger(this)) {
            this.setDeltaMovement(Vec3.ZERO);
            if (!this.level().isClientSide) {
                player.setYRot(this.getYRot());
                player.startRiding(this);
            }
            player.setYRot(this.getYRot());
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.interact(player, hand);
    }

    /**
     * 攻击与掉落物品
     * 当扫帚没有乘客 并且玩家按下shift键时
     * 删除实体并给予玩家对应掉落物
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        } else if (this.isInvulnerableTo(source)) {
            return false;
        } else if (source.getEntity() instanceof Player player && player.isDiscrete()) {
            ItemStack itemStack = new ItemStack(this.getDropItem());
            if (this.hasCustomName()) {
                itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
            }
            this.spawnAtLocation(itemStack);
            this.discard();
            return true;
        }
        return false;
    }

    // 无摔落伤害
    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        this.resetFallDistance();
    }

    // 扫帚控制
    // 正是因为如此 有BUG啊有BUG!!!
    public void controlBroom() {
        // 确保本地实例控制扫帚
        if (!this.isControlledByLocalInstance()) return;
        // 如果有乘客在控制
        if (this.isVehicle()) {
            keyForward = keyForward();
            keyBack = keyBack();
            keyLeft = keyLeft();
            keyRight = keyRight();
            keyUp = keyUp();
            keyDown = keyDown();

            // 同步玩家视角方向到扫帚
            this.setYRot(this.getControllingPassenger().getYRot());

            // 获取扫帚方向
            double yawRadians = Math.toRadians(this.getControllingPassenger().getYRot());

            // 当前速度
            double moveX = this.getDeltaMovement().x;
            double moveY = this.getDeltaMovement().y;
            double moveZ = this.getDeltaMovement().z;

            // 推进力增量（加速度）
            double forwardSpeed = 0.1 * MajoBroomConfig.Speed; // 前进速度 2倍
            double backSpeed = 0.05 * MajoBroomConfig.Speed;    // 后退速度
            double lateralSpeed = 0.05 * MajoBroomConfig.Speed; // 左右移动速度
            double verticalSpeed = 0.05 * MajoBroomConfig.Speed; // 上下移动速度
            double friction = 0.95; // 惯性摩擦系数（数值越小，惯性越强）

            // 计算移动方向
            if (keyForward) {
                moveX += Math.cos(yawRadians + Math.PI / 2) * forwardSpeed;
                moveZ += Math.sin(yawRadians + Math.PI / 2) * forwardSpeed;
            }
            if (keyBack) {
                moveX -= Math.cos(yawRadians + Math.PI / 2) * backSpeed;
                moveZ -= Math.sin(yawRadians + Math.PI / 2) * backSpeed;
            }
            if (keyLeft) {
                moveX += Math.cos(yawRadians) * lateralSpeed;
                moveZ += Math.sin(yawRadians) * lateralSpeed;
            }
            if (keyRight) {
                moveX -= Math.cos(yawRadians) * lateralSpeed;
                moveZ -= Math.sin(yawRadians) * lateralSpeed;
            }
            if (keyUp) {
                moveY += verticalSpeed;
            }
            if (keyDown) {
                moveY -= verticalSpeed;
            }

            // 应用摩擦力（让速度逐渐衰减以模拟惯性）
            moveX *= friction;
            moveY *= friction;
            moveZ *= friction;

            // 加入上下漂浮效果
            moveY += 0.01 * Math.sin(this.tickCount * Math.PI / 18);

            // 设置新的移动速度 执行移动
            this.setDeltaMovement(moveX, moveY, moveZ);
            this.move(MoverType.SELF, this.getDeltaMovement());

            return;
        }

        // 如果没有乘客，并且不在地面，则让扫帚掉落
        if (!this.onGround()) {
            this.setDeltaMovement(0, -1.0f, 0);
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.tickLerp();
        this.controlBroom();
    }
}
