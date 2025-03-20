package com.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.Tooltip;
import com.main.tooltips.TooltipPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// screen is off click on the screen zooms in and then can interact with it ect
// get emails and also information on weather can be accessed there
// emails are the way to get new tasks and you can choose to deposit recourse in it as well


public class MakersCenter implements Screen {
    private Screen previousScreen;
    private Stage stage;
    private Texture backgroundTexture;
    private ShapeRenderer shapeRenderer;
    private ComputerScreen computerScreen;

    // camera zoom into the screen
    private boolean zooming = false;
    private float zoomTarget = 0.5f;
    private float zoomSpeed = 1.0f;
    private boolean zoomedIn = false;

    public MakersCenter(Screen previousScreen) {
        this.previousScreen = previousScreen;
        TutorialManager.getInstance().startTutorial("makersCenter");
    }

    @Override
    public void show() {
        GameState.getInstance().setCurrentScreen("MC");
        SoundManager.getInstance().pauseMusic("background");
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        backgroundTexture = new Texture(Gdx.files.internal("ui/makersCentreBackground.png"));
        Image background = new Image(backgroundTexture);
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(background);


        shapeRenderer = new ShapeRenderer();
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                if (!zooming && !zoomedIn) {
                    zooming = true;
                    stage.getCamera().viewportWidth *= zoomTarget;
                    stage.getCamera().viewportHeight *= zoomTarget;
                    stage.getCamera().position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f+10, 0);
                    stage.getCamera().update();

                    GameState.getInstance().setCurrentScreen("MCZ");
                    computerScreen = new ComputerScreen();
                    computerScreen.build();
                    Group computerUI = computerScreen.getContainer();
                    computerUI.setPosition(
                        (Gdx.graphics.getWidth() - computerUI.getWidth()) / 2,
                        (Gdx.graphics.getHeight() - computerUI.getHeight()) / 2
                    );
                    stage.addActor(computerUI);

                    zoomedIn = true;
                    zooming = false;

                }
                return true;
            }
        });

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {

                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE && !TutorialManager.getInstance().isDelayTimer()) {
                    GameState.getInstance().setCurrentScreen("MS");
                    ((Game) Gdx.app.getApplicationListener()).setScreen(previousScreen);
                    zooming = false;
                    zoomedIn = false;
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void render(float delta) {
        Tooltip.getInstance().clear();
        TutorialManager.getInstance().update();


        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        TutorialManager.getInstance().render();
        Tooltip.getInstance().render(GameState.getInstance().getUiCamera(), GameState.getInstance().getViewport().getWorldWidth(), GameState.getInstance().getViewport().getWorldHeight());
        handleSettings();
    }

    private void handleSettings(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            zooming = false;
            zoomedIn = false;
            ((Game) Gdx.app.getApplicationListener()).setScreen(new Settings(((Game) Gdx.app.getApplicationListener()).getScreen()));

        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        shapeRenderer.dispose();
    }
}
