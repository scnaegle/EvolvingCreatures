package vcreature.creatureUtil;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Transform;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import vcreature.phenotype.*;

/**
 * Class responsible for making a random, valid creature.
 * Intended use is to make a RandCreature in the physics space
 * save it into DNA by making a new DNA object with the RandCreature in its parameter
 * and immediately remove the RandCreature from the physics space (RandCreature.removeAll())
 *
 * This class is used by MainSim when a population is initialized by a number of random creatures
 */

public class RandCreature
{
  public PhysicsSpace physicsSpace;
  public Node jMonkeyRootNode;
  public ArrayList<Block> body = new ArrayList<>();
  public ArrayList<Vector3f[]> blockProperties;
  public ArrayList<float[]> blockAngles;
  public float[] axisAligned = {0,0,0};
  public static Random rand = new Random();
  public Vector3f tmpVec3 = new Vector3f();

  public ArrayList<IdSurfaceEdge> availableLocations;
  public IdSurfaceEdge currentISE;

  private float elapsedSimulationTime;

  public RandCreature()
  {
    physicsSpace = null;
    jMonkeyRootNode = null;
  }

  /**
   * Constructor to create a RandomCreature
   * @param physicsSpace physics space to simulate in
   * @param jMonkeyRootNode RootNode of physics space to build in
   */
  public RandCreature(PhysicsSpace physicsSpace, Node jMonkeyRootNode)
  {
    this.physicsSpace = physicsSpace;
    this.jMonkeyRootNode = jMonkeyRootNode;

    //arrays which hold the creature properties which will be saved in DNA
    blockProperties = new ArrayList<>();
    blockAngles = new ArrayList<>();

    availableLocations = new ArrayList<>();

    //choose random number of blocks
    int blockNumber = rand.nextInt(CreatureConstants.MAX_BLOCKS-2)+2;

    //make a random sized root
    makeRandomRoot();

    while (getNumberOfBodyBlocks() < blockNumber && !availableLocations.isEmpty())
    {
      int randomPlace = rand.nextInt(availableLocations.size());
      currentISE = availableLocations.get(randomPlace);

      Block parent = body.get(currentISE.id);
      addRandomBlock(parent, currentISE.surface, currentISE.edge);
    }
  }

  /**
   * Get dna object.  If DNA has not been made, make new DNA.
   * @return        dna object
   */
  public DNA getDNA()
  {
    return new DNA(this);
  }

  /**
   *
   * @return number of body blocks in the creature
   */
  public int getNumberOfBodyBlocks()
  {
    return body.size();
  }

