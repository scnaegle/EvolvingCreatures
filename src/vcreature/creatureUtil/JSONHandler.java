package vcreature.creatureUtil;

import org.json.JSONObject;
import vcreature.phenotype.Block;
import vcreature.phenotype.Creature;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Justin Thomas(jthomas105@unm.edu)
 * JSON code from Sean, decoupled from creature class.
 */
public class JSONHandler
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


    Block part;
    ArrayList<HashMap<String, Object>> body_details = new ArrayList<>();
    for(int i = 0; i < size; ++i)
    {
      part = c.getBlockByID(i);
      body_details.add(part.toHash());
    }

    character_hash.put("blocks", body_details);
    JSONObject json = new JSONObject(character_hash);

    return json;
  }
}
