package com.fudan.lab5.ai;

import java.util.List;

@FunctionalInterface
public interface DeepSeekClient {
    List<String> generateDrafts(String prompt);
}
