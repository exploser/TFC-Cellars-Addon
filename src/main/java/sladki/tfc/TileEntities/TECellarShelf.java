package sladki.tfc.TileEntities;

import com.bioxx.tfc.Core.TFC_Core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class TECellarShelf extends TileEntity implements IInventory
{

	private ItemStack[] inventory;

	private boolean inCellar = false;
	private float temperature = 0;

	private int updateTickCounter = 120;

	public TECellarShelf()
	{
		inventory = new ItemStack[getSizeInventory()];
	}

	/*
	 * public void getShelfInfo(EntityPlayer player) { player.addChatMessage(new
	 * ChatComponentText("In cellar: " + inCellar)); player.addChatMessage(new
	 * ChatComponentText("Temperature: " + temperature)); }
	 */

	@Override
	public void updateEntity()
	{
		if (worldObj.isRemote)
		{
			return;
		}

		// Wait 120 ticks for cellars updates to prevent ticking decay before
		if (inCellar)
		{
			// if the shelf is inside a cellar, use cellar temperature
			float envDecay = TFC_Core.getEnvironmentalDecay(temperature);
			TFC_Core.handleItemTicking(this, worldObj, xCoord, yCoord, zCoord, envDecay);
		}
		else
		{
			// otherwise, let TFC handle the ticking
			if (updateTickCounter > 0)
			{
				updateTickCounter--;

				// To ensure correct work for cellars built in multiple chunks
				// TODO: enable this if really needed
				/*
				 * if (updateTickCounter == 100) { World world =
				 * this.getWorldObj();
				 * 
				 * world.getBlock(xCoord + 4, 0, zCoord); world.getBlock(xCoord
				 * - 4, 0, zCoord); world.getBlock(xCoord, 0, zCoord + 4);
				 * world.getBlock(xCoord, 0, zCoord - 4);
				 * 
				 * world.getBlock(xCoord + 4, 0, zCoord - 4);
				 * world.getBlock(xCoord + 4, 0, zCoord + 4);
				 * world.getBlock(xCoord - 4, 0, zCoord - 4);
				 * world.getBlock(xCoord - 4, 0, zCoord + 4); }
				 */
				return;
			}

			TFC_Core.handleItemTicking(this, worldObj, xCoord, yCoord, zCoord);
		}
	}

	public void updateShelf(boolean inCellar, float temp)
	{
		this.inCellar = inCellar;
		this.temperature = temp;
	}

	@Override
	public int getSizeInventory()
	{
		return 14;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		ItemStack stack = inventory[slot];
		if (stack != null)
		{
			if (stack.stackSize <= amount)
			{
				setInventorySlotContents(slot, null);
			}
			else
			{
				stack = stack.splitStack(amount);
			}
		}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		inventory[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit())
		{
			stack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public String getInventoryName()
	{
		return "Cellar Shelf";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return true;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++)
		{
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < getSizeInventory())
			{
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);

		NBTTagList tagList = new NBTTagList();
		for (int i = 0; i < getSizeInventory(); i++)
		{
			if (inventory[i] != null)
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				inventory[i].writeToNBT(tag);
				tagList.appendTag(tag);
			}
		}
		tagCompound.setTag("Items", tagList);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tagCompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		readFromNBT(packet.func_148857_g());
	}

}
