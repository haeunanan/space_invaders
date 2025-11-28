package org.newdawn.spaceinvaders;

public class ShopManager {
    private final PlayerStats playerStats;
    private boolean shopOpen = false;

    public ShopManager(PlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    public boolean isShopOpen() {
        return shopOpen;
    }

    public void setShopOpen(boolean open) {
        this.shopOpen = open;
    }

    public void toggleShop() {
        this.shopOpen = !this.shopOpen;
    }

    public void handlePurchase(int mx, int my) {
        // 상점 패널 레이아웃 상수 (Game/UIRenderer와 동일하게 맞춤)
        int overlayX = 40;
        int overlayY = 40;
        int overlayW = 720;
        int overlayH = 520;
        int pad = 20;
        int panelW = (overlayW - pad * 4) / 3;
        int panelH = overlayH - 120;
        int panelY = overlayY + 60; // 제목 아래부터

        // 3개의 아이템 슬롯 체크
        for (int i = 0; i < 3; i++) {
            int px = overlayX + pad + i * (panelW + pad);
            int py = panelY;

            // 클릭 좌표 확인
            if (mx >= px && mx <= px + panelW && my >= py && my <= py + panelH) {
                buyItem(i);
            }
        }
    }

    private void buyItem(int index) {
        switch (index) {
            case 0:
                tryBuyAttackSpeed();
                break;
            case 1:
                tryBuyMoveSpeed();
                break;
            case 2:
                tryBuyMissileCount();
                break;
            default:
                // 예상치 못한 인덱스가 들어올 경우 아무 작업도 하지 않음
                break;
        }
    }
    private void tryBuyAttackSpeed() {
        if (playerStats.getAttackLevel() < Constants.MAX_UPGRADES &&
                playerStats.spendCoins(Constants.UPGRADE_COST)) {
            playerStats.increaseAttackLevel();
        }
    }
    private void tryBuyMoveSpeed() {
        if (playerStats.getMoveLevel() < Constants.MAX_UPGRADES &&
                playerStats.spendCoins(Constants.UPGRADE_COST)) {
            playerStats.increaseMoveLevel();
        }
    }
    private void tryBuyMissileCount() {
        if (playerStats.getMissileLevel() < Constants.MAX_UPGRADES &&
                playerStats.spendCoins(Constants.UPGRADE_COST)) {
            playerStats.increaseMissileLevel();
            playerStats.setMissileCount(Math.min(5, playerStats.getMissileCount() + 1));
        }
    }
}
