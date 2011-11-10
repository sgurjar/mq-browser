package com.sgurjar.mqb;

import static com.sgurjar.mqb.Util.*;

import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;

public class QBrowser {

	private final Map<String, String> mqhost;
	private MQQueueManager mqicon;
	private Connection jmscon;
	private Printer printer;

	public QBrowser(final String mqhost) {
		this.mqhost = parse_mqhost(mqhost);
	}

	public void setPrinter(final Printer printer) {
		this.printer = printer;
		this.printer.printText(String.valueOf(this.mqhost));
	}

	public void q(final String qname) {
		try {
			if (this.mqicon == null) this.mqicon = mqi_open(this.mqhost);
			final String[] values = Util.q(this.mqicon, qname, this.printer.attributes());
			this.printer.print(qname, values);
		} catch (final MQException ex) {
			System.err.println("ERROR: "+ qname + " " + MQExceptionUtil.details(ex));
		}
	}

	public void browse(final String qname, final int nmsgs) {
		if (nmsgs <= 0)
			return;

		try {
			if (this.jmscon == null) this.jmscon = jms_open(this.mqhost);

			final Session session = jms_session(this.jmscon);

			int count = 0;
			for (final Message m : msgs(session, qname)) {
				this.printer.print(qname, m);
				if (++count == nmsgs) break;
			}

		} catch (final JMSException ex) {
			System.err.println("ERROR: "+ qname + " " + rootCause(ex).getMessage());
		}
	}

	public void close() {
		Util.close(this.mqicon);
		Util.close(this.jmscon);
	}
}
