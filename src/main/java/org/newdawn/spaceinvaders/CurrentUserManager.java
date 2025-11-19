package org.newdawn.spaceinvaders;

/**
 * 현재 로그인한 사용자의 정보를 관리하는 싱글턴(Singleton) 클래스.
 * 게임 내 어디서든 이 클래스를 통해 로그인 상태와 사용자 정보에 접근할 수 있다.
 */
public class CurrentUserManager {
    private static CurrentUserManager instance;
    private String uid;
    private String nickname;
    private CurrentUserManager() {}

    public static CurrentUserManager getInstance() {
        if (instance == null) {
            instance = new CurrentUserManager();
        }
        return instance;
    }
    /**
     * 로그인 성공 시 호출되어 사용자 정보 저장
     * @param uid 사용자의 고유 ID
     * @param nickname 사용자의 닉네임
     */
    public void login(String uid, String nickname) {
        this.uid = uid;
        this.nickname = nickname;
    }
    /**
     * 로그아웃 시 호출되어 저장된 사용자 정보를 모두 지움
     */
    public void logout() {
        this.uid = null;
        this.nickname = null;
    }

    public String getUid() {
        return uid;
    }

    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    /**
     * 현재 로그인 상태인지 확인
     * @return 로그인 상태이면 true, 아니면 false
     */
    public boolean isLoggedIn() {
        return uid != null && !uid.isEmpty();
    }
}