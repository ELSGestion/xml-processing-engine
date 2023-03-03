package eu.els.sie.xml.processing.engine.utils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.exception.XPathEvaluationException;
import eu.els.sie.xml.processing.engine.listener.MessageLevelEnum;
import eu.els.sie.xml.processing.engine.step.ProcessingStep;
import eu.els.sie.xml.saxon.utils.Log4jMessageListenerProxy;
import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.MessageListener2;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class Transform {
	private TransformTypeEnum type;
	private String uri;
	private String initialMode;
	private String initialTemplate;
	private String messageListenerClass;
	private Boolean useWhen;
	private Param uriParam;
	private Param initialModeParam;
	private Param initialTemplateParam;
	private Param useWhenParam;
	private List<Param> params;
	private Info info;
	private XsltExecutable xsltExec;
	private XQueryExecutable xqueryExec;

	public Transform(Info info) {
		super();
		this.info = info;
		messageListenerClass = "com.sitc.api.saxon.utils.Log4jMessageListenerProxy";
		params = new LinkedList<>();
	}

	public void load(XdmNode node, Map<String, String> namespaces) throws ProcessingException {
		uriParam = new Param("uri", node.getAttributeValue(Const.URI_NAME), info);

		type = TransformTypeEnum.valueOf(node.getNodeName().getLocalName());

		String initialModeString = node.getAttributeValue(Const.INITIAL_MODE_NAME);
		String initialTemplateString = node.getAttributeValue(Const.INITIAL_TEMPLATE_NAME);
		String useWhenString = node.getAttributeValue(Const.USE_WHEN_NAME);

		if (initialModeString != null) {
			initialModeParam = new Param("initial-mode", initialModeString, info);
		}

		if (initialTemplateString != null) {
			initialTemplateParam = new Param("initial-template", initialTemplateString, info);
		}

		if (useWhenString != null) {
			useWhenParam = new Param("use-when", useWhenString, info);
		}

		Map<String, String> lNamespaces = new HashMap<String, String>(namespaces);

		XdmSequenceIterator<XdmNode> it = node.axisIterator(Axis.NAMESPACE);
		while (it.hasNext()) {
			XdmNode child = it.next();
			if (XdmNodeKind.NAMESPACE.equals(child.getNodeKind())) {
				String prefix = child.getNodeName() == null ? "" : child.getNodeName().getLocalName();
				String uri = child.getStringValue();
				lNamespaces.put(prefix, uri);
			}
		}

		it = node.axisIterator(Axis.CHILD);

		while (it.hasNext()) {
			XdmNode child = it.next();
			XdmNodeKind kind = child.getNodeKind();
			switch (kind) {
			case ELEMENT:
				Info cInfo = new Info(info.getSystemUri(), child.getLineNumber(), child.getColumnNumber());
				if (child.getNodeName().equals(Const.PARAM_NAME)) {
					Map<String, String> pNamespaces = new HashMap<String, String>(lNamespaces);

					XdmSequenceIterator<XdmNode> cit = child.axisIterator(Axis.NAMESPACE);
					while (cit.hasNext()) {
						XdmNode pchild = cit.next();
						if (XdmNodeKind.NAMESPACE.equals(pchild.getNodeKind())) {
							String prefix = pchild.getNodeName() == null ? "" : pchild.getNodeName().getLocalName();
							String uri = pchild.getStringValue();
							pNamespaces.put(prefix, uri);
						}
					}

					String name = child.getAttributeValue(Const.NAME_NAME);
					String select = child.getAttributeValue(Const.SELECT_NAME);
					Param param = new Param(name, select, pNamespaces, cInfo);
					params.add(param);
				}
				break;
			default:
				break;
			}
		}

	}

	public void evaluate(ProcessingStep ps, List<Map<QName, Param>> params) throws ProcessingException {
		if (params.size() > 0) {
			Param option = params.get(0).get(Const.SAXON_MESSAGE_LISTENER_CLASS_NAME);
			if (option != null) {
				if (option instanceof Option) {
					try {
						messageListenerClass = option.getValue(0).getStringValue();
					} catch (XPathEvaluationException e) {
						throw new ProcessingException(
								"Error evaluating [" + Const.SAXON_MESSAGE_LISTENER_CLASS_NAME.toString() + "] option",
								e);
					}
				}
			}
		}

		ps.evaluate(uriParam, params);
		try {
			uri = uriParam.getValue(0).getStringValue();
		} catch (Exception e) {
			throw new ProcessingException("The uri gives null !", e, info);
		}

		if (initialModeParam != null) {
			ps.evaluate(initialModeParam, params);
			try {
				initialMode = initialModeParam.getValue(0).getStringValue();
			} catch (Exception e) {
				throw new ProcessingException("The initial-mode gives null !", e, info);
			}
		}

		if (initialTemplateParam != null) {
			ps.evaluate(initialTemplateParam, params);
			try {
				initialTemplate = initialTemplateParam.getValue(0).getStringValue();
			} catch (Exception e) {
				throw new ProcessingException("The initial-template gives null !", e, info);
			}
		}

		useWhen = true;
		if (useWhenParam != null) {
			ps.evaluate(useWhenParam, params);
			try {
				XdmAtomicValue bool = (XdmAtomicValue) useWhenParam.getValue(0);
				try {
					useWhen = bool.getBooleanValue();
				} catch (Exception e) {
					throw new ProcessingException(e, info);
				}
			} catch (Exception e) {
				throw new ProcessingException("The use-when gives null !", e, info);
			}
		}

		for (Param param : this.params) {
			ps.evaluate(param, params);
		}
	}

	public void compile(ProcessingStep ps, Processor processor, String baseUri) throws ProcessingException {

		Source source = null;

		switch (type) {
		case xslt:
			XsltCompiler xsltCompiler = processor.newXsltCompiler();
			try {
				ResourceRequest request = new ResourceRequest();
				request.uri = uri;
				request.baseUri = baseUri;
				source = processor.getUnderlyingConfiguration().getResourceResolver().resolve(request);
			} catch (Exception e) {
				throw new ProcessingException("Failed to load " + type.name() + " uri [" + uri + "] !", e, info);
			}

			try {
				xsltExec = xsltCompiler.compile(source);
			} catch (Exception e) {
				throw new ProcessingException("Failed to compile xslt uri [" + uri + "] !", e, info);
			}
			break;
		case xquery:
			XQueryCompiler xqueryCompiler = processor.newXQueryCompiler();
			try {
				ResourceRequest request = new ResourceRequest();
				request.uri = uri;
				request.baseUri = baseUri;
				source = processor.getUnderlyingConfiguration().getResourceResolver().resolve(request);
			} catch (Exception e) {
				throw new ProcessingException("Failed to load " + type.name() + " uri [" + uri + "] !", e, info);
			}
			try {
				xqueryExec = xqueryCompiler.compile(new URL(source.getSystemId()).openStream());
			} catch (Exception e) {
				throw new ProcessingException("Failed to compile xslt uri [" + uri + "] !", e, info);
			}
			break;
		default:
			break;
		}
	}

	public Destination load(ProcessingStep ps, Processor processor, File file) throws ProcessingException {
		switch (type) {
		case xslt:

			XsltTransformer transformer = xsltExec.load();

			try {
				MessageListener2 listener = (MessageListener2) Class.forName(messageListenerClass).newInstance();
				transformer.setMessageListener(listener);
			} catch (Exception e) {
				ps.message(this.getClass().getCanonicalName(),
						"Unable to create an instance of [{}] as MessageListenerClass : default one will be used instead.",
						new Object[] { messageListenerClass }, MessageLevelEnum.warn, e);
				transformer.setMessageListener(new Log4jMessageListenerProxy());
			}

			if (initialMode != null) {
				try {
					transformer.setInitialMode(new QName(initialMode));
				} catch (SaxonApiException e) {
					throw new ProcessingException("Failed to set initial Mode [" + initialMode + "] !", e, info);
				}
			}

			if (initialTemplate != null) {
				transformer.setInitialTemplate(new QName(initialTemplate));
			}

			/***
			 * Seems to be a bug : when activated, the next transforms has no context node
			 */
			// transformer.setTraceListener(listener);

			for (Param param : params) {
				QName name = param.getName();
				XdmValue value = param.getValue();
				transformer.setParameter(name, value);
			}

			transformer.setParameter(new QName(Const.NAMESPACE_URI, "input-file-uri"),
					new XdmAtomicValue(file.toURI().toString()));

			return transformer;
		case xquery:

			XQueryEvaluator evaluator = xqueryExec.load();

			/***
			 * Seems to be a bug : when activated, the next transforms has no context node
			 */

			for (Param param : params) {
				QName name = param.getName();
				XdmValue value = param.getValue();
				evaluator.setExternalVariable(name, value);
			}

			evaluator.setExternalVariable(new QName(Const.NAMESPACE_URI, "input-file-uri"),
					new XdmAtomicValue(file.toURI().toString()));

			return evaluator;
		default:
			break;
		}

		return null;
	}

	public String getUri() {
		return uri;
	}

	public TransformTypeEnum getType() {
		return type;
	}

	public String getInitialMode() {
		return initialMode;
	}

	public String getInitialTemplate() {
		return initialTemplate;
	}

	public Boolean getUseWhen() {
		return useWhen;
	}

	public String getMessageListenerClass() {
		return messageListenerClass;
	}

}
