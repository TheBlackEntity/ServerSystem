package me.entity303.serversystem.bstats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.entity303.serversystem.main.ServerSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * bStats collects some data for plugin authors.
 * <p>
 * Check out https://bStats.org/ to learn more about bStats!
 */
public class MetricsLite {
    // The version of this bStats class
    public static final int B_STATS_VERSION = 1;
    // The url to which the data is sent
    private static final String URL = "https://bStats.org/submitData/bukkit";
    // Should failed requests be logged?
    private static boolean logFailedRequests;
    // Should the sent data be logged?
    private static boolean logSentData;
    // Should the response text be logged?
    private static boolean logResponseStatusText;
    // The uuid of the server
    private static String serverUUID;

    static {
        // You can use the property to disable the check in your test environment
        if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
            // Maven's Relocate is clever and changes strings, too. So we have to use this little "trick" ... :D
            final String defaultPackage = new String(
                    new byte[]{'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's', '.', 'b', 'u', 'k', 'k', 'i', 't'});
            final String examplePackage = new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
            // We want to make sure nobody just copy & pastes the example and use the wrong package names
            if (MetricsLite.class.getPackage().getName().equals(defaultPackage) || MetricsLite.class.getPackage().getName().equals(examplePackage))
                throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
        }
    }

    // The plugin
    private final ServerSystem plugin;
    // The plugin id
    private final int pluginId;
    // Is bStats enabled on this server?
    private final boolean enabled;

    /**
     * Class constructor.
     *
     * @param plugin   The plugin which stats should be submitted.
     * @param pluginId The id of the plugin.
     *                 It can be found at <a href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public MetricsLite(ServerSystem plugin, int pluginId) {
        if (plugin == null) throw new IllegalArgumentException("Plugin cannot be null!");
        this.plugin = plugin;
        this.pluginId = pluginId;

        // Get the config file
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Check if the config file exists
        if (!config.isSet("serverUuid")) {

            // Add default values
            config.addDefault("enabled", true);
            // Every server gets it's unique random id.
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            // Should failed request be logged?
            config.addDefault("logFailedRequests", false);
            // Should the sent data be logged?
            config.addDefault("logSentData", false);
            // Should the response text be logged?
            config.addDefault("logResponseStatusText", false);

            // Inform the server owners about bStats
            config.options().header(
                    "bStats collects some data for plugin authors like how many servers are using their plugins.\n" +
                            "To honor their work, you should not disable it.\n" +
                            "This has nearly no effect on the server performance!\n" +
                            "Check out https://bStats.org/ to learn more :)"
            ).copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) {
            }
        }

        // Load the data
        MetricsLite.serverUUID = config.getString("serverUuid");
        MetricsLite.logFailedRequests = config.getBoolean("logFailedRequests", false);
        this.enabled = config.getBoolean("enabled", true);
        MetricsLite.logSentData = config.getBoolean("logSentData", false);
        MetricsLite.logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        if (this.enabled) {
            boolean found = false;
            // Search for all other bStats Metrics classes to see if we are the first one
            for (Class<?> service : Bukkit.getServicesManager().getKnownServices())
                try {
                    service.getField("B_STATS_VERSION"); // Our identifier :)
                    found = true; // We aren't the first
                    break;
                } catch (NoSuchFieldException ignored) {
                }
            // Register our service
            Bukkit.getServicesManager().register(MetricsLite.class, this, plugin, ServicePriority.Normal);
            // We are the first!
            if (!found) this.startSubmitting();
        }
    }

    /**
     * Sends the data to the bStats server.
     *
     * @param plugin Any plugin. It's just used to get a logger instance.
     * @param data   The data to send.
     * @throws Exception If the request failed.
     */
    private static void sendData(ServerSystem plugin, JsonObject data) throws Exception {
        if (data == null) throw new IllegalArgumentException("Data cannot be null!");
        if (Bukkit.isPrimaryThread())
            throw new IllegalAccessException("This method must not be called from the main thread!");
        if (MetricsLite.logSentData) plugin.log("Sending data to bStats: " + data);
        HttpsURLConnection connection = (HttpsURLConnection) new URL(MetricsLite.URL).openConnection();

        // Compress the data to save bandwidth
        byte[] compressedData = MetricsLite.compress(data.toString());

        // Add headers
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip our request
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json"); // We send our data in JSON format
        connection.setRequestProperty("User-Agent", "MC-Server/" + MetricsLite.B_STATS_VERSION);

        // Send data
        connection.setDoOutput(true);
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(compressedData);
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) builder.append(line);
        } catch (IOException e) {
            if (!e.getMessage().toLowerCase().contains("code: 429")) {
                e.printStackTrace();
                return;
            }
        }

