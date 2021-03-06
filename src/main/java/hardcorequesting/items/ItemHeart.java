package hardcorequesting.items;

import hardcorequesting.*;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.ModConfig;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static hardcorequesting.items.ItemInfo.HEART_ICONS;

//import net.minecraft.client.renderer.texture.IIconRegister;
//import net.minecraft.util.IIcon;

public class ItemHeart extends Item {

    public ItemHeart() {
        super();
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(64);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setRegistryName(ItemInfo.HEART_UNLOCALIZED_NAME);
        this.setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.HEART_UNLOCALIZED_NAME);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (int i = 0 ; i < HEART_ICONS.length ; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(ModInformation.ASSET_PREFIX + ":" + HEART_ICONS[i], "inventory"));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack) {
        int i = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 15);
        return super.getUnlocalizedName() + "_" + HEART_ICONS[i];
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs creativeTabs, List stackList) {
        for (int x = 0; x < HEART_ICONS.length; x++) {
            stackList.add(new ItemStack(this, 1, x));
        }
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {

            if (item.getItemDamage() == 3) {
                if (!QuestingData.isHardcoreActive()) {
                    player.addChatComponentMessage(new TextComponentTranslation("hqm.message.noHardcoreYet"));
                } else if (QuestingData.getQuestingData(player).getRawLives() < ModConfig.MAXLIVES) {
                    QuestingData.getQuestingData(player).addLives(player, 1);
                    player.addChatComponentMessage(new TextComponentTranslation("hqm.message.addOne"));
                    int lives = QuestingData.getQuestingData(player).getLives();
                    player.addChatComponentMessage(new TextComponentTranslation("hqm.message.haveRemaining", lives));
                    SoundHandler.play(Sounds.LIFE, player);
                    if (!player.capabilities.isCreativeMode) {
                        --item.stackSize;

                    }
                } else {
                    player.addChatComponentMessage(new TextComponentTranslation("hqm.message.haveMaxLives"));
                }
            }
            if (item.getItemDamage() == 4) {
                if (!QuestingData.isHardcoreActive()) {
                    player.addChatComponentMessage(new TextComponentTranslation("hqm.message.noHardcoreYet"));
                } else {
                    SoundHandler.play(Sounds.ROTTEN, player);
                    player.addChatComponentMessage(new TextComponentTranslation("hqm.message.eatRottenHearth"));
                    QuestingData.getQuestingData(player).removeLifeAndSendMessage(player);
                    DeathType.HQM.onDeath(player);

                    if (!player.capabilities.isCreativeMode)
                        --item.stackSize;
                }

            }
            return new ActionResult<>(EnumActionResult.SUCCESS, item);

        }
        return new ActionResult<>(EnumActionResult.SUCCESS, item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List tooltip, boolean extraInfo) {
        super.addInformation(item, player, tooltip, extraInfo);

        if (item.getItemDamage() == 3) {
            tooltip.add(Translator.translate("item.hqm:hearts_heart.tooltip"));
            if (ModConfig.ROTTIMER) {
                NBTTagCompound tagCompound = item.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    item.setTagCompound(tagCompound);
                }
                if (tagCompound.hasKey("RotTime")) {
                    int rot = tagCompound.getInteger("RotTime");
                    int maxRot = tagCompound.getInteger("MaxRot");
                    float percentage = (float) ((rot * 100) / maxRot);
                    tooltip.add(Translator.translate("item.hqm:hearts_heart.freshness", percentage));
                }
            }
        }
        if (item.getItemDamage() == 4) {
            tooltip.add(Translator.translate("item.hqm:hearts_rottenheart.tooltip"));
        }
    }

    @Override
    public boolean hasEffect(ItemStack item) {
        return item.getItemDamage() == 3 || item.getItemDamage() == 4;
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int par4, boolean par5) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (itemStack.getItemDamage() == 3 && ModConfig.ROTTIMER) {
                NBTTagCompound tagCompound = itemStack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    itemStack.setTagCompound(tagCompound);
                }
                if (!tagCompound.hasKey("RotTime")) {
                    int rot = (ModConfig.MAXROT * 20);
                    tagCompound.setInteger("MaxRot", rot);
                    tagCompound.setInteger("RotTime", rot);
                } else {
                    int newRot = tagCompound.getInteger("RotTime");
                    if (newRot <= 0) {
                        itemStack.setItemDamage(4);
                        entityPlayer.addChatComponentMessage(new TextComponentTranslation("hqm.message.hearthDecay"));
                    } else {
                        tagCompound.setInteger("RotTime", newRot - 1);
                    }
                }
            }
        }
    }
}
