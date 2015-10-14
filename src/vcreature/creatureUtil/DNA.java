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
  private Vector3f[] sizeAndShape;
  private float[] neuronRules;

  /**
   * Default constructor initializes all values to zero.
   */
  public DNA()
  {
    sizeAndShape = new Vector3f[6];
    for(int i = 0; i < 6; ++i)
    {
      sizeAndShape[i] = new Vector3f(0, 0, 0);
    }
    neuronRules = new float[5];
  }

  //Getters TODO Javadoc
  public int getNumBlocks()
  {
    return numBlocks;
  }

  public Vector3f[] getSizeAndShape()
  {
    return sizeAndShape;
  }

  public Vector3f getSizeAndShapeAt(int i)
  {
    return sizeAndShape[i];
  }

  public float[] getNeuronRules()
  {
    return neuronRules;
  }

  public float getNeuronRuleAt(int i)
  {
    return neuronRules[i];
  }

  public float getNeuronRuleAt(EnumNeuronInput e)
  {
    return neuronRules[e.ordinal()];
  }
  //end getters.

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
    for(Vector3f v : sizeAndShape)
    {
      stringOut += v.x;
      stringOut += ' ';
      stringOut += v.y;
      stringOut += ' ';
      stringOut += v.z;
      stringOut += ' ';
    }

    for(float f : neuronRules)
    {
      stringOut += f;
      stringOut += ' ';
    }

    stringOut += '\n';
    return stringOut;
  }
}
