package com.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameEndScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private ScrollPane scrollPane;
    private Table timeline;
    private BitmapFont font;

    private float scrollSpeed = 100f;
    private float totalScrollWidth;

    // Player stats UI
    private Table playerStatsPanel;
    private Label playerStatsLabel;

    @Override
    public void show() {

        font = new BitmapFont();
        font.setColor(Color.WHITE);
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(20);
        stage.addActor(root);

        // Title
        Label title = new Label("Finished... You Won!!!", skin, "default");
        title.setFontScale(2);
        root.add(title).colspan(3).center().padBottom(20);
        root.row();

        // Group Info
        Label groupInfo = new Label("Group 42\nDeclan, Tom, Josh, Charles, Dan, Paulius", skin);
        root.add(groupInfo).colspan(3).center().padBottom(20);
        root.row();

        // Player Stats with dynamic buttons
        createPlayerStatsPanel();
        root.add(playerStatsPanel).pad(10);

        // Group Stats Panel
        root.add(createStatsPanel("Group Stats")).pad(10);

        // Game Stats Panel
        root.add(createStatsPanel("Game Stats")).pad(10);
        root.row();

        // Timeline setup
        timeline = createYearTimeline();
        totalScrollWidth = 4 * (300 + 30);

        Table wrappedTimeline = new Table();
        wrappedTimeline.add(timeline);
        wrappedTimeline.add(createYearTimeline()); // duplicate for wrapping

        scrollPane = new ScrollPane(wrappedTimeline, skin);
        scrollPane.setScrollingDisabled(false, true);
        scrollPane.setOverscroll(false, false);
        scrollPane.setFadeScrollBars(true);
        scrollPane.setScrollBarPositions(false, false);
        scrollPane.setForceScroll(false, false);
        scrollPane.setScrollX(0);
        scrollPane.setTouchable(Touchable.disabled); // Prevents any player interaction


        // Add scrolling panel to bottom
        root.row();
        root.add(scrollPane).colspan(3)
            .height(200)
            .width(Gdx.graphics.getWidth() - 100)
            .expandY()
            .bottom()
            .padBottom(20);

        TextButton restartButton = new TextButton("Restart", new TextButton.TextButtonStyle(null, null, null, font));
        restartButton.getLabel().setFontScale(2f);
        restartButton.setColor(Color.GREEN);
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Dispose of the current screen
                dispose();
                Tooltip.getInstance().resetInstance();
                // Create a new MainMenuScreen
                ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenuScreen());
            }
        });
        root.add(restartButton).colspan(3).center().padBottom(20);
    }

    private void createPlayerStatsPanel() {
        playerStatsPanel = new Table(skin);
        playerStatsPanel.setBackground("default-round");

        Table buttonRow = new Table(skin);
        int numPlayers = PlayerManager.getInstance().getPlayers().size();

        for (int i = 0; i < numPlayers; i++) {
            final int playerIndex = i;
            TextButton button = new TextButton("Player " + (i + 1), skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    updatePlayerStats(playerIndex);
                }
            });
            buttonRow.add(button).pad(5);
        }

        playerStatsLabel = new Label("Select a player to view stats.", skin);
        playerStatsLabel.setAlignment(Align.topLeft);
        playerStatsLabel.setWrap(true);
        playerStatsLabel.setFontScale(1.0f);

        playerStatsPanel.add(buttonRow).row();
        playerStatsPanel.add(playerStatsLabel).width(250).pad(10).left().top();
    }

    private void updatePlayerStats(int index) {
        // Sample data — replace with real player object data
        String[] achievements = {
            "• Completed 5 modules\n• Opened a local café",
            "• Mentored 3 peers\n• Launched online store",
            "• Built community hub\n• Organized workshop",
            "• Secured seed funding\n• Expanded operations"
        };

        String[] mistakes = {
            "• Ignored customer feedback\n• Overspent early budget",
            "• Failed to file taxes\n• Hired underqualified staff",
            "• Skipped marketing phase\n• Product recall incident",
            "• Late rent payments\n• Mismanaged inventory"
        };

        String info = "Achievements:\n" + (index < achievements.length ? achievements[index] : "N/A") +
            "\n\nMistakes:\n" + (index < mistakes.length ? mistakes[index] : "N/A");

        playerStatsLabel.setText(info);
    }

    private Table createStatsPanel(String title) {
        Table panel = new Table(skin);
        panel.setBackground("default-round");

        Label label = new Label(title + "\n\nAchievements | Mistakes", skin);
        label.setAlignment(Align.center);
        label.setFontScale(1.2f);
        panel.add(label).pad(10);

        return panel;
    }

    private Table createYearTimeline() {
        Table timeline = new Table();

        String[] years = {
            "1 Year After...\n• Increase in potential businesses.\n• Increase in attendance at tertiary classes.",
            "2 Years After...\n• Starter businesses are growing.\n• Entrepreneurs are confident and sustainable.",
            "3 Years After...\n• Better business + IT knowledge.\n• Decreasing unemployment.",
            "4 Years After...\n• More qualified residents.\n• Rising economic stability."
        };

        for (String yearText : years) {
            Table yearBox = new Table(skin);
            yearBox.setBackground("default-round");

            Label label = new Label(yearText, skin);
            label.setWrap(true);
            label.setFontScale(1.1f);

            yearBox.add(label).width(300).pad(15);
            timeline.add(yearBox).padRight(30);
        }

        return timeline;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);

        float currentScroll = scrollPane.getScrollX();
        currentScroll += delta * scrollSpeed;

        if (currentScroll >= totalScrollWidth) {
            currentScroll -= totalScrollWidth;
        }

        scrollPane.setScrollX(currentScroll);

        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); skin.dispose(); }
}
