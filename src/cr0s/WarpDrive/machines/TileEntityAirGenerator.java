package cr0s.WarpDrive.machines;

import cpw.mods.fml.common.FMLCommonHandler;
import cr0s.WarpDrive.WarpDriveConfig;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityAirGenerator extends WarpEnergyTE {
    private final int EU_PER_NEWAIRBLOCK = 12;
    private final int EU_PER_EXISTINGAIRBLOCK = 4;	// 1 solar = 1 EU/t
    private final int MAX_ENERGY_VALUE = 8 * EU_PER_NEWAIRBLOCK;

    private int cooldownTicks = 0;
    private final int AIR_GENERATION_TICKS = 40;
    private final int START_CONCENTRATION_VALUE = 15;

    @Override
    public void updateEntity() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		super.updateEntity();

		if (this.isInvalid()) {
			return;
		}
		
        // Air generator works only in spaces
        if (worldObj.provider.dimensionId != WarpDriveConfig.G_SPACE_DIMENSION_ID && worldObj.provider.dimensionId != WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) {
        	if (getBlockMetadata() != 0) {
        		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2); // set disabled texture
        	}
            return;
        }

        cooldownTicks++;
        if (cooldownTicks > AIR_GENERATION_TICKS) {
        	if (consumeEnergy(EU_PER_NEWAIRBLOCK, true)) {
	        	if (getBlockMetadata() != 1) {
	        		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 2); // set enabled texture
	        	}
        	} else {
            	if (getBlockMetadata() != 0) {
            		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2); // set disabled texture
            	}
        	}
        	releaseAir( 1,  0,  0);
        	releaseAir(-1,  0,  0);
        	releaseAir( 0,  1,  0);
        	releaseAir( 0, -1,  0);
        	releaseAir( 0,  0,  1);
        	releaseAir( 0,  0, -1);

        	cooldownTicks = 0;
        }
    }
    
    private void releaseAir(int xOffset, int yOffset, int zOffset) {
    	int blockId = worldObj.getBlockId(xCoord + xOffset, yCoord + yOffset, zCoord + zOffset);
	    if (WarpDriveConfig.isAirBlock(worldObj, blockId, xCoord + xOffset, yCoord + yOffset, zCoord + zOffset)) {// can be air
	    	int energy_cost = (blockId !=  WarpDriveConfig.airID) ? EU_PER_NEWAIRBLOCK : EU_PER_EXISTINGAIRBLOCK;
	    	if (consumeEnergy(energy_cost,  true)) {// enough energy
		        if (worldObj.setBlock(xCoord + xOffset, yCoord + yOffset, zCoord + zOffset, WarpDriveConfig.airID, START_CONCENTRATION_VALUE, 2)) {// needs to renew air or was not maxed out
		        	consumeEnergy(EU_PER_NEWAIRBLOCK, false);
		        } else {
		        	consumeEnergy(EU_PER_EXISTINGAIRBLOCK, false);
		        }
	    	}
	    	else
	    	{// low energy => remove air block
	    		if (blockId == WarpDriveConfig.airID){
	    			int metadata = worldObj.getBlockMetadata(xCoord + xOffset, yCoord + yOffset, zCoord + zOffset);
	    			if (metadata > 4) {
	    				worldObj.setBlockMetadataWithNotify(xCoord + xOffset, yCoord + yOffset, zCoord + zOffset, metadata - 4, 2);
	    			} else if (metadata > 1) {
	    				worldObj.setBlockMetadataWithNotify(xCoord + xOffset, yCoord + yOffset, zCoord + zOffset, 1, 2);
	    			} else {
	    			//	worldObj.setBlock(xCoord + xOffset, yCoord + yOffset, zCoord + zOffset, 0, 0, 2);	    			
	    			}	    			
	    		}
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

    @Override
    public int getMaxEnergyStored() {
    	return MAX_ENERGY_VALUE;
    }

    // IEnergySink methods implementation
    @Override
    public int getMaxSafeInput() {
        return Integer.MAX_VALUE;
    }
}
