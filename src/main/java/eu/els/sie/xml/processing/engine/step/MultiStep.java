package eu.els.sie.xml.processing.engine.step;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.ParamTypeEnum;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

public class MultiStep extends Step {

	protected LinkedList<Step> steps;

	public MultiStep() {
		super();
		steps = new LinkedList<Step>();
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {

		Map<QName, Param> lMap = new HashMap<>();
		params.add(lMap);

		for (Step step : steps) {
			Result result = step.execute(params);
			if (result.getType() != ResultTypeEnum.OK) {
				return result;
			}
			if (step instanceof ParamStep) {
				ParamStep paramStep = (ParamStep) step;
				Param param = paramStep.getParam();
				switch (param.getType()) {
				case option:
				case param:
					if (params.size() >= 2) {
						Map<QName, Param> cMap = params.get(params.size() - 2);
						if (cMap.containsKey(param.getName()) && (param.getType() == ParamTypeEnum.option
								|| param.getType() == ParamTypeEnum.param)) {
							param = cMap.get(param.getName());
						}
					}
					break;
				case variable:
					for (Map<QName, Param> map : params) {
						if (map.containsKey(param.getName())) {
							map.put(param.getName(), param);
						}
					}
					break;
				default:
					break;
				}
				lMap.put(param.getName(), param);
			}
		}

		params.remove(params.size() - 1);

		return new Result();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);

		XdmSequenceIterator<XdmNode> it = node.axisIterator(Axis.NAMESPACE);
		while (it.hasNext()) {
			XdmNode child = it.next();
			if (XdmNodeKind.NAMESPACE.equals(child.getNodeKind())) {
				String prefix = child.getNodeName() == null ? "" : child.getNodeName().getLocalName();
				String uri = child.getStringValue();
				namespaces.put(prefix, uri);
			}
		}

		it = node.axisIterator(Axis.CHILD);
		while (it.hasNext()) {
			XdmNode child = it.next();
			if (XdmNodeKind.ELEMENT.equals(child.getNodeKind())) {
				Step step = null;
				if (child.getNodeName().equals(Const.PARAM_NAME)) {
					step = new ParamStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.OPTION_NAME)) {
					step = new ParamStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.VARIABLE_NAME)) {
					step = new ParamStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.PIPE_NAME)) {
					step = new PipeStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.PROCESSING_NAME)) {
					step = new ProcessingStep();
					((ProcessingStep) step).configUri = owner.configUri;
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.CHOOSE_NAME)) {
					step = new ChooseStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.IF_NAME)) {
					step = new IfStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.READ_NAME)) {
					step = new ReadStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.FOREACH_NAME)) {
					step = new ForEachStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.MESSAGE_NAME)) {
					step = new MessageStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.CALL_NAME)) {
					step = new CallStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.JAVA_NAME)) {
					step = new JavaStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.SCRIPT_NAME)) {
					step = new ScriptStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.TCF_NAME)) {
					step = new TryCatchFinallyStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.EXIT_NAME)) {
					step = new ExitStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.BREAK_NAME)) {
					step = new BreakStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.CONTINUE_NAME)) {
					step = new ContinueStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.WAIT_NAME)) {
					step = new WaitStep();
					step.setParent(this);
					step.load(child, namespaces);
				} else if (child.getNodeName().equals(Const.RETURN_NAME)) {
					step = new ReturnStep();
					step.setParent(this);
					step.load(child, namespaces);
				}
				if (step != null) {
					steps.add(step);
				}
			}
		}
	}

}
