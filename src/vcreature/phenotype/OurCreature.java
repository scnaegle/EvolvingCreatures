package vcreature.phenotype;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import vcreature.creatureUtil.CreatureConstants;
import vcreature.creatureUtil.DNA;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Justin Thomas(jthomas105@unm.edu)
 * Our Creature extends Joel's creature.
 *
 *
 * blockProperties array:
 *  0: BlockVectors.CENTER: Center of the block in world coordinates, used for
 *  root block, not used for child blocks (is there for reference).
 *  1: BlockVectors.SIZE: Dimensions of a block used for construction (1/2 the
 *  size of the final block in each direction).
 *  2: BlockVectors.JOINT_A: Joint pivot position in coordinates relative to the
 *  parent block's local coordinate system(if parentBlock.startCenter were at
 *  0,0,0).
 *  3: BlockVectors.JOINT_B: Joint pivot position in coordinates relative to this
 *  block's coordinate system (if thisBlock.startCenter were at 0,0,0).
 *  4 & 5: BlockVectors.AXIS_A & BlockVectors.AXIS_B: Axis of rotation of the
 *  block's hingeJoint.
 */

public class OurCreature extends Creature
{
  private DNA dna;
  private PhysicsSpace physicsSpace;
  private Node visualWorld;
  private ArrayList<float[]> blockAngles;
  private Vector3f tmpVec3 = new Vector3f();
  private ArrayList<Vector3f[]> blockProperties;



  //====================Begin Constructors======================================
  /**
   * Default constructor, attaches creature to world
   * @param physicsWorld
   * @param visualWorld
   */
  public OurCreature(PhysicsSpace physicsWorld, Node visualWorld)
  {
    super(physicsWorld, visualWorld);
    physicsSpace = physicsWorld;
    this.visualWorld = visualWorld;
    blockProperties = new ArrayList<>();
    blockAngles = new ArrayList<>();
  }

  /**
   * Constructor that creates creature with DNA object
   * @param physWorld       the physics world this belongs to.
   * @param visWorld        the
   * @param dna
   */
  public OurCreature(PhysicsSpace physWorld, Node visWorld, DNA dna)
  {
    this(physWorld, visWorld);
    this.dna = dna;
    this.dna.initializeCreature(this);
  }

  /**
   * Contstructor for testing, creates flappy bird.
   * TODO remove when done testing.
   * @param physWorld       physicsSpace
   * @param visWorld        rootNode
   * @param isFlappy        this creates flapppy bird.
   */
  public OurCreature(PhysicsSpace physWorld, Node visWorld, boolean isFlappy)
  {
    this(physWorld, visWorld);
    if(isFlappy)
    {
      makeFlappyBird();
    }
    else
    {
      makeNotFlappy();
    }
    placeOnGround();
  }
  //==================End Constructors==========================================

  public void detachTest()
  {
    Block b;
    b = getBlockByID(getNumberOfBodyBlocks()-1);
    physicsSpace.remove(b.getPhysicsControl());
    if (b.getJoint() != null) physicsSpace.remove(b.getJoint());
    physicsSpace.remove(b.getPhysicsControl());
    blockProperties.remove(getNumberOfBodyBlocks()-1);
    blockAngles.remove(getNumberOfBodyBlocks()-1);
  }


  /**
   * Detach creature from world without disrupting it.
   */
  public void detach()
  {
    for(int i = 0; i < getNumberOfBodyBlocks(); ++i)
    {
      Block b = getBlockByID(i);
      if(b.getJoint() != null)
      {
        physicsSpace.remove(b.getJoint());
      }
      physicsSpace.remove(b.getPhysicsControl());
      visualWorld.detachChild(b.getGeometry());
    }
  }

