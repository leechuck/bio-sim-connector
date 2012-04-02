import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;


public class AnnotationAdder extends JDialog implements PropertyChangeListener {
	public JLabel modellabel = new JLabel("Select model");
	public JComboBox modelbox = new JComboBox();
	public JLabel urilabel = new JLabel("Enter uri");
	public JTextField uribox = new JTextField();
	public JLabel labellabel = new JLabel("Enter human readable name (RDF:label)");
	public JTextField labelbox = new JTextField();
	public JOptionPane optionPane;
	public Object[] options;
	
	public String base = BioSimConnector.base;

	
	public AnnotationAdder(){
		setModal(true);
		String[] modelarray = BioSimConnector.modelsandannotationuris.keySet().toArray(new String[]{});
		Arrays.sort(modelarray);
		
		for(String model : modelarray){
			if(!model.equals("")){
				modelbox.addItem(model);
			}	
		}
		modelbox.setSelectedIndex(-1);
		Object[] array = {modellabel, modelbox, urilabel, uribox, labellabel, labelbox};

		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, null);
		optionPane.addPropertyChangeListener(this);
		options = new Object[] { "Apply", "Cancel" };
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);

		setContentPane(optionPane);

		this.pack();
		this.setLocationRelativeTo(null);
		this.setModal(false);
		this.setVisible(true);
	}

	
	public void applyAnnotation(String model, String uri, String label){
		
		String modelspecuri = base + OWLMethods.URIencoding(label) + "_in_" + model;
		
		
		OWLClass anncls = BioSimConnector.factory.getOWLClass(IRI.create(uri));
		OWLClass modspeccls = BioSimConnector.factory.getOWLClass(IRI.create(base + OWLMethods.URIencoding(label) + "_in_" + model));
		OWLMethods.setClsObjectProperty(BioSimConnector.biosimconnectoront, anncls, modspeccls, base + "points-to", base + "points_to", true, BioSimConnector.manager);
		if(!BioSimConnector.biosimconnectoront.getClassesInSignature().contains(anncls)){
			OWLMethods.setRDFLabel(BioSimConnector.biosimconnectoront, anncls, label, BioSimConnector.manager);
		}
		else{
			for(String annofsameref : OWLMethods.getClsValueObjectProperty(BioSimConnector.biosimconnectoront, uri, base + "points-to")){
				OWLMethods.setClsObjectProperty(BioSimConnector.biosimconnectoront, modspeccls, 
						BioSimConnector.factory.getOWLClass(IRI.create(annofsameref)), base + "points-to", base + "points-to", true, BioSimConnector.manager);
				
			}
		}
		
		for(String othermodann : BioSimConnector.modelsandannotationuris.get(model)){
			OWLClass othermodanncls = BioSimConnector.factory.getOWLClass(IRI.create(othermodann));
			OWLMethods.setClsObjectProperty(BioSimConnector.biosimconnectoront, modspeccls, othermodanncls, base + "points-to", base + "points-to", true, BioSimConnector.manager);
		}
		// Need to add new nodes and edges to graph
		addNode(uri,label);
		addNode(modspeccls.getIRI().toString(),"");
		//BioSimConnector.setComboBoxData();
		//BioSimConnector.setEdges();
	}
	
	
	public void addNode(String uri, String label){
		Node thenode = new Node(BioSimConnector.nodearray.length);
		thenode.uri = uri;
		// Only reference uris have labels so far, if label present, update the autocomplete list and the combo box items
		if(!label.equals("")){
			thenode.label = label; 
			String index = label + " (" + BioSimConnector.nsandtlas.get(OWLMethods.getNamespaceFromIRI(uri)) + ")";
			//BioSimConnector.autocompletelist.add(index);
			BioSimConnector.autonamesuris.put(index, uri);
			}
		
		//BioSimConnector.nodes.add(thenode);
		BioSimConnector.urisandnodes.put(uri, thenode);
		//BioSimConnector.nodesanduris.put(thenode, uri);
	}
	

	public void propertyChange(PropertyChangeEvent arg0) {
		System.out.println("prop change");
		String value = optionPane.getValue().toString();
		if (value == "Apply" && modelbox.getSelectedIndex()!=-1 && !uribox.getText().equals("") && !labelbox.getText().equals("")) {
			System.out.println("apply change");
			applyAnnotation((String)modelbox.getSelectedItem(), uribox.getText(), labelbox.getText());
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			setVisible(false);
		}
		else{
			setVisible(false);
		}
	}
}
