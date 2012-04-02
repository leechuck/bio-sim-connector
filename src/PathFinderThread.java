
import java.awt.Component;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Statement;

public class PathFinderThread implements Runnable {
	public boolean running;
	public Thread looper = new Thread(this);
	public int value;
	public String runningmethod;
	public Method method;
	public static boolean cont = false;
	public Node startnode;
	public Node targetnode;

	public PathFinderThread() {
		running = false;
	}

	public void start() {
		if (!running) {
			running = true;
			cont = true;
			BioSimConnector.gobutton.setText("Stop");
			looper.start();
		}
	}
	
	public void stop(){
		cont = false;
		running = false;
		BioSimConnector.gobutton.setText("Go");
		BioSimConnector.gobutton.repaint();
		BioSimConnector.gobutton.validate();
		
	}

	public void run() {
			findPaths();
	}

	// The primary method for finding shortest paths through terms in the knowledge base
	public void findPaths(){
		String sourceuri = BioSimConnector.autonamesuris.get(BioSimConnector.sourcebox.getSelectedItem());
		String targeturi = BioSimConnector.autonamesuris.get(BioSimConnector.targetbox.getSelectedItem());
		BioSimConnector.resultslist.removeAll();
		Boolean nopaths = true;
		ArrayList<DijkstrasResult> results = new ArrayList<DijkstrasResult>();
		Comparator<Component> byScore = new ComparatorByScore();
		DijkstrasResult[] resultsarray = new DijkstrasResult[]{};
		ArrayList<String> displaylist = new ArrayList<String>();

		int cons = 0;
		
		Set<String> sourceuris = new HashSet<String>();
		Set<String> targeturis = new HashSet<String>();
		
		int posofsource = BioSimConnector.urisandnodes.get(sourceuri).name;
		for(int f=BioSimConnector.rowlist.indexOf(posofsource); f<=BioSimConnector.rowlist.lastIndexOf(posofsource);f++){
			if((int)BioSimConnector.valuelist.get(f) == BioSimConnector.baseweight){
				sourceuris.add(BioSimConnector.nodearray[BioSimConnector.columnlist.get(f)].uri);
			}
		}
		int posoftarget = BioSimConnector.urisandnodes.get(targeturi).name;
		for(int g=BioSimConnector.rowlist.indexOf(posoftarget); g<=BioSimConnector.rowlist.lastIndexOf(posoftarget);g++){
			if((int)BioSimConnector.valuelist.get(g) == BioSimConnector.baseweight){
				targeturis.add(BioSimConnector.nodearray[BioSimConnector.columnlist.get(g)].uri);
			}
		}
		
		
		float totalcons = sourceuris.size() * targeturis.size();
		// Total number of node-to-node paths that we will attempt to find
		System.out.println(totalcons + " pairs");
		Iterator sourceit = sourceuris.iterator();
		int i = 0;
		while(sourceit.hasNext() && cont){
			String stsource = (String)sourceit.next();
			Iterator targetit = targeturis.iterator();
			while(targetit.hasNext() && cont){
				i++;
				String sttarget = (String)targetit.next();
				Runtime.getRuntime().gc();
				
				DijkstrasResult result = runDijkstrasAlgorithm(BioSimConnector.urisandnodes.get(stsource), BioSimConnector.urisandnodes.get(sttarget));
				
				displaylist.clear();

				// If we do have a connection b/w the two concepts
				if(result.nodepath!=null && result.pathscore!=Integer.MAX_VALUE){
					// Add the result to the tally
					results.add(result);
					nopaths = false;
				}
				else{
					//BioSimConnector.resultslist.setListData(displaylist.toArray(new String[]{}));
					displaylist.add("Attempt " + i + " failed to find a path between concepts\n");
					BioSimConnector.log.debug("Attempt " + i + " failed to find a path concepts:\n " + stsource + "\n " + sttarget);

				}
				resultsarray = results.toArray(new DijkstrasResult[]{});
				
				// Rank the results by score (lowest is best)
				Arrays.sort(resultsarray, byScore);
				
				// Refresh the results that will be displayed
				for(int r=0;r<resultsarray.length;r++){
					if(resultsarray[r].pathscore!=Integer.MAX_VALUE){
						displaylist.add("Path score: " + resultsarray[r].pathscore);
						BioSimConnector.log.debug("Path score: " + resultsarray[r].pathscore);
						for(int x=0; x<resultsarray[r].nodepath.size(); x++){
							String uri = resultsarray[r].nodepath.get(x).uri;
							String spacer = ""; for(int s=0;s<x;s++){spacer = spacer + " ";}
							String suffix = " in " + BioSimConnector.getModelNameFromURI(uri);
							if(BioSimConnector.nsandtlas.keySet().contains(OWLMethods.getNamespaceFromIRI(uri))){
								suffix = " (" + uri + ")";
							}
							displaylist.add(spacer + "'" + resultsarray[r].nodepath.get(x).label + "'" + suffix);
							// Add the relation that connects the terms
							if(x<resultsarray[r].nodepath.size()-1){
								displaylist.add(spacer + " <" + OWLMethods.getOWLEntityNameFromIRI(findRelationBetweenNodes(resultsarray[r].nodepath.get(x),resultsarray[r].nodepath.get(x+1))) + ">");
							}
						}
						displaylist.add("\n");
					}
					else{
						BioSimConnector.log.debug("Path score equals max integer");
					}
				}
				displaylist.add("\n");
				// Show the results in the GUI...
				BioSimConnector.resultslist.removeAll();
				BioSimConnector.resultslist.setListData(displaylist.toArray(new String[]{}));
				//... and the command line
				for(String line : displaylist){
					BioSimConnector.log.debug(line);
				}
				
				BioSimConnector.repaintAll();
				if(BioSimConnector.loadingbar.isIndeterminate()){BioSimConnector.loadingbar.setIndeterminate(false);}
				cons++;
				BioSimConnector.loadingbar.setValue(Math.round(99*cons/totalcons));
			}
		}
		// Display no path found message if none found
		if(nopaths && cont){
			BioSimConnector.resultslist.removeAll();
			BioSimConnector.resultslist.setListData(new String[]{"No path found between " + BioSimConnector.sourcebox.getSelectedItem() + " and " + BioSimConnector.targetbox.getSelectedItem()});
		}
		BioSimConnector.loadingbar.setIndeterminate(false);
		BioSimConnector.loadingbar.setValue(0);
		BioSimConnector.repaintAll();
		BioSimConnector.gobutton.setText("Go");
	}
	
	
	public static DijkstrasResult runDijkstrasAlgorithm(Node sourcenode, Node targetnode){
		DijkstrasResult result = new DijkstrasResult();
		result.nodepath = findShortestPath(sourcenode, targetnode);
		if(result.nodepath!=null){
			result.pathscore = computePathScore(result.nodepath);
		}
		return result;
	}
	
	
	public static ArrayList<Node> findShortestPath(Node sourcenode, Node targetnode){
		System.out.println(sourcenode.uri);
		System.out.println(targetnode.uri);
		int numnodes = BioSimConnector.nodearray.length;
		int dist[] = new int[numnodes];
		Node previous[] = new Node[numnodes];
		BioSimConnector.Q = new ArrayList<Node>();

		for(int x=0; x<numnodes; x++){
			BioSimConnector.Q.add(BioSimConnector.nodearray[x]);
			dist[x] = Integer.MAX_VALUE;
		}
		// Find immediate neighbors of source node, initialize arrays
		
		for(int y=BioSimConnector.rowlist.indexOf(sourcenode.name);y<=BioSimConnector.rowlist.lastIndexOf(sourcenode.name); y++){
			if(BioSimConnector.valuelist.get(y)!=0.0){
				dist[BioSimConnector.columnlist.get(y)] = (int)BioSimConnector.valuelist.get(y); 
				previous[BioSimConnector.columnlist.get(y)] = sourcenode;
			}
		}
		dist[sourcenode.name] = 0;
		int m = 0;
		while(m<numnodes-1 && cont){ 
			//System.out.println(Q.size());
			int disttest = Integer.MAX_VALUE;
			Node u = BioSimConnector.nodearray[sourcenode.name];
			Iterator qit = BioSimConnector.Q.iterator();
			while(qit.hasNext() && cont){
				Node node = (Node)qit.next();
				if(dist[node.name]<disttest){
					disttest = dist[node.name];
					u = node;
				}
			}
			BioSimConnector.Q.remove(u);
			
			// This is where the magic happens
			int min = BioSimConnector.rowlist.indexOf(u.name);
			int max = BioSimConnector.rowlist.lastIndexOf(u.name);
			for(int y=min;y<=max;y++){
				int indexofto = BioSimConnector.columnlist.get(y);
				// if path from source to u to (edge.to) is less than source to (edge.to)
				if(BioSimConnector.valuelist.get(y) != 0.0 && dist[u.name] + BioSimConnector.valuelist.get(y) < dist[indexofto]){
					dist[indexofto] = dist[u.name] + (int)BioSimConnector.valuelist.get(y);
					previous[indexofto] = u;
				}
			}
			m++;
		}
		
		ArrayList<Node> path = new ArrayList<Node>(); 
		int x = targetnode.name;
		if(previous[x]!=null && cont){
			path.add(0,targetnode);
			while(previous[x]!=sourcenode){
				path.add(0,previous[x]);
				x = previous[x].name;
			}
			path.add(0,sourcenode);
		}
		return path;
	}
	
	
	public static String findRelationBetweenNodes(Node node1, Node node2){
		for(Statement st : BioSimConnector.bscrdf.getResource(node1.uri).listProperties().toSet()){
			if(st.getObject().toString().equals(node2.uri)){
				return st.getPredicate().getURI().toString();
			}
		}
		return "";
	}
	
	
	public static int computePathScore(ArrayList<Node> path){
		Boolean foundedge = false;
		int pathscore = 0;
		for(int x=0; x<path.size()-1; x++){
			for(int u=BioSimConnector.rowlist.indexOf(path.get(x).name); u<=BioSimConnector.rowlist.lastIndexOf(path.get(x).name); u++){
				if(BioSimConnector.columnlist.get(u)==path.get(x+1).name && BioSimConnector.valuelist.get(u)!=0.0){
					foundedge = true;
					pathscore = pathscore + (int)BioSimConnector.valuelist.get(u);
					break;
				}
			}
			if(!foundedge){return Integer.MAX_VALUE;}
		}
		// If no path was found, set score to max possible value
		if(pathscore == 0){pathscore = Integer.MAX_VALUE;}
		return pathscore;
	}
	
	
	public class ComparatorByScore implements Comparator<Component> {
		// Comparator interface requires defining compare method.
		public int compare(Component comp1, Component comp2) {
			DijkstrasResult dr1 = (DijkstrasResult)comp1;
			DijkstrasResult dr2 = (DijkstrasResult)comp2;
			return dr1.pathscore-dr2.pathscore;
		}
	}
	
}
