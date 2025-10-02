// FirebaseInitializer.java
package org.newdawn.spaceinvaders;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInitializer {

    public void initialize() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/space-invaders-dd665-firebase-adminsdk-fbsvc-dfecce036c.json"); // 실제 키 파일명으로 변경

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