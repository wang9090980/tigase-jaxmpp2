package tigase.jaxmpp.core.client.logger;

public class LoggerFactory implements Logger {

	private static LoggerSpiFactory spiFactory = new LoggerSpiFactory() {

		@Override
		public LoggerSpi getLoggerSpi(String name) {
			return new LoggerSpi() {

				@Override
				public boolean isLoggable(LogLevel level) {
					return false;
				}

				@Override
				public void log(LogLevel level, String msg) {
				}

				@Override
				public void log(LogLevel level, String msg, Throwable thrown) {
				}
			};
		}
	};

	public static Logger getLogger(Class<?> class1) {
		return getLogger(class1.getName());
	}

	public static Logger getLogger(String name) {
		final LoggerSpi spi = spiFactory.getLoggerSpi(name);
		return new LoggerFactory(spi);
	}

	public static void setLoggerSpiFactory(final LoggerSpiFactory spiFactory) {
		LoggerFactory.spiFactory = spiFactory;
	}

	private final LoggerSpi spi;

	private LoggerFactory(LoggerSpi spi) {
		this.spi = spi;
	}

	@Override
	public void config(String string) {
		log(LogLevel.CONFIG, string);
	}

	@Override
	public void fine(String string) {
		log(LogLevel.FINE, string);
	}

	@Override
	public void finer(String string) {
		log(LogLevel.FINER, string);
	}

	@Override
	public void finest(String string) {
		log(LogLevel.FINEST, string);
	}

	@Override
	public void info(String string) {
		log(LogLevel.INFO, string);
	}

	@Override
	public boolean isLoggable(LogLevel level) {
		return spi.isLoggable(level);
	}

	@Override
	public void log(LogLevel level, String msg) {
		spi.log(level, msg);
	}

	@Override
	public void log(LogLevel level, String msg, Throwable thrown) {
		spi.log(level, msg, thrown);
	}

}