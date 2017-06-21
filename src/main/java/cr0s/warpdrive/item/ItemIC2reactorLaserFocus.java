package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.InterfaceList({
	@Optional.Interface(iface = "ic2.api.reactor.IReactorComponent", modid = "IC2")
	})
public class ItemIC2reactorLaserFocus extends Item implements IReactorComponent {
	
	private static final int MAX_HEAT = 3000;
	
	public ItemIC2reactorLaserFocus() {
		super();
		setMaxDamage(MAX_HEAT);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setUnlocalizedName("warpdrive.energy.IC2reactorLaserFocus");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		itemIcon = iconRegister.registerIcon("warpdrive:reactorFocus");
	}
	
	private static void damageComponent(ItemStack self, int damage) {
		int currDamage = self.getItemDamage();
		int nextDamage = Math.min(MAX_HEAT, Math.max(0, currDamage + damage));
		self.setItemDamage(nextDamage);
	}
	
	private static void balanceComponent(ItemStack self, ItemStack other) {
		final int selfBalance = 4;
		int otherDamage = other.getItemDamage();
		int myDamage = self.getItemDamage();
		int newOne = (otherDamage + (selfBalance - 1) * myDamage) / selfBalance;
		int newTwo = otherDamage - (newOne - myDamage);
		self.setItemDamage(newTwo);
		other.setItemDamage(newOne);
	}
	
	@Optional.Method(modid = "IC2")
	private static void coolComponent(ItemStack self, IReactorComponent comp, IReactor reactor, ItemStack stack, int x, int y) {
		int maxTransfer = MAX_HEAT - self.getItemDamage();
		int compHeat = comp.getCurrentHeat(reactor, stack, x, y);
		int transferHeat = -Math.min(compHeat, maxTransfer);
		int retained = comp.alterHeat(reactor, stack, x, y, transferHeat);
		damageComponent(self, retained - transferHeat);
	}
	
	@Optional.Method(modid = "IC2")
	private static void coolReactor(IReactor reactor, ItemStack stack) {
		int reactorHeat = reactor.getHeat();
		int myHeat = stack.getItemDamage();
		int transfer = Math.min(MAX_HEAT - myHeat, reactorHeat);
		reactor.addHeat(-transfer);
		damageComponent(stack, transfer);
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public void processChamber(IReactor reactor, ItemStack yourStack, int x, int y, boolean heatrun) {
		if (heatrun) {
			int[] xDif = { -1, 0, 0, 1 };
			int[] yDif = { 0, -1, 1, 0 };
			for (int i = 0; i < xDif.length; i++) {
				int iX = x + xDif[i];
				int iY = y + yDif[i];
				ItemStack stack = reactor.getItemAt(iX, iY);
				if (stack != null) {
					Item item = stack.getItem();
					if (item instanceof ItemIC2reactorLaserFocus) {
						balanceComponent(yourStack, stack);
					} else if (item instanceof IReactorComponent) {
						coolComponent(yourStack, (IReactorComponent) item, reactor, stack, iX, iY);
					}
				}
			}
			
			coolReactor(reactor, yourStack);
		}
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public boolean acceptUraniumPulse(IReactor reactor, ItemStack yourStack, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun) {
		return false;
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public boolean canStoreHeat(IReactor reactor, ItemStack yourStack, int x, int y) {
		return true;
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public int getMaxHeat(IReactor reactor, ItemStack yourStack, int x, int y) {
		return MAX_HEAT;
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public int getCurrentHeat(IReactor reactor, ItemStack yourStack, int x, int y) {
		return yourStack.getItemDamage();
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public int alterHeat(IReactor reactor, ItemStack yourStack, int x, int y, int heat) {
		if (WarpDriveConfig.LOGGING_ENERGY) {
			WarpDrive.logger.info(this + " alterHeat " + heat);
		}
		int transferred = Math.min(heat, MAX_HEAT - yourStack.getItemDamage());
		damageComponent(yourStack, transferred);
		return heat - transferred;
	}
	
	@Override
	@Optional.Method(modid = "IC2")
	public float influenceExplosion(IReactor reactor, ItemStack yourStack) {
		return 0;
	}
	
}
