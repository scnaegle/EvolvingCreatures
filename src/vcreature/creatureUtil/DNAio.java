package vcreature.creatureUtil;

import vcreature.mainSimulation.MainSim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Justin Thomas(jthomas105@unm.edu)
 */
public class DNAio
{
  /**
   * Take an array of the population's DNA and write to a file.
   * @param population        Array List of DNA objects.
   */
  public static void writePopulation(ArrayList<DNA> population)
  {
    StringBuilder outString = new StringBuilder();
    for(DNA dna : population)
    {
      outString.append(dna);
      System.out.println(dna);
    }
    System.out.println("outString" + outString);
    try
    {
      FileWriter writer = new FileWriter(MainSim.output_file);
      writer.write(outString.toString());
      writer.close();
    }
    catch(IOException e)
    {
      System.out.println("Could not write file");
    }
  }

  /**
   * Create DNAs from file
   * @param f                 File input.
   * @param population        Population arraylist to fill.
   */
  public static void readPopulation(File f, ArrayList<DNA> population)
  {
    //while more DNAs
      //first line int numblocks
      //make DNA
      //for numblocks
        //make blockDNA
          //set ID (int)
          //set parent id (int)
          //set vector array (float 3-tuple) * 6
          //int numNeurons (int)
          //for numNeurons
            //set neuron rules(5) (float, int)
      //nextBlock
    //if more, next line is new numblocks.
  }
}