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
  //TODO: need to select random block to do mutation on
  //TODO: if block is child, need to check if we will mutate a child's child and so on
  //TODO: will want to mutate HCFlappyBird for testing, and see if GUI will update it
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
   * Mutates the structure of a block
   * Copies all other components of the sample creature
   * from the population that isn't being changed
   * @param sample single creature from the population
   * @param targetBlockID the id for the block that will be mutated
   */
  private void mutateBlockStructure(Creature sample, int targetBlockID)
  {
    //TODO: deepClone methods want method to do copying for me instead of in here
    Block currentBlock;
    Block block;
    Block parent;
    Vector3f center;
    Vector3f size;
    Vector3f pivotA;
    Vector3f pivotB;
    Neuron neuron;
    EnumNeuronInput a;
    EnumNeuronInput b;
    EnumNeuronInput c;
    EnumNeuronInput d;
    EnumNeuronInput e;
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
          parent = randomCreature.getBlockByID(currentBlock.getIdOfParent());
          pivotA = new Vector3f(currentBlock.getJoint().getPivotA());
          pivotB = new Vector3f(currentBlock.getJoint().getPivotB());
          block = randomCreature.addBlock(center,size,parent,pivotA,pivotB,Vector3f.UNIT_Z,Vector3f.UNIT_Z);
          //loop through neuron table
          for(Neuron blockNeuron : currentBlock.getNeuronTable())
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
        else randomCreature.addRoot(center,size);
      }
      else//Mutate Block here
      {
        if(i == 0) randomCreature.addRoot(center,size);
        else
        {
          parent = randomCreature.getBlockByID(currentBlock.getIdOfParent());
          pivotA = new Vector3f(currentBlock.getJoint().getPivotA());
          pivotB = new Vector3f(currentBlock.getJoint().getPivotB());
          block = randomCreature.addBlock(center,size,parent,pivotA,pivotB,Vector3f.UNIT_Z,Vector3f.UNIT_Z);
          //loop through neuron table
          for(Neuron blockNeuron : currentBlock.getNeuronTable())
          {
            a = blockNeuron.getInputType(0);
            b = blockNeuron.getInputType(1);
            c = blockNeuron.getInputType(2);
            d = blockNeuron.getInputType(3);
            e = blockNeuron.getInputType(4);
            neuron = new Neuron(a,b,c,d,e);

            for(int j = 0; j < NEURON_COUNT; j++)
            {
              neuron.setInputValue(j,blockNeuron.getInputValue(j));
            }

            block.addNeuron(neuron);

          }
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
