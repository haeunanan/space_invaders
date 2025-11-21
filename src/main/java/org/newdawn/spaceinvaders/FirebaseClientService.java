package org.newdawn.spaceinvaders;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseClientService {

    // 중요: 이 API 키는 Firebase 프로젝트 설정에서 찾아야 합니다.
    // 이 키는 클라이언트에 노출되어도 안전한 키입니다.
    private static final String WEB_API_KEY = "AIzaSyCg47obQ1LAaQ1d0M87t8KGcVn4rGabDio";

    private static final String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + WEB_API_KEY;
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + WEB_API_KEY;

    /**
     * 이메일과 비밀번호로 Firebase에 회원가입을 요청합니다.
     * @return 회원가입 성공 시 true, 실패 시 false
     */
    public boolean signUp(String email, String password) {
        try {
            // 1. HTTP 연결 설정
            URL url = new URL(SIGN_UP_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 2. Firebase에 보낼 JSON 데이터 생성
            String jsonInputString = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"returnSecureToken\": true}";

            // 3. 데이터 전송
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 4. 응답 코드 확인 (200이면 성공)
            int responseCode = conn.getResponseCode();
            System.out.println("Sign Up Response Code :: " + responseCode);

            return responseCode == HttpURLConnection.HTTP_OK;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 이메일과 비밀번호로 Firebase에 로그인을 요청하고 ID 토큰을 받아옵니다.
     * @return 로그인 성공 시 ID 토큰, 실패 시 null
     */
    public String signIn(String email, String password) {
        try {
            URL url = new URL(SIGN_IN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"returnSecureToken\": true}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Sign In Response Code :: " + responseCode);

            // 응답 본문(JSON) 읽기 (성공/실패 모두 로깅)
            InputStreamReader isr;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                isr = new InputStreamReader(conn.getInputStream(), "utf-8");
            } else {
                isr = new InputStreamReader(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), "utf-8");
            }

            BufferedReader br = new BufferedReader(isr);
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            String responseBody = response.toString();
            System.out.println("Sign In Response Body :: " + responseBody);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 안전하게 idToken 추출
                if (responseBody.contains("\"idToken\"")) {
                    try {
                        String idToken = responseBody.split("\"idToken\"\\s*:\\s*\"")[1].split("\"")[0];
                        return idToken;
                    } catch (Exception ex) {
                        System.err.println("idToken 파싱 실패: " + ex.getMessage());
                        return null;
                    }
                } else {
                    System.err.println("idToken 필드가 응답에 없습니다.");
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
