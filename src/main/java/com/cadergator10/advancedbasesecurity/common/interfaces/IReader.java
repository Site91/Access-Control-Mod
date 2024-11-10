package com.cadergator10.advancedbasesecurity.common.interfaces;

import com.cadergator10.advancedbasesecurity.util.ReaderText;
import net.minecraft.util.text.TextComponentString;

public interface IReader extends IDevice{
    public void updateVisuals(int light, ReaderText str);
}
