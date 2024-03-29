package vcreature.creatureUtil;


import com.jme3.math.Vector3f;
import jogamp.opengl.util.av.NullGLMediaPlayer;
import vcreature.mainSimulation.MainSim;
import vcreature.mainSimulation.Population;
import vcreature.phenotype.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Justin Thomas 10/14/2015
 * A class containing the DNA description of the block.  Will have a tostring
 * method for saving the creature, getters and setters to manipulate the block.
 * Will have a deep copy method/constructor.
 * Comparable on fitness.
 */

public class DNA implements Comparable
{
  private final int THIS = 0;
  private final int OTHER = 1;
  private Random rand;
  private int numBlocks;
  private int length;
  private BlockDNA[] blockDNAs;
  private DNA[] output;
  private Vector3f tempVec3;
  private float fitness;
  private String mutationSequence;
  private int mutationSequenceChance; //1/mutationSequenceChance of following muationSequence

  /**
   * Default constructor initializes all values to zero or null.
   */
  public DNA()
  {
    blockDNAs = new BlockDNA[CreatureConstants.MAX_BLOCKS];
    output = new DNA[2];
    rand = new Random();
    length = 0;
    tempVec3 = new Vector3f();
    fitness = 0;
    mutationSequence = null;
    mutationSequenceChance = 10;
  }

  /**
   * DNA Built from creature.  Pass in a built creature to make dna.
   * 1.) For each block in the creature call BlockDNA constructor.
   * Most of the work is done there.
   * 2.) Call populateVectorDNA with blockDNA index(corresponds to ID), and
   * sizeAndShape array(corresponds with OurCreature.blockProperties list
   * element).
   * @param c       Creature to build from.
   */
  public DNA(OurCreature c)
  {
    this();
    length = 0;
    numBlocks = c.getNumberOfBodyBlocks();
    for(int i = 0; i < numBlocks; ++i)
    {
      blockDNAs[i] = new BlockDNA(c.getBlockByID(i));
      c.populateVectorDNA(i, blockDNAs[i].sizeAndShape);
      blockDNAs[i].setAngles(c.getBlockAngles(i));
    }
  }

  /**
   * Construct from another DNA
   * @param other       otherDNA
   */
  public DNA(DNA other)
  {
    this();
    this.length = other.length;
    this.numBlocks = other.numBlocks;
    for(int i = 0; i < CreatureConstants.MAX_BLOCKS; ++i)
    {
      if(other.blockDNAs[i] != null)
      {
        this.blockDNAs[i] = new BlockDNA(other.blockDNAs[i]);
      }
      else
      {
        this.blockDNAs[i] = null;
      }
    }
    calculateNumBlocks();
    recalculateDNALength();
  }

  public DNA(RandCreature c)
  {
    this();
    length = 0;
    numBlocks = c.body.size();
    for (int i = 0; i < numBlocks; ++i)
    {
      blockDNAs[i] = new BlockDNA(c.body.get(i));
      c.populateVectorDNA(i, blockDNAs[i].sizeAndShape);
      blockDNAs[i].setAngles(c.getBlockAngles(i));
    }
    bumpUp();
  }

  /**
   * Construct DNA of desired size (called from DNAio)
   * @param size      value for numBlocks.
   */
  public DNA(int size)
  {
    this();
    numBlocks = size;
  }

  /**
   * Get the blockDNAs array.
   * @return
   */
  public BlockDNA[] getBlockDNAArray()
  {
    return blockDNAs;
  }

  /**
   * Get blockDNA at index i (id)
   * @param i
   * @return BlockDNA at blockDNAs[i]
   */
  public BlockDNA getBlockDNA(int i)
  {
    return blockDNAs[i];
  }
  /**
   * Store fitness in dna
   * @param newFitness        the fitness score
   */
  public void storeFitness(float newFitness)
  {
    fitness = newFitness;
  }

  /**
   * Get last fitness score.
   * @return        this fitness score.
   */
  public float getFitness()
  {
    return fitness;
  }

  /**
   * Get number of blocks according to DNA.
   * @return   numBlocks.
   */
  public int getNumBlocks()
  {
    return numBlocks;
  }

  /**
   * Get number of neuron rules for a block(neuronDNAs.size)
   * @param blockID       block to get number from.
   * @return              number of neuron rules, or -1 if blockID is invalid.
   */
  public int getNumRules(int blockID)
  {
    if(validateBlockIndex(blockID))
    {
      return blockDNAs[blockID].neuronDNAs.size();
    }
    return -1;
  }

