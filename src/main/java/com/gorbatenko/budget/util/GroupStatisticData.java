package com.gorbatenko.budget.util;

import com.gorbatenko.budget.model.Kind;
import com.gorbatenko.budget.model.Type;
import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

@Data
public class GroupStatisticData extends StatisticData {
    TreeMap<Type, Map<Kind, Double>> mapKind;
    Map<Kind, Long> mapKindCount;
    Map<Type, Double> mapMaxPrice;
    TreeMap<String, TreeMap<Type, Double>> totalMap;
    Double profit;
    Double spending;
    TreeMap<String, Double> dynamicRemain;
    Double remainOnStartPeriod;
    Double remainOnEndPeriod;
    GroupPeriod groupPeriod;
}
