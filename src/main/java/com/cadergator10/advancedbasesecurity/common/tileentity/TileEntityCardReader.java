package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.interfaces.IReader;
import com.cadergator10.advancedbasesecurity.common.items.IDCard;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import com.cadergator10.advancedbasesecurity.util.ReaderText;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TileEntityCardReader extends TileEntitySimpleBase implements IReader, ITickable {
	public UUID deviceId = UUID.randomUUID();
	public int lightFlag = 0;
	public ReaderText tempText = new ReaderText("ERROR", (byte)4); //text local to this reader alone for some cases.
	public int tempTextDelay = 0;
	public ReaderText currText = new ReaderText("ERROR", (byte)4);

	public TileEntityCardReader(){
		super();
	}

	public String doRead(@Nonnull ItemStack itemStack, EntityPlayer em, EnumFacing side) {
		IDCard.CardTag cardTag = new IDCard.CardTag(itemStack);
		//perform request to the global system
		int value = AdvBaseSecurity.instance.doorHandler.checkSwipe(cardTag.cardId, deviceId);
		//check values
		if(value == -4){
			setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.nodoor").getUnformattedText(), (byte)4), 20 * 3);
		}
		else if(value == -3){
			setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.nouser").getUnformattedText(), (byte)4), 20 * 3);
		}
		else if(value == -1){
			setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.blocked").getUnformattedText(), (byte)4), 20 * 3);
		}
		else if(value == 0){
			setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.denied").getUnformattedText(), (byte)4), 20 * 3);
		}
		else if(value == 400){
			setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.error").getUnformattedText(), (byte)4), 20 * 3);
		}
		return "access";
	}

	@Override
	public void setDoor(@Nonnull ItemStack heldItem) {
		ItemLinkingCard.CardTag cardTag = new ItemLinkingCard.CardTag(heldItem);
		if(cardTag.doorId != null){
			boolean found = AdvBaseSecurity.instance.doorHandler.SetDevID(deviceId, cardTag.doorId, false);
			if(found){
				int lightFlagT = AdvBaseSecurity.instance.doorHandler.getReaderLight(deviceId);
				ReaderText currTextT= AdvBaseSecurity.instance.doorHandler.getReaderLabel(deviceId);
				if(lightFlagT != lightFlag || !currTextT.text.equals(currText.text) || currTextT.color != currText.color) //determine if dirty
				{
					this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
					this.world.scheduleBlockUpdate(this.pos, this.world.getBlockState(this.pos).getBlock(),1,1);
					getUpdateTag();
					markDirty();
				}
			}
		}
	}

	public void setTempText(ReaderText text, int ticks){
		tempText = text;
		tempTextDelay = ticks;
		this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
		this.world.scheduleBlockUpdate(this.pos, this.world.getBlockState(this.pos).getBlock(),1,1);
		getUpdateTag();
		markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		AdvBaseSecurity.instance.logger.info("Starting Reader NBT Read: " + nbt.toString());
		if(nbt.hasUniqueId("deviceId"))
			this.deviceId = nbt.getUniqueId("deviceId");
		AdvBaseSecurity.instance.logger.info("Device ID r: " + deviceId);
		if(nbt.hasKey("temptext") && nbt.hasKey("tempcol")){
			this.tempText = new ReaderText(nbt.getString("temptext"), nbt.getByte("temptext"));
		}
		if(nbt.hasKey("temptick"))
			this.tempTextDelay = nbt.getInteger("temptick");
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
		AdvBaseSecurity.instance.logger.info("Ending Reader NBT Read");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		AdvBaseSecurity.instance.logger.info("Ending Reader NBT Write");
		if(this.deviceId != null)
			nbt.setUniqueId("deviceId", this.deviceId);
		if(this.tempText != null){
			nbt.setString("temptext", this.tempText.text);
			nbt.setByte("tempcol", this.tempText.color);
		}
		nbt.setInteger("temptick", this.tempTextDelay);
//		nbt.setInteger("", this.lightFlag);
//		if(this.currText != null)
//			nbt.setString("textlabel",this.currText.getFormattedText());
		AdvBaseSecurity.instance.logger.info("Ending Reader NBT Write: " + nbt.toString());
		return nbt;
	}

	@Override
	public void newId() {
		this.deviceId = UUID.randomUUID();
		markDirty();
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
		this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
		this.world.scheduleBlockUpdate(this.pos, this.world.getBlockState(this.pos).getBlock(),1,1);
		getUpdateTag();
		markDirty();
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

	@Override
	public void update() {
		if(!getWorld().isRemote) //make sure not client, since this isn't necessary then
			if(tempTextDelay > 0) {
				tempTextDelay--;
				if(tempTextDelay == 0){
					this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
					getUpdateTag();
				}
				markDirty();
			}
	}
}
