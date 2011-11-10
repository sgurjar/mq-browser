package com.sgurjar.mqb;

import static com.sgurjar.mqb.Util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CmdArgs {
	public static class UsageException extends ParseException{
		private static final long serialVersionUID = 1L;
		public UsageException(final String message) { super(message); }
	}

	private final Options opts = new Options();
	private final String progname;

	private String mqhost;
	private String[] qnames;
	private int nbrowse = 0;
	private CommandLine cli;

	public CmdArgs(final String progname) {
		this.progname = progname;

		final Option help = new Option("help", "help", false, "display this message");

		final Option mqhost = new Option("h", "host", true, "mq host, " + MQHOST_FORMAT);
		mqhost.setArgName("mqhost");
		mqhost.setArgs(1);

		final Option q = new Option("q", "queue", true, "queue name, use multiple options to pass multiple queues");
		q.setArgName("qname");

		final Option browse = new Option("b","browse", true, "browse <msgs> number of messages from queue");
        browse.setArgName("msgs");
        browse.setArgs(1);
        browse.setType(int.class);

		this.opts
			.addOption(help)
			.addOption(mqhost)
			.addOption(q)
			.addOption(browse);
	}

	public String   mqhost() { return  this.mqhost;}
	public String[] queues() { return  this.qnames;}
	public int      browse() { return  this.nbrowse;}

	public String usage() {
		final int width = 80;
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		final HelpFormatter h = new HelpFormatter();
		h.printUsage(pw, width, this.progname + " [options]");
		h.printWrapped(pw, width, "where options are:");
		h.printOptions(pw, width, this.opts, 4, 2);
		h.printWrapped(pw, width, "If you do not specify any filename or qname, reads qnames from standard input.");
		return sw.toString();
	}

	public CmdArgs parse(final String[] args) throws ParseException {
		this.cli = new GnuParser().parse(this.opts, args);

		if (this.cli.hasOption("help")) throw new UsageException("show help");

		this.mqhost = this.cli.getOptionValue('h');
		if (this.mqhost == null) throw new MissingArgumentException(this.mqhost);

		this.qnames = this.cli.getOptionValues('q');
		if (this.qnames == null) this.qnames = stdin();

		final String browse = this.cli.getOptionValue('b');
		if (browse != null) {
			try{this.nbrowse = Integer.parseInt(browse);}
			catch(final NumberFormatException ex){
				throw new ParseException("invalid number of messages to browse, " + browse);
			}
		}

		return this; // for chaining
	}

	String[] stdin() throws ParseException {
		final BufferedReader br = new BufferedReader(new InputStreamReader( System.in));
		final ArrayList<String> alist = new ArrayList<String>();

		try {
			for (String line = null;;) {
				line = br.readLine();
				if (line == null) break;
				if ((line = line.trim()).isEmpty() || line.startsWith("#")) continue;
				alist.add(line);
			}
		} catch (final IOException e) {
			throw new ParseException(Util.rootCause(e).getMessage());
		}

		return alist.toArray(new String[alist.size()]);
	}
}
