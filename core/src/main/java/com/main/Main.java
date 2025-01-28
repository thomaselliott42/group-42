package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.main.weatherSystem.WeatherManager;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.input;

public class Main implements Screen {
    private ShapeRenderer shapeRenderer;
    private List<Node> nodes;
    private Node currentNode;
    private final List<Player> players;

    private WeatherManager weatherManager;

    // Variables for camera movement
    private float dragStartX, dragStartY;
    private boolean dragging;

    private int turn;
    private int currentMoves;
    private int maxMoves;
    private int globalTurn = 0; // used to progress season
    private int years = 0;
    private ArrayList<String> seasons;
    private String currentSeason;

    private float spaceBarHeldTime = 0;  // To track the time the space bar is held
    private boolean isSpaceBarHeld = false;  // To check if space bar is currently held
    private final float requiredHoldTime = 1f;  // Time to hold in seconds

    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private Viewport viewport;

    private SpriteBatch batch;
    private BitmapFont font;

    private Renderer renderer;

    private boolean debugWindow = false;
    private float debugDisplayX = 50;  // Initial position
    private float debugDisplayY = 50;
    private float debugDisplayWidth = 100;  // Initial width
    private float debugDisplayHeight = 50;  // Initial height
    private boolean draggingDebugBox = false;
    private float offsetX = 0;
    private float offsetY = 0;

    // node box
    private float boxWidth = 150f;   // Make the box smaller
    private float boxHeight = 200f;  // Make the box smaller
    private float padding = 10f;
    private float tileSize = 80f;    // Adjust the tile size accordingly
    private float rightSidePadding = 10f; // Distance from the right edge of the screen
    private float centerX;
    private float centerY = padding + boxHeight / 2;  // Position the box at the bottom of the screen
    private boolean draggingNodeBox = false;

    // animation for player moving
    private boolean animatingPlayerMoving = false;
    private float moveSpeed = 4f;  // Adjust this to control animation speed
    private float circleRadius;
    private ArrayList<Task> task;
    private Sound movingSound;

    // timer for no movement left text
    private boolean hasClickedNM = false;
    private float timeLastNM;

    // Dice setup
    private Dice dice;
    private ModelBatch modelBatch;
    private PerspectiveCamera camera3d;

    // Test Bridge
    Node n1=null;
    Node n2=null;
    Boolean selectingNode = false;


    public Main(ArrayList<Player> players) {
        this.players = players;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        nodes = new ArrayList<>();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        setupNodes();
        setupCameras();
        initializeGame();
        fun();

        renderer = new Renderer(camera, uiCamera, viewport, circleRadius, players.get(0), this);  // Pass 'this' (Main) to Renderer
    }

    private void fun(){
        FileHandle musicFile = Gdx.files.internal("audio/backgroundMusic.mp3");
        if (!musicFile.exists() || musicFile.length() == 0) {
            throw new GdxRuntimeException("The MP3 file is empty or does not exist.");
        }
        Music music = Gdx.audio.newMusic(musicFile);
        music.setLooping(true);
        music.play();
        music.pause();
    }

