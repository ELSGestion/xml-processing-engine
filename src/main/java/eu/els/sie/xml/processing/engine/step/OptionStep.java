package eu.els.sie.xml.processing.engine.step;

import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Option;
import net.sf.saxon.s9api.XdmNode;

public class OptionStep extends ParamStep {

	public OptionStep() {
		super();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		String name = node.getAttributeValue(Const.NAME_NAME);
		String select = node.getAttributeValue(Const.SELECT_NAME);
		param = new Option(name,select,namespaces,info);
	}

}
