package vcreature.phenotype;


public class Neuron
{
  public static final int A=0;
  public static final int B=1;
  public static final int C=2;
  public static final int D=3;
  public static final int E=4;

  public static final int TOTAL_INPUTS    = 5;
  
  public static final int TOTAL_OPERATORS = 4;
  
  
  
  private EnumNeuronInput[] type = new EnumNeuronInput[TOTAL_INPUTS]; 
  private float[] constantValue = new float[TOTAL_INPUTS];  //used only for CONSTANT type.
  private int[]   blockIndex    = new   int[TOTAL_INPUTS];  //used for HEIGHT, TOUCH and JOINT types.
  
  private EnumOperator[] operator =  new EnumOperator[TOTAL_OPERATORS];
  
  /**
   * Instantiates a new Neuron.
   * Each effector (joint) has a sequence of 0 to n Neurons.<br>
   * Each Neuron can take up to 5 inputs. Any inputs that are left undefined default to
   * type=CONSTANT, value=0.
   * 
   * The default binary operator is +.
   * The default unary oporator is Identity.
   * 
   * Each rule takes five inputs (a, b, c, d, and e). 
   * Input can come from any one of five types of sources: <br>
   * 1)  The height of the center of gravity of the ith block in the creature's block array.<br>
   * 2)  The touch value of block i.<br>
   * 3)  The angle of the joint between block i and its parent.<br>
   * 4)  A constant float value.<br>
   * 5)  A global time equal to the number of simulation seconds since start.<br><br>
   * Each rule has four operators: two binary operators, and two unary operators. 
   * If the output of the first neuron, y, is greater 
   * than input c, then the second neuron in the rule is evaluated. 
   * The output of the second neuron (with inputs d and e) is the force
   * sent to the attached effector.<br><br>
   * 
   * Each sequence of Neurons form what is called a rule table that is evaluated 
   * from index 0 through n. The first Neuron in a rule table for which the output to the 
   * threshold trigger (if (y>c)) is true, will have its second half (with inputs d and e) evaluated 
   * and the second half output sent to the effector as an impulse to the joint.
   * Evaluation of the rule table stops when the first neuron threshold is triggered or when
   * all neurons in the table have failed to fire.<br>
   * When a neuron is activated, an output is sent to its attached effector as an inpulse (Newton seconds).<br>
   * 
   */
  
  public Neuron(EnumNeuronInput a, EnumNeuronInput b, EnumNeuronInput c,
                EnumNeuronInput d, EnumNeuronInput e) 
  {
    if (a!=null) type[A] = a; else type[A] = EnumNeuronInput.CONSTANT;
    if (b!=null) type[B] = b; else type[B] = EnumNeuronInput.CONSTANT;
    if (c!=null) type[C] = c; else type[C] = EnumNeuronInput.CONSTANT;
    if (d!=null) type[D] = d; else type[D] = EnumNeuronInput.CONSTANT;
    if (e!=null) type[E] = e; else type[E] = EnumNeuronInput.CONSTANT;
    
    operator[0] = EnumOperator.ADD;
    operator[2] = EnumOperator.ADD;
    
    operator[1] = EnumOperator.IDENTITY;
    operator[3] = EnumOperator.IDENTITY;
  }
  
  public EnumOperator    getOp(int i) {return operator[i];}
  public EnumNeuronInput getInputType(int i) {return type[i];}
  public float getInputValue(int i) {return constantValue[i];}
  public int   getBlockIdx(int i) {return blockIndex[i];}
  
  
  public void setOp(EnumOperator operatortype, int i) 
  { 
    if (i == 0 || i ==2)
    { if (operatortype.isUnary()) 
      { throw new IllegalArgumentException("Operator idx="+i+" must be binary");
      }
    }
    else 
    { if (!operatortype.isUnary()) 
      { throw new IllegalArgumentException("Operator idx="+i+" must be unary");
      }
    }
    operator[i]=operatortype;
  }
  public void setInputType (int i, EnumNeuronInput inputType) {type[i]=inputType;}
  public void setInputValue(int i, float value) {constantValue[i]=value;}
  public void setBlockIdx  (int i, int blockId)   {blockIndex[i]=blockId;}
  
  
  public float getOutput(float a, float b, int opIdx)
  {
    float x = 0;
    if (operator[opIdx] == EnumOperator.ADD)           x = a+b;
    else if (operator[opIdx] == EnumOperator.SUBTRACT) x = a-b;
    else if (operator[opIdx] == EnumOperator.MULTIPLY) x = a*b;
    else if (operator[opIdx] == EnumOperator.POWER)    x = (float)Math.pow(a,b);
    
    if (operator[opIdx+1] == EnumOperator.ABS)           return Math.abs(x);
    else if (operator[opIdx+1] == EnumOperator.IDENTITY) return x;
    else if (operator[opIdx] == EnumOperator.SIN)        return (float)Math.sin(x);
    else if (operator[opIdx] == EnumOperator.NEGATIVE)   return -x;
    
    return 0;
  }
  
  
  
  
  
  
  
  public String toString()
  {
    String out = "Neuron: if {" + type[0] + " " + operator[0] + " " + type[1] + 
        " --> " + operator[1] + "} > {" + type[2] + 
        " then {" + type[3] + ", " + operator[3] + " " + type[4] + " --> " + operator[4];
    return out;
  }
 
}
