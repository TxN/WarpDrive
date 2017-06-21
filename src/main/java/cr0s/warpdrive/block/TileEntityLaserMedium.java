package cr0s.warpdrive.block;

import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityLaserMedium extends TileEntityAbstractEnergy {
	private int ticks = 0;
	
	public TileEntityLaserMedium() {
		peripheralName = "warpdriveLaserMedium";
		OC_enable = false;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		ticks++;
		if (ticks > 20) {
			ticks = 0;
			
			int metadata = Math.max(0, Math.min(7, Math.round((energy_getEnergyStored() * 8) / energy_getMaxStorage())));
			if (getBlockMetadata() != metadata) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 3);
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
	}
	
	// IEnergySink methods implementation
	@Override
	public int energy_getMaxStorage() {
		return WarpDriveConfig.LASER_MEDIUM_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean energy_canInput(ForgeDirection from) {
		return true;
	}
}
