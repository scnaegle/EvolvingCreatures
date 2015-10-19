package vcreature.phenotype;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.creatureUtil.CreatureConstants;
import vcreature.creatureUtil.DNA;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Justin Thomas(jthomas105@unm.edu)
 * Our Creature extends Joel's creature.
 */
public class OurCreature extends Creature
{
  private DNA dna;
  private PhysicsSpace physicsSpace;
  private Node visualWorld;
  private ArrayList<Vector3f[]> blockProperties;
  private ArrayList<float[]> blockAngles;

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
    Vector3f torsoCenter = new Vector3f( 0.0f, 2.5f, 0.0f);     Vector3f torsoSize = new Vector3f( 2.0f, 1.5f, 1.5f);
    Vector3f leg1Center  = new Vector3f( 5.0f, 0.5f, 0.0f);     Vector3f leg1Size  = new Vector3f( 3.0f, 0.5f, 1.0f);
    Vector3f leg2Center  = new Vector3f(-5.0f, 0.5f, 0.0f);     Vector3f leg2Size  = new Vector3f( 3.0f, 0.5f, 1.0f);

    Block torso = addRoot(torsoCenter, torsoSize);

    Vector3f pivotA = new Vector3f( 2.0f, -1.5f,  0.0f); //Center of hinge in parents block's coordinates
    Vector3f pivotB = new Vector3f(-3.0f,  0.5f,  0.0f); //Center of hinge in child block's coordinates


    Block leg1  = addBlock(leg1Center, leg1Size, torso, pivotA, pivotB, Vector3f.UNIT_Z, Vector3f.UNIT_Z);

    Vector3f pivotC = new Vector3f(-2.0f, -1.5f,  0.0f); //Center of hinge in parents  block's coordinates
    Vector3f pivotD = new Vector3f( 3.0f,  0.5f,  0.0f); //Center of hinge in childs block's coordinates

    Block leg2  = addBlock(leg2Center, leg2Size,torso, pivotC,  pivotD, Vector3f.UNIT_Z, Vector3f.UNIT_Z);

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
  //==================End Constructors==========================================

  /**
   * Remove whole creature, temp fix until joel updates.
   */
  public void remove()
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
   * Create root block.
   * @param rootCenter        Location of block.
   * @param rootSize          Size of block.
   * @return
   */
  @Override
  public Block addRoot(Vector3f rootCenter, Vector3f rootSize)
  {
    Block b = super.addRoot(rootCenter, rootSize);
    blockProperties.add(makeBlockVectorArray(rootCenter, rootSize, null, null,
                                              null, null));
    blockAngles.add(CreatureConstants.IDENTITY_QUATERNION);
    return b;
  }

  /**
   * Add block, calls super.addBlock().  Also logs important vectors and block
   * angles.
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
    Vector3f bCenter = new Vector3f();
    Block b = super.addBlock(eulerAngles,halfsize,parent,pivotA,pivotB,axisA,axisB);
    blockProperties.add(makeBlockVectorArray(b.getCenter(bCenter), halfsize, pivotA, pivotB, axisA, axisB));
    blockAngles.add(Arrays.copyOf(eulerAngles, eulerAngles.length));
    return b;
  }


  /**
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
  /*
  @Override
  public Block addBlock(Vector3f center, Vector3f size, Block parent,
                        Vector3f pivotA, Vector3f pivotB, Vector3f axisA,
                        Vector3f axisB)
  {
    Block b = super.addBlock(center, size, parent, pivotA, pivotB, axisA,
        axisB);
    //blockProperties.add(makeBlockVectorArray(center, size, pivotA, pivotB,
                                              //axisA, axisB));
    System.out.println("Dep vec size " + blockProperties.size());
    return b;
  }*/

  /**
   * Populate the DNA's Size and shape array with all the right vectors.
   * @param id        id of block to change.
   * @param dna       sizeAndShape array from DNA => BlockDNA
   */
  public void populateVectorDNA(int id, Vector3f[] dna)
  {
    dna[BlockVector.CENTER.ordinal()] = new Vector3f(getBlockVector(id,
                                                    BlockVector.CENTER));
    dna[BlockVector.SIZE.ordinal()] = new Vector3f(getBlockVector(id,
                                                    BlockVector.SIZE));
    if(getBlockByID(id).getJoint() != null)
    {
      dna[BlockVector.JOINT_A.ordinal()] = new Vector3f(getBlockVector(id,
                                                        BlockVector.JOINT_A));
      dna[BlockVector.JOINT_B.ordinal()] = new Vector3f(getBlockVector(id,
                                                        BlockVector.JOINT_B));
      dna[BlockVector.AXIS_A.ordinal()] = new Vector3f(getBlockVector(id,
                                                        BlockVector.AXIS_A));
      dna[BlockVector.AXIS_B.ordinal()] = new Vector3f(getBlockVector(id,
                                                        BlockVector.AXIS_B));
    }
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
   * @param v         vector type
   * @return          Copy of specified block vector.
   */
  public Vector3f getBlockVectorCopy(int id, BlockVector v)
  {
    return new Vector3f(getBlockVector(id, v));
  }

  /**
   * Get dna object.
   * @return        dna object
   */
  public DNA getDNA()
  {
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
   * Make an array containing information on block location, size, and axis of
   * rotation.
   * @param center        block location
   * @param size          block size
   * @param jointA        joint location a.
   * @param jointB        joint location b.
   * @param axisA         axis of rotation a.
   * @param axisB         axis of rotation b.
   * @return              Vector3f containing axis' that we need access to.
   */
  private Vector3f[] makeBlockVectorArray(Vector3f center, Vector3f size,
                                          Vector3f jointA, Vector3f jointB,
                                          Vector3f axisA, Vector3f axisB)
  {
    Vector3f[] blockProperties = new Vector3f[6];
    blockProperties[BlockVector.CENTER.ordinal()] = new Vector3f(center);
    blockProperties[BlockVector.SIZE.ordinal()] = size;
    blockProperties[BlockVector.JOINT_A.ordinal()] = jointA;
    blockProperties[BlockVector.JOINT_B.ordinal()] = jointB;
    blockProperties[BlockVector.AXIS_A.ordinal()] = axisA;
    blockProperties[BlockVector.AXIS_B.ordinal()] = axisB;
    return blockProperties;
  }
}