  /**
   * @deprecated
   * Create root block.
   * @param rootCenter        Location of block.
   * @param rootSize          Size of block.
   * @return
   */
  @Override
  public Block addRoot(Vector3f rootCenter, Vector3f rootSize)
  {
    //Block b = super.addRoot(rootCenter, rootSize);
    //blockProperties.add(makeBlockVectorArray(rootCenter, rootSize, null, null,
     //                                         null, null));
    //blockAngles.add(CreatureConstants.IDENTITY_QUATERNION);

    return addRoot(rootCenter, rootSize, CreatureConstants.IDENTITY_QUATERNION);
  }

  /**
   * Current Root constructor.  What happens:
   * 1.) Call parent constructor.
   * 2.)call makeBlockVectorArray(rootCenter, rootSize) to store info that DNA
   * constructor will use.
   * 3.) add array created in step 2 to blockProperties.
   * 4.) add angles array to angles list.
   * @param rootCenter        root location.
   * @param rootSize          block size.
   * @param angles            angle orientation
   * @return                  return block that was created.
   */
  @Override
  public Block addRoot(Vector3f rootCenter, Vector3f rootSize, float[] angles)
  {
    Block b = super.addRoot(rootCenter, rootSize, angles);
    blockProperties.add(makeBlockVectorArray(rootCenter, rootSize));
    blockAngles.add(Arrays.copyOf(angles, angles.length));
    return b;
  }

  /**
   * Add block, calls super.addBlock().  Also logs important vectors and block
   * angles.  What happens:
   * 1.) Call parent constructor.
   * 2.)call makeBlockVectorArray(new block, halfsize, axis of rotation a & b)
   * to store info that DNA constructor will use.
   * 3.) add array created in step 2 to blockProperties.
   * 4.) add copy of angles array to angles list.
   * @param eulerAngles
   * @param halfsize half the extent (in meters) of the block in the x, y and z direction.
   * For example, a block with extent in the x dimension of 0.5 would extend from 0.5 meters from
   * the origin in the -x direction and 0.5 meters from the origin in the +x direction.
   * @param parent Block instance onto which this block will be joined.
   * @param pivotA Location in local coordinates of the pivot point on the parent block.
   * Local coordinates means the location on the block relitive to the block's center with zero rotation.
   * @param pivotB Location in local coordinates of the pivot point on this block.
   * @param axisA One-degree of freedom hinge axis in local coordinates of the parent block.
   * @param axisB One-degree of freedom hinge axis in local coordinates of the this block.
   * @return
   */
  @Override
  public Block addBlock(float[] eulerAngles, Vector3f halfsize, Block parent, Vector3f pivotA, Vector3f pivotB, Vector3f axisA, Vector3f axisB)
  {
    Block b = super.addBlock(eulerAngles,halfsize,parent,pivotA,pivotB,axisA,axisB);
    blockProperties.add(makeBlockVectorArray(b, halfsize, axisA, axisB));
    blockAngles.add(Arrays.copyOf(eulerAngles, eulerAngles.length));
    return b;
  }


  /**
   * @deprecated
   * Add block to the creature.  Log it in an accessible location.
   * @param center        Location of the block.
   * @param size          size of the creature.
   * @param parent        Block's parent.
   * @param pivotA        Joint connection location a.
   * @param pivotB        Joint connection location b.
   * @param axisA         axis of rotation a.
   * @param axisB         axis of rotation b.
   * @return              block created.
   */
  @Override
  public Block addBlock(Vector3f center, Vector3f size, Block parent,
                        Vector3f pivotA, Vector3f pivotB, Vector3f axisA,
                        Vector3f axisB)
  {
    Block b = super.addBlock(center, size, parent, pivotA, pivotB, axisA,
        axisB);
    blockProperties.add(makeBlockVectorArray(b, size, axisA, axisB));
    return b;
  }

