package com.hazed7.gentlestripping.mixin;

import com.hazed7.gentlestripping.GentleStripping;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(AxeItem.class)
public class AxeItemMixin {
    
    @Shadow @Final
    protected static Map<Block, Block> STRIPPED_BLOCKS;
    
    private static final ThreadLocal<Boolean> isProcessingLog = new ThreadLocal<>();
    
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void handleUnstripping(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!GentleStripping.CONFIG.enableUnstripping) {
            return;
        }
        
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        PlayerEntity player = context.getPlayer();
        
        boolean shouldUnstrip = player != null;
        if (GentleStripping.CONFIG.requireSneakingForUnstripping) {
            shouldUnstrip = shouldUnstrip && player.isSneaking();
        }
        
        if (shouldUnstrip) {
            Block unstrippedBlock = getUnstrippedBlock(blockState.getBlock());
            if (unstrippedBlock != null) {
                BlockState newState = unstrippedBlock.getDefaultState();
                
                if (blockState.getBlock() instanceof PillarBlock && unstrippedBlock instanceof PillarBlock) {
                    newState = newState.with(PillarBlock.AXIS, blockState.get(PillarBlock.AXIS));
                }
                
                if (!world.isClient) {
                    world.setBlockState(blockPos, newState, 11);
                    world.playSound(null, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                
                isProcessingLog.set(true);
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }
    
    @Inject(method = "useOnBlock", at = @At("HEAD"))
    private void markStrippingAction(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        
        if (blockState.isIn(BlockTags.LOGS) || STRIPPED_BLOCKS.containsKey(blockState.getBlock())) {
            isProcessingLog.set(true);
        }
    }
    
    @Inject(method = "useOnBlock", at = @At("RETURN"))
    private void clearProcessingFlag(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        isProcessingLog.remove();
    }
    
    @ModifyArg(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"), index = 0)
    private int preventDurabilityLoss(int amount) {
        if (Boolean.TRUE.equals(isProcessingLog.get()) && GentleStripping.CONFIG.preventDurabilityLoss) {
            return 0;
        }
        return amount;
    }
    
    private Block getUnstrippedBlock(Block strippedBlock) {
        for (Map.Entry<Block, Block> entry : STRIPPED_BLOCKS.entrySet()) {
            if (entry.getValue() == strippedBlock) {
                return entry.getKey();
            }
        }
        return null;
    }
} 