package ru.rsreu.thirdeye.tts;

import javax.sound.sampled.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;

public class AudioPlayer {
    /**
     * Воспроизводит WAV файл из ресурсов
     * @param resourcePath путь к ресурсу (например: "/sounds/sample.wav")
     */
    public static void playWavFromResourcesAlt(String resourcePath) {
        try {
            // Получаем InputStream из ресурсов
            InputStream audioStream = AudioPlayer.class.getResourceAsStream(resourcePath);

            if (audioStream == null) {
                throw new IOException("Ресурс не найден: " + resourcePath);
            }

            // Читаем все данные в массив байтов
            byte[] audioData = audioStream.readAllBytes();
            audioStream.close();

            // Создаем ByteArrayInputStream, который поддерживает mark/reset
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);

            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            Clip audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioInputStream);

            // Используем CountDownLatch для ожидания
            CountDownLatch latch = new CountDownLatch(1);

            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    latch.countDown();
                }
            });

            audioClip.start();
            latch.await(); // Ждем пока звук не закончится

            audioClip.close();
            audioInputStream.close();
            byteArrayInputStream.close();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void playWavFile(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            Clip audioClip = (Clip) AudioSystem.getLine(info);
            audioClip.open(audioStream);

            // Используем CountDownLatch для ожидания
            CountDownLatch latch = new CountDownLatch(1);

            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    latch.countDown();
                }
            });

            audioClip.start();
            latch.await(); // Ждем пока звук не закончится

            audioClip.close();
            audioStream.close();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
