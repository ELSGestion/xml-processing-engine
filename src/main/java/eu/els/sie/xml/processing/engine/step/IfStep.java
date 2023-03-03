package eu.els.sie.xml.processing.engine.step;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class IfStep extends Step {

	private Param testParam;
	private MultiStep step;

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		owner.evaluate(testParam, params);
		XdmValue test = testParam.getValue();
		if (test.size() == 0) {
		} else if (test.itemAt(0).isAtomicValue()) {
			XdmAtomicValue bool = (XdmAtomicValue) test.itemAt(0);
			try {
				if (bool.getBooleanValue()) {
					Map<QName, Param> map = new LinkedHashMap<>();
					params.add(map);
					Result result = step.execute(params);
					if (result.getType() != ResultTypeEnum.OK) {
						return result;
					}
					params.remove(map);
					return new Result();
				}
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}
		} else {
			Map<QName, Param> map = new LinkedHashMap<>();
			params.add(map);
			step.execute(params);
			params.remove(map);
			return new Result();
		}
		return new Result();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);

		testParam = new Param("test", node.getAttributeValue(Const.TEST_NAME), info);

		step = new MultiStep();
		step.setOwner(owner);
		step.load(node,namespaces);

	}

}
