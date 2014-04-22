package labyrinth;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Sphere;
import texture.Texture;
import texture.TextureLoader;


/**
 * Třída starající se o vykreslování objektů
 * @author og
 */
public class Models {
	
	/** Načítač textur */
	private static TextureLoader textureLoader;
	
	/** Textury */
	private static Texture wallSidesTex, wallTopTex, floorTex, fireTex,
			snowTex;
	
	/** Identifikace GL call-listů */
	private static int wallSidesId, wallTopId, floorId, playerId, enemyId,
			goalId;
	
	/** Exploze */
	private static Explosion explosion;

	
	/**
	 * Vykreslí stranu jednotkové krychle s texturou o daných rozměrech
	 * @param w Šířka textury
	 * @param h Výška textury
	 */
	private static void side(float w, float h) {
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glNormal3i(0, -1, 0);
		
		// rozkouskování pro lepší vykreslování s osvětlením
		float X = 5, Y = 5;
		for (int x = 0; x < X; x++) {
			for (int y = 0; y < Y; y++) {
				GL11.glTexCoord2f(0 + x * w / X, 0 + y * h / Y);
				GL11.glVertex3d(-0.5 + x / X, -0.5, -0.5 + y / Y);
				GL11.glTexCoord2f(0 + (x + 1) * w / X, 0 + y * h / Y);
				GL11.glVertex3d(-0.5 + (x + 1) / X, -0.5, -0.5 + y / Y);
				GL11.glTexCoord2f(0 + (x + 1) * w / X, 0 + (y + 1) * h / Y);
				GL11.glVertex3d(-0.5 + (x + 1) / X, -0.5, -0.5 + (y + 1) / Y);
				GL11.glTexCoord2f(0 + x * w / X, 0 + (y + 1) * h / Y);
				GL11.glVertex3d(-0.5 + x / X, -0.5, -0.5 + (y + 1) / Y);
			}
		}
		
		GL11.glEnd();
	}
	
	
	/**
	 * Určí barvu materiálu
	 * @param r Červená složka
	 * @param g Zelená složka
	 * @param b Modrá složka
	 */
	private static void color(double r, double g, double b) {
		/*
		FloatBuffer light = BufferUtils.createFloatBuffer(4).put(
				new float[] {(float)r, (float)g, (float)b, 1});
		light.flip();
		GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR,
				light);
		*/
		GL11.glColor3d(r, g, b);
	}
	
	
	/**
	 * Vykreslí boční strany krychle
	 * @param w Šířka textury
	 * @param h Výška textury
	 */
	private static void cubeSides(float w, float h) {
		GL11.glPushMatrix();
		color(1, 0, 0);
		side(w, h);
		GL11.glRotatef(90, 0, 0, 1);
		color(0, 1, 0);
		side(w, h);
		GL11.glRotatef(90, 0, 0, 1);
		color(0, 0, 1);
		side(w, h);
		GL11.glRotatef(90, 0, 0, 1);
		color(0.3f, 0, 0.4f);
		side(w, h);
		GL11.glPopMatrix();
	}
	
	
	/**
	 * Vykreslí horní stranu krychle
	 * @param w Šířka textury
	 * @param h Výška textury
	 */
	private static void cubeTop(float w, float h) {
		GL11.glPushMatrix();
		GL11.glRotatef(-90, 1, 0, 0);
		color(1, 1, 1);
		side(w, h);
		GL11.glPopMatrix();
	}
	
	
	/**
	 * Vykreslí jeden dílek podlahy
	 * @param w Šířka textury
	 * @param h Výška textury
	 */
	private static void floor(float w, float h) {
		GL11.glPushMatrix();
		GL11.glRotatef(-90, 1, 0, 0);
		GL11.glTranslatef(0, 0.5f, 0);
		color(0.9f, 0.3f, 0.1f);
		side(w, h);
		GL11.glPopMatrix();
	}
	
	
	/**
	 * Vykreslí hráče
	 */
	private static void player() {
		color(0.1f, 0.3f, 0.9f);
		(new Sphere()).draw(0.5f, 10, 10);
	}
	
	
	/**
	 * Vykreslí nepřítele
	 */
	private static void enemy() {
		color(1, 0, 0);
		(new Sphere()).draw(0.5f, 10, 10);
	}
	
	
	/**
	 * Vykreslí cíl
	 */
	private static void goal() {
		color(1, 1, 1);
		(new Cylinder()).draw(1, 0, 1, 10, 10);
	}
	
	
	/**
	 * Inicializace
	 * @throws IOException Chyba načítání textur
	 */
	public static void init() throws IOException {
		textureLoader = new TextureLoader();
		
		// načtení textur
		wallSidesTex = textureLoader.getTexture("resource/floor.jpg");
		wallTopTex = wallSidesTex;
		floorTex = wallSidesTex;
		fireTex = textureLoader.getTexture("resource/fire.jpg");
		snowTex = textureLoader.getTexture("resource/snow.jpg");
		
		// vygenerování GL call-listů:
		
		// pro boční strany krychle
		wallSidesId = GL11.glGenLists(1);
		GL11.glNewList(wallSidesId, GL11.GL_COMPILE);
		cubeSides(wallSidesTex.getWidth(), wallSidesTex.getHeight());
		GL11.glEndList();
		
		// pro horní stranu krychle
		wallTopId = GL11.glGenLists(1);
		GL11.glNewList(wallTopId, GL11.GL_COMPILE);
		cubeTop(wallTopTex.getWidth(), wallTopTex.getHeight());
		GL11.glEndList();
		
		// pro dílek podlahy
		floorId = GL11.glGenLists(1);
		GL11.glNewList(floorId, GL11.GL_COMPILE);
		floor(floorTex.getWidth(), floorTex.getHeight());
		GL11.glEndList();
		
		// pro hráče
		playerId = GL11.glGenLists(1);
		GL11.glNewList(playerId, GL11.GL_COMPILE);
		player();
		GL11.glEndList();
		
		// pro nepřítele
		enemyId = GL11.glGenLists(1);
		GL11.glNewList(enemyId, GL11.GL_COMPILE);
		enemy();
		GL11.glEndList();
		
		// pro cíl
		goalId = GL11.glGenLists(1);
		GL11.glNewList(goalId, GL11.GL_COMPILE);
		goal();
		GL11.glEndList();
	}
	
	
	/**
	 * Vykreslí krychli
	 */
	private static void renderWall() {
		wallSidesTex.bind();
		GL11.glCallList(wallSidesId);
		
		wallTopTex.bind();
		GL11.glCallList(wallTopId);
	}
	
	
	/**
	 * Vykreslí dílek podlahy
	 */
	private static void renderFloor() {
		floorTex.bind();
		GL11.glCallList(floorId);
	}
	

