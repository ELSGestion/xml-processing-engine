package eu.els.sie.xml.processing.engine.step;

import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Info;
import eu.els.sie.xml.processing.engine.utils.Variable;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class VariableStep extends ParamStep {

	public VariableStep() {
		super();
	}

	public VariableStep(QName name, String select, Info info) {
		this.info = info;
		param = new Variable(name, select, info);
	}

	public VariableStep(String name, XdmValue value, Info info) {
		this.info = info;
		param = new Variable(name, value, info);
	}

	public VariableStep(QName name, XdmValue value, Info info) {
		this.info = info;
		param = new Variable(name, value, info);
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		String name = node.getAttributeValue(Const.NAME_NAME);
		String select = node.getAttributeValue(Const.SELECT_NAME);
		param = new Variable(name, select, namespaces, info);
	}
}
