
package vcreature.mainSimulation;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.bounding.BoundingBox;
import vcreature.phenotype.Creature;
import vcreature.phenotype.Block;
import static vcreature.phenotype.Creature.JOINT_MAX_ANGULAR_SPEED;
import vcreature.phenotype.EnumNeuronInput;
import vcreature.phenotype.Neuron;


public class FlappyBird extends SimpleApplication implements ActionListener
{
  
  private BulletAppState bulletAppState;
  private PhysicsSpace physicsSpace;
  private float cameraAngle = (float)(Math.PI/2.0);
  private float elapsedSimulationTime = 0.0f;
  
  //Temporary vectors used on each frame. They here to avoid instanciating new vectors on each frame
  private Vector3f tmpVec3; //
  
  private Block torso, leg1, leg2;
  private Creature creature;

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
    
    physicsSpace.setGravity(new Vector3f(0, -9.81f, 0));
    physicsSpace.setAccuracy(1f/80f);
    physicsSpace.setMaxSubSteps(4);


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
    floor_phy.setFriction(0.0f);
    floor_phy.setRestitution(0.3f);
    floor_phy.setDamping(1.0f, 0.2f);
   
    Block.initStaticMaterials(assetManager);
    initCreature();
    initLighting();
    initKeys();

    flyCam.setDragToRotate(true);
    
  }
  
  private void initCreature()
  {
    Vector3f torsoCenter = new Vector3f( 0.0f, 2.5f, 0.0f);     Vector3f torsoSize = new Vector3f( 2.0f, 1.5f, 1.5f);
    Vector3f leg1Center  = new Vector3f( 5.0f, 0.5f, 0.0f);     Vector3f leg1Size  = new Vector3f( 3.0f, 0.5f, 1.0f);
    Vector3f leg2Center  = new Vector3f(-5.0f, 0.5f, 0.0f);     Vector3f leg2Size  = new Vector3f( 3.0f, 0.5f, 1.0f);
    
    creature = new Creature(physicsSpace, rootNode, torsoCenter, torsoSize);
    torso = creature.getBlockByID(0);
    
    //addBlock(Vector3f center, Vector3f size, Block parent, Vector3f pivotA, Vector3f pivotB, Vector3f axisA, Vector3f axisB)
    
    Vector3f pivotA = new Vector3f( 2.0f, -1.5f,  0.0f); //Center of hinge in the block's coordinates
    Vector3f pivotB = new Vector3f(-3.0f,  0.5f,  0.0f); //Center of hinge in the block's coordinates
    
    
    leg1  = creature.addBlock(leg1Center, leg1Size,torso, pivotA,  pivotB, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
    
    Vector3f pivotC = new Vector3f(-2.0f, -1.5f,  0.0f); //Center of hinge in the block's coordinates
    Vector3f pivotD = new Vector3f( 3.0f,  0.5f,  0.0f); //Center of hinge in the block's coordinates
    
    leg2  = creature.addBlock(leg2Center, leg2Size,torso, pivotC,  pivotD, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
    
    torso.setMaterial(Block.MATERIAL_GREEN);
    leg1.setMaterial(Block.MATERIAL_RED);
    leg2.setMaterial(Block.MATERIAL_BLUE);
    
    BoundingBox box = (BoundingBox) torso.getGeometry().getWorldBound();
    print("simpleInitApp(): torso.size=",box.getExtent(tmpVec3));
    
    Neuron leg1Neuron1 = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT,
                EnumNeuronInput.CONSTANT, null);
    
    leg1Neuron1.setInputValue(Neuron.C,11);
    leg1Neuron1.setInputValue(Neuron.D,-Float.MAX_VALUE);
    
    Neuron leg1Neuron2 = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT,
                EnumNeuronInput.CONSTANT, null);
    
    leg1Neuron2.setInputValue(Neuron.C,10);
    leg1Neuron2.setInputValue(Neuron.D,Float.MAX_VALUE);
    
    leg1.addNeuron(leg1Neuron1);
    leg1.addNeuron(leg1Neuron2);
    
    
    Neuron leg2Neuron1 = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT,
                EnumNeuronInput.CONSTANT, null);
    
    leg2Neuron1.setInputValue(Neuron.C,11);
    leg2Neuron1.setInputValue(Neuron.D,Float.MAX_VALUE);
    
    Neuron leg2Neuron2 = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT,
                EnumNeuronInput.CONSTANT, null);
    
    leg2Neuron2.setInputValue(Neuron.C,10);
    leg2Neuron2.setInputValue(Neuron.D,-Float.MAX_VALUE);
    
    leg2.addNeuron(leg2Neuron1);
    leg2.addNeuron(leg2Neuron2);
    
    
    /*
    if (elapsedSimulationTime > 11)
    {
      joint1.enableMotor(true, -JOINT_MAX_ANGULAR_SPEED, torso.getJointMaxImpulse());
      leg1.getPhysicsControl().activate();
      
      joint2.enableMotor(true, JOINT_MAX_ANGULAR_SPEED,  torso.getJointMaxImpulse());
      leg2.getPhysicsControl().activate();
    }
    else 
      
    if (elapsedSimulationTime > 10)
    {
      joint1.enableMotor(true, JOINT_MAX_ANGULAR_SPEED,  torso.getJointMaxImpulse());
      leg1.getPhysicsControl().activate();
      
      joint2.enableMotor(true, -JOINT_MAX_ANGULAR_SPEED,  torso.getJointMaxImpulse());
      leg2.getPhysicsControl().activate();
    }
    */
    
    
    
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
    inputManager.addMapping("Quit",  new KeyTrigger(KeyInput.KEY_Q));

    // Add the names to the action listener.
    inputManager.addListener(this,"Quit");
  }
  
  public void onAction(String name, boolean keyPressed, float tpf) 
  {
     System.out.format("Creature Fitness (Maximium height of lowest point) = %.3f meters]\n", creature.getFitness());
     System.exit(0);
  }

  
  /* Use the main event loop to trigger repeating actions. */
  @Override
  public void simpleUpdate(float deltaSeconds)
  {
    elapsedSimulationTime += deltaSeconds;
    //print("simpleUpdate() elapsedSimulationTime=", (float)elapsedSimulationTime);
    //print("simpleUpdate() joint1.getHingeAngle()=", joint1.getHingeAngle());
    creature.updateBrain(elapsedSimulationTime);

    //Move camera continously in circle of radius 25 meters centered 10 meters
    //  above the origin. 
    cameraAngle += deltaSeconds * 2.0 * Math.PI / 60.0; //rotate full circle every minute
    float x = (float) (25.0 * Math.cos(cameraAngle));
    float z = (float) (25.0 * Math.sin(cameraAngle));
    
    tmpVec3 = new Vector3f(x, 10.0f, z);
    cam.setLocation(tmpVec3);
    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
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
    AppSettings settings = new AppSettings(true);
    settings.setResolution(1024, 768);
    settings.setSamples(4); //activate antialising (softer edges, may be slower.) 

    //Set vertical syncing to true to time the frame buffer to coincide with the refresh frequency of the screen.
    //This also throttles the calls to simpleUpdate. Without this throttling, I get 1000+ pfs on my Alienware laptop
    //   Your application will have more work to do than to spend cycles rendering faster than the 
    //   capture rate of the RED Camera used to shoot Lord of the Rings.
    settings.setVSync(true);
    settings.setFrequency(60);//Frames per second
    settings.setTitle("Twitching Creature");
    
    System.out.println("Starting App");

    FlappyBird app = new FlappyBird();
    app.setShowSettings(false);
    app.setSettings(settings);
    app.start();
  }
}