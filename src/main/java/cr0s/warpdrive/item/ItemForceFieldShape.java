package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.forcefield.BlockForceFieldProjector;
import cr0s.warpdrive.block.forcefield.BlockForceFieldRelay;
import cr0s.warpdrive.data.EnumForceFieldShape;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemForceFieldShape extends Item {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	private static ItemStack[] itemStackCache;
	
	public ItemForceFieldShape() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.forcefield.shape");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		itemStackCache = new ItemStack[EnumForceFieldShape.length];
	}
	
	public static ItemStack getItemStack(EnumForceFieldShape enumForceFieldShape) {
		if (enumForceFieldShape != null) {
			int damage = enumForceFieldShape.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.itemForceFieldShape, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(EnumForceFieldShape enumForceFieldShape, int amount) {
		return new ItemStack(WarpDrive.itemForceFieldShape, amount, enumForceFieldShape.ordinal());
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[EnumForceFieldShape.length];
		for(EnumForceFieldShape enumForceFieldShape : EnumForceFieldShape.values()) {
			icons[enumForceFieldShape.ordinal()] = iconRegister.registerIcon("warpdrive:forcefield/shape_" + enumForceFieldShape.unlocalizedName);
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < EnumForceFieldShape.length) {
			return getUnlocalizedName() + "." + EnumForceFieldShape.get(damage).unlocalizedName;
		}
		return getUnlocalizedName();
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		if (damage >= 0 && damage < EnumForceFieldShape.length) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list) {
		for(EnumForceFieldShape enumForceFieldShape : EnumForceFieldShape.values()) {
			if (enumForceFieldShape != EnumForceFieldShape.NONE) {
				list.add(new ItemStack(item, 1, enumForceFieldShape.ordinal()));
			}
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		Block block = world.getBlock(x, y, z);
		return block instanceof BlockForceFieldRelay || block instanceof BlockForceFieldProjector || super.doesSneakBypassUse(world, x, y, z, player);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (StatCollector.canTranslate(tooltipName1)) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName1));
		}
		
		String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && StatCollector.canTranslate(tooltipName2)) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName2));
		}
		
		Commons.addTooltip(list, "\n");
		
		Commons.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.shape.tooltip.usage"));
	}
}