package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEnanReactorCore extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	IIcon[] iconBuffer;
	
	public BlockEnanReactorCore() {
		super(Material.iron);
		setBlockName("warpdrive.energy.EnanReactorCore");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityEnanReactorCore();
	}
	
	@Override
	public void breakBlock(World w, int x, int y, int z, Block oid, int om) {
		super.breakBlock(w, x, y, z, oid, om);
		
		int[] xo = { -2, 2, 0, 0 };
		int[] zo = { 0, 0, -2, 2 };
		for (int i = 0; i < 4; i++) {
			TileEntity te = w.getTileEntity(x + xo[i], y, z + zo[i]);
			if (te instanceof TileEntityEnanReactorLaser) {
				((TileEntityEnanReactorLaser) te).unlink();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
		if (side == 0 || side == 1) {
			return iconBuffer[16];
		}
		if (metadata >= 0 && metadata < 16) {
			return iconBuffer[metadata];
		}
		return iconBuffer[0];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0 || side == 1) {
			return iconBuffer[16];
		}
		return iconBuffer[7];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconBuffer = new IIcon[17];
		iconBuffer[16] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreTopBottom");
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide00");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide01");
		iconBuffer[2] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide02");
		iconBuffer[3] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide03");
		iconBuffer[4] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide10");
		iconBuffer[5] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide11");
		iconBuffer[6] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide12");
		iconBuffer[7] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide13");
		iconBuffer[8] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide20");
		iconBuffer[9] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide21");
		iconBuffer[10] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide22");
		iconBuffer[11] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide23");
		iconBuffer[12] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide30");
		iconBuffer[13] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide31");
		iconBuffer[14] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide32");
		iconBuffer[15] = iconRegister.registerIcon("warpdrive:energy/enanReactorCoreSide33");
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 3;
	}
}