  /**
   * Adjust the height of the creature.  Change the y values of
   * vectors that control creature's initial location
   * @param deltaY        amount to change by.
   */
  public void changeYLocations(float deltaY)
  {
    for(BlockDNA b : blockDNAs)
    {
      b.sizeAndShape[BlockVector.CENTER.ordinal()].y += deltaY;
      b.sizeAndShape[BlockVector.JOINT_A.ordinal()].y += deltaY;
      b.sizeAndShape[BlockVector.JOINT_B.ordinal()].y += deltaY;
    }
  }

  /**
   * Get center coordinates for a block.  This vector is separate from the
   * creature structure, but can be used to alter the DNA.
   * @param iD       int BlockID.
   * @return         Vector3f center coordinates, return null.
   */
  public Vector3f getBlockCenter(int iD)
  {
    for(BlockDNA bDNA : blockDNAs)
    {
      if(bDNA.blockID == iD)
      {
        return bDNA.sizeAndShape[BlockVector.CENTER.ordinal()];
      }
    }
    return null;
  }

  /**
   * Get size of a block.  Used to alter DNA
   * @param iD int BlockID
   * @return Vector3f size of block, return null
   */
  public Vector3f getBlockSize(int iD)
  {
    for(BlockDNA bDNA : blockDNAs)
    {
      if(bDNA.blockID == iD)
      {
        return bDNA.sizeAndShape[BlockVector.SIZE.ordinal()];
      }
    }
    return null;
  }

  /**
   * Get a specific Neuron's constant value of the given block
   * @param blockID ID of the block
   * @param neuronNum which neuron
   * @param constNum which neuron type's constant value
   * @return the constant value of the neuron, -1 if not a valid neuron
   */
  public float getNeuronConstant(int blockID, int neuronNum, int constNum)
  {
    if(validateNeuronIndices(blockID, neuronNum, constNum))
    {
      return blockDNAs[blockID].neuronDNAs.get(neuronNum).constantValues[constNum];
    }
    return -1;
  }

  /**
   * Gets the sequence of mutations this DNA went under in Hill Climbing
   * @return String representation of mutation sequence
   */
  public String getMutationSequence(){return mutationSequence;}

  /**
   * Sets the sequence of mutations for this DNA
   * Called from Hill Climbing
   * @param sequence String sequence of mutations
   */
  public void setMutationSequence(String sequence){mutationSequence = sequence;}

  /**
   * Gets the chance that Hill Climbing will follow the
   * mutation sequence of this DNA.
   * @return chance of following sequence
   */
  public int getMutationSequenceChance()
  {
    if(mutationSequenceChance < 2) return 2;
    return mutationSequenceChance;
  }

  /**
   * Alter the chance of following the mutation sequence
   * @param change being add/subtracted to chance of following mutation sequence
   */
  public void changeMutationSequenceChance(int change){mutationSequenceChance += change;}

  /**
   * Build creature with DNA.  Called from OurCreature constructor when a
   * creature is being built from DNA. What happens:
   * 1.) Call add root.
   * 2.) For each blockDNA that is not null, call addBlock, if angles aren't
   * included, it sends {0,0,0} as the angle
   * 3.) Call BlockDNA.add neurons with the current block as the argument to add
   * neuron info.
   * @param c       Creature to build.
   */
  public void initializeCreature(Creature c)
  {
    //create root block
    c.addRoot(newVector(0, BlockVector.CENTER), newVector(0, BlockVector.SIZE),
                        blockDNAs[0].angles);
    for(int i = 1; i < CreatureConstants.MAX_BLOCKS; ++i)
    {
      //if there is dna, and it's parent exists, add block
      if(blockDNAs[i] != null && c.getBlockByID(blockDNAs[i].parentID) != null)
      {
        BlockDNA bDNA = blockDNAs[i];
        if(bDNA.angles == null)
        {
          c.addBlock(CreatureConstants.IDENTITY_QUATERNION,
                      newVector(i, BlockVector.SIZE),
                      c.getBlockByID(bDNA.parentID),
                      newVector(i, BlockVector.JOINT_A),
                      newVector(i, BlockVector.JOINT_B),
                      newVector(i, BlockVector.AXIS_A));
        }
        else
        {
          c.addBlock(bDNA.angles, newVector(i, BlockVector.SIZE),
                                            c.getBlockByID(bDNA.parentID),
                                            newVector(i, BlockVector.JOINT_A),
                                            newVector(i, BlockVector.JOINT_B),
                                            newVector(i, BlockVector.AXIS_A));
        }
        blockDNAs[i].addNeurons(c.getBlockByID(blockDNAs[i].blockID));
      }
    }
  }

