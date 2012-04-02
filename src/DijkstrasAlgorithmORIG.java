import java.util.*;

public class DijkstrasAlgorithmORIG {
	public static int[] D;
	public static Node[] P;
	public static ArrayList<Node> C;
	public static int[][] Weight;
	public static Edge[] edgearray;
	public DijkstrasAlgorithmORIG(){
		
	}
   // assumes Nodes are numbered 0, 1, ... n and that the source Node is 0
   ArrayList<Node> findShortestPath(Node target) {
	   System.out.println("Source: " + BioSimConnector.nodearray[0].uri);
	   System.out.println("Target: " + target.uri);
       initializeWeight();

       D = new int[BioSimConnector.nodearray.length];	// distances from source to other nodes
       P = new Node[BioSimConnector.nodearray.length];  // previous node
       C = new ArrayList<Node>();

       // initialize:
       // (C)andidate set,
       // (D)yjkstra special path length, and
       // (P)revious Node along shortest path
       for(int i=0; i<BioSimConnector.nodearray.length; i++){
           C.add(BioSimConnector.nodearray[i]);
          // D[i] = BioSimConnector.Weight[0][i];
           if(D[i] != Integer.MAX_VALUE){
               P[i] = BioSimConnector.nodearray[0];
           }
       }

       // crawl the graph
       for(int i=0; i<BioSimConnector.nodearray.length-1; i++){
           // find the lightest Edge among the candidates
           int l = Integer.MAX_VALUE;
           Node n = BioSimConnector.nodearray[0];
           for(Node node : C){
               if(D[node.name] < l){
                   n = node;
                   l = D[node.name];
               }
           }
           C.remove(n);

           // see if any Edges from this Node yield a shorter path than from source->that Node
           for(int j=0; j<BioSimConnector.nodearray.length-1; j++){
        	   // if the distance from source to node n isn't inf and there's a lower weight between node n and node name j than between source and node name j, 
        	   // then set distance of node name j from source to distance of node n from source plus distance of node n from j
               if(D[n.name] != Integer.MAX_VALUE && Weight[n.name][j] != Integer.MAX_VALUE && D[n.name]+
            		   Weight[n.name][j] < D[j]){
                   // found one, update the path
                   D[j] = D[n.name] + Weight[n.name][j];
                   P[j] = n;
               }
           }
       }
       // we have our path. reuse C as the result list
       C.clear();
       int loc = target.name;
       C.add(target);
       // backtrack from the target by P(revious), adding to the result list
       while(P[loc] != BioSimConnector.nodearray[0]){
           if(P[loc] == null){
               // looks like there's no path from source to target
        	   System.out.println("No path");
               return null;
           }
           C.add(0, P[loc]);
           loc = P[loc].name;
       }
       C.add(0, BioSimConnector.nodearray[0]);
       System.out.println("Size of C: " + C.size());
       return C;
   }

   private void initializeWeight(){
       for(int i=0; i<BioSimConnector.nodearray.length; i++){
           Arrays.fill(Weight[i], Integer.MAX_VALUE);
       }
       for(Edge e : edgearray){
           Weight[e.from.name][e.to.name] = e.weight;
       }
   }
}