  /**
   * Makes a root block with random x,y,z values
   * This rootblock will be axis alligned
   * @return the Block which will serve as the creatures root
   */
  public Block makeRandomRoot()
  {
    //make the root's center at 0,0,0 in the physics space
    Vector3f rootCenter = new Vector3f(0f,0f,0f);
    Vector3f rootSize = new Vector3f(0f,0f,0f);

    //make a the root a random size
    rootSize.x = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    rootSize.y = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    rootSize.z = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;

    //add the root's properties to the arrays which hold the information for DNA creation
    blockProperties.add(makeBlockVectorArray(rootCenter, rootSize));
    blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));

    addAvailableEdges(0);

    //add the root block to the physics space
    return addRoot(rootCenter, rootSize, axisAligned);
  }

  /**
   * Adds a random block smartly to the creature
   * Instead of adding a random block anywhere on the surface of a parent, adds the block it connects to its parent
   * edge to edge
   * @param parent to connect a child block to
   * @param parentSurface parent surface to add to
   * @param parentEdge parent edge to add to
   */
  private void addRandomBlock(Block parent, int parentSurface, int parentEdge)
  {
    int childSurface;
    int childEdge;

    Vector3f childSize = new Vector3f(0f,0f,0f);
    Vector3f parentSize = new Vector3f(parent.getSizeX()/2, parent.getSizeY()/2, parent.getSize()/2);

    Vector3f parentJoint = new Vector3f(0f,0f,0f);
    Vector3f childJoint = new Vector3f(0f,0f,0f);

    Vector3f rotationAxis = new Vector3f(0f,0f,0f);

    //make a random sized child
    childSize.x = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    childSize.y = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    childSize.z = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;

    //choose the child's surface and edge to make a joint to based on the parent's surface and edge
    childSurface = correspondingChildSurface(parentSurface);
    childEdge = correspondingChildEdge(parentEdge);


    findVectorToEdge(parentSize, parentEdge, parentSurface, parentJoint, rotationAxis);
    findVectorToEdge(childSize, childEdge, childSurface, childJoint, rotationAxis);


    //add a new block to the creature
    Block newBlock = addBlock(axisAligned, childSize, parent, parentJoint, childJoint, rotationAxis);

    if (!removeIfIntersection()) //if the new block doesn't intersect
    {
      addRandomNeurons(newBlock); //add random neurons to the block
      addAvailableEdges(getNumberOfBodyBlocks()-1);

      //save the block data in DNA accesible arrays
      blockProperties.add(makeBlockVectorArray(newBlock, childSize, rotationAxis, rotationAxis));
      blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));
    }
  }

  /**
   * Add a random number of neurons to a block
   * @param block to add Neurons to
   */
  private void addRandomNeurons(Block block)
  {
    //choose a random number of neurons to add to a block
    int numberNeurons = rand.nextInt(CreatureConstants.MAX_NEURON_PER_BLOCK)+1;
    //get the maxImpulse available to the block
    float maxImpulse = block.getJointMaxImpulse();
    Neuron n;
    for (int i = 0; i <= numberNeurons; ++i)
    {
      //make a randomNeuron
      n = makeRandomNeuron(maxImpulse);
      //add it to the block
      block.addNeuron(n);
    }
  }

  /**
   * Makes a Neuron with a randomly chosen negative/positive max impulse
   * and randomly chosen firing time
   * @param maxImpulse that the neuron can fire
   * @return neuron to add to the block
   */
  private Neuron makeRandomNeuron(float maxImpulse)
  {
    //seconds at which the neuron fires
    float seconds = rand.nextInt(CreatureConstants.MAX_NEURON_SECONDS - CreatureConstants.MIN_NEURON_SECONDS) + CreatureConstants.MIN_NEURON_SECONDS;
    seconds += rand.nextFloat();
    float impulse = maxImpulse;
    //randomly choose negative or positive impulse
    int sign = rand.nextInt(2);

    //make a new neuron
    Neuron n = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT, EnumNeuronInput.CONSTANT, null);

    if (sign == 1)
    {
      impulse = -impulse;
    }

    n.setInputValue(Neuron.C, seconds);
    n.setInputValue(Neuron.D, impulse);

    return n;
  }

  /**
   * This method adds the root block to this creature. The root block is different from all other
   * blocks in that it has no parant. This means that no joint can be given an impulse that can
   * directly move the root block. The root block can only move as an indirect consequence of
   * an impulse applyed to one of its decendants, by gravity or drag.
   *
   * @param rootCenter
   * @param eulerAngles
   * @param rootHalfSize
   * @return a pointer to the root block of the creature;
   */
  public Block addRoot(Vector3f rootCenter, Vector3f rootHalfSize, float[] eulerAngles)
  {
    if (!body.isEmpty())
    { throw new IllegalArgumentException("This creature already has a root.");
    }

    Quaternion rotation = new Quaternion(eulerAngles);

    Block root = new Block(physicsSpace, jMonkeyRootNode, body.size(), rootCenter, rootHalfSize, rotation);

    body.add(root);

    return root;
  }

  /**
   * Add a block to this creature with a hinge as the new block to the given parent at
   * the given pivot points and along the given axis.
   * The new block's center in world coordinates is calculated from the parent's world
   * coordinates and the two local pivot points.
   * The pivot on the new axis is calculated to match the povit axis on the parent.
   *
   * @param eulerAngles
   * @param halfsize half the extent (in meters) of the block in the x, y and z direction.
   * For example, a block with extent in the x dimension of 0.5 would extend from 0.5 meters from
   * the origin in the -x direction and 0.5 meters from the origin in the +x direction.
   * @param parent Block instance onto which this block will be joined.
   * @param pivotA Location in local coordinates of the pivot point on the parent block.
   * Local coordinates means the location on the block relitive to the block's center with zero rotation.
   * @param pivotB Location in local coordinates of the pivot point on this block.
   * @param axisA One-degree of freedom hinge axis in local coordinates of the parent block.
   * @return a reference to the newly added block.
   */
  public Block addBlock(float[] eulerAngles, Vector3f halfsize, Block parent, Vector3f pivotA, Vector3f pivotB, Vector3f axisA)
  {
    if (body.isEmpty())
    { throw new IllegalArgumentException("This creature does not have a root Block.");
    }

    //Adding block resets the fitness of the creature.
    elapsedSimulationTime = 0;

    Quaternion rotationB = new Quaternion(eulerAngles);

    Transform parantTransform = parent.getGeometry().getWorldTransform();
    Vector3f pivotA_World = new Vector3f();
    parantTransform.transformVector(pivotA, pivotA_World);

    Transform childTransform = new Transform(rotationB);

    Vector3f centerB = new Vector3f();
    childTransform.transformVector(pivotB, centerB);
    centerB.negateLocal();

    centerB.addLocal(pivotA_World);

    //print("center=",centerB);

    Block block = new Block(physicsSpace, jMonkeyRootNode, body.size(), centerB, halfsize, rotationB);
    body.add(block);


    Vector3f axisB = new Vector3f(axisA);
    Transform parantRotation = new Transform(parent.getStartRotation());
    parantRotation.transformVector(axisA, axisB);
    Transform inverseChildRotation = new Transform(rotationB.inverse());
    inverseChildRotation.transformVector(axisB, axisB);

    //print("axisB=",axisB);

    RigidBodyControl controlA = parent.getPhysicsControl();
    RigidBodyControl controlB = block.getPhysicsControl();
    HingeJoint joint = new HingeJoint(controlA, controlB, pivotA, pivotB, axisA, axisB);
    joint.setCollisionBetweenLinkedBodys(true);

    joint.setLimit(PhysicsConstants.JOINT_ANGLE_MIN, PhysicsConstants.JOINT_ANGLE_MAX);
    block.setJointToParent(parent, joint);

    physicsSpace.add(joint);

    return block;
  }

  /**
   * finds if the most recently added block is intersecting with another block other than its parent
   * @return true if an intersection has happened, false if otherwise
   */
  public boolean removeIfIntersection()
  {
    //get the most recently added block
    int newBlockID = getNumberOfBodyBlocks()-1;
    CollisionResults collisionResults = new CollisionResults();

    Block newBlock = body.get(newBlockID);
    Block compareBlock;

    //compare newest added block to all other blocks in the creature
    for (int i = 0 ; i < newBlockID; ++i)
    {
      compareBlock = body.get(i);
      BoundingBox box = (BoundingBox) compareBlock.getGeometry().getWorldBound();
      newBlock.getGeometry().collideWith(box, collisionResults);

      //ignore if intersecting with parent
      if ((newBlock.getIdOfParent() != compareBlock.getID()) && collisionResults.size() > 0)
      {
        removeBlock(newBlockID);
        availableLocations.remove(currentISE);
        return true;
      }

      collisionResults.clear();
    }
    return false;
  }

  /**
   * Simple function which finds an edge of a child based on the passed in edge of the parent
   * for example, if the parentEdge is the top of the surface, then the child's edge should be the top edge of the child's
   * surface or the bottom edge of the child's surface
   *
   * @param parentEdge to connect child block to
   * @return edge of child to connect to parent
   */
  private int correspondingChildEdge(int parentEdge)
  {
    int or = rand.nextInt(2);
    int childEdge = 0;
    switch (parentEdge){
      case 0:
        if (or == 0)
          childEdge = 0;
        else
          childEdge = 2;
        break;
      case 1:
        if (or == 0)
          childEdge = 1;
        else
          childEdge = 3;
        break;
      case 2:
        if (or == 0)
          childEdge =0;
        else
          childEdge = 2;
        break;
      case 3:
        if (or == 0)
          childEdge = 1;
        else
          childEdge = 3;
        break;
      default:
        break;
    }
    return childEdge;
  }
  /**
   * Corresponding ints to surfaces on blocks
   * 0 = +y;
   * 1 = -y;
   * 2 = +x;
   * 3 = -x;
   * 4 = +z;
   * 5 = -z;
   * @param parentSurface
   * @return
   */
  public int correspondingChildSurface(int parentSurface)
  {
    int childSurface = -1;
    switch (parentSurface) {
      case 0: childSurface = 1;
        break;
      case 1: childSurface = 0;
        break;
      case 2: childSurface = 3;
        break;
      case 3: childSurface = 2;
        break;
      case 4: childSurface = 5;
        break;
      case 5: childSurface = 4;
        break;
    }
    return childSurface;
  }

  /**
   *
   * @param size of block you're adding a joint to (half vector)
   * @param edge of block you're adding a joint to
   * @param surface of block you're adding a joint to
   * @param joint to make
   * @param rotationAxis axis of rotation the joint should turn around
   */
  public void findVectorToEdge(Vector3f size, int edge, int surface, Vector3f joint, Vector3f rotationAxis)
  {
    int sign = 1;
    if (surface == 1 || surface == 0)
    {
      if (surface == 1) sign = -1;
      switch (edge)
      {
        case 0:
          joint.x = randomSurfacePoint(size.x);
          joint.y = size.y*(sign);
          joint.z = -size.z;
          rotationAxis.set(Vector3f.UNIT_X);
          break;
        case 1:
          joint.x = size.x;
          joint.y = size.y*(sign);
          joint.z = randomSurfacePoint(size.z);
          rotationAxis.set(Vector3f.UNIT_Z);
          break;
        case 2:
          joint.x = randomSurfacePoint(size.x);
          joint.y = size.y*(sign);
          joint.z = size.z;
          rotationAxis.set(Vector3f.UNIT_X);
          break;
        case 3:
          joint.x = -size.x;
          joint.y = size.y*(sign);
          joint.z = randomSurfacePoint(size.z);
          rotationAxis.set(Vector3f.UNIT_Z);
          break;
        default:
          break;
      }
    }
    else if (surface == 2 || surface == 3)
    {
      if (surface == 3) sign = -1;
      switch (edge)
      {
        case 0:
          joint.x = size.x*(sign);
          joint.y = size.y;
          joint.z = randomSurfacePoint(size.z);
          rotationAxis.set(Vector3f.UNIT_Z);
          break;
        case 1:
          joint.x = size.x*(sign);
          joint.y = randomSurfacePoint(size.y);
          joint.z = -size.z;
          rotationAxis.set(Vector3f.UNIT_Y);
          break;
        case 2:
          joint.x = size.x*(sign);
          joint.y = -size.y;
          joint.z = randomSurfacePoint(size.z);
          rotationAxis.set(Vector3f.UNIT_Z);
          break;
        case 3:
          joint.x = size.x*(sign);
          joint.y = randomSurfacePoint(size.y);
          joint.z = size.z;
          rotationAxis.set(Vector3f.UNIT_Y);
          break;
        default:
          break;
      }
    }
    else if (surface == 4 || surface == 5)
    {
      if (surface == 5) sign = -1;
      switch (edge)
      {
        case 0:
          joint.x = randomSurfacePoint(size.x);
          joint.y = size.y;
          joint.z = size.z * sign;
          rotationAxis.set(Vector3f.UNIT_X);
          break;
        case 1:
          joint.x = size.x;
          joint.y = randomSurfacePoint(size.y);
          joint.z = size.z * sign;
          rotationAxis.set(Vector3f.UNIT_Y);
          break;
        case 2:
          joint.x = randomSurfacePoint(size.x);
          joint.y = -size.y;
          joint.z = size.z * sign;
          rotationAxis.set(Vector3f.UNIT_X);
          break;
        case 3:
          joint.x = -size.x;
          joint.y = randomSurfacePoint(size.y);
          joint.z = size.z * sign;
          rotationAxis.set(Vector3f.UNIT_Z);
          break;
        default:
          break;
      }
    }
  }

  /**
   * finds a random point on the surface of a block
   * @param bounds
   * @return float value of point on block surface
   */
  public float randomSurfacePoint(float bounds)
  {
    int sign = rand.nextInt(2);
    int scale = (int) bounds;

    float point = rand.nextInt(scale+1) + rand.nextFloat();

    if (sign == 1)
    {
      point = -point;
    }

    return point;
  }

  /**
   * remove a block from a creature
   * @param blockID ID of block to remove
   */
  public void removeBlock(int blockID)
  {
    Block b = body.get(blockID);
    physicsSpace.remove(b.getPhysicsControl());
    HingeJoint jointToParent = b.getJoint();
    if (jointToParent != null) physicsSpace.remove(jointToParent);

    Geometry geometry = b.getGeometry();
    geometry.removeFromParent();

    body.remove(b);
  }

  /**
   *
   * @param id of block from creature
   * @return Block from creature
   */
  private Block getBlockByID(int id)
  {
    return body.get(id);
  }


  /**
   * Method which stores the block data into a DNA accesible format
   * @param id of the block
   * @param vecArr Vector array to copy into
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

  /**
   * Returns the specified vector info for the specified block.
   * Caution! returns pointer to block info vector stored in OurCreature, can
   * corrupt data.
   * @param id        id of block.
   * @param v         Vector type.
   * @return          Specified vector3f in blockProperties.
   */
  public Vector3f getBlockVector(int id, BlockVector v)
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
  public Vector3f[] makeBlockVectorArray(Vector3f center, Vector3f size)
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
  public Vector3f[] makeBlockVectorArray(Block b, Vector3f size, Vector3f axisA,
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

  public float[] getBlockAngles(int id)
  {
    return blockAngles.get(id);
  }



  /**
   * A removeAll method which works by removing children first instead of the parent
   * Avoids all the jmonkey warning errors which tells you that you're trying to remove
   * an invalid joint
   */
  public void removeAll()
  {
    Block toRemove;
    while (body.size() > 0)
    {
      toRemove = body.get(body.size()-1);
      physicsSpace.remove(toRemove.getPhysicsControl());
      HingeJoint jointToParent = toRemove.getJoint();
      if (jointToParent != null) physicsSpace.remove(jointToParent);
      Geometry geometry = toRemove.getGeometry();
      geometry.removeFromParent();

      body.remove(body.size()-1);
    }
  }

  /*
  public void remove()
  {
    if (body.size() > 0)
    { removeSubTree(body.get(0));
    }

    if (body.size() != 0)
    {
      System.out.println("ERROR: vcreature.phenotype.Creature.remove() failed");
    }

    elapsedSimulationTime  = 0;
  }



  private void removeSubTree(Block block)
  {
    for (Block child : block.getChildList())
    {
      removeSubTree(child);
    }

    physicsSpace.remove(block.getPhysicsControl());
    HingeJoint jointToParent = block.getJoint();
    if (jointToParent != null) physicsSpace.remove(jointToParent);

    Geometry geometry = block.getGeometry();
    geometry.removeFromParent();

    body.remove(block);
  }  */

  public void addAvailableEdges(int id)
  {
    for (int i = 0; i < 6; ++i)
    {
      for (int j = 0; j < 4; ++j)
      {
        availableLocations.add(new IdSurfaceEdge(id, i, j));
      }
    }
  }

  public class IdSurfaceEdge
  {
    int id;
    int surface;
    int edge;

    public IdSurfaceEdge(int ID, int surface, int edge)
    {
      this.id = ID;
      this.surface = surface;
      this.edge = edge;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this.id == ((IdSurfaceEdge) obj).getId() && this.surface == ((IdSurfaceEdge) obj).getSurface() && this.edge == ((IdSurfaceEdge) obj).getEdge())
      {
        return true;
      }
      else return false;
    }

    public int getId() { return id;}
    public int getSurface() { return surface;}
    public int getEdge() {return edge;}

  }
}
