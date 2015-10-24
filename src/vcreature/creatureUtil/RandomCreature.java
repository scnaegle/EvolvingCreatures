package vcreature.creatureUtil;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.bounding.BoundingBox;
import vcreature.phenotype.*;

import java.util.ArrayList;
import java.util.Random;

// Only 10 max blocks, generate number from 0-9
// while blocklist.size less than block number
//
// Choose random block from blocklist to add too
// Choose random side from chosen block to add too
// Choose random get appropriate side from newly created block
// Choose random point on block surface to add to
// Choose random point on edge of newly created block
// Attach block: make sure no overlap
//
// Give neurons to blocks joints
//

/**
 * Class responsible for random creature generation
 * Creature generation begins with a root node and follows set of rules to create random valid creature
 * Extends Joel's Creature class
 */
public class RandomCreature extends OurCreature
{
  private static Random rand = new Random();
  private PhysicsSpace physicsSpace;
  private Node visualWorld;
  private static float[] axisAligned = {0,0,0};

  /**
   * Default constructor, attaches creature to world
   * @param physicsSpace
   * @param jMonkeyRootNode
   */
  public RandomCreature(PhysicsSpace physicsSpace, Node jMonkeyRootNode)
  {
    super(physicsSpace, jMonkeyRootNode);

    this.physicsSpace = physicsSpace;
    this.visualWorld = jMonkeyRootNode;

    int blockNumber = rand.nextInt(CreatureConstants.MAX_BLOCKS)+1;

    //int blockNumber = 10;

    makeRoot();

    while (this.getNumberOfBodyBlocks() < blockNumber)
    {
      int parentBlock = rand.nextInt(getNumberOfBodyBlocks());
      addRandomBlock(parentBlock);
    }

    bumpUp();
  }

  /**
   * Make a root for the creature with random x,y,z values
   */
  private Block makeRoot()
  {
    Vector3f rootCenter = new Vector3f(0f,0f,0f);
    Vector3f rootSize = new Vector3f(0f,0f,0f);

    //choose random x,y,z values between 1 and 10

    rootSize.x = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    rootSize.y = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    rootSize.z = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;

    System.out.println("Parent Size: " + rootSize.x + " " + rootSize.y + " " + rootSize.z);

    return addRoot(rootCenter, rootSize, axisAligned);
  }

  private void addRandomBlock(int index)
  {
    Block parent = this.getBlockByID(index);
    newRandomBlock(parent);
  }

  private void newRandomBlock(Block parent)
  {
    int parentSurface;
    int childSurface;

    Block newBlock;
    Neuron newNeuron;

    //new vectors for child block
    Vector3f childSize = new Vector3f(0f,0f,0f);
    Vector3f parentSize = new Vector3f(parent.getSizeX()/2, parent.getSizeY()/2, parent.getSize()/2);

    //vectors for the joints between the parents and the child
    Vector3f parentJoint = new Vector3f(0f,0f,0f);
    Vector3f childJoint = new Vector3f(0f,0f,0f);

    Vector3f rotationAxis = new Vector3f(0f,0f,0f);

    //create random sizes for child block
    childSize.x = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    childSize.y = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    childSize.z = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;


    //select a random surface on parent to add child to
    parentSurface = rand.nextInt(6);
    childSurface = correspondingChildSurface(parentSurface); //find corresponding surface on child

    rotationAxis = randomAxis(parentSurface, rotationAxis);


    //make a new joint vector on the parent and on the child
    getSurfaceVector(parentJoint, parentSurface, parentSize);
    getSurfaceVector(childJoint, childSurface, childSize);

    //Vector3f parentCenter = new Vector3f();
    //Vector3f childCenter = getChildCenter(parent.getCenter(parentCenter), parentJoint, childJoint);

    //newBlock = new Block(physicsSpace, visualWorld, getNumberOfBodyBlocks()+1, childCenter, childSize, Quaternion.IDENTITY);

    //addBlock
    addBlock(axisAligned, childSize, parent, parentJoint, childJoint, rotationAxis, rotationAxis);

    //if (!removeIfIntersection())
    //{
      newNeuron = makeRandomNeuron();
      newBlock = getBlockByID(getNumberOfBodyBlocks()-1);
      newBlock.addNeuron(newNeuron);
    //}
  }

