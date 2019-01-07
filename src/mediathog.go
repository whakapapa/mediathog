package main

// main program launcher

import (
	"./base"
)

//TODO undone








//TODO old java below

package mediathog;

import com.jidesoft.utils.SystemInfo;
import com.jidesoft.utils.ThreadCheckingRepaintManager;
import javafx.application.Platform;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;
import mSearch.Config;
import mSearch.tool.Log;
import mSearch.tool.SingleInstance;
import mediathog.config.Daten;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static mediathek.tool.MVFunctionSys.startMeldungen;

public class Main {
	private static final String JAVAFX_CLASSNAME_APPLICATION_PLATFORM = "javafx.application.Platform";
	private static final String HTTP_PROXY_USER = "http.proxyUser";
	private static final String HTTP_PROXY_PW = "http.proxyPassword";
	private static final String LOG_TEXT_PROXY_AUTHENTICATION_SUCESSFUL = "Proxy Authentication: (%s)";
	private static final String LOG_TEXT_PROXY_AUTHENTICATION_NOT_CONFIGURED = "Proxy Authentication: not configured";
	private static final String LOG_TEXT_PROXY_PASSWORD_NOT_SET = "Proxy Authentication: Password is not set";
	private static final String LOG_TEXT_PROXY_AUTHENTICATION_CANNOT_ACCESS_PROXY_USER_PROXY_PW = "Proxy Authentication: cannot access proxyUser / proxyPassword";
	private static final String LOG_TEXT_MEDIATHEK_VIEW_IS_ALREADY_RUNNING = "Mediathog wird bereits ausgeführt!";
	private static final String X11_AWT_APP_CLASS_NAME = "awtAppClassName";

	private static final Logger logger = LogManager.getLogger(Main.class);



	/**
	* Tests if javafx is in the classpath by loading a well known class.
	*/
	private static void checkForJavaFX() {
		final String message = "Mediathog benötigt ein installiertes JavaFX.";

		try {
			Class.forName(JAVAFX_CLASSNAME_APPLICATION_PLATFORM);
			} catch (ClassNotFoundException e) {
				logger.error("JavaFX was not found on system.", e);
				if (GraphicsEnvironment.isHeadless()) {
					System.err.println(message);
					} else {
						//we have a screen
						JOptionPane.showMessageDialog(null,
							message,
							"JavaFX nicht gefunden", JOptionPane.ERROR_MESSAGE);
						}
						System.exit(3);
					}
				}



				private static void printArguments(final String... aArguments) {
					for (String argument : aArguments) {
						logger.info("Startparameter: {}", argument);
					}
				}

				private static String readPfadFromArguments(final String... aArguments) {
					String pfad = "";
					if (aArguments != null && aArguments.length > 0) {
						for (String arg : aArguments) {
							if (!arg.startsWith("-")) {
								if (!arg.endsWith(File.separator)) {
									arg += File.separator;
								}
								pfad = arg;
							}
						}
					}

					return pfad;
				}

				private static void setupPortableMode(String... args) {
					printArguments(args);
					final String basePath = readPfadFromArguments(args);
					if (!basePath.isEmpty()) {
						Config.setPortableMode(true);
						logger.info("Portable Mode: true");
						} else
						logger.info("Portable Mode: false");

						//initialize Daten object now for database
						Daten.getInstance(basePath);
					}

					/**
					* @param args the command line arguments
					*/
					public static void main(final String... args) {

						setupPortableMode(args);

						checkForJavaFX();

						IconFontSwing.register(FontAwesome.getIconFont());
						new Main().start(args);
					}



					private void start(String... args) {
						StartupMode startupMode = StartupMode.GUI;

						proxyAuthentication();

						if (args != null) {
							startupMode = processArgs(startupMode, args);
						}

						startUI(startupMode, args);
					}

					/*
					* Aufruf:
					* java -jar Mediathog [Pfad zur Konfigdatei, sonst homeverzeichnis] [Schalter]
					*
					* Programmschalter:
					*
					* -M Fenster maximiert starten
					* -A Automodus
					* -noGui ohne GUI starten und die Filmliste laden
					*
					* */

					private void startUI(StartupMode aStartupMode, final String... aArguments) {
						aStartupMode = switchToCLIModeIfNecessary(aStartupMode);
						switch (aStartupMode) {
						case AUTO:
							startAutoMode(aArguments);
							break;

						case FASTAUTO:
							startFastAutoMode(aArguments);
							break;

						case GUI:
							startGuiMode();
							break;
						default:
							startUI(StartupMode.GUI);
						}
					}


