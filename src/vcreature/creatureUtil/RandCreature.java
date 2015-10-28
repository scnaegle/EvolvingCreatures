package vcreature.creatureUtil;

import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Vector3f;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Transform;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import vcreature.phenotype.*;

public class RandCreature
{
  private final PhysicsSpace physicsSpace;
  private final Node jMonkeyRootNode;
  public ArrayList<Block> body = new ArrayList<>();
  private ArrayList<Vector3f[]> blockProperties;
  private ArrayList<float[]> blockAngles;
  private float[] axisAligned = {0,0,0};
  private static Random rand = new Random();
  private Vector3f tmpVec3 = new Vector3f();

  private float elapsedSimulationTime;

  public RandCreature(PhysicsSpace physicsSpace, Node jMonkeyRootNode)
  {
    this.physicsSpace = physicsSpace;
    this.jMonkeyRootNode = jMonkeyRootNode;

    blockProperties = new ArrayList<>();
    blockAngles = new ArrayList<>();

    int blockNumber = rand.nextInt(CreatureConstants.MAX_BLOCKS)+1;
    //int blockNumber = 3;


    makeRandomRoot();

    while (this.getNumberOfBodyBlocks() < blockNumber)
    {
      int parentBlockID = rand.nextInt(getNumberOfBodyBlocks());
      Block parent = body.get(parentBlockID);
      addRandomBlock(parent);
    }

    bumpUp();
  }

  public int getNumberOfBodyBlocks()
  {
    return body.size();
  }

  /**
   * Get dna object.  If DNA has not been made, make new DNA.
   * @return        dna object
   */
  public DNA getDNA()
  {
    return new DNA(this);
  }

