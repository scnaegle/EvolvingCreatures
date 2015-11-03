package vcreature.mainSimulation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import vcreature.creatureUtil.DNA;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by scnaegl on 11/2/15.
 */
public class Population {
  private ArrayList<Strand> strands = new ArrayList<>();
  private float total_recent_fitness = 0;
  private float avg_recent_fitness = 0;
  private float total_best_fitness = 0;
  private float avg_best_fitness = 0;

  public Population() {
    this.strands = new ArrayList<>();
  }

  public ArrayList<Strand> getStrands() {
    return strands;
  }

  public float getTotalRecentFitness() {
    if (total_recent_fitness == 0) updateFitnessCache();
    return total_recent_fitness;
  }

  public float getAverageRecentFitness() {
    if (avg_recent_fitness == 0) updateFitnessCache();
    return avg_recent_fitness;
  }

  public float getTotalBestFitness() {
    if (total_best_fitness == 0) updateFitnessCache();
    return total_best_fitness;
  }

  public float getAverageBestFitness() {
    if (avg_best_fitness == 0) updateFitnessCache();
    return avg_best_fitness;
  }

  public void add(DNA dna) {
    Strand gen = new Strand();
    gen.add(dna);
    strands.add(gen);
  }

  public void add(int strand_id, DNA dna) {
    get(strand_id).add(dna);
  }

  public Strand get(int strand_id) {
    return strands.get(strand_id);
  }

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

  public int size() {
    return strands.size();
  }

  public void remove(int index) {
    strands.remove(index);
  }

  public boolean isEmpty() {
    return strands.isEmpty();
  }

  public ArrayList<DNA> getBestDNAs() {
    return (ArrayList<DNA>)strands.stream().map(s -> s.getBest()).collect(Collectors.toList());
  }


  public class Strand {
    private ArrayList<DNA> generations = new ArrayList<>();
    private float total_fitness = 0;
    private float avg_fitness = 0;

    public Strand() {
      this.generations = new ArrayList<>();
    }

    public float getTotalFitness() {
      return total_fitness;
    }

    public float getAverageFitness() {
      return avg_fitness;
    }

    public ArrayList<DNA> getGenerations() {
      return generations;
    }

    public void add(DNA dna) {
      generations.add(dna);
    }

    public DNA get(int dna_id) {
      return generations.get(dna_id);
    }

    public void updateFitness(int dna_id, float fitness) {
      generations.get(dna_id).storeFitness(fitness);
      updateFitnessCache();
    }

    public void updateLastFitness(float fitness) {
      Iterables.getLast(generations).storeFitness(fitness);
      updateFitnessCache();
    }

    public void updateFitnessCache() {
      total_fitness = 0;
      for(DNA dna : generations) {
        total_fitness += dna.getFitness();
      }
      avg_fitness = total_fitness / generations.size();
    }

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

    public DNA getLast() {
      return Iterables.getLast(generations);
    }

    public int size() {
      return generations.size();
    }

    public void remove(int index) {
      generations.remove(index);
    }
  }
}
