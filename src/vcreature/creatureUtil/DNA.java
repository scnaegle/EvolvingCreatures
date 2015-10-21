package vcreature.creatureUtil;


import com.jme3.math.Vector3f;
import vcreature.mainSimulation.MainSim;
import vcreature.phenotype.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Justin Thomas 10/14/2015
 * A class containing the DNA description of the block.  Will have a tostring
 * method for saving the creature, getters and setters to manipulate the block.
 * Will have a deep copy method/constructor.
 */

public class DNA
{
  private int numBlocks;
  private int length;
  private BlockDNA[] blockDNAs;

  /**
   * Default constructor initializes all values to zero or null.
   */
  public DNA()
  {
    blockDNAs = new BlockDNA[CreatureConstants.MAX_BLOCKS];
  }

  /**
   * DNA Built from creature.  Pass in a built creature to make dna
   * @param c       Creature to build from.
   */
  public DNA(OurCreature c)
  {
    length = 0;
    blockDNAs = new BlockDNA[CreatureConstants.MAX_BLOCKS];
    numBlocks = c.getNumberOfBodyBlocks();
    for(int i = 0; i < numBlocks; ++i)
    {
      blockDNAs[i] = new BlockDNA(c.getBlockByID(i));
      c.populateVectorDNA(i, blockDNAs[i].sizeAndShape);
      blockDNAs[i].setAngles(c.getBlockAngles(i));
    }
  }

  /**
   * Get number of blocks according to DNA.  May not be up to date.
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
   * Build creature with DNA.  Called from OurCreature constructor.
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
      blockDNAs[index] = new BlockDNA(b);
    }
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

  //============================Nested BlockDNA================================
  /**
   * The DNA for each individual block.  To be stored in an array in the main
   * object.
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
      blockID = b.getID();
      parentID = b.getIdOfParent();
      sizeAndShape = new Vector3f[NUM_VECTORS];
      neuronDNAs = new ArrayList<>();
      ArrayList<Neuron> neuronTable = b.getNeuronTable();
      for(Neuron n : neuronTable)
      {
        neuronDNAs.add(new NeuronDNA(n));
        blockDNASize += Neuron.TOTAL_INPUTS;
      }
      length += blockDNASize;
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
