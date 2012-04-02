public class Node implements Comparable<Node> {
   
   int name;
   //boolean visited = false;   // used for Kosaraju's algorithm and Edmonds's algorithm
   int lowlink = -1;          // used for Tarjan's algorithm
   int index = -1;            // used for Tarjan's algorithm
   String associatedModel = "";
   String associatedRefConcept = "";
   String uri = "";
   String label = "";
   int distfromsource = Integer.MAX_VALUE;
   
   public Node(final int argName) {
       name = argName;
   }
   
   public int compareTo(final Node argNode) {
       return argNode == this ? 0 : -1;
   }
}