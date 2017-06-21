package cr0s.warpdrive.api;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public interface IBlockBase {
	
	byte getTier(final ItemStack itemStack);
	
	EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity);
}