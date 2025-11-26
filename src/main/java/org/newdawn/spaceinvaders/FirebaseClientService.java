package org.newdawn.spaceinvaders;

import okhttp3.Request;
import com.google.gson.Gson;
import java.util.Map;

public class FirebaseClientService {

    private static final String WEB_API_KEY = "AIzaSyCg47obQ1LAaQ1d0M87t8KGcVn4rGabDio";
    private static final String PROJECT_ID = "space-invaders-dd665";
    private static final String REALTIME_DB_URL = "https://space-invaders-dd665-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static final String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + WEB_API_KEY;
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + WEB_API_KEY;
    private static final String FIRESTORE_USERS_URL = "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents/users/";
    private static final String EXT_JSON = ".json";

    private static final String PATH_QUEUE = "matchmaking/queue/";
    private static final String PATH_COOP_QUEUE = "matchmaking/coop_queue/";
    private static final String PATH_MATCHES = "matches/";

    private final FirebaseNetworkHelper helper = new FirebaseNetworkHelper();
    private final Gson gson = new Gson();

    // --- 인증 관련 ---

    public String signUp(String email, String password) {
        String json = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"returnSecureToken\": true}";
        Request request = helper.buildPostRequest(SIGN_UP_URL, json);
        return helper.executeStringRequest(request, "localId");
    }

    public String signIn(String email, String password) {
        String json = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"returnSecureToken\": true}";
        Request request = helper.buildPostRequest(SIGN_IN_URL, json);
        return helper.executeStringRequest(request, "localId");
    }

    public boolean saveUsername(String uid, String username) {
        String url = FIRESTORE_USERS_URL + uid;
        String json = "{\"fields\": {\"nickname\": {\"stringValue\": \"" + username + "\"}}}";
        Request request = helper.buildPatchRequest(url, json);
        return helper.executeBooleanRequest(request);
    }

    public String getUsername(String uid) {
        String url = FIRESTORE_USERS_URL + uid;
        Request request = helper.buildGetRequest(url);
        return helper.executeStringRequest(request, "stringValue");
    }

    // --- 매치메이킹 관련 ---

    public boolean startMatchmaking(String uid, String nickname) {
        return registerToQueue(PATH_QUEUE, uid, nickname);
    }

    public boolean startCoopMatchmaking(String uid, String nickname) {
        return registerToQueue(PATH_COOP_QUEUE, uid, nickname);
    }

    private boolean registerToQueue(String path, String uid, String nickname) {
        String url = REALTIME_DB_URL + path + uid + EXT_JSON;
        String json = "{\"nickname\": \"" + nickname + "\", \"timestamp\": " + System.currentTimeMillis() + "}";
        Request request = helper.buildPutRequest(url, json);
        return helper.executeBooleanRequest(request);
    }

    public String findOpponent(String myUid) {
        return helper.findFirstOtherUser(REALTIME_DB_URL + "matchmaking/queue" + EXT_JSON, myUid);
    }

    public String findCoopOpponent(String myUid) {
        return helper.findFirstOtherUser(REALTIME_DB_URL + "matchmaking/coop_queue" + EXT_JSON, myUid);
    }

    public boolean deleteFromQueue(String uid) {
        return helper.executeBooleanRequest(helper.buildDeleteRequest(REALTIME_DB_URL + PATH_QUEUE + uid + EXT_JSON));
    }

    public boolean deleteFromCoopQueue(String uid) {
        return helper.executeBooleanRequest(helper.buildDeleteRequest(REALTIME_DB_URL + PATH_COOP_QUEUE + uid + EXT_JSON));
    }

    public boolean isUserInQueue(String myUid) {
        return helper.checkUserExists(REALTIME_DB_URL + PATH_QUEUE + myUid + EXT_JSON);
    }

    public boolean isUserInCoopQueue(String myUid) {
        return helper.checkUserExists(REALTIME_DB_URL + PATH_COOP_QUEUE + myUid + EXT_JSON);
    }

    public String createMatch(String player1Uid, String player2Uid) {
        String matchId = player1Uid.compareTo(player2Uid) < 0 ? player1Uid + "_" + player2Uid : player2Uid + "_" + player1Uid;
        String url = REALTIME_DB_URL + PATH_MATCHES + matchId + EXT_JSON;
        String json = "{\"player1\": \"" + player1Uid + "\", \"player2\": \"" + player2Uid + "\", \"status\": \"starting\"}";

        if (helper.executeBooleanRequest(helper.buildPutRequest(url, json))) {
            return matchId;
        }
        return null;
    }

    public String findMyMatch(String myUid) {
        String url = REALTIME_DB_URL + "matches.json?orderBy=\"player2\"&equalTo=\"" + myUid + "\"";
        return helper.findMyMatch(url);
    }

    // --- 게임 상태 동기화 ---

    public void updatePlayerState(String matchId, String playerNode, Map<String, Object> playerData) {
        String url = REALTIME_DB_URL + PATH_MATCHES + matchId + "/" + playerNode + EXT_JSON;
        String json = gson.toJson(playerData);
        helper.enqueueRequest(helper.buildPatchRequest(url, json));
    }

    public Map<String, Object> getOpponentState(String matchId, String opponentNode) {
        String url = REALTIME_DB_URL + PATH_MATCHES + matchId + "/" + opponentNode + EXT_JSON;
        return helper.executeMapRequest(helper.buildGetRequest(url));
    }

    public Map<String, Object> getMatchData(String matchId) {
        String url = REALTIME_DB_URL + PATH_MATCHES + matchId + EXT_JSON;
        return helper.executeMapRequest(helper.buildGetRequest(url));
    }
}