package com.main;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;

public class EmailApp {
    private Window emailWindow;
    private Skin skin;
    private List<Table> emailRows = new ArrayList<>(); // Track email rows for selection
    private Table selectedEmail = null; // Currently selected email

    public EmailApp(Skin skin) {
        this.skin = skin;
        createEmailUI();
    }

    private void createEmailUI() {
        emailWindow = new Window("Inbox", skin);
        emailWindow.setSize(400, 300);
        emailWindow.setPosition(10, 10);

        Table emailListTable = new Table();
        emailListTable.top().left();

        String[][] emails = {
            {"QUB Surveys", "Please give us feedback", "Hi Thomas, Module evaluation surveys are your opportunity to pr..."},
            {"Your Scan", "Message", "Your scan (Scan to My Email)..."}
        };

        for (String[] email : emails) {
            Table emailRow = new Table();
            emailRow.left().pad(10);

            Label senderLabel = new Label(email[0], skin, "default");
            senderLabel.setFontScale(1.1f);

            Label subjectLabel = new Label(email[1], skin, "default");
            subjectLabel.getStyle().fontColor = Color.DARK_GRAY;
            subjectLabel.setWrap(true);
            subjectLabel.setAlignment(Align.left);

            Label previewLabel = new Label(email[2], skin, "default");
            previewLabel.getStyle().fontColor = Color.GRAY;
            previewLabel.setWrap(true);
            previewLabel.setAlignment(Align.left);

            emailRow.add(senderLabel).left().padBottom(2).row();
            emailRow.add(subjectLabel).expandX().fillX().left().padBottom(2).row();
            emailRow.add(previewLabel).expandX().fillX().left().row();

            // Store row reference
            emailRows.add(emailRow);

            // Click listener to change selection
            emailRow.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectEmail(emailRow);
                }
            });

            emailListTable.add(emailRow).expandX().fillX().pad(5);
            emailListTable.row();
            emailListTable.add(new Image(skin.newDrawable("white", Color.LIGHT_GRAY)))
                .height(1).expandX().fillX().padBottom(5);
            emailListTable.row();
        }

        ScrollPane emailScroll = new ScrollPane(emailListTable, skin);
        emailScroll.setScrollingDisabled(true, false);

        Table leftPanel = new Table();
        leftPanel.add(emailScroll).width(250).fillY();

        Table emailContent = new Table();
        Label emailSubject = new Label("Subject: Your Scan", skin);
        Label emailBody = new Label("Message\n\nThis is a test email.", skin);
        emailBody.setWrap(true);
        emailBody.setAlignment(Align.topLeft);
        TextButton acceptButton = new TextButton("Accept", skin);

        emailContent.add(emailSubject).left().row();
        emailContent.add(emailBody).expand().fill().row();
        emailContent.add(acceptButton).padTop(10).row();

        Table mainTable = new Table();
        mainTable.add(leftPanel).width(250).fillY();
        mainTable.add(emailContent).expand().fill();

        emailWindow.add(mainTable).expand().fill();
        emailWindow.row();

        TextButton closeButton = new TextButton("Close", skin);
        closeButton.addListener(event -> {
            emailWindow.remove();
            return true;
        });

        emailWindow.add(closeButton).padTop(10);
    }

    private void selectEmail(Table emailRow) {
        // Reset the previous selection
        if (selectedEmail != null) {
            selectedEmail.setBackground(skin.newDrawable("white", null)); // Set background to blue
        }

        // Highlight new selection
        selectedEmail = emailRow;
        selectedEmail.setBackground(skin.newDrawable("white", Color.BLUE)); // Set background to blue
    }


    public Window getEmailWindow() {
        return emailWindow;
    }
}