        if (MetricsLite.logResponseStatusText) plugin.log("Sent data to bStats and received response: " + builder);
    }

    /**
     * Gzips the given String.
     *
     * @param str The string to gzip.
     * @return The gzipped String.
     * @throws IOException If the compression failed.
     */
    private static byte[] compress(final String str) throws IOException {
        if (str == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
            gzip.write(str.getBytes(StandardCharsets.UTF_8));
        }
        return outputStream.toByteArray();
    }

    /**
     * Checks if bStats is enabled.
     *
     * @return Whether bStats is enabled or not.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Starts the Scheduler which submits our data every 30 minutes.
     */
    private void startSubmitting() {
        final Timer timer = new Timer(true); // We use a timer cause the Bukkit scheduler is affected by server lags
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!MetricsLite.this.plugin.isEnabled()) { // Plugin was disabled
                    timer.cancel();
                    return;
                }
                // Nevertheless we want our code to run in the Bukkit main thread, so we have to use the Bukkit scheduler
                // Don't be afraid! The connection to the bStats server is still async, only the stats collection is sync ;)
                Bukkit.getScheduler().runTask(MetricsLite.this.plugin, MetricsLite.this::submitData);
            }
        }, 1000 * 60 * 5, 1000 * 60 * 30);
        // Submit the data every 30 minutes, first time after 5 minutes to give other plugins enough time to start
        // WARNING: Changing the frequency has no effect but your plugin WILL be blocked/deleted!
        // WARNING: Just don't do it!
    }

    /**
     * Gets the plugin specific data.
     * This method is called using Reflection.
     *
     * @return The plugin specific data.
     */
    public JsonObject getPluginData() {
        JsonObject data = new JsonObject();

        String pluginName = this.plugin.getDescription().getName();
        String pluginVersion = this.plugin.getDescription().getVersion();

        data.addProperty("pluginName", pluginName); // Append the name of the plugin
        data.addProperty("id", this.pluginId); // Append the id of the plugin
        data.addProperty("pluginVersion", pluginVersion); // Append the version of the plugin
        data.add("customCharts", new JsonArray());

        return data;
    }

    /**
     * Gets the server specific data.
     *
     * @return The server specific data.
     */
    private JsonObject getServerData() {
        // Minecraft specific data
        int playerAmount;
        try {
            // Around MC 1.8 the return type was changed to a collection from an array,
            // This fixes java.lang.NoSuchMethodError: org.bukkit.Bukkit.getOnlinePlayers()Ljava/util/Collection;
            Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            playerAmount = onlinePlayersMethod.getReturnType().equals(Collection.class)
                    ? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).size()
                    : ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer())).length;
        } catch (Exception e) {
            playerAmount = Bukkit.getOnlinePlayers().size(); // Just use the new method if the Reflection failed
        }
        int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
        String bukkitVersion = Bukkit.getVersion();
        String bukkitName = Bukkit.getName();

        // OS/Java specific data
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        int coreCount = Runtime.getRuntime().availableProcessors();

        JsonObject data = new JsonObject();

        data.addProperty("serverUUID", MetricsLite.serverUUID);

        data.addProperty("playerAmount", playerAmount);
        data.addProperty("onlineMode", onlineMode);
        data.addProperty("bukkitVersion", bukkitVersion);
        data.addProperty("bukkitName", bukkitName);

        data.addProperty("javaVersion", javaVersion);
        data.addProperty("osName", osName);
        data.addProperty("osArch", osArch);
        data.addProperty("osVersion", osVersion);
        data.addProperty("coreCount", coreCount);

        return data;
    }

    /**
     * Collects the data and sends it afterwards.
     */
    private void submitData() {
        final JsonObject data = this.getServerData();

        JsonArray pluginData = new JsonArray();
        // Search for all other bStats Metrics classes to get their plugin data
        for (Class<?> service : Bukkit.getServicesManager().getKnownServices())
            try {
                service.getField("B_STATS_VERSION"); // Our identifier :)

                for (RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(service))
                    try {
                        Object plugin = provider.getService().getMethod("getPluginData").invoke(provider.getProvider());
                        // old bstats version compatibility
                        if (plugin instanceof JsonObject) pluginData.add((JsonObject) plugin);
                        else try {
                            Class<?> jsonObjectJsonSimple = Class.forName("org.json.simple.JSONObject");
                            if (plugin.getClass().isAssignableFrom(jsonObjectJsonSimple)) {
                                Method jsonStringGetter = jsonObjectJsonSimple.getDeclaredMethod("toJSONString");
                                jsonStringGetter.setAccessible(true);
                                String jsonString = (String) jsonStringGetter.invoke(plugin);
                                JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();
                                pluginData.add(object);
                            }
                        } catch (ClassNotFoundException e) {
                            // minecraft version 1.14+
                            if (MetricsLite.logFailedRequests) {
                                this.plugin.error("Encountered unexpected exception: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    } catch (NullPointerException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                    }
            } catch (NoSuchFieldException ignored) {
            }

        data.add("plugins", pluginData);

        // Create a new thread for the connection to the bStats server
        new Thread(() -> {
            try {
                // Send the data
                MetricsLite.sendData(this.plugin, data);
            } catch (Exception e) {
                // Something went wrong! :(
                if (MetricsLite.logFailedRequests) {
                    this.plugin.error("Could not submit plugin stats of " + this.plugin.getName());
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
