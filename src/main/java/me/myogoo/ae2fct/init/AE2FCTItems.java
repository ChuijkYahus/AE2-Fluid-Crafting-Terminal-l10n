package me.myogoo.ae2fct.init;

import me.myogoo.ae2fct.Ae2fct;
import me.myogoo.ae2fct.item.TerminalFluidInteractCardItem;
import me.myogoo.ae2fct.item.VirtualFluidItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AE2FCTItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Ae2fct.MODID);

    public static final Supplier<VirtualFluidItem> VIRTUAL_FLUID_ITEM = ITEMS.register("virtual_fluid_item",
            VirtualFluidItem::new);
    public static final Supplier<TerminalFluidInteractCardItem> TERMINAL_FLUID_INTERACT_CARD = ITEMS.register(
            "terminal_fluid_interact_card",
            () -> new TerminalFluidInteractCardItem(new Item.Properties().stacksTo(1)));
}
