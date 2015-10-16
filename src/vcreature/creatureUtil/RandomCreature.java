package vcreature.creatureUtil;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;

import java.util.Random;

/**
 * Class responsible for random creature generation
 * Creature generation begins with a root node and follows set of rules to create random valid creature
 */
public class RandomCreature extends Creature
{
  private static Random rand = new Random();

  public RandomCreature(PhysicsSpace physicsSpace, Node jMonkeyRootNode)
  {
    super(physicsSpace, jMonkeyRootNode);

    Block root = makeRoot();

  }

  /**
   * Make a root for the creature with random x,y,z values
   */
  private Block makeRoot()
  {
    Vector3f rootCenter = new Vector3f(0f,0f,0f);
    Vector3f rootSize = new Vector3f();

    rootSize.x = ((rand.nextInt(9)+1) + rand.nextFloat())/2;
    rootSize.y = ((rand.nextInt(9)+1) + rand.nextFloat())/2;
    rootSize.z = ((rand.nextInt(9)+1) + rand.nextFloat())/2;

    rootCenter.y = rootSize.y;

    return addRoot(rootCenter, rootSize);
  }




}
