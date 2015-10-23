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
  private DNA bestfitDNA;
  private PhysicsSpace physicsSpace;
  private Node rootNode;
  private Random generator;
  private final int NEURON_COUNT = Neuron.TOTAL_INPUTS;
  private float elapsedTime = 0.0f;
  private float bestFitness = 0.0f;

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
    System.out.println("Fitness before :" + sample.getFitness());
    System.out.println("Update brain: " + sample.updateBrain(elapsedTime));
    return sample.getFitness();
  }

  /**
   * Will add a new block that is an exact copy of originalBlock
   * to the creature being modified
   * @param originalBlock from the creature
   * @param center of the new block
   * @param size of the new block
   */
  /*private void deepCopyBlock(Block originalBlock, Vector3f center, Vector3f size)
  {
    Block parent = randomCreature.getBlockByID(originalBlock.getIdOfParent());
    Vector3f pivotA = new Vector3f(originalBlock.getJoint().getPivotA());
    Vector3f pivotB = new Vector3f(originalBlock.getJoint().getPivotB());
    Block block = randomCreature.addBlock(center,size,parent,pivotA,pivotB,Vector3f.UNIT_Z,Vector3f.UNIT_Z);

    copyBlockNeurons(originalBlock, block);
  }*/

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
   * Mutates a block size by altering the dna
   * Will pick x,y, or z at random and pick a random
   * float change up to 1.0 to add to the selected direction
   * @param dna DNA that is being mutated
   * @param size original size of the block in the dna
   * @param targetID the ID of the block being mutated
   * @return if new size is different from original size
   */
  private boolean mutateBlockSize(DNA dna, Vector3f size, int targetID)
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
    dna.alterVector(size, targetID, BlockVector.SIZE);

    return !originalSize.equals(size);
  }

  //TODO: mutate neuron
  public boolean mutateBlockNeuron()
  {
    return false;
  }

  /**
   * Mutates the DNA of the target block given by the ID
   * //TODO: add more info
   * @param originalDNA DNA of the creature before mutation
   * @param mutatedDNA DNA copy of the originalDNA to mutate without losing information
   * @param sample OurCreature from the given population to due fitness test on //TODO: will probably want to remove
   * @param targetBlockID ID of the block that is being mutated
   * @return if mutation is successful or not
   */
  private boolean mutateBlock(DNA originalDNA, DNA mutatedDNA, OurCreature sample, int targetBlockID)
  {
    Vector3f size = originalDNA.getBlockSize(targetBlockID);
    OurCreature fitnessTestCreature;
    float originalFitness = fitnessTest(sample);
    float testFitness;
    if(originalFitness > bestFitness) bestfitDNA = originalDNA;

    if(mutateBlockSize(mutatedDNA, size, targetBlockID))
    {
      fitnessTestCreature = new OurCreature(physicsSpace,rootNode,mutatedDNA);
      testFitness = fitnessTest(fitnessTestCreature);
      fitnessTestCreature.detach();
      System.out.println("New fitness: " + testFitness);
      if(originalFitness < testFitness) return true;
    }

    return false;
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
   */
  public void hillClimb() {
    int blockID;
    int index = 0;
    OurCreature creature;
    boolean isMutated = false;
    DNA dna;
    DNA mutatedDNA;
    for(int i = 0; i < population.size(); i++)
    {
      creature = population.remove(i);
      dna = new DNA(creature);
      mutatedDNA = new DNA(creature);
      final int MAX_NUM_BLOCKS = creature.getNumberOfBodyBlocks();

      while(index < MAX_NUM_BLOCKS && !isMutated)
      {
        blockID = generator.nextInt(MAX_NUM_BLOCKS);
        if(mutateBlock(dna, mutatedDNA, creature, blockID)) isMutated = true;
        index++;
      }

      if(isMutated) mutatedPopulation.add(new OurCreature(physicsSpace,rootNode,bestfitDNA));
      else mutatedPopulation.add(new OurCreature(physicsSpace,rootNode,dna));
    }
    population = mutatedPopulation;
  }

  /**
   * Grabs a creature from the hill climbing to show
   * TODO: grab a random creature from population, or pick best fit one
   * @return creature from this population
   */
  public OurCreature getCreature()
  {
    return mutatedPopulation.get(0);
  }

}
