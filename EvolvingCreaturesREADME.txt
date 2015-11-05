Evolving Creatures
Project 2
CS351
11/4/15

Group: Spotted Eagle Rays
Sean Naegle
Justin Thomas
Zach Falgout
Julian Weisburd
https://github.com/scnaegle/EvolvingCreatures

Welcome to EvolvingCreatures!

The goal of this program is to find the highest jumping creature in an infinte search space through a combination of
hillclimbing search and genetic alogirthms.Each creature is a series of block. When a block is added to a creature it is added to a parent block. The point at which each block is connected to its parent is called a joint. 
Blocks can rotate about these joints. The jumping functionality comes from a series of neurons which each block has.
When a neuron is fired, blocks try to rotate about their joint axes with a force dependant on their parent block's
volume. Creatures live in a physics simulator which applies gravity and collision tests to the creature. So, in
order to jump, the overall force that is created by the creatures rotating blocks must be upwards and greater than
the force of gravity.

When EvolvingCreatures is started, a population of random creatures are created. Random creatures have a random
number of blocks, random block sizes, random joint placements, and random neurons. All blocks that are created are
axis aligned and all neurons have the same construction: they try to rotate a block with the maximum impulse allowed
around an axis at a certain time. While the original code based allowed for neurons to have much more complexity, we
decided to keep the neurons as simple as possible. 

Hill Climbing then will pick a random block of a creature to mutate, and will pick either to mutate the size of the block, or the neurons connected to that block. The neuron mutations Hill Climbing can choose from is,
mutating the neuron type, mutating the neuron constant, swapping 2 neuron types, or swapping 2 neuron constants. If fitness improves then Hill Climbing with some probability choose to repeat the same mutation sequence.
If fitness doesn't improve, or the event of repeating the mutation sequence isn't chosen, then it will go back to picking random mutations. Hill Climbing will set a flag when mutation needed threshold is reached, so
the Genetic Algorithm can do mutations on the population. The mutation needed threshold is reached when the overall average population fitness is worse than the last generation's, or when the absolute value of the deviation from the
current generation's and last generation's is less than 0.2.  Once Hill Climbing is finished going through the entire population of DNAs, it will return the mutated population back to the MainSim.

If the Genetic Algorithm is called into action to act on the population when Hill Climbing stalls out, then it will
attempt to produce creatures with better fitness by performing cross overs on the DNA of the creatures in the
population. EvolvingCreatures implements two differnt crossover heuristics and two different selection heuristics.
One of the crossover heuristics is single crossover. In single crossover, a single crossover point on both parents'
organism DNA strings is selected. All data beyond that point in either organism DNA is swapped between the two
parent organisms. The other crossover heuristic is uniform crossover. In uniform crossover, a fixed mixing
ration between two parents dictates how many crossover events take place. For example, if the mixing ration is 0.5
then the offspring has approximately half of the genes from the first parent and the other half of the genes from
the second parent. Crossover points are randomly selected. One of the selection heuristics used is tournament
selection. In tournament selection, creatures are selected at random from the population and put into a
"tournament." The winner (highest fitness) of each tournament is selected for crossover. The other selection
heuristic is truncation (referred to as culling in the code) selection. In truncation selection, only the fitest x% of a
population is retained from generation to generation. The population number is then restored with new creatures. As a
default, EvolvingCreature runs with single crossover and truncation selection heuristics. The user can change which 
heuristics are used by using the command line.

OurCreature
OurCreature is a wrapper class for the Creature class.  Where necessary, it intercepts and stores copies of the information
needed to create the DNA.  OurCreature contains constructors for making an empty creature, making a creature from a 
DNA object, and two prefabs; FlappyBird and a four legged variant of flappy bird.

RandCreature
RandCreature is the class which is responsible for generating a creature of random number of blocks, random block sizes,
and random block locations. Every block in RandCreature is connected edge-to-edge

LegCreature
Leg creature is the class which is responsible for generating a creature which has symmetric legs. Random blocks are
generated and added symmetrically to the root or other previously added blocks. This creates a creature with legs
sprouting from the root.

DNA
The DNA class is a numerical parallel to the OurCreature class, and by extension the provided Creature class.  Its nested
classes, BlockDNA and NeuronDNA, similarly parallel Block and Neuron.  These classes provide numerical and vector
representations of a creature, accessors and mutators for the creature's properties, and instructions and methods for
instantiating a creature from the DNA. In addition, DNA implements comparable, and is sorted on the fitness value.  It's
toString method creates a linear representation of the values used for construction for file i/o.

DNAio
DNAio contains static methods for reading and writing DNA .txt files.  It writes DNA's toString to a .txt file and parses
and constructs DNA from a DNA input file.

GUI
Working the GUI is relatively straightforward and intuitive. The main screen shows physics simulation. It is here
that a creature is loaded into the world and tries to jump. 

At the bottom of the main screen is a slider which allows the user to select the speed of the simulation. It should
be noted that changing the speed of the simulation will slightly change the fitness of the creature. If a user wants
to view the most "real" fitness of a creature, the simulation should be viewed on the lowest speed where the slider
is set all the way to the left. 

In the top left corner, a pull down menu allows a user to select an individual creature from a population. If an
individual creature is selected, the hillclimbing/genetic algorithm stops and the selected creature is loaded into
the simulation for user viewing. In order to return to the hillclimbing/genetic, the user should select the first
option on the menu: Run GA

Below the pull down menu are two self explanitory buttons: "Prev Creature" and "Next Creature." Clicking on "Prev
Creature" will allow the user to view the previous creature in the population and clicking "Next Creature" will
allow the user to view the next creature in the population.

Under these two buttons begins where statistics about the simulation are output.
GA Settings:
Max Population: How many creatures are allowed in the population
Max Blocks: The maximum number of Blocks a creature is allowed to have

Current Info:
Crossover Count: How many crossovers ahve occured in this population
Total Generations: How many generations this population has gone through
Creature id: the number of the creature in the population which is currently in the simulator.
Fitness: The current fitness of the creature which is being simulated.

Current Stats:
Total Fitness: The sum of all fitnesses of all creatures in the last completed generation
Average Fitness: The average fitness of the creatures in the last completed generation
Change from last Gen: The total of all fitnesses in the last generation subtracted from the total of all fitness in the gernation before that
Total Change from start:

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
We have included creature.txt in our zip file. To load this creature into the simulation simply add
--input creature.txt
to the command line arguments. 

--leg-creature
Have a population of only creatures with random legs

--random-creature
Have a population of only completely random creatures

--debug
Debug mode

Bugs:
Make sure that the creatures are valid (e.g. no blocks made within each other, no creature made in the floor) isn't perfect.
Blocks can somtimes have a large amount of space between them.

Contributions:
Sean: GUI, simulation master, and all around BOSS
Justin: DNA Structure, DNA I/O, Crossover, OurCreature(wrapper for Creature) structure.
Zach: Hill Climbing on DNA population
Julian: Random/Targeted creature generation

Joel: Original API for Evolving Creatures found at https://github.com/castellanos70/CS351_VirtualCreature_API
