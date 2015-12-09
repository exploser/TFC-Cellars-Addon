package sladki.tfc.Gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import net.minecraft.util.StatCollector;

import sladki.tfc.Cellars;
import sladki.tfc.Containers.ContainerIceBunker;
import sladki.tfc.TileEntities.TEIceBunker;

public class GuiIceBunker extends GuiContainer {

	private static ResourceLocation texture = new ResourceLocation(Cellars.MODID, "textures/gui/gui_iceBunker.png");
	private static Minecraft mc = Minecraft.getMinecraft();
	private int coolantAmount = 0;
	private float temperature = 0;

	public GuiIceBunker(InventoryPlayer inventoryPlayer, TEIceBunker tileEntity) {
		super(new ContainerIceBunker(inventoryPlayer, tileEntity));
		coolantAmount = tileEntity.getCoolantAmount();
		temperature = tileEntity.getTemperature();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		mc.getTextureManager().bindTexture(texture);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		// draw text and stuff here
		// the parameters for drawString are: string, x, y, color
		fontRendererObj.drawString("Coolant:", 8, 32, 0);
		fontRendererObj.drawString(coolantAmount + "/1200", 8, 42, 0);
		fontRendererObj.drawString("Temp.:", xSize - 50, 32, 0);
		fontRendererObj.drawString(Math.round(temperature) + "", xSize - 50, 42, 0);
		// draws "Inventory" or your regional equivalent
		mc.fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 0);
	}
}
