package vcreature.mainSimulation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import vcreature.creatureUtil.DNA;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by scnaegl on 11/2/15.
 * Stores the current Population and the entire history of each of the strands of DNA that we started with.
 * After every crossover this gets reset as all the creature are essentially "new" and not a continuation of
 * some mutated DNA.
 */
public class Population {
  // The array of all the strands we have
  private ArrayList<Strand> strands = new ArrayList<>();

  // Cache fields to keep track of some good stats for reporting.
  private float total_recent_fitness = 0;
  private float avg_recent_fitness = 0;
  private float total_best_fitness = 0;
  private float avg_best_fitness = 0;

  /**
   * Create new Population
   */
  public Population() {
    this.strands = new ArrayList<>();
  }

  /**
   * Get all Strands and their history
   * @return ArrayList of Strands
   */
  public ArrayList<Strand> getStrands() {
    return strands;
  }

  /**
   * Get the total fitness of the most recent generation
   * @return float total recent fitness
   */
  public float getTotalRecentFitness() {
    if (total_recent_fitness == 0) updateFitnessCache();
    return total_recent_fitness;
  }

  /**
   * Get the average fitness of the most recent generation
   * @return float average recent fitness
   */
  public float getAverageRecentFitness() {
    if (avg_recent_fitness == 0) updateFitnessCache();
    return avg_recent_fitness;
  }

  /**
   * Get the total fitness of all the BEST creatures regardless of generation.
   * This picks the best creature from each strand's history and adds up their fitness
   * @return float total best fitness
   */
  public float getTotalBestFitness() {
    if (total_best_fitness == 0) updateFitnessCache();
    return total_best_fitness;
  }

  /**
   * Get the average fitness of all the BEST creatures regardless of generation.
   * This picks the best creature from each strand's history and gets the average.
   * @return float average best fitness
   */
  public float getAverageBestFitness() {
    if (avg_best_fitness == 0) updateFitnessCache();
    return avg_best_fitness;
  }

  /**
   * Get the total fitness for a Strand, given the index in the array
   * @param strand_index Index in the array
   * @return float total fitness of strand
   */
  public float getTotalFitness(int strand_index) {
    return getWithRolloverIndex(strand_index).getTotalFitness();
  }

  /**
   * Get the average fitness for a Strand, given the index in the array
   * @param strand_index Index in the array
   * @return float average fitness of strand
   */
  public float getAverageFitness(int strand_index) {
    return getWithRolloverIndex(strand_index).getAverageFitness();
  }

  /**
   * Get the total fitness for a given generation. For this we allow rollover indexes, so if you want to
   * get the fitness for the last generation, then you can pass -1, or -2 for the second to last, etc...
   * @param gen_index Index of generation
   * @return float total generation fitness
   */
  public float getTotalGenerationFitness(int gen_index) {
    float total = 0;
    for(Strand strand : strands) {
      total += strand.getWithRolloverIndex(gen_index).getFitness();
    }
    return total;
  }

  /**
   * Get the average fitness for a given generation. For this we allow rollover indexes, so if you want to
   * get the fitness for the last generation, then you can pass -1, or -2 for the second to last, etc...
   * @param gen_index Index of generation
   * @return float average generation fitness
   */
  public float getAverageGenerationFitness(int gen_index) {
    return getTotalGenerationFitness(gen_index) / size();
  }

  /**
   * Add a DNA to the population. This will create a new Strand starting with this DNA
   * @param dna new DNA
   */
  public void add(DNA dna) {
    Strand gen = new Strand();
    gen.add(dna);
    strands.add(gen);
  }

  /**
   * Add a DNA to a specific strand. This will add a new DNA to the end of the strand's history
   * @param strand_id Strand Index
   * @param dna new DNA
   */
  public void add(int strand_id, DNA dna) {
    get(strand_id).add(dna);
  }

  /**
   * Get the strand with the given index
   * @param strand_id Strand index
   * @return Strand
   */
  public Strand get(int strand_id) {
    return strands.get(strand_id);
  }

  /**
   * Manually trigger updating the fitness caches so that they stay current
   */
  public void updateFitnessCache() {
    total_recent_fitness = 0;
    total_best_fitness = 0;
    for(Strand strand : strands) {
      total_recent_fitness += strand.getLast().getFitness();
      total_best_fitness += strand.getBest().getFitness();
    }
    avg_recent_fitness = total_recent_fitness / strands.size();
    avg_best_fitness = total_best_fitness / strands.size();
  }

  /**
   * Get the size of the population
   * @return Number of Strands in population
   */
  public int size() {
    return strands.size();
  }

  /**
   * Remove a strand at a given index
   * @param index Index of Strand to be removed
   */
  public void remove(int index) {
    strands.remove(index);
  }

  /**
   * Check if the population is Empty
   * @return True if the population is empty
   */
  public boolean isEmpty() {
    return strands.isEmpty();
  }

  /**
   * Get the best DNAs from each strand and return them as an arraylist
   * @return ArrayList of best DNAs
   */
  public ArrayList<DNA> getBestDNAs() {
    return (ArrayList<DNA>)strands.stream().map(s -> s.getBest()).collect(Collectors.toList());
  }

