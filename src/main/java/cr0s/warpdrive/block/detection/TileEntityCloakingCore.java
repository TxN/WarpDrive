package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CloakedArea;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import java.util.Arrays;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.common.Optional;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityCloakingCore extends TileEntityAbstractEnergy {
	
	private static final int CLOAKING_CORE_SOUND_UPDATE_TICKS = 40;
	
	public boolean isEnabled = false;
	public byte tier = 1; // cloaking field tier, 1 or 2
	
	// inner coils color map
	final float[] innerCoilColor_r = { 1.00f, 1.00f, 1.00f, 1.00f, 0.75f, 0.25f, 0.00f, 0.00f, 0.00f, 0.00f, 0.50f, 1.00f }; 
	final float[] innerCoilColor_g = { 0.00f, 0.25f, 0.75f, 1.00f, 1.00f, 1.00f, 1.00f, 1.00f, 0.50f, 0.25f, 0.00f, 0.00f }; 
	final float[] innerCoilColor_b = { 0.25f, 0.00f, 0.00f, 0.00f, 0.00f, 0.00f, 0.50f, 1.00f, 1.00f, 1.00f, 1.00f, 0.75f }; 
	
	// Spatial cloaking field parameters
	private static final int innerCoilsDistance = 2; // Step length from core block to main coils
	private final int[] outerCoilsDistance = {0, 0, 0, 0, 0, 0};
	public int minX = 0;
	public int minY = 0;
	public int minZ = 0;
	public int maxX = 0;
	public int maxY = 0;
	public int maxZ = 0;
	
	public boolean isValid = false;
	public boolean isCloaking = false;
	public int volume = 0;
	private int updateTicks = 0;
	private int laserDrawingTicks = 0;
	
	private boolean soundPlayed = false;
	private int soundTicks = 0;
	
	public TileEntityCloakingCore() {
		super();
		peripheralName = "warpdriveCloakingCore";
		addMethods(new String[] {
			"tier",				// set field tier to 1 or 2, return field tier
			"isAssemblyValid",	// returns true or false
			"enable"			// set field enable state (true or false), return true if enabled
		});
		CC_scripts = Arrays.asList("cloak1", "cloak2", "uncloak");
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		// Reset sound timer
		soundTicks--;
		if (soundTicks < 0) {
			soundTicks = CLOAKING_CORE_SOUND_UPDATE_TICKS;
			soundPlayed = false;
		}
		
		updateTicks--;
		if (updateTicks <= 0) {
			if (WarpDriveConfig.LOGGING_CLOAKING) {
				WarpDrive.logger.info(this + " Updating cloaking state...");
			}
			updateTicks = ((tier == 1) ? 20 : (tier == 2) ? 10 : 20) * WarpDriveConfig.CLOAKING_FIELD_REFRESH_INTERVAL_SECONDS; // resetting timer
			
			isValid = validateAssembly();
			isCloaking = WarpDrive.cloaks.isAreaExists(worldObj, xCoord, yCoord, zCoord); 
			if (!isEnabled) {// disabled
				if (isCloaking) {// disabled, cloaking => stop cloaking
					if (WarpDriveConfig.LOGGING_CLOAKING) {
						WarpDrive.logger.info(this + " Disabled, cloak field going down...");
					}
					disableCloakingField();
				} else {// disabled, no cloaking
					// IDLE
				}
			} else {// isEnabled
				boolean hasEnoughPower = countBlocksAndConsumeEnergy();
				if (!isCloaking) {// enabled, not cloaking
					if (hasEnoughPower && isValid) {// enabled, can cloak and able to
						setCoilsState(true);
						
						// Register cloak
						WarpDrive.cloaks.updateCloakedArea(worldObj,
								worldObj.provider.dimensionId, xCoord, yCoord, zCoord, tier,
								minX, minY, minZ, maxX, maxY, maxZ);
						if (!soundPlayed) {
							soundPlayed = true;
							worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:cloak", 4F, 1F);
						}
						
						// Refresh the field
						CloakedArea area = WarpDrive.cloaks.getCloakedArea(worldObj, xCoord, yCoord, zCoord);
						if (area != null) {
							area.sendCloakPacketToPlayersEx(false); // re-cloak field
						} else {
							if (WarpDriveConfig.LOGGING_CLOAKING) {
								WarpDrive.logger.info("getCloakedArea1 returned null for " + worldObj + " " + xCoord + "," + yCoord + "," + zCoord);
							}
						}
					} else {// enabled, not cloaking but not able to
						// IDLE
					}
				} else {// enabled & cloaked
					if (!isValid) {// enabled, cloaking but invalid
						if (WarpDriveConfig.LOGGING_CLOAKING) {
							WarpDrive.logger.info(this + " Coil(s) lost, cloak field is collapsing...");
						}
						energy_consume(energy_getEnergyStored());
						disableCloakingField();				
					} else {// enabled, cloaking and valid
						if (hasEnoughPower) {// enabled, cloaking and able to
							// IDLE
							// Refresh the field (workaround to re-synchronize players since client may 'eat up' the packets)
							CloakedArea area = WarpDrive.cloaks.getCloakedArea(worldObj, xCoord, yCoord, zCoord);
							if (area != null) {
								area.sendCloakPacketToPlayersEx(false); // re-cloak field
							} else {
								if (WarpDriveConfig.LOGGING_CLOAKING) {
									WarpDrive.logger.info("getCloakedArea2 returned null for " + worldObj + " " + xCoord + "," + yCoord + "," + zCoord);
								}
							}
							setCoilsState(true);
						} else {// loosing power
							if (WarpDriveConfig.LOGGING_CLOAKING) {
								WarpDrive.logger.info(this + " Low power, cloak field is collapsing...");
							}
							disableCloakingField();
						}
					}
				}
			}
		}
		
		if (laserDrawingTicks++ > 100) {
			laserDrawingTicks = 0;
			
			if (isEnabled && isValid) {
				drawLasers();
			}
		}
	}
	
	private void setCoilsState(final boolean enabled) {
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (enabled) ? 1 : 0, 2);
		
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			setCoilState(innerCoilsDistance, direction, enabled);
			setCoilState(outerCoilsDistance[direction.ordinal()], direction, enabled);
		}
	}
	
	private void setCoilState(final int distance, final ForgeDirection direction, final boolean enabled) {
		int x = xCoord + distance * direction.offsetX;
		int y = yCoord + distance * direction.offsetY;
		int z = zCoord + distance * direction.offsetZ;
		if (worldObj.getBlock(x, y, z).isAssociatedBlock(WarpDrive.blockCloakingCoil)) {
			if (distance == innerCoilsDistance) {
				worldObj.setBlockMetadataWithNotify(x, y, z, ((enabled) ? 9 : 1), 2);
			} else {
				worldObj.setBlockMetadataWithNotify(x, y, z, ((enabled) ? 10 : 2) + direction.ordinal(), 2);
			}
		}
	}
	
	private void drawLasers() {
		float r = 0.0f;
		float g = 1.0f;
		float b = 0.0f;
		if (!isCloaking) {// out of energy
			r = 0.75f;
			g = 0.50f;
			b = 0.50f;
		} else if (tier == 1) {
			r = 0.25f;
			g = 1.00f;
			b = 0.00f;
		} else if (tier == 2) {
			r = 0.00f;
			g = 0.25f;
			b = 1.00f;
		}
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			PacketHandler.sendBeamPacketToPlayersInArea(worldObj,
					new Vector3(
						xCoord + innerCoilsDistance * direction.offsetX,
						yCoord + innerCoilsDistance * direction.offsetY,
						zCoord + innerCoilsDistance * direction.offsetZ).translate(0.5),
					new Vector3(
						xCoord + outerCoilsDistance[direction.ordinal()] * direction.offsetX,
						yCoord + outerCoilsDistance[direction.ordinal()] * direction.offsetY,
						zCoord + outerCoilsDistance[direction.ordinal()] * direction.offsetZ).translate(0.5),
					r, g, b, 110, 0,
					AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
		}
		
		// draw connecting coils
		for (int i = 0; i < 5; i++) {
			ForgeDirection start = ForgeDirection.VALID_DIRECTIONS[i];
			for (int j = i + 1; j < 6; j++) {
				ForgeDirection stop = ForgeDirection.VALID_DIRECTIONS[j];
				// skip mirrored coils (removing the inner lines)
				if (start.getOpposite() == stop) {
					continue;
				}
				
				// draw a random colored beam
				int mapIndex = worldObj.rand.nextInt(innerCoilColor_b.length);
				r = innerCoilColor_r[mapIndex];
				g = innerCoilColor_g[mapIndex];
				b = innerCoilColor_b[mapIndex];
				
				PacketHandler.sendBeamPacketToPlayersInArea(worldObj,
					new Vector3(xCoord + innerCoilsDistance * start.offsetX, yCoord + innerCoilsDistance * start.offsetY, zCoord + innerCoilsDistance * start.offsetZ).translate(0.5),
					new Vector3(xCoord + innerCoilsDistance * stop .offsetX, yCoord + innerCoilsDistance * stop .offsetY, zCoord + innerCoilsDistance * stop .offsetZ).translate(0.5),
					r, g, b, 110, 0,
					AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
			}
		}
	}
	
	public void disableCloakingField() {
		setCoilsState(false);
		if (WarpDrive.cloaks.isAreaExists(worldObj, xCoord, yCoord, zCoord)) {
			WarpDrive.cloaks.removeCloakedArea(worldObj.provider.dimensionId, xCoord, yCoord, zCoord);
			
			if (!soundPlayed) {
				soundPlayed = true;
				worldObj.playSoundEffect(xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f, "warpdrive:decloak", 4F, 1F);
			}
		}
	}
	
	public boolean countBlocksAndConsumeEnergy() {
		int x, y, z, energyToConsume;
		volume = 0;
		if (tier == 1) {// tier1 = gaz and air blocks don't count
			for (y = minY; y <= maxY; y++) {
				for (x = minX; x <= maxX; x++) {
					for(z = minZ; z <= maxZ; z++) {
						if (!worldObj.isAirBlock(x, y, z)) {
							volume++;
						} 
					}
				}
			}
			energyToConsume = volume * WarpDriveConfig.CLOAKING_TIER1_ENERGY_PER_BLOCK;
		} else {// tier2 = everything counts
			for (y = minY; y <= maxY; y++) {
				for (x = minX; x <= maxX; x++) {
					for(z = minZ; z <= maxZ; z++) {
						if (!worldObj.getBlock(x, y, z) .isAssociatedBlock(Blocks.air)) {
							volume++;
						} 
					}
				}
			}
			energyToConsume = volume * WarpDriveConfig.CLOAKING_TIER2_ENERGY_PER_BLOCK;
		}
		
		// WarpDrive.logger.info(this + " Consuming " + energyToConsume + " EU for " + blocksCount + " blocks");
		return energy_consume(energyToConsume, false);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		tier = tag.getByte("tier");
		isEnabled = tag.getBoolean("enabled");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("tier", tier);
		tag.setBoolean("enabled", isEnabled);
	}
	
	public boolean validateAssembly() {
		final int maxOuterCoilDistance = WarpDriveConfig.CLOAKING_MAX_FIELD_RADIUS - WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS; 
		
		// Directions to check (all six directions: left, right, up, down, front, back)
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			
			// check validity of inner coil
			int x = xCoord + innerCoilsDistance * direction.offsetX;
			int y = yCoord + innerCoilsDistance * direction.offsetY;
			int z = zCoord + innerCoilsDistance * direction.offsetZ;
			if (worldObj.getBlock(x, y, z).isAssociatedBlock(WarpDrive.blockCloakingCoil)) {
				worldObj.setBlockMetadataWithNotify(x, y, z, 1, 2);
			} else {
				return false;
			}
			
			// find closest outer coil
			int newCoilDistance = 0;
			for (int distance = 3; distance < maxOuterCoilDistance; distance++) {
				x += direction.offsetX;
				y += direction.offsetY;
				z += direction.offsetZ;
				
				if (worldObj.getBlock(x, y, z).isAssociatedBlock(WarpDrive.blockCloakingCoil)) {
					worldObj.setBlockMetadataWithNotify(x, y, z, 2 + direction.ordinal(), 2);
					newCoilDistance = distance;
					break;
				}
			}
			
			// disable previous outer coil, in case a different one was found
			int oldCoilDistance = outerCoilsDistance[direction.ordinal()];
			if ( newCoilDistance != oldCoilDistance && oldCoilDistance > 0) {
				int oldX = xCoord + oldCoilDistance * direction.offsetX;
				int oldY = yCoord + oldCoilDistance * direction.offsetY;
				int oldZ = zCoord + oldCoilDistance * direction.offsetZ;
				if (worldObj.getBlock(oldX, oldY, oldZ).isAssociatedBlock(WarpDrive.blockCloakingCoil)) {
					worldObj.setBlockMetadataWithNotify(oldX, oldY, oldZ, 0, 2);
				}
			}
			
			// check validity and save new coil position
			if (newCoilDistance <= 0) {
				outerCoilsDistance[direction.ordinal()] = 0;
				if (WarpDriveConfig.LOGGING_CLOAKING) {
					WarpDrive.logger.info("Invalid outer coil assembly at " + direction);
				}
				return false;
			}
			outerCoilsDistance[direction.ordinal()] = newCoilDistance;
		}
		
		// Update cloaking field parameters defined by coils		
		minX =               xCoord - outerCoilsDistance[4] - WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS;
		maxX =               xCoord + outerCoilsDistance[5] + WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS;
		minY = Math.max(  0, yCoord - outerCoilsDistance[0] - WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS);
		maxY = Math.min(255, yCoord + outerCoilsDistance[1] + WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS);
		minZ =               zCoord - outerCoilsDistance[2] - WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS;
		maxZ =               zCoord + outerCoilsDistance[3] + WarpDriveConfig.CLOAKING_COIL_CAPTURE_BLOCKS;
		return true;
	}
	
	@Override
	public String getStatus() {
		if (worldObj == null) {
			return super.getStatus();
		}
		
		final String unlocalizedStatus;
		if (!isValid) {
			unlocalizedStatus = "warpdrive.cloakingCore.invalidAssembly";
		} else if (!isEnabled) {
			unlocalizedStatus = "warpdrive.cloakingCore.disabled";
		} else if (!isCloaking) {
			unlocalizedStatus = "warpdrive.cloakingCore.lowPower";
		} else {
			unlocalizedStatus = "warpdrive.cloakingCore.cloaking";
		}
		return super.getStatus()
		    + "\n" + StatCollector.translateToLocalFormatted(unlocalizedStatus,
				tier,
				volume);
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] tier(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			if (arguments.checkInteger(0) == 2) {
				tier = 2;
			} else {
				tier = 1;
			}
			markDirty();
		}
		return new Integer[] { (int)tier };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] isAssemblyValid(Context context, Arguments arguments) {
		return new Object[] { validateAssembly() };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			isEnabled = arguments.checkBoolean(0);
			markDirty();
		}
		return new Object[] { isEnabled };
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		switch (methodName) {
			case "tier":
				if (arguments.length == 1) {
					if (Commons.toInt(arguments[0]) == 2) {
						tier = 2;
					} else {
						tier = 1;
					}
					markDirty();
				}
				return new Integer[] { (int) tier };

			case "isAssemblyValid":
				return new Object[] { validateAssembly() };

			case "enable":
				if (arguments.length == 1) {
					isEnabled = Commons.toBool(arguments[0]);
					markDirty();
				}
				return new Object[] { isEnabled };
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public int energy_getMaxStorage() {
		return WarpDriveConfig.CLOAKING_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean energy_canInput(ForgeDirection from) {
		return true;
	}
}
