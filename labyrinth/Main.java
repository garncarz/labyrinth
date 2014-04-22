package labyrinth;

import java.awt.Font;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.*;


/**
 * Hlavní třída hry bludiště, stará se o běh
 * @author og
 */
public class Main {
	
	/** Šířka okna */
	private int width;
	
	/** Výška okna */
	private int height;
	
	/** Herní engine */
	private Game game;
	
	/** Plocha bludiště */
	private StringBuffer[] lab;
	
	/** Objekt vykreslující texty */
	private UnicodeFont font;
	
	/** Rychlost hry (resp. čekání při hrací jednotce) */
	private int delay = 30;
	
	/** Probíhá zrovna animace? */
	private boolean animation;
	
	/** Pomocná proměnná pro čas ubíhající od určité události */
	private long time;
	
	/** Pomocná proměnná pro start-end sekvenci o dané délce */
	private long start;
	
	/** Aktuální stav programu (hra/výhra/prohra) */
	private int sceneType = SCENE_GAME;
	
	/** Hrací stav */
	private static final int SCENE_GAME = 0;
	
	/** Stav výhry */
	private static final int SCENE_WIN = 1;
	
	/** Stav prohry */
	private static final int SCENE_LOSS = 2;
	
	/** Právě jsme se přepli do daného stavu programu? */
	private boolean justSwitched = true;
	
	/** Zvýšení jemnosti při 2D vykreslování */
	private int ortZoom = 8;
	
