package eu.els.sie.xml.processing.engine.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageListenerImpl implements MessageListener {

	@Override
	public void message(String source, String message, Object[] params, MessageLevelEnum level, Exception e) {
		Logger logger = LoggerFactory.getLogger(source);

		if (params.length == 0) {
			switch (level) {
			case debug:
				logger.debug(message);
				break;
			case error:
				if (e != null) {
					logger.error(message, e);
				} else {
					logger.error(message);
				}
				break;
			case info:
				logger.info(message);
				break;
			case warn:
				logger.warn(message);
				break;
			case trace:
				logger.trace(message);
				break;
			default:
				break;
			}

		} else {
			switch (level) {
			case debug:
				logger.debug(message, params);
				break;
			case error:
				if (e != null) {
					logger.error(message, params, e);
				} else {
					logger.error(message, params);
				}
				break;
			case info:
				logger.info(message, params);
				break;
			case warn:
				logger.warn(message, params);
				break;
			case trace:
				logger.trace(message, params);
				break;
			default:
				break;
			}

		}
	}

}
