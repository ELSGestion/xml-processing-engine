package eu.els.sie.xml.processing.engine.step;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Info;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

public class ChooseStep extends Step {

	private List<Param> tests;
	private List<MultiStep> steps;

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		for (int i = 0; i < tests.size(); i++) {
			Param param = tests.get(i);
			owner.evaluate(param, params);
			XdmValue test = param.getValue();
			if (test.size() == 0) {
			} else if (test.itemAt(0).isAtomicValue()) {
				XdmAtomicValue bool = (XdmAtomicValue) test.itemAt(0);
				try {
					if (bool.getBooleanValue()) {
						Map<QName, Param> map = new LinkedHashMap<>();
						params.add(map);
						Result result = steps.get(i).execute(params);
						if (result.getType()!=ResultTypeEnum.OK) {
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
				steps.get(i).execute(params);
				params.remove(map);
				return new Result();
			}
		}
		return new Result();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		tests = new LinkedList<>();
		steps = new LinkedList<>();

		XdmSequenceIterator<XdmNode> it = node.axisIterator(Axis.CHILD);
		int i = 0;
		while (it.hasNext()) {
			XdmNode child = it.next();
			XdmNodeKind kind = child.getNodeKind();
			switch (kind) {
			case ELEMENT:
				if (child.getNodeName().equals(Const.WHEN_NAME)) {
					String test = child.getAttributeValue(Const.TEST_NAME);
					tests.add(new Param(String.valueOf(i), test,
							new Info(owner.uri, child.getLineNumber(), child.getColumnNumber())));
				} else if (child.getNodeName().equals(Const.OTHERWISE_NAME)) {
					tests.add(new Param(String.valueOf(i), "true()",
							new Info(owner.uri, child.getLineNumber(), child.getColumnNumber())));
				}

				MultiStep multiStep = new MultiStep();
				multiStep.setOwner(owner);
				multiStep.load(child,namespaces);
				steps.add(multiStep);

				break;
			default:
				break;
			}
			i++;
		}
	}

}
