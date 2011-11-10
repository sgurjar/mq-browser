package com.sgurjar.mqb;

import java.util.Enumeration;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

public class MsgIterator implements Iterator<Message>, Iterable<Message> {
	
	final private Enumeration<?> enumeration;
	
	public MsgIterator(QueueBrowser br) throws JMSException {
		this.enumeration = br.getEnumeration();
	}
	
	@Override public boolean hasNext() { return enumeration.hasMoreElements(); }
	@Override public Message next   () { return (Message)enumeration.nextElement(); }
	@Override public void    remove () { throw new UnsupportedOperationException(); }

	@Override public Iterator<Message> iterator() { return this; }
}
