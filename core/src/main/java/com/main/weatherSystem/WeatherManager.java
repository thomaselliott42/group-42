package com.main.weatherSystem;


public class WeatherManager {

    // gets called once per global turn
    // have this called by game manger on a per-player basis then go through current tasks and apply
    // affects accordingly
    public void getWeather(String season, int numbPlayers) {
        // SUMMER
        if (season.equals("Summer")){
            System.out.println("SUMMER");
            for (int player = 1; player <= numbPlayers; player++) {
                System.out.println("\nPLAYER " + player + ":");
                System.out.println("Temperature: " + new Summer().generateTemperature() + " degrees");
                String weather = new Summer().generateWeather();
                System.out.println("Weather: " + weather);
                double[] effects = new Summer().weatherEffects(weather);
                System.out.println("Resource efficiency will be at " + (int) (effects[0] * 100) + "% of normal");
                System.out.println("Task speed will be at " + (int) (effects[1] * 100) + "% of normal");
                System.out.println("Community morale will be at " + (int) (effects[2] * 100) + "% of normal");
            }
            // AUTUMN
        } else if (season.equals("Autumn")) {
            System.out.println("AUTUMN");
            for (int player = 1; player <= numbPlayers; player++) {
                System.out.println("\nPLAYER " + player + ":");
                System.out.println("Temperature: " + new Autumn().generateTemperature() + " degrees");
                String weather = new Autumn().generateWeather();
                System.out.println("Weather: " + weather);
                double[] effects = new Autumn().weatherEffects(weather);
                System.out.println("Resource efficiency will be at " + (int) (effects[0] * 100) + "% of normal");
                System.out.println("Task speed will be at " + (int) (effects[1] * 100) + "% of normal");
                System.out.println("Community morale will be at " + (int) (effects[2] * 100) + "% of normal");
            }
            // WINTER
        } else if (season.equals("Winter")) {
            System.out.println("WINTER");
            for (int player = 1; player <= numbPlayers; player++) {
                System.out.println("\nPLAYER " + player + ":");
                System.out.println("Temperature: " + new Winter().generateTemperature() + " degrees");
                String weather = new Winter().generateWeather();
                System.out.println("Weather: " + weather);
                double[] effects = new Winter().weatherEffects(weather);
                System.out.println("Resource efficiency will be at " + (int) (effects[0] * 100) + "% of normal");
                System.out.println("Task speed will be at " + (int) (effects[1] * 100) + "% of normal");
                System.out.println("Community morale will be at " + (int) (effects[2] * 100) + "% of normal");
            }
            // SPRING
        } else if (season.equals("Spring")) {
            System.out.println("SPRING");
            for (int player = 1; player <= numbPlayers; player++) {
                System.out.println("\nPLAYER " + player + ":");
                System.out.println("Temperature: " + new Spring().generateTemperature() + " degrees");
                String weather = new Spring().generateWeather();
                System.out.println("Weather: " + weather);
                double[] effects = new Spring().weatherEffects(weather);
                System.out.println("Resource efficiency will be at " + (int) (effects[0] * 100) + "% of normal");
                System.out.println("Task speed will be at " + (int) (effects[1] * 100) + "% of normal");
                System.out.println("Community morale will be at " + (int) (effects[2] * 100) + "% of normal");
            }
        }
    }
}
