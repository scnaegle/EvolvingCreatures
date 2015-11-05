 package vcreature.mainSimulation;

 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.converters.FileConverter;
 import com.jme3.renderer.Renderer;
 import com.jme3.system.JmeContext;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.layout.align.HorizontalAlign;
 import vcreature.creatureUtil.CreatureConstants;
 import vcreature.hillClimbing.HillClimbing;
 import vcreature.creatureUtil.*;
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

 import java.io.File;
 import java.util.*;

//Added 10/14/2015 justin thomas
 import com.jme3.niftygui.NiftyJmeDisplay;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;

//JCommander for command-line arguments
 import com.beust.jcommander.Parameter;


public class MainSim extends SimpleApplication implements ActionListener, ScreenController
{
  @Parameter
  private List<String> parameters = new ArrayList<>();

  @Parameter(names = {"-h", "--help"}, description = "Shows the help text", help = true)
  private boolean help;

  @Parameter(names = "--headless", description = "If this flag is present then it will Run the GA in headless mode with no GUI")
  boolean headless = false;

  @Parameter(names = "--thread-count", description = "Number of threads to use, defaults to 1")
  public static int thread_count = 1;

  @Parameter(names = "--viewing-thread", description = "What thread you are currently viewing")
  public static int viewing_thread = 1;

  @Parameter(names = "--max-population", description = "Maximum number of Genomes in the population")
  int population_count = -1;

  @Parameter(names = "--max-num-blocks", description = "Maximum number of blocks for a creature")
  int max_num_blocks = -1;

  @Parameter(names = "--speed", description = "Set the speed of the simulation")
  int sim_speed = 1;

  @Parameter(names = "--hill-climb-only", description = "Run without crossover")
  boolean doing_crossover = true;

  @Parameter(names = "--uniform-crossover", description = "Use uniform crossover")
  boolean uniform_crossover = false;

  @Parameter(names = "--tournament-selection", description = "Set the crossover selection to tournament selection instead of culling selection")
  boolean tournament_selection = false;

  @Parameter(names = "--output", description = "File that you would like to output to", converter = FileConverter.class)
  public static File output_file = new File("dna_out.txt");

  @Parameter(names = "--output-best", description = "File that you would like to output the best creature to", converter = FileConverter.class)
  public static File output_best_creature = new File("best_creature.txt");

  @Parameter(names = "--input", description = "Input file to start the Genetic Algorithm", converter = FileConverter.class)
  public static File input_file = null;

  @Parameter(names = "--leg-creature", description = "Only make leg creatures")
  boolean leg_creature = false;

  @Parameter(names = "--random-creature", description = "Only make random creatures")
  boolean random_creature = false;

  @Parameter(names = "--debug", description = "Debug mode")
  boolean debug = false;

  private BulletAppState bulletAppState;
  private PhysicsSpace physicsSpace;
  private float cameraAngle = (float)(Math.PI/2.0);
  private float elapsedSimulationTime = 0.0f;

  //Temporary vectors used on each frame. They here to avoid instanciating new vectors on each frame
  private Vector3f tmpVec3; //

  private boolean isCameraRotating = true;

  private OurCreature myCreature;
//  private ArrayList<ArrayList<DNA>> population;
  private HillClimbing hillClimbing;
  private int current_creature_index = 0;
  private int crossover_count = 0;
  private int generation_total_count = 0;
  private int generation_count = 0;
  private boolean view_specific_creature = false;
  private int viewing_creature = -1;
  private int currently_displayed_creature = 0;
  private float start_total_fitness = 0;
  private Random rand =  new Random();
  private float bestFitnessSoFar;

  private Population population;

  //Nifty gui
  private Nifty nifty;

  private boolean isRunning = true;
  //TODO will perform crossover if this is true.  Need a command line arg to set
  //if we're doing just hill climb.
  //private boolean doingCrossover = true;


