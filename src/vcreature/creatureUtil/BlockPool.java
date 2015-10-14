package vcreature.creatureUtil;

import vcreature.phenotype.Block;

import java.util.LinkedList;

/**
 * Object pool for Blocks.  Keep the Blocks around since we'll be using
 * lots of them and save on Garbage collection.
 * One Global? One for each thread?
 * @author Justin Thomas(jthomas105@unm.edu)
 */
public class BlockPool
{
  private LinkedList<Block> pool;

  /**
   * Instantiate the list
   */
  public BlockPool()
  {
    pool = new LinkedList<>();
  }

  /**
   * Add a block to the list.
   * @param s       block to add
   */
  public void checkIn(Block s)
  {
    pool.add(s);
  }

  /**
   * Check a block out from the pool.  Remove from pool and return.  If pool is
   * empty, instantiate new block
   * @return        Block.
   */
  public Block checkOut()
  {
    if(pool.isEmpty())
    {
      return pool.poll();
    }
    else
    {
      //Instantiate and return new Block.
      return null;
    }
  }
}
