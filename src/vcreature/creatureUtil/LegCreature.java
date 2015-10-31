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

  public LegCreature(PhysicsSpace physicsSpace, Node rootNode)
  {
    this.physicsSpace = physicsSpace;
    this.jMonkeyRootNode = rootNode;

    availableEdges = new ArrayList<>();

    blockProperties = new ArrayList<>();
    blockAngles = new ArrayList<>();

    //int numberOfBlocks = ((rand.nextInt(3)+1)*4)+1;

    int numberOfBlocks = 5;

    makeRandomRoot();
    addAvailableEdges(0);

    while (body.size() < numberOfBlocks && !availableEdges.isEmpty())
    {
      int add = rand.nextInt(availableEdges.size());
      IdSurfaceEdge edgeAddTo = availableEdges.get(add);
      addMirroredBlocks(edgeAddTo);
    }

    bumpUp();
  }

  public void addMirroredBlocks(IdSurfaceEdge edgeAddTo)
  {
    int parentID = edgeAddTo.id;
    int parentEdge = edgeAddTo.edge;
    int childEdge;
    int mirrorParentID = 0;

    if (parentID != 0)
    {
      mirrorParentID = parentID + 1;
    }

    Block parent = getParentBlock(parentID);
    Block mirrorParent = getParentBlock(mirrorParentID);

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
    int sign =0;
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

  public void findVectorToEdge(Vector3f size, int edge, int surface, Vector3f joint, Vector3f rotationAxis)
  {
    int sign = 1;
    if (surface == 1) sign = -1;
      switch (edge)
      {
        case 0:
          joint.x = randomSurfacePoint(size.x);
          joint.y = size.y*(sign);
          joint.z = -size.z;
          rotationAxis.set(Vector3f.UNIT_X);
          break;
        case 1:
          joint.x = size.x;
          joint.y = size.y*(sign);
          joint.z = randomSurfacePoint(size.z);
          rotationAxis.set(Vector3f.UNIT_Z);
          break;
        case 2:
          joint.x = randomSurfacePoint(size.x);
          joint.y = size.y*(sign);
          joint.z = size.z;
          rotationAxis.set(Vector3f.UNIT_X);
          break;
        case 3:
          joint.x = -size.x;
          joint.y = size.y*(sign);
          joint.z = randomSurfacePoint(size.z);
          rotationAxis.set(Vector3f.UNIT_Z);
          break;
        default:
          break;
      }
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
        return true;
      }

      collisionResults.clear();
    }


    return false;
  }

  public void addAvailableEdges(int blockID)
  {
    IdSurfaceEdge temp;
    for (int i = 0; i <=3; ++i)
    {
      temp = new IdSurfaceEdge(blockID, 1, i);
      availableEdges.add(temp);
    }
  }

}
