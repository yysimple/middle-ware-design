package com.simple.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-27 21:16
 **/
public class Constants {

    public static Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
}
