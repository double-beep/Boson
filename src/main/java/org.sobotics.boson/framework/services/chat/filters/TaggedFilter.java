package org.sobotics.boson.framework.services.chat.filters;

import java.util.Arrays;

import org.sobotics.boson.framework.model.stackexchange.Question;

public class TaggedFilter extends Filter<Question> {
    private String[] tags;

    public TaggedFilter(String[] tags) {
        this.tags = tags;
    }

    @Override
    public boolean filter(Question data) {
        return Arrays.stream(tags).anyMatch(e -> Arrays.asList(data.getTags()).contains(e));
    }
}
