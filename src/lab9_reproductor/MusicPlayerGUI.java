/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab9_reproductor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author harle
 */
public class MusicPlayerGUI extends JFrame {

    private MusicPlayer player;
    private JButton playButton;
    private JButton stopButton;
    private JButton pauseButton;
    private JButton addButton;
    private JButton selectButton;

    private JList<String> playlist;
    private DefaultListModel<String> listModel;
    private JProgressBar progressBar;
    private JLabel songLabel;
    private Timer timer;
    private JLabel durationLabel;
    private JLabel currentPositionLabel;

    public MusicPlayerGUI() {
        player = new MusicPlayer();
        listModel = new DefaultListModel<>();
        playlist = new JList<>(listModel);
        

        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        pauseButton = new JButton("Pause");
        addButton = new JButton("Add");
        selectButton = new JButton("Select");

        playButton.addActionListener(e -> {
            int selectedIndex = playlist.getSelectedIndex();
            if (selectedIndex != -1) {
                player.selectSong(selectedIndex);
                player.play();
                updateProgressBar();
            } else {
                JOptionPane.showMessageDialog(this, "No hay canción seleccionada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        stopButton.addActionListener(e -> {
            player.stop();
            progressBar.setValue(0);
            if (timer.isRunning()) {
                timer.stop();
                playButton.setEnabled(true);
                pauseButton.setText("Pause");
            }
        });

        pauseButton.addActionListener(e -> {
            if (player.isPlaying()) {
                player.pause();
                pauseButton.setText("Resume");
                playButton.setEnabled(false);
            } else {
                player.resume();
                pauseButton.setText("Pause");
                playButton.setEnabled(true);
            }
        });

        addButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("WAV files", "wav"));
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                File musicFolder = new File("src/Music");
                if (!musicFolder.exists()) {
                    musicFolder.mkdirs();
                }
                File newFile = new File(musicFolder, selectedFile.getName());
                try {
                    Files.copy(selectedFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    listModel.addElement(selectedFile.getName().replace(".wav", ""));
                    player.addSong(newFile.getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        selectButton.addActionListener(e -> {
            int index = playlist.getSelectedIndex();
            if (index != -1) {
                player.selectSong(index);
                songLabel.setText("Now playing: " + listModel.getElementAt(index));
                progressBar.setValue(0);
            }
        });

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.BLUE);

        songLabel = new JLabel("No song playing");

        playlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = playlist.getSelectedIndex();
                if (index != -1) {
                    player.selectSong(index);
                    songLabel.setText("Now playing: " + listModel.getElementAt(index));
                    progressBar.setValue(0);
                }
            }
        });

        timer = new Timer(100, e -> {
            if (player.isPlaying()) {
                long currentSeconds = player.getCurrentMicroseconds() / 1_000_000;
                long totalSeconds = player.getTotalSeconds();
                int progress = (int) ((double) currentSeconds / totalSeconds * 100);
                progressBar.setValue(progress);

                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                durationLabel.setText(String.format("%02d:%02d", minutes, seconds));

                long currentMinutes = currentSeconds / 60;
                long currentSecondsRemaining = currentSeconds % 60;
                currentPositionLabel.setText(String.format("%02d:%02d", currentMinutes, currentSecondsRemaining));
            }
        });

        loadSongsFromDefaultFolder();

        JPanel controlPanel = new JPanel();
        controlPanel.add(playButton);
        controlPanel.add(stopButton);
        controlPanel.add(pauseButton);
        controlPanel.add(addButton);
        controlPanel.add(selectButton);

        controlPanel.setBackground(Color.DARK_GRAY);
        progressBar.setBackground(Color.GRAY);
        

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());
        progressPanel.add(progressBar, BorderLayout.CENTER);

        durationLabel = new JLabel("00:00");
        durationLabel.setHorizontalAlignment(SwingConstants.LEFT);
        progressPanel.add(durationLabel, BorderLayout.EAST);

        currentPositionLabel = new JLabel("00:00");
        currentPositionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        progressPanel.add(currentPositionLabel, BorderLayout.WEST);

        progressPanel.add(songLabel, BorderLayout.NORTH);
        progressBar.setStringPainted(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(playlist), BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
        getContentPane().add(progressPanel, BorderLayout.NORTH);

        setSize(1000, 600); // Ajustar el tamaño de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);

    }

    private void updateProgressBar() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private void loadSongsFromDefaultFolder() {
        File musicFolder = new File("src/Music");
        if (musicFolder.exists() && musicFolder.isDirectory()) {
            File[] files = musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (files != null) {
                for (File file : files) {
                    listModel.addElement(file.getName().replace(".wav", ""));
                    player.addSong(file.getAbsolutePath());
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MusicPlayerGUI::new);
    }
}
