package capsis.kernel;

import java.lang.instrument.*;  

/**
 * This Agent helps getting information at runtime on the jvm.
 * It must be packed into a agent.jar file.
 * It must be told to the JVM at runtime: java -javaagent:agent.jar MyMainClass
 * Its premain() method will be called before MyMainClass.main ()
 * It is then possible to get the instrumentation object with JavaAgent.getInstrumentation()
 * and ask it interesting questions.
 * E.g. Class[] getAllLoadedClasses() / long getObjectSize(Object objectToSize)...
 * 
 * @author F. de Coligny - november 2011
 * From Jim Yingst, http://www.coderanch.com/t/329407/java/java/find-all-loaded-classes-classloaders
 */
public class JavaAgent {  
    private static Instrumentation inst;  
   
    public static Instrumentation getInstrumentation() { return inst; }  
   
    public static void premain(String agentArgs, Instrumentation inst) {  
        System.out.println(inst.getClass() + ": " + inst);  
        JavaAgent.inst = inst;  
    }  
}  