package org.newdawn.spaceinvaders;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    // 싱글턴 인스턴스
    private static SoundManager instance = new SoundManager();
    // [수정] 스레드 풀 도입: 동시에 최대 10개의 사운드 재생 (필요에 따라 조절)
    private ExecutorService audioExecutor;

    public static SoundManager get() {
        return instance;
    }

    private SoundManager() {
        // 캐시된 스레드 풀을 사용하여 스레드 생성 비용 절감
        audioExecutor = Executors.newCachedThreadPool();
    }

    /**
     * 효과음을 한 번 재생합니다.
     * @param ref 사운드 파일 경로 (예: "sounds/shoot.wav")
     */
    public void playSound(String ref) {
        // [수정] 매번 new Thread() 하지 않고 풀에 작업 위임
        audioExecutor.execute(() -> {
            try {
                URL url = this.getClass().getClassLoader().getResource(ref);
                if (url == null) {
                    System.err.println("Sound file not found: " + ref);
                    return;
                }

                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                // 재생이 끝나면 자원 해제
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        try {
                            audioIn.close(); // 스트림도 닫아주는 것이 안전함
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                // [권장] 실제 운영 환경에서는 Logger 사용 권장
                e.printStackTrace();
            }
        });
    }

    // 게임 종료 시 호출해주면 좋음
    public void shutdown() {
        if (audioExecutor != null) {
            audioExecutor.shutdown();
        }
    }
}