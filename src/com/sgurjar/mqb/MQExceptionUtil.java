package com.sgurjar.mqb;

import java.lang.reflect.Field;
import java.text.MessageFormat;

/**
 * converts MQException completion code and reason to string
 */
public class MQExceptionUtil {
	private static final Class<?> MQ_EXCEPTION_CLASS = mqExceptionClass();

	private static final String COMPLETION_CODE = "completionCode";
	private static final String REASON_CODE 	= "reasonCode";
	private static final String EXCEPTION_SOURCE= "exceptionSource";

	private static final String MQCC_ = "MQCC_";
	private static final String MQRC_ = "MQRC_";

	private static final MessageFormat FMT = new MessageFormat("{0}({1}) {2}({3}) {4}");

	public static boolean isMQException(final Throwable t) {
		return (t != null) && (MQ_EXCEPTION_CLASS != null) && MQ_EXCEPTION_CLASS.isInstance(t);
	}

	public static String details(final Throwable t) { return details(t, ""); }

	public static String details(final Throwable t, final String message) {
		if (!isMQException(t)) { return message; }

		final Class<? extends Throwable> tclass = t.getClass();
		final String completionCode    = Integer.toString(intFieldValue(t, tclass, COMPLETION_CODE));
		final String reasonCode        = Integer.toString(intFieldValue(t, tclass, REASON_CODE));
		final Object exceptionSource   = fieldValue(t, tclass, EXCEPTION_SOURCE);
		final String completionCodeStr = completionCode(t);
		final String reasonCodeStr     = reasonCode(t);

		// {0}({1}),{2}({3}),{4}
		return FMT.format(new Object[] {
				completionCodeStr,
				completionCode,
				reasonCodeStr,
				reasonCode,
				exceptionSource }
		);
	}

	private static Class<?> mqExceptionClass() {
		try { return java.lang.Class.forName("com.ibm.mq.MQException"); }
		catch (final Throwable t) { return null; }
	}

	private static String completionCode(final Throwable t) {
		return getCode(t, COMPLETION_CODE, MQCC_);
	}

	private static String reasonCode(final Throwable t) {
		return getCode(t, REASON_CODE, MQRC_);
	}

	private static String getCode(final Throwable t, final String field_name,
			final String const_field_starts_with) {
		// if (!isMQException(t)) {return null;}
		// check for isMQException before making this call
		// this function is private so need make sure in
		// this class only dont want to do it twice
		try {
			final int code = t.getClass().getField(field_name).getInt(t);
			final Field[] mqeFields = t.getClass().getFields();

			for (int i = 0; i < mqeFields.length; i++)
				if (mqeFields[i].getName().startsWith(const_field_starts_with)
						&& (code == mqeFields[i].getInt(t))) {
					return mqeFields[i].getName();
				}
		} catch (final Throwable h) {
		}

		return null;
	}

	private static int intFieldValue(final Object obj, final Class<?> cls, final String fieldName) {
		try {
			return cls.getField(fieldName).getInt(obj);
		} catch (final Throwable t) {}

		return -1;
	}

	private static Object fieldValue(final Object obj, final Class<?> cls, final String fieldName) {
		try {
			return cls.getField(fieldName).get(obj);
		} catch (final Throwable t) {}

		return "";
	}
}
