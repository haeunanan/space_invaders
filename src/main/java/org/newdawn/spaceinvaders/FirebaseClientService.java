package org.newdawn.spaceinvaders;

import okhttp3.*; // OkHttp 관련 클래스들을 모두 import
import java.io.IOException;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FirebaseClientService {

    private static final String WEB_API_KEY = "AIzaSyCg47obQ1LAaQ1d0M87t8KGcVn4rGabDio";
    private static final String PROJECT_ID = "space-invaders-dd665";
    private static final String REALTIME_DB_URL = "https://space-invaders-dd665-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String JSON_TYPE_STR = "application/json; charset=utf-8";
    private static final String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + WEB_API_KEY;
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + WEB_API_KEY;
    private static final String FIRESTORE_USERS_URL = "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents/users/";
    private static final String EXT_JSON = ".json";
    private static final String PATH_QUEUE = "matchmaking/queue/";
    private static final String PATH_COOP_QUEUE = "matchmaking/coop_queue/";
    private static final String PATH_MATCHES = "matches/";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public String signUp(String email, String password) {
        String json = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"returnSecureToken\": true}";
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));
        Request request = new Request.Builder().url(SIGN_UP_URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Sign Up Response Code :: " + response.code());
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                if (responseBody.contains("\"localId\"")) {
                    return responseBody.split("\"localId\": \"")[1].split("\"")[0];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveUsername(String uid, String username) {
        String url = FIRESTORE_USERS_URL + uid;
        String json = "{\"fields\": {\"nickname\": {\"stringValue\": \"" + username + "\"}}}";
        RequestBody body = RequestBody.create(json, MediaType.get((JSON_TYPE_STR)));
        Request request = new Request.Builder().url(url).patch(body).build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Save Username Response Code :: " + response.code());
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String signIn(String email, String password) {
        String json = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"returnSecureToken\": true}";
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));
        Request request = new Request.Builder().url(SIGN_IN_URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Sign In Response Code :: " + response.code());
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                if (responseBody.contains("\"localId\"")) {
                    return responseBody.split("\"localId\": \"")[1].split("\"")[0];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUsername(String uid) {
        String url = FIRESTORE_USERS_URL + uid;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Get Username Response Code :: " + response.code());
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                if (responseBody.contains("\"nickname\"")) {
                    return responseBody.split("\"stringValue\": \"")[1].split("\"")[0];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Realtime Database의 매치메이킹 큐에 현재 사용자를 등록합니다.
     * @param uid 사용자의 고유 ID
     * @param nickname 사용자의 닉네임
     * @return 등록 성공 시 true, 실패 시 false
     */
    public boolean startMatchmaking(String uid, String nickname) {
        String url = REALTIME_DB_URL + PATH_QUEUE + uid + EXT_JSON;

        String json = "{\"nickname\": \"" + nickname + "\", \"timestamp\": " + System.currentTimeMillis() + "}";
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));

        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Start Matchmaking Response Code :: " + response.code());
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 매치메이킹 큐에서 나를 제외한 다른 사용자를 찾습니다.
     * @param myUid 검색에서 제외할 나의 UID
     * @return 찾은 상대방의 UID, 없으면 null
     */
    public String findOpponent(String myUid) {
        String url = REALTIME_DB_URL + "matchmaking/queue" + EXT_JSON;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();

                // 간단한 파싱: "큰따옴표로 둘러싸인 키 값"들을 찾습니다.
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"([a-zA-Z0-9_-]+)\":\\s*\\{");
                java.util.regex.Matcher matcher = pattern.matcher(responseBody);

                while (matcher.find()) {
                    String opponentUid = matcher.group(1);
                    if (!opponentUid.equals(myUid)) {
                        // 나 자신이 아닌 다른 사용자를 찾았으면 즉시 반환
                        return opponentUid;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 상대방을 찾지 못함
    }
    /**
     * 두 플레이어를 위한 새로운 매치(게임 방)를 생성합니다.
     * @param player1Uid 방장이 될 플레이어의 UID
     * @param player2Uid 참여자가 될 플레이어의 UID
     * @return 생성된 매치의 고유 ID, 실패 시 null
     */
    public String createMatch(String player1Uid, String player2Uid) {
        // 매치 ID를 두 UID를 정렬하여 조합 (항상 동일한 ID가 생성되도록)
        String matchId = player1Uid.compareTo(player2Uid) < 0 ? player1Uid + "_" + player2Uid : player2Uid + "_" + player1Uid;
        String url = REALTIME_DB_URL + PATH_MATCHES + matchId + EXT_JSON;

        // 게임 방의 초기 상태 데이터
        String json = "{\"player1\": \"" + player1Uid + "\", \"player2\": \"" + player2Uid + "\", \"status\": \"starting\"}";
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));
        Request request = new Request.Builder().url(url).put(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return matchId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 매치메이킹 큐에서 특정 사용자를 삭제합니다.
     * @param uid 삭제할 사용자의 UID
     * @return 삭제 성공 시 true
     */
    public boolean deleteFromQueue(String uid) {
        String url = REALTIME_DB_URL + PATH_QUEUE + uid + EXT_JSON;
        // DELETE 요청
        Request request = new Request.Builder().url(url).delete().build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 내가 아직 매치메이킹 큐에 있는지 확인합니다.
     * @param myUid 나의 UID
     * @return 큐에 있으면 true, 없으면 false
     */
    public boolean isUserInQueue(String myUid) {
        String url = REALTIME_DB_URL + PATH_QUEUE + myUid + EXT_JSON;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            // response.body()는 한 번만 읽을 수 있으므로 변수에 저장합니다.
            ResponseBody body = response.body();
            if (body != null) {
                String bodyString = body.string();
                // bodyString이 "null" 문자열이 아니면 데이터가 존재하는 것입니다.
                return !bodyString.equalsIgnoreCase("null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // 오류가 발생했거나 응답이 없을 경우
    }

    /**
     * 내가 참여자로 포함된 'matches'를 찾습니다.
     * @param myUid 나의 UID
     * @return 찾은 매치 ID, 없으면 null
     */
    public String findMyMatch(String myUid) {
        // Firebase Realtime DB 쿼리: player2 필드의 값이 내 uid와 같은 데이터를 찾습니다.
        String url = REALTIME_DB_URL + "matches.json?orderBy=\"player2\"&equalTo=\"" + myUid + "\"";
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                // 응답이 비어있지 않다면({"}"가 아니라면) 매치를 찾은 것입니다.
                if (responseBody != null && !responseBody.equals("{}")) {
                    // 응답에서 매치 ID (가장 바깥쪽 키)를 추출합니다.
                    return responseBody.split("\"")[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void updatePlayerState(String matchId, String playerNode, Map<String, Object> playerData) {
        String url = REALTIME_DB_URL + PATH_MATCHES + matchId + "/" + playerNode + EXT_JSON;

        String json = gson.toJson(playerData);
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));
        Request request = new Request.Builder().url(url).patch(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                System.err.println("Update Player State Failed: " + e.getMessage());
            }
            @Override public void onResponse(Call call, Response response) throws IOException { response.close(); }
        });
    }

    public Map<String, Object> getOpponentState(String matchId, String opponentNode) {
        String url = REALTIME_DB_URL + PATH_MATCHES + matchId + "/" + opponentNode + EXT_JSON;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                java.lang.reflect.Type type = new TypeToken<Map<String, Object>>(){}.getType();
                return gson.fromJson(responseBody, type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 특정 매치 ID의 상세 정보를 가져옵니다.
     * @param matchId 조회할 매치의 ID
     * @return 매치 정보가 담긴 Map, 실패 시 null
     */
    public Map<String, Object> getMatchData(String matchId) {
        String url = REALTIME_DB_URL + PATH_MATCHES + matchId + EXT_JSON;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                java.lang.reflect.Type type = new TypeToken<Map<String, Object>>(){}.getType();
                return gson.fromJson(responseBody, type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // FirebaseClientService.java 에 추가

    // 1. 협동 모드 대기열 등록
    public boolean startCoopMatchmaking(String uid, String nickname) {
        String url = REALTIME_DB_URL + PATH_COOP_QUEUE + uid + EXT_JSON; // coop_queue 사용
        String json = "{\"nickname\": \"" + nickname + "\", \"timestamp\": " + System.currentTimeMillis() + "}";
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));
        Request request = new Request.Builder().url(url).put(body).build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. 협동 모드 상대 찾기
    public String findCoopOpponent(String myUid) {
        String url = REALTIME_DB_URL + "matchmaking/coop_queue.json"; // coop_queue 사용
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"([a-zA-Z0-9_-]+)\":\\s*\\{");
                java.util.regex.Matcher matcher = pattern.matcher(responseBody);

                while (matcher.find()) {
                    String opponentUid = matcher.group(1);
                    if (!opponentUid.equals(myUid)) {
                        return opponentUid;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. 협동 모드 대기열에서 삭제
    public boolean deleteFromCoopQueue(String uid) {
        String url = REALTIME_DB_URL + PATH_COOP_QUEUE + uid + EXT_JSON; // coop_queue 사용
        Request request = new Request.Builder().url(url).delete().build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. 협동 모드 대기열 확인 (참가자용)
    public boolean isUserInCoopQueue(String myUid) {
        String url = REALTIME_DB_URL + PATH_COOP_QUEUE + myUid + EXT_JSON;
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body != null) {
                String bodyString = body.string();
                return !bodyString.equalsIgnoreCase("null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}