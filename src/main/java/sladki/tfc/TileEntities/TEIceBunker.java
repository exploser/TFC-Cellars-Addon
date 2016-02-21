package sladki.tfc.TileEntities;

import java.util.Arrays;
import java.util.List;

import com.bioxx.tfc.Core.TFC_Climate;
import com.bioxx.tfc.Core.TFC_Time;
import com.bioxx.tfc.api.TFCBlocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.Constants;
import ru.exsdev.ModManagerReworked;
import ru.exsdev.TileEntities.TEFoodPrepReworked;
import ru.exsdev.Utility.Coolants;
import sladki.tfc.ModConfig;
import sladki.tfc.ModManager;
import sladki.tfc.Blocks.BlockCellarDoor;

public class TEIceBunker extends TileEntity implements IInventory
{

	// NBT
	private ItemStack[] inventory = null;
	private float coolantAmount = 0;
	private long lastUpdate = 0;

	private int[] entrance = new int[4]; // x, z of the first door + offsetX,
											// offsetZ of the second door
	private int[] oldSize = new int[4]; // need in order to update containers if
										// a cellar has become not complete
	private int[] size = new int[4]; // internal size, +z -x -z + x
	private boolean isComplete = false;
	private float temperature = ModConfig.cellarTemperature;
	private int updateTickCounter = 1200;

	private int loss = 0;
	private int maxCoolantAmount = 120;

	private static String statusComplete = "Cooling...";

	private static int innerHeight = 2;
	private static List<Block> allowedBlocks = Arrays.asList(
			Blocks.wall_sign,
			Blocks.standing_sign,
			Blocks.air,
			TFCBlocks.barrel,
			TFCBlocks.foodPrep,
			ModManager.CellarWallBlock,
			ModManager.CellarShelfBlock,
			ModManagerReworked.BlockFoodPrepReworked
			);

	private enum BlockType
	{
		Block_Other,
		Block_Wall,
		Block_Door,
		Block_Allowed,
	}

	public TEIceBunker()
	{
		inventory = new ItemStack[getSizeInventory()];
	}

	public void getCellarInfo(EntityPlayer player)
	{
		if (isComplete())
		{
			if (temperature < 0)
			{
				player.addChatMessage(new ChatComponentText("It is icy here"));
			}
			else if (temperature < ModConfig.cellarTemperature - 1)
			{
				player.addChatMessage(new ChatComponentText("It is freezing here"));
			}
			else
			{
				player.addChatMessage(new ChatComponentText("The cellar is chilly"));
			}
			player.addChatMessage(new ChatComponentText(
					String.format("Temperature: %.2f, Coolant: %.2f", temperature, coolantAmount)));
		}
		else
		{
			player.addChatMessage(new ChatComponentText("The cellar is not complete: " + structureStatus()));
		}
	}

	@Override
	public void updateEntity()
	{
		if (worldObj.isRemote)
		{
			return;
		}

		// Check cellar compliance once per 1200 ticks, check coolant and update
		// containers once per 100 ticks
		if (updateTickCounter % 100 == 0)
		{

			if (updateTickCounter >= 1200)
			{
				updateCellar(true);
				updateTickCounter = 0;
			}
			else
			{
				updateCellar(false);
			}

			updateContainers(false);
		}
		updateTickCounter++;
	}

