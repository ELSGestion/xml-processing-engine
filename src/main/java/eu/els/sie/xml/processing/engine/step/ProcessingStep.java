package eu.els.sie.xml.processing.engine.step;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.listener.MessageLevelEnum;
import eu.els.sie.xml.processing.engine.listener.MessageListener;
import eu.els.sie.xml.processing.engine.listener.MessageListenerImpl;
import eu.els.sie.xml.processing.engine.listener.PipeListener;
import eu.els.sie.xml.processing.engine.listener.PipeListenerImpl;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.DirectResourceResolver;
import net.sf.saxon.lib.ResourceRequest;
import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.tree.util.DocumentNumberAllocator;

public class ProcessingStep extends MultiStep implements MessageListener {

	public static final String CONFIG_URI = "cp:/saxon/configuration.xml";
	public static final NamePool NAMEPOOL = new NamePool();
	public static final DocumentNumberAllocator DOCUMENTNUMBERALLOCATOR = new DocumentNumberAllocator();

	protected Map<QName, Param> params;
	protected String uri;

	protected String configUri;
	protected Source configSource;
	protected Configuration configuration;
	protected Processor processor;
	protected Map<String, String> namespaces;

	protected String messageListenerClass;
	protected String xpeListenerClass;
	protected String pipeListenerClass;

	protected String messageListenerClassXpath;
	protected String xpeListenerClassXpath;
	protected String pipeListenerClassXpath;

	protected MessageListener messageListener;
	protected MessageListener xpeListener;
	protected PipeListener pipeListener;

	public ProcessingStep() {
		super();

		namespaces = new HashMap<>();

		messageListenerClass = MessageListenerImpl.class.getName();
		xpeListenerClass = MessageListenerImpl.class.getName();
		pipeListenerClass = PipeListenerImpl.class.getName();

		xpeListener = new MessageListenerImpl();

		try {
			this.configuration = Configuration.newConfiguration();
			processor = new Processor(configuration);
			setConfigUri(CONFIG_URI);
		} catch (ProcessingException e) {
			message(this.getClass().getCanonicalName(),
					"Unable to load conf file uri [{}] : default one will be used instead.",
					new Object[] { CONFIG_URI }, MessageLevelEnum.warn, e);
		}

		this.owner = this;
	}

	public void load(final String absoluteUri) throws ProcessingException {
		load(absoluteUri, null);
	}

