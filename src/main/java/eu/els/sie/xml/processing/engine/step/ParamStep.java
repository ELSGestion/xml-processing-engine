package eu.els.sie.xml.processing.engine.step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Option;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.Variable;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class ParamStep extends Step {

	protected Param param;

	public ParamStep() {
		super();
	}

	public ParamStep(Param param) {
		this.param = param;
	}

	public Param getParam() {
		return param;
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		owner.evaluate(param, params);
		return new Result();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);

		Map<String, String> pNamespaces = new HashMap<String, String>(namespaces);

		XdmSequenceIterator<XdmNode> it = node.axisIterator(Axis.NAMESPACE);
		while (it.hasNext()) {
			XdmNode child = it.next();
			if (XdmNodeKind.NAMESPACE.equals(child.getNodeKind())) {
				String prefix = child.getNodeName() == null ? "" : child.getNodeName().getLocalName();
				String uri = child.getStringValue();
				pNamespaces.put(prefix, uri);
			}
		}

		String name = node.getAttributeValue(Const.NAME_NAME);
		String select = node.getAttributeValue(Const.SELECT_NAME);

		if (node.getNodeName().equals(Const.PARAM_NAME)) {
			param = new Param(name, select, pNamespaces, info);
		} else if (node.getNodeName().equals(Const.OPTION_NAME)) {
			param = new Option(name, select, pNamespaces, info);
		} else if (node.getNodeName().equals(Const.VARIABLE_NAME)) {
			param = new Variable(name, select, pNamespaces, info);
		}

	}

}
