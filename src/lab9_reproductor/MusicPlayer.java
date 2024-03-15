/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab9_reproductor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;

/**
 *
 * @author harle
 */
public class MusicPlayer {
    private List<String> playlist;
    private int currentSongIndex;
    private boolean isPlaying;
    private long currentMicroseconds;
    private long totalMicroseconds;
    private Clip clip;
    private long pausedMicroseconds; // Nueva variable para guardar la posición de la canción cuando se pausa
    private boolean wasPaused; // Nueva variable para indicar si la canción estaba pausada al detenerse

    private static final String MUSIC_FOLDER = "Music/";

    public MusicPlayer() {
        playlist = new ArrayList<>();
        currentSongIndex = 0;
        isPlaying = false;
        currentMicroseconds = 0;
        totalMicroseconds = 0;
        clip = null;
        pausedMicroseconds = 0; // Inicializar la posición pausada en 0
        wasPaused = false;
    }

    public void addSong(String filePath) {
        File sourceFile = new File(filePath);
        File destinationFolder = new File(MUSIC_FOLDER);

        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        File destinationFile = new File(destinationFolder, sourceFile.getName());

        try {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            playlist.add(destinationFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectSong(int index) {
        currentSongIndex = index;
    }

    public void play() {
        if (!isPlaying && currentSongIndex >= 0 && currentSongIndex < playlist.size()) {
            isPlaying = true;
            String filePath = playlist.get(currentSongIndex);
            try {
                if (clip == null || !clip.isOpen()) { // Si el Clip no existe o no está abierto, crear un nuevo Clip
                    File file = new File(filePath);
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                    clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    totalMicroseconds = clip.getMicrosecondLength();
                }
                if (wasPaused) { // Si la canción estaba pausada, reanudar desde la posición pausada
                    clip.setMicrosecondPosition(pausedMicroseconds);
                } else {
                    clip.setMicrosecondPosition(0); // Iniciar la canción desde el principio solo si no estaba pausada
                }
                clip.start();

                // Utilizar un Timer para actualizar la posición de reproducción
                Timer timer = new Timer(100, e -> {
                    if (clip.isRunning()) {
                        currentMicroseconds = clip.getMicrosecondPosition();
                    } else {
                        // La canción ha terminado de reproducirse
                        stop();
                    }
                });
                timer.setRepeats(true);
                timer.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        if (clip != null && clip.isRunning()) {
            pausedMicroseconds = clip.getMicrosecondPosition(); // Guardar la posición actual en pausedMicroseconds
            clip.stop();
            isPlaying = false;
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0); // Reiniciar la posición de reproducción al detener la canción
            currentMicroseconds = 0; // También reiniciar currentMicroseconds
            isPlaying = false;
            wasPaused = false;
        }
    }

    public void resume() {
        if (clip != null && !clip.isRunning()) {
            clip.setMicrosecondPosition(pausedMicroseconds); // Restaurar la posición pausada
            clip.start();
            isPlaying = true;
            wasPaused = false; // Indicar que la canción ya no está pausada
        }
    }

    
    
    public long getCurrentMicroseconds() {
        return currentMicroseconds;
    }

    public long getTotalMicroseconds() {
        return totalMicroseconds;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public long getTotalSeconds() {
        return totalMicroseconds / 1_000_000;
    }
}
