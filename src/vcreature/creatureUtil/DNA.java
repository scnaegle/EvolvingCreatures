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
  /*
   * BlockVector is organized for BlockDNA's size and shape array.
   * BlockVector.enum.ordinal() corresponds to the index of the array where
   * you will find that vector.
   */

  private int numBlocks;
  private int length; //TODO calculate in constructor.
  private BlockDNA[] blockDNAs;

  /**
   * Default constructor initializes all values to zero.
   */
  public DNA()
  {
    blockDNAs = new BlockDNA[CreatureConstants.MAX_BLOCKS];
  }

  /**
   * DNA Built from creature
   * @param c       Creature to build from.
   */
  public DNA(OurCreature c)
  {
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
   * @param iD       int BlockID
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
   * Build creature with DNA
   * @param c       Creature to build.
   */
  public void initializeCreature(Creature c)
  {
    //create root block
    c.addRoot(newVector(0, BlockVector.CENTER), newVector(0, BlockVector.SIZE));
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
   * Build a string representation of the DNA.  The string representation will
   * one int, followed by a series of floats.  All delineated by spaces, and
   * ended with a newline.
   * //TODO StringBuilder for not n00bish
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
   * Helper method that creates and returns a new vector from a block's size
   * and shape array
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
      //b.populateVectorDNA(sizeAndShape);
      neuronDNAs = new ArrayList<>();
      ArrayList<Neuron> neuronTable = b.getNeuronTable();
      for(Neuron n : neuronTable)
      {
        neuronDNAs.add(new NeuronDNA(n));
      }
    }

    /**
     * Get a copy of the creatures angle argument.
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
     * Get string representation of the block
     */
    public String getString()
    {
      String bString = new String();
      bString += blockID;
      bString += '\n';
      bString += parentID;
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
        else//TODO For testing: Remove when doing fileIO
        {
          bString += "null ";
        }
        bString += '\n';
      }
      if(neuronDNAs != null)
      {
        for(NeuronDNA nDNA : neuronDNAs)
        {
          bString += nDNA.getString();
        }
      }
      else
      {
        bString += "null ";
      }
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
       * @param n
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

      public String getString()
      {
        String nString = new String();
        nString += NUM_RULES;
        nString += '\n';
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
