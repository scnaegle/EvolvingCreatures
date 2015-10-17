package vcreature.creatureUtil;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;

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
  private static Random rand = new Random();

  /**
   * Default constructor, attaches creature to world
   * @param physicsSpace
   * @param jMonkeyRootNode
   */
  public RandomCreature(PhysicsSpace physicsSpace, Node jMonkeyRootNode)
  {
    super(physicsSpace, jMonkeyRootNode);

    int blockNumber = rand.nextInt(9)+1;

    Block root = makeRoot();

    while (this.getNumberOfBodyBlocks() < blockNumber)
    {
      //int parentBlock = rand.nextInt(getNumberOfBodyBlocks) + 1;
      //addRandomBlock(parentBlock);
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
    rootCenter.y = rootSize.y;

    return addRoot(rootCenter, rootSize);
  }

  private void addRandomBlock(int index)
  {
    //Block parent = this.getBlockByID(index);
    //Block child = newRandomBlock();
  }

  //private Block newRandomBlock()
  //{
    Vector3f rootSize = new Vector3f();
  //}



}
