package com.cadergator10.advancedbasesecurity.client.renderer;

import com.cadergator10.advancedbasesecurity.common.tileentity.TileEntityCardReader;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

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

public class RenderCardReader  extends TileEntitySpecialRenderer<TileEntityCardReader> {

	static float texPixel=1.0f/16f;
	static float uvWide = 1.0f/24f; //to ensure width is correct
	static float uvGen = uvWide * 8f;

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

	public RenderCardReader()
	{
		super();
	}

	@Override
	public void render(TileEntityCardReader tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
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

		this.bindTexture(new ResourceLocation("advancedbasesecurity", "textures/block/card_reader_front.png"));
		GlStateManager.scale(1.001, 1.001, 1.001); //just a dirty fix to avoid tiny gaps between keypad and blocks next to it
		drawBlock(tileEntity, time);

		GlStateManager.popMatrix();
	}

	private void transferSide(BufferBuilder vertexbuffer, float slide){
		//card swipe rightside
		vertexbuffer.pos(texPixel * (4+slide), texPixel * 4,    texPixel).tex(1f,          texPixel * 2f).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * (4+slide), texPixel * 11, texPixel).tex(1f,          texPixel * 9f).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * (4f+slide), texPixel * 11, 0f-texPixel      ).tex(1f-uvWide, texPixel * 9f).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * (4f+slide), texPixel * 4,    0f-texPixel      ).tex(1f-uvWide, texPixel * 2f).normal(-1f,0f,0f).endVertex();
		//card swipe leftside
		vertexbuffer.pos(texPixel * (5+slide), texPixel * 11,    texPixel).tex(1f-uvWide*2f,          texPixel * 2f).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * (5+slide), texPixel * 4, texPixel).tex(1f-uvWide*2f,          texPixel * 9f).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * (5f+slide), texPixel * 4, 0f-texPixel      ).tex(1f-uvWide, texPixel * 9f).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel * (5f+slide), texPixel * 11,    0f-texPixel      ).tex(1f-uvWide, texPixel * 2f).normal(1f,0f,0f).endVertex();
		//card swipe bottom
		vertexbuffer.pos(texPixel * (5+slide), texPixel * 4,    texPixel).tex(1f,          0f   ).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(texPixel * (4f+slide), texPixel * 4, texPixel).tex(1f-uvWide,          0f).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(texPixel * (4f+slide), texPixel * 4, 0f-texPixel      ).tex(1f-uvWide, texPixel * 2f).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(texPixel * (5+slide), texPixel * 4,    0f-texPixel      ).tex(1f, texPixel * 2f).normal(0f,1f,0f).endVertex();
		//card swipe top
		vertexbuffer.pos(texPixel * (4+slide), texPixel * 11,    texPixel).tex(1f-uvWide,          0f).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(texPixel * (5f+slide), texPixel * 11, texPixel).tex(1f-uvWide*2f,          0f).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(texPixel * (5f+slide), texPixel * 11, 0f-texPixel      ).tex(1f-uvWide*2f, texPixel * 2f).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(texPixel * (4+slide), texPixel * 11,    0f-texPixel      ).tex(1f-uvWide, texPixel * 2f).normal(0f,-1f,0f).endVertex();
		//card swipe face
		vertexbuffer.pos(texPixel * (4+slide), texPixel * 4,    0f-texPixel).tex(1f,          texPixel * 9f).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * (4f+slide), texPixel * 11, 0f-texPixel).tex(1f,          1f).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * (5f+slide), texPixel * 11, 0f-texPixel      ).tex(1f-uvWide, 1f).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * (5f+slide), texPixel * 4,    0f-texPixel      ).tex(1f-uvWide, texPixel * 9f ).normal(0f,0f,-1f).endVertex();
	}

	private void transferLight(BufferBuilder vertexbuffer, float flag){
		vertexbuffer.pos(texPixel * 5f, texPixel,    0f).tex(1f - uvWide * 8f, texPixel * flag).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * 5f, texPixel * 2f, 0f).tex(1f - uvWide * 8f,          texPixel * (flag + 1f)).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * 11f, texPixel * 2f, 0f ).tex(1f - uvWide * 2f, texPixel * (flag + 1f)).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel * 11f, texPixel,    0f).tex(1f - uvWide * 2f, texPixel * flag ).normal(0f,0f,-1f).endVertex();
	}

	public void drawBlock(TileEntityCardReader card, long time) {
		Tessellator tessellator=Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
//commented out all insets since I don't think its all needed for what I am doing. I just need the text displayed
		vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL); //tessellator.startDrawingQuads();
		//inset face
		vertexbuffer.pos(texPixel,    texPixel,    texPixel).tex(uvWide,    1f-texPixel).normal(0f, 0f, -1f).endVertex();
		vertexbuffer.pos(texPixel,    texPixel*15f, texPixel).tex(uvWide,    texPixel   ).normal(0f, 0f, -1f).endVertex();
		vertexbuffer.pos(1f-texPixel, texPixel*15f, texPixel).tex(1f-uvGen-uvWide, texPixel   ).normal(0f, 0f, -1f).endVertex();
		vertexbuffer.pos(1f-texPixel, texPixel,    texPixel).tex(1f-uvGen-uvWide, 1f-texPixel).normal(0f, 0f, -1f).endVertex();
		//bottom lip front
		vertexbuffer.pos(0f,          0f,          0f).tex(0f,          0f         ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel,    texPixel,    0f).tex(uvWide,    texPixel   ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(1f-texPixel, texPixel,    0f).tex(1f-uvGen-uvWide, texPixel   ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(1f,          0f,          0f).tex(1f-uvGen,          0f         ).normal(0f,0f,-1f).endVertex();
		//top lip front
		vertexbuffer.pos(texPixel,    1f-texPixel, 0f).tex(uvWide,    1f-texPixel).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(0f,          1f,          0f).tex(0f,          1f         ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(1f,          1f,          0f).tex(1f-uvGen,          1f         ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(1f-texPixel, 1f-texPixel, 0f).tex(1f-uvGen-uvWide, 1f-texPixel).normal(0f,0f,-1f).endVertex();
		//right lip front
		vertexbuffer.pos(0f,          0f,          0f).tex(0f,          0f         ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(0f,          1f,          0f).tex(0f,          1f         ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel,    1f-texPixel, 0f).tex(uvWide,    1f-texPixel).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(texPixel,    texPixel,    0f).tex(uvWide,    texPixel   ).normal(0f,0f,-1f).endVertex();
		//left lip front
		vertexbuffer.pos(1f-texPixel, texPixel,    0f).tex(1f-uvGen-uvWide, texPixel   ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(1f-texPixel, 1f-texPixel, 0f).tex(1f-uvGen-uvWide, 1f-texPixel).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(1f,          1f,          0f).tex(1f-uvGen,          1f         ).normal(0f,0f,-1f).endVertex();
		vertexbuffer.pos(1f,          0f,          0f).tex(1f-uvGen,          0f         ).normal(0f,0f,-1f).endVertex();

		//bottom lip inside
		vertexbuffer.pos(texPixel,    texPixel,    0f      ).tex(uvWide,    1f         ).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(texPixel,    texPixel,    texPixel).tex(uvWide,    1f-texPixel).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(1f-texPixel, texPixel,    texPixel).tex(1f-uvGen-uvWide, 1f-texPixel).normal(0f,1f,0f).endVertex();
		vertexbuffer.pos(1f-texPixel, texPixel,    0f      ).tex(1f-uvGen-uvWide, 1f         ).normal(0f,1f,0f).endVertex();
		//top lip inside
		vertexbuffer.pos(texPixel,    1f-texPixel, texPixel).tex(uvWide,    texPixel).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(texPixel,    1f-texPixel, 0f      ).tex(uvWide,    0f      ).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(1f-texPixel, 1f-texPixel, 0f      ).tex(1f-uvGen-uvWide, 0f      ).normal(0f,-1f,0f).endVertex();
		vertexbuffer.pos(1f-texPixel, 1f-texPixel, texPixel).tex(1f-uvGen-uvWide, texPixel).normal(0f,-1f,0f).endVertex();
		//right lip inside
		vertexbuffer.pos(texPixel,    texPixel,    0f      ).tex(1f-uvGen-uvWide, texPixel   ).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel,    1f-texPixel, 0f      ).tex(1f-uvGen-uvWide, 1f-texPixel).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel,    1f-texPixel, texPixel).tex(1f-uvGen,          1f-texPixel).normal(1f,0f,0f).endVertex();
		vertexbuffer.pos(texPixel,    texPixel,    texPixel).tex(1f-uvGen,          texPixel   ).normal(1f,0f,0f).endVertex();
		//left lip inside
		vertexbuffer.pos(1f-texPixel, texPixel,    texPixel).tex(1f-uvGen,          texPixel   ).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(1f-texPixel, 1f-texPixel, texPixel).tex(1f-uvGen,          1f-texPixel).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(1f-texPixel, 1f-texPixel, 0f      ).tex(1f-uvGen-uvWide, 1f-texPixel).normal(-1f,0f,0f).endVertex();
		vertexbuffer.pos(1f-texPixel, texPixel,    0f      ).tex(1f-uvGen-uvWide, texPixel   ).normal(-1f,0f,0f).endVertex();
		//card swiper
		transferSide(vertexbuffer, 0);
		transferSide(vertexbuffer, 1.1f);
		transferLight(vertexbuffer, card != null ? card.lightFlag : 0);

		tessellator.draw();

		//vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL); //tessellator.startDrawingQuads();
		//tessellator.setBrightness(255);

		FontRenderer font=this.getFontRenderer();
		if (font!=null)
		{
			String fbText = card!=null && card.currText!=null ? card.currText.text : "";
			byte fbColor = card!=null && card.currText!=null ? card.currText.color : 7;

			if (fbText!=null && fbText.length()>0)
				writeLabel(font, texPixel, display, fbColor, fbText);
		}
	}
}
