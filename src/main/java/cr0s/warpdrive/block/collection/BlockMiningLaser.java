package cr0s.warpdrive.block.collection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.BlockAbstractContainer;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMiningLaser extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	public static final int ICON_IDLE = 0;
	public static final int ICON_MINING_LOW_POWER = 1;
	public static final int ICON_MINING_POWERED = 2;
	public static final int ICON_SCANNING_LOW_POWER = 3;
	public static final int ICON_SCANNING_POWERED = 4;
	private static final int ICON_BOTTOM = 5;
	private static final int ICON_TOP = 6;
	
	public BlockMiningLaser() {
		super(Material.iron);
		setBlockName("warpdrive.collection.MiningLaser");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconBuffer = new IIcon[16];
		iconBuffer[ICON_IDLE              ] = iconRegister.registerIcon("warpdrive:collection/miningLaserSide_idle");
		iconBuffer[ICON_MINING_LOW_POWER  ] = iconRegister.registerIcon("warpdrive:collection/miningLaserSide_miningLowPower");
		iconBuffer[ICON_MINING_POWERED    ] = iconRegister.registerIcon("warpdrive:collection/miningLaserSide_miningPowered");
		iconBuffer[ICON_SCANNING_LOW_POWER] = iconRegister.registerIcon("warpdrive:collection/miningLaserSide_scanningLowPower");
		iconBuffer[ICON_SCANNING_POWERED  ] = iconRegister.registerIcon("warpdrive:collection/miningLaserSide_scanningPowered");
		iconBuffer[ICON_BOTTOM            ] = iconRegister.registerIcon("warpdrive:collection/miningLaserBottom");
		iconBuffer[ICON_TOP               ] = iconRegister.registerIcon("warpdrive:collection/miningLaserTop");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		}
		if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		if (metadata < iconBuffer.length) {
			return iconBuffer[metadata];
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0) {
			return iconBuffer[ICON_BOTTOM];
		}
		if (side == 1) {
			return iconBuffer[ICON_TOP];
		}
		return iconBuffer[ICON_SCANNING_LOW_POWER];
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityMiningLaser();
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 1;
	}
	
	/**
	 * Returns the item to drop on destruction.
	 */
	@Override
	public Item getItemDropped(int par1, Random random, int par3) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityMiningLaser) {
				Commons.addChatMessage(entityPlayer, ((TileEntityMiningLaser) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}