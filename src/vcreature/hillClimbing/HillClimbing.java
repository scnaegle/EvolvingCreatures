package vcreature.hillClimbing;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;

import java.util.Random;

/**
 * Created by zfalgout on 10/15/15.
 */
public class HillClimbing
{
  private Creature creature;
  private Creature mutatedCreature;
  private PhysicsSpace physicsSpace;
  private Node rootNode;
  private Random generator;

  //TODO: change so HillClimbing can take in a population instead of a single creature
  //TODO: mock fitness test
  //TODO: need to select random block to do mutation on
  //TODO: if block is child, need to check if we will mutate a child's child and so on
  //TODO: will want to mutate HCFlappyBird for testing, and see if GUI will update it
  //TODO: to start, want to print out creature info to see if HillClimbing is changing creature
  //NOTE: able to get blocks by their ID, ID seems to be the number they are added, getBlockByID will return that Block
  //NOTE: Block.toString will print out Block[ID]: {size in X dir, size in Y dir, size in Z dir}, if not ROOT,
  //then will print parent ID and the joint to that parent

  public HillClimbing(Creature sample, PhysicsSpace space, Node root)
  {
    creature = sample;
    physicsSpace = space;
    rootNode = root;
    generator = new Random();
  }

  private int fitnessTest(Creature sample)
  {
    Block block;
    int fitnessScore = 0;
    int blockCount = sample.getNumberOfBodyBlocks();
    float sizeX, sizeY, sizeZ;
    boolean invalidSize;
    for(int i = 0; i < blockCount; i++)
    {
      block = sample.getBlockByID(i);
      sizeX = block.getSizeX();
      sizeY = block.getSizeY();
      sizeZ = block.getSize();
      invalidSize = sizeX < 1 || sizeY < 1 || sizeZ < 1 || sizeX > 10 || sizeY > 10 || sizeZ > 10;

      if(invalidSize) return 0;
//prefer long legs
      fitnessScore -= 1/sizeX * 10;
      fitnessScore += sizeY/2;
      fitnessScore -= 1/sizeZ * 10;

    }
    return fitnessScore;
  }

  private void mutateBlockStructure(Creature sample, int targetBlockID)
  {
    Block currentBlock;
    mutatedCreature = new HCFlappyBird(physicsSpace,rootNode);

    for(int i = 0; i < mutatedCreature.getNumberOfBodyBlocks(); i++)
    {
      currentBlock = mutatedCreature.getBlockByID(i);
      currentBlock.setMaterial(Block.MATERIAL_BROWN);
    //  if(i < sample.getNumberOfBodyBlocks())//change back to i != targetID
     // {
        /*center = new Vector3f(currentBlock.getStartCenter());
        size = new Vector3f(currentBlock.getSizeX()/2,currentBlock.getSizeY()/2,currentBlock.getSize()/2);
        if(i == 0) mutatedCreature.addRoot(center,size);
        else
        {
          parent = sample.getBlockByID(currentBlock.getIdOfParent());
          pivotA = currentBlock.getJoint().getPivotA();
          pivotB = currentBlock.getJoint().getPivotB();
          mutatedCreature.addBlock(center,size,parent,pivotA,pivotB,Vector3f.UNIT_Z,Vector3f.UNIT_Z);
          mutatedCreature.getBlockByID(i).setMaterial(Block.MATERIAL_BROWN);
        }*/
     // }
    }
  }
//TODO: fix hillClimb
  public void hillClimb() {
    int blockID;
    final int MAX_NUM_BLOCKS = creature.getNumberOfBodyBlocks();

    blockID = generator.nextInt(MAX_NUM_BLOCKS);

    mutateBlockStructure(creature, blockID);
  }

  public Creature getCreature(){return mutatedCreature;}//new HCFlappyBird(physicsSpace,rootNode);}

}
