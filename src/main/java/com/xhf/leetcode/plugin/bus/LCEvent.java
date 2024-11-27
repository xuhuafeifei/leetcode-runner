package com.xhf.leetcode.plugin.bus;

import com.intellij.openapi.project.Project;

public class LCEvent {
    private Class<? extends LCEvent> topic;
    private Project project;

    public LCEvent(Class<? extends LCEvent> topic, Project project) {
        this.topic = topic;
        this.project = project;
    }

    public Class<? extends LCEvent> getTopic() {
        return topic;
    }

    public void setTopic(Class<? extends LCEvent> topic) {
        this.topic = topic;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}