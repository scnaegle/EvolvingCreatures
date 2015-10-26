package vcreature.hillClimbing;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.creatureUtil.DNA;
import vcreature.phenotype.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by zfalgout on 10/15/15.
 */
public class HillClimbing
{
  private ArrayList<DNA> population;
  private HashMap<DNA, Float> dna_fitness = new HashMap<>();
  private ArrayList<Float> elapsedTime = new ArrayList<Float>();
  private DNA bestfitDNA;
  private PhysicsSpace physicsSpace;
  private Node rootNode;
  private Random generator;
  private final int NEURON_COUNT = Neuron.TOTAL_INPUTS;
  private float bestFitness = 0.0f;

  public HillClimbing(ArrayList<DNA> population, PhysicsSpace space, Node root)
  {
    this.population = population;
    physicsSpace = space;
    rootNode = root;
    generator = new Random();
  }

  /**
   * Simulates the creature and gets the fitness of it.
   * At moment, creature will attempt to jump twice, need
   * to decouple creatures from gui
   * @param sampleDNA DNA of the creature to do fitness test on
   * @return fitness value of creature
   */
  private float fitnessTest(DNA sampleDNA)
  {
//    float fitness;
//
//    OurCreature sample = new OurCreature(physicsSpace,rootNode,sampleDNA);
//    sample.placeOnGround();
//    System.out.println("lowest point " + sample.getLowestPoint());
//    System.out.println("Fitness before :" + sample.getFitness());
//
//    for(float time : elapsedTime) sample.updateBrain(time);
//
//    fitness = sample.getFitness();
//    System.out.println("Fitness after: " + fitness);
//    sample.detach();
//    return fitness;
    if (sampleDNA == null || !dna_fitness.containsKey(sampleDNA)) {
      return 0;
    } else {
      System.out.println("dna_fitness: " + dna_fitness);
      System.out.println("sample DNA: " + sampleDNA);
      return dna_fitness.get(sampleDNA);
    }
  }

  public void setDNAFitness(DNA dna, float fitness) {
    dna_fitness.put(dna, fitness);
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
    return !(originalSize.equals(size));
  }

  private boolean mutateBlockAngles(DNA dna, int targetID)
  {

    return false;
  }

  //TODO: mutate neuron: DNA has alterNeuronInput, alterNeuronConstant, alterNeuronBlock
  private boolean mutateBlockNeuron(int targetID)
  {
    return false;
  }

  /**
   * Mutates the DNA of the target block given by the ID
   * //TODO: add more info
   * @param originalDNA DNA of the creature before mutation
   * @param mutatedDNA DNA copy of the originalDNA to mutate without losing information
   * @param targetBlockID ID of the block that is being mutated
   * @return if mutation is successful or not
   */
  private boolean mutateBlock(DNA originalDNA, DNA mutatedDNA, int targetBlockID)
  {
    Vector3f size = new Vector3f(originalDNA.getBlockSize(targetBlockID));
    float originalFitness = fitnessTest(originalDNA);
    System.out.println("original fitness: " + originalFitness);
    float testFitness;
    if(originalFitness > bestFitness) bestfitDNA = originalDNA;
    //TODO: can store fitness into DNA now
    if(mutateBlockSize(mutatedDNA, size, targetBlockID))
    {
      testFitness = fitnessTest(mutatedDNA);
      System.out.println("New fitness: " + testFitness);
      if(originalFitness < testFitness) return true;
      else mutatedDNA = new DNA(originalDNA);
      return true; //TODO: remove once fitnessTest is correct
    }
    else if(mutateBlockAngles(mutatedDNA, targetBlockID));
    else if(mutateBlockNeuron(targetBlockID));
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
    elapsedTime.add(time);
  }

  /**
   * Will perform the hill climbing on the given population when called.
   */
  public void hillClimb() {
    int blockID;
    int index = 0;
    boolean isMutated = false;
    DNA dna;
    DNA mutatedDNA;
    ArrayList<DNA> mutatedPopulation = new ArrayList<DNA>();

    for(int i = 0; i < population.size(); i++)
    {
      dna = population.remove(i);
      mutatedDNA = new DNA(dna);
      if(bestfitDNA == null) bestfitDNA = dna;
      final int MAX_NUM_BLOCKS = dna.getNumBlocks();

      while(index < MAX_NUM_BLOCKS && !isMutated)
      {
        blockID = generator.nextInt(MAX_NUM_BLOCKS);
        if(mutateBlock(dna, mutatedDNA, blockID)) isMutated = true;
        index++;
      }

      if(isMutated) mutatedPopulation.add(mutatedDNA);
      else mutatedPopulation.add(dna);
    }
    population = mutatedPopulation;
    bestfitDNA = population.get(0); //TODO: remove once fitnessTest if correct

  }

  /**
   * Grabs the DNA from the population that has the best fitness.
   * Should be used to show the current best fit creature of a population
   * in the GUI
   * @return DNA of the best fit creature
   */
  public DNA getBestfitDNA(){return bestfitDNA;}

}