  /**
   * Get the very best DNA out of all the Strands and all their history.
   * @return Best DNA
   */
  public DNA getBest() {
    DNA best = getBestDNAs().get(0);
    for(DNA dna : getBestDNAs()) {
      if (dna.getFitness() > best.getFitness()) {
        best = dna;
      }
    }
    return best;
  }

  /**
   * Get the increase in the total fitness from generation 1 to generation 2. This allows for rollover indexes,
   * so you can pass in -1 to get the last generation.
   * @param gen1 First Generation
   * @param gen2 Second Generation
   * @return float 2nd gen fitness - 1st gen fitness
   */
  public float changeInTotalFitness(int gen1, int gen2) {
    try {
      return getTotalGenerationFitness(gen2) - getTotalGenerationFitness(gen1);
    } catch (IndexOutOfBoundsException e) {
      try {
        return getTotalGenerationFitness(gen2);
      } catch (IndexOutOfBoundsException e2) {
        return 0;
      }
    }
  }

  /**
   * Get the increase in the average fitness from generation 1 to generation 2. This allows for rollover indexes,
   * so you can pass in -1 to get the last generation.
   * @param gen1 First Generation
   * @param gen2 Second Generation
   * @return float 2nd gen avg fitness - 1st gen avg fitness
   */
  public float changeInAverageFitness(int gen1, int gen2) {
    try {
      return getAverageGenerationFitness(gen2) - getAverageGenerationFitness(gen1);
    } catch (IndexOutOfBoundsException e) {
      try {
        return getAverageGenerationFitness(gen2);
      } catch (IndexOutOfBoundsException e2) {
        return 0;
      }
    }
  }

  /**
   * Calculates the rollover index. So if index is -1 then the calculated index will be the last element in
   * the array, -2 will be the second to last, etc...
   * @param index Rollover Index
   * @return Strand at the calculated index
   */
  private Strand getWithRolloverIndex(int index) {
    if (index < 0) {
      return get(size() - index);
    } else {
      return get(index);
    }
  }

  /**
   * The Strand class keeps track of each starting DNA's history through the hill climbing process.
   */
  public class Strand {
    // Array list of all the generations the DNA went through
    private ArrayList<DNA> generations = new ArrayList<>();

    // Cache fields for the total and average fitnesses
    private float total_fitness = 0;
    private float avg_fitness = 0;

    /**
     * Create new Strand with history
     */
    public Strand() {
      this.generations = new ArrayList<>();
    }

    /**
     * Get total fitness of the Strand
     * @return float total fitness
     */
    public float getTotalFitness() {
      return total_fitness;
    }

    /**
     * Get the average fitness of the Strand
     * @return float average fitness
     */
    public float getAverageFitness() {
      return avg_fitness;
    }

    /**
     * Get all the generations for the Strand
     * @return ArrayList of DNA
     */
    public ArrayList<DNA> getGenerations() {
      return generations;
    }

    /**
     * Add a new generation DNA
     * @param dna new DNA
     */
    public void add(DNA dna) {
      generations.add(dna);
    }

    /**
     * Get the DNA at the given index
     * @param dna_id index of DNA
     * @return
     */
    public DNA get(int dna_id) {
      return generations.get(dna_id);
    }

    /**
     * Update the fitenss of a DNA at the given index
     * @param dna_id Index DNA
     * @param fitness fitness of DNA
     */
    public void updateFitness(int dna_id, float fitness) {
      generations.get(dna_id).storeFitness(fitness);
      updateFitnessCache();
    }

    /**
     * Update the last generation's fitness for this Strand
     * @param fitness
     */
    public void updateLastFitness(float fitness) {
      Iterables.getLast(generations).storeFitness(fitness);
      updateFitnessCache();
    }

    /**
     * Do a manual update the fitness cache to keep things up to date
     */
    public void updateFitnessCache() {
      total_fitness = 0;
      for(DNA dna : generations) {
        total_fitness += dna.getFitness();
      }
      avg_fitness = total_fitness / generations.size();
    }

    /**
     * Get the best DNA out of all the generations for this Strand.
     * @return best DNA
     */
    public DNA getBest() {
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
     * Get the last generation DNA
     * @return last DNA
     */
    public DNA getLast() {
      Iterables.getLast(generations).bumpUp(); //This probably shouldn't go here. Talk tomorrow about where to put it
      return Iterables.getLast(generations);
    }

    /**
     * Get the number of generations there are currently
     * @return int Number of generations
     */
    public int size() {
      return generations.size();
    }

    /**
     * Remove a generation at the given index
     * @param index Generation index
     */
    public void remove(int index) {
      generations.remove(index);
    }

    /**
     * Calculates the rollover index. So if index is -1 then the calculated index will be the last element in
     * the array, -2 will be the second to last, etc...
     * @param index Rollover Index
     * @return Strand at the calculated index
     */
    private DNA getWithRolloverIndex(int index) {
      if (index < 0) {
        return get(size() + index);
      } else {
        return get(index);
      }
    }
  }
}
