package com.cadergator10.advancedbasesecurity.client.renderer;

import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReader;
import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReaderSmall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

//Pretty much just handles the light bar. Simple really
public class RenderCardReaderSmall extends TileEntitySpecialRenderer<TileEntityCardReaderSmall> {

	static float texPixel=1.0f/16f;
	
	static ButtonPosition display = null;
	static {
		display = new ButtonPosition( 2f, 13f, 14f, 2f);
	}

	static class ButtonPosition
	{
		public float x, y;
		public float w, h;

		ButtonPosition(float x, float y, float w, float h)
		{
			this.x=x;
			this.y=y;
			this.w=w;
			this.h=h;
		}
	}

	public static void writeLabel(FontRenderer font, float depth, ButtonPosition pos, int color, String label)
	{
		//OpenSecurity.logger.info(label);
		float x=pos.x*texPixel;
		float y=pos.y*texPixel;
		float w=pos.w*texPixel;
		float h=pos.h*texPixel;

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		//GL11.glEnable(GL11.GL_BLEND);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTranslatef(x+w/2f, y+h/2f, depth+texPixel*-.07f);
		int labelW=font.getStringWidth(label);
		float scale=Math.min(h/10F, 0.8F*w/labelW);
		GL11.glScalef(-scale,-scale,scale);
		GL11.glTranslatef(.5f,.5f,0f);
		GL11.glDepthMask(false);
		int argb = 0xFF000000;
		if((color&4)!=0) argb|=0xFF0000;
		if((color&2)!=0) argb|=0xFF00;
		if((color&1)!=0) argb|=0xFF;
		font.drawString(label, -labelW/2, -4, argb);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	public RenderCardReaderSmall()
	{
		super();
	}

	@Override
	public void render(TileEntityCardReaderSmall tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(.5f, 0, .5f);
		GlStateManager.rotate(tileEntity.getAngle(), 0f, 1f, 0f);
		GlStateManager.translate(-.5f, 0, -.5f);

		IBlockState state = tileEntity.getWorld().getBlockState(tileEntity.getPos());
		EnumFacing facing = EnumFacing.byHorizontalIndex(state.getBlock().getMetaFromState(state));
		int li = tileEntity.getWorld().getCombinedLight(tileEntity.getPos().offset(facing.getOpposite()), 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, li % 65536, li / 65536);

		long time = tileEntity.getWorld().getTotalWorldTime();

		this.bindTexture(new ResourceLocation("advancedbasesecurity", "textures/block/card_reader_small.png"));
		GlStateManager.scale(1.001, 1.001, 1.001); //just a dirty fix to avoid tiny gaps between keypad and blocks next to it
		drawBlock(tileEntity, time);

		GlStateManager.popMatrix();
	}

	private void transferLight(BufferBuilder vertexbuffer, float flag){
		//front
		vertexbuffer.pos(texPixel * 9f, texPixel * 6.5f,    texPixel * 14.5f).tex(1f - (texPixel * flag), 1f).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * 9f, texPixel * 9.5f, texPixel * 14.5f).tex(1f - (texPixel * flag), 1f - (texPixel * 3f)).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 9.5f, texPixel * 14.5f ).tex(1f - (texPixel * (flag + 1f)), 1f - (texPixel * 3f)).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 6.5f,    texPixel * 14.5f).tex(1f - (texPixel * (flag + 1f)), 1f ).normal(0f,0f,-1f).endVertex();
		//top
		vertexbuffer.pos(texPixel * 9f, texPixel * 9.5f,    texPixel * 14.5f).tex(1f - (texPixel * flag), 1f - (texPixel * 2f)).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(texPixel * 9f, texPixel * 9.5f, texPixel * 15f).tex(1f - (texPixel * flag),          1f - (texPixel * 3f)).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 9.5f, texPixel * 15f ).tex(1f - (texPixel * (flag + 1f)), 1f - (texPixel * 3f)).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 9.5f,    texPixel * 14.5f).tex(1f - (texPixel * (flag + 1f)), 1f - (texPixel * 2f) ).normal(0f,-1f,0f).endVertex();
		//bottom
		vertexbuffer.pos(texPixel * 9f, texPixel * 6.5f,    texPixel * 15f).tex(1f - (texPixel * (flag + 1f)), 1f).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(texPixel * 9f, texPixel * 6.5f, texPixel * 14.5f).tex(1f - (texPixel * (flag + 1f)),          1f - texPixel).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 6.5f, texPixel * 14.5f ).tex(1f - (texPixel * flag), 1f - texPixel).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 6.5f,    texPixel * 15f).tex(1f - (texPixel * flag), 1f).normal(0f,1f,0f).endVertex();
		//left
		vertexbuffer.pos(texPixel * 10f, texPixel * 6.5f,    texPixel * 14.5f).tex(1f - (texPixel * (flag + 1f)), 1f).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 9.5f, texPixel * 14.5f).tex(1f - (texPixel * (flag + 1f)),          1f - (texPixel * 3f)).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 9.5f, texPixel * 15f).tex(1f - (texPixel * flag), 1f - (texPixel * 3f)).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * 10f, texPixel * 6.5f,    texPixel * 15f).tex(1f - (texPixel * flag), 1f ).normal(1f,0f,0f).endVertex();
		//right
		vertexbuffer.pos(texPixel * 9f, texPixel * 6.5f,    texPixel * 15f).tex(1f - (texPixel * flag), 1f).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * 9f, texPixel * 9.5f, texPixel * 15f).tex(1f - (texPixel * flag),          1f - (texPixel * 3f)).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * 9f, texPixel * 9.5f, texPixel * 14.5f).tex(1f - (texPixel * (flag + 1f)), 1f - (texPixel * 3f)).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * 9f, texPixel * 6.5f,    texPixel * 14.5f).tex(1f - (texPixel * (flag + 1f)), 1f ).normal(-1f,0f,0f).endVertex();
	}

	public void drawBlock(TileEntityCardReader card, long time) {
		Tessellator tessellator=Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
//commented out all insets since I don't think its all needed for what I am doing. I just need the text displayed
		vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL); //tessellator.startDrawingQuads();
		transferLight(vertexbuffer, card != null ? (card.tempTextDelay > 0 ? card.tempLightFlag : card.lightFlag) : 0);

		tessellator.draw();

		//vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL); //tessellator.startDrawingQuads();
		//tessellator.setBrightness(255);

//		FontRenderer font=this.getFontRenderer();
//		if (font!=null)
//		{
//			String fbText = card!=null ? (card.tempTextDelay > 0 && card.tempText!=null ? card.tempText.text : (card.currText!=null ? card.currText.text : "")) : "";
//			byte fbColor = card!=null ? (card.tempTextDelay > 0 && card.tempText!=null ? card.tempText.color : (card.currText!=null ? card.currText.color : 7)) : 7;
//
//			if (fbText!=null && fbText.length()>0)
//				writeLabel(font, texPixel, display, fbColor, fbText);
//		}
	}
}