  /**
   * Get length of DNA that is used for crossover. (Maybe random access?)
   * blocks * (vectors + 1) + blocks * (numNeurons * 5)
   * @return      the length of the dna used for crossover
   */
  public int getLength()
  {
    return length;
  }


  /**
   * Alter a block's angle array.  Pass in new length 3 float array of the
   * format {angleX, angleY, angleZ} (in radians)  This sets the angle array in
   * the DNA. Joel's Creature constructor converts to a Quaternion, we don't
   * have to worry about it.
   * @param newAngles       new value for the angles.
   * @param id              blockDNA to alter.
   */
  public void alterAngles(float[] newAngles, int id)
  {
    if(validateBlockIndex(id))
    {
      blockDNAs[id].angles = Arrays.copyOf(newAngles, 3);
    }
  }

  /**
   * Alter angle array with primitive values
   * @param x       float x angle
   * @param y       float y angle
   * @param z       float z angle
   * @param id      block id
   */
  public void alterAngles(float x, float y, float z, int id)
  {
    if(validateBlockIndex(id))
    {
      blockDNAs[id].angles[0] = x;
      blockDNAs[id].angles[1] = y;
      blockDNAs[id].angles[2] = z;
    }
  }

  /**
   * Alter one of a block's size and shape vectors.  Specified by BlockVector
   * enum.
   * @param newVector       new value for the vector.
   * @param id              blockDNA to alter.
   * @param type            the type of vector to alter.
   */
  public void alterVector(Vector3f newVector, int id, BlockVector type)
  {
    if(validateBlockIndex(id))
    {
      blockDNAs[id].sizeAndShape[type.ordinal()] = new Vector3f(newVector);
    }
  }

  /**
   * Alter vector with primitive values.
   * @param x       float x value of vector.
   * @param y       float y value of vector.
   * @param z       float z value of vector.
   * @param id      block id.
   * @param type    Blockvector.ordinal()
   */
  public void alterVector(float x, float y, float z, int id, int type)
  {
    if(validateBlockIndex(id))
    {
      blockDNAs[id].sizeAndShape[type] = new Vector3f(x, y, z);
    }
  }

  /**
   * Alters the joints of the block with the given id
   * so the joints are corrected for any size changes
   * of that block. Will also fix the joints of blocks
   * that have ids greater than the given id.
   * @param id of block that needs its joints altered
   */
  public void alterJoints(int id)
  {
    if(validateBlockIndex(id))
    {
      for(int i = id; i < getNumBlocks(); i++)
      {
        blockDNAs[i].alterJointA(this);//Maybe not needed??
        blockDNAs[i].alterJointB(this);
      }
    }
  }

  public void alterJoints(int id, DNA dna)
  {
    if(validateBlockIndex(id))
    {
      for(int i = id; i < getNumBlocks(); i++)
      {
        blockDNAs[i].alterJointA(dna);//Maybe not needed??
        blockDNAs[i].alterJointB(dna);
      }
    }
  }

  /**
   * Alter neuron's input type.
   * @param blockID       blockID
   * @param neuronNum     what neuron
   * @param inputNum      what position in the input type array
   * @param type          new type.
   */
  public void alterNeuronInput(int blockID, int neuronNum, int inputNum,
                               EnumNeuronInput type)
  {
    if(validateNeuronIndices(blockID, neuronNum, inputNum))
    {
      blockDNAs[blockID].neuronDNAs.get(neuronNum).inputTypes[inputNum] = type;
    }
  }

  /**
   * Alter neuron's input type with primitive type.
   * @param blockID       blockID
   * @param neuronNum     what neuron
   * @param inputNum      what position in the input type array
   * @param type          new type.
   */
  public void alterNeuronInput(int blockID, int neuronNum, int inputNum,
                               int type)
  {
    alterNeuronInput(blockID, neuronNum, inputNum, EnumNeuronInput.values()[type]);
  }

  /**
   * Alter neuron constantValue.
   * @param blockID       block to alter.
   * @param neuronNum     neuron to alter.
   * @param constNum      rule to alter.
   * @param constant      value to set.
   */
  public void alterNeuronConstant(int blockID, int neuronNum, int constNum,
                                  float constant)
  {
    if(validateNeuronIndices(blockID, neuronNum, constNum))
    {
      blockDNAs[blockID].neuronDNAs.get(neuronNum).constantValues[constNum]
                                                                     = constant;
    }
  }

