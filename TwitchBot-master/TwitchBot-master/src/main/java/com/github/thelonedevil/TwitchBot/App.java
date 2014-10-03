package com.github.thelonedevil.TwitchBot;

import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.User;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 */
@SuppressWarnings("rawtypes")
public class App extends ListenerAdapter implements Listener {

	static CustomBot bot;
	static Channel channel;
	List<String> list = new ArrayList<String>();
	List<String> userlist = new ArrayList<String>();
	static Listener listener = new App();
	static boolean stop = false;
	static Map<String, Map<String, Map<String, String>>> commands;
    //static Map<String,List<String>> mods;
    String stopMessage = "My Master has ordered me to take some time off. I believe this is so My Master can bring in the exterminator. As such i will be leaving now, and will be back once My Master is done.";

	public static void main(String[] args) {

		String oauth;
		try {
			oauth = getOauth();
			@SuppressWarnings("unchecked")
			Configuration<?> configuration = new Configuration.Builder().setName("Spritleybot").setAutoNickChange(false).setCapEnabled(false).addListener(listener).setServerHostname("irc.twitch.tv")
					.setServerPort(6667).setServerPassword(oauth).addAutoJoinChannel("#thespriteful")/*.addAutoJoinChannel("#lone_bot")*/.buildConfiguration();
			System.out.println("Bot config built");
			reload();
			bot = new CustomBot(configuration);
			bot.startBot();
		} catch (Exception ex) {
			System.out.println("error happened");
			ex.printStackTrace();
		}

	}