	/** Nápověda pro klávesy */
	private String[] infos = {
			"F2 ", "restart   ",
			"Q ", "konec   ",
			"P/SPACE ", "pauza   ",
			"+/- ", "rychlost   ",
			"F ", "fullscreen   "
		};

	
	/**
	 * Konstruktor, vytváří okno a spouští hru
	 */
	public Main() {
		try {
			Display.setTitle("Labyrinth");
			if (!fullscreen())
				window();
			Display.create();
			Keyboard.create();
			
			init();
			start();
			info("Ahoj!");
			game = new Game();
			Models.init();
			// sekvence od posledního start() musí trvat alespoň 1 sekundu
			end(1500);
			
			start();
			info("Inicializuji audio...");
			Audio.init();
			end(1500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Přepne do plné obrazovky
	 * @return Podařilo se?
	 */
	private boolean fullscreen() {
		try {
			// najde nejvhodnější mód
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			DisplayMode mode = modes[0];
			
			for (int i = 1; i < modes.length; i++)
				if (modes[i].getHeight() > mode.getHeight())
					mode = modes[i];
			
			// použije nejvhodnější mód
			width = mode.getWidth();
			height = mode.getHeight();
			Display.setFullscreen(true);
			Display.setDisplayMode(mode);
			Mouse.setGrabbed(true);  // skryj kurzor
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * Přepne do okna
	 * @return Podařilo se?
	 */
	private boolean window() {
		try {
			Mouse.setGrabbed(false);  // povol kurzor
			width = 555;
			height = 555;
			DisplayMode mode = new DisplayMode(width, height);
			Display.setFullscreen(false);
			Display.setDisplayMode(mode);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * Vrací FloatBuffer vytvořený z daných čtyř double hodnot
	 * @param a První hodnota
	 * @param b Druhá hodnota
	 * @param c Třetí hodnota
	 * @param d Čtvrtá hodnota
	 * @return FloatBuffer naplněný danými hodnotami
	 */
	private FloatBuffer fb(double a, double b, double c, double d) {
		FloatBuffer fb = BufferUtils.createFloatBuffer(4).put(
				new float[] {(float)a, (float)b, (float)c, (float)d});
		fb.flip();
		return fb;
	}
	
	
	/**
	 * Inicializace fontu a GL záležitostí
	 */
	private void init() {
		font = new UnicodeFont(new Font("Arial", Font.PLAIN, 150));
		font.getEffects().add(new GradientEffect(java.awt.Color.white,
				java.awt.Color.black, 1.2f));
		font.addAsciiGlyphs();
		font.addGlyphs("Žů");  // nutné pro české znaky!
		try {
			font.loadGlyphs();
		} catch (SlickException e) { e.printStackTrace(); }
		
		GL11.glEnable(GL11.GL_SMOOTH);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LESS);
		GL11.glClearDepth(1);
		
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,
				GL11.GL_MODULATE);
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, fb(0, 0, 0, 0));
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, fb(1, 1, 1, 1));
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, fb(1, 1, 1, 1));
		
		glPersp();
	}
	
	
	/**
	 * Přepne do perspektivní projekce
	 */
	private void glPersp() {
		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(90, (float)width / height, 0.1f, 100);
	}
	
	
	/**
	 * Přepne do 2D projekce
	 */
	private void glOrtho() {
		GL11.glViewport(0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(-width * ortZoom / 2, width * ortZoom / 2,
				-height * ortZoom / 2, height * ortZoom / 2,
				-1, 1);
	}
	
	
	/**
	 * Vykreslí na danou pozici daný text
	 * @param x X-ová souřadnice
	 * @param y Y-ová souřadnice
	 * @param s Text
	 * @param c Barva textu
	 */
	private void glPrint(float x, float y, String s, Color c) {
		glOrtho();  // přepnutí do 2D
		GL11.glDisable(GL11.GL_LIGHTING);  // dočasné vypnutí světel
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glRotatef(180, 1, 0, 0);
		font.drawString(x * ortZoom, y * ortZoom, s, c);
		glPersp();  // raději se vždy vrátíme automaticky zpátky
		GL11.glEnable(GL11.GL_LIGHTING);
	}
	
	
	/**
	 * Vypíše na obrazovku danou informaci
	 * @param s Informace
	 */
	private void info(String s) {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		glPrint(-width / ortZoom * 2, 0, s, Color.blue);
		Display.update();
	}
	
	
	/**
	 * Vypíše informace o uživatelovi (počet životů a kolo)
	 */
	private void printPlayerInfo() {
		String[] infos = {
				"Kolo: ", String.valueOf(game.level) + "   ",
				"Životů: ", String.valueOf(game.lives)
		};
		
		int posV = -height / 2 + 10;
		int posH = -width / 2 + 10;
		for (int i = 0; i < infos.length; i++) {
			String s = infos[i];
			if (i % 2 == 0)
				glPrint(posH, posV, s, Color.blue);
			else
				glPrint(posH, posV, s, Color.red);
			posH += font.getWidth(s) / ortZoom;
		}
	}
	
	
	/**
	 * Vypíše klávesy pro příkazy
	 */
	private void printKeyInfo() {
		int posV = height / 2 - 30;
		int posH = -width / 2 + 15;
		for (int i = 0; i < infos.length; i++) {
			String s = infos[i];
			if (i % 2 == 0)
				glPrint(posH, posV, s, Color.yellow);
			else
				glPrint(posH, posV, s, Color.blue);
			posH += font.getWidth(s) / ortZoom;
		}
	}
	

	/**
	 * Zjistí, jestli uživatel nevydal nějaký příkaz,
	 * případně jej provede
	 */
	private void playerCommands() {
		// restart
		if (Keyboard.isKeyDown(Keyboard.KEY_F2)) {
			game.restart();
			sceneType = SCENE_GAME;
			justSwitched = true;
		}
		// konec
		else if (Keyboard.isKeyDown(Keyboard.KEY_Q))
			quit();
		// pauza
		else if (Keyboard.isKeyDown(Keyboard.KEY_P))
			do {
				sleep(100);
				Display.update();
			} while (!Keyboard.isKeyDown(Keyboard.KEY_SPACE));
		// fullscreen
		else if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			if (Display.isFullscreen())
				window();
			else
				fullscreen();
		}
		// zrychlení
		else if (Keyboard.isKeyDown(Keyboard.KEY_ADD)) {
			if (delay > 2)
				delay -= 2;
		}
		// zpomalení
		else if (Keyboard.isKeyDown(Keyboard.KEY_SUBTRACT)) {
			delay += 2;
		}
	}
	
	
	/**
	 * Zaspí na daný čas
	 * @param milis Čas v milisekundách
	 */
	public static void sleep(int milis) {
		if (milis <= 0)
			return;
		try {
			Thread.sleep(milis);
		} catch (Exception e) { e.printStackTrace(); };
	}
	
	
	/**
	 * Začne měřit čas pro start-end sekvenci o dané délce
	 */
	private void start() {
		start = System.currentTimeMillis();
	}
	
	
	/**
	 * Ukončí start-end sekvenci dovršením alespoň daného času
	 * @param time Čas pro danou sekvenci
	 */
	private void end(int time) {
		long end = System.currentTimeMillis();
		sleep(time - (int)(end - start));
	}
	
	
	/**
	 * Aktualizuje zvuky sonarů podle blízkosti nepřátel hráči
	 */
	private void updateSonars() {
		for (int i = 1; i < game.enemies; i++) {
			double vol = 1;
			double dist = game.getDistFromPlayer(i);
			if (dist > 1)
				vol = 1 / dist;
			Audio.setVol(Audio.ENEMY1 + i - 1, vol);
		}
	}
	
	
	/**
	 * Samotný běh hry
	 */
	public void run() {
		while (true) {
			// obsluha požadavků uživatele
			playerCommands();
			
			// pokud jsme se právě přepli do nového stavu,
			// provedem jeho inicializaci
			if (justSwitched) {
				Audio.stopAll();
				switch (sceneType) {
				case SCENE_GAME: gameInit(); break;
				case SCENE_WIN: winInit(); break;
				case SCENE_LOSS: lossInit(); break;
				}
				justSwitched = false;
			}
			
			start();
			
			// vymazání plochy				
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			labView();  // pohled na scénu
			
			// vykreslení a provedení scény podle aktuálního typu
			switch (sceneType) {
			case SCENE_GAME:
				playerLight();  // světlo na hráče
				gameUpdate();
				break;
			case SCENE_WIN:
				winUpdate();
				break;
			case SCENE_LOSS:
				lossUpdate();
				break;
			}
			
			printKeyInfo();  // nápověda
			
			// obnovení zobrazení a počkání
			Display.update();
			
			end(delay);
			time += System.currentTimeMillis() - start;
			
			// ukončení hry v případě vyžádání
			if (Display.isCloseRequested())
				quit();
		}
	}
	
	
	/**
	 * Ukončí hru s úklidem
	 */
	private void quit() {
		try {
			Audio.destroy();
		} catch (Exception e) { e.printStackTrace(); }
		Keyboard.destroy();
		Display.destroy();
		System.exit(0);
	}
	
	
	/**
	 * Přepne na pohled na bludiště
	 */
	private void labView() {
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		float centerX = lab[0].length() / 2.0f;
		float centerY = lab.length / 2.0f;
		GLU.gluLookAt(centerX, centerY - 4, 15, centerX, centerY,
				0, 0, 1, 0);
	}
	
	/**
	 * Zaměří světlo na střed bludiště
	 */
	private void centerLight() {
		float centerX = lab[0].length() / 2.0f;
		float centerY = lab.length / 2.0f;
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION,
				fb(centerX, centerY, 20, 1));
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPOT_DIRECTION,
				fb(0, 0, -1, 1));
	}
	
	
	/**
	 * Zaměří světlo na hráče
	 */
	private void playerLight() {
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION,
				fb(game.getPosCol(0), game.getPosRow(0), 10, 1));
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPOT_DIRECTION,
				fb(0, 0, -1, 1));
	}
	
	
	/**
	 * Inicializuje zobrazení hry
	 */
	private void gameInit() {
		animation = false;
		Audio.playEnemies();
		start();
		info("Generuji nové kolo...");
		game.nextLevel();
		lab = game.getLabyrinth();
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_SPOT_CUTOFF, 20);
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_CONSTANT_ATTENUATION, 1);
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_LINEAR_ATTENUATION, 0.01f);
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_QUADRATIC_ATTENUATION, 0.01f);
		end(2000);
	}
	
	
	/**
	 * Inicializuje zobrazení prohry 
	 */
	private void lossInit() {
		time = 0;
		Audio.play(Audio.LOSS);
		game.setLossLevel();
		lab = game.getLabyrinth();
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_SPOT_CUTOFF, 180);
	}
	
	
	/**
	 * Inicializuje zobrazení výhry
	 */
	private void winInit() {
		time = 0;
		Audio.play(Audio.WIN);
		game.setWinnerLevel();
		lab = game.getLabyrinth();
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_CONSTANT_ATTENUATION, 1);
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_LINEAR_ATTENUATION, 0);
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_QUADRATIC_ATTENUATION, 0);
	}
	
	
	/**
	 * Aktualizuje zobrazení i průběh hry
	 */
	private void gameUpdate() {
		// vyrenderování scény
		Models.renderLabyrinth(lab);
		for (int i = 1; i < game.enemies; i++)
			Models.renderEnemy(game.getPosRow(i), game.getPosCol(i));
		Models.renderGoal(game.getPosRow(game.enemies),
				game.getPosCol(game.enemies));
		// pokud hráč narazil na nepřítele, bude místo něj již jen exploze
		if (game.isCollision()) {
			if (!animation) {
				animation = true;
				time = 0;
				Models.initExplosion();
				Audio.pauseEnemies();
				Audio.play(Audio.EXPLOSION);
			}
			Models.renderExplosion(game.getPosRow(0), game.getPosCol(0),
					true);
		}
		// dostal se hráč do cíle? ano => exploze sněhu
		else if (game.isGoal()) {
			if (!animation) {
				animation = true;
				time = 0;
				Models.initExplosion();
				Audio.pauseEnemies();
				Audio.play(Audio.TELEPORT);
			}
			Models.renderExplosion(game.getPosRow(0), game.getPosCol(0),
					false);
		}
		// jinak normální vykreslení hráče
		else
			Models.renderPlayer(game.getPosRow(0), game.getPosCol(0));
		
		// aktualizace sonarů
		updateSonars();
		
		// pokud probíhá animace, budeme v ní pokračovat pouze po určitý čas;
		// poté se program rozhodne, jak pokračovat
		if (animation) {
			if (time > 2000) {
				// opakování kola
				if (game.isRepetition()) {
					animation = false;
					Audio.playEnemies();
					game.repeatLevel();
				}
				// prohra
				else if (game.isLoss()) {
					sceneType = SCENE_LOSS;
					justSwitched = true;
				}
				// další kolo
				else if (game.isPromotion()) {
					justSwitched = true;
				}
				// výhra
				if (game.isWin()) {
					sceneType = SCENE_WIN;
					justSwitched = true;
				}
			}
		}
		// pokud neprobíhá animace, hraje se dále
		else
			game.update();
		
		printPlayerInfo();
	}
	
	
	/**
	 * Aktualizuje zobrazení prohry
	 */
	private void lossUpdate() {
		if (time < 5000)
			return;
		// zatemnění scény
		float dark = 10;
		if (time < 8000)
			dark = 40 - (time - 5000) / 100;
		centerLight();  // světlo doprostřed
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_CONSTANT_ATTENUATION, dark);
		Models.renderLabyrinth(lab);
	}
	
	
	/**
	 * Aktualizuje zobrazení výhry
	 */
	private void winUpdate() {
		if (time < 16000)
			return;
		// rozevření kužele osvětlení
		float angle = 1;
		// kužel se rozevírá
		angle += 0.5f * (time - 16000) / 100;
		centerLight();  // světlo doprostřed
		GL11.glLightf(GL11.GL_LIGHT0, GL11.GL_SPOT_CUTOFF, angle);
		Models.renderLabyrinth(lab);
	}
	
	
	/**
	 * Spouští aplikaci
	 * @param args Argumenty, nepoužívají se
	 */
	public static void main(String[] args) {
		System.setProperty("org.lwjgl.librarypath",
				System.getProperty("user.dir") + "/labyrinth.lib/natives/");
		Main m = new Main();
		m.run();
	}
	
}
