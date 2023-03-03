package eu.els.sie.xml.processing.engine.step;

import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

public class BreakStep extends Step {

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		return new Result();
	}

}
