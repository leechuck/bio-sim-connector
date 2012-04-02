public class Edge implements Comparable<Edge> {
   
   final Node from, to;
   final int weight;
   final String relation;
   
   public Edge(final Node argFrom, final Node argTo, final int argWeight, final String argRelation){
       from = argFrom;
       to = argTo;
       weight = argWeight;
       relation = argRelation;
   }
   
   public int compareTo(final Edge argEdge){
       return weight - argEdge.weight;
   }
}