package org.newdawn.spaceinvaders.stage;

import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.SpriteStore;
import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class JupiterStage extends Stage {

    private boolean thunderActive = false;
    private long thunderTimer = 0;
    private int lightningType = 0; // 0:없음, 1:상단, 2:하단

    // 적들의 원래 속도를 저장할 맵
    private final Map<AlienEntity, Double> savedDx = new HashMap<>();

    public JupiterStage(Game game) {
        super(game, 2);
    }

    @Override
    public void init() {
        background = org.newdawn.spaceinvaders.SpriteStore.get().getSprite(getBackgroundSpriteRef()).getImage();

        // 변수 설정 (기존 값들)
        double moveSpeed = 110;
        int alienRows = 3;
        double firingChance = 0.0002;
        int startY = 60;
        String spriteRef = "sprites/alien_jupiter_big.png";

        // [수정] 반복문 삭제 -> setupAliens 한 줄로 대체!
        // (이미지, 행, 열, 시작Y, 가로간격, 세로간격, 속도, 공격확률)
        setupAliens(spriteRef, alienRows, 8, startY, 70, 50, moveSpeed, firingChance);
    }
    @Override
    protected void customizeAlien(org.newdawn.spaceinvaders.entity.AlienEntity alien) {
        alien.setHp(3);
    }

    @Override
    public void update(long delta) {
        // 1. 번개 시작 (0.5% 확률)
        if (!thunderActive && Math.random() < 0.005) {
            startThunder();
        }

        // 2. 번개 진행 중
        if (thunderActive) {
            thunderTimer -= delta;

            // 적들이 움직이지 못하게 강제로 0으로 고정 (혹시 모를 이동 방지)
            for (Entity e : getEntities()) {
                if (e instanceof AlienEntity) {
                    e.setDX(0);
                }
            }

            if (thunderTimer <= 0) {
                endThunder();
            }
        }
    }

    private void startThunder() {
        thunderActive = true;
        thunderTimer = 600; // 0.6초간 지속
        lightningType = Math.random() < 0.5 ? 1 : 2; // 상단(1) 또는 하단(2) 랜덤

        savedDx.clear();
        // 모든 적의 현재 속도를 저장하고 멈춤
        for (Entity e : getEntities()) {
            if (e instanceof AlienEntity) {
                savedDx.put((AlienEntity) e, e.getDX()); // 현재 속도 저장
                e.setDX(0); // 멈춤
            }
        }
    }

    private void endThunder() {
        thunderActive = false;
        lightningType = 0;

        // 적들의 속도 복구
        for (Entity e : getEntities()) {
            if (e instanceof AlienEntity) {
                Double originalSpeed = savedDx.get(e);
                if (originalSpeed != null) {
                    e.setDX(originalSpeed);
                } else {
                    // 만약 저장된 게 없다면 기본값으로 복구 (오류 방지)
                    e.setDX(-110);
                }
            }
        }
        savedDx.clear();
    }

    @Override
    public boolean isItemAllowed() {
        return false; // [기믹] 목성에서는 아이템이 나오지 않음 (순수 실력)
    }

    public boolean isLightningActive() {
        return thunderActive;
    }

    public int getLightningType() {
        return lightningType;
    }

    @Override
    public boolean isCompleted() {
        for (Entity e : getEntities()) {
            if (e instanceof AlienEntity) return false;
        }
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Jupiter – Lightning Storm";
    }

    @Override
    public String getBackgroundSpriteRef() {
        return "sprites/bg_jupiter.png";
    }
}