  /**
   * Populate the DNA's Size and shape array with all the right vectors.  Pass
   * in block id and vector array.
   * This is called from the DNA constructor, with the blockID and DNA's
   * sizeAndShape array.  This array corresponds with blockProperties exactly.
   * @param id        id of block to change.
   * @param vecArr       sizeAndShape array from dna.blockDNAs[i]
   */
  public void populateVectorDNA(int id, Vector3f[] vecArr)
  {
    vecArr[BlockVector.CENTER.ordinal()] = new Vector3f(getBlockVector(id,
                                                    BlockVector.CENTER));
    vecArr[BlockVector.SIZE.ordinal()] = new Vector3f(getBlockVector(id,
                                                    BlockVector.SIZE));
    if(getBlockByID(id).getJoint() != null)
    {
      vecArr[BlockVector.JOINT_A.ordinal()] = new Vector3f(getBlockVector(id,
                                                        BlockVector.JOINT_A));
      vecArr[BlockVector.JOINT_B.ordinal()] = new Vector3f(getBlockVector(id,
                                                        BlockVector.JOINT_B));
      vecArr[BlockVector.AXIS_A.ordinal()] = new Vector3f(getBlockVector(id,
                                                        BlockVector.AXIS_A));
      vecArr[BlockVector.AXIS_B.ordinal()] = new Vector3f(getBlockVector(id,
                                                        BlockVector.AXIS_B));
    }
  }

  public float getLowestPoint()
  {
    Block tempBlock;
    float currentHeightOfLowestPoint = Float.MAX_VALUE;
    for (int i = 0; i < getNumberOfBodyBlocks(); ++i)
    {
      tempBlock = getBlockByID(i);
      float height = tempBlock.getHeight();
      if (height < currentHeightOfLowestPoint) currentHeightOfLowestPoint = height;
    }
    return currentHeightOfLowestPoint;
  }


  /**
   * Temporary function which bumps up a creature to floor level
   * Replaces Joel's placeOnGround until I learn how to work this class better
   */
  public void bumpUp()
  {
    Block tempBlock;
    Vector3f[] tempVecArray;
    Vector3f tempVec;
    float lowestPoint = -getLowestPoint();
    for (int i = 0; i < getNumberOfBodyBlocks()-1; ++i)
    {
      tempBlock = getBlockByID(i);
      RigidBodyControl physicsControl = tempBlock.getPhysicsControl();
      physicsControl.getPhysicsLocation(tmpVec3);
      tmpVec3.y -= lowestPoint;
      physicsControl.setPhysicsLocation(tmpVec3);
      tempVecArray = blockProperties.get(i);
      tempVec = tempVecArray[0];
      tempVec.y = tempVec.y + lowestPoint;
    }
  }

  public void removeSubTreeOurCreature(Block block)
  {
    int blockId = block.getID();
    //super.removeSubTree(block);

    blockProperties.remove(blockId);
  }


  /**
   * Get float array representing the rotation of the specifed block.
   * Caution! sends pointer to array.
   * @param id        block's ID
   * @return          float array representing the Quaternion block was made
   *                  with.
   */
  public float[] getBlockAngles(int id)
  {
    return blockAngles.get(id);
  }


  /**
   * Get a deep copy of specified vector info for the specified block.
   * @param id        id of block.
   * @param v         vector type.
   * @return          Copy of specified block vector.
   */
  public Vector3f getBlockVectorCopy(int id, BlockVector v)
  {
    return new Vector3f(getBlockVector(id, v));
  }

  /**
   * Get dna object.  If DNA has not been made, make new DNA.
   * @return        dna object
   */
  public DNA getDNA()
  {
    if(dna == null)
    {
      dna = new DNA(this);
    }
    return dna;
  }

  /**
   * Returns the specified vector info for the specified block.
   * Caution! returns pointer to block info vector stored in OurCreature, can
   * corrupt data.
   * @param id        id of block.
   * @param v         Vector type.
   * @return          Specified vector3f in blockProperties.
   */
  private Vector3f getBlockVector(int id, BlockVector v)
  {
    return blockProperties.get(id)[v.ordinal()];
  }

