package com.xhf.leetcode.plugin.review.backend.util;

/**
 * @author 文艺倾年
 */
public enum AlgorithmType {
    FREE_SPACED_REPETITION_SCHEDULER("fsrs", 4);
    /**
     * Konstruktor für das Enum
     * @param databaseTable der Name der Datenbanktabelle
     * @param ratingButtons die Anzahl an Bewertungsbuttons
     */
    AlgorithmType(String databaseTable, int ratingButtons) {
        this.databaseTable = databaseTable;
        this.ratingButtons = ratingButtons;
    }

    private String databaseTable;
    private int ratingButtons;

    /**
     * Erhalten des Datenbanktabellennamens für den Algorithmus
     * @return der Datenbanktabellenname
     */
    public String getDatabaseTable() {
        return this.databaseTable;
    }

    /**
     * Erhalten der Anzahl an Bewertungsbuttons für den Algorithmus
     * @return die Anzahl an Bewertungsbuttons
     */
    public int getRatingButtons() {
        return this.ratingButtons;
    }
}
