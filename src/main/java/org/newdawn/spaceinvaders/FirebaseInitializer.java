package org.newdawn.spaceinvaders;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.InputStream; // FileInputStream 대신 InputStream 사용
import java.io.IOException;

public class FirebaseInitializer {

    public void initialize() {
        try {
            // [수정] FileInputStream 대신 getResourceAsStream 사용 (가장 안전한 방법)
            // 파일명만 입력하면 resources 폴더 안에서 자동으로 찾습니다.
            // [FirebaseInitializer.java]
            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("space-invaders-dd665-firebase-adminsdk-fbsvc-dfecce036c.json");

            if (serviceAccount == null) {
                System.err.println("Firebase 키 파일을 찾을 수 없습니다! resources 폴더를 확인하세요.");
                return;
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase has been initialized.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}