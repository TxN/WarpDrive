package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.data.EnumDecorativeType;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockDecorative extends ItemBlock {
	
	public ItemBlockDecorative(Block block) {
		super(block);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.decoration.decorative");
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTabs, List list) {
		for (EnumDecorativeType enumDecorativeType : EnumDecorativeType.values()) {
			list.add(new ItemStack(item, 1, enumDecorativeType.ordinal()));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack == null) {
			return getUnlocalizedName();
		}
		return "tile.warpdrive.decoration.decorative." + EnumDecorativeType.get(itemstack.getItemDamage()).unlocalizedName;
	}
}
