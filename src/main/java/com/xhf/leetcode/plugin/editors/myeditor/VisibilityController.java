package com.xhf.leetcode.plugin.editors.myeditor;

import javax.swing.Timer;

public abstract class VisibilityController {

    private static final int RETENTION_DELAY = 1500;
    private static final int TRANSITION_DELAY = 50;
    private static final int TRANSITION_VALUE = 20;
    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = 0;
    private final Timer transitionTimer = new Timer(TRANSITION_DELAY, null);
    private final Timer retentionTimer = new Timer(RETENTION_DELAY, null);
    private boolean isDisposed = false;
    private int opacityCounter = 0;
    private State state = State.INVISIBLE;

    {
        transitionTimer.setRepeats(true);
        transitionTimer.addActionListener(e -> refresh());

        retentionTimer.setRepeats(false);
        retentionTimer.addActionListener(e -> {
            if (isRetention()) {
                scheduleShow();
            } else {
                scheduleHide();
            }
        });
    }

    public float getOpacity() {
        synchronized (this) {
            return opacityCounter / 100.0f;
        }
    }

    public void scheduleHide() {
        synchronized (this) {
            if (state != State.INVISIBLE) {
                state = State.HIDING;
                propagateStateChanges();
            }
        }
    }

    public void scheduleShow() {
        synchronized (this) {
            if (state != State.VISIBLE) {
                state = State.SHOWING;
                propagateStateChanges();
            }
        }
    }

    public void dispose() {
        synchronized (this) {
            isDisposed = true;
            transitionTimer.stop();
            retentionTimer.stop();
        }
    }

    protected abstract boolean isRetention();

    protected abstract void repaintComponent();

    protected abstract void setVisibleFlag(boolean visible);

    private void refresh() {
        synchronized (this) {
            if (state == State.HIDING) {
                opacityCounter -= TRANSITION_VALUE;
            } else if (state == State.SHOWING) {
                opacityCounter += TRANSITION_VALUE;
            }

            opacityCounter = Math.min(MAX_VALUE, Math.max(MIN_VALUE, opacityCounter));

            if (opacityCounter == MAX_VALUE) {
                state = State.VISIBLE;
            } else if (opacityCounter == MIN_VALUE) {
                state = State.INVISIBLE;
            }

            propagateStateChanges();
        }
    }

    private void propagateStateChanges() {
        if (isDisposed) {
            return;
        }

        switch (state) {
            case SHOWING:
            case HIDING:
                transitionTimer.start();
                break;
            default:
                transitionTimer.stop();
        }

        if (state == State.VISIBLE && isAutoHide()) {
            retentionTimer.restart();
        } else {
            retentionTimer.stop();
        }

        setVisibleFlag(state != State.INVISIBLE);
        repaintComponent();
    }

    protected abstract boolean isAutoHide();

    private enum State {INVISIBLE, VISIBLE, HIDING, SHOWING}
}
