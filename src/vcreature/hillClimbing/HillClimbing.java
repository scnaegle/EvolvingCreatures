package vcreature.hillClimbing;

import com.google.common.collect.Iterables;
import com.jme3.math.Vector3f;
import vcreature.creatureUtil.CreatureConstants;
import vcreature.creatureUtil.DNA;
import vcreature.phenotype.*;

import java.util.ArrayList;
import java.util.Random;
/**
 * Created by zfalgout on 10/15/15.
 */
public class HillClimbing
{
  private ArrayList<ArrayList<DNA>> population;
  private Random generator;
  private float overallAvgFitness;
  private float previousOverallAvgFitness;
  private int MutationThresholdCount;
  private final float POPULATION_DEVIATION_THRESHOLD = 0.2f;
  private final EnumNeuronInput[] NEURON_INPUT = {EnumNeuronInput.HEIGHT, EnumNeuronInput.TOUCH, EnumNeuronInput.JOINT,
          EnumNeuronInput.CONSTANT, EnumNeuronInput.TIME};
  private final int NEURON_COUNT = NEURON_INPUT.length;
  private boolean mutationNeeded;

  //TODO: javadoc here
  public HillClimbing(ArrayList<ArrayList<DNA>> population)
  {
    this.population = population;
    generator = new Random();
    MutationThresholdCount = 0;
    overallAvgFitness = 0f;
    previousOverallAvgFitness = 0f;
    mutationNeeded = false;
  }

  /**
   * Checks to see if the new fitness of the sample DNA
   * is better than the previous DNA's fitness
   * @param sampleDNA that is being mutated
   * @param previousDNA of last generation to test against
   * @return if new fitness is better than old fitness
   */
  private boolean fitnessTest(DNA sampleDNA, DNA previousDNA)
  {
    if(previousDNA == null) return true;
    float fitness = sampleDNA.getFitness();
    overallAvgFitness += fitness;
    return fitness > previousDNA.getFitness();
  }

  /**
   * Mutates a block size by altering the dna
   * Will pick x,y, or z at random and pick a random
   * float change up to 1.0 to add to the selected direction
   * @param sampleDNA DNA that is being mutated
   * @param size original size of the block in the dna
   * @param blockID the ID of the block being mutated
   */
  private void mutateBlockSize(DNA sampleDNA, Vector3f size, int blockID)
  {
    float x = 0;
    float y = 0;
    float z = 0;
    float temp;
    float sizeChange = generator.nextFloat();//will get float between 0.0 and 1.0

    if(sizeChange == 0.0) sizeChange += .1;

    boolean addOp = generator.nextBoolean();
    boolean sizeNot10 = size.getX() < CreatureConstants.MAX_BLOCK_SIZE && size.getY() < CreatureConstants.MAX_BLOCK_SIZE && size.getZ() < CreatureConstants.MAX_BLOCK_SIZE;
    boolean sizeNot1 = size.getX() > CreatureConstants.MIN_BLOCK_SIZE && size.getY() > CreatureConstants.MIN_BLOCK_SIZE && size.getZ() > CreatureConstants.MIN_BLOCK_SIZE;

    switch (generator.nextInt(3))
    {
      case(0):
        temp = x;
        if(addOp && sizeNot10)
        {
          if((temp + sizeChange) < CreatureConstants.MAX_BLOCK_SIZE) x += sizeChange;
        }
        else if(sizeNot1)
        {
          if((temp - sizeChange) > CreatureConstants.MIN_BLOCK_SIZE) x -= sizeChange;
        }
        break;
      case(1):
        temp = y;
        if(addOp && sizeNot10)
        {
          if((temp + sizeChange) < CreatureConstants.MAX_BLOCK_SIZE) y += sizeChange;
        }
        else if(sizeNot1)
        {
          if((temp - sizeChange) > CreatureConstants.MIN_BLOCK_SIZE) y -= sizeChange;
        }
        break;
      default:
        temp = z;
        if(addOp && sizeNot10)
        {
          if((temp + sizeChange) < CreatureConstants.MAX_BLOCK_SIZE) z += sizeChange;
        }
        else if(sizeNot1)
        {
          if((temp - sizeChange) > CreatureConstants.MIN_BLOCK_SIZE) z -= sizeChange;
        }
        break;
    }
    size.addLocal(x, y, z);
    sampleDNA.alterVector(size, blockID, BlockVector.SIZE);
    sampleDNA.alterJoints(blockID);
  }

