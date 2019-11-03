package com.gorbatenko.budget.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import com.gorbatenko.budget.web.charts.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ChartUtil {
    final static private float TRANSPARENT_VALUE = 1.0f;

    public static String createMdbChart(ChartType chartType, Type type, TreeMap<Type, Map<Kind, Double>> data){
        if(data.get(type) == null) {
            return "";
        }
        ChartData chartData = createChartData(chartType, type, data.get(type));
        Map<String, Object> options = createOptions(chartType);
        MdbChart mdbChart = new MdbChart(chartType.getValue(), chartData, options);
        return mdbChartToJSON(mdbChart);
    }

    private static String mdbChartToJSON(MdbChart mdbChart){
        ObjectMapper mapper = new ObjectMapper();
        //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            return mapper.writeValueAsString(mdbChart);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static Map<String, Object> createOptions(ChartType chartType) {
        Map<String, Object> result = new HashMap<>();
        switch (chartType) {
            case HORIZONTALBAR : {
                /*
                Map<String, Boolean> beginAtZero = new HashMap<>();
                beginAtZero.put("beginAtZero", true);

                Map<String, Map<String, Boolean>> ticks = new HashMap<>();
                ticks.put("ticks", beginAtZero);

                Map<String, Map<String, Map<String, Boolean>>> xAxes = new HashMap<>();
                xAxes.put("xAxes", ticks);

                result.put("scales", xAxes);*/
                break;
            }
            case DOUGHNUT: {
                result.put("responsive", true);
                break;
            }
        }
        return result;
    }

    private static ChartData createChartData(ChartType chartType, Type type, Map<Kind, Double> data) {
        String[] labels = data.keySet().stream().map(Kind::getName).toArray(String[]::new);
        Map<String, Double> map = new HashMap<>();
        for (Map.Entry<Kind, Double> entry : data.entrySet()) {
            map.put(entry.getKey().getName(), entry.getValue());
        }
        return new ChartData(labels, createChartDataset(chartType, type, labels, map));
    }

    private static ChartDatasets[] createChartDataset(ChartType chartType, Type type, String[] labels, Map<String, Double> map) {
        String[] data = createData(labels, map);
        String[] backgroundColor;

        ChartDatasets[] result = new ChartDatasets[1];

        switch (chartType) {
            case HORIZONTALBAR:
                backgroundColor = createRGB(map.size(), true);
                String[] borderColor = createRGB(map.size(), false);
                result[0] = new ChartDatasetsHorizont(type.getValue(), data, false, backgroundColor, borderColor);
                break;
            case DOUGHNUT:
                backgroundColor = createRGB(map.size(), false);
                String[] hoverBackgroundColor = createRGB(map.size(), false);
                result[0] = new ChartDatasetsDoughnut(type.getValue(), data, backgroundColor, hoverBackgroundColor);
                break;
        }
        return result;
    }

    private static String[] createData(String[] labels, Map<String, Double> map) {
        String[] result = new String[map.size()];
        for(int i = 0; i < labels.length; i++) {
            result[i] = map.get(labels[i]).toString();
        }
        return result;
    }

    private static String[] createRGB(int count, boolean isTransparent) {
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = createRGB(isTransparent);
        }
        return result;
    }

    private static String createRGB(boolean isTransparent) {
        return "rgb" + (isTransparent ? "a" : "") + "(" +
                generateRGBColor() + "," +
                generateRGBColor() + "," +
                generateRGBColor() + (isTransparent ? "," + TRANSPARENT_VALUE : "") + ")";
    }

    private static int generateRGBColor() {
        return 1 + (int) (Math.random() * 254);
    }
}

