package org.cloudburstmc.updater.block;

import org.cloudburstmc.updater.common.CompoundTagUpdaterContext;
import org.cloudburstmc.updater.common.StateUpdater;

import java.util.Map;
import java.util.function.Function;

public class BlockStateUpdater_1_20_70 implements StateUpdater {
    public static final StateUpdater INSTANCE = new BlockStateUpdater_1_20_70();

    @Override
    public void registerUpdaters(CompoundTagUpdaterContext ctx) {
        this.addTypeUpdater(ctx, "minecraft:double_wooden_slab", "wood_type", type -> "minecraft:" + type + "_double_slab");
        this.addTypeUpdater(ctx, "minecraft:leaves", "old_leaf_type", type -> "minecraft:" + type + "_leaves");
        this.addTypeUpdater(ctx, "minecraft:leaves2", "new_leaf_type", type -> "minecraft:" + type + "_leaves");
        this.addTypeUpdater(ctx, "minecraft:wooden_slab", "wood_type", type -> "minecraft:" + type + "_slab");

        ctx.addUpdater(1, 20, 70)
                .match("name", "minecraft:wood")
                .edit("states", helper -> {
                    Map<String, Object> states = helper.getCompoundTag();
                    Object bit = states.remove("stripped_bit");
                    boolean toggles = bit instanceof Byte && (byte) bit == 1 || bit instanceof Boolean && (boolean) bit;

                    String type = (String) states.remove("wood_type");
                    helper.getRootTag().put("name", toggles ? "minecraft:stripped_" + type + "_wood" : "minecraft:" + type + "_wood");
                });

        // Vanilla does not use updater for this block for some reason
        ctx.addUpdater(1, 20, 70, false, false)
                .match("name", "minecraft:grass")
                .edit("name", helper -> helper.replaceWith("name", "minecraft:grass_block"));
    }

    private void addTypeUpdater(CompoundTagUpdaterContext context, String identifier, String typeState, Function<String, String> rename) {
        context.addUpdater(1, 20, 70)
                .match("name", identifier)
                .visit("states")
                .edit(typeState, helper -> helper.getRootTag().put("name", rename.apply((String) helper.getTag())))
                .remove(typeState);

    }
}
