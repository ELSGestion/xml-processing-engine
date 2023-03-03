package eu.els.sie.xml.processing.engine.step;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.Variable;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class TryCatchFinallyStep extends Step {

	private MultiStep tryStep;
	private MultiStep catchStep;
	private MultiStep finallyStep;
	private Variable exceptionVariable;

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {
		try {
			return tryStep.execute(params);
		} catch (Exception e) {

			try {

				Gson gson = new Gson();
				String json = gson.toJson(e);

				Param jsonTOXML = new Param("json-to-xml", "json-to-xml($json)");
				List<Map<QName, Param>> jsonTOXMLparams = new ArrayList<Map<QName, Param>>();
				jsonTOXMLparams.add(new LinkedHashMap<QName, Param>());
				jsonTOXMLparams.get(0).put(new QName("json"), new Param("json", new XdmAtomicValue(json)));

				owner.evaluate(jsonTOXML, jsonTOXMLparams);

				VariableStep exceptionStep = new VariableStep(exceptionVariable.getName(), jsonTOXML.getValue(), info);
				exceptionStep.owner = this.owner;
				catchStep.steps.add(0, exceptionStep);

			} catch (Exception e1) {
				throw new ProcessingException(e1, info);
			}

			return catchStep.execute(params);

		} finally {
			if (finallyStep != null) {
				return finallyStep.execute(params);
			}
		}
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);

		XdmSequenceIterator<XdmNode> it = node.axisIterator(Axis.CHILD);
		while (it.hasNext()) {
			XdmNode child = it.next();
			XdmNodeKind kind = child.getNodeKind();
			switch (kind) {
			case ELEMENT:
				MultiStep multiStep = new MultiStep();
				multiStep.setOwner(owner);
				multiStep.load(child, namespaces);
				if (child.getNodeName().equals(Const.TRY_NAME)) {
					tryStep = multiStep;
				} else if (child.getNodeName().equals(Const.CATCH_NAME)) {
					catchStep = multiStep;
					String exceptionVariableName = child.getAttributeValue(Const.EXCEPTION_VARIABLE_NAME_NAME);
					if (exceptionVariableName == null) {
						exceptionVariableName = "e";
					}
					exceptionVariable = new Variable(exceptionVariableName, "", namespaces, info);
				} else {
					finallyStep = multiStep;
				}
				break;
			default:
				break;
			}
		}
	}

}
