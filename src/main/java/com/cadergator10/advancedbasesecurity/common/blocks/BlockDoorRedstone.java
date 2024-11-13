package com.cadergator10.advancedbasesecurity.common.blocks;

import baubles.api.BaublesApi;
import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.items.IDCard;
import com.cadergator10.advancedbasesecurity.common.items.ItemLinkingCard;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReader;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityDoorRedstone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockDoorRedstone extends Block implements ITileEntityProvider {
    public static final String NAME = "redstone_control";
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockDoorRedstone() {
        super(Material.IRON);
        setDefaultState(blockState.getBaseState().withProperty(POWERED, false));
        setTranslationKey("advancedbasesecurity." + NAME);
        setRegistryName(AdvBaseSecurity.MODID, NAME);
        setHardness(0.5f);
        setCreativeTab(ContentRegistry.CREATIVETAB);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityDoorRedstone();
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (state.getValue(POWERED))
            return 15;
        else
            return 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (state.getValue(POWERED))
            return 15;
        else
            return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem;
        if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard) {
            heldItem = player.getHeldItemMainhand();
        } else if (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemLinkingCard) {
            heldItem = player.getHeldItemOffhand();
        } else {
            return false;
        }

        if (!heldItem.isEmpty()) {
            System.out.println(heldItem.getItem().getRegistryName().toString());
            Item equipped = heldItem.getItem();
            TileEntityDoorRedstone tile = (TileEntityDoorRedstone) world.getTileEntity(pos);

            if (!world.isRemote) {
                if(equipped instanceof ItemLinkingCard)
                    tile.setDoor(heldItem);
            }
            return true;
        }
        return false;
    }
}
