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
    centerList = new ArrayList<Vector3f>();

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

    Vector3f childSize = new Vector3f();
    Vector3f childCenter = new Vector3f();

    Vector3f parentJoint = new Vector3f();
    Vector3f childJoint = new Vector3f();

    childSize.x = ((rand.nextInt(9)+1) + rand.nextFloat())/2;
    childSize.y = ((rand.nextInt(9)+1) + rand.nextFloat())/2;
    childSize.z = ((rand.nextInt(9)+1) + rand.nextFloat())/2;

    parentSurface = rand.nextInt(6);
    childSurface = correspondingChildSurface(parentSurface);

    findRandomParentJoint(parentJoint, parentSurface, parent);
    findRandomChildJoint(childJoint, childSurface, childSize);

    //transformChild(childSize,childCenter,);

    addBlock(childCenter, childSize, parent, parentJoint,  childJoint, Vector3f.UNIT_Z, Vector3f.UNIT_Z);
  }

  private void findRandomParentJoint(Vector3f pJoint, int pSurface, Block parent)
  {
    Vector3f parentCenter = centerList.get(parent.getID());

  }

  private void findRandomChildJoint(Vector3f cJoint, int cSurface, Vector3f cSize)
  {

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
    }
    return childSurface;
  }

  private Vector3f pointOnSurface(int blockId, int surface)
  {
    Vector3f joint = new Vector3f();



    return joint;
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