  @Override
  public void simpleInitApp()
  {
    /**
     * Set up Physics
     */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    physicsSpace = bulletAppState.getPhysicsSpace();
    //bulletAppState.setDebugEnabled(true);

    physicsSpace.setGravity(PhysicsConstants.GRAVITY);
    physicsSpace.setAccuracy(PhysicsConstants.PHYSICS_UPDATE_RATE);
    physicsSpace.setMaxSubSteps(4);
    speed = 1;



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

    population = new Population();

    if (input_file != null)
    {
      System.out.println("reading from file: " + input_file);
      DNAio.readPopulation(input_file, population);
      if(population_count == -1) population_count = population.size();
      System.out.println("read in " + population.size() + " creatures");
    } else
    {
      //myCreature = new OurCreature(physicsSpace, rootNode, false);
      //population.add(myCreature.getDNA());
      //myCreature.remove();
      //myCreature = new OurCreature(physicsSpace, rootNode, true);
      //population.add(myCreature.getDNA());
      //myCreature.remove();

      while (population.size() < CreatureConstants.MAX_POPULATION)
      {
        population.add(makeAndGetRandomDNA());
      }
    }

    //testOut();
    hillClimbing = new HillClimbing(population);

    startSimForCreature(current_creature_index);

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

  private void startSimForCreature(int creature_index) {
    currently_displayed_creature = creature_index;
    try
    {
      if (myCreature != null) {
        myCreature.remove();
      }
      myCreature = new OurCreature(physicsSpace, rootNode, population.get(creature_index).getLast());
      myCreature.placeOnGround();
      //System.out.println("Valid " + myCreature.isValid());
      elapsedSimulationTime = 0.0f;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      population.get(creature_index).updateLastFitness(0.0f);
      creature_index++;
      startSimForCreature(creature_index);
    }
  }

  private void storeFitnessForCurrentCreature() {
    population.get(current_creature_index).updateLastFitness(myCreature.getFitness());
  }

  private void showSettings() {
    System.out.println("headless: " + headless);
    System.out.println("speed: " + speed);
    System.out.println("thread_count: " + thread_count);
    System.out.println("viewing_thread: " + viewing_thread);
    System.out.println("population_count: " + population_count);
    System.out.println("max number of blocks:" + max_num_blocks);
    System.out.println("output file: " + output_file);
    System.out.println("input: " + input_file);
    System.out.println("debug: " + debug);
    System.out.println("settings: " + settings);
  }

  

  private void setCreatureConstants() {
    if (max_num_blocks != -1) CreatureConstants.MAX_BLOCKS = max_num_blocks;
    if (population_count != -1) CreatureConstants.MAX_POPULATION = population_count;
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
//    inputManager.addMapping("Change Creature", new KeyTrigger(KeyInput.KEY_C));
    inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_SPACE));

