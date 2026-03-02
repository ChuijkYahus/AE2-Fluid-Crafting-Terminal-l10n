package me.myogoo.ae2fct.item;

import me.myogoo.ae2fct.init.AE2FCTDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;

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
            java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new me.myogoo.ae2fct.client.VirtualFluidItemRenderer(
                        net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        net.minecraft.client.Minecraft.getInstance().getEntityModels());
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