  /**
   * Mutates a block's neuron input type
   * @param mutatedDNA DNA being mutated
   * @param neuronID which neuron of the block being mutated
   * @param blockID ID of block
   */
  private void mutateNeuronInput(DNA mutatedDNA, int neuronID, int blockID)
  {
    int mutatingNeuron = generator.nextInt(NEURON_COUNT);
    int newType = mutatingNeuron; //New Neuron type

    while(newType == mutatingNeuron) newType = generator.nextInt(NEURON_COUNT);

    EnumNeuronInput newInput = NEURON_INPUT[newType];

    mutatedDNA.alterNeuronInput(blockID,neuronID,mutatingNeuron,newInput);
  }

  /**
   * Mutates a block's neuron constant value
   * @param mutatedDNA DNA being mutated
   * @param neuronID which neuron of the block being mutated
   * @param blockID ID of block
   */
  private void mutateNeuronConstant(DNA mutatedDNA, int neuronID, int blockID)
  {
    int mutatingNeuron = generator.nextInt(NEURON_COUNT);
    float mutatingConstant = mutatedDNA.getNeuronConstant(blockID,neuronID,mutatingNeuron);

    if(mutatingConstant != -1)
    {
      float mutateChange = generator.nextFloat();
      boolean addOp = generator.nextBoolean();

      if(addOp) mutatingConstant += mutateChange;
      else if((mutatingConstant -= mutateChange) < 0) mutatingConstant = 0;

      mutatedDNA.alterNeuronConstant(blockID,neuronID,mutatingNeuron,mutatingConstant);
    }
  }

  /**
   * Swaps 2 neuron inputs of a block
   * @param mutatedDNA DNA being mutated
   * @param neuronID which neuron of the block being mutated
   * @param blockID ID of block
   */
  private void swapNeuronInput(DNA mutatedDNA, int neuronID, int blockID)
  {
    int neuronA = generator.nextInt(NEURON_COUNT);
    int neuronB = neuronA;

    while(neuronB == neuronA) neuronB = generator.nextInt(NEURON_COUNT);

    EnumNeuronInput inputA = NEURON_INPUT[neuronA];
    EnumNeuronInput inputB = NEURON_INPUT[neuronB];

    mutatedDNA.alterNeuronInput(blockID,neuronID,neuronA,inputB);
    mutatedDNA.alterNeuronInput(blockID,neuronID,neuronB,inputA);
  }

  /**
   * Swaps 2 neuron constants of a block
   * @param mutatedDNA DNA being mutated
   * @param neuronID which neuron of block being mutated
   * @param blockID ID of block
   */
  private void swapNeuronConstants(DNA mutatedDNA, int neuronID, int blockID)
  {
    int neuronA = generator.nextInt(NEURON_COUNT);
    int neuronB = neuronA;

    while(neuronB == neuronA) neuronB = generator.nextInt(NEURON_COUNT);

    float constantA = mutatedDNA.getNeuronConstant(blockID,neuronID,neuronA);
    float constantB = mutatedDNA.getNeuronConstant(blockID,neuronID,neuronB);

    if(constantA != -1 && constantB != -1)
    {
      mutatedDNA.alterNeuronConstant(blockID,neuronID,neuronA,constantB);
      mutatedDNA.alterNeuronConstant(blockID,neuronID,neuronB,constantA);
    }
  }

  /**
   * Mutates a DNA's block's neuron
   * @param mutatedDNA DNA being mutated
   * @param blockID ID of block
   */
  private void mutateBlockNeuron(DNA mutatedDNA, int blockID)
  {
    int numOfNeurons = mutatedDNA.getNumRules(blockID);
    int neuronID = generator.nextInt(numOfNeurons);
    int mutationType = generator.nextInt(4);

    switch(mutationType)
    {
      case(0):
        mutateNeuronInput(mutatedDNA,neuronID,blockID);
        break;
      case(1):
        mutateNeuronConstant(mutatedDNA,neuronID,blockID);
        break;
      case(2):
        swapNeuronInput(mutatedDNA,neuronID,blockID);
        break;
      default:
        swapNeuronConstants(mutatedDNA,neuronID,blockID);
        break;
    }
  }

