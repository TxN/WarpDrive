package cr0s.warpdrive.api;

import cr0s.warpdrive.data.VectorI;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;

public interface IStarMapRegistryTileEntity {
	
	// get the registry type
	String getStarMapType();
	
	// get the unique id
	UUID getUUID();
	
	// get the area controlled by this tile entity 
	AxisAlignedBB getStarMapArea();
	
	// mass of the multi-block
	int getMass();
	
	// isolation rate from radars
	double getIsolationRate();
	
	// name to remove for Friend-or-Foe
	String getStarMapName();
	
	// report an update in the area
	void onBlockUpdatedInArea(final VectorI vector, final Block block, final int metadata);
}
