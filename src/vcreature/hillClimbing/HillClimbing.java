package vcreature.hillClimbing;

import com.google.common.collect.Iterables;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.creatureUtil.CreatureConstants;
import vcreature.creatureUtil.DNA;
import vcreature.phenotype.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by zfalgout on 10/15/15.
 */
public class HillClimbing
{
  private ArrayList<ArrayList<DNA>> population;
  private Random generator;
  private final int NEURON_COUNT = Neuron.TOTAL_INPUTS;

  public HillClimbing(ArrayList<ArrayList<DNA>> population)
  {
    this.population = population;
    generator = new Random();
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
    return sampleDNA.getFitness() > previousDNA.getFitness();
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
  private void mutateBlockSize(DNA dna, Vector3f size, int targetID)
  {
    float x = 0;
    float y = 0;
    float z = 0;
    float temp;
    float sizeChange = generator.nextFloat();//will get float between 0.0 and 1.0

    if(sizeChange == 0.0) sizeChange += .1;

    boolean addOp = generator.nextInt(2) == 0;
    boolean sizeNot10 = size.getX() < CreatureConstants.MAX_BLOCK_SIZE && size.getY() < CreatureConstants.MAX_BLOCK_SIZE && size.getZ() < CreatureConstants.MAX_BLOCK_SIZE;
    boolean sizeNot1 = size.getX() > CreatureConstants.MIN_BLOCK_SIZE && size.getY() > CreatureConstants.MIN_BLOCK_SIZE && size.getZ() > CreatureConstants.MIN_BLOCK_SIZE;
    //TODO: shift the joint to be at the edge again
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
    dna.alterVector(size, targetID, BlockVector.SIZE);
  }


  //TODO: mutate neuron: DNA has alterNeuronInput, alterNeuronConstant, alterNeuronBlock
  private void mutateBlockNeuron(int targetID)
  {
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
    //TODO: need to keep track of what mutations were done, want to repeat if fitnessTest is true
    Vector3f size = new Vector3f(originalDNA.getBlockSize(targetBlockID));

    switch (mutationType)
    {
      case 0:
        mutateBlockNeuron(targetBlockID);
        break;
      default:
        mutateBlockSize(mutatedDNA, size, targetBlockID);
        break;
    }
  }

  /**
   * Will perform the hill climbing on the given population when called.
   */
  public ArrayList<ArrayList<DNA>> hillClimb() {
    int blockID;
    int dnaListSize;
    int mutationType; //0 for neuron mutation, else size mutation of block
    DNA dna;
    DNA previousDNA;
    DNA mutatedDNA;

    for(int i = 0; i < population.size(); i++)
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
        blockID = generator.nextInt(MAX_NUM_BLOCKS);
      }
      if(previousDNA != null) System.out.println("current fitness vs old fitness" + dna.getFitness() + " " + previousDNA.getFitness());
      mutationType = generator.nextInt(2);

      mutateBlock(dna, mutatedDNA, blockID, mutationType);
      population.get(i).add(mutatedDNA);

    }

    return population;
  }
}
