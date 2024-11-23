package net.zhaiji.majobroom.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zhaiji.majobroom.Majobroom;
import net.zhaiji.majobroom.register.InitItem;

public class MajoItemModelProvider extends ItemModelProvider {

    public MajoItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Majobroom.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(InitItem.MAJO_BROOM.get());
    }
}
