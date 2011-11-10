package com.sgurjar.mqb;

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnectionFactory;

public final class Util {
	private Util(){/*static only*/}

	public static final String MQHOST_FORMAT = "host[:port]@channel[:qmgr]";

	static final String[] MQEVR = { "DISABLED", "ENABLED", "EXCEPTION", "NO_DISPLAY" };
	static final String[] MQQSIE = { "NONE", "HIGH", "OK" };

	public static enum Attribute {
		CURRENT_Q_DEPTH         (MQC.MQIA_CURRENT_Q_DEPTH),
		OPEN_INPUT_COUNT        (MQC.MQIA_OPEN_INPUT_COUNT),
		OPEN_OUTPUT_COUNT       (MQC.MQIA_OPEN_OUTPUT_COUNT),
		MAX_MSG_LENGTH          (MQC.MQIA_MAX_MSG_LENGTH),
		DEPTH_HIGH_EVENT        (MQC.MQIA_Q_DEPTH_HIGH_EVENT){
			@Override
			public String value(final int val) { return MQEVR[val]; }
		},
		DEPTH_HIGH_LIMIT        (MQC.MQIA_Q_DEPTH_HIGH_LIMIT){
			@Override
			public String value(final int val) { return val + "%"; }
		},
		DEPTH_LOW_EVENT         (MQC.MQIA_Q_DEPTH_LOW_EVENT){
			@Override
			public String value(final int val) { return MQEVR[val]; }
		},
		DEPTH_LOW_LIMIT         (MQC.MQIA_Q_DEPTH_LOW_LIMIT){
			@Override
			public String value(final int val) { return val + "%"; }
		},
		DEPTH_MAX_EVENT         (MQC.MQIA_Q_DEPTH_MAX_EVENT){
			@Override
			public String value(final int val) { return MQEVR[val]; }
		},
		MAX_Q_DEPTH             (MQC.MQIA_MAX_Q_DEPTH),
		Q_SERVICE_INTERVAL_EVENT(MQC.MQIA_Q_SERVICE_INTERVAL_EVENT){
			@Override
			public String value(final int val) { return MQQSIE[val]; }
		},
		Q_SERVICE_INTERVAL      (MQC.MQIA_Q_SERVICE_INTERVAL){
			@Override
			public String value(final int val) { return (val/1000)+"secs"; }
		};

		private final int num;
		private Attribute(final int n) {this.num = n;}
		public int index() { return this.num; }
		public String value(final int val) {return String.valueOf(val);}
	};

	private static final SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

	public static Map<String, String> parse_mqhost(final String mqhost) {
		/*
		 * host[:port]@channel[:qmgr]
		 */

		final String[] a = mqhost.split("@");
		if (a.length < 2)
			throw new IllegalArgumentException("invalid mqhost string '"
					+ mqhost + "'. valid mqhost format is " + MQHOST_FORMAT);

		final String a1 = a[0];
		final String a2 = a[1];

		String[] x = a1.split(":");
		final String host = x[0];
		int port = 1414;
		if (x.length > 1)
			port = Integer.parseInt(x[1]);

		x = a2.split(":");
		final String channel = x[0];
		String qmgrname = null;
		if (x.length > 1)
			qmgrname = x[1];

		final Map<String, String> hm = new LinkedHashMap<String, String>();

		hm.put("host", host);
		hm.put("port", String.valueOf(port));
		hm.put("channel", channel);
		hm.put("mgr", qmgrname);

		return hm;
	}

	public static MQQueueManager mqi_open(final Map<String, String> params) throws MQException {
		MQException.log = null; // disable error logging
		MQEnvironment.hostname = params.get("host");
		MQEnvironment.port = Integer.parseInt(params.get("port"));
		MQEnvironment.channel = params.get("channel");
		return new MQQueueManager(params.get("mgr"));
	}

    public static void close(final MQQueueManager con) {
		if (con != null) {
			try { con.close(); }
			catch (final MQException e) { e.printStackTrace(System.err); }
		}
	}

