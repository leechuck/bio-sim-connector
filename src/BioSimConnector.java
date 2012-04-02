import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;


public class BioSimConnector extends JFrame implements ActionListener{
/**
	 * 
	 */
	private static final long serialVersionUID = -8087296771656256991L;
//		public static File biosimconnectorfile = new File("./resources/BSCsmall.rdf");
	public static BioSimConnector bsc;
	public static File biosimconnectorfile = new File("./resources/BSC.rdf");
	public static Model bscrdf;
	public static Property lowweight;
	public static Property normweight;
	public static Property fiveweight;
	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory factory;
	public static OWLOntology biosimconnectoront;
	public static List<String> autocompleteurilist = new ArrayList<String>();
	public static Hashtable<String,String> autonamesuris = new Hashtable<String,String>();
	public static Hashtable<String,Node> urisandnodes = new Hashtable<String,Node>(); 
	public static String base = "http://sbp.bhi.washington.edu/BioSimConnector#";
	public static Set<String> stopuris = new HashSet<String>();
	public static Hashtable<String,Set<String>> zeroweightconnections = new Hashtable<String,Set<String>>();
	public static Hashtable<String,String[]> nsandtlas = new Hashtable<String,String[]>();

	// UI stuff
	public static JMenuBar menubar = new JMenuBar();
	public static JMenu filemenu = new JMenu("File");
	public static JMenuItem finditem = new JMenuItem("Find path");
	public static JMenuItem filemenuaddann = new JMenuItem("Add annotation to knowledge base");
	
	public static JPanel mainpanel = new JPanel();
	public static JPanel toppanel = new JPanel();
	public static JPanel bottompanel = new JPanel();
	public static JComboBox sourcebox = new JComboBox(new String[]{});
	public static JComboBox targetbox = new JComboBox(new String[]{});
	public static JButton gobutton = new JButton("Go");
	public static JPanel scrollpanepanel = new JPanel();
	public static JTextArea ta = new JTextArea();
	public static JList resultslist = new JList();
	public static JScrollPane scrollpane = new JScrollPane(resultslist);
	public static JProgressBar loadingbar = new JProgressBar();
	public static ComboKeyHandler sourceckh;
	public static ComboKeyHandler targetckh;

	public static int initwidth = 720;
	public static int initheight = 700;
	public static int baseweight = 10;
	public static int lowweightnum = 1;
	public int maskkey;
	public static GenericThread gt;
	public static PathFinderThread tt;
	public static Node[] nodearray;
//	public static Edge[] edgearray;
	public static int[][] edgearray;
    //public static SparseDoubleMatrix2D Weight;
    public static IntArrayList rowlist = new IntArrayList();
    public static IntArrayList columnlist = new IntArrayList();
    public static DoubleArrayList valuelist = new DoubleArrayList();
    public static ArrayList<Node> Q;
    public static int[] dist;
    public static ArrayList<ArrayList<int[]>> nodeindexesandcons;
    public static int[][][] nodeindexesandconsint;
    
    public static Hashtable<String,Integer> relationsandweights = new Hashtable<String,Integer>();

