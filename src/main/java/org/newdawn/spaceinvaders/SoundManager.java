package org.newdawn.spaceinvaders;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    // 싱글턴 인스턴스
    private static SoundManager instance = new SoundManager();

    public static SoundManager get() {
        return instance;
    }

    private SoundManager() {}

    /**
     * 효과음을 한 번 재생합니다.
     * @param ref 사운드 파일 경로 (예: "sounds/shoot.wav")
     */
    public void playSound(String ref) {
        // 게임 성능 저하를 막기 위해 별도 스레드에서 재생
        new Thread(() -> {
            try {
                URL url = this.getClass().getClassLoader().getResource(ref);
                if (url == null) {
                    System.err.println("Sound file not found: " + ref);
                    return;
                }

                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                // 재생이 끝나면 자원 해제 (메모리 누수 방지)
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
