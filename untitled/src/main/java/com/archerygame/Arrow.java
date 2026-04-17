package com.archerygame;

public class Arrow extends Thread {
    private int x;
    private final int y;
    private final int panelWidth;
    private final GameController controller;
    private static final int STEP = 10;

    public Arrow(int startX, int startY, GameController controller, int panelWidth) {
        this.x = startX;
        this.y = startY;
        this.controller = controller;
        this.panelWidth = panelWidth;
    }

    @Override
    public void run() {
        while (controller.isGameRunning() && !controller.isPaused() && x < panelWidth - 20) {
            x += STEP;
            controller.moveArrow(x);
            // Если попали – выходим из цикла (стрела уничтожается)
            if (controller.isArrowHit()) {
                break;
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                break;
            }
        }
        controller.finishArrow();
    }
}