	public void load(final String absoluteUri, final String absoluteConfigUri) throws ProcessingException {

		this.uri = absoluteUri;

		try {
			// uri relative par rappprt au dossier courant : exemple : src/main/test.xpe
			uri = resolve(uri, null).getSystemId();
		} catch (Exception e) {
			throw new ProcessingException("Error when loading source with uri [" + uri + "]", e, info);
		}

		checkIsAbsoluteURI(uri);

		if (absoluteConfigUri != null) {
			checkIsAbsoluteURI(absoluteConfigUri);
			setConfigUri(absoluteConfigUri);
		}

		XdmNode configRoot = null;

		Source source = null;
		try {
			source = resolve(this.uri);
		} catch (Exception e) {
			throw new ProcessingException("Error when loading source with uri [" + this.uri + "]", e, info);
		}

		if (source == null) {
			throw new ProcessingException("Error when processing file with uri [" + absoluteUri + "] : uri not found !",
					info);
		}

		this.uri = source.getSystemId();

		checkValidity(this.uri);

		/***
		 * FOR CONFIG
		 */

		try {
			configRoot = processor.newDocumentBuilder().build(source);
		} catch (Exception e) {
			throw new ProcessingException("Error when loading file with uri [" + absoluteUri + "] !", e, info);
		}

		checkValidity(configRoot, this.uri);

		XPathSelector xs;
		try {
			xs = processor.newXPathCompiler().compile("/*/*:config").load();
		} catch (Exception e) {
			throw new ProcessingException("Error when processing file with uri [" + absoluteUri + "] !", e, info);
		}
		try {
			xs.setContextItem(configRoot);
		} catch (Exception e) {
			throw new ProcessingException("Error when processing file with uri [" + absoluteUri + "] !", e, info);
		}
		XdmNode configNode;
		try {
			configNode = (XdmNode) xs.evaluateSingle();
		} catch (Exception e) {
			throw new ProcessingException("Error when processing file with uri [" + absoluteUri + "] !", e, info);
		}

		if (configNode != null) {
			configUri = configNode.getAttributeValue(Const.URI_NAME);
			Param configUriParam = new Param("config-uri", configUri, info);

			evaluate(configUriParam, new ArrayList<>());

			try {
				configUri = configUriParam.getValue(0).getStringValue();
			} catch (Exception e) {
				throw new ProcessingException("Thue uri gives null !", e, info);
			}

			setConfigUri(configUri);
		}

		try {
			configRoot = processor.newDocumentBuilder().build(source);
		} catch (Exception e) {
			throw new ProcessingException("Error when loading file with uri [" + absoluteUri + "] !", e, info);
		}

		try {
			xs = processor.newXPathCompiler().compile("/*").load();
		} catch (Exception e) {
			throw new ProcessingException("Error when processing file with uri [" + absoluteUri + "] !", e, info);
		}

		try {
			xs.setContextItem(configRoot);
		} catch (Exception e) {
			throw new ProcessingException("Error when processing file with uri [" + absoluteUri + "] !", e, info);
		}
		XdmNode node;
		try {
			node = (XdmNode) xs.evaluateSingle();
		} catch (Exception e) {
			throw new ProcessingException("Error when processing file with uri [" + absoluteUri + "] !", e, info);
		}

		XdmSequenceIterator<XdmNode> nsit = node.axisIterator(Axis.NAMESPACE);
		while (nsit.hasNext()) {
			XdmNode lns = nsit.next();
			if (lns.getNodeName() != null) {
				String nsUri = lns.getStringValue();
				String nsPrefix = lns.getNodeName().getLocalName();
				namespaces.put(nsPrefix, nsUri);
			}
		}

		messageListenerClassXpath = node.getAttributeValue(Const.MESSAGE_LISTENER_CLASS_NAME);

		pipeListenerClassXpath = node.getAttributeValue(Const.PIPE_LISTENER_CLASS_NAME);

		xpeListenerClassXpath = node.getAttributeValue(Const.LISTENER_CLASS_NAME);

		super.load(node, namespaces);
	}

	private void checkIsAbsoluteURI(final String uri) throws ProcessingException {
		URI lURI;
		try {
			lURI = URI.create(uri);
		} catch (Exception e) {
			throw new ProcessingException("[" + uri + "] : uri not valid !", e, info);
		}
		if (!lURI.isAbsolute()) {
			throw new ProcessingException("[" + uri + "] : uri not absolute !", info);
		}
	}

	private void setConfigUri(String configUri) throws ProcessingException {
		try {
			configSource = resolve(configUri);
		} catch (Exception e) {
			throw new ProcessingException(e, info);
		}

		this.configUri = configSource.getSystemId();
		checkIsAbsoluteURI(this.configUri);

		try {
			configuration = Configuration.readConfiguration(configSource);
			configuration.setNamePool(NAMEPOOL);
			configuration.setDocumentNumberAllocator(DOCUMENTNUMBERALLOCATOR);
		} catch (Exception e) {
			throw new ProcessingException(e, info);
		}

		this.processor = new Processor(configuration);
	}

	private void checkValidity(XdmNode configRoot, String uri) throws ProcessingException {
		try {
			XPathSelector xs = processor.newXPathCompiler().compile("//*:continue[empty(ancestor::*:foreach)]").load();
			xs.setContextItem(configRoot);
			XdmNode continueNode = (XdmNode) xs.evaluateSingle();
			if (continueNode != null) {
				throw new ProcessingException("Error when processing file with uri [" + uri
						+ "] ! Le step continue must be within a foreach step", info);
			}
		} catch (Exception e) {
			throw new ProcessingException("Error when processing file with uri [" + uri + "] !", e, info);
		}
	}

