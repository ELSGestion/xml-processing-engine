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

public class WaitStep extends Step {

	private Param periodParam;

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		if (node.getAttributeValue(Const.PERIOD_NAME) != null) {
			periodParam = new Param("period", node.getAttributeValue(Const.PERIOD_NAME), info);
		}
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		if (periodParam != null) {
			owner.evaluate(periodParam, params);
			XdmValue periodValue = periodParam.getValue();
			if (periodValue.size() == 0) {
				
			} else if (periodValue.itemAt(0).isAtomicValue()) {
				XdmAtomicValue periodAtomicValue = (XdmAtomicValue) periodValue.itemAt(0);
				try {
					Thread.sleep(periodAtomicValue.getLongValue());
				} catch (Exception e) {
					throw new ProcessingException(e, info);
				}
			} else {
				throw new ProcessingException("need to be integer", info);
			}
		}

		return new Result();
	}

}
