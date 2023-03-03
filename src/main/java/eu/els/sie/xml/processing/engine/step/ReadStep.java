package eu.els.sie.xml.processing.engine.step;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.Variable;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;

public class ReadStep extends Step {

	private Variable name;

	public ReadStep() {
		super();
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		// NE pas fermé le scanner/system.in, sinon impossible de relire la console
		// apres

		String s = "";
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		try {
			s = in.nextLine();
		} catch (Throwable e) {
			// NO LINE FOUND ==> equivalent to ENTER so empty string
		}
		name.setValue(new XdmAtomicValue(s));
		params.get(params.size() - 1).put(name.getName(), name);

		return new Result();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		name = new Variable(node.getAttributeValue(Const.NAME_NAME), "", namespaces, info);
	}

}
