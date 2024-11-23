package net.zhaiji.majobroom.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zhaiji.majobroom.Majobroom;
import net.zhaiji.majobroom.entity.MajoBroomEntity;

import java.util.function.Supplier;

public class InitEntity {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Majobroom.MOD_ID);

    public static Supplier<EntityType<MajoBroomEntity>> MAJO_BROOM = ENTITY_TYPES.register("majo_broom", () -> MajoBroomEntity.TYPE);
}