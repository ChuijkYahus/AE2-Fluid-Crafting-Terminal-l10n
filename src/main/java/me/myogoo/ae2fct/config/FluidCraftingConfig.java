package me.myogoo.ae2fct.config;


import net.neoforged.neoforge.common.ModConfigSpec;

public class FluidCraftingConfig {
    public static final ModConfigSpec ClientSpec = new Client().get();

    public static class Client {
        private final ModConfigSpec spec;

        Client() {
            var builder = new ModConfigSpec.Builder();

            this.spec = builder.build();
        }

        public ModConfigSpec get() {
            return spec;
        }
    }
}