	private void updateCellar(boolean checkCompliance)
	{

		temperature = ModConfig.cellarTemperature;

		if (checkCompliance)
		{
			isComplete = structureStatus() == statusComplete;
			this.worldObj.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
		}

		if (isComplete())
		{
			float outsideTemp = TFC_Climate.getHeightAdjustedTemp(this.worldObj, xCoord, yCoord + 1, zCoord);
			if (coolantAmount <= 0)
			{
				for (int slot = 3; slot >= 0; slot--)
				{
					if (inventory[slot] != null)
					{
						int addedCoolant = Coolants.getCoolantFromItem(inventory[slot].getItem());
						coolantAmount += addedCoolant;
						maxCoolantAmount = addedCoolant;
						lastUpdate = TFC_Time.getTotalHours();
						decrStackSize(slot, 1);
						temperature = ModConfig.iceHouseTemperature;
						break;
					}
				}
			}

			if (coolantAmount > 0)
			{
				// TODO: check the formula
				if (lastUpdate < TFC_Time.getTotalHours())
				{
					if (outsideTemp > ModConfig.iceHouseTemperature + loss)
					{
						int volume = (size[1] + size[3] + 1) * (size[0] + size[2] + 1);
						coolantAmount -= (ModConfig.coolantConsumptionMultiplier
								* (0.025 * volume * outsideTemp + volume + 2)) / TFC_Time.HOURS_IN_DAY;
					}
					lastUpdate++;
				}
			}
			temperature = Math.min(ModConfig.iceHouseTemperature + loss, outsideTemp);

		}
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	private void calculateDoorsLoss()
	{
		loss = 0;
		// 1st door
		Block door = this.worldObj.getBlock(xCoord + entrance[0], yCoord + 1, zCoord + entrance[1]);
		if (door == ModManager.CellarDoorBlock && !((BlockCellarDoor) door).isDoorOpen(this.worldObj,
				xCoord + entrance[0], yCoord + 1, zCoord + entrance[1]))
		{

		}
		else
		{
			loss = 1;
		}

		// 2nd door
		door = this.worldObj.getBlock(xCoord + entrance[0] + entrance[2], yCoord + 1,
				zCoord + entrance[1] + entrance[3]);
		if (door == ModManager.CellarDoorBlock && !((BlockCellarDoor) door).isDoorOpen(this.worldObj,
				xCoord + entrance[0] + entrance[2], yCoord + 1, zCoord + entrance[1] + entrance[3]))
		{

		}
		else
		{
			if (loss > 0)
			{
				loss = 4;
			}
			else
			{
				loss = 1;
			}
		}
	}

	private String structureStatus()
	{
		oldSize[1] = size[1];
		oldSize[3] = size[3];
		oldSize[2] = size[2];
		oldSize[0] = size[0];

		entrance[0] = 0;
		entrance[1] = 0;
		entrance[2] = 0;
		entrance[3] = 0;

		BlockType blockType = BlockType.Block_Other;

		// get size
		for (int direction = 0; direction < 4; direction++)
		{
			for (int distance = 1; distance < 6; distance++)
			{
				// max distance between an ice bunker and a wall is 3
				if (distance == 5)
				{
					if (ModConfig.isDebugging)
					{
						System.out.println("Cellar at " + this.xCoord + " " + this.yCoord + " " + this.zCoord
								+ " can't find a wall on " + direction + " side");
					}

					return "Hole in a wall";
				}

				if (direction == 1)
				{
					blockType = getBlockType(-distance, 1, 0);
				}
				else if (direction == 3)
				{
					blockType = getBlockType(distance, 1, 0);
				}
				else if (direction == 2)
				{
					blockType = getBlockType(0, 1, -distance);
				}
				else if (direction == 0)
				{
					blockType = getBlockType(0, 1, distance);
				}

				if (blockType == BlockType.Block_Door || blockType == BlockType.Block_Wall)
				{
					size[direction] = distance - 1;
					break;
				}

				if (blockType == BlockType.Block_Other)
				{
					return "Unwanted blocks inside a wall";
				}
			}
		}

		// check blocks and set entrance
		for (int y = 0; y < innerHeight + 2; y++)
		{
			for (int x = -size[1] - 1; x <= size[3] + 1; x++)
			{
				for (int z = -size[2] - 1; z <= size[0] + 1; z++)
				{

					// Ice bunker
					if (y == 0 && x == 0 && z == 0)
					{
						continue;
					}

					blockType = getBlockType(x, y, z);

					// Blocks inside the cellar
					if (y > 0 && y <= innerHeight)
					{
						if (x >= -size[1] && x <= size[3])
						{
							if (z >= -size[2] && z <= size[0])
							{
								if (blockType == BlockType.Block_Allowed)
								{
									continue;
								}
								return "Unwanted blocks inside the cellar: " + this.worldObj
										.getBlock(xCoord + x, yCoord + y, zCoord + z).getUnlocalizedName();
							}
						}
					}

					// Corners
					if ((x == -size[1] - 1 || x == size[3] + 1) && (z == -size[2] - 1 || z == size[0] + 1))
					{
						if (blockType == BlockType.Block_Wall)
						{
							continue;
						}
						return "Incomplete corners";
					}

					// Doors
					if (blockType == BlockType.Block_Door)
					{
						// upper part of the door
						if (entrance[0] == x && entrance[1] == z)
						{
							continue;
						}

						// 1 entrance only!
						if (entrance[0] == 0 && entrance[1] == 0)
						{
							entrance[0] = x;
							entrance[1] = z;
							if (x == -size[1] - 1)
							{
								entrance[2] = -1;
							}
							else if (x == size[3] + 1)
							{
								entrance[2] = 1;
							}
							else if (z == -size[2] - 1)
							{
								entrance[3] = -1;
							}
							else if (z == size[0] + 1)
							{
								entrance[3] = 1;
							}

							continue;
						}

						if (ModConfig.isDebugging)
						{
							System.out.println("Cellar at " + this.xCoord + " " + this.yCoord + " " + this.zCoord
									+ " has too many doors");
						}

						return "Multiple doors";
					}

					// Walls
					if (blockType == BlockType.Block_Wall)
					{
						continue;
					}
					return "Wrong block somewhere " + blockType.toString() + " " + x + " " + y + " " + z;
				}
			}
		}

		if (entrance[0] == 0 && entrance[1] == 0)
		{
			if (ModConfig.isDebugging)
			{
				System.out
						.println("Cellar at " + this.xCoord + " " + this.yCoord + " " + this.zCoord + " has no doors");
			}
			return "No doors found";
		}

		// check the entrance
		// TODO: why is y comparison so strange?
		for (int y = 0; y < innerHeight + 2; y++)
		{
			for (int x = -MathHelper.abs_int(entrance[3]); x <= MathHelper.abs_int(entrance[3]); x++)
			{
				for (int z = -MathHelper.abs_int(entrance[2]); z <= MathHelper.abs_int(entrance[2]); z++)
				{

					blockType = getBlockType(x + entrance[0] + entrance[2], y, z + entrance[1] + entrance[3]);

					if (y > 0 && y < innerHeight + 1)
					{
						if (x == 0 && z == 0)
						{
							if (blockType == BlockType.Block_Door)
							{
								continue;
							}

							if (ModConfig.isDebugging)
							{
								System.out.println("Cellar at " + this.xCoord + " " + this.yCoord + " " + this.zCoord
										+ " doesn't have the second door, block there is " + blockType);
							}
							return "Only one door found";
						}
					}

					if (blockType == BlockType.Block_Wall)
					{
						continue;
					}

					if (ModConfig.isDebugging)
					{
						System.out.println("Door in the cellar at " + this.xCoord + " " + this.yCoord + " "
								+ this.zCoord + " isn't surrounded by wall, block there is " + blockType);
					}
					return "Doors are not surrounded by walls";
				}
			}
		}

		if (ModConfig.isDebugging)
		{
			System.out.println("Cellar at " + this.xCoord + " " + this.yCoord + " " + this.zCoord + " is complete");
		}

		calculateDoorsLoss();

		return statusComplete;
	}

	private BlockType getBlockType(int x, int y, int z)
	{
		Block block = this.getWorldObj().getBlock(xCoord + x, yCoord + y, zCoord + z);
		if (block == ModManager.CellarWallBlock)
		{
			return BlockType.Block_Wall;
		}
		else if (block == ModManager.CellarDoorBlock)
		{
			return BlockType.Block_Door;
		}
		else if (allowedBlocks.contains(block))
		{
			return BlockType.Block_Allowed;
		}

		if (ModConfig.isDebugging)
		{
			MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(
					"Incorrect cellar block at " + x + " " + y + " " + z + " " + block.getUnlocalizedName()));

		}

		return BlockType.Block_Other;
	}

