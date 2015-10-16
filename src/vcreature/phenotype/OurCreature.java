package vcreature.phenotype;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import vcreature.creatureUtil.DNA;

/**
 * @author Justin Thomas(jthomas105@unm.edu)
 * Our Creature extends Joel's creature.
 */
public class OurCreature extends Creature
{
  private DNA dna;
  /**
   * Default constructor, attaches creature to world
   * @param physicsWorld
   * @param visualWorld
   */
  public OurCreature(PhysicsSpace physicsWorld, Node visualWorld)
  {
    super(physicsWorld, visualWorld);
  }

  /**
   * Constructor that creates creature with DNA object
   * @param physWorld       the physics world this belongs to.
   * @param visWorld        the
   * @param dna
   */
  public OurCreature(PhysicsSpace physWorld, Node visWorld, DNA dna)
  {
    super(physWorld, visWorld);
    this.dna = dna;
  }

  /**
   * Build the creature using it's DNA object.
   */
  private void buildWithDNA()
  {

  }
}
