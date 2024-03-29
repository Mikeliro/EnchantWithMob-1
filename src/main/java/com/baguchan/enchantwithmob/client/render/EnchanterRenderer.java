package com.baguchan.enchantwithmob.client.render;

import com.baguchan.enchantwithmob.EnchantWithMob;
import com.baguchan.enchantwithmob.client.model.EnchanterModel;
import com.baguchan.enchantwithmob.entity.EnchanterEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.EnchantmentTableTileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchanterRenderer<T extends EnchanterEntity> extends MobRenderer<T, EnchanterModel<T>> {
    private static final ResourceLocation ILLAGER = new ResourceLocation(EnchantWithMob.MODID, "textures/entity/enchanter.png");

    protected final BookModel bookModel = new BookModel();

    public EnchanterRenderer(EntityRendererManager p_i47477_1_) {
        super(p_i47477_1_, new EnchanterModel<>(), 0.5F);
        this.addLayer(new HeadLayer<>(this));
        //this.addLayer(new CrossArmHeldItemLayer<>(this));
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        float bookAnimation = entityIn.getBookAnimationScale(partialTicks);

        float f = MathHelper.approach(partialTicks, entityIn.yBodyRotO, entityIn.yBodyRot);
        float swingProgress = this.getAttackAnim(entityIn, partialTicks);

        if (entityIn.isAlive()) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.0D, 1.1625D, 0.0F);
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-f + 90F));
            matrixStackIn.translate(-0.575D, 0.0D, 0.0D);
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(60.0F * bookAnimation));

            //When spell casting, stop animation
            if (swingProgress > 0 && !entityIn.isCastingSpell()) {
                matrixStackIn.translate(-0.05F * (1.0F - swingProgress), -0.1F * (1.0F - swingProgress), 0.0D);
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(45.0F * (1.0F - swingProgress)));
            }

            this.bookModel.setupAnim(0.0F, MathHelper.clamp(bookAnimation, 0.0F, 0.1F), MathHelper.clamp(bookAnimation, 0.0F, 0.9F), bookAnimation);
            IVertexBuilder ivertexbuilder = EnchantmentTableTileEntityRenderer.BOOK_LOCATION.buffer(bufferIn, RenderType::entitySolid);
            this.bookModel.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStackIn.popPose();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return ILLAGER;
    }
}