  /**
   * Alter neuron blockIndex value
   * @param blockID         block to alter.
   * @param neuronNum       neuron to alter.
   * @param blockNum        which rule.
   * @param blockIndexVal   value to set blockIndex to.
   */
  public void alterNeuronBlock(int blockID, int neuronNum, int blockNum,
                                  int blockIndexVal)
  {
    if(validateNeuronIndices(blockID, neuronNum, blockNum))
    {
      blockDNAs[blockID].neuronDNAs.get(neuronNum).blockIndex[blockNum]
          = blockIndexVal;
    }
  }

  /**
   * Add a block to the DNA.  If the blockID is already in use, it will be
   * overwritten by the new block. If the index is invalid nothing will happen.
   * @param b       block to add.
   */
  public void addBlockToDNA(Block b)
  {
    int index = b.getID();
    if(validateBlockIndex(index))
    {
      if(blockDNAs[index] != null)
      {
        length -= blockDNAs[index].blockDNASize;
      }
      blockDNAs[index] = new BlockDNA(b);
      length += blockDNAs[index].blockDNASize;
      calculateNumBlocks();
    }
  }

  /**
   * Create new block with an id and parent id, to be used from a DNA file
   * Scanner
   * @param id        id of this block
   * @param parent    id of parent block
   */
  public void addBlockToDNA(int id, int parent)
  {
    blockDNAs[id] = new BlockDNA(id, parent);
  }

  public void addNeuronToBlock(int blockID)
  {
    blockDNAs[blockID].addNeuronDNA();
  }

  /**
   * Perform single crossover.  On the block level right now.
   * @param other
   * @return
   */
  public DNA[] singleCrossover(DNA other)
  {
    int thisLength = this.numBlocks;
    int otherLength = other.numBlocks;
    int length;
    length = thisLength < otherLength ? thisLength : otherLength;
    int crossPoint = rand.nextInt(length) + 1;
    output[THIS] = new DNA();
    output[OTHER] = new DNA();
    int i;
    for(i = 0; i < crossPoint; ++i)
    {
      output[THIS].blockDNAs[i] = new BlockDNA(this.blockDNAs[i]);//
      output[THIS].blockDNAs[i].alterJointA(output[THIS]);
      output[OTHER].blockDNAs[i] = new BlockDNA(other.blockDNAs[i]);
      output[OTHER].blockDNAs[i].alterJointA(output[OTHER]);
    }
    for(int j = i; j < otherLength; ++j)
    {
      output[THIS].blockDNAs[j] = new BlockDNA(other.blockDNAs[j]);
      output[THIS].blockDNAs[j].alterJointA(output[THIS]);
    }
    for(int j = i; j < thisLength; ++j)
    {
      output[OTHER].blockDNAs[j] = new BlockDNA(this.blockDNAs[j]);
      output[OTHER].blockDNAs[j].alterJointA(output[OTHER]);
    }
    output[THIS].recalculateDNALength();
    output[THIS].calculateNumBlocks();
    output[OTHER].recalculateDNALength();
    output[OTHER].calculateNumBlocks();
    return output;
  }

  /**
   * Swap every other block until one runs out of blocks
   * @param other       DNA to cross with.
   * @return
   */
  public DNA[] uniformCrossover(DNA other)
  {
    //get length
    int thisLength = this.getNumBlocks();
    int otherLength = other.getNumBlocks();
    int length, longerDNA;
    //get shortest length.
    length = thisLength < otherLength ? thisLength : otherLength;
    //setup output DNAs
    output[THIS] = new DNA();
    output[OTHER] = new DNA();
    //initialize outputDNAs
    for(int i = 0; i < CreatureConstants.MAX_BLOCKS; ++i)
    {
      if(this.blockDNAs[i] != null)
      {
        output[THIS].blockDNAs[i] = new BlockDNA(this.blockDNAs[i]);
      }
      if(other.blockDNAs[i] != null)
      {
        output[OTHER].blockDNAs[i] = new BlockDNA(other.blockDNAs[i]);
      }
    }
    //while interation < shortest length swap every other block.
    for(int i = 0; i < length; i += 2)
    {
      output[THIS].blockDNAs[i] = new BlockDNA(other.blockDNAs[i]);
      //output[THIS].blockDNAs[i].alterJointA(output[THIS]);
      output[OTHER].blockDNAs[i] = new BlockDNA(this.blockDNAs[i]);
      //output[OTHER].blockDNAs[i].alterJointA(output[OTHER]);
    }
    output[THIS].alterJoints(1, output[THIS]);
    output[OTHER].alterJoints(1, output[OTHER]);
    output[THIS].recalculateDNALength();
    output[THIS].calculateNumBlocks();
    output[OTHER].recalculateDNALength();
    output[OTHER].calculateNumBlocks();
    return output;
  }

