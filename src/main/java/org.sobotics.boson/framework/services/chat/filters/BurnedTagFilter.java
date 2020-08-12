package org.sobotics.boson.framework.services.chat.filters;

import java.io.IOException;

import org.sobotics.boson.framework.model.stackexchange.Tag;
import org.sobotics.boson.framework.utils.HttpRequestUtils;

import com.google.gson.JsonObject;

public class BurnedTagFilter extends Filter<Tag> {
    private String name;

    public BurnedTagFilter(String name) {
        this.name = name;
    }

    @Override
    public boolean filter(Tag data) {

        String tagdorApi = "http://tagdor.sobotics.org/tags/";

        try {
            JsonObject json = HttpRequestUtils.get(tagdorApi + data.getName());
            return json.get("burninated").getAsBoolean();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
