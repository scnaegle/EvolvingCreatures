package vcreature.creatureUtil;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import vcreature.phenotype.Block;
import vcreature.phenotype.EnumNeuronInput;
import vcreature.phenotype.Neuron;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class responsible for making a valid, random creature with symmetrical legs
 * Intended use is to make LegCreature in the physics space, save it into DNA immediately
 * and remove LegCreature from the world (removeAll()).
 *
 * This class is used by MainSim when a population is initialized by a number of random creatures
 */
public class LegCreature extends RandCreature
{
  public ArrayList<IdSurfaceEdge> availableEdges;
  public ArrayList<Integer> numberOfBlocks;


  public LegCreature(PhysicsSpace physicsSpace, Node rootNode)
  {
    this.physicsSpace = physicsSpace;
    this.jMonkeyRootNode = rootNode;

    availableEdges = new ArrayList<>();
    numberOfBlocks = new ArrayList<>();
    fillBlockPossibilities();

    blockProperties = new ArrayList<>();
    blockAngles = new ArrayList<>();

    //find a random number of blocks to construct the creature with
    int blockCount = numberOfBlocks.get(rand.nextInt(numberOfBlocks.size()));

    makeRandomRoot();

    //while you can still add blocks and you haven't tried to add a block att all the edges
    while (body.size() < blockCount && !availableEdges.isEmpty())
    {
      //select a random parent, surface, and edge to add to from the available edges list
      currentISE = availableEdges.get(rand.nextInt(availableEdges.size()));
      addMirroredBlocks(currentISE); //add blocks
    }
  }

  /**
   * Find all the odd numbers from 3 up to the max number of blocks
   */
  private void fillBlockPossibilities()
  {
    int tempInt;
    if (CreatureConstants.MAX_BLOCKS%2==0)
    {
      tempInt = CreatureConstants.MAX_BLOCKS - 1;
    }
    else tempInt = CreatureConstants.MAX_BLOCKS;

    for (int i = 3; i <= tempInt; i=i+2)
    {
      numberOfBlocks.add(i);
    }
  }