  /**
   * make blockProperties array for Root Block.  Stores location (center) and
   * halfsize that will be passed to constructor.  Joint related properties are
   * set to null.  Use when adding root.
   * @param center        block location
   * @param size          block size
   * @return              Vector3f containing axis' that we need access to.
   */
  private Vector3f[] makeBlockVectorArray(Vector3f center, Vector3f size)
  {
    Vector3f[] blockProperties = new Vector3f[6];
    blockProperties[BlockVector.CENTER.ordinal()] = center;
    blockProperties[BlockVector.SIZE.ordinal()] = size;
    blockProperties[BlockVector.JOINT_A.ordinal()] = null;
    blockProperties[BlockVector.JOINT_B.ordinal()] = null;
    blockProperties[BlockVector.AXIS_A.ordinal()] = null;
    blockProperties[BlockVector.AXIS_B.ordinal()] = null;

    return blockProperties;
  }

  /**
   * Make block properties array for standard block.  Use when adding blocks.
   * Center is set to block.startCenter but is not used.
   * size is halfsize that is passed to the constructor.
   * joint related vectors are local coordinates relative to the block's center.
   * @param b
   * @param size
   * @param axisA
   * @param axisB
   * @return
   */
  private Vector3f[] makeBlockVectorArray(Block b, Vector3f size, Vector3f axisA,
                                          Vector3f axisB)
  {
    Vector3f[] blockProperties = new Vector3f[6];
    tmpVec3 = b.getStartCenter(tmpVec3);
    blockProperties[BlockVector.CENTER.ordinal()] = new Vector3f(tmpVec3);
    blockProperties[BlockVector.SIZE.ordinal()] = size;
    if(b.getJoint() != null)
    {
      blockProperties[BlockVector.JOINT_A.ordinal()] = b.getJoint().getPivotA();
      blockProperties[BlockVector.JOINT_B.ordinal()] = b.getJoint().getPivotB();
      blockProperties[BlockVector.AXIS_A.ordinal()] = axisA;
      blockProperties[BlockVector.AXIS_B.ordinal()] = axisB;
    }
    return blockProperties;
  }

  /**
   * Make flappy bird.
   */
  private void makeFlappyBird()
  {
    //a Center is only needed for the root.
    //Note: This is intentionally placed 10 meters above the ground.
    //The Creature method placeOnGround() will be used to place it on the ground before starting the simulation.
    Vector3f torsoCenter = new Vector3f( 0.0f, 12.5f, 0.0f);

    Vector3f torsoSize = new Vector3f( 2.0f, 1.5f, 1.5f);
    Vector3f leg1Size  = new Vector3f( 3.0f, 0.5f, 1.0f);
    Vector3f leg2Size  = new Vector3f( 3.0f, 0.5f, 1.0f);

    //Euler rotation angles (x,y,z) aka (pitch, yaw, rall)).
    //Note: Euler angles are applying in order: (y, z, x) aka (yaw, roll, pitch).
    float[] eulerAngles = {0, FastMath.PI/6.0f, 0};


    Block torso = addRoot(torsoCenter, torsoSize, eulerAngles);

    Vector3f pivotA = new Vector3f( 2.0f, -1.5f,  0.0f); //Center of hinge in the block's coordinates
    Vector3f pivotB = new Vector3f(-3.0f,  0.5f,  0.0f); //Center of hinge in the block's coordinates


    //Notice that even though the blocks are rotated 30 degrees, since the pivot points and pivot axes are
    //   specified in each block's local coordinates, there is no change to these values.
    Block leg1  = addBlock(eulerAngles, leg1Size,torso, pivotA,  pivotB, Vector3f.UNIT_Z, Vector3f.UNIT_Z);



    Vector3f pivotC = new Vector3f(-2.0f, -1.5f,  0.0f); //Center of hinge in the block's coordinates
    Vector3f pivotD = new Vector3f( 3.0f,  0.5f,  0.0f); //Center of hinge in the block's coordinates

    Block leg2  = addBlock(eulerAngles, leg2Size,torso, pivotC,  pivotD, Vector3f.UNIT_Z, Vector3f.UNIT_Z);





    torso.setMaterial(Block.MATERIAL_GREEN);
    leg1.setMaterial(Block.MATERIAL_RED);
    leg2.setMaterial(Block.MATERIAL_BLUE);



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
  }

