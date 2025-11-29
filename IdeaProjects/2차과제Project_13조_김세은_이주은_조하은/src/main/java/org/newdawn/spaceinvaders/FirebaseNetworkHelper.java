package org.newdawn.spaceinvaders;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirebaseNetworkHelper {
    // ... (기존 필드 및 Request 생성 메서드들은 그대로 유지) ...
    private static final String JSON_TYPE_STR = "application/json; charset=utf-8";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public Request buildPostRequest(String url, String json) {
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));
        return new Request.Builder().url(url).post(body).build();
    }
    public Request buildPutRequest(String url, String json) {
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));
        return new Request.Builder().url(url).put(body).build();
    }
    public Request buildPatchRequest(String url, String json) {
        RequestBody body = RequestBody.create(json, MediaType.get(JSON_TYPE_STR));
        return new Request.Builder().url(url).patch(body).build();
    }
    public Request buildDeleteRequest(String url) {
        return new Request.Builder().url(url).delete().build();
    }
    public Request buildGetRequest(String url) {
        return new Request.Builder().url(url).get().build();
    }

    // ... (getResponseBody 메서드 기존 유지) ...
    private String getResponseBody(Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==========================================
    // [리팩토링] findFirstOtherUser 수정
    // ==========================================
    public String findFirstOtherUser(String url, String myUid) {
        String responseBody = getResponseBody(buildGetRequest(url));

        // [변경] Null 체크를 여기서 수행하여 parseOtherUserUid의 부담을 줄임
        if (responseBody == null) {
            return null;
        }

        return parseOtherUserUid(responseBody, myUid);
    }

    // ==========================================
    // [리팩토링] parseOtherUserUid 수정 (복잡도 감소)
    // ==========================================
    private String parseOtherUserUid(String json, String myUid) {
        // 이제 json은 null이 아님을 보장받음
        Pattern pattern = Pattern.compile("\"([a-zA-Z0-9_-]+)\":\\s*\\{");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String foundUid = matcher.group(1);
            // 조건문이 하나만 남게 되어 구조가 단순해짐
            if (!foundUid.equals(myUid)) {
                return foundUid;
            }
        }
        return null;
    }

    // ==========================================
    // [리팩토링] executeStringRequest 수정 (동일한 원리 적용)
    // ==========================================
    public String executeStringRequest(Request request, String keyToFind) {
        String responseBody = getResponseBody(request);

        // [변경] Null 체크를 여기서 수행
        if (responseBody == null) {
            return null;
        }

        // 키가 없으면 전체 반환
        if (keyToFind == null) {
            return responseBody;
        }

        return extractValueFromJson(responseBody, keyToFind);
    }

    private String extractValueFromJson(String json, String key) {
        if (json.contains("\"" + key + "\"")) {
            return json.split("\"" + key + "\": \"")[1].split("\"")[0];
        }
        return null;
    }

    // ... (나머지 executeBooleanRequest, executeMapRequest 등은 기존 유지) ...
    public boolean executeBooleanRequest(Request request) {
        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> executeMapRequest(Request request) {
        String responseBody = getResponseBody(request);
        if (responseBody != null) {
            java.lang.reflect.Type type = new TypeToken<Map<String, Object>>(){}.getType();
            return gson.fromJson(responseBody, type);
        }
        return null;
    }

    public void enqueueRequest(Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { System.err.println("Request Failed: " + e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException { response.close(); }
        });
    }

    public boolean checkUserExists(String url) {
        String responseBody = getResponseBody(buildGetRequest(url));
        return responseBody != null && !responseBody.equalsIgnoreCase("null");
    }

    public String findMyMatch(String url) {
        String responseBody = getResponseBody(buildGetRequest(url));
        if (responseBody != null && !responseBody.equals("{}") && responseBody.contains("\"")) {
            return responseBody.split("\"")[1];
        }
        return null;
    }
}