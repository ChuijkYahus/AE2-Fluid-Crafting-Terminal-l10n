package me.myogoo.ae2fct.item;

import me.myogoo.ae2fct.client.VirtualFluidItemRenderer;
import me.myogoo.ae2fct.init.AE2FCTDataComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;
import java.util.function.Consumer;

public class VirtualFluidItem extends Item {
    public VirtualFluidItem() {
        super(new Properties());
    }

    public FluidStack getFluidStack(ItemStack stack) {
        if (!stack.has(AE2FCTDataComponent.VIRTUAL_FLUID)) {
            return FluidStack.EMPTY;
        }
        return Objects.requireNonNull(stack.get(AE2FCTDataComponent.VIRTUAL_FLUID)).fluid();
    }

    @Override
    public void initializeClient(
            Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new VirtualFluidItemRenderer(
                        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        Minecraft.getInstance().getEntityModels());
            }
        });
    }

    @Override
    public Component getName(ItemStack stack) {
        if (!stack.has(AE2FCTDataComponent.VIRTUAL_FLUID)) {
            return super.getName(stack);
        }
        FluidStack fs = stack.get(AE2FCTDataComponent.VIRTUAL_FLUID).fluid();
        if (fs == null || fs.isEmpty()) {
            return super.getName(stack);
        }
        return fs.getHoverName();
    }

}
