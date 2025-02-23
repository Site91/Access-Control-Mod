package com.cadergator10.advancedbasesecurity.common.blocks;

import com.cadergator10.advancedbasesecurity.AdvBaseSecurity;
import com.cadergator10.advancedbasesecurity.common.ContentRegistry;
import com.cadergator10.advancedbasesecurity.common.interfaces.IDoorControl;
import com.cadergator10.advancedbasesecurity.common.items.ItemDoorManager;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityDoorControlRedstone;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockDoorRedstone extends Block implements ITileEntityProvider {
    public static final String NAME = "redstone_control";
    public static Block DEFAULTITEM;
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockDoorRedstone() {
        super(Material.IRON);
        setDefaultState(blockState.getBaseState().withProperty(POWERED, false));
        setTranslationKey("advancedbasesecurity." + NAME);
        setRegistryName(AdvBaseSecurity.MODID, NAME);
        setHardness(0.5f);
        setCreativeTab(ContentRegistry.CREATIVETAB);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntityDoorControlRedstone tile = (TileEntityDoorControlRedstone) worldIn.getTileEntity(pos);
        if(!worldIn.isRemote) {
            tile.newId();
            if (!AdvBaseSecurity.instance.doorHandler.allDoorControllers.containsKey(tile.getId()))
                AdvBaseSecurity.instance.doorHandler.allDoorControllers.put(tile.getId(), tile);
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if(!worldIn.isRemote) {
            IDoorControl te = (IDoorControl) worldIn.getTileEntity(pos);
            AdvBaseSecurity.instance.doorHandler.allDoorControllers.remove(te.getId());
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityDoorControlRedstone();
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return getStrongPower(state, world, pos, side);
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (state.getValue(POWERED))
            return 15;
        else
            return 0;
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(POWERED) ? 0:1;
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(POWERED, meta == 1);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {
                POWERED
        });
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        TileEntityDoorControlRedstone tile = (TileEntityDoorControlRedstone) worldIn.getTileEntity(pos);
        worldIn.setBlockState(pos, state.withProperty(POWERED, tile.isPowered()));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem;
        if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemDoorManager) {
            heldItem = player.getHeldItemMainhand();
        } else if (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemDoorManager) {
            heldItem = player.getHeldItemOffhand();
        } else {
            return false;
        }

        if (!heldItem.isEmpty()) {
            Item equipped = heldItem.getItem();
            TileEntityDoorControlRedstone tile = (TileEntityDoorControlRedstone) world.getTileEntity(pos);

            if (!world.isRemote) {
                if(equipped instanceof ItemDoorManager)
                    tile.setDoor(heldItem);
            }
            return true;
        }
        return false;
    }
}