  public boolean removeIfIntersection()
  {
    int newBlockID = getNumberOfBodyBlocks()-1;
    CollisionResults collisionResults = new CollisionResults();

    Block newBlock = getBlockByID(newBlockID);
    Block compareBlock;

    for (int i = 0 ; i < newBlockID; ++i)
    {
      compareBlock = getBlockByID(i);
      BoundingBox box = (BoundingBox) compareBlock.getGeometry().getWorldBound();
      newBlock.getGeometry().collideWith(box, collisionResults);

      if ((newBlock.getIdOfParent() != compareBlock.getID()) && collisionResults.size() > 0)
      {
        detachTest();
        return true;
      }

      collisionResults.clear();
    }
    return false;
  }

  private Neuron makeRandomNeuron()
  {
    //float seconds = (rand.nextInt(20)+1) + rand.nextFloat();
    float seconds = 3;
    float impulse = Float.MAX_VALUE;
    int sign = rand.nextInt(2);

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
   * Makes a vector to a joint a child block's surface
   *
   * @param joint joint to draw vector to
   * @param surface surface to draw vector to
   * @param cSize child size
   */
  private void getSurfaceVector(Vector3f joint, int surface, Vector3f cSize)
  {
    switch (surface) {
      case 0: joint.y = cSize.y;
        joint.x = randomSurfacePoint(cSize.x);
        joint.z = randomSurfacePoint(cSize.z);
        break;
      case 1: joint.y = -cSize.y;
        joint.x = randomSurfacePoint(cSize.x);
        joint.z = randomSurfacePoint(cSize.z);
        break;
      case 2: joint.x = cSize.x;
        joint.y = randomSurfacePoint(cSize.y);
        joint.z = randomSurfacePoint(cSize.z);
        break;
      case 3: joint.x = -cSize.x;
        joint.y = randomSurfacePoint(cSize.y);
        joint.z = randomSurfacePoint(cSize.z);
        break;
      case 4: joint.z = cSize.z;
        joint.x = randomSurfacePoint(cSize.x);
        joint.y = randomSurfacePoint(cSize.y);
        break;
      case 5: joint.z = -cSize.z;
        joint.x = randomSurfacePoint(cSize.x);
        joint.y = randomSurfacePoint(cSize.y);
        break;
      default:
        break;
    }
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
  private int correspondingChildSurface(int parentSurface)
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

  private Vector3f randomAxis(int surface, Vector3f axis)
  {
    switch (surface){
      case 0: randomYorZ(axis);
        break;
      case 1: randomYorZ(axis);
        break;
      case 2: randomXorZ(axis);
        break;
      case 3: randomXorZ(axis);
        break;
      case 4: randomXorY(axis);
        break;
      case 5: randomXorY(axis);
        break;
      default: axis = Vector3f.ZERO;
        break;
    }
    return axis;
  }

  private Vector3f randomYorZ(Vector3f axis)
  {
    int random = rand.nextInt(2);
    if (random == 0)
    {
      axis = Vector3f.UNIT_Y;
    }
    else
    {
      axis = Vector3f.UNIT_Z;
    }
    return axis;
  }

  private Vector3f randomXorZ(Vector3f axis)
  {
    int random = rand.nextInt(2);
    if (random == 0)
    {
      axis = Vector3f.UNIT_X;
    }
    else
    {
      axis = Vector3f.UNIT_Z;
    }
    return axis;
  }

  private Vector3f randomXorY(Vector3f axis)
  {
    int random = rand.nextInt(2);
    if (random == 0)
    {
      axis = Vector3f.UNIT_X;
    }
    else
    {
      axis = Vector3f.UNIT_Y;
    }
    return axis;
  }


  private float randomSurfacePoint(float bounds)
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

  private Vector3f getChildCenter(Vector3f parentCenter, Vector3f parentJoint, Vector3f childJoint)
  {
    Vector3f childCenter = new Vector3f();

    childCenter.add(parentCenter);
    childCenter.add(parentJoint);
    childCenter.add(childJoint);

    return childCenter;
  }
}
