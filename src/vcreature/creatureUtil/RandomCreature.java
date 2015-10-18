package vcreature.creatureUtil;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;

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
public class RandomCreature extends Creature
{
  public ArrayList<Vector3f> centerList;

  private static Random rand = new Random();

  /**
   * Default constructor, attaches creature to world
   * @param physicsSpace
   * @param jMonkeyRootNode
   */
  public RandomCreature(PhysicsSpace physicsSpace, Node jMonkeyRootNode)
  {
    super(physicsSpace, jMonkeyRootNode);
    centerList = new ArrayList();
    
    int blockNumber = rand.nextInt(CreatureConstants.MAX_BLOCKS);

    makeRoot();

    while (this.getNumberOfBodyBlocks() < blockNumber)
    {
      int parentBlock = rand.nextInt(getNumberOfBodyBlocks());
      addRandomBlock(parentBlock);
    }

  }

  /**
   * Make a root for the creature with random x,y,z values
   */
  private Block makeRoot()
  {
    Vector3f rootCenter = new Vector3f(0f,0f,0f);
    Vector3f rootSize = new Vector3f();

    //choose random x,y,z values between 1 and 10
    rootSize.x = ((rand.nextInt(9)+1) + rand.nextFloat())/2;
    rootSize.y = ((rand.nextInt(9)+1) + rand.nextFloat())/2;
    rootSize.z = ((rand.nextInt(9)+1) + rand.nextFloat())/2;

    //bump up the root node so it doesn't overlap with floor
    //bumpUpCreature();
    rootCenter.y = rootSize.y;

    centerList.add(rootCenter);

    return addRoot(rootCenter, rootSize);
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

    //new vectors for child block
    Vector3f childSize = new Vector3f();
    Vector3f childCenter = new Vector3f();

    //vectors for the joints between the parents and the child
    Vector3f parentJoint = new Vector3f();
    Vector3f childJoint = new Vector3f();

    //create random sizes for child block
    childSize.x = ((rand.nextInt(9)+1) + rand.nextFloat())/2;
    childSize.y = ((rand.nextInt(9)+1) + rand.nextFloat())/2;
    childSize.z = ((rand.nextInt(9)+1) + rand.nextFloat())/2;

    //select a random surface on parent to add child to
    parentSurface = rand.nextInt(6);
    childSurface = correspondingChildSurface(parentSurface); //find corresponding surface on child

    //make a new joint vector on the parent and on the chold
    getSurfaceVector(parentJoint, parentSurface, parent);
    getSurfaceVector(childJoint, childSurface, childSize);

    //find the child's center relative to the world origin
    findChildCenter(parentJoint, childJoint, childCenter);

    //add the block
    addBlock(childCenter, childSize, parent, parentJoint,  childJoint, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
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

  private void findChildCenter(Vector3f parentJoint, Vector3f childJoint, Vector3f childCenter)
  {
    
  }

  private void getSurfaceVector(Vector3f joint, int surface, Block b)
  {
    switch (surface) {
      case 0: joint.y = b.getSizeY();
              joint.x = randomSurfacePoint(b.getSizeX());
              joint.z = randomSurfacePoint(b.getSize());
        break;
      case 1: joint.y = -b.getSizeY();
              joint.x = randomSurfacePoint(b.getSizeX());
              joint.z = randomSurfacePoint(b.getSize());
        break;
      case 2: joint.x = b.getSizeX();
              joint.y = randomSurfacePoint(b.getSizeY());
              joint.z = randomSurfacePoint(b.getSize());
        break;
      case 3: joint.x = -b.getSizeX();
              joint.y = randomSurfacePoint(b.getSizeY());
              joint.z = randomSurfacePoint(b.getSize());
        break;
      case 4: joint.z = b.getSize();
              joint.x = randomSurfacePoint(b.getSizeX());
              joint.y = randomSurfacePoint(b.getSizeY());
        break;
      case 5: joint.z = b.getSize();
              joint.x = randomSurfacePoint(b.getSizeX());
              joint.y = randomSurfacePoint(b.getSizeY());
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

  private float randomSurfacePoint(float bounds)
  {
    int sign = rand.nextInt(1);
    int scale = (int) bounds;

    float point = rand.nextInt(scale+1) + rand.nextFloat();

    if (sign == 1)
    {
      point = -point;
    }

    return point;
  }

  /**
   * Finds the lowest point if it's below
   */
  private void bumpUpCreature()
  {
    float lowestPoint = 0;
    for (int i = 0; i < getNumberOfBodyBlocks(); ++i)
    {
      if (lowestPoint >= this.getHeight(i) && this.getHeight(i) < 0);
      {
        lowestPoint = this.getHeight(i);
      }
    }
    lowestPoint = Math.abs(lowestPoint);
  }
}