	private void checkValidity(final String uri) throws ProcessingException {
		try {

			SchemaFactory schemaFactory = null;
			try {
				Class<?> schemaFactoryClass = Class.forName("com.saxonica.ee.jaxp.SchemaFactoryImpl");
				schemaFactory = (SchemaFactory) schemaFactoryClass.newInstance();
			} catch (Exception e) {
				schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
			}

			String schemaURI = "cp:/grammars/xpe-conf.xsd";

			Source schemaSource = null;
			try {
				schemaSource = resolve(schemaURI, null);
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}

			if (schemaSource == null) {
				URL schemaURL = new URL(schemaURI);
				message(this.getClass().toString(), "Checking configuration validity against {}",
						new Object[] { schemaURL.toExternalForm() }, MessageLevelEnum.debug, null);
				schemaSource = new StreamSource(schemaURL.openStream());
			} else {
				message(this.getClass().toString(), "Checking configuration validity against {}",
						new Object[] { schemaSource.getSystemId() }, MessageLevelEnum.debug, null);
			}

			schemaSource.setSystemId(schemaURI);
			Schema schema = schemaFactory.newSchema(schemaSource);
			SchemaValidationErrorListener errListener = new SchemaValidationErrorListener();
			Validator validator = schema.newValidator();
			validator.setErrorHandler(errListener);
			SAXSource saxSource = new SAXSource(new InputSource(uri));
			validator.validate(saxSource);
			if (errListener.hasErrors()) {
				throw new ProcessingException(schemaURI + " does not respect configuration schema", info);
			}
		} catch (Exception e) {
			throw new ProcessingException("Error when processing file with uri [" + uri + "] !", e, info);
		}
	}

	private class SchemaValidationErrorListener implements ErrorHandler {
		private boolean errors = false;

		public boolean hasErrors() {
			return errors;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			message(this.getClass().toString(), "validating configFile {}", new Object[] { exception.getMessage() },
					MessageLevelEnum.warn, null);
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			message(this.getClass().toString(), "validating configFile {}", new Object[] { exception.getMessage() },
					MessageLevelEnum.error, null);
			errors = true;
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			message(this.getClass().toString(), "validating configFile {}", new Object[] { exception.getMessage() },
					MessageLevelEnum.fatalError, null);
		}
	}

	public Result execute(Map<QName, Param> params) throws ProcessingException {
		this.params = params;

		List<Map<QName, Param>> nParams = new LinkedList<>(Arrays.asList(params));

		if (messageListenerClassXpath != null) {
			Param messageListenerClassParam = new Param(Const.MESSAGE_LISTENER_CLASS_NAME, messageListenerClassXpath,
					info);
			evaluate(messageListenerClassParam, nParams);
			try {
				messageListenerClass = messageListenerClassParam.getValue(0).getStringValue();
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}
		}

		try {
			messageListener = (MessageListener) Class.forName(messageListenerClass).newInstance();
		} catch (Exception e) {
			message(this.getClass().getCanonicalName(),
					"Unable to create an instance of [{}] as MessageListener : default one will be used instead.",
					new Object[] { messageListenerClass }, MessageLevelEnum.warn, e);
		}

		if (pipeListenerClassXpath != null) {
			Param pipeListenerClassParam = new Param(Const.PIPE_LISTENER_CLASS_NAME, pipeListenerClassXpath, info);
			evaluate(pipeListenerClassParam, nParams);
			try {
				pipeListenerClass = pipeListenerClassParam.getValue(0).getStringValue();
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}
		}

		try {
			pipeListener = (PipeListener) Class.forName(pipeListenerClass).newInstance();
		} catch (Exception e) {
			message(this.getClass().getCanonicalName(),
					"Unable to create an instance of [{}] as PipeListener : default one will be used instead.",
					new Object[] { pipeListenerClass }, MessageLevelEnum.warn, e);
		}

		if (xpeListenerClassXpath != null) {
			Param xpeListenerClassParam = new Param(Const.LISTENER_CLASS_NAME.getLocalName(), xpeListenerClassXpath,
					info);
			evaluate(xpeListenerClassParam, nParams);
			try {
				xpeListenerClass = xpeListenerClassParam.getValue(0).getStringValue();
			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}
		}

		try {
			xpeListener = (MessageListener) Class.forName(xpeListenerClass).newInstance();
		} catch (Exception e) {
			message(this.getClass().getCanonicalName(),
					"Unable to create an instance of [{}] as xpeListener : default one will be used instead.",
					new Object[] { xpeListenerClass }, MessageLevelEnum.warn, e);
		}

		message(this.getClass().toString(), "Start processing [{}] with the following params [{}].",
				new Object[] { uri, params.values().toString() }, MessageLevelEnum.debug, null);

		return super.execute(nParams);

	}