					private void startGuiMode() {
						EventQueue.invokeLater(() ->
						{

							//JavaFX stuff
							Platform.setImplicitExit(false);



							if (Config.isDebuggingEnabled()) {
								// use for debugging EDT violations
								RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
							}

							//prevent startup of multiple instances...
							SingleInstance singleInstanceWatcher = new SingleInstance();
							if (singleInstanceWatcher.isAppAlreadyActive()) {
								JOptionPane.showMessageDialog(null, LOG_TEXT_MEDIATHEK_VIEW_IS_ALREADY_RUNNING);
								System.exit(1);
							}


							getPlatformWindow().setVisible(true);
							});
						}

						private MediathekGui getPlatformWindow() {
							MediathekGui window;
							setupX11WindowManagerClassName();

							window = new MediathekGui();


							return window;
						}

						/**
						* Setup the X11 window manager WM_CLASS hint.
						* Enables e.g. GNOME to determine application name and to enable app specific functionality.
						*/
						private void setupX11WindowManagerClassName() {
							try {
								Toolkit xToolkit = Toolkit.getDefaultToolkit();
								java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField(X11_AWT_APP_CLASS_NAME);
								awtAppClassNameField.setAccessible(true);
								awtAppClassNameField.set(xToolkit, setProg);
								} catch (Exception ignored) {
									logger.warn("Could not set awtAppClassName");
								}
							}

							private void startFastAutoMode(final String[] args) {
								final MediathekAuto mvAuto = new MediathekAuto(args);
								mvAuto.setFastAuto(true);
								mvAuto.starten();
							}

							private void startAutoMode(final String[] args) {
								new MediathekAuto(args).starten();
							}

							private StartupMode switchToCLIModeIfNecessary(final StartupMode aState) {
								/*
								If user tries to start MV from command-line without proper options,
								instead of crashing while trying to open Swing windows, just change to CLI mode and warn the user.
								*/
								if (GraphicsEnvironment.isHeadless() && (aState == StartupMode.GUI)) {
									logger.warn("Headless environment detected but -auto was not specified.");
									System.err.println("Mediathog wurde nicht als Kommandozeilenprogramm gestartet.");
									System.err.println("Startmodus wurde auf -auto geändert.");
									System.err.println();
									return StartupMode.AUTO;
								}
								return aState;
							}

							private StartupMode processArgs(final StartupMode aStartupMode, final String... aArguments) {
								StartupMode newStartupMode = null;
								for (String argument : aArguments) {
									argument = argument.toLowerCase();
									switch (argument) {
									case ProgramArguments.STARTUPMODE_AUTO:
										newStartupMode = StartupMode.AUTO;
										break;

									case ProgramArguments.STARTUPMODE_FASTAUTO:
										newStartupMode = StartupMode.FASTAUTO;
										break;

									case ProgramArguments.STARTUPMODE_VERBOSE:
										EventQueue.invokeLater(() ->
										{
											startMeldungen();
											logger.info("Systemmeldung");
											Log.errorLog(100000000, "Fehlermeldung");
											Log.endMsg();
											System.exit(0);
											});
											break;

										case ProgramArguments.STARTUPMODE_DEBUG:
											Config.enableDebugMode();
											break;

										case ProgramArguments.STARTUPMODE_MAXIMIZED:
											Daten.setStartMaximized(true);
											break;
										}
									}

									return newStartupMode == null ? aStartupMode : newStartupMode;
								}

								private void proxyAuthentication() {
									//TODO remove if not used anymore by URLConnection
									try {
										final String prxUser = System.getProperty(HTTP_PROXY_USER, null);
										final String prxPassword = System.getProperty(HTTP_PROXY_PW, null);
										if (prxUser != null && prxPassword != null) {
											final PasswordAuthentication authenticator = new PasswordAuthentication(prxUser, prxPassword.toCharArray());
											Authenticator.setDefault(new Authenticator() {
												@Override
												protected PasswordAuthentication getPasswordAuthentication() {
													return authenticator;
												}
												});
												logger.info(String.format(LOG_TEXT_PROXY_AUTHENTICATION_SUCESSFUL, prxUser));
												} else if (prxUser != null && prxPassword == null) {
													logger.info(LOG_TEXT_PROXY_PASSWORD_NOT_SET);
													} else {
														logger.info(LOG_TEXT_PROXY_AUTHENTICATION_NOT_CONFIGURED);
													}

													} catch (SecurityException se) {
														logger.warn(LOG_TEXT_PROXY_AUTHENTICATION_CANNOT_ACCESS_PROXY_USER_PROXY_PW + se.toString());
													}
												}

												private enum StartupMode {

													GUI, AUTO, FASTAUTO
												}

												private final class ProgramArguments {
													private static final String STARTUPMODE_AUTO = "-auto";
													private static final String STARTUPMODE_FASTAUTO = "-fastauto";
													private static final String STARTUPMODE_DEBUG = "-d";
													private static final String STARTUPMODE_MAXIMIZED = "-m";
													private static final String STARTUPMODE_VERBOSE = "-v";
												}
											}
