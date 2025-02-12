package com.xhf.leetcode.plugin.model;

import com.intellij.openapi.project.Project;

public interface DeepCodingQuestion {
    String getTitleSlug();
    Question toQuestion(Project project);
}
