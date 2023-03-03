package eu.els.sie.xml.processing.engine.listener;

import org.apache.logging.log4j.message.ParameterizedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipeListenerImpl implements PipeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(PipeListenerImpl.class);

	@Override
	public void succed(String pipeName, int total, String inputUri, int nb) {
		LOGGER.debug("Transfo succed {}, {}, {}, {}", pipeName, total, inputUri, nb);
	}

	@Override
	public void failed(String pipeName, int total, String inputUri, int nb, Throwable e) {
		LOGGER.error(new ParameterizedMessage("Transfo failed {}, {}, {}, {}", pipeName, total, inputUri, nb)
				.getFormattedMessage(), e);
	}

	@Override
	public void interrupted(String pipeName, int total, String inputUri, int nb) {
		LOGGER.debug("Transfo interrupted {}, {}, {}, {}", pipeName, total, inputUri, nb);
	}

	@Override
	public void start(String pipeName, int total, String inputUri, int nb) {
		LOGGER.debug("Start transfo {}, {}, {}, {}", pipeName, total, inputUri, nb);
	}

}
