// FirebaseInitializer.java
package org.newdawn.spaceinvaders;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInitializer {

    public void initialize() {
        // 먼저 클래스패스에서 서비스 계정 JSON을 찾습니다 (JAR로 배포 시 필요).
        String resourceName = "/space-invaders-dd665-firebase-adminsdk-fbsvc-dfecce036c.json"; // 클래스패스 루트에 놓을 경우
        String fallbackPath = "src/main/resources/space-invaders-dd665-firebase-adminsdk-fbsvc-dfecce036c.json"; // 개발 중 파일 경로

        try (java.io.InputStream serviceAccountStream = FirebaseInitializer.class.getResourceAsStream(resourceName) != null
                ? FirebaseInitializer.class.getResourceAsStream(resourceName)
                : new FileInputStream(fallbackPath)) {

            if (serviceAccountStream == null) {
                System.err.println("Firebase service account not found on classpath or at: " + fallbackPath);
                return;
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase has been initialized.");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}