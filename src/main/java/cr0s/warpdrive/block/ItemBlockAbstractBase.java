package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.api.IBlockBase;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockAbstractBase extends ItemBlock {
	
	public ItemBlockAbstractBase(Block block) {
		super(block);	// sets field_150939_a to block
		if (block instanceof BlockAbstractContainer) {
			setHasSubtypes(((BlockAbstractContainer) block).hasSubBlocks);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return field_150939_a.getIcon(2, damage);
	}
	
	@Override
	public int getColorFromItemStack(ItemStack itemStack, int indexPass) {
		return Block.getBlockFromItem(itemStack.getItem()).getRenderColor(itemStack.getItemDamage());
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		if ( itemStack == null 
		  || !(field_150939_a instanceof BlockAbstractContainer)
		  || !((BlockAbstractContainer) field_150939_a).hasSubBlocks ) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + itemStack.getItemDamage();
	}
	
	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		if ( itemStack == null
		  || !(field_150939_a instanceof IBlockBase) ) {
			return super.getRarity(itemStack);
		}
		return ((IBlockBase) field_150939_a).getRarity(itemStack, super.getRarity(itemStack));
	}
	
	private String getStatus(final NBTTagCompound nbtTagCompound, final int metadata) {
		TileEntity tileEntity = field_150939_a.createTileEntity(null, metadata);
		if (tileEntity instanceof TileEntityAbstractBase) {
			if (nbtTagCompound != null) {
				tileEntity.readFromNBT(nbtTagCompound);
			}
			return ((TileEntityAbstractBase) tileEntity).getStatus();
			
		} else {
			return "";
		}
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
		
		Commons.addTooltip(list, StatCollector.translateToLocalFormatted(getStatus(itemStack.getTagCompound(), itemStack.getItemDamage())));
	}
}
