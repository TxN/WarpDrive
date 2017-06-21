package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLaser extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;

	private static final int ICON_SIDE = 0;

	public BlockLaser() {
		super(Material.iron);
		setHardness(50.0F);
		setResistance(20.0F * 5 / 3);
		setBlockName("warpdrive.machines.Laser");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconBuffer = new IIcon[1];
		// Solid textures
		iconBuffer[ICON_SIDE] = iconRegister.registerIcon("warpdrive:laserSide");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		return iconBuffer[ICON_SIDE];
	}

	@Override
	public TileEntity createNewTileEntity(World parWorld, int i) {
		return new TileEntityLaser();
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}

	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return Item.getItemFromBlock(this);
	}
	
	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityLaser) {
				Commons.addChatMessage(entityPlayer, ((TileEntityLaser) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}