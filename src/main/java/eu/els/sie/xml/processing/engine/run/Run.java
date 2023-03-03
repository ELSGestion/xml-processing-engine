package eu.els.sie.xml.processing.engine.run;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import eu.els.sie.xml.processing.engine.exception.ProcessingException;
import eu.els.sie.xml.processing.engine.step.ProcessingStep;
import eu.els.sie.xml.processing.engine.utils.Param;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;

public class Run {

	public Run() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws ProcessingException {
		
		Options options = new Options();
		Option xpeUriOpt = Option.builder().option("xpeUri").longOpt("xmlProcessingEngineUri")
				.desc("The uri of the XPE script").hasArg(true).numberOfArgs(1).required(false).build();
		Option helpOpt = Option.builder().option("h").longOpt("help").desc("The help").hasArg(false).build();
		Option paramsOpt = Option.builder().option("P").desc("The params of he XPE script").hasArgs().valueSeparator('=').required(false)
				.build();
		options.addOption(xpeUriOpt);
		options.addOption(helpOpt);
		options.addOption(paramsOpt);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}

		if (cmd.hasOption(helpOpt)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("XPE CommandLine", options);
		} else if (cmd.hasOption(xpeUriOpt)) {
			ProcessingStep proc = new ProcessingStep();
			proc.load(cmd.getOptionValue(xpeUriOpt));
			Map<QName, Param> params = new HashMap<>();

			for (Entry<Object, Object> entry : cmd.getOptionProperties(paramsOpt).entrySet()) {
				params.put(new QName(entry.getKey().toString()), new Param(new QName(entry.getKey().toString()),
						new XdmAtomicValue(entry.getValue().toString())));
			}

			proc.execute(params);
		}

	}

}
