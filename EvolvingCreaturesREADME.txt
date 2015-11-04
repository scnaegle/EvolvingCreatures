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


GUI


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

--debug
Debug mode


Contributions:
Sean: 
Justin: DNA Structure, DNA I/O, Crossover, OurCreature(wrapper for Creature) structure.
Zach: 
Julian: Random/Targeted creature generation

Joel: Original API for Evolving Creatures found at https://github.com/castellanos70/CS351_VirtualCreature_API