  /**
   * Makes a root block with random x,y,z values
   * This rootblock will be axis alligned
   * @return the Block which will serve as the creatures root
   */
  @Override
  public Block makeRandomRoot()
  {
    //make the root's center at 0,0,0 in the physics space
    Vector3f rootCenter = new Vector3f(0f,0f,0f);
    Vector3f rootSize = new Vector3f(0f,0f,0f);

    //make a the root a random size
    rootSize.x = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    rootSize.y = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    rootSize.z = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;

    //add the root's properties to the arrays which hold the information for DNA creation
    blockProperties.add(makeBlockVectorArray(rootCenter, rootSize));
    blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));

    addAvailableEdges(0);

    //add the root block to the physics space
    return addRoot(rootCenter, rootSize, axisAligned);
  }

  /**
   * Takes the passed in Id+surface+edge of a parent block and tries to add a child to that location
   * @param edgeAddTo id+surface+edge of parent block you want to add a child to
   */
  public void addMirroredBlocks(IdSurfaceEdge edgeAddTo)
  {
    int parentID = edgeAddTo.id;
    int parentEdge = edgeAddTo.edge;
    int parentSurface= edgeAddTo.surface;
    int childEdge;
    int childSurface;
    int mirrorParentID = 0;

    //if the parent is the root, then both children will be children of the root
    if (parentID != 0)
    {
      mirrorParentID = parentID + 1;
    }

    Block parent = getParentBlock(parentID);
    Block mirrorParent = getParentBlock(mirrorParentID);

    childSurface = correspondingChildSurface(edgeAddTo.surface);
    //find the an edge on the child block to make a joint to
    childEdge = findChildEdge(parentEdge);

    Vector3f parentSize = new Vector3f(parent.getSizeX()/2, parent.getSizeY()/2, parent.getSize()/2);
    Vector3f childSize = new Vector3f(0f,0f,0f);

    childSize.x = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    childSize.y = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    childSize.z = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;

    Vector3f parentJoint = new Vector3f(0f,0f,0f);
    Vector3f childJoint = new Vector3f(0f,0f,0f);

    Vector3f parentMirrorJoint = new Vector3f(0f,0f,0f);
    Vector3f childMirrorJoint = new Vector3f(0f,0f,0f);

    Vector3f rotationAxis = new Vector3f(0f,0f,0f);

    //findVectorToEdge(parentSize, parentEdge, parentSurface, parentJoint, rotationAxis);
    //findVectorToEdge(childSize, childEdge, childSurface, childJoint, rotationAxis);

    //find a joint for the parent and child so that the blocks can connect
    findVectorToEdge(parentSize, parentEdge, 1, parentJoint, rotationAxis);
    findVectorToEdge(childSize, childEdge, 0, childJoint, rotationAxis);

    //mirror the joints so that you can add a mirrored block to the otherside of the creature
    findMirrorVector(parentEdge, parentJoint, parentMirrorJoint);
    findMirrorVector(childEdge, childJoint, childMirrorJoint);

    Block newBlock = addBlock(axisAligned, childSize, parent, parentJoint, childJoint, rotationAxis);
    Block mirrorNewBlock = addBlock(axisAligned, childSize, mirrorParent, parentMirrorJoint, childMirrorJoint, rotationAxis);

    //if adding blocks doesn't make a collision
    if (!removeIfIntersection())
    {
      //add the new childs available edges to list of possible edges to add a new block to
      addAvailableEdges(newBlock.getID());
      //add neurons to the new blocks
      addMirroredNeurons(newBlock, mirrorNewBlock);
      //copy the Block's information for DNA creation
      blockProperties.add(makeBlockVectorArray(newBlock, childSize, rotationAxis, rotationAxis));
      blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));
      blockProperties.add(makeBlockVectorArray(mirrorNewBlock, childSize, rotationAxis, rotationAxis));
      blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));
    }
  }


  /**
   * Add mirrored neurons to the two blocks passed in
   * if one block gets a neuron with +impulse, then the other block will get the same neuron but with -impulse
   * @param newBlock
   * @param mirror
   */
  private void addMirroredNeurons(Block newBlock, Block mirror)
  {
    int numberNeurons = rand.nextInt(CreatureConstants.MAX_NEURON_PER_BLOCK)+1;
    int sign;
    float seconds;
    float maxImpulse = newBlock.getJointMaxImpulse();
    Neuron n;
    Neuron m;
    for (int i = 0; i <= numberNeurons; ++i)
    {
      //select random time for neuron firing
      seconds = rand.nextInt(CreatureConstants.MAX_NEURON_SECONDS - CreatureConstants.MIN_NEURON_SECONDS) + CreatureConstants.MIN_NEURON_SECONDS;
      seconds += rand.nextFloat();
      sign = rand.nextInt(2);
      if (sign != 0)
      {
        maxImpulse = -maxImpulse;
      }
      //if one neuron's impulse is + then the other should be -
      //both should fire at the same time
      n = makeNewNeuron(maxImpulse, seconds);
      m = makeNewNeuron(-maxImpulse, seconds);
      newBlock.addNeuron(n);
      mirror.addNeuron(m);
    }
  }

  /**
   * Makes a new neuron that will fire at the passed in time with the passed in impulse
   * @param maxImpulse impulse to fire with
   * @param seconds time to fire
   * @return
   */
  private Neuron makeNewNeuron(float maxImpulse, float seconds)
  {
    Neuron n = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT, EnumNeuronInput.CONSTANT, null);
    n.setInputValue(Neuron.C, seconds);
    n.setInputValue(Neuron.D, maxImpulse);
    return n;
  }

  /**
   * Mirrors a vector about the X axis
   * @param edgeToMirror edge of a block which the vector is pointing to
   * @param joint joint you want to mirror
   * @param mirrorJoint mirrored joint you want to create
   */
  public void findMirrorVector(int edgeToMirror,  Vector3f joint, Vector3f mirrorJoint)
  {
      if (edgeToMirror == 0 || edgeToMirror == 2)
      {
        mirrorJoint.x = -joint.x;
        mirrorJoint.y = joint.y;
        mirrorJoint.z = -joint.z;
      }
      else if (edgeToMirror == 1 || edgeToMirror == 3)
      {
        mirrorJoint.x = - joint.x;
        mirrorJoint.y = joint.y;
        mirrorJoint.z = -joint.z;
      }
  }

  /**
   * Get the parent block of the id of the block passed in
   * @param idOfParent
   * @return
   */
  public Block getParentBlock(int idOfParent)
  {
    if (idOfParent == 0)
    {
      return body.get(0);
    }
    else
    {
      return body.get(idOfParent);
    }
  }

  /**
   * Based in the edge of the parent passed in, chooses an appropriate edge on the child to attach to the parent
   * @param parentEdge
   * @return edge of child to connect to parent
   */
  public int findChildEdge(int parentEdge)
  {
    int childEdge = 0;
    boolean eitherOr = rand.nextBoolean();
    if (parentEdge == 0 || parentEdge == 2)
    {
      if (eitherOr) childEdge = 2;
      else childEdge =0;
    }
    if (parentEdge == 1 || parentEdge == 3)
    {
      if (eitherOr) childEdge = 3;
      else childEdge =1;
    }
    return childEdge;
  }

  /**
   * Checks to see if the newly added blocks cause a collision with the existing blocks on the creature
   * If yes, removes both of the newly added blocks
   * @return true if there was an intersection, false if not
   */
  @Override
  public boolean removeIfIntersection()
  {
    int newBlockID = getNumberOfBodyBlocks()-1;
    CollisionResults collisionResults = new CollisionResults();

    Block newBlock = body.get(newBlockID);
    Block compareBlock;

    for (int i = 0 ; i < newBlockID; ++i)
    {
      compareBlock = body.get(i);
      BoundingBox box = (BoundingBox) compareBlock.getGeometry().getWorldBound();
      newBlock.getGeometry().collideWith(box, collisionResults);

      if ((newBlock.getIdOfParent() != compareBlock.getID()) && collisionResults.size() > 0)
      {
        removeBlock(newBlockID);
        removeBlock(newBlockID-1);
        availableEdges.remove(currentISE);
        return true;
      }

      collisionResults.clear();
    }

    return false;
  }

  /**
   * add to the list of edges available to add blocks to when a new block is added to the creature
   * @param blockID ID of block just added to creature
   */
  @Override
  public void addAvailableEdges(int blockID)
  {

    for (int i = 1; i < 6; ++i)
    {
      for (int j = 0; j < 4; ++j)
      {
        availableEdges.add(new IdSurfaceEdge(blockID, i, j));
      }
    }
  }

}
