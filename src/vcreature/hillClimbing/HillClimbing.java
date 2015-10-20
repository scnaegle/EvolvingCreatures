package vcreature.hillClimbing;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.creatureUtil.DNA;
import vcreature.phenotype.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zfalgout on 10/15/15.
 */
public class HillClimbing
{
  private ArrayList<OurCreature> population;
  private ArrayList<OurCreature> mutatedPopulation;
  private OurCreature randomCreature;
  private PhysicsSpace physicsSpace;
  private Node rootNode;
  private Random generator;
  private DNA dna;
  private final int NEURON_COUNT = Neuron.TOTAL_INPUTS;
  private float elapsedTime = 0.0f;

  public HillClimbing(ArrayList<OurCreature> population, PhysicsSpace space, Node root)
  {
    this.population = population;
    mutatedPopulation = new ArrayList<OurCreature>();
    //creature = sample;
    physicsSpace = space;
    rootNode = root;
    generator = new Random();
  }

  /**
   * Simulates the creature and gets the fitness of it.
   * At moment, creature will attempt to jump twice, need
   * to decouple creatures from gui
   * @param sample creature to do fitness test on
   * @return fitness value of creature
   */
  private float fitnessTest(Creature sample)
  {
    sample.updateBrain(elapsedTime);
    return sample.getFitness();
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
    Vector3f originalSize = new Vector3f(size);
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
    //TODO: use new add block methods
    if(isRoot) mutatedBlock = randomCreature.addRoot(center,size);
    else
    {
      Block parent = randomCreature.getBlockByID(originalBlock.getIdOfParent());
      Vector3f pivotA = new Vector3f(originalBlock.getJoint().getPivotA());
      Vector3f pivotB = new Vector3f(originalBlock.getJoint().getPivotB());
      mutatedBlock = randomCreature.addBlock(center,size,parent,pivotA,pivotB,Vector3f.UNIT_Z,Vector3f.UNIT_Z);
      copyBlockNeurons(originalBlock,mutatedBlock);
    }
    return !originalSize.equals(size);
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
          if(mutateBlockSize(currentBlock,center,size, false))
          {
            //TODO: test fitness
          }
        }
        System.out.println("Simulated fitness: " + fitnessTest(randomCreature));

      }
    }
  }

  /**
   * Gives Hill Climbing info about how much time has gone by
   * in the simulation.  Need the time inorder to do fitness
   * calculations
   * @param time passed in the simulation
   */
  public void setElapsedTime(float time)
  {
    elapsedTime = time;
  }

  /**
   * Will perform the hill climbing on the given population when called.
   * TODO: will need it to go through the population
   */
  public void hillClimb() {
    int blockID;
    OurCreature creature;
    for(int i = 0; i < population.size(); i++)
    {
      creature = population.remove(i);
      OurCreature clone = new OurCreature(physicsSpace,null); //TODO: test to see if clone will jump without affecting GUI
      final int MAX_NUM_BLOCKS = creature.getNumberOfBodyBlocks();
      //NOTE: may want mutateBlock to return boolean then if all mutates don't improve fitness come back here and choose new block
      blockID = generator.nextInt(MAX_NUM_BLOCKS);
      mutateBlock(creature, blockID);
      mutatedPopulation.add(randomCreature);
    }
    population = mutatedPopulation;
  }

  /**
   * Grabs a creature from the hill climbing to show
   * TODO: grab a random creature from population, or pick best fit one
   * @return creature from this population
   */
  public OurCreature getCreature(){return randomCreature;}

}
