package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.api.IControlChannel;
import cr0s.warpdrive.api.IVideoChannel;
import cr0s.warpdrive.api.IWarpTool;
import cr0s.warpdrive.block.energy.BlockEnergyBank;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemTuningFork extends Item implements IWarpTool {
	
	@SideOnly(Side.CLIENT)
	private IIcon icons[];
	
	public ItemTuningFork() {
		super();
		setMaxDamage(0);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.tool.tuning_fork");
		setFull3D();
		setHasSubtypes(true);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[16];
		
		for (int i = 0; i < 16; ++i) {
			icons[i] = iconRegister.registerIcon("warpdrive:tool/tuning_fork-" + getDyeColorName(i));
		}
	}
	
	public static String getDyeColorName(int metadata) {
		return ItemDye.field_150921_b[metadata];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int damage) {
		if (damage < icons.length) {
			return icons[damage];
		}
		return Blocks.fire.getFireIcon(0);
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list) {
		for(int dyeColor = 0; dyeColor < 16; dyeColor++) {
			list.add(new ItemStack(item, 1, dyeColor));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < 16) {
			return getUnlocalizedName() + "." + ItemDye.field_150923_a[damage];
		}
		return getUnlocalizedName();
	}
	
	public static int getVideoChannel(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningFork)) {
			return -1;
		}
		return (itemStack.getItemDamage() % 16) + 100;
	}
	
	public static int getBeamFrequency(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningFork)) {
			return -1;
		}
		return ((itemStack.getItemDamage() % 16) + 1) * 10;
	}
	
	public static int getControlChannel(ItemStack itemStack) {
		if (!(itemStack.getItem() instanceof ItemTuningFork)) {
			return -1;
		}
		return ((itemStack.getItemDamage() % 16) + 2);
	}
	
	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null) {
			return false;
		}
		
		boolean hasVideoChannel = tileEntity instanceof IVideoChannel;
		boolean hasBeamFrequency = tileEntity instanceof IBeamFrequency;
		boolean hasControlChannel = tileEntity instanceof IControlChannel;
		if (!hasVideoChannel && !hasBeamFrequency && !hasControlChannel) {
			return false;
		}
		if (hasVideoChannel && !(entityPlayer.isSneaking() && hasBeamFrequency)) {
			((IVideoChannel)tileEntity).setVideoChannel(getVideoChannel(itemStack));
			Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.video_channel.set",
					tileEntity.getBlockType().getLocalizedName(),
					getVideoChannel(itemStack)));
			world.playSoundAtEntity(entityPlayer, "WarpDrive:ding", 0.1F, 1F);
			
		} else if (hasControlChannel && !(entityPlayer.isSneaking() && hasBeamFrequency)) {
			((IControlChannel)tileEntity).setControlChannel(getControlChannel(itemStack));
			Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.control_channel.set",
				tileEntity.getBlockType().getLocalizedName(),
				getControlChannel(itemStack)));
			world.playSoundAtEntity(entityPlayer, "WarpDrive:ding", 0.1F, 1F);
			
		} else if (hasBeamFrequency) {
			((IBeamFrequency)tileEntity).setBeamFrequency(getBeamFrequency(itemStack));
			Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.beam_frequency.set",
					tileEntity.getBlockType().getLocalizedName(),
					getBeamFrequency(itemStack)));
			world.playSoundAtEntity(entityPlayer, "WarpDrive:ding", 0.1F, 1F);
			
		} else {
			Commons.addChatMessage(entityPlayer, "Error: invalid state, please contact the mod authors"
					+ "\nof " + itemStack
					+ "\nand " + tileEntity);
		}
		return true;
	}
	
	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		Block block = world.getBlock(x, y, z);
		return block instanceof BlockEnergyBank || super.doesSneakBypassUse(world, x, y, z, player);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltip = "";
		tooltip += StatCollector.translateToLocalFormatted("warpdrive.video_channel.tooltip", getVideoChannel(itemStack));
		tooltip += "\n" + StatCollector.translateToLocalFormatted("warpdrive.beam_frequency.tooltip", getBeamFrequency(itemStack));
		tooltip += "\n" + StatCollector.translateToLocalFormatted("warpdrive.control_channel.tooltip", getControlChannel(itemStack));
		
		tooltip += "\n\n" + StatCollector.translateToLocal("item.warpdrive.tool.tuning_fork.tooltip.usage");
		
		Commons.addTooltip(list, tooltip);
	}
}
