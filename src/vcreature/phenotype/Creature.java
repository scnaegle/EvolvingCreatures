package vcreature.phenotype;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import java.util.ArrayList;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;



/**
 * The Class Creature.<br>
 * This is the required phenotype that is passed to the simulator.<br>
 * A block contains a Joint to its parent. <br>
 * Each joint contains an ArrayList of Rules for each of its degrees of freedom.<br>
 * Each creature must have exactly one block with jointToParent == null.<br>
 */
public class Creature
{
  /**
   * Instantiates a new creature.
   * Copies all values of blocks, joints and rules to internal tree structure.
   * After a Creature is instantiated, any changes made to its structure, will not
   * be noticed by the physics engine. Thus, all calls to 
   * getJointAngle(), getBoxForwardVector(), etc will be based on the original
   * Creature structure.
   * 
   * This constructor throws an IllegalArgumentException if:<br>
   * 1) Any block has a length, width or height less than 0.5 meters.<br>
   * 2) The body does not have a root block (block with jointToParent == null).<br>
   * 3) The body has more than one root block.<br> 
   * 5) When all the degrees of freedom of all joint are at the starting angle
   * of zero radians, a pair of blocks that are not parent/child intersect.
   *
   * 
   * The root block will always have ID=0
   */
  
  public static final float JOINT_ANGLE_MIN = -(float)(Math.PI/2.0);
  public static final float JOINT_ANGLE_MAX =  (float)(Math.PI/2.0);
  
  //Maxinium angular speed of the hinge joint in Radians per second. 
  //This value is so high that almost always the limiting factor will be the 
  //supplied impulse with acting on the block mass.
  public static final float JOINT_MAX_ANGULAR_SPEED = 1000f; 
  
  private final PhysicsSpace physicsSpace; 
  private final Node jMonkeyRootNode;
  private ArrayList<Block> body = new ArrayList<Block>();
  
  //Temporary vectors used on each frame. They here to avoid instanciating new vectors on each frame
  private Vector3f tmpVec3; //
  private float maxHeightOfLowestPoint = 0;  //fitness
  
  private float elapsedSimulationTime;
  
  public Creature(PhysicsSpace physicsSpace, Node jMonkeyRootNode, Vector3f rootCenter, Vector3f rootSize)
  {
    this.physicsSpace = physicsSpace;
    this.jMonkeyRootNode = jMonkeyRootNode;
    Block root = new Block(physicsSpace, jMonkeyRootNode, body.size(), rootCenter, rootSize);
    body.add(root);
  }
 
 
  public Block addBlock(Vector3f center, Vector3f size, Block parent, Vector3f pivotA, Vector3f pivotB, Vector3f axisA, Vector3f axisB)
  {
    Block block = new Block(physicsSpace, jMonkeyRootNode, body.size(), center, size);
    body.add(block);
    
    
    RigidBodyControl controlA = parent.getPhysicsControl();
    RigidBodyControl controlB = block.getPhysicsControl();
    HingeJoint joint = new HingeJoint(controlA, controlB, pivotA, pivotB, axisA, axisB);
    joint.setCollisionBetweenLinkedBodys(false);
    
    joint.setLimit(JOINT_ANGLE_MIN, JOINT_ANGLE_MAX);
    block.setJointToParent(parent, joint);
    
    physicsSpace.add(joint);
    
    return block;
  }

  
  public void setBlockMaterial(int id, Material mat)
  {
    body.get(id).setMaterial(mat);
  }

  public int getNumberOfBodyBlocks(){return body.size();}

  public Block getBlockByID(int id){return body.get(id);}
 
  /**
   * Gets the angle of the joint connecting the given block index with its parent.
   * This value is calculated and returned by the bullet physics engine.<br>
   * At simulation time 0.0, the value of every angle will always be zero.
   *
   * @param id of the child box.
   * @return the joint angle in radians +- deflection the zero point defined by the 
   * block orientations at the time.
   */
  public final float getJointAngle(int id)
  { return body.get(id).getJoint().getHingeAngle();
  }
  
  
  public float updateBrain(float elapsedSimulationTime)
  {
    this.elapsedSimulationTime = elapsedSimulationTime;
    for (Block block : body)
    {
      HingeJoint joint = block.getJoint();
      if (joint == null) continue;
      
      ArrayList<Neuron> neuronTable = block.getNeuronTable();
      for (Neuron neuron: neuronTable)
      {
        if (brainNeuronFire(neuron))
        { sendNeuronInpulse(block, joint, neuron);
          break;
        }
      }
    }
    return updateFitness();
  }
  
  
  private float updateFitness()
  {
    float currentHeightOfLowestPoint = Float.MAX_VALUE;
    for (Block block : body)
    {
      BoundingBox box = (BoundingBox) block.getGeometry().getWorldBound();
    
      tmpVec3 = box.getMin(tmpVec3);
      if (tmpVec3.y < currentHeightOfLowestPoint) currentHeightOfLowestPoint = tmpVec3.y;
    }
   
    if (currentHeightOfLowestPoint > maxHeightOfLowestPoint) maxHeightOfLowestPoint = currentHeightOfLowestPoint;
    return maxHeightOfLowestPoint;
  }
  
  public float getFitness()
  {
    return maxHeightOfLowestPoint;
  }
  
  
  public boolean brainNeuronFire(Neuron neuron)
  {
    float a = getNeuronInput(neuron, Neuron.A);
    float b = getNeuronInput(neuron, Neuron.B);
    float y = neuron.getOutput(a,b, 0);
    float c = getNeuronInput(neuron, Neuron.C);
    
    if (y>c) return true;
    return false;
  }
  
  public void sendNeuronInpulse(Block block, HingeJoint joint, Neuron neuron)
  {
    float d       = getNeuronInput(neuron, Neuron.D);
    float e       = getNeuronInput(neuron, Neuron.E);
    float impulse = neuron.getOutput(d,e, 2);
    float speed   = JOINT_MAX_ANGULAR_SPEED;
    if (impulse < 0)
    {
      speed   = -speed;
      impulse = -impulse;
    }
    
    if (impulse > block.getJointMaxImpulse()) impulse = block.getJointMaxImpulse();
   
    
    joint.enableMotor(true, speed, impulse);
    block.getPhysicsControl().activate();
  
  }
  
  
  //It seems like this method belongs in the Neuron class, but the Neuron class does not 
  //   know block heights, joints nor elapsed time.
  public float getNeuronInput(Neuron neuron, int i)
  {
    float x = 0;
    if (neuron.getInputType(i) == EnumNeuronInput.CONSTANT) x = neuron.getInputValue(i);
    else if (neuron.getInputType(i) == EnumNeuronInput.TIME) x = elapsedSimulationTime;
    //* Other cases not yet implemented //

    return x;
  }
          
}
