
package vcreature.mainSimulation;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.jme3.system.JmeContext;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.elements.render.TextRenderer;
import org.json.JSONObject;
import vcreature.creatureUtil.CreatureConstants;
import vcreature.creatureUtil.JSONHandler;
import vcreature.creatureUtil.RandomCreature;
import vcreature.phenotype.Creature;
import vcreature.phenotype.OurCreature;
import vcreature.phenotype.PhysicsConstants;
import vcreature.phenotype.Block;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.light.AmbientLight;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.system.AppSettings;
import vcreature.creatureUtil.DNA;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Added 10/14/2015 justin thomas
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

//JCommander for command-line arguments
import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;

import javax.lang.model.element.Element;


public class MainSim extends SimpleApplication implements ActionListener, ScreenController
{
  @Parameter
  private List<String> parameters = new ArrayList<>();

  @Parameter(names = {"-h", "--help"}, description = "Shows the help text", help = true)
  private boolean help;

  @Parameter(names = { "--log", "--verbose" }, description = "Level of verbosity")
  public static Integer verbose = 1;

  @Parameter(names = "--headless", description = "If this flag is present then it will Run the GA in headless mode with no GUI")
  boolean headless = false;

  @Parameter(names = "--thread-count", description = "Number of threads to use, defaults to 1")
  public static int thread_count = 1;

  @Parameter(names = "--viewing-thread", description = "What thread you are currently viewing")
  public static int viewing_thread = 1;

  @Parameter(names = "--population-count", description = "Starting number of Genomes in the population")
  int starting_population_count = 100;

  @Parameter(names = "--max-num-blocks", description = "Maximum number of blocks for a creature")
  int max_num_blocks = 10;

  @Parameter(names = "--speed", description = "Set the speed of the simulation")
  int sim_speed = 1;

  @Parameter(names = "--output-frequency", description = "Defines how often we dump the Genomes to a log defined by number of seconds.")
  public static int output_frequency = 300;

  @Parameter(names = "--output", description = "File that you woud like to output to", converter = FileConverter.class)
  public static File output_file = new File("dna_out.txt");

  @Parameter(names = "--input", description = "Input file to start the Genetic Algorithm", converter = FileConverter.class)
  public static File input_file = null;

  @Parameter(names = "--debug", description = "Debug mode")
  boolean debug = false;

  private BulletAppState bulletAppState;
  private PhysicsSpace physicsSpace;
  private float cameraAngle = (float)(Math.PI/2.0);
  private float elapsedSimulationTime = 0.0f;
  
  //Temporary vectors used on each frame. They here to avoid instanciating new vectors on each frame
  private Vector3f tmpVec3; //
  private OurCreature myCreature;
  private boolean isCameraRotating = true;

  private DNA testDNA;
  //private RandomCreature myCreature;
  private OurCreature ourCreature; //TODO testing, remove.

  //Nifty gui
  private Nifty nifty;

  private ArrayList<BulletAppState> bulletAppStates = new ArrayList<>();


  @Override
  public void simpleInitApp()
  {
    /**
     * Set up Physics
     */
//    bulletAppState = new BulletAppState();
//    stateManager.attach(bulletAppState);
//    physicsSpace = bulletAppState.getPhysicsSpace();
//    //bulletAppState.setDebugEnabled(true);
//
//    physicsSpace.setGravity(PhysicsConstants.GRAVITY);
//    physicsSpace.setAccuracy(PhysicsConstants.PHYSICS_UPDATE_RATE);
//    physicsSpace.setMaxSubSteps(4);
//    speed = 1;

    bulletAppState = createAndSetUpNewBulletAppState();
    stateManager.attach(bulletAppState);
    physicsSpace = bulletAppState.getPhysicsSpace();
   


    //Set up inmovable floor
    Box floor = new Box(50f, 0.1f, 50f);
    Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    Texture floorTexture = assetManager.loadTexture("Textures/FloorTile.png");

    floorTexture.setWrap(Texture.WrapMode.Repeat);
    floor_mat.setTexture("ColorMap", floorTexture);

    floor.scaleTextureCoordinates(new Vector2f(50, 50));
    Geometry floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(floor_mat);
    floor_geo.setShadowMode(ShadowMode.Receive);
    floor_geo.setLocalTranslation(0, -0.11f, 0);
    rootNode.attachChild(floor_geo);

    /* Make the floor physical with mass 0.0f */
    RigidBodyControl floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    physicsSpace.add(floor_phy);
    floor_phy.setFriction(PhysicsConstants.GROUND_SLIDING_FRICTION);
    floor_phy.setRestitution(PhysicsConstants.GROUND_BOUNCINESS);
    floor_phy.setDamping(PhysicsConstants.GROUND_LINEAR_DAMPINING,
        PhysicsConstants.GROUND_ANGULAR_DAMPINING);
    
   
    Block.initStaticMaterials(assetManager);

    //Test Crossover
    ourCreature = new OurCreature(physicsSpace, rootNode, true);
    testDNA = ourCreature.getDNA();
    ourCreature.remove();
    OurCreature otherCreature = new OurCreature(physicsSpace, rootNode, false);
    DNA testDNA2 = otherCreature.getDNA();
    otherCreature.remove();
    DNA[] crossed = testDNA.singleCrossover(testDNA2);
    ourCreature = new OurCreature(physicsSpace, rootNode, crossed[0]);
    ourCreature.placeOnGround();


    initLighting();
    initKeys();

    initializeGUI();
    //jsonOps();

    flyCam.setDragToRotate(true);

    setCreatureConstants();
    setSpeed(sim_speed);

    if (debug) {
      showSettings();
    }
  }