  /**
   * Mutates the DNA of the target block given by the ID, and will
   * do the mutation given by the mutation type
   * @param originalDNA DNA of the creature before mutation
   * @param mutatedDNA DNA copy of the originalDNA to mutate without losing information
   * @param targetBlockID ID of the block that is being mutated
   * @param mutationType which mutation will be done
   */
  private void mutateBlock(DNA originalDNA, DNA mutatedDNA, int targetBlockID, int mutationType)
  {
    Vector3f size = new Vector3f(originalDNA.getBlockSize(targetBlockID));

    if(targetBlockID == 0) mutationType = 1;

    switch (mutationType)
    {
      case 0:
        mutateBlockNeuron(mutatedDNA, targetBlockID);
        break;
      default:
        mutateBlockSize(mutatedDNA, size, targetBlockID);
        break;
    }
  }

  /**
   * Will check if the entire population's avg fitness is getting better, worse,
   * or increase is small.  If overall avg fitness is worse than the last generation's
   * overall avg fitness, than the counter for how many times a decrease is allowed is
   * incremented.  If decrease happens 3 times, then a mutation is needed.
   * If the abs deviation between this generation and last generation's fitness, is less
   * than the stall deviation for a population, then a mutation is needed.
   */
  private void checkAvgFitnessDeviation()
  {
    if(overallAvgFitness < previousOverallAvgFitness) MutationThresholdCount++;
    float deviation = overallAvgFitness - previousOverallAvgFitness;
    if(Math.abs(deviation) < POPULATION_DEVIATION_THRESHOLD) MutationThresholdCount++;
    if(MutationThresholdCount == 5) mutationNeeded = true;
  }

  /**
   * Checks to see if Hill Climbing is in need of the
   * genetic algorithm to do mutations.
   * @return if mutation is needed
   */
  public boolean isMutationNeeded()
  {
    return mutationNeeded;
  }

  /**
   * Gets the over all avg fitness for the entire population
   * @return avg fitness for all DNAs in population
   */
  public float getPopulationAvgFitness(){return overallAvgFitness;}

  /**
   * Will perform the hill climbing on the given population when called.
   */
  public ArrayList<ArrayList<DNA>> hillClimb() {
    int blockID;
    int dnaListSize;
    int mutationType; //0 for neuron mutation, else size mutation of block
    int totalNumDNA = population.size();

    DNA dna;
    DNA previousDNA;
    DNA mutatedDNA;

    overallAvgFitness = 0f;
    //TODO: need to keep track of what mutations were done, want to try to repeat if fitnessTest is true

    for(int i = 0; i < totalNumDNA; i++)
    {
      dna = Iterables.getLast(population.get(i));
      dnaListSize = population.get(i).size();
      if(dnaListSize > 1) previousDNA = Iterables.get(population.get(i), dnaListSize - 2);
      else previousDNA = null;

      final int MAX_NUM_BLOCKS = dna.getNumBlocks();

      if(fitnessTest(dna, previousDNA))
      {
        blockID = generator.nextInt(MAX_NUM_BLOCKS);//TODO: weighted selection
        mutatedDNA = new DNA(dna);
      }
      else
      {
        mutatedDNA = new DNA(previousDNA);
        population.get(i).remove(dnaListSize-1);
        blockID = generator.nextInt(MAX_NUM_BLOCKS);
      }

      mutationType = generator.nextInt(2);

      mutateBlock(dna, mutatedDNA, blockID, mutationType);
      population.get(i).add(mutatedDNA);

    }

    overallAvgFitness /= totalNumDNA;
    checkAvgFitnessDeviation();
    previousOverallAvgFitness = overallAvgFitness;
    System.out.println("Overall Fitness: " + overallAvgFitness);
    return population;
  }
}
