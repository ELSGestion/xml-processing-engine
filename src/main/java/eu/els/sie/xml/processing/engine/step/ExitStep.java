package eu.els.sie.xml.processing.engine.step;

import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class ExitStep extends Step {

	private Param codeParam;

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		if (node.getAttributeValue(Const.CODE_NAME) != null) {
			codeParam = new Param("code", node.getAttributeValue(Const.CODE_NAME), info);
		}
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		if (codeParam == null) {
			System.exit(0);
		}
		owner.evaluate(codeParam, params);
		XdmValue codeValue = codeParam.getValue();
		if (codeValue.size() == 0) {
			System.exit(0);
		} else if (codeValue.itemAt(0).isAtomicValue()) {
			XdmAtomicValue codeAtomicValue = (XdmAtomicValue) codeValue.itemAt(0);
			try {
				System.exit((int) codeAtomicValue.getLongValue());
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}
		} else {
			throw new ProcessingException("need to be integer", info);
		}

		return new Result(ResultTypeEnum.EXIT);
	}

}
