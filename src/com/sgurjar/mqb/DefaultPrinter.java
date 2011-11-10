package com.sgurjar.mqb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;

import com.sgurjar.mqb.Util.Attribute;

public class DefaultPrinter implements Printer {

	private final Attribute[] attributes = {
			Attribute.CURRENT_Q_DEPTH,
			Attribute.OPEN_INPUT_COUNT,
			Attribute.OPEN_OUTPUT_COUNT,
			Attribute.MAX_MSG_LENGTH,
			Attribute.DEPTH_HIGH_EVENT,
			Attribute.DEPTH_HIGH_LIMIT,
			Attribute.DEPTH_LOW_EVENT,
			Attribute.DEPTH_LOW_LIMIT,
			Attribute.DEPTH_MAX_EVENT,
			Attribute.MAX_Q_DEPTH,
			Attribute.Q_SERVICE_INTERVAL_EVENT,
			Attribute.Q_SERVICE_INTERVAL
	};

	private final StringTemplate qtemplate = new StringTemplate(
		"${QNAME}" +
		" CURDEPTH(${CURRENT_Q_DEPTH})" +
		" IPPROCS(${OPEN_INPUT_COUNT})" +
		" OPPROCS(${OPEN_OUTPUT_COUNT})"+
		" MAX_MSGLEN(${MAX_MSG_LENGTH})"+
		" Q_DEPTH_HIGH(${DEPTH_HIGH_EVENT},${DEPTH_HIGH_LIMIT})"+
		" Q_DEPTH_LOW(${DEPTH_LOW_EVENT},${DEPTH_LOW_LIMIT})"+
		" Q_DEPTH_MAX(${DEPTH_MAX_EVENT},${MAX_Q_DEPTH})"+
		" Q_SERVICE_INTERVAL(${Q_SERVICE_INTERVAL_EVENT},${Q_SERVICE_INTERVAL})"+
		" Q_CREATION_DATE(${Q_CREATION_DATE})"
		);

	private final StringTemplate msgtemplate = new StringTemplate(
		  "JMSTimestamp=${JMSTimestamp}"
		+ "\nSize=${msgsize} bytes"
		+ "${msgheader}"
		+ "${msgbody}"
		+ "\n==============================================================================");

	@Override
	public Attribute[] attributes() { return this.attributes; }

	@Override
	public void print(final String qname, final String[] values) {
		final HashMap<String, String> vars = new HashMap<String, String>();
		for (int i = 0; i < this.attributes.length; i++) {
			vars.put(this.attributes[i].name(), values[i]);
		}
		vars.put("QNAME", qname);
		vars.put("Q_CREATION_DATE", values[this.attributes.length]);
		System.out.println(this.qtemplate.inflate(vars));
	}

    final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");

	@Override
	public void print(final String qname, final Message m) {
		final Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("msgheader", m);
		String body = null;

		try {body = Util.msg_body(m);}
		catch (final JMSException ex) {body = Util.rootCause(ex).getMessage();}

		try {vars.put("JMSTimestamp", this.DATE_FMT.format(new Date(m.getJMSTimestamp())));}
		catch (final JMSException ex) {vars.put("JMSTimestamp", Util.rootCause(ex).getMessage());}

		vars.put("msgsize", body.length());
		vars.put("msgbody", body);
		System.out.println(this.msgtemplate.inflate(vars));
	}

	@Override
	public void printText(final String text) {
		System.out.println(text);
	}

}
