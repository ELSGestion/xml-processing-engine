package eu.els.sie.xml.processing.engine.utils;

import net.sf.saxon.s9api.QName;

public class Const {

	public static final String NAMESPACE_URI = "http://com.sitc.xml.processing.engine.org/config";

	public static final QName URI_NAME = new QName("", "uri");

	public static final QName NAME_NAME = new QName("", "name");

	public static final QName SELECT_NAME = new QName("", "select");

	public static final QName SOURCE_NAME = new QName("", "source");

	public static final QName PARAM_NAME = new QName(NAMESPACE_URI, "param");

	public static final QName OPTION_NAME = new QName(NAMESPACE_URI, "option");

	public static final QName VARIABLE_NAME = new QName(NAMESPACE_URI, "variable");

	public static final QName GP_NAME = new QName(NAMESPACE_URI, "gp");

	public static final QName TRANSFORM_NAME = new QName(NAMESPACE_URI, "transform");

	public static final QName PROCESSING_NAME = new QName(NAMESPACE_URI, "processing");

	public static final QName CALL_NAME = new QName(NAMESPACE_URI, "call");

	public static final Object CHOOSE_NAME = new QName(NAMESPACE_URI, "choose");

	public static final Object IF_NAME = new QName(NAMESPACE_URI, "if");

	public static final Object READ_NAME = new QName(NAMESPACE_URI, "read");

	public static final Object WHEN_NAME = new QName(NAMESPACE_URI, "when");

	public static final QName TEST_NAME = new QName("", "test");

	public static final Object OTHERWISE_NAME = new QName(NAMESPACE_URI, "otherwise");

	public static final Object MESSAGE_NAME = new QName(NAMESPACE_URI, "message");

	public static final Object FOREACH_NAME = new QName(NAMESPACE_URI, "foreach");

	public static final QName LEVEL_NAME = new QName("", "level");

	public static final Object JAVA_NAME = new QName(NAMESPACE_URI, "java");

	public static final QName CLASS_NAME = new QName("", "class");

	public static final QName METHOD_ATT_NAME = new QName("", "method");

	public static final QName PARAM_ATT_NAME = new QName("", "param");

	public static final QName THROWABLE_ATT_NAME = new QName("", "throwable");

	public static final QName RESULT_ATT_NAME = new QName("", "result");

	public static final QName ARGS_NAME = new QName("", "args");

	public static final Object SCRIPT_NAME = new QName(NAMESPACE_URI, "script");

	public static final QName CODE_NAME = new QName("", "code");

	public static final QName PERIOD_NAME = new QName("", "period");

	public static final Object EXIT_NAME = new QName(NAMESPACE_URI, "exit");

	public static final Object BREAK_NAME = new QName(NAMESPACE_URI, "break");
	
	public static final Object CONTINUE_NAME = new QName(NAMESPACE_URI, "continue");
	
	public static final Object WAIT_NAME = new QName(NAMESPACE_URI, "wait");
	
	public static final Object RETURN_NAME = new QName(NAMESPACE_URI, "return");
	
	public static final Object XSLT_NAME = new QName(NAMESPACE_URI, "xslt");

	public static final Object XQUERY_NAME = new QName(NAMESPACE_URI, "xquery");

	public static final Object PIPE_NAME = new QName(NAMESPACE_URI, "pipe");

	public static final QName XSLT_URI_NAME = new QName("", "xslt");

	public static final QName SOURCE_URI_NAME = new QName("", "source");

	public static final QName DESTINATION_URI_NAME = new QName("", "destination");

	public static final QName PARALLEL_NAME = new QName("", "parallel");

	public static final QName THREADS_NAME = new QName("", "threads");

	public static final QName FILE_SIZE_MAX_NAME = new QName("", "file-size-max");

	public static final QName FILE_NAME_FILTER_NAME = new QName("", "file-name-filter");

	public static final QName DIR_NAME_FILTER_NAME = new QName("", "dir-name-filter");

	public static final QName RECURSIVE_NAME = new QName("", "recursive");

	public static final QName BREAK_ON_ERROR_NAME = new QName("", "break-on-error");

	public static final QName DELETE_SOURCE_NAME = new QName("", "delete-source");

	public static final QName DEBUG_NAME = new QName("", "debug");

	public static final QName DEBUG_DIR_URI_NAME = new QName("", "debug-dir-uri");

	public static final QName TCF_NAME = new QName(NAMESPACE_URI, "try-catch-finally");

	public static final QName TRY_NAME = new QName(NAMESPACE_URI, "try");

	public static final QName CATCH_NAME = new QName(NAMESPACE_URI, "catch");

	public static final QName FINALLY_NAME = new QName(NAMESPACE_URI, "finally");

	public static final QName CONFIG_NAME = new QName(NAMESPACE_URI, "config");

	public static final QName EXCEPTION_VARIABLE_NAME_NAME = new QName("", "exception-variable-name");

	public static final QName SOURCE_FILE_NAME_VARIABLE_NAME_NAME = new QName("", "source-file-name-variable-name");

	public static final QName DESTINATION_FILE_NAME_NAME = new QName("", "destination-file-name");

	public static final QName LISTENER_CLASS_NAME = new QName("", "listener-class");

	public static final QName MESSAGE_LISTENER_CLASS_NAME = new QName("", "message-listener-class");

	public static final QName SAXON_MESSAGE_LISTENER_CLASS_NAME = new QName("http://saxon.sf.net/", "message-listener-class");

	public static final QName PIPE_LISTENER_CLASS_NAME = new QName("", "pipe-listener-class");

	public static final QName INITIAL_MODE_NAME = new QName("", "initial-mode");

	public static final QName INITIAL_TEMPLATE_NAME = new QName("", "initial-template");

	public static final QName USE_WHEN_NAME = new QName("", "use-when");;

}
