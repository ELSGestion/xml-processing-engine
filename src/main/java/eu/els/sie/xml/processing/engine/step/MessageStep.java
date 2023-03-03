package eu.els.sie.xml.processing.engine.step;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.listener.MessageLevelEnum;
import eu.els.sie.xml.processing.engine.listener.MessageListener;
import eu.els.sie.xml.processing.engine.listener.MessageListenerImpl;
import eu.els.sie.xml.processing.engine.utils.Const;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

public class MessageStep extends Step {

	private String select;
	private String param;
	private String source;
	private String throwable;
	private String listenerClass;
	private MessageLevelEnum level;

	private MessageListener listener;

	public MessageStep() {
		super();
		listener = new MessageListenerImpl();
	}

	@Override
	public Result execute(List<Map<QName, Param>> params) throws ProcessingException {

		listener = owner.messageListener;

		Param selectParam = new Param(new QName("select"), select, info);
		owner.evaluate(selectParam, params);

		Param sourceParam = new Param(new QName("source"), source, info);
		owner.evaluate(sourceParam, params);

		Exception messageException = null;

		if (throwable != null) {
			Param throwableParam = new Param("", throwable, info);
			owner.evaluate(throwableParam, params);

			try {

				Gson gson = new Gson();

				Param xmlToJSON = new Param("json-to-xml", "xml-to-json($xml)");
				List<Map<QName, Param>> xmlToJSONparams = new ArrayList<Map<QName, Param>>();
				xmlToJSONparams.add(new LinkedHashMap<QName, Param>());
				xmlToJSONparams.get(0).put(new QName("xml"), new Param("xml", throwableParam.getValue()));

				owner.evaluate(xmlToJSON, xmlToJSONparams);

				messageException = gson.fromJson(xmlToJSON.getValue(0).getStringValue(),Exception.class);

			} catch (Exception e) {
				throw new ProcessingException(e, info);
			}

		}

		if (listenerClass != null) {
			Param listenerClassParam = new Param(Const.LISTENER_CLASS_NAME.getLocalName(), listenerClass, info);
			owner.evaluate(listenerClassParam, params);
			try {
				listener = (MessageListener) Class.forName(listenerClassParam.getValue(0).getStringValue())
						.newInstance();
			} catch (Exception e) {
				owner.message(this.getClass().getCanonicalName(),
						"Unable to create an instance of [{}] as MessageListener : default one will be used instead.",
						new Object[] { listenerClass }, MessageLevelEnum.warn, e);
			}
		}

		Object[] messageParams = new Object[] {};

		if (param != null) {
			Param paramParam = new Param("", param, info);
			owner.evaluate(paramParam, params);
			messageParams = new Object[paramParam.getValue().size()];
			for (int i = 0; i < paramParam.getValue().size(); i++) {
				messageParams[i] = paramParam.getValue().itemAt(i);
			}
		}

		listener.message(sourceParam.getValue().toString(), selectParam.getValue().toString(), messageParams, level,
				messageException);

		return new Result();
	}

	@Override
	public void load(XdmNode node, Map<String, String> namespaces) {
		setInfo(node);
		select = node.getAttributeValue(Const.SELECT_NAME);
		source = node.getAttributeValue(Const.SOURCE_NAME);
		param = node.getAttributeValue(Const.PARAM_ATT_NAME);
		throwable = node.getAttributeValue(Const.THROWABLE_ATT_NAME);
		listenerClass = node.getAttributeValue(Const.LISTENER_CLASS_NAME);
		if (node.getAttributeValue(Const.LEVEL_NAME) != null) {
			level = MessageLevelEnum.valueOf(node.getAttributeValue(Const.LEVEL_NAME));
		} else {
			level = MessageLevelEnum.info;
		}
	}

}
