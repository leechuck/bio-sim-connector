import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;


public class RDFMethods {
	public static Model removeResource(Resource res, Model rdfmodel){
		for(Statement st : rdfmodel.listStatements().toSet()){
			if(st.getSubject().toString()==res.getURI() || st.getObject().toString()==res.getURI()){
				rdfmodel.remove(st);
			}
		}
		return rdfmodel;
	}
}