    // Add the names to the action listener.
    inputManager.addListener(this, "Quit");
    inputManager.addListener(this, "Toggle Camera Rotation");
    inputManager.addListener(this, "Pause");
  }

  public void onAction(String name, boolean isPressed, float timePerFrame)
  {
    if (isPressed && name.equals("Toggle Camera Rotation"))
    { isCameraRotating = !isCameraRotating;
    }
//    else if (isPressed && name.equals("Change Creature")) {
//      System.out.format("Creature Fitness (Maximum height of lowest point) = %.3f meters]\n", myCreature.getFitness());
//
//      myCreature.remove();
//
//      cameraAngle = (float)(Math.PI/2.0);
//      elapsedSimulationTime = 0.0f;
//    }
    else if (isPressed && name.equals("Pause")) {
      System.out.println("Got here");
      if (isRunning)
      {
        isRunning = false;
      } else {
        isRunning = true;
      }
    }
    else if (name.equals("Quit"))
    {
      System.out.format("Creature Fitness (Maximium height of lowest point) = %.3f meters]\n", myCreature.getFitness());
      System.exit(0);
    }
  }

  //TODO: looks like there are creatures spawning in floor.  Caught one by time test and throwing an exception
  private boolean validCreature()
  {
    //System.out.println("*******Time passed*********");
    //System.out.println(elapsedSimulationTime);
    if(!myCreature.isValid() || elapsedSimulationTime < 2 && myCreature.getFitness() > 4)
    {
      System.err.println("****INVALID CREATURE****");
      return false;
     // throw new IllegalStateException();
    }
    return true;
  }

  private DNA makeAndGetRandomDNA()
  {
    RandCreature creature;
    DNA dna;
    boolean leg;

    if (leg_creature && !random_creature)
    {
      creature = new LegCreature(physicsSpace, rootNode);
      dna = creature.getDNA();
      creature.removeAll();
    }
    else if (random_creature && !leg_creature)
    {
      creature = new RandCreature(physicsSpace, rootNode);
      dna = creature.getDNA();
      creature.removeAll();
    }

    else
    {
      leg = rand.nextBoolean();
      if (leg)
      {
        creature = new LegCreature(physicsSpace, rootNode);
        dna = creature.getDNA();
      }
      else
      {
        creature = new RandCreature(physicsSpace, rootNode);
        dna = creature.getDNA();
      }
      creature.removeAll();
    }
    return dna;
  }

  /* Use the main event loop to trigger repeating actions. */
  @Override
  public void simpleUpdate(float deltaSeconds)
  {
    if (isRunning)
    {
      elapsedSimulationTime += deltaSeconds;
      //print("simpleUpdate() elapsedSimulationTime=", (float)elapsedSimulationTime);
      //print("simpleUpdate() joint1.getHingeAngle()=", joint1.getHingeAngle());
      //TODO put Back: myCreature.updateBrain(elapsedSimulationTime);
      myCreature.updateBrain(elapsedSimulationTime);
      if (!validCreature()) {
        population.get(current_creature_index).updateLastFitness(0.0f);
        if(current_creature_index < CreatureConstants.MAX_POPULATION - 1)
        {
          current_creature_index++;
          startSimForCreature(current_creature_index);
        }
      }
      if (debug) {
        System.out.println("Max Fitness: " + myCreature.getFitness());
      }
      if (!headless)
      {
        updateGUIFitnessParamsText();
      }

      if (isCameraRotating)
      {
        //Move camera continously in circle of radius 25 meters centered 10 meters
        //  above the origin.
        cameraAngle += deltaSeconds * 2.0 * Math.PI / 60.0; //rotate full circle every minute
        float x = (float) (50.0 * Math.cos(cameraAngle));
        float z = (float) (50.0 * Math.sin(cameraAngle));

        tmpVec3 = new Vector3f(x, 10.0f, z);
        cam.setLocation(tmpVec3);
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
      }

      if (elapsedSimulationTime > CreatureConstants.SIMULATION_TIME)
      {
        if (view_specific_creature) {
          startSimForCreature(viewing_creature);
        }
        else
        {
          //System.out.println("current creature fitness: " + myCreature.getFitness());
          storeFitnessForCurrentCreature();
          myCreature.remove();
          if (current_creature_index < CreatureConstants.MAX_POPULATION - 1)
          {
            current_creature_index++;
            startSimForCreature(current_creature_index);
          }
          else
          {
            if (bestFitnessSoFar < population.getBest().getFitness())
            {
              bestFitnessSoFar = population.getBest().getFitness();
            }
            System.out.println("BEST FITNESS SO FAR: " + bestFitnessSoFar);
            //DNAio.writePop(bestCreature.getDNA());
            DNAio.writePopulation(population);

            population.updateFitnessCache();
            if (crossover_count == 0 && generation_count == 0) {
              start_total_fitness = population.getTotalRecentFitness();
            }
            updateGUICurrentStatsText();
            if (debug)
            {
              // Show all fitnesses
              System.out.println("All Fitnesses: ");
              for (int i = 0; i < CreatureConstants.MAX_POPULATION; i++)
              {
                System.out.format("%d: %f\n", i, population.get(i).getLast().getFitness());
              }
            }

            current_creature_index = 0;
            population = hillClimbing.hillClimb();

            generation_total_count++;
            generation_count++;

          //limiting number of hillclimb generations to limit runaway hillclimb
            //sessions.
          if(hillClimbing.isMutationNeeded() || generation_count > 100)
          {
            //TODO GA here
            ArrayList<DNA> tempPop = trimPopulation();

            if(doing_crossover)//&& if doing tournamentSelection
            {
              tempPop = doCrossovers(tempPop);
            }
            for(DNA dna : tempPop)
            {
              dna.bumpUp();
            }
            listIntoPopulation(tempPop);
            //may want to reset population after GA to free up memory from keeping track of mutation history of DNAs before GA
            hillClimbing = new HillClimbing(population); //if population isn't reset, then this can be removed
            generation_count = 0;
            crossover_count++;
          }
            startSimForCreature(current_creature_index);

          }
        }
      }
    }
  }

  /**
   * Assuming crossovers are being done, select and run appropriate crossover.
   * @param tempPop           1d array of population (from best hillclimbing
   *                          result).
   * @return                  1d array of population.
   */
  private ArrayList<DNA> doCrossovers(ArrayList<DNA> tempPop)
  {
    if(tournament_selection)
    {
      tempPop = tournamentSelection(tempPop);
    }
    else
    {
      tempPop = cullPopulationSelection(tempPop);
    }
    return tempPop;
  }

  /**
   * Tournament Selection crossover.  Select two creatures at random from the
   * population.  perform crossover. put into new list. return list to make
   * new population from
   * @param tempPop       1d list of the population
   * @return              new population of crossovered dnas.
   */
  private ArrayList<DNA> tournamentSelection(ArrayList<DNA> tempPop)
  {
    Random rand = new Random();
    int size = tempPop.size();
    int index1, index2;
    DNA dna1, dna2;
    DNA fit1, fit2;
    DNA[] children;
    //new empty list to hold new population
    ArrayList<DNA> newPop = new ArrayList<>();
    //while newpop isn't at new population size
    while(newPop.size() < CreatureConstants.MAX_POPULATION)
    {
      //pick two random creatures
      index1 = rand.nextInt(size);
      index2 = rand.nextInt(size);
      while(index1 == index2)//if same creature is picked for #2 choose another
      {
        index2 = rand.nextInt(size);
      }
      dna1 = tempPop.get(index1);
      dna2 = tempPop.get(index2);
      //store the fittest of the two
      fit1 = dna1.getFitness() > dna2.getFitness() ? dna1 : dna2;
      index1 = rand.nextInt(size);
      index2 = rand.nextInt(size);
      //get two more and compare
      while(index1 == index2)
      {
        index2 = rand.nextInt(size);
      }
      dna1 = tempPop.get(index1);
      dna2 = tempPop.get(index2);
      //store fittest of two.
      fit2 = dna1.getFitness() > dna2.getFitness() ? dna1 : dna2;
      //crossover the two fittest from comparisons
      children = performCrossover(dna1, dna2);
      //add to new population
      newPop.add(children[0]);
      newPop.add(children[1]);
      //do again
    }
    return newPop;
  }

  /**
   * Cull population and perform crossover.
   * @param tempPop       1d list of population.
   */
  private ArrayList<DNA> cullPopulationSelection(ArrayList<DNA> tempPop)
  {
    //sort by fitness
    Collections.sort(tempPop);
    //cull least fit
    cullLeastFit(tempPop);
    int size = tempPop.size();
    int count = 0;
    DNA workingDNA;
    DNA[] children;
    //if haven't crossed over entire population and population isn't empty
    while(count < size && !tempPop.isEmpty())
    {
      //if there are at least 2 DNAs in population pull the first two and cross
      //here for safety.
      if(tempPop.size() >= 2)
      {
        workingDNA = tempPop.remove(0);
        children = performCrossover(workingDNA, tempPop.remove(0));
        tempPop.add(children[0]);
        tempPop.add(children[1]);
        count += 2;
      }
    }
    //System.out.println("CROSSOVER " +  population.size());
//    population.sort(null);
    return tempPop;
  }

  /**
   * Make and return a list of the best dna from the population.  Gets the best
   * generation version for each DNA in the population.
   * @return        1D ArrayList of the best dna from the population
   */
  private ArrayList<DNA> trimPopulation()
  {
    //System.out.println("called trim");
    ArrayList<DNA> newPop = new ArrayList<>();
    DNA best;
    //For each creature history
    for(Population.Strand strand : population.getStrands())
    {
      best = strand.get(0);
      //Choose the best fit dna
      for(DNA dna : strand.getGenerations())
      {
        if(dna.getFitness() >= best.getFitness())
        {
          best = dna;
        }
      }
      //add to newpop
      newPop.add(best);
    }
    //newPop.sort(null); //sort by fitness
    return newPop;
  }

  /**
   * Cull the least fit members of the population and replace with a random
   * @param population        1d population array.
   */
  private void cullLeastFit(ArrayList<DNA> population)
  {
    int numToCull = (int)(CreatureConstants.MAX_POPULATION * CreatureConstants.CULL_PERCENT);
    if(numToCull < 1)
    {
      numToCull = 1;
    }
    //System.out.println("Culling " + numToCull);
    for(int i = 0; i < numToCull; ++i)
    {
      if(!population.isEmpty())
      {
        population.remove(0);
      }
    }
    while(population.size() < CreatureConstants.MAX_POPULATION)
    {
      population.add(makeAndGetRandomDNA());
    }
  }

  /**
   * Perform appropriate crossover.
   * @param dna1
   * @param dna2
   * @return
   */
  private DNA[] performCrossover(DNA dna1, DNA dna2)
  {
    //if singleCrossover
    if(!uniform_crossover)
    {
      return dna1.singleCrossover(dna2);
    }
    else
    {
      return dna1.uniformCrossover(dna2);
    }
  }
  /**
   * Put a 1D arraylist of dna into a population array.
   * @param newPop        1d array of DNA
   */
  private void listIntoPopulation(ArrayList<DNA> newPop)
  {
    population = new Population();
    for(DNA dna : newPop)
    {
      population.add(dna);
    }
  }

  /**
   * @deprecated
   * Go through 1d array of population and perform crossovers.
   * @param population        1d array of DNAs
   */
  private void cullCrossover(ArrayList<DNA> population)
  {
    int size = population.size();
    int count = 0;
    DNA workingDNA;
    DNA[] children;
    //if haven't crossed over entire population and population isn't empty
    while(count < size && !population.isEmpty())
    {
      //if there are at least 2 DNAs in population pull the first two and cross
      //here for safety.
      if(population.size() >= 2)
      {
        workingDNA = population.remove(0);
        children = workingDNA.singleCrossover(population.remove(0));
        population.add(children[0]);
        population.add(children[1]);
        count += 2;
      }
    }
    population.sort(null);
  }


  private void updateGUIFitnessParamsText() {
    setTextForElement("crossover_text", "Crossover Count: " + crossover_count);
    setTextForElement("total_generation_text", "Total Generations: " + generation_total_count);
    setTextForElement("generation_text", "Generation: " + generation_count);
    setTextForElement("creature_id_text", "Creature id: " + currently_displayed_creature);
    setTextForElement("fitness_text", "Fitness: " + myCreature.getFitness());
  }

  private void updateGUISettingsText() {
    setTextForElement("max_population_text", "Max Population: " + CreatureConstants.MAX_POPULATION);
    setTextForElement("max_blocks_text", "Max Blocks: " + CreatureConstants.MAX_BLOCKS);
  }

  private void updateGUICurrentStatsText() {
    setTextForElement("total_fitness_text", "Total Fitness: " + population.getTotalRecentFitness());
    setTextForElement("avg_fitness_text", "Avg Fitness: " + population.getAverageRecentFitness());
    setTextForElement("change_from_last_generation_text", "Change from last Gen: " + population.changeInTotalFitness(-2, -1));
    setTextForElement("total_change_from_start_text", "Total Change from start: " + totalFitnessChangeFromStart());
  }

  private float totalFitnessChangeFromStart() {
    return population.getTotalRecentFitness() - start_total_fitness;
  }

  private void setTextForElement(String element, String text) {
    de.lessvoid.nifty.elements.Element nifty_element = nifty.getCurrentScreen().findElementByName(element);
    TextRenderer renderer = nifty_element.getRenderer(TextRenderer.class);
    renderer.setText(text);
    renderer.setTextHAlign(HorizontalAlign.left);
    renderer.setLineWrapping(true);
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
    settings.setTitle("Evolving Creatures");

    System.out.println("Starting App");

    app.setShowSettings(false);
    app.setSettings(settings);
    if (app.headless) {
      app.start(JmeContext.Type.Headless);
    } else {
      app.start();
    }
  }

  /**
  public class BestCreature
  {
    DNA bestDNASoFar;
    float fitness;

    public BestCreature()
    {
      bestDNASoFar = null;
    }

    public DNA getDNA()
    {
      return bestDNASoFar;
    }

    public float getFitness()
    {
      return fitness;
    }

    public void setBestDNA(DNA toCompare)
    {
      if (bestDNASoFar == null)
      {
        bestDNASoFar = new DNA(toCompare);
        fitness = toCompare.getFitness();
      }
      else
      {
        if (toCompare.getFitness() > fitness)
        {
          bestDNASoFar = new DNA(toCompare);
          fitness = toCompare.getFitness();
        }
      }
    }
  }*/


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
    if (!headless) {
      updateGUISettingsText();
      updateGUICurrentStatsText();
    }
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

  public void setViewingCreature(int viewing_creature) {
    System.out.println("Set the viewing creature...");
    if (viewing_creature != -1 && viewing_creature >= 0 && viewing_creature < CreatureConstants.MAX_POPULATION) {
      this.view_specific_creature = true;
      this.viewing_creature = viewing_creature;
      startSimForCreature(viewing_creature);
    } else {
      this.view_specific_creature = false;
      startSimForCreature(current_creature_index);
    }
    System.out.println("viewing creature: " + viewing_creature);
  }

  public void showPreviousCreature() {
    System.out.println("showing previous creature...");
    if(current_creature_index > 0) current_creature_index--;
    startSimForCreature(current_creature_index);
  }

  public void showNextCreature() {
    System.out.println("showing next creature...");
    if(current_creature_index < CreatureConstants.MAX_POPULATION) current_creature_index++;
    startSimForCreature(current_creature_index);
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
    this.population_count = max_population;
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
   * Test File I/O.
   */
  private void testOut()
  {
    File f = new File("dna_out.txt");
    DNAio.writePopulation(population);
    population = new Population();
    File g = new File("dna_out.txt");
    DNAio.readPopulation(g, population);
    System.out.println("Pop Size " + population.size());
  }

}