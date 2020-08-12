package org.sobotics.boson.framework.services.others;

import java.io.IOException;
import java.util.List;

import org.sobotics.boson.framework.model.heatdetector.request.Content;
import org.sobotics.boson.framework.model.heatdetector.request.HeatDetectorRequest;
import org.sobotics.boson.framework.model.heatdetector.response.HeatDetectorResponse;
import org.sobotics.boson.framework.model.heatdetector.response.Result;
import org.sobotics.boson.framework.utils.HttpRequestUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class HeatDetectorService {

    private final String URL = "https://jdd.cloud/heatdetector-api/api/classify/";

    private int limit;

    private String domain;


    public HeatDetectorService(int limit, String domain) {
        this.limit = limit;
        this.domain = domain;
    }

    public List<Result> getHeatDetectorData(List<Content> inputData) {

        HeatDetectorRequest request = new HeatDetectorRequest();
        request.setContents(inputData);
        request.setDomain(domain);
        request.setMinScore(limit);
        JsonObject returnData;

        System.err.println(request);

        try {
            returnData = HttpRequestUtils.postJson(URL, new Gson().toJson(request));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        handleBackoff(returnData);

        HeatDetectorResponse response = new Gson().fromJson(returnData, HeatDetectorResponse.class);

        // TODO: Add Back off logic here

        System.err.println(response);

        return response.getResult();

    }

    public int getLimit() {
        return limit;
    }

    private void handleBackoff(JsonObject root) {
        if (root.has("backOff")) {
            int backoff = root.get("backOff").getAsInt();
            // System.out.println("Backing off for " + backoff + " milliseconds");
            try {
                Thread.sleep(backoff);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
