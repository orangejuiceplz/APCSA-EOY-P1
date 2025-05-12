package com.DLlabs.app;

import com.github.prominence.openweathermap.api.OpenWeatherMapManager;

public class Test {
    public static void main(String[] args) {
        String secret = System.getenv("APIKEY_CODESPACE");
        OpenWeatherMapManager manager = new OpenWeatherMapManager(secret);
        // Use manager to query weather here
    }
}