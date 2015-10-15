package vcreature.phenotype;



import com.jme3.bullet.PhysicsSpace;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.scene.Node;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.RigidBodyControl;

/**
 * The Class Block.
 */
public class Block
{
  public static final float BLOCK_DENSITY = 4f; //Killograms per cubic meter.
  public static Material MATERIAL_RED;
  public static Material MATERIAL_BLUE;
  public static Material MATERIAL_GREEN;
  public static Material MATERIAL_BROWN;
  public static Material MATERIAL_GRAY;
  
  private final float sizeX, sizeY, sizeZ; //meters
  private final Vector3f startCenter; //meters
  private final int id; //Assigned when added to Creature. 0=root and +1 for each block added in order the blocks are added. This is used by DNA and logic curits
  
  private Block parent;
  private HingeJoint jointToParent;
  private ArrayList<Neuron> neuronTable = new ArrayList<Neuron>();
  private ArrayList<Block> childList    = new ArrayList<Block>();
  private Geometry geometry;
  private RigidBodyControl physicsControl;

  private Vector3f jointAxisA, jointAxisB;
  
  //Temporary vectors used on each frame. They here to avoid instanciating new vectors on each frame
  private Vector3f tmpVec3; //
  
  
  //Creates a box that has a center of 0,0,0 and extends in the out from 
    //the center by the given amount in each direction. 
    // So, for example, a box with extent of 0.5 would be the unit cube.
  public Block(PhysicsSpace physicsSpace, Node rootNode, int id, Vector3f center, Vector3f size)
  { 
    if (size.x < 0.5f || size.y < 0.5f || size.z < 0.5f) 
    { throw new IllegalArgumentException("No dimension may be less than 0.5 from block's center: ("+vectorToStr(size));
    }
    
    if (max(size) > 10*min(size))
    {  throw new IllegalArgumentException("Largest dimension must be no more than 10x the smallest: ("+vectorToStr(size));
    }
    
    this.id = id;
    startCenter = new Vector3f(center);
    sizeX = size.x*2;
    sizeY = size.y*2;
    sizeZ = size.z*2;

    
    //Creates a box that has a center of 0,0,0 and extends in the out from 
    //the center by the given amount in each direction. 
    // So, for example, a box with extent of 0.5 would be the unit cube.
    Box box = new Box(size.x, size.y, size.z);
    geometry = new Geometry("Box", box);
    geometry.setMaterial(MATERIAL_GRAY);
    rootNode.attachChild(geometry);
    geometry.setShadowMode(ShadowMode.Cast);
    
    BoxCollisionShape collisionBox = new BoxCollisionShape(size.mult(0.95f));
    physicsControl = new RigidBodyControl(collisionBox, getMass());
    geometry.addControl(physicsControl);
    physicsControl.setPhysicsLocation(center);
    physicsSpace.add(physicsControl);
    physicsControl.setRestitution(PhysicsConstants.BLOCK_BOUNCINESS);
    physicsControl.setFriction(PhysicsConstants.SLIDING_FRICTION);
    physicsControl.setDamping(PhysicsConstants.LINEAR_DAMPINING, 
            PhysicsConstants.ANGULAR_DAMPINING);
  }

  public void storeJointAxis(Vector3f axisA, Vector3f axisB)
  {
    jointAxisA = axisA;
    jointAxisB = axisB;
  }
  private void addChild(Block child) {childList.add(child);}
  
  public void setMaterial(Material mat)
  {
    geometry.setMaterial(mat);
  }


  public void setJointToParent(Block parent, HingeJoint joint)
  {
     jointToParent = joint;
     parent.addChild(this);
     this.parent = parent;
  }

  public void addNeuron(Neuron neuron)
  {
     neuronTable.add(neuron);
  }
  
  public Geometry getGeometry() {return geometry;}
  public RigidBodyControl getPhysicsControl() {return physicsControl;}
  public HingeJoint getJoint(){ return jointToParent;}
  
  public int getID() {return id;}

  public int getIdOfParent(){ return parent.getID();}
  

  public float getSizeX() {return sizeX;}
  

  public float getSizeY() {return sizeY;}
  

  public float getSize() {return sizeZ;}
  
  public ArrayList<Neuron> getNeuronTable() { return neuronTable;}
  
  
  public static void initStaticMaterials(AssetManager assetManager)
  {
    MATERIAL_RED   = initStaticMaterial(assetManager, ColorRGBA.Red);
    MATERIAL_BLUE  = initStaticMaterial(assetManager, ColorRGBA.Blue);
    MATERIAL_GREEN = initStaticMaterial(assetManager, ColorRGBA.Green);
    MATERIAL_BROWN = initStaticMaterial(assetManager, ColorRGBA.Brown);
    MATERIAL_GRAY  = initStaticMaterial(assetManager, ColorRGBA.Gray);
  }
  
