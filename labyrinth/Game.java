package labyrinth;

import org.lwjgl.input.Keyboard;

/**
 * Třída vyjadřující stav hry a umožňující její vývoj
 * @author og
 */
public class Game {
	
	/**
	 * Pomocná třída pro souřadnici na hrací ploše
	 * s 10násobnou jemností	 	 
	 * @author og
	 */
	public class Coord {
		
		/** Řádek na hrací ploše */
		int row;
		
		/** Sloupec na hrací ploše */
		int col;
		
		/**
		 * Konstruktor
		 */
		public Coord() {
			row = col = 0;
		}
		
		/**
		 * Konstruktor
		 * @param row Řádek na hrací ploše
		 * @param col Sloupec na hrací ploše
		 */
		public Coord(int row, int col) {
			this.row = row;
			this.col = col;
		}
		
		/**
		 * Vrátí součet s druhou pozicí
		 * @param c Druhá pozice
		 * @return Součet pozic
		 */
		public Coord add(Coord c) {
			if (c == null)
				return null;
			return new Coord(row + c.row, col + c.col);
		}
		
		/**
		 * Porovná kolizi s druhou souřadnicí
		 * @param c Druhá souřadnice
		 * @return Kolidují souřadnice?
		 */
		public boolean collides(Coord c) {
			return (row == c.row && Math.abs(col - c.col) < 10) ||
					(col == c.col && Math.abs(row - c.row) < 10);
		}
		
		/**
		 * Vrátí vzdálenost od druhé souřadnice
		 * @param c Druhá souřadnice
		 * @return Vzdálenost
		 */
		public int distance(Coord c) {
			if (c == null)
				return Integer.MAX_VALUE;
			return (int)Math.sqrt(Math.pow(row - c.row, 2) +
					Math.pow(col - c.col, 2));
		}
	}
	
	/** Pozice postav na ploše */
	private Coord[] pos;
	
	/** Zálohy pozic */
	private Coord[] oldPos;
	
	/** Následující směr hráče */
	private Coord playerNextDir;
	
	/** Zamýšlený směr hráče */
	private Coord playerIntentDir;
	
	/** Počet nepřátel plus jedna */
	public int enemies = 4;
	
	/** Počet životů na začátku hry */
	public int LIVES = 50;
	
	/** Poslední kolo */
	public int MAX_LEVEL = 5;
	
	/** Počet životů */
	public int lives;
	
	/** Kolo */
	public int level;
	
	/** Došlo ke kolizi? */
	private boolean collision = false;
	
	/** Je hráč v cíli? */
	private boolean goal = false;
	
