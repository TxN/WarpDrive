package cr0s.warpdrive.block.weapon;

import cr0s.warpdrive.block.TileEntityAbstractInterfaced;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityWeaponController extends TileEntityAbstractInterfaced {
	
	public TileEntityWeaponController() {
		super();
		
		peripheralName = "warpdriveWeaponController";
		addMethods(new String[] {
		});
		CC_scripts = Arrays.asList("startup");
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
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
	public String toString() {
		return String.format("%s @ \'%s\' (%d %d %d)", getClass().getSimpleName(),
				worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord);
	}
}