	public Source resolve(String uri, String base) throws Exception {
		ResourceRequest request = new ResourceRequest();
		request.baseUri = base == null ? new File(System.getProperty("user.dir")).toURI().toString() : base;
		request.uri = new URI(request.baseUri).resolve(uri).toString();
		return request.resolve(new ResourceResolver[] { processor.getUnderlyingConfiguration().getResourceResolver(),
				(ResourceResolver) new DirectResourceResolver(processor.getUnderlyingConfiguration()) });
	}

	public Source resolve(String uri) throws Exception {
		ResourceRequest request = new ResourceRequest();
		request.baseUri = this.uri;
		request.uri = new URI(request.baseUri).resolve(uri).toString();
		return request.resolve(new ResourceResolver[] { processor.getUnderlyingConfiguration().getResourceResolver(),
				(ResourceResolver) new DirectResourceResolver(processor.getUnderlyingConfiguration()) });
	}

	public void evaluate(Param param, List<Map<QName, Param>> params) throws ProcessingException {
		if (param.getXpath() != null) {
			XdmValue result = null;
			XPathCompiler comp = processor.newXPathCompiler();
			try {
				comp.setBaseURI(new URI(uri));
			} catch (Exception e1) {
				/***
				 * unrecheable
				 */
			}
			for (Map.Entry<String, String> entry : namespaces.entrySet()) {
				if (!entry.getKey().isEmpty()) {
					comp.declareNamespace(entry.getKey(), entry.getValue());
				}
			}
			for (Map<QName, Param> map : params) {
				for (Map.Entry<QName, Param> entry : map.entrySet()) {
					comp.declareVariable(entry.getKey());
				}
			}
			XPathExecutable xe;
			try {
				xe = comp.compile(param.getXpath());
			} catch (Exception e) {
				throw new ProcessingException("Unable to compile xpath [" + param.getXpath() + "]", e, param.getInfo());
			}

			XPathSelector xs = xe.load();

			for (Map<QName, Param> map : params) {
				for (Map.Entry<QName, Param> entry : map.entrySet()) {
					try {
						xs.setVariable(entry.getKey(), entry.getValue().getValue());
					} catch (Exception e) {
						throw new ProcessingException(e, param.getInfo());
					}
				}
			}
			try {
				result = xs.evaluate();
			} catch (Exception e) {
				throw new ProcessingException("Unable to evaluate xpath [" + param.getXpath() + "]", e,
						param.getInfo());
			}
			if (result == null) {
				throw new ProcessingException(
						"Unable to evaluate xpath = [" + param.getXpath() + "] : evaluation gives null !",
						param.getInfo());
			}
			param.setValue(result);
		}
	}

	@Override
	public void message(String source, String message, Object[] params, MessageLevelEnum level, Exception e) {
		xpeListener.message(source, message, params, level, e);
	}

}