  /**
   * Make not flappy bird.
   */
  private void makeNotFlappy()
  {
    //a Center is only needed for the root.
    //Note: This is intentionally placed 10 meters above the ground.
    //The Creature method placeOnGround() will be used to place it on the ground before starting the simulation.
    Vector3f torsoCenter = new Vector3f( 0.0f, 12.5f, 0.0f);

    Vector3f torsoSize = new Vector3f( 2.0f, 1.5f, 1.5f);
    Vector3f leg1Size  = new Vector3f( 3.0f, 0.5f, 1.0f);
    Vector3f leg2Size  = new Vector3f( 3.0f, 0.5f, 1.0f);

    //Euler rotation angles (x,y,z) aka (pitch, yaw, rall)).
    //Note: Euler angles are applying in order: (y, z, x) aka (yaw, roll, pitch).
    float[] eulerAngles = {0, FastMath.PI/6.0f, 0};


    Block torso = addRoot(torsoCenter, torsoSize, eulerAngles);

    Vector3f pivotA = new Vector3f( 2.0f, -1.5f,  0.0f); //Center of hinge in the block's coordinates
    Vector3f pivotB = new Vector3f(-3.0f,  0.5f,  0.0f); //Center of hinge in the block's coordinates


    //Notice that even though the blocks are rotated 30 degrees, since the pivot points and pivot axes are
    //   specified in each block's local coordinates, there is no change to these values.
    Block leg1  = addBlock(eulerAngles, leg1Size,torso, pivotA,  pivotB, Vector3f.UNIT_Z, Vector3f.UNIT_Z);



    Vector3f pivotC = new Vector3f(-2.0f, -1.5f,  0.0f); //Center of hinge in the block's coordinates
    Vector3f pivotD = new Vector3f( 3.0f,  0.5f,  0.0f); //Center of hinge in the block's coordinates

    Block leg2  = addBlock(eulerAngles, leg2Size,torso, pivotC,  pivotD, Vector3f.UNIT_Z, Vector3f.UNIT_Z);





    torso.setMaterial(Block.MATERIAL_GREEN);
    leg1.setMaterial(Block.MATERIAL_RED);
    leg2.setMaterial(Block.MATERIAL_BLUE);



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

    Vector3f pivotE = new Vector3f(-3.0f, 0.0f,  0.0f); //Center of hinge in parents  block's coordinates
    Vector3f pivotF = new Vector3f( 3.0f,  0.0f,  0.0f); //Center of hinge in childs block's coordinates

    Block leg3  = addBlock(CreatureConstants.IDENTITY_QUATERNION, leg2Size,leg2, pivotE,  pivotF, Vector3f.UNIT_Z, Vector3f.UNIT_Z);

    torso.setMaterial(Block.MATERIAL_GREEN);
    leg1.setMaterial(Block.MATERIAL_RED);
    leg2.setMaterial(Block.MATERIAL_BLUE);

    Neuron leg3Neuron1 = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT,
        EnumNeuronInput.CONSTANT, null);

    Neuron leg3Neuron2 = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT,
        EnumNeuronInput.CONSTANT, null);

    leg3Neuron1.setInputValue(Neuron.C,11);
    leg3Neuron1.setInputValue(Neuron.D,-Float.MAX_VALUE);

    leg3Neuron2.setInputValue(Neuron.C,10);
    leg3Neuron2.setInputValue(Neuron.D,Float.MAX_VALUE);

    leg3.addNeuron(leg3Neuron1);
    leg3.addNeuron(leg3Neuron2);
  }
}