	public static String[] q(final MQQueueManager con, final String qname,
			final Attribute... attributes) throws MQException {
		/* returns String[] parallel indexed to attributes
		 * each element of String[] is value of attribute at
		 * same index in attributes. Size of the returned String[]
		 * is 1 larger then size of attributes array, last index in
		 * String[] holds creation date of the queue.
		 */
		final int sz = attributes.length;
		final int[] selectors = new int[sz];
		final int[] intattrs = new int[sz];
		final byte[] charattrs = new byte[sz];

		final String[] ret = new String[sz+1]; // +1 for creationdate

		for (int i = 0; i < sz; i++) {
			selectors[i] = attributes[i].index();
			intattrs[i] = 0;
			charattrs[i] = (byte) 0;
		}

		MQQueue queue = null;

		try {
			queue = con.accessQueue(qname, MQC.MQOO_INQUIRE);
			queue.inquire(selectors, intattrs, charattrs);
			for (int i = 0; i < sz; i++) {
				ret[i] = attributes[i].value((intattrs[i]));
			}
			ret[sz] = datefmt.format(queue.getCreationDateTime().getTime());
		} finally {
			if (queue != null)
				queue.close();
		}
		return ret;
	}

	public static Connection jms_open(final Map<String, String> params) throws JMSException {
		final MQQueueConnectionFactory facotry = new MQQueueConnectionFactory();
		facotry.setHostName(params.get("host"));
		facotry.setPort(Integer.parseInt(params.get("port")));
		facotry.setChannel(params.get("channel"));
		final String mgr = params.get("mgr");
		if (mgr != null)facotry.setQueueManager(mgr);
		facotry.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
		final Connection qconn = facotry.createConnection();
		return qconn;
	}

	public static void close(final Connection con) {
		if (con != null) {
			try { con.close(); }
			catch (final JMSException e) { e.printStackTrace(System.err); }
		}
	}

	public static Session jms_session(final Connection con) throws JMSException {
		final boolean transacted = false;
		final int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
		return con.createSession(transacted, acknowledgeMode);
	}

	public static QueueBrowser jms_browser(final Session session, final String qname,
			final String selector) throws JMSException {
		final javax.jms.Queue queue = session.createQueue("queue:///" + qname);
		return session.createBrowser(queue, selector);
	}

	public static QueueBrowser jms_browser(final Session session, final String qname)
			throws JMSException {
		return jms_browser(session, qname, null);
	}

	public static MsgIterator msgs(final Session session, final String qname)
			throws JMSException {
		return new MsgIterator(jms_browser(session, qname));
	}

	public static MsgIterator msgs(final Session session, final String qname,
			final String selector) throws JMSException {
		return new MsgIterator(jms_browser(session, qname, selector));
	}

	public static String msg_body(final Message msg) throws JMSException {
		if (msg instanceof BytesMessage) {
			final BytesMessage bmsg = (BytesMessage) msg;
			return bmsg.readUTF();
		} else if (msg instanceof MapMessage) {
			final MapMessage mmsg = (MapMessage) msg;
			final Enumeration<?> keys = mmsg.getMapNames();
			final HashMap<String, Object> hm = new HashMap<String, Object>();
			while (keys.hasMoreElements()) {
				final String key = String.valueOf(keys.nextElement());
				hm.put(key, mmsg.getObject(key));
			}
			return String.valueOf(hm);
		} else if (msg instanceof ObjectMessage) {
			final ObjectMessage omsg = (ObjectMessage) msg;
			return String.valueOf(omsg.getObject());
		} else if (msg instanceof StreamMessage) {
			final StreamMessage smsg = (StreamMessage) msg;
			return String.valueOf(smsg.readObject());
		} else if (msg instanceof TextMessage) {
			final TextMessage tmsg = (TextMessage) msg;
			return tmsg.getText();
		} else {
			throw new JMSException("unsupported Message type, " + (msg != null ? msg.getClass() : "null"));
		}
	}

	public static Throwable rootCause(final Throwable e) {
		if (e instanceof JMSException) {
			final Exception cause = ((JMSException) e).getLinkedException();
			return (cause != null) ? rootCause(cause) : e;
		} else {
			final Throwable cause = e.getCause();
			return (cause != null) ? rootCause(cause) : e;
		}
	}
}