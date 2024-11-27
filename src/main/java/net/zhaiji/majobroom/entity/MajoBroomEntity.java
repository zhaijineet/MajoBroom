package net.zhaiji.majobroom.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MajoBroomEntity extends Entity {
    public static final EntityType<MajoBroomEntity> TYPE = EntityType.Builder.<MajoBroomEntity>of(MajoBroomEntity::new, MobCategory.MISC)
            .fireImmune()
            .sized(1.3f, 0.5f)
            .build("majo_broom");


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

    // 添加旋转增量字段
    private float deltaRotation;

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
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {

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
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    // 是否可以交互
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    // 是否可以碰
    @Override
    public boolean isPushable() {
        return true;
    }

    // 与扫帚交互
    @Override
    public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand) {
        if (!player.isDiscrete() && this.canAddPassenger(this)) {
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
    public boolean hurt(@NotNull DamageSource source, float amount) {
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
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {
        this.resetFallDistance();
    }

    // 扫帚控制
    // 正是因为如此 有BUG啊有BUG!!!
    public void controlBroom() {
        if (!this.isControlledByLocalInstance()) return;
        
        if (this.isVehicle()) {
            keyForward = keyForward();
            keyBack = keyBack();
            keyLeft = keyLeft();
            keyRight = keyRight();
            keyUp = keyUp();
            keyDown = keyDown();

            // 同步玩家视角方向到扫帚
            this.setYRot(Objects.requireNonNull(this.getControllingPassenger()).getYRot());

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

        } else {
            // 修改没有乘客时的行为
            if (!this.onGround()) {
                // 保持水平方向的惯性，添加向下的重力
                double gravity = -0.03; // 减小重力
                double drag = 0.99;    // 增加空气阻力
                
                // 应用空气阻力和重力
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(
                    motion.x * drag,
                    Math.max(motion.y * drag + gravity, -0.5), // 限制最大下落速度
                    motion.z * drag
                );
                
                // 移动实体
                this.move(MoverType.SELF, this.getDeltaMovement());
            } else {
                // 在地面上时缓慢减速
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(
                    motion.x * 0.9, // 增加地面摩擦力
                    0.0,
                    motion.z * 0.9
                );
                
                if (Math.abs(motion.x) < 0.005 && Math.abs(motion.z) < 0.005) {
                    this.setDeltaMovement(Vec3.ZERO);
                }
                
                this.move(MoverType.SELF, this.getDeltaMovement());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.tickLerp();
        this.controlBroom();
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        Vec3 currentPos = this.position();        // 保存扫帚位置
        Vec3 currentMotion = this.getDeltaMovement(); // 保存扫帚动量
        
        super.removePassenger(passenger);
        
        // 保持扫帚的位置和动量
        this.setPos(currentPos.x, currentPos.y, currentPos.z);
        this.setDeltaMovement(currentMotion);
        
        // 将玩家放在扫帚旁边
        passenger.setPos(
            passenger.getX(),
            this.getY(),
            passenger.getZ()
        );
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity passenger, @NotNull EntityDimensions dimensions, float partialTick) {
        // 调整垂直偏移，让玩家更贴近扫帚
        return new Vec3(
            0.0D,                    // 水平前后偏移
            -0.15D,                   // 固定的垂直偏移，使用负值让玩家坐得更低
            0.0D                     // 水平左右偏移
        ).yRot(-this.getYRot() * ((float)Math.PI / 180F));
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, Entity.@NotNull MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            EntityDimensions dimensions = passenger.getDimensions(passenger.getPose());
            Vec3 attachmentPoint = this.getPassengerAttachmentPoint(passenger, dimensions, 1.0F);
            
            // 更新乘客位置
            moveFunction.accept(passenger,
                this.getX() + attachmentPoint.x,
                this.getY() + attachmentPoint.y,
                this.getZ() + attachmentPoint.z
            );
            
            // 同步乘客旋转
            passenger.setYRot(passenger.getYRot() + this.deltaRotation);
            passenger.setYHeadRot(passenger.getYHeadRot() + this.deltaRotation);
            this.clampRotation(passenger);
        }
    }

    protected void clampRotation(Entity entityToUpdate) {
        entityToUpdate.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(entityToUpdate.getYRot() - this.getYRot());
        float f1 = Mth.clamp(f, -105.0F, 105.0F);
        entityToUpdate.yRotO += f1 - f;
        entityToUpdate.setYRot(entityToUpdate.getYRot() + f1 - f);
        entityToUpdate.setYHeadRot(entityToUpdate.getYRot());
    }

    @Override
    public void onPassengerTurned(@NotNull Entity entityToUpdate) {
        this.clampRotation(entityToUpdate);
    }
}
