package com.main;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

public class GameState {
    private static GameState instance;

    public float textScale;
    private boolean colourBlind;
    private Camera uiCamera;
    private Viewport viewport;
    private String currentScreen;

    // Private constructor to prevent instantiation
    private GameState() {
        this.textScale = 1.0f;
        this.colourBlind = false;
    }

    // Static method to get the single instance of the class
    public static GameState getInstance() {
        if (instance == null) {
            synchronized (GameState.class) {
                if (instance == null) {
                    instance = new GameState();
                }
            }
        }
        return instance;
    }
    public String getCurrentScreen() {
        return currentScreen;
    }


    public void setCurrentScreen(String currentScreen) {
        this.currentScreen = currentScreen;
    }

    public void setUiCamera(Camera camera) {
        this.uiCamera = camera;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public Camera getUiCamera() {
        return uiCamera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public boolean isColourBlind() {
        return colourBlind;
    }

    public void setColourBlind(){
        colourBlind = !colourBlind;
    }


    public float getTextScale() {
        return textScale;
    }

    public void setTextScale(float textScale) {
        this.textScale = textScale;
    }

}
