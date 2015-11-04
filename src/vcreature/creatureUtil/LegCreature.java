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

    int blockCount = numberOfBlocks.get(rand.nextInt(numberOfBlocks.size()));

    makeRandomRoot();

    while (body.size() < blockCount && !availableEdges.isEmpty())
    {
      currentISE = availableEdges.get(rand.nextInt(availableEdges.size()));
      addMirroredBlocks(currentISE);
    }
  }

  public void fillBlockPossibilities()
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

  public void addMirroredBlocks(IdSurfaceEdge edgeAddTo)
  {
    int parentID = edgeAddTo.id;
    int parentEdge = edgeAddTo.edge;
    int parentSurface= edgeAddTo.surface;
    int childEdge;
    int childSurface;
    int mirrorParentID = 0;

    if (parentID != 0)
    {
      mirrorParentID = parentID + 1;
    }

    Block parent = getParentBlock(parentID);
    Block mirrorParent = getParentBlock(mirrorParentID);

    childSurface = correspondingChildSurface(edgeAddTo.surface);
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

    findVectorToEdge(parentSize, parentEdge, 1, parentJoint, rotationAxis);
    findVectorToEdge(childSize, childEdge, 0, childJoint, rotationAxis);

    findMirrorVector(parentEdge, parentJoint, parentMirrorJoint);
    findMirrorVector(childEdge, childJoint, childMirrorJoint);

    Block newBlock = addBlock(axisAligned, childSize, parent, parentJoint, childJoint, rotationAxis);
    Block mirrorNewBlock = addBlock(axisAligned, childSize, mirrorParent, parentMirrorJoint, childMirrorJoint, rotationAxis);

    if (!removeIfIntersection())
    {
      addAvailableEdges(newBlock.getID());
      addMirroredNeurons(newBlock, mirrorNewBlock);
      blockProperties.add(makeBlockVectorArray(newBlock, childSize, rotationAxis, rotationAxis));
      blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));
      blockProperties.add(makeBlockVectorArray(mirrorNewBlock, childSize, rotationAxis, rotationAxis));
      blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));
    }
  }


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
      seconds = rand.nextInt(CreatureConstants.MAX_NEURON_SECONDS - CreatureConstants.MIN_NEURON_SECONDS) + CreatureConstants.MIN_NEURON_SECONDS;
      seconds += rand.nextFloat();
      sign = rand.nextInt(2);
      if (sign != 0)
      {
        maxImpulse = -maxImpulse;
      }
      n = makeNewNeuron(maxImpulse, seconds);
      m = makeNewNeuron(-maxImpulse, seconds);
      newBlock.addNeuron(n);
      mirror.addNeuron(m);
    }
  }

  public Neuron makeNewNeuron(float maxImpulse, float seconds)
  {
    Neuron n = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT, EnumNeuronInput.CONSTANT, null);
    n.setInputValue(Neuron.C, seconds);
    n.setInputValue(Neuron.D, maxImpulse);
    return n;
  }

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
