// AuthService.java
package org.newdawn.spaceinvaders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

public class AuthService {

    /**
     * 클라이언트로부터 받은 ID 토큰 검증
     * @param idToken 검증할 ID 토큰 문자열
     * @return 검증 성공 시 사용자의 UID, 실패 시 null
     */
    public String verifyIdToken(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            System.err.println("ID 토큰이 비어있습니다.");
            return null;
        }

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            String uid = decodedToken.getUid();
            System.out.println("인증 성공! UID: " + uid);
            return uid;
        } catch (Exception e) {
            System.err.println("인증 실패: " + e.getMessage());
            return null;
        }
    }
}