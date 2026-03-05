package com.xhf.leetcode.plugin.review.backend.entity;

import java.util.Objects;

/**
 * 卡片实体类，对应数据库中的cards表
 */
public class Card {

    private int cardId;
    private String front;
    private String back;
    private long created;
    private float stability;
    private float difficulty;
    private int elapsedDays;
    private int repetitions;
    private int dayInterval;
    private int state;
    private long nextRepetition;
    private long lastReview;

    public Card() {
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public float getStability() {
        return stability;
    }

    public void setStability(float stability) {
        this.stability = stability;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public int getElapsedDays() {
        return elapsedDays;
    }

    public void setElapsedDays(int elapsedDays) {
        this.elapsedDays = elapsedDays;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getDayInterval() {
        return dayInterval;
    }

    public void setDayInterval(int dayInterval) {
        this.dayInterval = dayInterval;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getNextRepetition() {
        return nextRepetition;
    }

    public void setNextRepetition(long nextRepetition) {
        this.nextRepetition = nextRepetition;
    }

    public long getLastReview() {
        return lastReview;
    }

    public void setLastReview(long lastReview) {
        this.lastReview = lastReview;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Card card = (Card) o;
        return cardId == card.cardId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId);
    }
}