  /**
   * Implement comparable.  DNA compares on fitness.
   * @param other       other DNA object.
   * @return            -1 if this fitness is less than other.
   *                    0 if this fitness is equal to other.
   *                    1 if this fitness is greater than other.
   */
  @Override
  public int compareTo(Object other)
  {
    DNA o = (DNA)other;
    if(this == o)
    {
      return 0;
    }
    else if(this.fitness < o.getFitness())
    {
      return -1;
    }
    else if (this.fitness > o.getFitness())
    {
      return 1;
    }
    else
    {
      return 0;
    }
  }

  /**
   * Override hashcode.
   * @return
   */
  @Override
  public int hashCode()
  {
    int result = 5;
    int hashBase = 31;
    result += hashBase * numBlocks;
    for(BlockDNA bDNA : blockDNAs)
    {
      if (bDNA != null) {
        result += bDNA.getHash();
      }
    }
    return result;
  }
  /**
   * Override .equals
   * @param o
   * @return equals or not
   */
  @Override
  public boolean equals(Object o)
  {
    if(o == null)
    {
      return false;//
    }
    if(getClass() != o.getClass())
    {
      return false;
    }
    DNA other = (DNA) o;
    if(this == other)
    {
      return true;
    }
    if(this.numBlocks != other.numBlocks)
    {
      return false;
    }
    boolean isEqual = true;
    for(int i = 0; i < numBlocks; ++i)
    {
      isEqual = blockDNAs[i].valuesAreSame(other.blockDNAs[i]);
    }
    return isEqual;
  }

  /**
   * Build a string representation of the DNA.  The string representation will
   * one int, followed by a series of floats.  All delineated by spaces, and
   * ended with a newline.
   * @return String representation of dna
   */
  @Override
  public String toString()
  {
    String stringOut = new String(Integer.toString(numBlocks));
    stringOut += '\n';
    for(BlockDNA b : blockDNAs)
    {
      if (b != null)
      {
        stringOut += b.getString();
      }
    }
    return stringOut;
  }

  /**
   * Diff check dna blocks.  For each other block in each dna check if blocks are
   * same or not
   * @param pop       1d array of population
   * @return          return number of differences found.
   */
  public static int numDifferences(ArrayList<DNA> pop)
  {
    int differences = 0;
    int popSize = pop.size();
    DNA current, different;
    BlockDNA currentBlock, otherBlock;
    boolean isNull1, isNull2, foundDifference;
    //for each dna until second to last
    for(int i = 0; i < popSize - 1; ++i)
    {
      current = pop.get(i);
      //for each dna beyond this in the list
      for(int j = i + 1; j < popSize; ++j)
      {
        different = pop.get(j);
        //for each block
        for(int k = 0; k < CreatureConstants.MAX_BLOCKS; ++k)
        {
          foundDifference = false;
          //check if same
          currentBlock = current.getBlockDNA(k);
          otherBlock = different.getBlockDNA(k);
          if((currentBlock == null && otherBlock != null) ||
              currentBlock != null && otherBlock == null)
          {
            foundDifference = true;
          }
          if(!foundDifference && currentBlock != null)
          {
            foundDifference = !currentBlock.valuesAreSame(otherBlock);
          }
          if(foundDifference)
          {
            differences++;
          }
        }
      }
    }
    return differences;
  }