    private void setupNodes() {
        int gridRows = 2 * 2;
        int gridCols = 2 * 2;
        float spacing = 100;

        float startX = Gdx.graphics.getWidth() / 2f;
        float startY = Gdx.graphics.getHeight() / 2f - spacing;

        // Generate nodes in an isometric grid format
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                float isoX = startX + (col - row) * spacing * 0.5f;   // Isometric x
                float isoY = startY + (col + row) * spacing * 0.25f;  // Isometric y
                Node node = new Node(isoX, isoY, "Node " + (row * gridCols + col + 1), 20);
                nodes.add(node);
            }
        }

        linkNodes(gridRows, gridCols);
    }

    private void setupCameras() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom -= .5f;

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), uiCamera);
        viewport.apply();


        circleRadius = nodes.get(0).size / 8f;  // Size of each player indicator circle

    }

    private void initializeGame() {
        seasons = new ArrayList<>();
        seasons.add("Spring");
        seasons.add("Summer");
        seasons.add("Autumn");
        seasons.add("Winter");

        currentSeason = seasons.get(0);
        this.weatherManager = new WeatherManager();


        this.movingSound = Gdx.audio.newSound(Gdx.files.internal("audio/moving.mp3"));
        // load tasks
        task = ResourceLoader.loadTask();

        while (true) {
            int a = MathUtils.random(nodes.size() - 1);
            if (a != 0) {
                nodes.get(a).setIsJobCentre(true);
                nodes.get(a).updateColour();
                break;
            }
        }

        int taskId = 0;
        while (true) {
            int a = MathUtils.random(nodes.size() - 1);
            if (a != 0 && !nodes.get(a).isJobCentre) {
                if (taskId >= task.size()) {
                    break;

                }
                nodes.get(a).setTask(task.get(taskId));

                taskId++;
            }
        }

        // Steps :
        // 1. setup smaller nodes
        // 2. check through the nodes with tasks that have subtasks (currentNode.getTask() != null && currentNode.getTask().getSteps() != null)
        // 3. then assign number of squares for those subs-tasks and assign them to the node as sub-nodes position them on the line to the connecting node
        // 4. when player gets to the end of sub-node goes to connect node or if on main node they can skip to the other main node assign subtask to those nodes
        for (Node currentNode : nodes) {
            Task task = currentNode.getTask();
            if (task != null && task.getSteps() != null) {
                List<Task> subtasks = task.getSteps();
                currentNode.subNodes = new ArrayList<>();

                // Assign sub-nodes along the line to the first linked node
                if (!currentNode.links.isEmpty() || checkLinkedNode(currentNode)) {
                    Node connectedNode;
                    if(currentNode.links.isEmpty()){
                        connectedNode = getLinkedNode(currentNode); // Assume first link as the main connection

                    }else{
                        connectedNode = currentNode.links.get(0); // Assume first link as the main connection

                    }
                    float dx = (connectedNode.x - currentNode.x) / (subtasks.size() + 1); // x increment
                    float dy = (connectedNode.y - currentNode.y) / (subtasks.size() + 1); // y increment

                    for (int i = 0; i < subtasks.size(); i++) {
                        float subX = currentNode.x + dx * (i + 1); // Increment position along the line
                        float subY = currentNode.y + dy * (i + 1);
                        Node subNode = new Node(subX, subY, currentNode.id + "-sub" + i, currentNode.size / 2);
                        subNode.setTask(subtasks.get(i)); // Assign subtask to sub-node
                        currentNode.subNodes.add(subNode);

                        // Link the sub-node to the main and connected nodes
                        currentNode.addLink(subNode);
                        subNode.addLink(currentNode);
                        subNode.addLink(connectedNode);

                        // Add links to all other sub-nodes for this node
                        for (int j = 0; j < currentNode.subNodes.size(); j++) {
                            Node otherSubNode = currentNode.subNodes.get(j);
                            if (otherSubNode != subNode) { // Don't add a self-link
                                subNode.addLink(otherSubNode);
                                otherSubNode.addLink(subNode);
                            }
                        }
                    }
                }
            }
        }


        for (Player player : players) {
            nodes.get(0).occupy(player);
            player.setCurrentNode(nodes.get(0));
            player.setPlayerNodeCirclePos(circleRadius);
        }

        currentMoves = 0;
        turn = 0;
        currentNode = nodes.get(0);

        centerX = Gdx.graphics.getWidth() - rightSidePadding - boxWidth / 2;  // Move to the right side of the screen

        setupDice();

        dice.setIsVisible(true);
    }

    public void setupDice(){
        modelBatch = new ModelBatch();

        // Load textures for each dice face
        Texture[] diceTextures = new Texture[6];
        for (int i = 0; i < 6; i++) {
            diceTextures[i] = new Texture("ui/dice_face_" + (i + 1) + ".png"); // dice1.png to dice6.png
        }

        // Create the dice
        dice = new Dice(diceTextures);

        // Set up a perspective camera
        camera3d = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Position the camera in front of the dice, facing the front face
        camera3d.position.set(0f, 0f, 3f);  // Move the camera along the Z-axis (3 units away from the dice)
        camera3d.lookAt(0f, 0f, 0f);        // Make the camera look at the center of the dice

        camera3d.near = 0.1f;
        camera3d.far = 100f;
        camera3d.update();
    }

    public Boolean checkLinkedNode(Node currentNode) {
        for (Node node : nodes) {
            return node.links.contains(currentNode);
        }
        return null;
    }

    public Node getLinkedNode(Node currentNode) {
        for (Node node : nodes) {
            if (node.links.contains(currentNode)) {
                return node;
            }
        }
        return null;

    }


    @Override
    public void render(float v) {
        renderer.camera.update();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl.glClearColor(0.0078f, 0.0078f, 0.0078f, 0.71f);

        // Clear both the color and depth buffers
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Work in progress -- Placeholder assets, data and look", Gdx.graphics.getWidth() / 2 - 150, Gdx.graphics.getHeight());
        batch.end();
        handleInput();

        if (input.isButtonJustPressed(0) && !dice.getIsVisible()) {

            nodeClicked();
        }

        updatePlayerAnimation();

        renderer.renderNodes(nodes);
        renderer.renderUI(players, turn, maxMoves, currentMoves);


        // Draw the progress bar if space bar is held, or the instruction text otherwise
        if (isSpaceBarHeld) {
            float progress = Math.min(spaceBarHeldTime / requiredHoldTime, 1);  // Progress between 0 and 1
            renderer.renderProgressBar(progress, players.get(turn).getColor());
        } else {
            batch.begin();
            font.draw(batch, "Hold space-bar to end turn", (Gdx.graphics.getWidth() - 200) / 2, 20);
            batch.end();
        }


        // Render debug window if enabled
        if (debugWindow) {
            renderer.renderDebugInfo(debugDisplayX, debugDisplayY, debugDisplayWidth, debugDisplayHeight, players, currentNode, turn, globalTurn, currentSeason, years);
        }

        nodeHover();

//        renderer.renderCurrentNodeBox(currentNode, boxWidth, boxHeight, padding, tileSize, rightSidePadding, centerX, centerY);

        camera3d.update();

        dice.update(v); // Update the dice

        modelBatch.begin(camera3d);
        if(dice.getIsVisible()){
            dice.render(modelBatch); // Render the dice
        }
        modelBatch.end();

        if(hasClickedNM){
            timeLastNM -= v;
            if(timeLastNM >= 0){
                batch.begin();
                font.draw(batch, "No moves left ... Hold space-bar to end turn", (Gdx.graphics.getWidth() - 200) / 2, (Gdx.graphics.getHeight() - 200) / 2);
                batch.end();

            }
        }


    }

    @Override
    public void resize(int width, int height) {
        // Adjust main camera (if needed)
        camera.viewportWidth = Gdx.graphics.getWidth();
        camera.viewportHeight = Gdx.graphics.getHeight();

        camera.update();

        // Adjust the UI camera and viewport
        viewport.update(width, height);
        uiCamera.update();

    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    private void linkNodes(int gridRows, int gridCols) {
        // Step 1: Randomly link nodes
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int currentIndex = row * gridCols + col;
                Node currentNode = nodes.get(currentIndex);

                if (MathUtils.randomBoolean()) {
                    // Link to the node on the right if available
                    if (col < gridCols - 1) {
                        Node rightNode = nodes.get(currentIndex + 1);
                        currentNode.addLink(rightNode);
                    }

                    // Link to the node below if available
                    if (row < gridRows - 1) {
                        Node belowNode = nodes.get(currentIndex + gridCols);
                        currentNode.addLink(belowNode);
                    }
                }
            }
        }

        // Step 2: Ensure every node has at least one link
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                int currentIndex = row * gridCols + col;
                Node currentNode = nodes.get(currentIndex);

                // If the current node has no links, link it to either the right or below node
                if (currentNode.links.isEmpty()) {
                    // Link to the right node if available
                    if (col < gridCols - 1) {
                        Node rightNode = nodes.get(currentIndex + 1);
                        currentNode.addLink(rightNode);
                    }
                    // If no right node, link to the below node if available
                    else if (row < gridRows - 1) {
                        Node belowNode = nodes.get(currentIndex + gridCols);
                        currentNode.addLink(belowNode);
                    }
                }
            }
        }
    }

    private void updatePlayerAnimation() {
        if (animatingPlayerMoving) {
            players.get(turn).playerCircleX = MathUtils.lerp(players.get(turn).playerCircleX, players.get(turn).playerTargetX, moveSpeed * Gdx.graphics.getDeltaTime());
            players.get(turn).playerCircleY = MathUtils.lerp(players.get(turn).playerCircleY, players.get(turn).playerTargetY, moveSpeed * Gdx.graphics.getDeltaTime());

            // Check if we have reached the target position (with a small threshold)
            if (Math.abs(players.get(turn).playerCircleX - players.get(turn).playerTargetX) < 1 && Math.abs(players.get(turn).playerCircleY - players.get(turn).playerTargetY) < 1) {

                players.get(turn).playerCircleX = players.get(turn).playerTargetX;  // Ensure it exactly matches
                players.get(turn).playerCircleY = players.get(turn).playerTargetY;
                animatingPlayerMoving = false;  // Stop the animation
                Gdx.app.log("Debug", "Animation Finished");

            }
        }
    }


    private void nodeHover() {
        Vector3 mousePos = new Vector3(input.getX(), input.getY(), 0);
        camera.unproject(mousePos);

        for (Node node : nodes) {


            if (mousePos.x >= node.x && mousePos.x <= node.x + node.size &&
                mousePos.y >= node.y && mousePos.y <= node.y + node.size) {

                if (debugWindow) {
                    renderer.renderDebugTravelLine(players.get(turn));
                }

                renderer.renderPopUp(node);


            }

            if (node.subNodes != null) {
                for (Node subNode : node.subNodes) {
                    if (mousePos.x >= subNode.x && mousePos.x <= subNode.x + subNode.size &&
                        mousePos.y >= subNode.y && mousePos.y <= subNode.y + subNode.size) {


                        if (debugWindow) {
                            renderer.renderDebugTravelLine(players.get(turn));
                        }


                        renderer.renderPopUp(subNode);


                    }
                }
            }

        }


    }

    public void nodeClicked() {
        Vector3 mousePos = new Vector3(input.getX(), input.getY(), 0);
        camera.unproject(mousePos);

        // Check if player has moves left
        if (currentMoves + 1 > maxMoves) {
            hasClickedNM = true;
            timeLastNM = 1.5f;
            return;
        }

        // Iterate over all nodes
        for (Node node : nodes) {

            // Check if the clicked position is within the bounds of the sub-nodes
            if (node.subNodes != null) {
                if(handleSubNodeClick(mousePos, node)){
                    return;  // Exit if a sub-node was clicked
                }
            }

            // Check if the clicked position is within the bounds of the main node
            if (handleNodeClick(mousePos, node)) {
                return;  // Exit if a main node was clicked
            }
        }
    }

    // Helper method to handle sub-node click
    private boolean handleSubNodeClick(Vector3 mousePos, Node node) {
        for (Node subNode : node.subNodes) {
            if (mousePos.x >= subNode.x && mousePos.x <= subNode.x + subNode.size &&
                mousePos.y >= subNode.y && mousePos.y <= subNode.y + subNode.size) {

                if (currentNode != subNode && currentNode.containsCurrentPlayer(players.get(turn))) {
                    if (currentNode.links.contains(subNode) || subNode.links.contains(currentNode)) {
                        moveToNode(subNode);
                        Gdx.app.log("DEBUG", "Sub-node changed");
                        movingSound.play(0.3f);

                        return true;  // Exit if sub-node is clicked
                    }
                }
            }
        }
        return false;  // Return false if no sub-node was clicked
    }

    // Helper method to handle main node click
    private boolean handleNodeClick(Vector3 mousePos, Node node) {
        if (mousePos.x >= node.x && mousePos.x <= node.x + node.size &&
            mousePos.y >= node.y && mousePos.y <= node.y + node.size) {

            if (currentNode != node && currentNode.containsCurrentPlayer(players.get(turn))) {
                if (currentNode.links.contains(node) || node.links.contains(currentNode)) {
                    moveToNode(node);
                    Gdx.app.log("DEBUG", "Node changed");
                    movingSound.play(0.3f);

                    return true;  // Exit if main node is clicked
                }
            }
        }
        return false;  // Return false if no main node was clicked
    }

    // Helper method to move player to a specific node
    private void moveToNode(Node targetNode) {
        currentMoves++;

        // De-occupy current node and occupy the target node
        for (Player occupant : targetNode.occupants) {
            occupant.setPlayerNodeCirclePos(circleRadius);
        }

        currentNode.deOccupy(players.get(turn).getName());
        targetNode.occupy(players.get(turn));
        players.get(turn).setCurrentNode(targetNode);
        currentNode = targetNode;

        players.get(turn).setPlayerNodeTarget(circleRadius);
        animatingPlayerMoving = true;

        if (debugWindow) {
            renderer.renderDebugTravelLine(players.get(turn));
        }
    }


    private void handleInput() {
        // Handle space-bar press logic
        handleSpaceBarInput();

        // Handle camera zoom (UP/DOWN keys)
        handleCameraZoom();

        // Toggle debug window visibility
        handleDebugWindowToggle();

        // Handle mouse interactions for dragging boxes or interacting with UI
        handleMouseInput();

        // Handle dragging of the camera
        handleCameraDrag();

        //Handle dice
        handleDice();

        //Temporary Handle
        handleAttachTask();

        // Test
        //handleBridge();

    }

    private void handleBridge(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // Start the bridge creation process
            selectingNode = true;
            n1 = null;
            n2 = null;
        }

        if (selectingNode) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mousePos);

            if (Gdx.input.justTouched()) { // Check for a mouse click
                for (Node node : nodes) {
                    // Check if the clicked position is within the bounds of the node
                    if (mousePos.x >= node.x && mousePos.x <= node.x + node.size &&
                        mousePos.y >= node.y && mousePos.y <= node.y + node.size) {

                        if (n1 == null) {
                            // Select the first node
                            n1 = node;
                            n1.setHighlighted(true);
                            break;
                        } else if (n1 != node) {
                            // Select the second node
                            n2 = node;
                            n2.setHighlighted(true);
                            break;
                        }
                    }
                }
            }

            if (n1 != null && n2 != null) {
                // Both nodes are selected, create the bridge
                n1.links.add(n2);
                n2.links.add(n1);

                n1.setHighlighted(false);
                n2.setHighlighted(false);

                // Reset the selection state
                selectingNode = false;
            }
        }
    }

    private void handleDice(){

        if(dice.getIsVisible()){

            float mouseX = input.getX();
            float mouseY = Gdx.graphics.getHeight() - input.getY();

            Vector3 dicePosition = dice.getPosition();

            // Project the 3D world position into 2D screen space
            Vector3 screenPosition = camera3d.project(dicePosition);

            // Check if the mouse is inside the dice (which is assumed to be a 100x100 unit square)
            float diceX = screenPosition.x;
            float diceY = screenPosition.y;

            if(!dice.isAlreadyRolled()){
                // Check if the click is inside the bounds of the dice
                if (input.isButtonPressed(0) && isMouseInsideBox( mouseX, mouseY, diceX, diceY, 400, 400)) {
                    dice.onClicked(); // Trigger the dice roll
                    dice.setIsVisible(true);
                }
            }else{

                // Check if the click is inside the bounds of the dice
                if (input.isButtonPressed(0) && isMouseInsideBox(mouseX, mouseY, diceX, diceY, 400, 400)) {
                    dice.setIsVisible(false); // Hide the dice
                    dice.setAlreadyRolled(false); // Reset the roll state
                    maxMoves = dice.getFaceValue(); // Set maxMoves to the rolled face value

                    dice.resetFace();
                }
            }

        }

    }

    private void handleAttachTask(){

        if(Gdx.input.isKeyJustPressed(Input.Keys.K)
            && currentNode.getTask() != null
            && !currentNode.getTask().taskTaken()
            && players.size() > 1
        ){

            //screen to pop up with player names and squares in the middle
            renderer.hidePlayerPopup(true);

        }
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)
            || Gdx.input.isKeyJustPressed(Input.Keys.L)
            && currentNode.getTask() != null
            && !currentNode.getTask().taskTaken()) {

            // check if task already in it or not
            if (currentNode.getTask().getOwner() == null || currentNode.getTask().getOwner() != null
                && !currentNode.getTask().getOwner().equals(players.get(turn))) {
                players.get(turn).addTask(currentNode.getTask());
                currentNode.getTask().setOwner(players.get(turn));
                currentNode.getTask().setTaken(true);
                renderer.updatePlayerTab(players.get(turn));
                Gdx.app.log("DEBUG", "Attaching task");
                renderer.setPlayerTab();
            }else{
                Gdx.app.log("DEBUG", "Task already attached");

            }
        }

        }

    private void handleSpaceBarInput() {
        if (input.isKeyPressed(Input.Keys.SPACE)) {
            isSpaceBarHeld = true;
            spaceBarHeldTime += Gdx.graphics.getDeltaTime();  // Increment hold time

            // Check if the space bar has been held for the required duration
            if (spaceBarHeldTime >= requiredHoldTime) {
                // Cycle through players' turns
                if (turn + 1 < players.size()) {
                    turn++;
                } else {
                    turn = 0;


                    globalTurn ++;


                    // progress season and get weather for that
                    weatherManager.getWeather(currentSeason, players.size());

                    // initiate the new season
                    currentSeason = seasons.get(globalTurn % 4);



                    // call the weather stuff
                }
                currentNode = players.get(turn).getCurrentNode();
                currentMoves = 0;

                renderer.updatePlayerTab(players.get(turn));

                renderer.hidePlayerPopup(false);
                dice.resetFace();
                dice.setAlreadyRolled(false);
                dice.setRolling(false);
                dice.setIsVisible(true);

                maxMoves = 0;

                // Reset timer after action
                spaceBarHeldTime = 0;
                isSpaceBarHeld = false;
            }
        } else {
            // Reset timer if space bar is released
            spaceBarHeldTime = 0;
            isSpaceBarHeld = false;
        }
    }

    private void handleCameraZoom() {
        if (input.isKeyPressed(Input.Keys.UP)) {
            renderer.camera.zoom -= 0.02f;
        } else if (input.isKeyPressed(Input.Keys.DOWN)) {
            renderer.camera.zoom += 0.02f;
        }
    }

    private void handleDebugWindowToggle() {
        if (input.isKeyJustPressed(Input.Keys.D)) {
            debugWindow = !debugWindow;
        }
    }

    private void handleMouseInput() {
        float mouseX = input.getX();
        float mouseY = Gdx.graphics.getHeight() - input.getY(); // Adjust for Y-axis inversion

        // Check if the mouse is inside the currentNode box
//        if(isMouseInsideBox(mouseX, mouseY, centerX, centerY, boxWidth, boxHeight)){
//            handleNodeBoxWindowDrag(mouseX, mouseY);
//        }

        // Handle dragging of the debug window
        if (debugWindow) {
            handleDebugWindowDrag(mouseX, mouseY);
        }
    }

    private boolean isMouseInsideBox(float mouseX, float mouseY, float boxCenterX, float boxCenterY, float boxWidth, float boxHeight) {
        // Get the bounds of the box
        float boxLeft = boxCenterX - boxWidth / 2;
        float boxRight = boxCenterX + boxWidth / 2;
        float boxTop = boxCenterY + boxHeight / 2;
        float boxBottom = boxCenterY - boxHeight / 2;

        // Check if mouse is inside the box's bounds
        return mouseX >= boxLeft && mouseX <= boxRight && mouseY >= boxBottom && mouseY <= boxTop;
    }

    private void handleNodeBoxWindowDrag(float mouseX, float mouseY) {
        Vector3 mousePosition = uiCamera.unproject(new Vector3(mouseX, mouseY, 0));

        draggingNodeBox = true;
        offsetX = mousePosition.x - debugDisplayX;
        offsetY = mousePosition.y - debugDisplayY;

        Gdx.app.log("DEBUG", "NodeBox drag");

        // Update position while dragging
        if (draggingNodeBox) {

            centerX =  mousePosition.x - offsetX;
            centerY = mousePosition.y - offsetY;
        }

        // Stop dragging when mouse is released
        if (!input.isButtonPressed(Input.Buttons.LEFT)) {
            draggingNodeBox = false;
        }

    }

    private void handleDebugWindowDrag(float mouseX, float mouseY) {
        if (input.isButtonJustPressed(Input.Buttons.LEFT) &&
            isMouseInsideBox(mouseX, mouseY, debugDisplayX + debugDisplayWidth / 2, debugDisplayY + debugDisplayHeight / 2, debugDisplayWidth, debugDisplayHeight)) {

            // Start dragging the debug box
            draggingDebugBox = true;
            offsetX = mouseX - debugDisplayX;
            offsetY = mouseY - debugDisplayY;
        }

        // Update position while dragging
        if (draggingDebugBox) {
            debugDisplayX = mouseX - offsetX;
            debugDisplayY = mouseY - offsetY;
        }

        // Stop dragging when mouse is released
        if (!input.isButtonPressed(Input.Buttons.LEFT)) {
            draggingDebugBox = false;
        }

        // Handle resizing of the debug window
    }

    private void handleCameraDrag() {
        if (input.isButtonPressed(Input.Buttons.LEFT) && !draggingDebugBox) {
            if (!dragging) {
                dragStartX = input.getX();
                dragStartY = input.getY();
                dragging = true;
            } else {
                float deltaX = dragStartX - input.getX();
                float deltaY = dragStartY - input.getY();
                renderer.camera.translate(deltaX * renderer.camera.zoom, deltaY * renderer.camera.zoom);
                dragStartX = input.getX();
                dragStartY = input.getY();
            }
        } else {
            dragging = false;
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }


}