  private void showSettings() {
    System.out.println("verbose: " + verbose);
    System.out.println("headless: " + headless);
    System.out.println("speed: " + speed);
    System.out.println("thread_count: " + thread_count);
    System.out.println("viewing_thread: " + viewing_thread);
    System.out.println("starting_population_count: " + starting_population_count);
    System.out.println("max number of blocks:" + max_num_blocks);
    System.out.println("output frequency: " + output_frequency);
    System.out.println("output file: " + output_file);
    System.out.println("input: " + input_file);
    System.out.println("debug: " + debug);
    System.out.println("settings: " + settings);
  }

  private BulletAppState createAndSetUpNewBulletAppState() {
    BulletAppState bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
    //bulletAppState.setDebugEnabled(true);

    physicsSpace.setGravity(PhysicsConstants.GRAVITY);
    physicsSpace.setAccuracy(PhysicsConstants.PHYSICS_UPDATE_RATE);
    physicsSpace.setMaxSubSteps(4);
    return bulletAppState;
  }

  private void setCreatureConstants() {
    CreatureConstants.MAX_BLOCKS = max_num_blocks;
    CreatureConstants.MAX_POPULATION = starting_population_count;
  }

  private void initLighting()
  {
    //  ust add a light to make the lit object visible!
    DirectionalLight sun = new DirectionalLight();
    sun.setDirection(new Vector3f(0, -10, -2).normalizeLocal());
    sun.setColor(ColorRGBA.White);
    rootNode.addLight(sun);

    //Without ambient light, the seen looks like outerspace with razer sharp black shadows.
    AmbientLight ambient = new AmbientLight();
    ambient.setColor(ColorRGBA.White.mult(0.3f));
    rootNode.addLight(ambient);
    
    // SHADOW
    // the second parameter is the resolution. Experiment with it! (Must be a power of 2)
    DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 2);
    dlsr.setLight(sun);
    viewPort.addProcessor(dlsr);
  }
  
  
  
  private void initKeys() {
    inputManager.addMapping("Quit", new KeyTrigger(KeyInput.KEY_Q));
    inputManager.addMapping("Toggle Camera Rotation", new KeyTrigger(KeyInput.KEY_P));
    inputManager.addMapping("Change Creature", new KeyTrigger(KeyInput.KEY_C));

    // Add the names to the action listener.
    inputManager.addListener(this, "Quit");
    inputManager.addListener(this, "Toggle Camera Rotation");
  }
  
  public void onAction(String name, boolean isPressed, float timePerFrame) 
  {
    if (isPressed && name.equals("Toggle Camera Rotation"))
    { isCameraRotating = !isCameraRotating;
    }
    else if (isPressed && name.equals("Change Creature")) {
      System.out.format("Creature Fitness (Maximum height of lowest point) = %.3f meters]\n", myCreature.getFitness());

      myCreature.remove();
//      myCreature = new FlappyBird2(physicsSpace, rootNode);

      cameraAngle = (float)(Math.PI/2.0);
      elapsedSimulationTime = 0.0f;
    }
    else if (name.equals("Quit"))
    {
      //TODO put back: System.out.format("Creature Fitness (Maximium height of lowest point) = %.3f meters]\n", myCreature.getFitness());
      System.out.format("Creature Fitness (Maximium height of lowest point) = %.3f meters]\n", ourCreature.getFitness());
      System.exit(0);
    }
  }

  
  /* Use the main event loop to trigger repeating actions. */
  @Override
  public void simpleUpdate(float deltaSeconds)
  {
    elapsedSimulationTime += deltaSeconds;
    //print("simpleUpdate() elapsedSimulationTime=", (float)elapsedSimulationTime);
    //print("simpleUpdate() joint1.getHingeAngle()=", joint1.getHingeAngle());
    //TODO put Back: myCreature.updateBrain(elapsedSimulationTime);
    ourCreature.updateBrain(elapsedSimulationTime);

    if (headless && debug)
    {
      System.out.println("Max Fitness: " + ourCreature.getFitness());
    } else
    {
      de.lessvoid.nifty.elements.Element nifty_element = nifty.getCurrentScreen().findElementByName("fitness_text");
      nifty_element.getRenderer(TextRenderer.class).setText("Fitness: " + ourCreature.getFitness());
    }

    if (isCameraRotating)
    {
      //Move camera continously in circle of radius 25 meters centered 10 meters
      //  above the origin. 
      cameraAngle += deltaSeconds * 2.0 * Math.PI / 60.0; //rotate full circle every minute
      float x = (float) (25.0 * Math.cos(cameraAngle));
      float z = (float) (25.0 * Math.sin(cameraAngle));
    
      tmpVec3 = new Vector3f(x, 10.0f, z);
      cam.setLocation(tmpVec3);
      cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }
  }
 
  
 

  
  private void print(String msg, float x)
  {
    String className = this.getClass().getSimpleName();
    System.out.format("%s.%s %.3f\n", className, msg, x);
  }
  
  private void print(String msg, Vector3f vector)
  {
    String className = this.getClass().getSimpleName();
    System.out.format("%s.%s [%.3f, %.3f, %.3f]\n", className, msg, vector.x, vector.y, vector.z);
  }

  public static void main(String[] args)
  {
    MainSim app = new MainSim();
    JCommander jc = new JCommander(app, args);

    if (app.help) {
      jc.usage();
      System.exit(0);
    }

    AppSettings settings = new AppSettings(true);
    settings.setResolution(1024, 768);
    settings.setSamples(4); //activate antialising (softer edges, may be slower.)

    //Set vertical syncing to true to time the frame buffer to coincide with the refresh frequency of the screen.
    //This also throttles the calls to simpleUpdate. Without this throttling, I get 1000+ pfs on my Alienware laptop
    //   Your application will have more work to do than to spend cycles rendering faster than the
    //   capture rate of the RED Camera used to shoot Lord of the Rings.
    settings.setVSync(true);
    settings.setFrequency((int) app.speed * 60);//Frames per second
    settings.setTitle("Flappy Bird Creature");

    System.out.println("Starting App");

    app.setShowSettings(false);
    app.setSettings(settings);
    if (app.headless) {
      app.start(JmeContext.Type.Headless);
    } else {
      app.start();
    }
  }

  //============GUI Stuff======================================================

  /**
   * This initializes and displays the gui, I don't understand it fully yet, but
   * will look into it further.
   * From example code at:
   * https://code.google.com/p/jmonkeyengine/source/browse/trunk/engine/src/test/jme3test/niftygui/TestNiftyGui.java
   * XML stuff:
   * http://wiki.jmonkeyengine.org/doku.php/jme3:advanced:nifty_gui_xml_layout
   */
  private void initializeGUI()
  {
    //Begin GUI setup
    NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
        inputManager,
        audioRenderer,
        guiViewPort);
    nifty = niftyDisplay.getNifty();
    NiftySelectController controller = new NiftySelectController(this);
    nifty.fromXml("Interface/gaGUI.xml", "hud", controller);
    if (debug) {
      nifty.setDebugOptionPanelColors(true);
    }
    // attach the nifty display to the gui view port as a processor
    guiViewPort.addProcessor(niftyDisplay);
  }

  public void updateSettings() {
//    nifty.gotoScreen("hud");
  }

  public void setThreadCount(int thread_count) {
    this.thread_count = thread_count;
  }

  public void setViewingThread(int viewing_thread) {
    this.viewing_thread = viewing_thread;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
    physicsSpace.setMaxSubSteps(speed * 4);
    settings.setFrequency(speed * 60);
    this.setSettings(settings);
    this.restart();
  }

  public void setMaxNumBlocks(int max_num_blocks) {
    this.max_num_blocks = max_num_blocks;
    setCreatureConstants();
  }

  public void setMaxPopulation(int max_population) {
    this.starting_population_count = max_population;
    setCreatureConstants();
  }

  //=====begin ScreenController implementation================================
  public void bind(Nifty nifty, Screen screen) {
    System.out.println("bind( " + screen.getScreenId() + ")");
  }

  public void onStartScreen() {
    System.out.println("onStartScreen");
  }

  public void onEndScreen() {
    System.out.println("onEndScreen");
  }

  public void quit(){
    nifty.gotoScreen("end");
  }
  //=========end ScreenController implementation===============================

  //==========TESTING METHODS=================================================
  /**
   * Test json IO
   */
  private void jsonOps()
  {
    System.out.println("creature JSON: ");
    JSONObject jsonObject = JSONHandler.toJSON(myCreature);
    JSONHandler.writeGenomeFile(jsonObject);
    JSONObject jsonIn = JSONHandler.readGenomeFile("dnaOut.txt");
    System.out.println(jsonIn);
  }

  public class FileConverter implements IStringConverter<File>
  {
    @Override
    public File convert(String value)
    {
      return new File(value);
    }
  }
}
