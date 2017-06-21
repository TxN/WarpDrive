package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCrystalToken extends Item {	
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	private static ItemStack[] itemStackCache;
	private static final int COUNT = 6; 
	
	public ItemCrystalToken() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.tool.crystalToken");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		itemStackCache = new ItemStack[COUNT];
	}
	
	public static ItemStack getItemStack(final int damage) {
		if (damage < COUNT) {
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.itemCrystalToken, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(final int damage, final int amount) {
		if (damage < COUNT) {
			return new ItemStack(WarpDrive.itemCrystalToken, amount, damage);
		}
		return new ItemStack(WarpDrive.itemCrystalToken, amount, 0);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[COUNT];
		for(int damage = 0; damage < COUNT; damage++) {
			icons[damage] = iconRegister.registerIcon("warpdrive:tool/crystal_token-" + damage);
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < COUNT) {
			return "item.warpdrive.tool.crystalToken" + damage;
		}
		return getUnlocalizedName();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(final int damage) {
		if (damage >= 0 && damage < COUNT) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list) {
		for(int damage = 0; damage < COUNT; damage++) {
			list.add(new ItemStack(item, 1, damage));
		}
	}
	
	public static String getSchematicName(ItemStack itemStack) {
		String schematicName = "" + itemStack.getItemDamage();
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound != null && tagCompound.hasKey("shipName")) {
			schematicName = tagCompound.getString("shipName");
		}
		return schematicName;
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (StatCollector.canTranslate(tooltipName1)) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName1, getSchematicName(itemStack)));
		}
		
		String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && StatCollector.canTranslate(tooltipName2)) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName2, getSchematicName(itemStack)));
		}
	}
}