	/**
	 * Vykreslí celé bludiště (bez postav)
	 * @param lab Bludiště
	 */
	public static void renderLabyrinth(StringBuffer[] lab) {
		// každý řádek
		for (int row = 0; row < lab.length; row++)
			// každý sloupec uvnitř řádku
			for (int col = 0; col < lab[row].length(); col++)
			{
				GL11.glPushMatrix();
				// přesun na danou pozici
				GL11.glTranslatef(col, row, 0);
				// vykreslení krychle stěny bludiště,
				if (lab[row].charAt(col) == 'X')
					renderWall();
				// případně podlahy
				renderFloor();
				GL11.glPopMatrix();
			}
	}
	
	
	/**
	 * Vykreslí hráče
	 * @param row Řádková souřadnice
	 * @param col Sloupcová souřadnice
	 */
	public static void renderPlayer(float row, float col) {
		GL11.glPushMatrix();
		GL11.glTranslatef(col, row, 0);
		GL11.glCallList(playerId);
		GL11.glPopMatrix();
	}
	
	
	/**
	 * Vykreslí nepřítele
	 * @param row Řádková souřadnice
	 * @param col Sloupcová souřadnice
	 */
	public static void renderEnemy(float row, float col) {
		GL11.glPushMatrix();
		GL11.glTranslatef(col, row, 0);
		GL11.glCallList(enemyId);
		GL11.glPopMatrix();
	}
	
	
	/**
	 * Vykreslí cíl
	 * @param row Řádková souřadnice
	 * @param col Sloupcová souřadnice
	 */
	public static void renderGoal(float row, float col) {
		GL11.glPushMatrix();
		GL11.glTranslatef(col, row, 0);
		GL11.glCallList(goalId);
		GL11.glPopMatrix();
	}
	
	
	/**
	 * Resetuje explozi 
	 */
	public static void initExplosion() {
		explosion = new Explosion();
	}
	
	
	/**
	 * Vykreslí explozi na daných souřadnicích
	 * @param row Řádková souřadnice
	 * @param col Sloupcová souřadnice
	 * @param fire Textura ohně?
	 */
	public static void renderExplosion(float row, float col, boolean fire) {
		GL11.glPushMatrix();
		GL11.glTranslatef(col, row, 0);
		GL11.glRotatef(-90, 0, 0, 1);
		if (fire) {
			fireTex.bind();
			explosion.render(fireTex.getWidth(), fireTex.getHeight());
		}
		else {
			snowTex.bind();
			explosion.render(snowTex.getWidth(), snowTex.getHeight());
		}
		GL11.glPopMatrix();
	}
	
}
