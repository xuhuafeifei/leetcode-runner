package com.xhf.leetcode.plugin.model;

import com.intellij.application.Topics;
import java.util.List;

public class SkillSet {

    private List<LangLevels> langLevels;
    private List<Topics> topics;
    private List<TopicAreaScore> topicAreaScores;

    public List<LangLevels> getLangLevels() {
        return langLevels;
    }

    public void setLangLevels(List<LangLevels> langLevels) {
        this.langLevels = langLevels;
    }

    public List<Topics> getTopics() {
        return topics;
    }

    public void setTopics(List<Topics> topics) {
        this.topics = topics;
    }

    public List<TopicAreaScore> getTopicAreaScores() {
        return topicAreaScores;
    }

    public void setTopicAreaScores(List<TopicAreaScore> topicAreaScores) {
        this.topicAreaScores = topicAreaScores;
    }
}
