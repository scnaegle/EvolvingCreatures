package vcreature.creatureUtil;


import com.jme3.math.Vector3f;
import vcreature.mainSimulation.MainSim;
import vcreature.phenotype.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Justin Thomas 10/14/2015
 * A class containing the DNA description of the block.  Will have a tostring
 * method for saving the creature, getters and setters to manipulate the block.
 * Will have a deep copy method/constructor.
 */

public class DNA
{
  private final int THIS = 0;
  private final int OTHER = 1;
  private Random rand;
  private int numBlocks;
  private int length;
  private BlockDNA[] blockDNAs;
  private DNA[] output;
  private Vector3f tempVec3;

  /**
   * Default constructor initializes all values to zero or null.
   */
  public DNA()
  {
    blockDNAs = new BlockDNA[CreatureConstants.MAX_BLOCKS];
    output = new DNA[2];
    rand = new Random();
    tempVec3 = new Vector3f();
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
                      newVector(i, BlockVector.AXIS_A),
                      newVector(i, BlockVector.AXIS_B));
        }
        else
        {
          c.addBlock(bDNA.angles, newVector(i, BlockVector.SIZE),
                                            c.getBlockByID(bDNA.parentID),
                                            newVector(i, BlockVector.JOINT_A),
                                            newVector(i, BlockVector.JOINT_B),
                                            newVector(i, BlockVector.AXIS_A),
                                            newVector(i, BlockVector.AXIS_B));
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
      blockDNAs[blockID].neuronDNAs.get(neuronNum).constantValues[blockNum]
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
    int crossPoint = rand.nextInt(length - 1) + 1;
    output[THIS] = new DNA();
    output[OTHER] = new DNA();
    int i;
    System.out.println(crossPoint);
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
      System.out.println(output[THIS].blockDNAs[j].sizeAndShape[2]);
      output[THIS].blockDNAs[j].alterJointA(output[THIS]);
      System.out.println(output[THIS].blockDNAs[j].sizeAndShape[2]);


    }
    for(int j = i; j < thisLength; ++j)
    {
      output[OTHER].blockDNAs[j] = new BlockDNA(this.blockDNAs[j]);
      output[OTHER].blockDNAs[j].alterJointA(output[OTHER]);
    }
    output[0].recalculateDNALength();
    output[0].calculateNumBlocks();
    output[1].recalculateDNALength();
    output[1].calculateNumBlocks();
    return output;
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
      else//TODO For testing: Remove when doing fileIO
      {
        stringOut += "null\n";
      }
    }
    return stringOut;
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
    private int blockDNASize = 7;

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
     * Crossover method to adjust joint A to correct placement.
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
      bString += '\n';
      return bString;
    }

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
    }
    //===========End NeuronDNA===============================================
  }
  //=================End BlockDNA============================================
}
