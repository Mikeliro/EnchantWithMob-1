package com.baguchan.enchantwithmob.item;

import com.baguchan.enchantwithmob.EnchantConfig;
import com.baguchan.enchantwithmob.EnchantWithMob;
import com.baguchan.enchantwithmob.mobenchant.MobEnchant;
import com.baguchan.enchantwithmob.registry.MobEnchants;
import com.baguchan.enchantwithmob.utils.MobEnchantUtils;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class MobEnchantBookItem extends Item {
    public MobEnchantBookItem(Properties group) {
        super(group);
    }


    /*
     * Implemented onRightClick (method) inside CommonEventHandler instead of this method
     */
    /*@Override
    public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
        if (MobEnchantUtils.hasMobEnchant(stack)) {
            target.getCapability(EnchantWithMob.MOB_ENCHANT_CAP).ifPresent(cap ->
            {
                MobEnchantUtils.addMobEnchantToEntityFromItem(stack, target, cap);
            });
            playerIn.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);

            stack.damageItem(1, playerIn, (entity) -> entity.sendBreakAnimation(hand));

            return ActionResultType.SUCCESS;
        }

        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }*/

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (EnchantConfig.COMMON.enchantYourSelf.get() && MobEnchantUtils.hasMobEnchant(stack)) {
            playerIn.getCapability(EnchantWithMob.MOB_ENCHANT_CAP).ifPresent(cap ->
            {
                MobEnchantUtils.addItemMobEnchantToEntity(stack, playerIn, cap);
            });
            playerIn.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);

            stack.hurtAndBreak(1, playerIn, (entity) -> entity.broadcastBreakEvent(handIn));

            playerIn.getCooldowns().addCooldown(stack.getItem(), 60);

            return ActionResult.success(stack);
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public void fillItemCategory(ItemGroup p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
        if (this.allowdedIn(p_150895_1_)) {
            for (MobEnchant enchant : MobEnchants.getRegistry()) {
                ItemStack stack = new ItemStack(this);
                MobEnchantUtils.addMobEnchantToItemStack(stack, enchant, enchant.getMaxLevel());
                p_150895_2_.add(stack);
            }
        }

    }

    public static ListNBT getEnchantmentList(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTag();
        return compoundnbt != null ? compoundnbt.getList(MobEnchantUtils.TAG_STORED_MOBENCHANTS, 10) : new ListNBT();
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (MobEnchantUtils.hasMobEnchant(stack)) {
            ListNBT listnbt = MobEnchantUtils.getEnchantmentListForNBT(stack.getTag());

            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundNBT compoundnbt = listnbt.getCompound(i);

                MobEnchant mobEnchant = MobEnchantUtils.getEnchantFromNBT(compoundnbt);
                int level = MobEnchantUtils.getEnchantLevelFromNBT(compoundnbt);

                if (mobEnchant != null) {
                    TextFormatting[] textformatting = new TextFormatting[]{TextFormatting.AQUA};

                    tooltip.add(new TranslationTextComponent("mobenchant.enchantwithmob.name." + mobEnchant.getRegistryName().getNamespace() + "." + mobEnchant.getRegistryName().getPath()).withStyle(textformatting).append(" ").append(new TranslationTextComponent("enchantment.level." + level).withStyle(textformatting)));
                }
            }

            List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();

            for (int i = 0; i < listnbt.size(); ++i) {
                CompoundNBT compoundnbt = listnbt.getCompound(i);

                MobEnchant mobEnchant = MobEnchantUtils.getEnchantFromNBT(compoundnbt);
                int mobEnchantLevel = MobEnchantUtils.getEnchantLevelFromNBT(compoundnbt);

                if (mobEnchant != null) {
                    Map<Attribute, AttributeModifier> map = mobEnchant.getAttributeModifierMap();
                    if (!map.isEmpty()) {
                        for (Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                            AttributeModifier attributemodifier = entry.getValue();
                            AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), mobEnchant.getAttributeModifierAmount(mobEnchantLevel, attributemodifier), attributemodifier.getOperation());
                            list1.add(new Pair<>(entry.getKey(), attributemodifier1));
                        }
                    }
                }
            }


            if (!list1.isEmpty()) {
                tooltip.add(StringTextComponent.EMPTY);
                tooltip.add((new TranslationTextComponent("mobenchant.enchantwithmob.when_ehcnanted")).withStyle(TextFormatting.DARK_PURPLE));

                for (Pair<Attribute, AttributeModifier> pair : list1) {
                    AttributeModifier attributemodifier2 = pair.getSecond();
                    double d0 = attributemodifier2.getAmount();
                    double d1;
                    if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                        d1 = attributemodifier2.getAmount();
                    } else {
                        d1 = attributemodifier2.getAmount() * 100.0D;
                    }

                    if (d0 > 0.0D) {
                        tooltip.add((new TranslationTextComponent("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(pair.getFirst().getDescriptionId()))).withStyle(TextFormatting.BLUE));
                    } else if (d0 < 0.0D) {
                        d1 = d1 * -1.0D;
                        tooltip.add((new TranslationTextComponent("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(pair.getFirst().getDescriptionId()))).withStyle(TextFormatting.RED));
                    }
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack p_77636_1_) {
        return true;
    }
}
