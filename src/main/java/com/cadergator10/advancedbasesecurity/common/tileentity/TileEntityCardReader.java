package com.cadergator10.advancedbasesecurity.common.tileentity;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.interfaces.IReader;
import com.cadergator10.advancedbasesecurity.common.items.SwipeCard;
import com.cadergator10.advancedbasesecurity.util.ReaderText;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;

/**
 * Readers that scan the cards of players. Get the data off this card and send to the DoorHandler.
 * Other readers need to extend this class as they have a different specialrenderer requiring a new tileentity.
 * These card readers extend this class:
 * @see TileEntityCardReaderSmall
 * @see TileEntityCardReaderDouble
 */
public class TileEntityCardReader extends TileEntityDeviceBase implements IReader, ITickable {
	public int lightFlag = 0; //The light that should be shown on the bar. Binary counter: 1 = RED, 2 = YELLOW, 4 = GREEN
	public ReaderText tempText = new ReaderText("ERROR", (byte)4); //text local to this reader alone for some cases. Disappears after a timer usually.
	public int tempTextDelay = 0; //Delay to show tempText
	public ReaderText currText = new ReaderText("ERROR", (byte)4); //Current text to display on the reader. tempText has priority when tempTextDelay above 0
	public int tempLightFlag; //Temporary flag with priority over lightFlag. Same rules for light bar. Same rules when it comes to the delay.

	public TileEntityCardReader(){
		super();
	}

	/**
	 * When the card is swiped, this is called.
	 * @param itemStack the card item.
	 * @param em the player
	 * @param side the side clicked
	 * @return technically never used. Just returns "access"
	 */
	public String readCard(@Nonnull ItemStack itemStack, EntityPlayer em, EnumFacing side) {
		AdvBaseSecurity.LogDebug("Reader with deviceID " + deviceId + " clicked with card.");
		if (door != null){
			SwipeCard.CardTag cardTag = new SwipeCard.CardTag(itemStack);
			if(cardTag.cardId == null)
			{
				setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.nouser").getUnformattedText(), (byte) 4), 20 * 3, 3);
				return "access";
			}
			//perform request to the global system
			int value = door.checkSwipe(cardTag.cardId.DoorID, deviceId, em, true);
			AdvBaseSecurity.instance.logger.debug("Received value of " + value);
			//check values
			if (value == -4) {
				setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.nodoor").getUnformattedText(), (byte) 4), 20 * 3, 3);
			} else if (value == -3) {
				setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.nouser").getUnformattedText(), (byte) 4), 20 * 3, 3);
			} else if (value == -1) {
				setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.blocked").getUnformattedText(), (byte) 4), 20 * 3, 1);
			} else if (value == 0) {
				setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.denied").getUnformattedText(), (byte) 4), 20 * 3, 1);
			} else if (value == 400) {
				setTempText(new ReaderText(new TextComponentTranslation("advancedbasesecurity.reader.text.error").getUnformattedText(), (byte) 4), 20 * 3, 7);
			}
		}
		else
			AdvBaseSecurity.LogDebug("no door has been set on reader with deviceID " + deviceId);
		return "access";
	}

	@Override
	public void setDoor(@Nonnull ItemStack heldItem) {
		super.setDoor(heldItem);
		if(door != null) {
			int lightFlagT = door.getReaderLight(deviceId);
			ReaderText currTextT = door.getReaderLabel(deviceId);
			if (lightFlagT != lightFlag || !currTextT.text.equals(currText.text) || currTextT.color != currText.color) //determine if dirty
			{
				this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
				this.world.scheduleBlockUpdate(this.pos, this.world.getBlockState(this.pos).getBlock(), 1, 1);
				getUpdateTag();
			}
		}
	}