	public static CommandLineLog log = new CommandLineLog();
	public static Hashtable<String,Set<String>> modelsandannotationuris = new Hashtable<String,Set<String>>();
	public Boolean MACOSX;
	public static Hashtable<String, String[]> stopURIsAndLabels;

	
	public BioSimConnector() throws OWLOntologyCreationException, FileNotFoundException{
		super("OSXAdapter");
		
		stopURIsAndLabels = ResourcesManager.createHashtableFromFile("resources/stopURIs");
		
		// Prefer to stay in BioModels?
		relationsandweights.put(base + "adjacent-reaction", 10);
		relationsandweights.put(base + "from-same-model-as", 10);
		relationsandweights.put(base + "same-GO-leaf-reaction-as", 1);
		relationsandweights.put(base + "annotation-for", 10); // why when change this number, no paths can be found for anything?
		relationsandweights.put(base + "refers-to", 10);
		relationsandweights.put(base + "participates-in-same-reaction-as", 1);
		relationsandweights.put(base + "same-annotation-as", 10);
		relationsandweights.put(base + "reactant-for", 1);
		relationsandweights.put(base + "product-of", 1);
		relationsandweights.put(base + "has-reactant", 1);
		relationsandweights.put(base + "has-product", 1);
		
//		fiveweight = ResourceFactory.createProperty(base + "points-to-weight-5");
//		lowweight = ResourceFactory.createProperty(base + "points-to-low-weight");
//		normweight = ResourceFactory.createProperty(base + "points-to");
		
		loadingbar.setVisible(true);
		loadingbar.setPreferredSize(new Dimension(300,30));
		loadingbar.setMaximumSize(new Dimension(999999,30));
		
		gt = new GenericThread(this, "LoadKB");
		maskkey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		MACOSX = System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
		
		setTitle("BioSimConnector");
		factory = manager.getOWLDataFactory();
		
		if (MACOSX) {
			try {
				OSXAdapter.setQuitHandler(this,getClass().getDeclaredMethod("quit", (Class[]) null));
			} catch (Exception e) {
				System.err.println("Error while loading the OSXAdapter:");
				e.printStackTrace();
			}
		}
			
		
		menubar.setOpaque(true);
		menubar.setPreferredSize(new Dimension(initwidth, 20));
		finditem.addActionListener(this);
		finditem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,maskkey));

		filemenuaddann.setEnabled(false);
		filemenu.add(filemenuaddann);
		filemenu.add(finditem);
		menubar.add(filemenu);
		this.setJMenuBar(menubar);
		filemenuaddann.addActionListener(this);
		
		nsandtlas = ResourcesManager.createHashtableFromFile("resources/namespacesAndAcronyms");
		
		//Create string set of stop uris
		stopuris.addAll(stopURIsAndLabels.keySet());
				
		scrollpane.setPreferredSize(new Dimension(480,550));
		scrollpane.getVerticalScrollBar().setUnitIncrement(9);
		scrollpane.setBackground(Color.white);
		
		setBackground(new Color(202,225,255)); // setBackground(new Color(207, 215, 252));
		setPreferredSize(new Dimension(initwidth, initheight));
		
		gobutton.addActionListener(this);
		
		sourcebox.setEditable(true);
		targetbox.setEditable(true);
		sourcebox.setPreferredSize(new Dimension(300,30));
		sourcebox.setForeground(Color.blue);
		targetbox.setPreferredSize(new Dimension(300,30));
		targetbox.setForeground(Color.blue);
		
		sourcebox.setEnabled(false);
		targetbox.setEnabled(false);
		gobutton.setEnabled(false);
		
		setLayout(new BorderLayout());
		toppanel.add(sourcebox);
		toppanel.add(targetbox);
		toppanel.add(gobutton);
		
		add(toppanel, BorderLayout.NORTH);
		
		ta.setPreferredSize(new Dimension(450,420));
		ta.setEditable(false);
		bottompanel.setLayout(new BorderLayout());
		bottompanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		bottompanel.add(scrollpane, BorderLayout.NORTH);
		bottompanel.add(loadingbar, BorderLayout.SOUTH);
		
		add(bottompanel, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(null);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					quit();
				} catch (OWLOntologyStorageException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		System.out.println("Loading...");
		loadingbar.setIndeterminate(true);
		gt.start();
	}
	
	// MAIN method
	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException {
		log.debug("total: " + Runtime.getRuntime().totalMemory());
		log.debug("free: " + Runtime.getRuntime().freeMemory());
		bsc = new BioSimConnector(); 
	}
	
	// Load the knowledge base, remove stop words, create the sparse matrix that we will be finding paths through
	public static void LoadKB() throws OWLOntologyCreationException, IOException{
		System.out.println("Check 1: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		ArrayList<Node> nodes = new ArrayList<Node>();
		Hashtable<String,String> urisandlabels = new Hashtable<String,String>();

		bscrdf = ModelFactory.createDefaultModel();
		int n = 0;
		InputStream in = FileManager.get().open(biosimconnectorfile.getAbsolutePath());
		System.out.println("Reading in knowledge base...");
		bscrdf.read(in,null);
		System.out.println("...Finished.");
		in.close();
		
		// Remove stop terms
		Set<String> allstopuris = new HashSet<String>();
		allstopuris.addAll(stopuris);
		
		for(String stopuri : stopuris){
			Resource stopres = bscrdf.getResource(stopuri);
			
			for(Statement subst : stopres.listProperties(normweight).toSet()){
				allstopuris.add(subst.getObject().toString());
			}
		}
		log.debug("Removing...");
		for(Statement st : bscrdf.listStatements().toSet()){
			if(allstopuris.contains(st.getSubject().toString()) || 
					allstopuris.contains(st.getObject().toString())){
				bscrdf.remove(st);
			}
		}
		log.debug("...done.");
		System.out.println("Check 2: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

		Set<Resource> labelednodes = bscrdf.listSubjectsWithProperty(RDFS.label).toSet();
		ResIterator allit = bscrdf.listSubjects();
		ArrayList<String> autocompletelist = new ArrayList<String>();
		while(allit.hasNext()){
			
			Resource rdfnode = allit.next();
			String uri = rdfnode.getURI();
			//System.out.println(uri);
			if(uri.startsWith("http://purl.org/obo/owl/CHEBI#") || uri.startsWith("http://purl.org/obo/owl/GO#") ||
					uri.startsWith("http://sig.uw.edu/fma#") || uri.startsWith("http://www.reactome.org/biopax")
					|| uri.startsWith("http://purl.uniprot.org/uniprot#")){
				
				String index = "";
				
				// If the resource has an RDFS:label
				if(labelednodes.contains(rdfnode.asResource())){
					String rdflabel = rdfnode.asResource().getProperty(RDFS.label).getLiteral().toString();
					index = rdflabel + " (" + nsandtlas.get(OWLMethods.getNamespaceFromIRI(uri))[0] + ")";
					urisandlabels.put(uri, rdflabel);
					// Associate instances of this reference resources with RDFS:labels
					for(Statement st : rdfnode.listProperties(normweight).toSet()){
						urisandlabels.put(st.getObject().toString(), rdflabel);
					}
				}
				else{
					index = OWLMethods.getOWLEntityNameFromIRI(uri) + " (" + nsandtlas.get(OWLMethods.getNamespaceFromIRI(uri))[0] + ")";
				}
				autocompletelist.add(index);
				autonamesuris.put(index, uri);
			}
			Node node = new Node(n);
			node.uri = uri;
			nodes.add(node);
			urisandnodes.put(uri, node);
			n++;
		}
		
		for(Node node : nodes){
			if(urisandlabels.keySet().contains(node.uri)){
				node.label = urisandlabels.get(node.uri);
			}
		}
		
		System.out.println("Check 3: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

		nodearray = nodes.toArray(new Node[]{});
		
		// SET THE EDGES
		SparseDoubleMatrix2D Weight = new SparseDoubleMatrix2D(nodearray.length,nodearray.length);
		for(Resource res : bscrdf.listSubjects().toSet()){
			for(Statement st : res.listProperties().toSet()){
				if(relationsandweights.keySet().contains(st.getPredicate().getURI())){
					Weight.set(urisandnodes.get(res.getURI()).name, urisandnodes.get(st.getObject().toString()).name, relationsandweights.get(st.getPredicate().getURI()));
				}
			}
			//	Weight.set(urisandnodes.get(res.getURI()).name, urisandnodes.get(st.getObject().toString()).name, baseweight);
			//}
//			for(Statement st : res.listProperties(lowweight).toSet()){
//				Weight.set(urisandnodes.get(res.getURI()).name, urisandnodes.get(st.getObject().toString()).name, lowweightnum);
//				Weight.set(urisandnodes.get(st.getObject().toString()).name, urisandnodes.get(res.getURI()).name, lowweightnum);
//			}
//			for(Statement st : res.listProperties(fiveweight).toSet()){
//				Weight.set(urisandnodes.get(res.getURI()).name, urisandnodes.get(st.getObject().toString()).name, 5);
//				Weight.set(urisandnodes.get(st.getObject().toString()).name, urisandnodes.get(res.getURI()).name, 5);
//			}
		}
		
		Weight.getNonZeros(rowlist, columnlist, valuelist);
		
		System.out.println(nodearray.length);
		System.out.println(rowlist.size());
		setComboBoxData(autocompletelist);
		
		loadingbar.setIndeterminate(false);
		loadingbar.setValue(0);
		//bscrdf = null;
		nodes = null;
		autocompletelist = null;
		Runtime.getRuntime().gc();
		System.out.println("Loaded.");
		
		gobutton.setEnabled(true);
		sourcebox.setEnabled(true);
		targetbox.setEnabled(true);
		
		sourcebox.setSelectedIndex(-1);
		targetbox.setSelectedIndex(-1);
		gt.stop();
	}
	
	
	
	// Set data for autocomplete
	public static void setComboBoxData(ArrayList<String> autocompletelist){
		String sourceboxselection = (String)sourcebox.getSelectedItem();
		String targetboxselection = (String)targetbox.getSelectedItem();
		sourcebox.removeAllItems();
		targetbox.removeAllItems();
		String[] autocompletearray = (String[]) autocompletelist.toArray(new String[]{});
		Arrays.sort(autocompletearray);
		for(int x=0; x<autocompletearray.length; x++){
			String item = autocompletearray[x];
			sourcebox.addItem(item);
			targetbox.addItem(item);
		}
		sourcebox.setSelectedItem(sourceboxselection);
		targetbox.setSelectedItem(targetboxselection);
		sourceckh = new ComboKeyHandler(sourcebox);
		targetckh = new ComboKeyHandler(targetbox);
	}
	
// Actions
	public void actionPerformed(ActionEvent arg0) {
		// Go pressed
		Object o = arg0.getSource();
		if((o == gobutton || o == finditem) && gobutton.getText().equals("Go") 
				&& sourcebox.getSelectedItem()!=null && targetbox.getSelectedItem()!=null){
			resultslist.setListData(new String[]{});
			repaintAll();
			tt = new PathFinderThread();
			tt.start();
			loadingbar.setValue(0);
			loadingbar.setIndeterminate(true);
			loadingbar.setVisible(true);
		}
		// Stop pressed
		else if(o == gobutton && gobutton.getText().equals("Stop")){
			tt.stop();
			loadingbar.setValue(0);
			System.out.println("Search aborted");
		}
		
		if(o == filemenuaddann){
			//AnnotationAdder aa = new AnnotationAdder();
		}
		
	}
	
	
	public void showIndeterminateProgbar(){}
	
	public void quit() throws OWLOntologyStorageException{}
	
	
	public static String getModelNameFromURI(String uri){
		if(uri.contains("BIOMD")){
			return uri.substring(uri.indexOf("BIOMD"), uri.length());
		}
		else if(uri.contains("_from_")){
			return uri.substring(uri.lastIndexOf("_from_") + 6,uri.length());
		}
		else return OWLMethods.getOWLEntityNameFromIRI(uri);
			
	}
	
	
	
	public static void repaintAll(){
		bsc.update(bsc.getGraphics());
		scrollpane.repaint();
		scrollpane.validate();
		bottompanel.repaint();
		bottompanel.validate();
		resultslist.update(resultslist.getGraphics());
	}
}
