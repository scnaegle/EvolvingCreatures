//TODO: 1st mutateBlocks

package vcreature.hillClimbing;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.creatureUtil.DNA;
import vcreature.creatureUtil.RandomCreature;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;
import vcreature.phenotype.EnumNeuronInput;
import vcreature.phenotype.Neuron;

import java.util.Random;

/**
 * Created by zfalgout on 10/15/15.
 */
public class HillClimbing
{
  private Creature creature;//TODO: can remove after Hill Climbing is set to take in population
  private Creature mutatedCreature;
  private Creature randomCreature;
  private PhysicsSpace physicsSpace;
  private Node rootNode;
  private Random generator;
  private DNA dna;
  private final int NEURON_COUNT = Neuron.TOTAL_INPUTS;

  //TODO: mock fitness test
  //TODO: to start, want to print out creature info to see if HillClimbing is changing creature

  //TODO: take in arraylist of creatures, population
  public HillClimbing(Creature sample, PhysicsSpace space, Node root)
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

      block.addNeuron(neuron);

    }
  }

  /**
   * Mutates the structure of a block
   * Copies all other components of the sample creature
   * from the population that isn't being changed
   * @param sample single creature from the population
   * @param targetBlockID the id for the block that will be mutated
   */
  private void mutateBlockStructure(Creature sample, int targetBlockID)
  {
    Block currentBlock;
    Vector3f center;
    Vector3f size;
    float sizeX;
    float sizeY;
    float sizeZ;

    randomCreature = new Creature(physicsSpace,rootNode);
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
        if(i == 0) randomCreature.addRoot(center,size);
        else
        {
          deepCopyBlock(currentBlock,center,size);
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

    blockID = generator.nextInt(MAX_NUM_BLOCKS);
    mutateBlockStructure(creature, blockID);
  }

  /**
   * Grabs a creature from the hill climbing to show
   * TODO: grap a random creature from population, or pick best fit one
   * @return creature from this population
   */
  public Creature getCreature(){return randomCreature;}

}
