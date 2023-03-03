package eu.els.sie.xml.processing.engine.step;

import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

public class ReturnStep extends Step {

	private Param selectParam;

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		selectParam = new Param("select", node.getAttributeValue(Const.SELECT_NAME), info);
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		owner.evaluate(selectParam, params);
		return new Result(ResultTypeEnum.RETURN, selectParam.getValue());
	}

}
