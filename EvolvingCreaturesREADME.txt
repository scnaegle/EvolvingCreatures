Evolving Creatures
Project 2
CS351
11/4/15

Group: Spotted Eagle Rays
Sean Naegle
Justin Thomas
Zach Falgout
Julian Weisburd


Welcome to EvolvingCreatures!
The goal of this program is to find the highest jumping creature in an infinte search space through a combination of hillclimbing search and genetic alogirthms.Each creature is a series of block. When a block is added to a creature it is added to a parent block. The point at which each block is connected to its parent is called a joint. Blocks can rotate about these joints. The jumping functionality comes from a series of neurons which each block has. When a neuron is fired, blocks try to rotate about their joint axes with a force dependant on their parent block's volume. Creatures live in a physics simulator which applies gravity and collision tests to the creature. So, in order to jump, the overall force that is created by the creatures rotating blocks must be upwards and greater than the force of gravity.

When EvolvingCreatures is started, a population of random creatures are created. Random creatures have a random number of blocks, random block sizes, random joint placements, and random neurons. All blocks that are created are axis aligned and all neurons have the same construction: they try to rotate a block with the maximum impulse allowed around an axis at a certain time. While the original code based allowed for neurons to have much more complexity, we decided to keep the neurons as simple as possible. 

Hill Climbing then will pick a random block of a creature to mutate, and will pick either to mutate the size of the block, or the neurons connected to that block. The neuron mutations Hill Climbing can choose from is,
mutating the neuron type, mutating the neuron constant, swapping 2 neuron types, or swapping 2 neuron constants. If fitness improves then Hill Climbing with some probability choose to repeat the same mutation sequence.
If fitness doesn't improve, or the event of repeating the mutation sequence isn't chosen, then it will go back to picking random mutations. Hill Climbing will set a flag when mutation needed threshold is reached, so
GA can do mutations on the population.  The mutation needed threshold is reached when the overall average population fitness is worse than the last generation's, or when the absolute value of the deviation from the
current generation's and last generation's is less than 0.2.  Once Hill Climbing is finished going through the entire population of DNAs, it will return the mutated population back to the MainSim.

GUI
Working the GUI is relatively straightforward and intuitive. The main screen shows physics simulation. It is here that a creature is loaded into the world and tries to jump. 

At the bottom of the main screen is a slider which allows the user to select the speed of the simulation. It should be noted that changing the speed of the simulation will slightly change the fitness of the creature. If a user wants to view the most "real" fitness of a creature, the simulation should be viewed on the lowest speed where the slider is set all the way to the left. 

In the top left corner, a pull down menu allows a user to select an individual creature from a population. If an individual creature is selected, the hillclimbing/genetic algorithm stops and the selected creature is loaded into the simulation for user viewing. In order to return to the hillclimbing/genetic, the user should select the first option on the menu: Run GA

Below the pull down menu are two self explanitory buttons: "Prev Creature" and "Next Creature." Clicking on "Prev Creature" will allow the user to view the previous creature in the population and clicking "Next Creature" will allow the user to view the next creature in the population.

Under these two buttons is where information related to the simulation is output. 
Crossover Count: How many successful crossovers have occured in this population.
Total Generations: How many generations this population have gone through.
Creature id: The number of the creature in the population which is currently in the physics simulator.
Fitness: The current fitness of the creature which is being simulated. Updated in real time!


Console Commands

-h, --help
Shows the help text

--hill-climb-only
If this flag is set, the GA will only hillclimb.

--tournament-selection
If this flag is set, the ga will use tournament selection, otherwise it will cull the least fit and replace with random creatures.

--uniform-crossover
If this flag is set, the GA will swap every other block on the creature, otherwise it will use single crossover.

--headless
If this flag is present then EvolvingCreatures will run the Genetic Algorithm in headless mode with no GUI

--population-count int
Starting number of genomes in the population

--max-num-blocks int
Maximum number of blocks for a creature

--speed int
Set the speed of the simulation

--output file
File that you would like to output to

--input file
Input file to start the Genetic Algorithm
This is how a user should load in any creature he or she saves.
We have included best_creature.txt in our zip file. To load our best creature into the simulation simply add
--input best_creature.txt
to the command line arguments. 

--debug
Debug mode


Contributions:
Sean: 
Justin: DNA Structure, DNA I/O, Crossover, OurCreature(wrapper for Creature) structure.
Zach: Hill Climbing on DNA population
Julian: Random/Targeted creature generation

Joel: Original API for Evolving Creatures found at https://github.com/castellanos70/CS351_VirtualCreature_API
