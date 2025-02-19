package com.mrcrayfish.backpacked.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mrcrayfish.backpacked.client.renderer.backpack.BackpackRenderContext;
import com.mrcrayfish.backpacked.common.backpack.Backpack;
import com.mrcrayfish.backpacked.common.backpack.BackpackManager;
import com.mrcrayfish.backpacked.common.backpack.BackpackProperties;
import com.mrcrayfish.backpacked.common.backpack.ModelMeta;
import com.mrcrayfish.backpacked.core.ModDataComponents;
import com.mrcrayfish.backpacked.item.BackpackItem;
import com.mrcrayfish.backpacked.platform.ClientServices;
import com.mrcrayfish.backpacked.platform.Services;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Author: MrCrayfish
 */
public class BackpackLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M>
{
    private final ItemRenderer itemRenderer;

    public BackpackLayer(RenderLayerParent<T, M> renderer, ItemRenderer itemRenderer)
    {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource source, int light, T player, float p_225628_5_, float p_225628_6_, float partialTick, float p_225628_8_, float p_225628_9_, float p_225628_10_)
    {
        ItemStack stack = Services.BACKPACK.getBackpackStack(player);
        if(stack.getItem() instanceof BackpackItem)
        {
            BackpackProperties properties = stack.getOrDefault(ModDataComponents.BACKPACK_PROPERTIES.get(), BackpackProperties.DEFAULT);
            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            if(chestStack.getItem() == Items.ELYTRA && !properties.showWithElytra())
                return;

            if(!Services.BACKPACK.isBackpackVisible(player))
                return;

            Backpack backpack = BackpackManager.instance().getClientBackpack(properties.model());
            if(backpack == null)
                return;

            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.scale(1.05F, -1.05F, -1.05F);
            int offset = !chestStack.isEmpty() ? 3 : 2;
            pose.translate(0, -0.06, offset * 0.0625);

            ModelMeta meta = BackpackManager.instance().getModelMeta(backpack);
            meta.renderer().ifPresentOrElse(renderer -> {
                pose.pushPose();
                BackpackRenderContext context = new BackpackRenderContext(pose, source, light, stack, backpack, player, partialTick, player.tickCount, model -> {
                    this.itemRenderer.render(stack, ItemDisplayContext.NONE, false, pose, source, light, OverlayTexture.NO_OVERLAY, model);
                });
                renderer.forEach(function -> function.apply(context));
                pose.popPose();
            }, () -> {
                BakedModel model = ClientServices.MODEL.getBakedModel(backpack.getBaseModel());
                this.itemRenderer.render(stack, ItemDisplayContext.NONE, false, pose, source, light, OverlayTexture.NO_OVERLAY, model);
            });
            this.itemRenderer.render(stack, ItemDisplayContext.NONE, false, pose, source, light, OverlayTexture.NO_OVERLAY, this.getModel(backpack.getStrapsModel()));

            pose.popPose();
        }
    }

    private BakedModel getModel(ResourceLocation location)
    {
        BakedModel model = ClientServices.MODEL.getBakedModel(location);
        return model != null ? model : this.itemRenderer.getItemModelShaper().getModelManager().getMissingModel();
    }
}
