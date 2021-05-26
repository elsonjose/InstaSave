package instagram.video.downloader.story.saver.downloader.photo.repost.instasaver.Utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.Nullable;

import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class CookieUtils {

    private static final String TAG = "CookieUtils";

    public static final CookieManager COOKIE_MANAGER = CookieManager.getInstance();

       @Nullable
    private static String getCookieValue(final String cookies, final String name) {
        if (cookies == null) return null;
        final Pattern pattern = Pattern.compile(name + "=(.+?);");
        final Matcher matcher = pattern.matcher(cookies);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getUserIdFromCookie(final String cookies) {
        final String dsUserId = getCookieValue(cookies, "ds_user_id");
        if (dsUserId == null) {
            return "";
        }
        return dsUserId;
    }

    @Nullable
    public static String getCookie(@Nullable final String webViewUrl) {
        final List<String> domains = new ArrayList<>(Arrays.asList(
                "https://instagram.com",
                "https://instagram.com/",
                "http://instagram.com",
                "http://instagram.com",
                "https://www.instagram.com",
                "https://www.instagram.com/",
                "http://www.instagram.com",
                "http://www.instagram.com/"
        ));
        if (!TextUtils.isEmpty(webViewUrl)) {
            domains.add(0, webViewUrl);
        }

        return getLongestCookie(domains);
    }

    @Nullable
    private static String getLongestCookie(final List<String> domains) {
        int longestLength = 0;
        String longestCookie = null;

        for (final String domain : domains) {
            final String cookie = COOKIE_MANAGER.getCookie(domain);
            if (cookie != null) {
                final int cookieLength = cookie.length();
                if (cookieLength > longestLength) {
                    longestCookie = cookie;
                    longestLength = cookieLength;
                }
            }
        }

        return longestCookie;
    }
}
