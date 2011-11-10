package com.sgurjar.mqb;

import javax.jms.Message;

import com.sgurjar.mqb.Util.Attribute;

public interface Printer {
	Attribute[] attributes();
	void print(final String qname, final String[] values);
	void print(final String qname, final Message m);
	void printText(final String text);
}