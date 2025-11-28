package ru.rsreu.thirdeye.tts;

public class TTSMainTest {
    public static void main(String[] args) {
        TTSClient ttsClient = new TTSClient("https://semionmur-tts.hf.space/tts/getAudio");

        ttsClient.synthesizeSpeechToResources("Мой друг долбаёб как же я ебал это всё", "output.wav");

        //AudioPlayer.playWavFile("output.wav");
        AudioPlayer.playWavFromResourcesAlt("/output.wav");
    }
}