	/** Obsah hrací plochy */
	private StringBuffer[] lab;
	
	
	/**
	 * Vrací podle klávesnice následující směr hráče
	 * @return Následující směr hráče
	 */
	private Coord playerRead() {
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			return new Coord(0, -1);
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			return new Coord(0, 1);
		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
			return new Coord(1, 0);
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			return new Coord(-1, 0);
		return null;
	}
	
	
	/**
	 * Kontroluje platnost pozice vzhledem k ploše
	 * @param c Pozice
	 * @return Je možné se přesunout na pozici? (Nehledě na kolize.)
	 */
	private boolean correctCoord(Coord c) {
		if (c == null)
			return false;
		
		int rowm = c.row % 10;
		int colm = c.col % 10;
		
		// můžeme se pohybovat pouze jedním směrem
		if (rowm > 0 && colm > 0)
			return false;
		
		int row = c.row / 10;
		int col = c.col / 10;
		
		// nesmyslná pozice či zeď
		if (row <= 0 || row >= lab.length - 1 ||
				col <= 0 || col >= lab[0].length() - 1 ||
				lab[row].charAt(col) == 'X')
			return false;
		
		// přímo na pozici
		if (rowm == 0 && colm == 0)
			return true;
		
		// v pohybu - není na následující pozici zeď?
		if (rowm > 0 &&
				(row + 1) < lab.length - 1 &&
				lab[row + 1].charAt(col) != 'X')
			return true;
		if (colm > 0 &&
				(col + 1) < lab[0].length() - 1 &&
				lab[row].charAt(col + 1) != 'X')
			return true;
		return false;
	}
	
	
	/**
	 * Zkontroluje srážku hráče s nepřítelem
	 * @return Srazil se hráč s nepřítelem?
	 */
	public boolean isCollision() {
		if (collision)
			return true;
		for (int i = 1; i < enemies; i++)
			if (pos[0].collides(pos[i])) {
				lives--;
				collision = true;
			}
		return collision;
	}
	
	
	/**
	 * Kontroluje dosažení cíle hráčem
	 * @return Je hráč v cíli?
	 */
	public boolean isGoal() {
		if (goal)
			return true;
		if (pos[0].collides(pos[enemies])) {
			level++;
			goal = true;
		}
		return goal;
	}
	
	
	/**
	 * Konstruktor
	 */
	public Game() {
		restart();
	}
	
	
	/**
	 * Restartuje hru
	 */
	public void restart() {
		lives = LIVES;
		level = 1;
	}
	
	
	/**
	 * Vytvoří další kolo
	 */
	public void nextLevel() {
		// budeme se tak dlouho pokoušet vygenerovat, až vygenerujeme
		boolean ok = false;
		while (!ok)
			try {
				generateRandomLevel(15, 20);
				ok = true;
			} catch (Exception e) { }
		
		generatePositions();
		repeatLevel();
	}
	
	
	/**
	 * Připraví pozice na ploše pro opakování kola
	 */
	public void repeatLevel() {
		collision = false;
		goal = false;
		playerIntentDir = playerNextDir = null;
		
		// zkopírování pozic ze záloh
		pos = new Coord[enemies + 1];
		for (int i = 0; i <= enemies; i++)
			pos[i] = new Coord(oldPos[i].row, oldPos[i].col);
		
		// žádný pohyb v první časové jednotce
		playerNextDir = new Coord(0, 0);
	}
	
	
	/**
	 * Vygeneruje náhodnou situaci na ploše o daných rozměrech
	 * @param X Výška
	 * @param Y Šířka
	 */
	private void generateRandomLevel(int X, int Y) throws Exception {
		// číslo pokusu - abychom neuvízli
		int tryNr = 0;
		
		// vytvoření zaplněné plochy
		lab = new StringBuffer[X];
		for (int i = 0; i < X; i++) {
			lab[i] = new StringBuffer(Y);
			for (int j = 0; j < Y; j++)
				lab[i].append('X');
		}
		
		// vytvoření prvního volného políčka
		int x = (int)(Math.random() * (X - 2)) + 1;
		int y = (int)(Math.random() * (Y - 2)) + 1;
		lab[x].setCharAt(y, '.');
		int free = 1;
		
		// celkový zamýšlený počet volných políček
		int toBeFree = (int)(X * Y * 0.5);
		
		// dokud není dostatečný počet volných políček
		while (free < toBeFree) {
			// pokus se najít další souřadnici volného políčka
			x = (int)(Math.random() * (X - 2)) + 1;
			y = (int)(Math.random() * (Y - 2)) + 1;
			if (lab[x].charAt(y) == '.')
				continue;
			
			if (++tryNr > X * Y * 5)
				throw new Exception("Prilis mnoho pokusu");
			
			// spočítej počet volných sousedů
			int neighbr = 0;
			if (lab[x + 1].charAt(y) == '.')
				neighbr++;
			if (lab[x - 1].charAt(y) == '.')
				neighbr++;
			if (lab[x].charAt(y + 1) == '.')
				neighbr++;
			if (lab[x].charAt(y - 1) == '.')
				neighbr++;
			
			// a volných šikmých sousedů
			int near = 0;
			if (lab[x + 1].charAt(y + 1) == '.')
				near++;
			if (lab[x - 1].charAt(y - 1) == '.')
				near++;
			if (lab[x - 1].charAt(y + 1) == '.')
				near++;
			if (lab[x + 1].charAt(y - 1) == '.')
				near++;
			
			// v určitých případech zde volné políčko nevytvoříme
			if (neighbr == 0 || near + neighbr > 3 ||
					(near + neighbr >= 2 && Math.random() > 0.5))
				continue;
			
			// vytvoření volného políčka
			lab[x].setCharAt(y, '.');
			free++;
		}
	}
	
	
	/**
	 * Vygeneruje pozice hráče, nepřátel a cíle na ploše
	 */
	private void generatePositions() {
		// rozměry
		int X = lab.length + 1;
		int Y = lab[0].length() + 1;
		
		int tryNr = 0;
		int x, y;
		
		// pozice hráče a nepřátel
		pos = new Coord[enemies + 1];
		
		// nejprve pozice hráče na volném políčku
		int x0 = 0, y0 = 0;
		while (pos[0] == null) {
			x0 = (int)(Math.random() * (X - 2)) + 1;
			y0 = (int)(Math.random() * (Y - 2)) + 1;
			if (lab[x0].charAt(y0) == '.')
				pos[0] = new Coord(x0 * 10, y0 * 10);
		}
		
		// pozice nepřátel a cíle na volných políčcích dostatečně daleko
		// od hráče
		for (int i = 1; i < enemies + 1; i++) {
			tryNr = 0;
			while (pos[i] == null) {
				x = (int)(Math.random() * (X - 2)) + 1;
				y = (int)(Math.random() * (Y - 2)) + 1;
				if (lab[x].charAt(y) == '.' &&
						(Math.pow(x - x0, 2) + Math.pow(y - y0, 2) > 80 ||
						tryNr > 10))  // nebudeme zkoušet věčně
				{
					pos[i] = new Coord(x * 10, y * 10);
					lab[x].setCharAt(y, 'o');
				}
				tryNr++;
			}
		}
		
		// zkopírování pozic do záloh
		oldPos = new Coord[enemies + 1];
		for (int i = 0; i <= enemies; i++)
			oldPos[i] = new Coord(pos[i].row, pos[i].col);
	}
	
	
	/**
	 * Nastaví jako aktuální vítěznou plochu
	 */
	public void setWinnerLevel() {
		String[] labS = new String[] {
				"XXXXXXXXXXXXXXXXXXX",
				"X.................X",
				"X....X...X...X....X",
				"X...X.XXXXXXX.X...X",
				"X...X.X.....X.X...X",
				"X...X.X..X..X.X...X",
				"X....XX.XX..XX....X",
				"X.....X..X..X.....X",
				"X.....X.....X.....X",
				"X......XXXXX......X",
				"X........X........X",
				"X........X........X",
				"X.......XXX.......X",
				"X.................X",
				"XXXXXXXXXXXXXXXXXXX",
		};

		lab = new StringBuffer[labS.length];
		for (int i = 0; i < lab.length; i++)
			lab[labS.length - 1 - i] = new StringBuffer(labS[i]);
	}
	
	
	/**
	 * Nastaví jako aktuální plochu prohry
	 */
	public void setLossLevel() {
		String[] labS = new String[] {
				"XXXXXXXXXXXXXXXXXXX",
				"X.................X",
				"X.....XXXXXXX.....X",
				"X....X.......X....X",
				"X...X.........X...X",
				"X...X..X...X..X...X",
				"X...X....X....X...X",
				"X...X.........X...X",
				"X...X..XXXXX..X...X",
				"X...X.X.....X.X...X",
				"X...X.........X...X",
				"X....X.......X....X",
				"X.....XXXXXXX.....X",
				"X.................X",
				"XXXXXXXXXXXXXXXXXXX",
		};

		lab = new StringBuffer[labS.length];
		for (int i = 0; i < lab.length; i++)
			lab[labS.length - 1 - i] = new StringBuffer(labS[i]);
	}
	
	
	/**
	 * Provede taktiku daného nepřítele
	 * @param nr Číslo nepřítele
	 */
	private void updateAI(int nr) {
		// pohyb nepřítele přiblížením k pozici hráče
		Coord co = null;
		boolean ok;
		
		// nejdříve se pokusíme pohnout v řádku
		if (pos[nr].row > pos[0].row)
			co = new Coord(-1, 0);
		else if (pos[nr].row < pos[0].row)
			co = new Coord(1, 0);
		
		if (co != null) {
			co = pos[nr].add(co);
			// lze se takto pohnout v řádku?
			ok = true;
			if (!correctCoord(co))
				ok = false;
			for (int i = 1; ok && i < enemies; i++)
				if (i != nr && co.collides(pos[i]))
					ok = false;
			if (ok) {
				pos[nr] = co;
				return;
			}
		}
		
		// nejde, zkusíme v sloupci
		co = null;
		if (pos[nr].col > pos[0].col)
			co = new Coord(0, -1);
		else if (pos[nr].col < pos[0].col)
			co = new Coord(0, 1);
		co = pos[nr].add(co);
		
		if (co != null) {
			ok = true;
			if (!correctCoord(co))
				ok = false;
			for (int i = 1; ok && i < enemies; i++)
				if (i != nr && co.collides(pos[i]))
					ok = false;
			if (ok)
				pos[nr] = co;
		}
	}

	
	/**
	 * Vývoj hry
	 */
	public void update() {
		// v případě kolize se již dále nehraje
		if (!collision)
		{
			// přeje si hráč v dalším kroku změnit směr?
			Coord co = playerRead();
			if (co != null)
				playerIntentDir = co;
			
			// aktualizace pozice hráče
			co = pos[0].add(playerIntentDir);
			if (correctCoord(co))
				playerNextDir = playerIntentDir;
			co = pos[0].add(playerNextDir);
			if (correctCoord(co))
				pos[0] = co;
			
			// aktualizace pozic nepřátel
			for (int i = 1; i < enemies; i++)
				updateAI(i);
		}
	}
	
	
	/**
	 * Vrací bludiště
	 * @return Bludiště 
	 */
	public StringBuffer[] getLabyrinth() {
		return lab;
	}
	
	
	/**
	 * Vrací řádkovou souřadnici dané postavy na ploše
	 * převedenou na reálnou přesnost	 
	 * @param nr Číslo postavy
	 * @return Řádková souřadnice
	 */
	public float getPosRow(int nr) {
		return pos[nr].row / 10.0f;
	}
	
