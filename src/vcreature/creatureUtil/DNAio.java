package vcreature.creatureUtil;

import com.google.common.collect.Iterables;
import com.jme3.math.Vector3f;
import vcreature.mainSimulation.MainSim;
import vcreature.phenotype.Neuron;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Justin Thomas(jthomas105@unm.edu)
 */
public class DNAio
{
  /**
   * Take an array of the population's DNA and write to a file.
   * @param population        Array List of DNA objects.
   */
  public static void writePopulation(ArrayList<ArrayList<DNA>> population)
  {
    StringBuilder outString = new StringBuilder();
    DNA dna;
    for(ArrayList<DNA> generations : population)
    {
      dna = getBestGeneration(generations);
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

  private static DNA getBestGeneration(ArrayList<DNA> generations) {
    DNA best;
    best = Iterables.getLast(generations);
    for(DNA dna : generations) {
      if (dna.getFitness() >= best.getFitness()) {
        best = dna;
      }
    }
    return best;
  }

  /**
   * Create DNAs from file
   * @param f                 File input.
   * @param population        Population arraylist to fill.
   */
  public static void readPopulation(File f, ArrayList<DNA> population)
  {
    try
    {
      parseInput(f, population);
    }
    catch (IOException e)
    {
      System.out.println("Population read error");
    }
  }

  private static boolean parseInput(File f, ArrayList<DNA> population) throws IOException
  {
    int numBlocks, numNeurons;
    int id, parentID, neuronInputType, neuronBlockID;
    float x, y, z, neuronConstant;
    Scanner s = new Scanner(f);
    //while more DNAs
    while(s.hasNext())
    {
      //first line int numblocks
      //make DNA
      if (s.hasNextInt())
      {

        //for numblocks
        numBlocks = s.nextInt();
        DNA dnaIn = new DNA(numBlocks);
        population.add(dnaIn);

        for (int i = 0; i < numBlocks; ++i)
        {
          //make blockDNA
          //set ID (int)
          id = s.nextInt();
          //set parent id (int)
          parentID = s.nextInt();
          dnaIn.addBlockToDNA(id, parentID);
          //Get angle array
          x = s.nextFloat();
          y = s.nextFloat();
          z = s.nextFloat();
          dnaIn.alterAngles(x, y, z, i);
          //set vector array (float 3-tuple) * 6
          for(int j = 0; j < 6; ++j)
          {
            x = s.nextFloat();
            y = s.nextFloat();
            z = s.nextFloat();
            dnaIn.alterVector(x, y, z, i, j);
          }

          //int numNeurons (int)
          numNeurons = s.nextInt();
          //for numNeurons
          for(int j = 0; j < numNeurons; ++j)
          {
            //set neuron rules(5) (float, int)
            for(int k = 0; k < Neuron.TOTAL_INPUTS; ++k)
            {
              dnaIn.addNeuronToBlock(i);
              neuronInputType = s.nextInt();
              neuronConstant = s.nextFloat();
              neuronBlockID = s.nextInt();
              dnaIn.alterNeuronInput(i, j, k, neuronInputType);
              dnaIn.alterNeuronConstant(i, j, k, neuronConstant);
              dnaIn.alterNeuronBlock(i, j, k, neuronBlockID);
            }
          }
          //nextBlock
        }
        //if more, next line is new numblocks.
      }
      else
      {
        s.next();
      }
    }
    return true;
  }
}
