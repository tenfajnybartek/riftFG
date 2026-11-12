package net.dzikoysk.funnyguilds.telemetry;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import panda.std.Option;
import panda.utilities.IOUtils;

/**
 * TODO: Move this to a separate library.
 */
public final class FunnyTelemetry {

    private FunnyTelemetry() {
    }

    private static final Gson gson = new Gson();
    public static final String URL = "https://funnytelemetry.dzikoysk.net";
    public static final String FUNNYBIN_POST = URL + "/funnybin/api/post";
    public static final String FUNNYBIN_POST_BUNDLE = URL + "/funnybin/api/bundle/post";

    public static FunnybinResponse postToFunnybin(String paste, PasteType pasteType, String tag) throws IOException {
        return sendPost(FUNNYBIN_POST + "?type=" + pasteType + "&tag=" + encodeUTF8(tag), paste, FunnybinResponse.class);
    }

    public static Option<FunnybinResponse> createBundle(List<String> pastes) throws IOException {
        if (pastes.isEmpty()) {
            return Option.none();
        }

        Iterator<String> iterator = pastes.iterator();
        StringBuilder url = new StringBuilder(createQueryElement("paste", iterator.next()));

        while (iterator.hasNext()) {
            url.append("&");
            addQueryElement("paste", iterator.next(), url);
        }

        return Option.of(sendPost(FUNNYBIN_POST_BUNDLE + "?" + url, "", FunnybinResponse.class));
    }

    private static <T> T sendPost(String url, String body, Class<T> response) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        if (bodyBytes.length > 8 * 1024 * 1024) {
            throw new IOException("Paste exceeds 8 MiB");
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", "FunnyGuilds");
            connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            connection.setFixedLengthStreamingMode(bodyBytes.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(bodyBytes);
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("FunnyTelemetry returned HTTP " + status);
            }
            try (InputStream input = connection.getInputStream()) {
                byte[] bytes = input.readNBytes(1024 * 1024 + 1);
                if (bytes.length > 1024 * 1024) { throw new IOException("Telemetry response exceeds 1 MiB"); }
                T result = gson.fromJson(new String(bytes, StandardCharsets.UTF_8), response);
                if (result == null) { throw new IOException("Empty telemetry response"); }
                return result;
            }
            catch (com.google.gson.JsonParseException exception) {
                throw new IOException("Invalid telemetry response", exception);
            }
        }
        finally {
            connection.disconnect();
        }
    }

    private static String encodeUTF8(String str) throws UnsupportedEncodingException {
        if (str == null) {
            return "";
        }

        return URLEncoder.encode(str, "UTF-8");
    }

    private static String createQueryElement(String key, String value) throws UnsupportedEncodingException {
        String result = encodeUTF8(key);
        if (value != null) {
            result += "=" + encodeUTF8(value);
        }

        return result;
    }

    private static void addQueryElement(String key, String value, StringBuilder builder) throws UnsupportedEncodingException {
        builder.append(encodeUTF8(key));

        if (value != null) {
            builder.append('=');
            builder.append(encodeUTF8(value));
        }
    }

}
