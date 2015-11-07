package vcreature.creatureUtil;

import com.google.common.collect.Iterables;
import com.jme3.math.Vector3f;
import sun.applet.Main;
import vcreature.mainSimulation.MainSim;
import vcreature.mainSimulation.Population;
import vcreature.phenotype.Neuron;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
  public static void writePopulation(Population population)
  {
    StringBuilder outString = new StringBuilder();
    for(DNA dna : population.getBestDNAs())
    {
      outString.append(dna);
    }
    //System.out.println("outString" + outString);
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
   * Take a creature's DNA and write to a file
   * @param bestCreature  creature you want to save
   */
  public static void writeSingleCreature(DNA bestCreature)
  {
    StringBuilder outString = new StringBuilder();
    outString.append(bestCreature);
    try
    {
      FileWriter writer = new FileWriter(MainSim.output_best_creature);
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
  public static void readPopulation(File f, Population population)
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

  /**
   * Helper method that parses the DNA input file.  Very brittle, files must be
   * in the exact format that writePopulation outputs.
   * Goes line by line through the input file instantiating and filling in DNA
   * pieces in the order that they arrive.
   * @param f               file to parse
   * @param population      population DNA array to fill.
   * @return
   * @throws IOException    IOExceptions handled in readPopulation
   */
  private static boolean parseInput(File f, Population population) throws IOException
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
            dnaIn.addNeuronToBlock(i);
            //set neuron rules(5) (float, int)
            for(int k = 0; k < Neuron.TOTAL_INPUTS; ++k)
            {
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
    s.close();
    return true;
  }
}
