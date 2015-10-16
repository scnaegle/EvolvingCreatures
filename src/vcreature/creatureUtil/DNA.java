package vcreature.creatureUtil;


import com.jme3.math.Vector3f;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;
import vcreature.phenotype.EnumNeuronInput;

/**
 * @author Justin Thomas 10/14/2015
 * A class containing the DNA description of the block.  Will have a tostring
 * method for saving the creature, getters and setters to manipulate the block.
 * Will have a deep copy method/constructor.
 * Creatures should be able to use this as a constructor.
 */

//TODO Enum of what each vector 3 stands for, ordered by index of vector.
public class DNA
{
  /*
   * BlockVector is organized for BlockDNA's size and shape array.
   * BlockVector.enum.ordinal() corresponds to the index of the array where
   * you will find that vector.
   */
  public enum BlockVector{CENTER, SIZE, JOINT_A, JOINT_B, AXIS_A, AXIS_B}

  public static final int MAX_BLOCKS = 10;
  private int numBlocks;
  private int length; //TODO calculate in constructor.
  private BlockDNA[] blockDNAs;

  /**
   * Default constructor initializes all values to zero.
   */
  public DNA()
  {
    blockDNAs = new BlockDNA[MAX_BLOCKS];
  }

  /**
   * DNA Built from creature
   * @param c       Creature to build from.
   */
  public DNA(Creature c)
  {
    blockDNAs = new BlockDNA[MAX_BLOCKS];
    numBlocks = c.getNumberOfBodyBlocks();
    for(int i = 0; i < numBlocks; ++i)
    {
      blockDNAs[i] = new BlockDNA(c.getBlockByID(i));
    }
  }

  public int getNumBlocks()
  {
    return numBlocks;
  }

  //TODO getters and setters.

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
        stringOut += b.blockID;
        stringOut += ' ';
        stringOut += b.parentID;
        stringOut += ' ';
        for (Vector3f v : b.sizeAndShape)
        {
          if (v != null)
          {
            stringOut += v.x;
            stringOut += ' ';
            stringOut += v.y;
            stringOut += ' ';
            stringOut += v.z;
            stringOut += ' ';
          }
          else//TODO For testing: Remove when doing fileIO
          {
            stringOut += "null ";
          }
        }

        for (float f : b.neuronRules)
        {
          stringOut += f;
          stringOut += ' ';
        }
      }
      else//TODO For testing: Remove when doing fileIO
      {
        stringOut += "null ";
      }
      stringOut += '\n';
    }
    return stringOut;
  }

  /**
   * The DNA for each individual block.  To be stored in an array in the main
   * object.
   */
  private class BlockDNA
  {
    private final int NUM_VECTORS = 6;
    private final int NUM_RULES = 5;
    private int blockID, parentID;
    private Vector3f[] sizeAndShape;
    private float[] neuronRules;

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
      neuronRules = new float[NUM_RULES];
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
      b.populateVectorDNA(sizeAndShape);
      neuronRules = new float[NUM_RULES];
    }
  }
}
