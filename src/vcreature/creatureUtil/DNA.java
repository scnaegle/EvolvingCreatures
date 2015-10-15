package vcreature.creatureUtil;

import com.jme3.math.Vector3f;
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
  private int numBlocks;
  private int length; //TODO calculate in constructor.
  private BlockDNA[] blockDNAs;

  /**
   * Default constructor initializes all values to zero.
   */
  public DNA(int numBlocks)
  {
    this.numBlocks = numBlocks;
    blockDNAs = new BlockDNA[this.numBlocks];
    for(int i = 0; i < this.numBlocks; ++i)
    {
      blockDNAs[i] = new BlockDNA(i);
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
   * @return
   */
  @Override
  public String toString()
  {
    String stringOut = new String(Integer.toString(numBlocks));
    stringOut += ' ';
    for(BlockDNA b : blockDNAs)
    {
      stringOut += b.blockID;
      stringOut += ' ';
      stringOut += b.parentID;
      stringOut += ' ';
      for (Vector3f v : b.sizeAndShape)
      {
        stringOut += v.x;
        stringOut += ' ';
        stringOut += v.y;
        stringOut += ' ';
        stringOut += v.z;
        stringOut += ' ';
      }

      for (float f : b.neuronRules)
      {
        stringOut += f;
        stringOut += ' ';
      }
    }
    stringOut += '\n';
    return stringOut;
  }

  /**
   * The DNA for each individual block.  To be stored in an array in the main
   * object.
   */
  private class BlockDNA
  {
    private int blockID, parentID;
    private Vector3f[] sizeAndShape;
    private float[] neuronRules;

    public BlockDNA(int id)
    {
      blockID = id;
      parentID = -1; //will be -1 until initialized
      sizeAndShape = new Vector3f[6];
      for(int i = 0; i < 6; ++i)
      {
        sizeAndShape[i] = new Vector3f(0, 0, 0);
      }
      neuronRules = new float[5];
    }
  }
}