  private static Material initStaticMaterial(AssetManager assetManager, ColorRGBA color)
  {
    Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
    mat.setBoolean("UseMaterialColors", true);
    mat.setColor("Ambient", color);
    mat.setColor("Diffuse", color);
    mat.setColor("Specular", color);
    mat.setFloat("Shininess", 64f);  // [0,128]
    return mat;
  }

  public HashMap<String, Object> toHash() {
    HashMap<String, Object> part_hash = new HashMap<>();
    if (parent != null) {
      part_hash.put("parent_id", parent.getID());
    }
    part_hash.put("center", getCenterHash());
    part_hash.put("dimensions", getDimensionHash());
    if (jointToParent != null) {
      part_hash.put("joint", getJointHash());
    }
    part_hash.put("Neurons", getNeuronTableHash());
    return part_hash;
  }

  private HashMap<String, Float> getCenterHash() {
    HashMap<String, Float> center_hash = new HashMap<>();
    center_hash.put("X", startCenter.getX());
    center_hash.put("Y", startCenter.getY());
    center_hash.put("Z", startCenter.getZ());
    return center_hash;
  }

  private HashMap<String, Float> getDimensionHash() {
    HashMap<String, Float> dimension_hash = new HashMap<>();
    dimension_hash.put("X", sizeX);
    dimension_hash.put("Y", sizeY);
    dimension_hash.put("Z", sizeZ);
    return dimension_hash;
  }

  private HashMap<String, Object> getJointHash() {
    HashMap<String, Float> pivotA_hash = new HashMap<>();
    pivotA_hash.put("X", jointToParent.getPivotA().getX());
    pivotA_hash.put("Y", jointToParent.getPivotA().getX());
    pivotA_hash.put("Z", jointToParent.getPivotA().getX());

    HashMap<String, Float> pivotB_hash = new HashMap<>();
    pivotB_hash.put("X", jointToParent.getPivotA().getX());
    pivotB_hash.put("Y", jointToParent.getPivotA().getX());
    pivotB_hash.put("Z", jointToParent.getPivotA().getX());

    HashMap<String, Object> joint_hash = new HashMap<>();
    joint_hash.put("PivotA", pivotA_hash);
    joint_hash.put("PivotB", pivotB_hash);
    joint_hash.put("AxisA", jointAxisA);
    joint_hash.put("AxisB", jointAxisB);

    /*
    for (Field field : jointToParent.getClass().getDeclaredFields()) {
      if (Modifier.isProtected(field.getModifiers())
          && (field.getName().startsWith("axis"))
          ) {
        field.setAccessible(true);
        Object value = null;
        try {
          value = field.get(jointToParent);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
        if (value != null) {
          Vector3f vect_value = (Vector3f) value;
          HashMap<String, Float> axis_hash = new HashMap<>();
          axis_hash.put("X", vect_value.getX());
          axis_hash.put("Y", vect_value.getY());
          axis_hash.put("Z", vect_value.getZ());

        }
      }
    }
    */
    return joint_hash;
  }

  private HashMap<Integer, Object> getNeuronTableHash() {
    HashMap<Integer, Object> neuron_hash = new HashMap<>();
    int i = 0;
    for(Neuron neuron : neuronTable) {
      neuron_hash.put(i, neuron.getHash());
      i++;
    }
    return neuron_hash;
  }

  public String toString()
  {
    String s = "Block["+id+"]: {" + sizeX + ", " + sizeY + ", " + sizeZ + "}\n";
    
    if (parent == null)
    { s += "     ROOT\n\n";
    }
    else
    {
      s += "     Parent: [ID=" + parent.getID() + "]: " + jointToParent + "\n"; 
    }
    return s;
  }
  
  
   //Return the maximium impulse that can be supplyed by a joint on the given parent.
  //The maximium impulse that can be applyed to a joint is proportional to the parent's surface area.
  //The maximium impulse is returned in Newton seconds.
public final float getJointMaxImpulse()
  {
    
    return parent.sizeX*parent.sizeY + parent.sizeY*parent.sizeZ + parent.sizeZ*parent.sizeX;
  }


public final float getMass()
  {
    return sizeX*sizeY*sizeZ*BLOCK_DENSITY;
  }

public static float min(Vector3f vec)
{
  if (vec.x <= vec.y && vec.x <= vec.z) return vec.x;
  if (vec.y <= vec.x && vec.y <= vec.z) return vec.y;
  return vec.z;
}

public static float max(Vector3f vec)
{
  if (vec.x >= vec.y && vec.x >= vec.z) return vec.x;
  if (vec.y >= vec.x && vec.y >= vec.z) return vec.y;
  return vec.z;
}

public static String vectorToStr(Vector3f vec)
{
  return "("+vec.x +", "+vec.y +", "+vec.z +")";
}

}