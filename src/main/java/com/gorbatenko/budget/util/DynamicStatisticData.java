package com.gorbatenko.budget.util;

import lombok.Data;

import java.util.TreeMap;

@Data
public class DynamicStatisticData extends StatisticData {
    String positionName;
    double positionSum;
    TreeMap<String, Double> mapKindSort;
    GroupPeriod groupPeriod;
}
