package vcreature.creatureUtil;

import org.json.JSONException;
import org.json.JSONObject;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Justin Thomas 10/14/2015
 * JSON code from Sean, decoupled from creature class.
 * Contains static methods for dealing with JSON Objects.
 */
public class DNA
{
  //  public void buildFromJSON(JSONObject json) {
//    JSONObject myjson = new JSONObject(the_json);
//    JSONArray the_json_array = myjson.getJSONArray("profiles");
//    int size = the_json_array.length();
//    ArrayList<JSONObject> arrays = new ArrayList<JSONObject>();
//    for (int i = 0; i < size; i++) {
//      JSONObject another_json_object = the_json_array.getJSONObject(i);
//      //Blah blah blah...
//      arrays.add(another_json_object);
//    }
//
////Finally
//    JSONObject[] jsons = new JSONObject[arrays.size()];
//    arrays.toArray(jsons);
//  }

  /**
   * Converts creature c to a JSONObject
   * @param c       Creature to parse.
   * @return        Return JSON representation of genome.
   */
  public static JSONObject toJSON(Creature c) {
    HashMap<String, Object> character_hash = new HashMap<>();
    int size = c.getNumberOfBodyBlocks();
    character_hash.put("number_of_blocks", size);
    ArrayList<HashMap<String, Object>> body_details = new ArrayList<>();
    Block root;
    root = c.getBlockByID(0);
    getBlocks(root, body_details);
    character_hash.put("blocks", body_details);


    JSONObject json = new JSONObject(character_hash);

    return json;
  }

  /**
   * Recursive function to populate the JSONObject with block info nested by
   * parent child relationship.
   * @param part        block in question for this level
   * @return            block list.
   */
  private static void getBlocks(Block part, ArrayList<HashMap<String, Object>> partMap)
  {
    partMap.add(part.toHash());
    ArrayList<Block> children = part.getChildList();
    for(Block child : children)
    {
      getBlocks(child, partMap);
    }
  }

  /**
   * Write JSONObject to output file
   * @param jsonObj   JSONObject to write
   */
  public static void writeGenomeFile(JSONObject jsonObj)
  {

    try
    {
      FileWriter w = new FileWriter("dnaOut.txt");
      jsonObj.write(w);
      w.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.out.println("Write error.");
    }
    catch(JSONException e)
    {
      e.printStackTrace();
      System.out.println("JSON error.");
    }
  }

  /**
   * Test json file reader.  Reads and creates a JSONObject.
   * @param path        Path to file to use
   * @return            JSONObject created
   */
  public static JSONObject readGenomeFile(String path)
  {
    JSONObject obj = null;
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(path));
      String lineIn = reader.readLine();
      obj = new JSONObject(lineIn);
    }
    catch(FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    catch(JSONException e)
    {
      e.printStackTrace();
    }
    return obj;
  }
}