	/**
	 * Vrací sloupcovou souřadnici dané postavy na ploše
	 * převedenou na reálnou přesnost	 
	 * @param nr Číslo postavy
	 * @return Sloupcová souřadnice
	 */
	public float getPosCol(int nr) {
		return pos[nr].col / 10.0f;
	}
	
	
	/**
	 * Vrátí vzdálenost nepřítele od hráče
	 * převedenou na reálnou hodnotu
	 * @param nr Číslo nepřítele
	 * @return Vzdálenost
	 */
	public float getDistFromPlayer(int nr) {
		return pos[0].distance(pos[nr]) / 10.0f;
	}

	
	/**
	 * Vrací, zda uživatel neprohrál
	 * @return Prohrál uživatel?
	 */
	public boolean isLoss() {
		return isCollision() && lives <= 0;
	}
	
	
	/**
	 * Vrací, zda uživatel nevyhrál
	 * @return Vyhrál uživatel?
	 */
	public boolean isWin() {
		return isGoal() && level > MAX_LEVEL;
	}
	
	
	/**
	 * Vrací, zda uživatel opakuje kolo
	 * @return Opakuje se kolo?
	 */
	public boolean isRepetition() {
		return isCollision() && lives > 0;
	}
	
	
	/**
	 * Vrací, zda se uživatel dostal do dalšího kola
	 * @return Další kolo?
	 */
	public boolean isPromotion() {
		return isGoal() && level <= MAX_LEVEL;
	}
	
}
