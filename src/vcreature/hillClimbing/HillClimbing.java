//TODO: 1st mutateBlocks

package vcreature.hillClimbing;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.creatureUtil.DNA;
import vcreature.phenotype.*;

import java.util.Random;

/**
 * Created by zfalgout on 10/15/15.
 */
public class HillClimbing
{
  private OurCreature creature;//TODO: can remove after Hill Climbing is set to take in population
  private Creature mutatedCreature;
  private OurCreature randomCreature;
  private PhysicsSpace physicsSpace;
  private Node rootNode;
  private Random generator;
  private DNA dna;
  private final int NEURON_COUNT = Neuron.TOTAL_INPUTS;

  //TODO: mock fitness test
  //TODO: to start, want to print out creature info to see if HillClimbing is changing creature

  //TODO: take in arraylist of creatures, population
  public HillClimbing(OurCreature sample, PhysicsSpace space, Node root)
  {
    creature = sample;
    physicsSpace = space;
    rootNode = root;
    generator = new Random();
  }

  //mock fitness test
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

  /**
   * Will add a new block that is an exact copy of originalBlock
   * to the creature being modified
   * @param originalBlock from the creature
   * @param center of the new block
   * @param size of the new block
   */
  private void deepCopyBlock(Block originalBlock, Vector3f center, Vector3f size)
  {
    Block parent = randomCreature.getBlockByID(originalBlock.getIdOfParent());
    Vector3f pivotA = new Vector3f(originalBlock.getJoint().getPivotA());
    Vector3f pivotB = new Vector3f(originalBlock.getJoint().getPivotB());
    Block block = randomCreature.addBlock(center,size,parent,pivotA,pivotB,Vector3f.UNIT_Z,Vector3f.UNIT_Z);

    copyBlockNeurons(originalBlock, block);
  }

  /**
   * Copies all the neurons from the original block
   * to the mutated block
   * @param originalBlock starting block
   * @param mutatedBlock block that is having mutations
   */
  private void copyBlockNeurons(Block originalBlock, Block mutatedBlock)
  {
    Neuron neuron;
    EnumNeuronInput a;
    EnumNeuronInput b;
    EnumNeuronInput c;
    EnumNeuronInput d;
    EnumNeuronInput e;

    //loop through neuron table
    for(Neuron blockNeuron : originalBlock.getNeuronTable())
    {
      a = blockNeuron.getInputType(0);
      b = blockNeuron.getInputType(1);
      c = blockNeuron.getInputType(2);
      d = blockNeuron.getInputType(3);
      e = blockNeuron.getInputType(4);
      neuron = new Neuron(a,b,c,d,e);

      for(int j = 0; j < NEURON_COUNT; j++)
      {
        neuron.setInputValue(j, blockNeuron.getInputValue(j));
      }

      mutatedBlock.addNeuron(neuron);

    }
  }

  /**
   * Mutates a blocks size by picking a random float to add or
   * subtract from the current size, and picks a x,y,z at random
   * to alter size
   * @param originalBlock original block that will be mutated
   * @param center center of block
   * @param size current size of the original block
   * @param isRoot if the original block is a root block
   * @return the new mutated size is not equal to the original size, true, else false
   */
  private boolean mutateBlockSize(Block originalBlock, Vector3f center, Vector3f size, boolean isRoot)
  {
    float x = 0;
    float y = 0;
    float z = 0;
    float temp;
    float sizeChange = generator.nextFloat();//will get float between 0.0 and 1.0

    if(sizeChange == 0.0) sizeChange += .1;

    boolean addOp = generator.nextInt(2) == 0;
    boolean sizeNot10 = size.getX() < 10 && size.getY() < 10 && size.getZ() < 10;
    boolean sizeNot1 = size.getX() > 1 && size.getY() > 1 && size.getZ() > 1;
    switch (generator.nextInt(3))
    {
      case(0):
        temp = x;
        if(addOp && sizeNot10)
        {
          if((temp + sizeChange) < 10) x += sizeChange;
        }
        else if(sizeNot1)
        {
          if((temp - sizeChange) > 1) x -= sizeChange;
        }
        break;
      case(1):
        temp = y;
        if(addOp && sizeNot10)
        {
          if((temp + sizeChange) < 10) y += sizeChange;
        }
        else if(sizeNot1)
        {
          if((temp - sizeChange) > 1) y -= sizeChange;
        }
        break;
      default:
        temp = z;
        if(addOp && sizeNot10)
        {
          if((temp + sizeChange) < 10) z += sizeChange;
        }
        else if(sizeNot1)
        {
          if((temp - sizeChange) > 1) z -= sizeChange;
        }
        break;
    }
    size.addLocal(x, y, z);

    Block mutatedBlock;

    if(isRoot) mutatedBlock = randomCreature.addRoot(center,size);
    else
    {
      Block parent = randomCreature.getBlockByID(originalBlock.getIdOfParent());
      Vector3f pivotA = new Vector3f(originalBlock.getJoint().getPivotA());
      Vector3f pivotB = new Vector3f(originalBlock.getJoint().getPivotB());
      mutatedBlock = randomCreature.addBlock(center,size,parent,pivotA,pivotB,Vector3f.UNIT_Z,Vector3f.UNIT_Z);
      copyBlockNeurons(originalBlock,mutatedBlock);
    }

    Vector3f mutatedSize = new Vector3f(x,y,z);

    return !size.equals(mutatedSize);
  }

  /**
   * Mutates the structure of a block
   * Copies all other components of the sample creature
   * from the population that isn't being changed
   * @param sample single creature from the population
   * @param targetBlockID the id for the block that will be mutated
   */
  private void mutateBlock(OurCreature sample, int targetBlockID)
  {
    Block currentBlock;
    Vector3f center;
    Vector3f size;
    float sizeX;
    float sizeY;
    float sizeZ;

    randomCreature = new OurCreature(physicsSpace,rootNode);
    dna = new DNA(sample);

    for(int i = 0; i < sample.getNumberOfBodyBlocks(); i++)
    {
      currentBlock = sample.getBlockByID(i);
      sizeX = currentBlock.getSizeX()/2;
      sizeY = currentBlock.getSizeY()/2;
      sizeZ = currentBlock.getSize()/2;
      center = new Vector3f(dna.getBlockCenter(i));
      size = new Vector3f(sizeX,sizeY,sizeZ);

      if(i != targetBlockID)
      {
        if(i != 0)
        {
          deepCopyBlock(currentBlock,center,size);
        }
        else randomCreature.addRoot(center,size);
      }
      else//Mutate Block here
      {
        if(i == 0) mutateBlockSize(currentBlock,center,size,true);
        else
        {
          mutateBlockSize(currentBlock,center,size, false);
        }
      }
    }
  }

  /**
   * Will perform the hill climbing on the given population when called.
   * TODO: will need it to go through the population
   */
  public void hillClimb() {
    int blockID;
    final int MAX_NUM_BLOCKS = creature.getNumberOfBodyBlocks();
    //NOTE: may want mutateBlock to return boolean then if all mutates don't improve fitness come back here and choose new block
    blockID = generator.nextInt(MAX_NUM_BLOCKS);
    mutateBlock(creature, blockID);
  }

  /**
   * Grabs a creature from the hill climbing to show
   * TODO: grap a random creature from population, or pick best fit one
   * @return creature from this population
   */
  public OurCreature getCreature(){return randomCreature;}

}
