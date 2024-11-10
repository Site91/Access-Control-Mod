package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.interfaces.IReader;
import com.cadergator10.advancedbasesecurity.common.items.IDCard;
import com.cadergator10.advancedbasesecurity.util.ReaderText;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TileEntityCardReader extends TileEntity implements IReader {
	public UUID deviceId;
	public int lightFlag;
	public ReaderText currText;

	public TileEntityCardReader(){
		super();
	}

	public String doRead(@Nonnull ItemStack itemStack, EntityPlayer em, EnumFacing side) {
		IDCard.CardTag cardTag = new IDCard.CardTag(itemStack);
		//perform request to the global system
		return "access";
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("deviceId"))
			this.deviceId = nbt.getUniqueId("doordeviceId");
		else
			this.deviceId = null; //commented out NBT for some of it since I'm going to be using the DoorHandler values.
//		if(nbt.hasKey("lights"))
//			this.lightFlag = nbt.getInteger("lights");
//		else
//			this.lightFlag = 0;
//		if(nbt.hasKey("textlabel"))
//			this.currText = new TextComponentString(nbt.getString("textlabel"));
		lightFlag = AdvBaseSecurity.instance.doorHandler.getReaderLight(deviceId);
		currText = AdvBaseSecurity.instance.doorHandler.getReaderLabel(deviceId);
		//check if in list
		if(!AdvBaseSecurity.instance.doorHandler.allReaders.containsKey(this.deviceId))
			AdvBaseSecurity.instance.doorHandler.allReaders.put(this.deviceId, this);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(this.deviceId != null)
			nbt.setUniqueId("swipeInd", this.deviceId);
//		nbt.setInteger("", this.lightFlag);
//		if(this.currText != null)
//			nbt.setString("textlabel",this.currText.getFormattedText());
		return nbt;
	}

	@Override
	public void newId() {
		this.deviceId = UUID.randomUUID();
	}

	@Override
	public UUID getId() {
		return this.deviceId;
	}

	@Override
	public String getDevType() {
		return "reader";
	}

	@Override
	public void updateVisuals(int light, ReaderText str) {
		lightFlag = light;
		currText = str;
	}

	public IBlockState getFacing()
	{
		return world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()));
	}

	public float getAngle()
	{
		//this was getFacing()
		return getFacing().getBlock().getMetaFromState(getFacing()) * 270;
	}
}