	public void setTempText(ReaderText text, int ticks, int flag){
		AdvBaseSecurity.LogDebug("Temporary Text of Reader " + deviceId + " being changed from " + tempText + " to " + text + " | also lightbar from " + tempLightFlag + " to " + flag);
		tempText = text;
		tempTextDelay = ticks;
		tempLightFlag = flag;
		this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
		this.world.scheduleBlockUpdate(this.pos, this.world.getBlockState(this.pos).getBlock(),1,1);
		getUpdateTag();
		markDirty();
	}

	@Override
	public void onPlace() {
		//check if in list
		if (!AdvBaseSecurity.instance.doorHandler.allReaders.containsKey(this.deviceId))
			AdvBaseSecurity.instance.doorHandler.allReaders.put(this.deviceId, this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(nbt.hasKey("temptext") && nbt.hasKey("tempcol")){
			this.tempText = new ReaderText(nbt.getString("temptext"), nbt.getByte("tempcol"));
		}
		if(nbt.hasKey("temptick"))
			this.tempTextDelay = nbt.getInteger("temptick");
		if(nbt.hasKey("tempflag"))
			this.tempLightFlag = nbt.getInteger("tempflag");
		else
			this.tempLightFlag = 0;
		if(!nbt.hasKey("toclient") || !nbt.getBoolean("toclient")) {
			if(door != null) {
				lightFlag = door.getReaderLight(deviceId);
				currText = door.getReaderLabel(deviceId);
			}
			//check if in list
		}
		else{
			if(nbt.hasKey("lightFlag"))
				lightFlag = nbt.getInteger("lightFlag");
			else
				lightFlag = 7;
			if(nbt.hasKey("currText")){
				NBTTagCompound tag = nbt.getCompoundTag("currText");
				if(tag.hasKey("text") && tag.hasKey("color"))
					currText = new ReaderText(tag.getString("text"), tag.getByte("color"));
				else
					currText = new ReaderText("TAG ERROR", (byte)4);
			}
			else
				currText = new ReaderText("PACKET ERROR", (byte)4);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if(this.tempText != null){
			nbt.setString("temptext", this.tempText.text);
			nbt.setByte("tempcol", this.tempText.color);
		}
		nbt.setInteger("tempflag", this.tempLightFlag);
		nbt.setInteger("temptick", this.tempTextDelay);
//		nbt.setInteger("", this.lightFlag);
//		if(this.currText != null)
//			nbt.setString("textlabel",this.currText.getFormattedText());
		return nbt;
	}

	@Override
	public NBTTagCompound pushMoretoUpdate(NBTTagCompound nbt) {
		nbt.setBoolean("toclient", true);
		nbt.setInteger("lightFlag", lightFlag);
		NBTTagCompound tag = new NBTTagCompound();
		if(currText != null && currText.text != null){
			tag.setString("text", currText.text);
			tag.setByte("color", currText.color);
		}
		else{
			tag.setString("text", "NULL ERROR");
			tag.setByte("color", (byte)4);
		}
		nbt.setTag("currText", tag);
		return nbt;
	}

	@Override
	public String getDevType() {
		return "reader";
	}

	@Override
	public void updateVisuals(int light, ReaderText str) { //When the base visuals need to be changed.
		AdvBaseSecurity.LogDebug("Main Visuals of Reader " + deviceId + " being changed from " + currText + " to " + str + " | also lightbar from " + lightFlag + " to " + light);
		lightFlag = light;
		currText = str;
		this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
		this.world.scheduleBlockUpdate(this.pos, this.world.getBlockState(this.pos).getBlock(),1,1);
		getUpdateTag();
		markDirty();
	}

	public IBlockState getFacing() //get dir the block is facing as a blockstate
	{
		return world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()));
	}

	public float getAngle() //Convert getFacing to a float
	{
		//this was getFacing()
		return getFacing().getBlock().getMetaFromState(getFacing()) * 270;
	}

	@Override
	public void update() { //every tick check the temporary text on the reader timer. Update blockstate if reaching 0 (if not already 0)
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
