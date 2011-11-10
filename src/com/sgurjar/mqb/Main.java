package com.sgurjar.mqb;

import org.apache.commons.cli.ParseException;

import com.sgurjar.mqb.CmdArgs.UsageException;

public class Main {

	static final String PROG_NAME = System.getProperty("progname", Main.class.getSimpleName());

	public static void main(final String[] args) {
		final CmdArgs cmdargs = new CmdArgs(PROG_NAME);

		try {
			cmdargs.parse(args);
		} catch (final ParseException e) {
			if (!(e instanceof UsageException))System.out.println(e.getMessage());
			System.out.println(cmdargs.usage());
			return;
		}

		final QBrowser qb = new QBrowser(cmdargs.mqhost());
		qb.setPrinter(new DefaultPrinter());

		try {
			for (final String qname : cmdargs.queues()) {
				qb.q(qname);
				qb.browse(qname, cmdargs.browse());
			}
		} finally {
			qb.close();
		}
	}
}
