package eu.els.sie.xml.processing.engine.step;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Info;
import eu.els.sie.xml.processing.engine.utils.Option;
import eu.els.sie.xml.processing.engine.utils.Param;
import eu.els.sie.xml.processing.engine.utils.Variable;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;

public class CallStep extends Step {

	protected Param uriParam;
	protected Variable name;
	protected ProcessingStep step;
	protected Map<QName, Param> params;
	private Param configUriParam;

	public CallStep() {
		super();
		params = new LinkedHashMap<QName, Param>();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		setInfo(node);
		uriParam = new Param("uri", node.getAttributeValue(Const.URI_NAME), info);

		String lName = node.getAttributeValue(Const.NAME_NAME);
		if (lName != null) {
			name = new Variable(lName, "", namespaces, info);
		}

		XdmSequenceIterator<XdmNode> it = node.axisIterator(Axis.CHILD);

		while (it.hasNext()) {
			XdmNode child = it.next();
			XdmNodeKind kind = child.getNodeKind();
			switch (kind) {
			case ELEMENT:
				if (child.getNodeName().equals(Const.PARAM_NAME)) {
					String name = child.getAttributeValue(Const.NAME_NAME);
					String select = child.getAttributeValue(Const.SELECT_NAME);
					Param param = new Param(name, select,
							new Info(owner.uri, child.getLineNumber(), child.getColumnNumber()));
					params.put(param.getName(), param);
				} else if (child.getNodeName().equals(Const.OPTION_NAME)) {
					String name = child.getAttributeValue(Const.NAME_NAME);
					String select = child.getAttributeValue(Const.SELECT_NAME);
					Option option = new Option(name, select, namespaces,
							new Info(owner.uri, child.getLineNumber(), child.getColumnNumber()));
					params.put(option.getName(), option);
				} else if (child.getNodeName().equals(Const.CONFIG_NAME)) {
					configUriParam = new Param("config-uri", child.getAttributeValue(Const.URI_NAME), info);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {

		String configUri;

		if (configUriParam != null) {
			owner.evaluate(configUriParam, params);

			try {
				configUri = configUriParam.getValue(0).getStringValue();
			} catch (Exception e) {
				throw new ProcessingException("Thue uri gives null !", e, info);
			}

			try {
				configUri = owner.resolve(configUri).getSystemId();
			} catch (Exception e) {
				throw new ProcessingException(
						"Error when solving uri [" + configUri + "] relative to owner.uri [" + owner.uri + "]", e,
						info);
			}

		} else {
			configUri = owner.configUri;
		}

		step = new ProcessingStep();
		step.configUri = configUri;
		step.params = this.params;

		step.messageListenerClass = owner.messageListenerClass;
		step.pipeListenerClass = owner.pipeListenerClass;
		step.xpeListenerClass = owner.xpeListenerClass;

		owner.evaluate(uriParam, params);

		String uri = null;
		try {
			uri = uriParam.getValue(0).getStringValue();
		} catch (Exception e) {
			throw new ProcessingException("The uri gives null !", e, info);
		}

		try {
			uri = owner.resolve(uri).getSystemId();
		} catch (Exception e) {
			throw new ProcessingException("The resolved uri gives null !", e, info);
		}

		step.load(uri, configUri);

		for (Map.Entry<QName, Param> entry : step.params.entrySet()) {
			owner.evaluate(entry.getValue(), params);
		}

		Result result = step.execute(step.params);
		if (name != null) {
			name.setValue(result.getType() == ResultTypeEnum.RETURN ? (XdmValue) result.getObject()
					: XdmEmptySequence.getInstance());
			params.get(params.size() - 1).put(name.getName(), name);
		}

		return new Result();
	}

}
