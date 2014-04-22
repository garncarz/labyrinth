package labyrinth;

import org.lwjgl.opengl.GL11;

/**
 * Třída vykreslující explozi,
 * podle http://nehe.ceske-hry.cz/tut_19.php
 */
public class Explosion {

	/** Počet částic exploze */
	private int MAX_PARTICLES = 1000;
	
	/** Zpomalení částic */
	private float slowDown = 0.2f;
	
	/** Zoom */
	private float zoom = -0.5f;
	
	/** Vlastnosti částice */
	private class Particle {
		/** Aktivní? */
		boolean active;
		
		/** Zbývající život */
		float life;
		
		/** Rychlost stárnutí */
		float fade;
		
		/** Červená složka barvy */
		float r;
		
		/** Zelená složka barvy */
		float g;
		
		/** Modrá složka barvy */
		float b;
		
		/** X pozice */
		float x;
		
		/** Y pozice */
		float y;
		
		/** Z pozice */
		float z;
		
		/** X směr a rychlost */
		float xi;
		
		/** Y směr a rychlost */
		float yi;
		
		/** Z směr a rychlost */
		float zi;
		
		/** Polovina délky */
		float l;
		
		/** Zmenšování poloviny délky */
		float d;
	}  // class Particle

	/** Pole částic */
	private Particle particle[] = new Particle[MAX_PARTICLES];
	
	/** Barevná paleta */
	private static final float colors[][] =
			{
				{1, 0.5f, 0.5f}, {1, 0.75f, 0.5f}, {1, 1, 0.5f},
				{0.75f, 1, 0.5f}, {0.5f, 1, 0.5f}, {0.5f, 1, 0.75f},
				{0.5f, 1, 1}, {0.5f, 0.75f, 1}, {0.5f, 0.5f, 1},
				{0.75f, 0.5f, 1}, {1, 0.5f, 1}, {1, 0.5f, 0.75f}
			};
	
	
	/**
	 * Konstruktor, inicializuje částice
	 */
	public Explosion() {
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		
		for (int loop = 0; loop < MAX_PARTICLES; loop++) {
			particle[loop] = new Particle();
			
			particle[loop].active = true;
			particle[loop].life = 1;
			particle[loop].fade = (float)(Math.random() + 0.003);
			
			// barva
			particle[loop].r = colors[loop % colors.length][0];
			particle[loop].g = colors[loop % colors.length][1];
			particle[loop].b = colors[loop % colors.length][2];
			
			// pozice
			particle[loop].x = particle[loop].y = particle[loop].z = 0;  
			
			// rychlosti a směry pohybu
			particle[loop].xi = (float)(Math.random() - 0.5);
			particle[loop].yi = (float)(Math.random() - 0.5);
			particle[loop].zi = (float)(Math.random() - 0.5);
			
			// velikost částice a její zmenšování
			particle[loop].l = 0.5f;
			particle[loop].d = 0.01f;
		}
	}
	
	
	/**
	 * Vykreslí daný okamžik exploze
	 * @param w Šířka textury
	 * @param h Výška textury
	 */
	public void render(float w, float h) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glNormal3i(0, 0, 1);
		
		for (int loop = 0; loop < MAX_PARTICLES; loop++) {
			if (!particle[loop].active)
				continue;
			
			float x = particle[loop].x;
			float y = particle[loop].y;
			float z = particle[loop].z + zoom;
			float l = particle[loop].l;
			
			GL11.glColor4f(particle[loop].r, particle[loop].g,
					particle[loop].b, particle[loop].life);
			
			GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
			GL11.glTexCoord2d(w, h);
			GL11.glVertex3f(x + l, y + l, z);
			GL11.glTexCoord2d(0, h);
			GL11.glVertex3f(x - l, y + l, z);
			GL11.glTexCoord2d(w, 0);
			GL11.glVertex3f(x + l, y - l, z);
			GL11.glTexCoord2d(0, 0);
			GL11.glVertex3f(x - l, y - l, z);
			GL11.glEnd();
			
			// pohyby po osou
			particle[loop].x += particle[loop].xi * slowDown;
			particle[loop].y += particle[loop].yi * slowDown;
			particle[loop].z += particle[loop].zi * slowDown;
			
			// zmenšování
			if (particle[loop].l > particle[loop].d)
				particle[loop].l -= particle[loop].d;
			
			// stárnutí
			particle[loop].life -= particle[loop].fade;
			if (particle[loop].life < 0) {
				particle[loop].active = false;
			}
		}
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}  // render
	
}
