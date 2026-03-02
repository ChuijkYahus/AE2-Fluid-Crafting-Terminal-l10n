package me.myogoo.ae2fct.util;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import me.myogoo.ae2fct.init.AE2FCTDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import me.myogoo.ae2fct.codec.VirtualFluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.Optional;

public class FluidCraftingHelper {

    /**
     * Attempts to resolve fluid from an ingredient's accepted items,
     * and if enough fluid is available in MEStorage, extracts it
     * and returns a "simulated bucket" representing the item.
     */
    public static ItemStack tryExtractFluidForIngredient(Ingredient ingredient, MEStorage networkStorage,
            IEnergySource energy, IActionSource src) {
        if (networkStorage == null)
            return ItemStack.EMPTY;

        for (ItemStack stack : ingredient.getItems()) {
            FluidStack requiredFluid = FluidStack.EMPTY;
            Optional<FluidStack> fluidOpt = FluidUtil.getFluidContained(stack);
            if (fluidOpt.isPresent() && !fluidOpt.get().isEmpty()) {
                requiredFluid = fluidOpt.get();
            } else if (stack.getItem() instanceof BucketItem bucketItem) {
                // Vanilla bucket fallback
                // we have to check if it's not the empty bucket
                Fluid f = BuiltInRegistries.FLUID.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
                if (f != null && f != Fluids.EMPTY) {
                    requiredFluid = new FluidStack(f, 1000);
                } else if (bucketItem != Items.BUCKET) {
                    // Fallback for getting fluid from bucket in another way if needed
                    // In 1.21.1 BucketItem doesn't always expose public `content` directly
                    // Let's assume builtInRegistries works or FluidUtil caught it
                }
            }

            if (!requiredFluid.isEmpty()) {
                AEFluidKey fluidKey = AEFluidKey.of(requiredFluid);
                if (fluidKey != null) {
                    long simulated = StorageHelper.poweredExtraction(energy, networkStorage, fluidKey,
                            requiredFluid.getAmount(), src, Actionable.SIMULATE);
                    if (simulated >= requiredFluid.getAmount()) {
                        long extracted = StorageHelper.poweredExtraction(energy, networkStorage, fluidKey,
                                requiredFluid.getAmount(), src, Actionable.MODULATE);
                        if (extracted >= requiredFluid.getAmount()) {
                            ItemStack simBucket = stack.copy();
                            simBucket.setCount(1);
                            simBucket.set(AE2FCTDataComponent.VIRTUAL_FLUID,
                                    new VirtualFluid(requiredFluid, requiredFluid.getAmount()));
                            return simBucket;
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack tryExtractFluidForTemplate(ItemStack providedTemplate, MEStorage networkStorage,
            IEnergySource energy, IActionSource src) {
        if (networkStorage == null)
            return ItemStack.EMPTY;

        FluidStack requiredFluid = FluidStack.EMPTY;
        Optional<FluidStack> fluidOpt = FluidUtil.getFluidContained(providedTemplate);
        if (fluidOpt.isPresent() && !fluidOpt.get().isEmpty()) {
            requiredFluid = fluidOpt.get();
        } else if (providedTemplate.getItem() instanceof BucketItem bucketItem) {
            Fluid f = BuiltInRegistries.FLUID.get(BuiltInRegistries.ITEM.getKey(providedTemplate.getItem()));
            if (f != null && f != Fluids.EMPTY) {
                requiredFluid = new FluidStack(f, 1000);
            }
        }

        if (!requiredFluid.isEmpty()) {
            AEFluidKey fluidKey = AEFluidKey.of(requiredFluid);
            if (fluidKey != null) {
                long simulated = StorageHelper.poweredExtraction(energy, networkStorage, fluidKey,
                        requiredFluid.getAmount(), src, Actionable.SIMULATE);
                if (simulated >= requiredFluid.getAmount()) {
                    long extracted = StorageHelper.poweredExtraction(energy, networkStorage, fluidKey,
                            requiredFluid.getAmount(), src, Actionable.MODULATE);
                    if (extracted >= requiredFluid.getAmount()) {
                        ItemStack simBucket = providedTemplate.copy();
                        simBucket.setCount(1);
                        simBucket.set(AE2FCTDataComponent.VIRTUAL_FLUID,
                                new VirtualFluid(requiredFluid, requiredFluid.getAmount()));
                        return simBucket;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