	private static String getOauth() throws IOException {
		String oauth = "";
		File config = new File("config.txt");
        System.out.println(config.getCanonicalPath());
		FileInputStream fis = new FileInputStream(config);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("oauth:")) {
				oauth = line;
			}
		}

		br.close();
		return oauth;
	}

	@SuppressWarnings("unchecked")
	private static void reload() throws IOException {
		Yaml yaml = new Yaml();
		File command = new File("commands.yml");
		FileInputStream ios = new FileInputStream(command);
		commands = (Map<String, Map<String, Map<String, String>>>) yaml.load(ios);
        ios.close();

       /* File mod = new File("mods.yml");
        ios = new FileInputStream(mod);
        mods =  (Map<String,List<String>>)yaml.load(ios);
        // System.out.println(commands.toString());*/
	}

	private boolean isMod(User user, Channel channel) {
		Set channels = user.getChannelsOpIn();
        //List mods1 = mods.get(channel.getName());
		if (channels.contains(channel)/*|| mods1.contains(user.getNick())*/) {
			return true;
		} else
			return false;

	}

	@Override
	public void onMessage(MessageEvent event) {
		Channel channel = event.getChannel();
		String channel1 = channel.getName();

		if (event.getMessage().startsWith("!")) {

			if (commands.containsKey(event.getChannel().getName().replace("#", ""))) {
				// commands for channel
				Map<String, Map<String, String>> mod = commands.get(channel1.replace("#", ""));
				if (isMod(event.getUser(), event.getChannel())) {
					// mods do all commands
					if (mod.get("mod").containsKey(event.getMessage().replace("!", "").split(" ")[0])) {
                        String message = mod.get("mod").get(event.getMessage().replace("!", "").split(" ")[0]);
                        if(message.contains("%user%")){
                            message = message.replace("%user%", event.getUser().getNick());
                        }
                        if(message.contains("%touser%")){
                            message = message.replace("%touser%", event.getMessage().split(" ")[1]);
                        }
						event.getChannel().send().message(message);
					} else if (mod.get("user").containsKey(event.getMessage().replace("!", "").split(" ")[0])) {
                        String message = mod.get("user").get(event.getMessage().replace("!", "").split(" ")[0]);
                        if(message.contains("%user%")){
                            message = message.replace("%user%", event.getUser().getNick());
                        }
                        if(message.contains("%touser%")){
                            message = message.replace("%touser%", event.getMessage().split(" ")[1]);
                        }
                        event.getChannel().send().message(message);
					}
				} else if (!isMod(event.getUser(), event.getChannel())) {
					// non mod can do user commands
					if (mod.get("user").containsKey(event.getMessage().replace("!", "").split(" ")[0])) {
                        String message = mod.get("user").get(event.getMessage().replace("!", "").split(" ")[0]);
                        if(message.contains("%user%")){
                            message = message.replace("%user%", event.getUser().getNick());
                        }
                        if(message.contains("%touser%")){
                            message = message.replace("%touser%", event.getMessage().split(" ")[1]);
                        }
                        event.getChannel().send().message(message);
					}
				}
			}
		} else {
			if (event.getMessage().equalsIgnoreCase("?commands")) {
				if (isMod(event.getUser(), event.getChannel())) {
                    event.respond("The command list is not available at this time");
				}
			}
			if (event.getChannel().getName().equalsIgnoreCase("#spritleybot")) {
				if (event.getMessage().startsWith("?join")) {
					String name = event.getUser().getNick();
                    bot.sendIRC().joinChannel("#"+name);
				}
			}

			if (event.getMessage().startsWith("?dual")) {
				String name = event.getMessage().split(" ")[1];
				String url = "http://kbmod.com/multistream/view/?s0=thespriteful&s1=" + name + "&layout=4";
				event.getChannel().send().message("GO watch the multistream with " + name + " here " + url); // do
				// name
			}
			if (event.getMessage().startsWith("?tri")) {
				String[] message = event.getMessage().split(" ");
				String name1 = message[1];
				String name2 = message[2];
				String url = "http://kbmod.com/multistream/view/?s0=thespriteful&s1=" + name1 + "&s2=" + name2 + "&layout=7";
				event.getChannel().send().message("GO watch the multistream with " + name1 + " and " + name2 + " here " + url); // do
				// name
			}
			if (event.getMessage().equalsIgnoreCase("?reload")) {
				if (isMod(event.getUser(), event.getChannel())) {
					try {
						reload();
						event.getChannel().send().message("Commands Reloaded");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (event.getMessage().equalsIgnoreCase("?stop")) {
				if (event.getUser().getNick().equalsIgnoreCase("thespriteful")) {
					//event.respond("I must go, my people need me!");
                    Set<Channel> channels = event.getBot().getUserChannelDao().getChannels(event.getBot().getUserBot());
                    for (Channel ch : channels){
                        ch.send().message(stopMessage);
                    }
					bot.stop();
				} else {
					event.respond("Only My Owner: thespriteful can use that command");

				}
			}

			if (event.getMessage().equalsIgnoreCase("?Spritleybot-leave")) {
				if (isMod(event.getUser(), event.getChannel())) {
					event.respond("I will leave now at the request of a Moderator of this channel");
					event.getChannel().send().part();
				}
			}
			if (event.getMessage().equalsIgnoreCase("?help?")) {
				if (isMod(event.getUser(), event.getChannel())) {
					event.respond("go help yourself");
				} else if (!userlist.contains(event.getUser().getNick())) {
					event.respond("no.");
					userlist.add(event.getUser().getNick());
				}
			}
			if (event.getMessage().contains("hello ") || event.getMessage().contains("hi ") || event.getMessage().contains("hey ") || event.getMessage().contains("Hello ")
					|| event.getMessage().contains("Hi ") || event.getMessage().contains("Hey ") || event.getMessage().equalsIgnoreCase("hello") || event.getMessage().equalsIgnoreCase("hi")
					|| event.getMessage().equalsIgnoreCase("hey") || event.getMessage().contains("hello!") || event.getMessage().contains("hi!") || event.getMessage().contains("hey!")
					|| event.getMessage().contains("Hello!") || event.getMessage().contains("Hi!") || event.getMessage().contains("Hey!")) {
				if (!list.contains(event.getUser().getNick())) {
					event.respond("Welcome to "+event.getChannel().getName()+"'s Channel");
					list.add(event.getUser().getNick());
				}
			}
			if (event.getMessage().equalsIgnoreCase("?clearWelcomes")) {
				list = new ArrayList<String>();
			}

			// timeouts
			if (!isMod(event.getUser(), channel)) {
				if (event.getMessage().contains(" ")) {
					String[] message = event.getMessage().split(" ");
					int i = 0;
					int s = message.length;
					while (i < s) {
						Pattern pattern = Pattern.compile("(?:(())(www\\.([^/?#\\s]*))|((http(s)?|ftp):)(//([^/?#\\s]*)))([^?#\\s]*)(\\?([^#\\s]*))?(#([^\\s]*))?");
						Pattern pattern1 = Pattern.compile("^[a-zA-Z0-9\\-\\.]+\\.(com|org|net|mil|edu|co.uk|be|ly|tk|COM|ORG|NET|MIL|EDU|CO.UK|TK|BE|LY)$*");
						Matcher matcher = pattern.matcher(message[i]);
						Matcher matcher1 = pattern1.matcher(message[i]);
						if (matcher.find() || matcher1.find()) {
							event.respond("That is a link, and you are not a Mod, so stop posting it");
							event.getChannel().send().message(".timeout " + event.getUser().getNick() + " 1");
						}
						i++;
					}
				} else {
					Pattern pattern = Pattern.compile("(?:(())(www\\.([^/?#\\s]*))|((http(s)?|ftp):)(//([^/?#\\s]*)))([^?#\\s]*)(\\?([^#\\s]*))?(#([^\\s]*))?");
					Pattern pattern1 = Pattern.compile("^[a-zA-Z0-9\\-\\.]+\\.(com|org|net|mil|edu|co.uk|be|ly|tk|COM|ORG|NET|MIL|EDU|CO.UK|TK|BE|LY)$*");
					Matcher matcher = pattern.matcher(event.getMessage());
					Matcher matcher1 = pattern1.matcher(event.getMessage());
					if (matcher.find() || matcher1.find()) {
						event.respond("That is a link, and you are not a Mod, so stop posting it");
						event.getChannel().send().message(".timeout " + event.getUser().getNick() + " 1");
					} else {

					}
				}
			}
		}

	}
}
