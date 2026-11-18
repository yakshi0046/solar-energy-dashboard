package com.solardash.rules;

/** One hourly data point, free of any persistence types so the rules stay easy to unit test. */
public record Sample(double powerKw, double expectedKw) {}
