package labyrinth;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

/**
 * Třída zabezpečující přehrávání zvuků hry,
 * podle http://lwjgl.org/documentation_openal_03.php
 */
public class Audio {
	
	public static final int
			/** Exploze */
			EXPLOSION = 1,
			/** Teleportace */
			TELEPORT = 2,
			/** Výhra */
			WIN = 3,
			/** Prohra */
			LOSS = 4,
			/** Efekty nepřítele č. 1 */
			ENEMY1 = 5,
			/** Efekty nepřítele č. 2 */
			ENEMY2 = 6,
			/** Efekty nepřítele č. 3 */
			ENEMY3 = 7,
			/** Efekty nepřítele č. 4 */
			ENEMY4 = 8;
	
	/** Počet zvukových stop */
	public static final int TRACKS = 9;
	
	/** Zvukové buffery */
	private static IntBuffer buffer = BufferUtils.createIntBuffer(TRACKS);

	/** Body emitující zvuky */
	private static IntBuffer source = BufferUtils.createIntBuffer(TRACKS);
	
	
	/**
	 * Inicializace audia
	 * @throws Exception V případě chyby
	 */
	public static void init() throws Exception {
		AL.create();
		AL10.alGenBuffers(buffer);
		
		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			throw new Exception("Nepodarilo se vytvorit zvukove buffery");
		
		WaveData wave = WaveData.create("resource/sonar.wav");
		AL10.alBufferData(buffer.get(ENEMY1), wave.format, wave.data,
				wave.samplerate);
		AL10.alBufferData(buffer.get(ENEMY2), wave.format, wave.data,
				wave.samplerate);
		AL10.alBufferData(buffer.get(ENEMY3), wave.format, wave.data,
				wave.samplerate);
		AL10.alBufferData(buffer.get(ENEMY4), wave.format, wave.data,
				wave.samplerate);
		wave.dispose();
		
		wave = WaveData.create("resource/explosion.wav");
		AL10.alBufferData(buffer.get(EXPLOSION), wave.format, wave.data,
				wave.samplerate);
		wave.dispose();
		
		wave = WaveData.create("resource/teleport.wav");
		AL10.alBufferData(buffer.get(TELEPORT), wave.format, wave.data,
				wave.samplerate);
		wave.dispose();
		
		wave = WaveData.create("resource/italy.wav");
		AL10.alBufferData(buffer.get(WIN), wave.format, wave.data,
				wave.samplerate);
		wave.dispose();
		
		wave = WaveData.create("resource/soviet.wav");
		AL10.alBufferData(buffer.get(LOSS), wave.format, wave.data,
				wave.samplerate);
		wave.dispose();
		
		
		AL10.alGenSources(source);
		
		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			throw new Exception("Nepodarilo se vytvorit zdroje zvuku");
		
		for (int i = 1; i < TRACKS; i++)
			AL10.alSourcei(source.get(i), AL10.AL_BUFFER, buffer.get(i));
		
		// sonary se budou opakovat
		for (int i = ENEMY1; i <= ENEMY4; i++)
			AL10.alSourcei(source.get(i), AL10.AL_LOOPING, AL10.AL_TRUE);
		
		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			throw new Exception("Nepodarilo se propojit zdroje zvuku s buffery");
		
		muteEnemies();
		play(ENEMY1);
		Main.sleep(500);
		play(ENEMY2);
		Main.sleep(500);
		play(ENEMY3);
		/*
		// nepřítel č. 4 se stejně nevyužívá
		Main.sleep(500);
		play(ENEMY4);
		*/
	}
	
	
	/**
	 * Uklidí audio paměť a vypne jeho zpracovávání
	 */
	public static void destroy() {
		AL10.alDeleteSources(source);
		AL10.alDeleteBuffers(buffer);
		AL.destroy();
	}

	
	/**
	 * Spustí přehrávání dané stopy
	 * @param track Číslo stopy
	 */
	public static void play(int track) {
		AL10.alSourcePlay(source.get(track));
	}
	
	
	/**
	 * Pozastaví přehrávání dané stopy
	 * @param track Číslo stopy
	 */
	public static void pause(int track) {
		AL10.alSourcePause(source.get(track));
	}
	
	
	/**
	 * Zastaví přehrávání dané stopy
	 * @param track Číslo stopy
	 */
	public static void stop(int track) {
		AL10.alSourceStop(source.get(track));
	}
	
	
	/**
	 * Ztlumí sonary nepřátel
	 */
	public static void muteEnemies() {
		setVol(ENEMY1, 0);
		setVol(ENEMY2, 0);
		setVol(ENEMY3, 0);
		setVol(ENEMY4, 0);
	}
	
	
	/**
	 * Spustí sonary nepřátel
	 */
	public static void playEnemies() {
		muteEnemies();
		play(ENEMY1);
		play(ENEMY2);
		play(ENEMY3);
		play(ENEMY4);
	}
	
	
	/**
	 * Pozastaví sonary nepřátel
	 */
	public static void pauseEnemies() {
		pause(ENEMY1);
		pause(ENEMY2);
		pause(ENEMY3);
		pause(ENEMY4);
	}
	
	
	/** 
	 * Určí hlasitost zvuku danému kanálu
	 * @param nr Číslo kanálu
	 * @param volume Hlasitost
	 */
	public static void setVol(int nr, double volume) {
		AL10.alSourcef(source.get(nr), AL10.AL_GAIN, (float)volume);
	}
	
	
	/**
	 * Umlčí veškeré přehrávání audia
	 */
	public static void stopAll() {
		for (int i = 1; i <= LOSS; i++)
			stop(i);
		pauseEnemies();
	}
	
}