  private Block makeRandomRoot()
  {
    Vector3f rootCenter = new Vector3f(0f,0f,0f);
    Vector3f rootSize = new Vector3f(0f,0f,0f);

    rootSize.x = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    rootSize.y = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    rootSize.z = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;

    blockProperties.add(makeBlockVectorArray(rootCenter, rootSize));
    blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));

    return addRoot(rootCenter, rootSize, axisAligned);
  }

  private void addRandomBlock(Block parent)
  {
    int parentSurface;
    int childSurface;
    int childEdge;

    Neuron newNeuron;

    Vector3f childSize = new Vector3f(0f,0f,0f);
    Vector3f parentSize = new Vector3f(parent.getSizeX()/2, parent.getSizeY()/2, parent.getSize()/2);

    Vector3f parentJoint = new Vector3f(0f,0f,0f);
    Vector3f childJoint = new Vector3f(0f,0f,0f);

    Vector3f rotationAxis = new Vector3f(0f,0f,0f);

    childSize.x = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    childSize.y = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;
    childSize.z = ((rand.nextInt(CreatureConstants.MAX_BLOCK_SIZE)+CreatureConstants.MIN_BLOCK_SIZE) + rand.nextFloat())/2;

    parentSurface = rand.nextInt(6);
    childSurface = correspondingChildSurface(parentSurface);
    childEdge = rand.nextInt(4);

    findSurfaceVectorParent(parentJoint, parentSurface, parentSize);
    findSurfaceVectorChild(childSurface, childEdge, childJoint, childSize, rotationAxis);

    Block newBlock = addBlock(axisAligned, childSize, parent, parentJoint, childJoint, rotationAxis);

    if (!removeIfIntersection())
    {
      newNeuron = makeRandomNeuron();
      newBlock.addNeuron(newNeuron);
      blockProperties.add(makeBlockVectorArray(newBlock, childSize, rotationAxis, rotationAxis));
      blockAngles.add(Arrays.copyOf(axisAligned, axisAligned.length));
    }


  }

  private Neuron makeRandomNeuron()
  {
    float seconds = (rand.nextInt(20)+1) + rand.nextFloat();
    float impulse = Float.MAX_VALUE;
    int sign = rand.nextInt(2);

    Neuron n = new Neuron(EnumNeuronInput.TIME, null, EnumNeuronInput.CONSTANT, EnumNeuronInput.CONSTANT, null);

    if (sign == 1)
    {
      impulse = -impulse;
    }

    n.setInputValue(Neuron.C, seconds);
    n.setInputValue(Neuron.D, impulse);

    return n;
  }


  public Block addRoot(Vector3f rootCenter, Vector3f rootHalfSize, float[] eulerAngles)
  {
    if (!body.isEmpty())
    { throw new IllegalArgumentException("This creature already has a root.");
    }

    Quaternion rotation = new Quaternion(eulerAngles);

    Block root = new Block(physicsSpace, jMonkeyRootNode, body.size(), rootCenter, rootHalfSize, rotation);

    body.add(root);

    return root;
  }

  public Block addBlock(float[] eulerAngles, Vector3f halfsize, Block parent, Vector3f pivotA, Vector3f pivotB, Vector3f axisA)
  {
    if (body.isEmpty())
    { throw new IllegalArgumentException("This creature does not have a root Block.");
    }

    //Adding block resets the fitness of the creature.
    elapsedSimulationTime = 0;

    Quaternion rotationB = new Quaternion(eulerAngles);

    Transform parantTransform = parent.getGeometry().getWorldTransform();
    Vector3f pivotA_World = new Vector3f();
    parantTransform.transformVector(pivotA, pivotA_World);

    Transform childTransform = new Transform(rotationB);

    Vector3f centerB = new Vector3f();
    childTransform.transformVector(pivotB, centerB);
    centerB.negateLocal();

    centerB.addLocal(pivotA_World);

    //print("center=",centerB);

    Block block = new Block(physicsSpace, jMonkeyRootNode, body.size(), centerB, halfsize, rotationB);
    body.add(block);


    Vector3f axisB = new Vector3f(axisA);
    Transform parantRotation = new Transform(parent.getStartRotation());
    parantRotation.transformVector(axisA, axisB);
    Transform inverseChildRotation = new Transform(rotationB.inverse());
    inverseChildRotation.transformVector(axisB, axisB);

    //print("axisB=",axisB);

    RigidBodyControl controlA = parent.getPhysicsControl();
    RigidBodyControl controlB = block.getPhysicsControl();
    HingeJoint joint = new HingeJoint(controlA, controlB, pivotA, pivotB, axisA, axisB);
    joint.setCollisionBetweenLinkedBodys(true);

    joint.setLimit(PhysicsConstants.JOINT_ANGLE_MIN, PhysicsConstants.JOINT_ANGLE_MAX);
    block.setJointToParent(parent, joint);

    physicsSpace.add(joint);

    return block;
  }

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
        return true;
      }

      collisionResults.clear();
    }
    return false;
  }

  /**
   * Corresponding ints to surfaces on blocks
   * 0 = +y;
   * 1 = -y;
   * 2 = +x;
   * 3 = -x;
   * 4 = +z;
   * 5 = -z;
   * @param parentSurface
   * @return
   */
  private int correspondingChildSurface(int parentSurface)
  {
    int childSurface = -1;
    switch (parentSurface) {
      case 0: childSurface = 1;
        break;
      case 1: childSurface = 0;
        break;
      case 2: childSurface = 3;
        break;
      case 3: childSurface = 2;
        break;
      case 4: childSurface = 5;
        break;
      case 5: childSurface = 4;
        break;
    }
    return childSurface;
  }

  private void findSurfaceVectorParent(Vector3f joint, int surface, Vector3f pSize)
  {
    switch (surface) {
      case 0: joint.y = pSize.y;
        joint.x = randomSurfacePoint(pSize.x);
        joint.z = randomSurfacePoint(pSize.z);
        break;
      case 1: joint.y = -pSize.y;
        joint.x = randomSurfacePoint(pSize.x);
        joint.z = randomSurfacePoint(pSize.z);
        break;
      case 2: joint.x = pSize.x;
        joint.y = randomSurfacePoint(pSize.y);
        joint.z = randomSurfacePoint(pSize.z);
        break;
      case 3: joint.x = -pSize.x;
        joint.y = randomSurfacePoint(pSize.y);
        joint.z = randomSurfacePoint(pSize.z);
        break;
      case 4: joint.z = pSize.z;
        joint.x = randomSurfacePoint(pSize.x);
        joint.y = randomSurfacePoint(pSize.y);
        break;
      case 5: joint.z = -pSize.z;
        joint.x = randomSurfacePoint(pSize.x);
        joint.y = randomSurfacePoint(pSize.y);
        break;
      default:
        break;
    }
  }

  private void findSurfaceVectorChild(int childSurface, int childEdge, Vector3f joint, Vector3f cSize, Vector3f rAxis)
  {
    if (childSurface == 0)
    {
      switch (childEdge) {
        case 0:
          joint.x = randomSurfacePoint(cSize.x);
          joint.y = cSize.y;
          joint.z = -cSize.z;
          randXorY(rAxis);
          //XorY
          break;
        case 1:
          joint.x = cSize.x;
          joint.y = cSize.y;
          joint.z = randomSurfacePoint(cSize.y);
          randYorZ(rAxis);
          //ZorY

          break;
        case 2:
          joint.x = randomSurfacePoint(cSize.x);
          joint.y = cSize.y;
          joint.z = cSize.z;
          randXorY(rAxis);
          //XorY

          break;
        case 3:
          joint.x = -cSize.x;
          joint.y = cSize.y;
          joint.z = randomSurfacePoint(cSize.z);
          randYorZ(rAxis);
          //ZorY

          break;
        default:
          break;
      }
    }

    else if (childSurface == 1)
    {
      switch (childEdge) {
        case 0:
          joint.x = randomSurfacePoint(cSize.x);
          joint.y = -cSize.y;
          joint.z = -cSize.z;
          randXorY(rAxis);
          //XorY

          break;
        case 1:
          joint.x = cSize.x;
          joint.y = -cSize.y;
          joint.z = randomSurfacePoint(cSize.z);
          randYorZ(rAxis);
          //ZorY

          break;
        case 2:
          joint.x = randomSurfacePoint(cSize.x);
          joint.y = -cSize.y;
          joint.z = cSize.z;
          randXorY(rAxis);
          //XorY

          break;
        case 3:
          joint.x = -cSize.x;
          joint.y = -cSize.y;
          joint.z = randomSurfacePoint(cSize.z);
          randYorZ(rAxis);
          //ZorY

          break;
        default:
          break;
      }
    }

    else if (childSurface == 2)
    {
      switch (childEdge) {
        case 0:
          joint.x = cSize.x;
          joint.y = cSize.y;
          joint.z = randomSurfacePoint(cSize.z);
          randXorZ(rAxis);
          //ZorX

          break;
        case 1:
          joint.x = cSize.x;
          joint.y = randomSurfacePoint(cSize.y);
          joint.z = -cSize.z;
          randXorY(rAxis);
          //YorX

          break;
        case 2:
          joint.x = cSize.x;
          joint.y = -cSize.y;
          joint.z = randomSurfacePoint(cSize.z);
          randXorZ(rAxis);
          //ZorX
          break;

        case 3:
          joint.x = cSize.x;
          joint.y = randomSurfacePoint(cSize.y);
          joint.z = cSize.z;
          randXorY(rAxis);
          //YorX

          break;
        default:
          break;
      }
    }

    else if (childSurface == 3)
    {
      switch (childEdge) {
        case 0:
          joint.x = -cSize.x;
          joint.y = cSize.y;
          joint.z = randomSurfacePoint(cSize.z);
          randXorZ(rAxis);
          //ZorX
          break;
        case 1:
          joint.x = -cSize.x;
          joint.y = randomSurfacePoint(cSize.y);
          joint.z = cSize.z;
          randXorY(rAxis);
          //YorX
          break;
        case 2:
          joint.x = -cSize.x;
          joint.y = -cSize.y;
          joint.z = randomSurfacePoint(cSize.z);
          randXorZ(rAxis);
          //ZorX

          break;
        case 3:
          joint.x = -cSize.x;
          joint.y = randomSurfacePoint(cSize.y);
          joint.z = -cSize.z;
          randXorY(rAxis);
          //YorX
          break;
        default:
          break;
      }
    }

    //+z surface
    else if (childSurface == 4)
    {
      switch (childEdge) {
        case 0:
          joint.x = randomSurfacePoint(cSize.x);
          joint.y = cSize.y;
          joint.z = cSize.z;
          randXorZ(rAxis);
          //XorZ
          break;
        case 1:
          joint.x = cSize.x;
          joint.y = randomSurfacePoint(cSize.y);
          joint.z = cSize.z;
          randYorZ(rAxis);
          //YorZ
          break;
        case 2:
          joint.x = randomSurfacePoint(cSize.x);
          joint.y = -cSize.y;
          joint.z = cSize.z;
          randXorZ(rAxis);
          //XorZ
          break;
        case 3:
          joint.x = -cSize.x;
          joint.y = randomSurfacePoint(cSize.y);
          joint.z = cSize.z;
          randYorZ(rAxis);
          //YorZ
          break;
        default:
          break;
      }
    }

    //-z surface
    else
    {
      switch (childEdge) {
        case 0:
          joint.x =randomSurfacePoint(cSize.x);
          joint.y = cSize.y;
          joint.z = -cSize.z;
          randXorZ(rAxis);
          //XorZ
          break;
        case 1:
          joint.x = -cSize.x;
          joint.y = randomSurfacePoint(cSize.y);
          joint.z = -cSize.z;
          randYorZ(rAxis);
          //YorZ
          break;
        case 2:
          joint.x = randomSurfacePoint(cSize.x);
          joint.y = -cSize.y;
          joint.z = -cSize.z;
          randXorZ(rAxis);
          //XorZ
          break;
        case 3:
          joint.x = cSize.x;
          joint.y = randomSurfacePoint(cSize.y);
          joint.z = -cSize.z;
          randYorZ(rAxis);
          //YorZ
          break;
        default:
          break;
      }
    }
  }

  private void randXorZ(Vector3f rAxis)
  {
    int randI = rand.nextInt(2);
    if (randI == 0)
    {
      rAxis.set(Vector3f.UNIT_X);
    }
    else
    {
      rAxis.set(Vector3f.UNIT_Z);
    }
  }

  private void randYorZ(Vector3f rAxis)
  {
    int randI = rand.nextInt(2);
    if (randI == 0)
    {
      rAxis.set(Vector3f.UNIT_Y);
    }
    else
    {
      rAxis.set(Vector3f.UNIT_Z);
    }
  }

  private void randXorY(Vector3f rAxis)
  {
    int randI = rand.nextInt(2);
    if (randI == 0)
    {
      rAxis.set(Vector3f.UNIT_X);
    }
    else
    {
      rAxis.set(Vector3f.UNIT_Y);
    }
  }

  private float randomSurfacePoint(float bounds)
  {
    int sign = rand.nextInt(2);
    int scale = (int) bounds;

    float point = rand.nextInt(scale+1) + rand.nextFloat();

    if (sign == 1)
    {
      point = -point;
    }

    return point;
  }

  private void removeBlock(int blockID)
  {
    Block b = body.get(blockID);
    physicsSpace.remove(b.getPhysicsControl());
    HingeJoint jointToParent = b.getJoint();
    if (jointToParent != null) physicsSpace.remove(jointToParent);

    Geometry geometry = b.getGeometry();
    geometry.removeFromParent();

    body.remove(b);
  }

  private Block getBlockByID(int id)
  {
    return body.get(id);
  }

  private float getCurrentHeightOfLowestPoint()
  {
    float lowestPoint = Float.MAX_VALUE;
    float testPoint;
    Vector3f[] tempVecArray;
    Vector3f tempVecCenter;
    Vector3f tempVecSize;
    for (int i = 0; i < getNumberOfBodyBlocks(); ++i)
    {
      tempVecArray = blockProperties.get(i);
      tempVecCenter=tempVecArray[0];
      tempVecSize=tempVecArray[1];
      testPoint = tempVecCenter.y - tempVecSize.y;
      if (testPoint < lowestPoint) lowestPoint = testPoint;
    }
    System.out.println("LOWEST POINT: " + lowestPoint);
    return lowestPoint;
  }

  public void populateVectorDNA(int id, Vector3f[] vecArr)
  {
    vecArr[BlockVector.CENTER.ordinal()] = new Vector3f(getBlockVector(id,
            BlockVector.CENTER));
    vecArr[BlockVector.SIZE.ordinal()] = new Vector3f(getBlockVector(id,
            BlockVector.SIZE));
    if(getBlockByID(id).getJoint() != null)
    {
      vecArr[BlockVector.JOINT_A.ordinal()] = new Vector3f(getBlockVector(id,
              BlockVector.JOINT_A));
      vecArr[BlockVector.JOINT_B.ordinal()] = new Vector3f(getBlockVector(id,
              BlockVector.JOINT_B));
      vecArr[BlockVector.AXIS_A.ordinal()] = new Vector3f(getBlockVector(id,
              BlockVector.AXIS_A));
      vecArr[BlockVector.AXIS_B.ordinal()] = new Vector3f(getBlockVector(id,
              BlockVector.AXIS_B));
    }
  }

  /**
   * Returns the specified vector info for the specified block.
   * Caution! returns pointer to block info vector stored in OurCreature, can
   * corrupt data.
   * @param id        id of block.
   * @param v         Vector type.
   * @return          Specified vector3f in blockProperties.
   */
  private Vector3f getBlockVector(int id, BlockVector v)
  {
    return blockProperties.get(id)[v.ordinal()];
  }



  /**
   * make blockProperties array for Root Block.  Stores location (center) and
   * halfsize that will be passed to constructor.  Joint related properties are
   * set to null.  Use when adding root.
   * @param center        block location
   * @param size          block size
   * @return              Vector3f containing axis' that we need access to.
   */
  private Vector3f[] makeBlockVectorArray(Vector3f center, Vector3f size)
  {
    Vector3f[] blockProperties = new Vector3f[6];
    blockProperties[BlockVector.CENTER.ordinal()] = center;
    blockProperties[BlockVector.SIZE.ordinal()] = size;
    blockProperties[BlockVector.JOINT_A.ordinal()] = null;
    blockProperties[BlockVector.JOINT_B.ordinal()] = null;
    blockProperties[BlockVector.AXIS_A.ordinal()] = null;
    blockProperties[BlockVector.AXIS_B.ordinal()] = null;

    return blockProperties;
  }

  /**
   * Make block properties array for standard block.  Use when adding blocks.
   * Center is set to block.startCenter but is not used.
   * size is halfsize that is passed to the constructor.
   * joint related vectors are local coordinates relative to the block's center.
   * @param b
   * @param size
   * @param axisA
   * @param axisB
   * @return
   */
  private Vector3f[] makeBlockVectorArray(Block b, Vector3f size, Vector3f axisA,
                                          Vector3f axisB)
  {
    Vector3f[] blockProperties = new Vector3f[6];
    tmpVec3 = b.getStartCenter(tmpVec3);
    blockProperties[BlockVector.CENTER.ordinal()] = new Vector3f(tmpVec3);
    blockProperties[BlockVector.SIZE.ordinal()] = size;
    if(b.getJoint() != null)
    {
      blockProperties[BlockVector.JOINT_A.ordinal()] = b.getJoint().getPivotA();
      blockProperties[BlockVector.JOINT_B.ordinal()] = b.getJoint().getPivotB();
      blockProperties[BlockVector.AXIS_A.ordinal()] = axisA;
      blockProperties[BlockVector.AXIS_B.ordinal()] = axisB;
    }
    return blockProperties;
  }

  public float[] getBlockAngles(int id)
  {
    return blockAngles.get(id);
  }

  public void bumpUp()
  {
    //Vector3f[] tempVecArray;
    //Vector3f tempVec;
    float lowestPoint = getCurrentHeightOfLowestPoint();
    for (int i = 0; i < getNumberOfBodyBlocks(); ++i)
    {
      blockProperties.get(i)[0].y -= lowestPoint;
    }
  }

  public void remove()
  {
    if (body.size() > 0)
    { removeSubTree(body.get(0));
    }

    if (body.size() != 0)
    {
      System.out.println("ERROR: vcreature.phenotype.Creature.remove() failed");
    }

    elapsedSimulationTime  = 0;
  }



  /**
   * This method should only be called by itself or remove()
   * to remove ALL blocks from the creature.
   * @param block
   */
  private void removeSubTree(Block block)
  {
    for (Block child : block.getChildList())
    {
      removeSubTree(child);
    }

    physicsSpace.remove(block.getPhysicsControl());
    HingeJoint jointToParent = block.getJoint();
    if (jointToParent != null) physicsSpace.remove(jointToParent);

    Geometry geometry = block.getGeometry();
    geometry.removeFromParent();

    body.remove(block);
  }

}