	public void updateContainers(boolean isDestroying)
	{
		if (isDestroying)
		{
			isComplete = false;
			oldSize[1] = size[1];
			oldSize[3] = size[3];
			oldSize[2] = size[2];
			oldSize[0] = size[0];
		}

		for (int y = 1; y <= innerHeight; y++)
		{
			if (isComplete())
			{
				for (int z = -size[2]; z <= size[0]; z++)
				{
					for (int x = -size[1]; x <= size[3]; x++)
					{
						updateContainer(x, y, z);
					}
				}
			}
			else
			{
				for (int z = -oldSize[2]; z <= oldSize[0]; z++)
				{
					for (int x = -oldSize[1]; x <= oldSize[3]; x++)
					{
						updateContainer(x, y, z);
					}
				}
			}
		}
	}

	private void replaceFoodPrep(int x, int y, int z)
	{
		if (this.getWorldObj().isRemote || !isComplete())
		{
			return;
		}
		this.getWorldObj().setBlock(x, y, z, ModManagerReworked.BlockFoodPrepReworked);
	}

	private void updateContainer(int x, int y, int z)
	{
		Block block = this.getWorldObj().getBlock(xCoord + x, yCoord + y, zCoord + z);
		if (block == ModManager.CellarShelfBlock)
		{
			TileEntity tileEntity = this.worldObj.getTileEntity(xCoord + x, yCoord + y, zCoord + z);
			if (tileEntity != null)
			{
				((TECellarShelf) tileEntity).updateShelf(isComplete(), temperature);
			}
		}
		else if (block == ModManagerReworked.BlockFoodPrepReworked)
		{
			TileEntity tileEntity = this.worldObj.getTileEntity(xCoord + x, yCoord + y, zCoord + z);
			if (tileEntity != null)
			{
				((TEFoodPrepReworked) tileEntity).updateCellarState(isComplete(), getTemperature());
			}
		}
		else if (block == TFCBlocks.foodPrep)
		{
			TileEntity tileEntity = this.worldObj.getTileEntity(xCoord + x, yCoord + y, zCoord + z);
			if (tileEntity != null)
			{
				IInventory fp = (IInventory) tileEntity;

				ItemStack[] stacks = new ItemStack[fp.getSizeInventory()];

				for (int i = 0; i < fp.getSizeInventory(); i++)
				{
					stacks[i] = fp.getStackInSlot(i);
				}

				this.worldObj.removeTileEntity(xCoord + x, yCoord + y, zCoord + z);
				this.worldObj.setBlock(xCoord + x, yCoord + y, zCoord + z, ModManagerReworked.BlockFoodPrepReworked);

				TileEntity tileEntityNew = this.worldObj.getTileEntity(xCoord + x, yCoord + y, zCoord + z);

				fp = (IInventory) tileEntityNew;

				if (tileEntityNew != null)
				{
					for (int i = 0; i < fp.getSizeInventory(); i++)
					{
						fp.setInventorySlotContents(i, stacks[i]);
					}

					((TEFoodPrepReworked) tileEntityNew).updateCellarState(isComplete(), getTemperature());
				}

			}
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 5;
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
		return "Ice Bunker";
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
		lastUpdate = tagCompound.getLong("LastUpdate");
		coolantAmount = tagCompound.getFloat("CoolantAmount");
		maxCoolantAmount = tagCompound.getInteger("MaxCoolantAmount");
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
		tagCompound.setLong("LastUpdate", lastUpdate);
		tagCompound.setFloat("CoolantAmount", coolantAmount);
		tagCompound.setInteger("MaxCoolantAmount", maxCoolantAmount);

	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		tagCompound.setFloat("CoolantAmount", coolantAmount);
		tagCompound.setInteger("MaxCoolantAmount", maxCoolantAmount);
		tagCompound.setFloat("Temperature", temperature);
		tagCompound.setBoolean("IsComplete", isComplete());
		writeToNBT(tagCompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tagCompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
		coolantAmount = tagCompound.getInteger("CoolantAmount");
		maxCoolantAmount = tagCompound.getInteger("MaxCoolantAmount");
		temperature = tagCompound.getFloat("Temperature");
		this.isComplete = tagCompound.getBoolean("IsComplete");
	}

	public float getCoolantAmount()
	{
		return this.coolantAmount;
	}

	public float getTemperature()
	{
		if (isComplete())
			return this.temperature;
		else
			return TFC_Climate.getHeightAdjustedTemp(this.worldObj, xCoord, yCoord + 1, zCoord);
	}

	public int getMaxCoolantAmount()
	{
		return maxCoolantAmount;
	}

	public boolean isComplete()
	{
		return isComplete;
	}

}