  /**
   * Is block index valid.
   * @param i       index to check.
   * @return        is parameter valid.
   */
  private boolean validateBlockIndex(int i)
  {
    if(0 <= i && i < numBlocks)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Validate the indices for neuron value.
   * @param block     index of block to alter
   * @param neuron    index of neuron to alter
   * @param i         index of rule to alter
   * @return          parameters are valid.
   */
  private boolean validateNeuronIndices(int block, int neuron, int i)
  {
    boolean isValid = false;
    if(validateBlockIndex(block))
    {
      if(0 <= neuron && neuron < blockDNAs[block].neuronDNAs.size())
      {
        if(0 <= i && i < Neuron.TOTAL_INPUTS)
        {
          isValid = true;
        }
      }
    }
    return isValid;
  }

  /**
   * Helper method that creates and returns a new vector from a block's size
   * and shape array.
   * @param dnaNum        block to get vector from.
   * @param type          which vector we want.
   * @return              new vector3f
   */
  private Vector3f newVector(int dnaNum, BlockVector type)
  {
    return new Vector3f(blockDNAs[dnaNum].sizeAndShape[type.ordinal()]);
  }

  /**
   * Count how many blocks are in the DNA
   */
  private void calculateNumBlocks()
  {
    numBlocks = 0;
    for(BlockDNA bDNA : blockDNAs)
    {
      if(bDNA != null)
      {
        numBlocks ++;
      }
    }
  }

  /**
   * Recalculate number of elements in DNA.
   */
  private void recalculateDNALength()
  {
    length = 0;
    for(BlockDNA bDNA : blockDNAs)
    {
      if(bDNA != null)
      {
        length += bDNA.blockDNASize;
      }
    }
  }

  /**
   * Change a DNA so that none of the blocks can exist under the floor
   */
  public void bumpUp()
  {
    float lowestPoint =0;
    float temp;
    for (BlockDNA b : blockDNAs)
    {
      if (b!=null)
      {
        //find the lowest y value in the blockDNAs
        temp = b.getLowestPoint();
        if (temp < lowestPoint)
        {
          lowestPoint = temp;
        }
      }
    }

    for (BlockDNA b : blockDNAs)
    {
      if (b!=null)
      {
        //subtract the lowest point all the block centers
        b.subtractFromCenterY(lowestPoint);
      }
    }
  }


  //============================Nested BlockDNA================================
  /**
   * The DNA for each individual block.  To be stored in an array in the main
   * object.
   * sizeAndShape array:
   *  0: BlockVectors.CENTER: Center of the block in world coordinates, used for
   *  root block, not used for child blocks (is there for reference).
   *  1: BlockVectors.SIZE: Dimensions of a block used for construction (1/2 the
   *  size of the final block in each direction).
   *  2: BlockVectors.JOINT_A: Joint pivot position in coordinates relative to the
   *  parent block's local coordinate system(if parentBlock.startCenter were at
   *  0,0,0).
   *  3: BlockVectors.JOINT_B: Joint pivot position in coordinates relative to this
   *  block's coordinate system (if thisBlock.startCenter were at 0,0,0).
   *  4 & 5: BlockVectors.AXIS_A & BlockVectors.AXIS_B: Axis of rotation of the
   *  block's hingeJoint.
   */
  private class BlockDNA
  {
    private final int NUM_VECTORS = 6;
    private int blockID, parentID;
    private Vector3f[] sizeAndShape;
    private float[] angles;
    private ArrayList<NeuronDNA> neuronDNAs;
    private int blockDNASize = CreatureConstants.BLOCK_DNA_BASE_SIZE;

    /**
     * Default constructor creates a blank block.
     */
    public BlockDNA()
    {
      blockID = -1; //will be -1 until initialized
      parentID = -1; //will be -1 until initialized
      sizeAndShape = new Vector3f[NUM_VECTORS];
      for(int i = 0; i < NUM_VECTORS; ++i)
      {
        sizeAndShape[i] = new Vector3f(0, 0, 0);
      }
      neuronDNAs = new ArrayList<>();
      angles = new float[3];
    }

    /**
     * Constructor for building with a creature
     * @param b     Block from the template creature.
     */
    public BlockDNA(Block b)
    {
      this();
      blockID = b.getID();
      parentID = b.getIdOfParent();
      ArrayList<Neuron> neuronTable = b.getNeuronTable();
      for(Neuron n : neuronTable)
      {
        neuronDNAs.add(new NeuronDNA(n));
        blockDNASize += Neuron.TOTAL_INPUTS;
      }
      length += blockDNASize;
    }

    /**
     * Construct BlockDNA from other blockDNA.
     * @param other       Blueprint to build from.
     */
    public BlockDNA(BlockDNA other)
    {
      this();
      this.blockID = other.blockID;
      this.parentID = other.parentID;
      for(int i = 0; i < NUM_VECTORS; ++i)
      {
        if(other.sizeAndShape[i] != null)
        {
          this.sizeAndShape[i] = new Vector3f(other.sizeAndShape[i]);
        }
        else
        {
          this.sizeAndShape[i] = null;
        }
      }
      this.angles = Arrays.copyOf(other.angles, other.angles.length);
      for(int i = 0; i < other.neuronDNAs.size(); ++i)
      {
        this.neuronDNAs.add(new NeuronDNA(other.neuronDNAs.get(i)));
      }
    }

    /**
     * Instantiate blockDNA with ID and parent ID, to be called from DNAio
     * @param id        blockID number.
     * @param parent    parent ID number.
     */
    public BlockDNA(int id, int parent)
    {
      this();
      blockID = id;
      parentID = parent;
    }

    public float getLowestPoint()
    {
      return sizeAndShape[0].y - sizeAndShape[1].y;
    }

    /**
     * Change the center y value of a block by the passed in amount
     * @param lowestPoint amount to change block.y center by
     */
    public void subtractFromCenterY(float lowestPoint)
    {
      sizeAndShape[0].y -= lowestPoint;
    }

    /**
     * Crossover method to adjust joint A to correct placement.  This will alter
     * the jointA position (placement in relation to the parent block.
     */
    public void alterJointA(DNA dna)
    {
      int parentIndex = parentID;
      if(validateBlockIndex(parentIndex))
      {
        Vector3f thisJointA = sizeAndShape[BlockVector.JOINT_A.ordinal()];
        Vector3f parentSize = dna.blockDNAs[parentIndex].sizeAndShape[BlockVector.SIZE.ordinal()];
        tempVec3 = new Vector3f(getFloatSign(thisJointA.x),
                                getFloatSign(thisJointA.y),
                                getFloatSign(thisJointA.z));
        sizeAndShape[BlockVector.JOINT_A.ordinal()] = parentSize.mult(tempVec3);
      }
    }

    /**
     * Adjusts joint B of the block to align after size has been mutated.
     * @param dna containing the block whose B joint is being altered
     */
    public void alterJointB(DNA dna)
    {
      if(validateBlockIndex(blockID))
      {
        Vector3f thisJointB = sizeAndShape[BlockVector.JOINT_B.ordinal()];
        Vector3f ourSize = dna.blockDNAs[blockID].sizeAndShape[BlockVector.SIZE.ordinal()];
        tempVec3 = new Vector3f(getFloatSign(thisJointB.x),
                getFloatSign(thisJointB.y),
                getFloatSign(thisJointB.z));
        sizeAndShape[BlockVector.JOINT_B.ordinal()] = ourSize.mult(tempVec3);
      }
    }
    /**
     * Get a copy of the creatures angle array.
     * @param angleArr        float array representing rotation quaternion.
     */
    public void setAngles(float[] angleArr)
    {
      angles = Arrays.copyOf(angleArr, angleArr.length);
    }

    /**
     * Add neurons to the creature.  Instantiate neuron, set values, attach.
     * @param b       Block to add neurons to.
     */
    public void addNeurons(Block b)
    {
      Neuron n;
      for(NeuronDNA nDNA : neuronDNAs)
      {
        n = new Neuron(nDNA.inputTypes[0], nDNA.inputTypes[1],
                      nDNA.inputTypes[2], nDNA.inputTypes[3],
                      nDNA.inputTypes[4]);
        for(int i = 0; i < nDNA.NUM_RULES; ++i)
        {
          n.setInputValue(i, nDNA.constantValues[i]);
          n.setBlockIdx(i, nDNA.blockIndex[i]);
        }
        b.addNeuron(n);
      }
    }

    /**
     * Add a neuron DNA to this block.
     */
    public void addNeuronDNA()
    {
      neuronDNAs.add(new NeuronDNA());
    }


    /**
     * Get string representation of the block.
     * @return string representation of the block
     */
    public String getString()
    {
      String bString = new String();
      bString += blockID;
      bString += '\n';
      bString += parentID;
      bString += '\n';
      for (float f : angles)
      {
        bString += f;
        bString += ' ';
      }
      bString += '\n';
      for (Vector3f v : sizeAndShape)
      {
        if (v != null)
        {
          bString += v.x;
          bString += ' ';
          bString += v.y;
          bString += ' ';
          bString += v.z;
          bString += ' ';
        }
        else
        {
          bString += "null";
        }
        bString += '\n';
      }
      if(neuronDNAs != null)
      {
        bString += neuronDNAs.size();
        bString += '\n';
        for(NeuronDNA nDNA : neuronDNAs)
        {
          bString += nDNA.getString();
        }
      }
      else
      {
        bString += 0;
      }
      //bString += '\n';
      return bString;
    }

    /**
     * Return a hashy number.
     * @return      int based on members.
     */
    public int getHash()
    {
      int result;
      int hash = 31;
      result = hash * blockID;
      result += hash * parentID;
      for(Vector3f v : sizeAndShape)
      {
        result += hash * v.x;
        result += hash * v.y;
        result += hash * v.z;
      }
      for(NeuronDNA nDNA : neuronDNAs)
      {
        for(int i = 0; i < Neuron.TOTAL_INPUTS; ++i)
        {
          result += hash * nDNA.blockIndex[i];
          result += hash * nDNA.constantValues[i];
          result += hash * nDNA.inputTypes[i].ordinal();
        }
      }
      return result;
    }

    /**
     * Check values to make sure they are the same.
     * @param other
     * @return
     */
    public boolean valuesAreSame(BlockDNA other)
    {
      if(blockID != other.blockID || parentID != other.parentID)
      {
        return false;
      }
      for(int i = 0; i < NUM_VECTORS; ++i)
      {
        if(!this.sizeAndShape[i].equals(other.sizeAndShape[i]))
        {
          return false;
        }
      }
      if(this.neuronDNAs.size() != other.neuronDNAs.size())
      {
        return false;
      }
      else
      {
        for(int i = 0; i < this.neuronDNAs.size(); ++i)
        {
          if(!neuronDNAs.get(i).valuesAreSame(other.neuronDNAs.get(i)))
          {
            return false;
          }
        }
      }
      return true;
    }

    /**
     * Returns -1.0f, 1.0f, or 0.0f depending on the sign of the float.
     * Used to flip the sign of elements of the parent vector.
     * @param f       float to get sign from
     * @return        sign.
     */
    private float getFloatSign(float f)
    {
      if(f != 0.0f)
      {
        return f/Math.abs(f);
      }
      else
      {
        return 0.0f;
      }
    }

    //==============Nested NeuronDNA===========================================
    /**
     * Nested class containing Neuron DNA, one per neuron per block.
     */
    private class NeuronDNA
    {
      private final int NUM_RULES = Neuron.TOTAL_INPUTS;
      EnumNeuronInput[] inputTypes = new EnumNeuronInput[NUM_RULES];
      float[] constantValues = new float[NUM_RULES];
      int[] blockIndex = new int[NUM_RULES];

      /**
       * Default constructor does nothing.
       */
      public NeuronDNA()
      {
        ;
      }

      /**
       * Constructor that accepts a neuron then populates the dna values.
       * @param n  Neuron in question.
       */
      public NeuronDNA(Neuron n)
      {
        for(int i = 0; i < NUM_RULES; ++i)
        {
          inputTypes[i] = n.getInputType(i);
          constantValues[i] = n.getInputValue(i);
          blockIndex[i] = n.getBlockIdx(i);
        }
      }

      /**
       * Construct NeuronDNA from other NeuronDNA
       * @param other       the blueprint to build from.
       */
      public NeuronDNA(NeuronDNA other)
      {
        for(int i = 0; i < NUM_RULES; ++i)
        {
          inputTypes[i] = other.inputTypes[i];
          constantValues[i] = other.constantValues[i];
          blockIndex[i] = other.blockIndex[i];
        }
      }

      /**
       * String representation of neuron dna.
       * @return        string with neuron rules.
       */
      public String getString()
      {
        String nString = new String();
        for(int i = 0; i < NUM_RULES; ++i)
        {
          if(inputTypes[i] != null)
          {
            nString += inputTypes[i].ordinal();
          }
          else
          {
            nString += "null";
          }
          nString += ' ';
          nString += constantValues[i];
          nString += ' ';
          nString += blockIndex[i];
          nString += '\n';
        }
        return nString;
      }

      /**
       * Compare all values to see if it is equal.
       * @return
       */
      public boolean valuesAreSame(NeuronDNA other)
      {
        for(int i = 0; i < NUM_RULES; ++i)
        {
          if(this.inputTypes[i] != other.inputTypes[i] ||
              this.constantValues[i] != other.constantValues[i] ||
              this.blockIndex[i] != other.blockIndex[i])
          {
            return false;
          }
        }
        return true;
      }
    }
    //===========End NeuronDNA===============================================
  }
  //=================End BlockDNA============================================
}
