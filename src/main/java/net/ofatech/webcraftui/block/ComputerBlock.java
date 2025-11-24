package net.ofatech.webcraftui.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.ofatech.webcraftui.client.UiCarouselScreen;
import org.jetbrains.annotations.NotNull;

public class ComputerBlock extends Block {
    public ComputerBlock(Properties properties) {
        super(properties);
    }

    // Called when the player interacts without using an item (empty hand or after item handling fails)
    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            UiCarouselScreen.open();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
